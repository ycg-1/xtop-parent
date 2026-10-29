package com.xtop.infra.service.canal;
import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.net.InetSocketAddress;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class CanalEsSyncService {

    // 【新增】从 yml 读取配置
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

    private CanalConnector connector;
    private final Map<String, SyncConfig> syncConfigMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        loadSyncConfig();

        // 【修改】使用 yml 配置来初始化 Canal 连接器
        connector = CanalConnectors.newSingleConnector(
                new InetSocketAddress(canalHost, canalPort),
                canalDestination,
                canalUsername,
                canalPassword
        );

        new Thread(this::process, "canal-listener-thread").start();
    }

    // 其他代码保持不变（process、handleEntries、upsertToEs 等）...

    private void process() {
        try {
            connector.connect();
            connector.subscribe(".*\\..*");
            log.info("Canal 客户端启动成功，当前监听表数量: {}", syncConfigMap.size());

            while (true) {
                Message message = connector.getWithoutAck(1000);
                long batchId = message.getId();

                if (batchId != -1 && !message.getEntries().isEmpty()) {
                    handleEntries(message.getEntries());
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

    private void handleEntries(List<CanalEntry.Entry> entries) {
        for (CanalEntry.Entry entry : entries) {
            if (entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONBEGIN ||
                    entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONEND) {
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
        if (id == null) return;

        try {
            IndexRequest request = new IndexRequest(config.getEsIndex()).id(id).source(doc, XContentType.JSON);
            esClient.index(request, RequestOptions.DEFAULT);
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
        if (id == null) return;

        try {
            esClient.delete(new DeleteRequest(config.getEsIndex(), id), RequestOptions.DEFAULT);
        } catch (Exception e) {
            log.error("ES Delete 失败 | Index:{} | ID:{}", config.getEsIndex(), id, e);
        }
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

    @lombok.Data
    private static class SyncConfig {
        private String esIndex;
        private String idColumn;
    }
}
