package com.xtop.batch.service.flink.manager;




import com.xtop.batch.dao.mapper.flinkJob.FlinkJobConfigMapper;
import com.xtop.batch.model.flinkJob.entity.FlinkJobConfig;
import com.xtop.batch.model.flinkJob.message.KafkaMessage;
import com.xtop.batch.service.flink.config.FlinkKafkaProperties;
import com.xtop.batch.service.flink.function.StrategyProcessFunction;
import com.xtop.batch.service.flink.registry.StrategyRegistry;
import com.xtop.batch.service.flink.serialization.DeserializerFactory;
import com.xtop.batch.service.flink.strategy.KafkaConsumerStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.JobID;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.client.program.rest.RestClusterClient;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.PipelineOptions;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.flink.core.execution.SavepointFormatType;
import org.apache.flink.runtime.jobgraph.JobGraph;
import org.apache.flink.runtime.jobgraph.SavepointRestoreSettings;

import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.environment.CheckpointConfig;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.graph.StreamGraph;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlinkJobManager {

    private final FlinkKafkaProperties props;
    private final StrategyRegistry registry;
    private final DeserializerFactory deserializerFactory;
    private final FlinkJobConfigMapper mapper;
    private final RestClusterClient<String> restClusterClient;

    /** 本地缓存 jobName -> JobID */
    private final Map<String, JobID> localJobs = new ConcurrentHashMap<>();

    /**
     * 启动作业：读 DB 配置 -> 构建 Flink 作业 -> 提交集群 -> 回写 status=1
     */
    @Transactional(rollbackFor = Exception.class)
    public String start(String jobName) throws Exception {
        FlinkJobConfig cfg = mapper.getByName(jobName);
        if (cfg == null) {
            throw new IllegalArgumentException("作业不存在: " + jobName);
        }
        if (cfg.getStatus() != null && cfg.getStatus() == 1) {
            throw new IllegalStateException("作业已在运行: " + jobName);
        }

        // 1. 获取策略
        KafkaConsumerStrategy strategy = registry.get(cfg.getHandlerBean());

        // 2. 构建环境（Flink 1.19：通过 Configuration 设置作业名）
        Configuration conf = new Configuration();
        conf.set(PipelineOptions.NAME, "kafka-" + jobName);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment(conf);

        env.setParallelism(cfg.getParallelism() != null
                ? cfg.getParallelism()
                : props.getDefaultParallelism());

        // 3. checkpoint 配置（Flink 1.19 API）
        env.enableCheckpointing(props.getCheckpointInterval(), CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(10000);
        env.getCheckpointConfig().setCheckpointTimeout(600000);
        env.getCheckpointConfig().setMaxConcurrentCheckpoints(1);
        env.getCheckpointConfig().setExternalizedCheckpointCleanup(
                CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);

        // 4. Kafka Source（反序列化器由工厂根据 DB 配置动态创建）
        String servers = (cfg.getBootstrapServers() != null && !cfg.getBootstrapServers().isEmpty()
                ? cfg.getBootstrapServers()
                : props.getBootstrapServers());

        OffsetsInitializer offsets = "latest".equalsIgnoreCase(cfg.getAutoOffsetReset())
                ? OffsetsInitializer.latest()
                : OffsetsInitializer.earliest();

        KafkaRecordDeserializationSchema<KafkaMessage<?>> deserializer =
                deserializerFactory.create(cfg);

        KafkaSource<KafkaMessage<?>> source = KafkaSource.<KafkaMessage<?>>builder()
                .setBootstrapServers(servers)
                .setTopics(cfg.getTopic())
                .setGroupId(cfg.getGroupId())
                .setStartingOffsets(offsets)
                .setDeserializer(deserializer)
                .setProperty("partition.discovery.interval.ms",
                        String.valueOf(props.getPartitionDiscoveryInterval()))
                .build();

        env.fromSource(source, WatermarkStrategy.noWatermarks(), "KafkaSource-" + cfg.getTopic())
                .process(new StrategyProcessFunction(strategy));

        // 5. 构建 JobGraph
        StreamGraph streamGraph = env.getStreamGraph();
        JobGraph jobGraph = streamGraph.getJobGraph();

        // 从 savepoint 恢复
        if (cfg.getSavepointPath() != null && !cfg.getSavepointPath().isEmpty()) {
            jobGraph.setSavepointRestoreSettings(
                    SavepointRestoreSettings.forPath(cfg.getSavepointPath(), false));
            log.info("从 savepoint 恢复: {}", cfg.getSavepointPath());
        }

        // 6. 提交到集群
        JobID jobID = restClusterClient.submitJob(jobGraph).get();
        localJobs.put(jobName, jobID);

        // 7. 回写 DB
        mapper.updateStatus(cfg.getId(), 1, jobID.toString(), cfg.getSavepointPath());
        log.info("作业启动成功 name={} jobId={}", jobName, jobID);
        return jobID.toString();
    }

    /**
     * 停止作业：触发 savepoint -> cancel -> 回写 status=0
     */
    /**
     * 停止作业：触发 savepoint -> cancel -> 回写 status=0
     */
    /**
     * 停止作业：触发 savepoint 并取消作业 -> 回写 status=0
     */
    @Transactional(rollbackFor = Exception.class)
    public void stop(String jobName, boolean withSavepoint) throws Exception {
        FlinkJobConfig cfg = mapper.getByName(jobName);
        if (cfg == null) {
            throw new IllegalArgumentException("作业不存在: " + jobName);
        }
        if (cfg.getStatus() == null || cfg.getStatus() != 1) {
            throw new IllegalStateException("作业未运行: " + jobName);
        }

        JobID jobID = localJobs.getOrDefault(jobName, JobID.fromHexString(cfg.getJobId()));
        String savepointPath = cfg.getSavepointPath();

        if (withSavepoint) {
            // 触发 savepoint 并取消作业，返回 savepoint 路径
            savepointPath = restClusterClient
                    .cancelWithSavepoint(jobID, null,  SavepointFormatType.CANONICAL)
                    .get(120, TimeUnit.SECONDS);
            log.info("savepoint 完成 jobName={} path={}", jobName, savepointPath);
        } else {
            // 不带 savepoint，直接 cancel
            restClusterClient.cancel(jobID).get();
        }

        localJobs.remove(jobName);
        mapper.updateStatus(cfg.getId(), 0, cfg.getJobId(), savepointPath);
        log.info("作业已停止 name={} savepoint={}", jobName, savepointPath);
    }

    /**
     * 对账：把 DB 里 status=1 但集群已不存在的作业标记为异常(2)
     */
    public void reconcile() {
        List<FlinkJobConfig> running = mapper.listRunning();
        for (FlinkJobConfig cfg : running) {
            if (cfg.getJobId() == null || cfg.getJobId().isEmpty()) {
                continue;
            }
            try {
                JobID jobID = JobID.fromHexString(cfg.getJobId());
                restClusterClient.getJobStatus(jobID).get();
            } catch (Exception e) {
                log.warn("作业已失联 name={} jobId={}", cfg.getJobName(), cfg.getJobId());
                mapper.updateStatus(cfg.getId(), 2, cfg.getJobId(), cfg.getSavepointPath());
            }
        }
    }
}