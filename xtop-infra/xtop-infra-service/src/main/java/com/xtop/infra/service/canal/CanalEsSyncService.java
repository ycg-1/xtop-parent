package com.xtop.infra.service.canal;

import com.alibaba.fastjson2.JSON;
import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class CanalEsSyncService {

    @Value("${canal.host}")
    private String canalHost;

    @Value("${canal.port}")
    private int canalPort;

    @Value("${canal.destination}")
    private String canalDestination;

    @Value("${canal.username}")
    private String canalUsername;

    @Value("${canal.password}")
    private String canalPassword;

    @Autowired
    private RestHighLevelClient esClient;

    @Autowired
    private DataSource dataSource;

    // TODO: Redis分布式锁 - 接入Redis后取消注释
    // @Autowired
    // private org.springframework.data.redis.core.StringRedisTemplate redisTemplate;

    private CanalConnector connector;
    private final Map<String, SyncConfig> syncConfigMap = new ConcurrentHashMap<>();

    private final BlockingQueue<CanalIncrementEvent> incrementalBuffer = new LinkedBlockingQueue<>(100_000);
    private final AtomicBoolean fullSyncFinished = new AtomicBoolean(false);

    @PostConstruct
    public void init() {
        loadSyncConfig();

        connector = CanalConnectors.newSingleConnector(
                new InetSocketAddress(canalHost, canalPort),
                canalDestination,
                canalUsername,
                canalPassword
        );

        new Thread(this::process, "canal-listener-thread").start();

        // TODO: Redis分布式锁 - 加锁后执行全量同步
        // String lockKey = "canal:fullsync:lock";
        // String lockValue = InetAddress.getLocalHost().getHostAddress() + ":" + System.currentTimeMillis();
        // Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, 1, TimeUnit.HOURS);
        // if (Boolean.TRUE.equals(acquired)) {
        //     try {
        //         syncFullData();
        //         replayIncrementalAfterFullSync();
        //     } finally {
        //         String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        //         redisTemplate.execute(
        //             new org.springframework.data.redis.core.script.DefaultRedisScript<>(script, Long.class),
        //             Collections.singletonList(lockKey), lockValue
        //         );
        //     }
        // } else {
        //     log.info("其他实例正在执行全量同步，本实例跳过全量，仅做增量");
        // }

        // 当前无Redis版本
        syncFullData();
        replayIncrementalAfterFullSync();

        fullSyncFinished.set(true);
    }

    private void process() {
        try {
            connector.connect();
            connector.subscribe(".*\\..*");
            log.info("Canal 客户端启动成功，当前监听表数量: {}", syncConfigMap.size());

            while (true) {
                Message message = connector.getWithoutAck(1000);
                long batchId = message.getId();

                if (batchId != -1 && !message.getEntries().isEmpty()) {
                    if (!fullSyncFinished.get()) {
                        bufferIncrementalEvents(message.getEntries());
                    } else {
                        handleEntries(message.getEntries());
                    }
                    connector.ack(batchId);
                } else {
                    Thread.sleep(1000);
                }
            }
        } catch (Exception e) {
            log.error("Canal 监听异常", e);
        } finally {
            connector.disconnect();
        }
    }

    private void bufferIncrementalEvents(List<CanalEntry.Entry> entries) {
        for (CanalEntry.Entry entry : entries) {
            if (entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONBEGIN

                    || entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONEND) {
                continue;
            }

            String schema = entry.getHeader().getSchemaName();
            String table = entry.getHeader().getTableName();
            String configKey = schema + "." + table;

            SyncConfig config = syncConfigMap.get(configKey);
            if (config == null) {
                continue;
            }

            try {
                CanalEntry.RowChange rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
                CanalEntry.EventType eventType = rowChange.getEventType();

                for (CanalEntry.RowData rowData : rowChange.getRowDatasList()) {
                    CanalIncrementEvent event = new CanalIncrementEvent();
                    event.setSchema(schema);
                    event.setTable(table);
                    event.setConfig(config);
                    event.setEventType(eventType);
                    event.setRowData(rowData);
                    incrementalBuffer.offer(event);
                }
            } catch (Exception e) {
                log.error("解析 binlog 异常，缓冲失败", e);
            }
        }
    }

    private void handleEntries(List<CanalEntry.Entry> entries) {
        for (CanalEntry.Entry entry : entries) {
            if (entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONBEGIN

                    || entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONEND) {
                continue;
            }

            CanalEntry.RowChange rowChange;
            try {
                rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
            } catch (Exception e) {
                log.error("解析 binlog 异常", e);
                continue;
            }

            String schema = entry.getHeader().getSchemaName();
            String table = entry.getHeader().getTableName();
            String configKey = schema + "." + table;
            SyncConfig config = syncConfigMap.get(configKey);
            if (config == null) {
                continue;
            }

            CanalEntry.EventType eventType = rowChange.getEventType();
            for (CanalEntry.RowData rowData : rowChange.getRowDatasList()) {
                switch (eventType) {
                    case INSERT:
                    case UPDATE:
                        upsertToEs(rowData.getAfterColumnsList(), config);
                        break;
                    case DELETE:
                        deleteFromEs(rowData.getBeforeColumnsList(), config);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private void upsertToEs(List<CanalEntry.Column> columns, SyncConfig config) {
        Map<String, Object> doc = new LinkedHashMap<>();
        String id = null;
        for (CanalEntry.Column column : columns) {
            if (config.getIdColumn().equalsIgnoreCase(column.getName())) {
                id = column.getValue();
            }
            doc.put(column.getName(), column.getValue());
        }
        if (id == null) {
            return;
        }

        try {
            RestClient lowLevel = esClient.getLowLevelClient();
            Request request = new Request("PUT", "/" + config.getEsIndex() + "/_doc/" + id);
            request.addParameter("timeout", "1m");
            request.setJsonEntity(JSON.toJSONString(doc));

            Response response = lowLevel.performRequest(request);
            int status = response.getStatusLine().getStatusCode();

            if (status == 200 || status == 201) {
                String body = EntityUtils.toString(response.getEntity());
                log.info("ES Upsert 成功 | Index:{} | ID:{} | status:{} | body:{}",
                        config.getEsIndex(), id, status, body);
            } else {
                log.error("ES Upsert 异常 | Index:{} | ID:{} | status:{}",
                        config.getEsIndex(), id, status);
            }
        } catch (Exception e) {
            log.error("ES Upsert 失败 | Index:{} | ID:{}", config.getEsIndex(), id, e);
        }
    }

    private void deleteFromEs(List<CanalEntry.Column> columns, SyncConfig config) {
        String id = null;
        for (CanalEntry.Column column : columns) {
            if (config.getIdColumn().equalsIgnoreCase(column.getName())) {
                id = column.getValue();
                break;
            }
        }
        if (id == null) {
            return;
        }

        try {
            RestClient lowLevel = esClient.getLowLevelClient();
            Request request = new Request("DELETE", "/" + config.getEsIndex() + "/_doc/" + id);
            request.addParameter("timeout", "1m");

            Response response = lowLevel.performRequest(request);
            int status = response.getStatusLine().getStatusCode();

            if (status == 200 || status == 404) {
                log.info("ES Delete 成功 | Index:{} | ID:{} | status:{}", config.getEsIndex(), id, status);
            } else {
                log.error("ES Delete 异常 | Index:{} | ID:{} | status:{}", config.getEsIndex(), id, status);
            }
        } catch (Exception e) {
            log.error("ES Delete 失败 | Index:{} | ID:{}", config.getEsIndex(), id, e);
        }
    }

    private void syncFullData() {
        if (syncConfigMap.isEmpty()) {
            log.warn("没有可用的 ES 同步配置，跳过全量同步");
            return;
        }

        for (Map.Entry<String, SyncConfig> entry : syncConfigMap.entrySet()) {
            String configKey = entry.getKey();
            SyncConfig config = entry.getValue();

            String[] parts = configKey.split("\\.", 2);
            if (parts.length != 2) {
                continue;
            }

            String schema = parts[0];
            String table = parts[1];
            String idColumn = config.getIdColumn();

            log.info("开始全量同步 | schema:{} | table:{} | index:{}", schema, table, config.getEsIndex());

            int batchSize = 1000;
            long lastId = 0;
            int total = 0;

            while (true) {
                String sql = "SELECT * FROM " + schema + "." + table
                        + " WHERE " + idColumn + " > ? ORDER BY " + idColumn + " ASC LIMIT ?";

                try (Connection conn = dataSource.getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql)) {

                    ps.setLong(1, lastId);
                    ps.setInt(2, batchSize);

                    try (ResultSet rs = ps.executeQuery()) {
                        int count = 0;
                        long maxId = lastId;

                        while (rs.next()) {
                            Map<String, Object> doc = new LinkedHashMap<>();
                            java.sql.ResultSetMetaData metaData = rs.getMetaData();
                            int columnCount = metaData.getColumnCount();

                            for (int i = 1; i <= columnCount; i++) {
                                String columnName = metaData.getColumnName(i);
                                Object value = rs.getObject(i);
                                doc.put(columnName, value);
                            }

                            Object idValue = doc.get(idColumn);
                            if (idValue == null) {
                                continue;
                            }

                            String id = String.valueOf(idValue);
                            upsertToEs(doc, config, id);

                            if (idValue instanceof Number) {
                                long currentId = ((Number) idValue).longValue();
                                if (currentId > maxId) {
                                    maxId = currentId;
                                }
                            }

                            count++;
                        }

                        if (count == 0) {
                            break;
                        }

                        total += count;
                        lastId = maxId;
                    }
                } catch (SQLException e) {
                    log.error("全量同步异常 | schema:{} | table:{}", schema, table, e);
                    break;
                }
            }

            log.info("全量同步完成 | schema:{} | table:{} | index:{} | 同步条数:{}",
                    schema, table, config.getEsIndex(), total);
        }
    }

    private void upsertToEs(Map<String, Object> doc, SyncConfig config, String id) {
        try {
            RestClient lowLevel = esClient.getLowLevelClient();
            Request request = new Request("PUT", "/" + config.getEsIndex() + "/_doc/" + id);
            request.addParameter("timeout", "1m");
            request.setJsonEntity(JSON.toJSONString(doc));

            Response response = lowLevel.performRequest(request);
            int status = response.getStatusLine().getStatusCode();

            if (status != 200 && status != 201) {
                log.error("全量 ES Upsert 异常 | Index:{} | ID:{} | status:{}", config.getEsIndex(), id, status);
            }
        } catch (Exception e) {
            log.error("全量 ES Upsert 失败 | Index:{} | ID:{}", config.getEsIndex(), id, e);
        }
    }

    private void replayIncrementalAfterFullSync() {
        if (incrementalBuffer.isEmpty()) {
            return;
        }

        log.info("开始回放全量期间增量数据，缓冲条数: {}", incrementalBuffer.size());

        int count = 0;
        while (!incrementalBuffer.isEmpty()) {
            CanalIncrementEvent event = incrementalBuffer.poll();
            if (event == null) {
                break;
            }

            switch (event.getEventType()) {
                case INSERT:
                case UPDATE:
                    upsertToEs(event.getRowData().getAfterColumnsList(), event.getConfig());
                    break;
                case DELETE:
                    deleteFromEs(event.getRowData().getBeforeColumnsList(), event.getConfig());
                    break;
                default:
                    break;
            }

            count++;
        }

        log.info("全量期间增量回放完成，回放条数: {}", count);
    }

    @PreDestroy
    public void destroy() {
        if (connector != null) connector.disconnect();
    }

    private void loadSyncConfig() {
        syncConfigMap.clear();
        String sql = "SELECT schema_name, table_name, es_index, id_column FROM test.canal_sync_config WHERE is_active = 1";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String schema = rs.getString("schema_name");
                String table = rs.getString("table_name");
                SyncConfig config = new SyncConfig();
                config.setEsIndex(rs.getString("es_index"));
                config.setIdColumn(rs.getString("id_column"));
                syncConfigMap.put(schema + "." + table, config);
            }
            log.info("成功加载 {} 条 ES 同步配置", syncConfigMap.size());
        } catch (SQLException e) {
            log.error("加载 ES 同步配置失败", e);
        }
    }

    @Data
    private static class SyncConfig {
        private String esIndex;
        private String idColumn;
    }

    @Data
    private static class CanalIncrementEvent {
        private String schema;
        private String table;
        private SyncConfig config;
        private CanalEntry.EventType eventType;
        private CanalEntry.RowData rowData;
    }
}