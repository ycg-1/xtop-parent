package com.xtop.batch.service.flink.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "flink.kafka")
public class FlinkKafkaProperties {

    /** 全局 Kafka 地址 */
    private String bootstrapServers;

    /** 默认并行度 */
    private int defaultParallelism = 3;

    /** 分区发现间隔（毫秒） */
    private int partitionDiscoveryInterval = 30000;

    /** checkpoint 间隔（毫秒） */
    private long checkpointInterval = 30000;
}
