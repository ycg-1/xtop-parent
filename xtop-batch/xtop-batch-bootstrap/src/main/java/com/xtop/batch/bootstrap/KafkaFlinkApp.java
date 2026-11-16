package com.xtop.batch.bootstrap;



import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.time.Duration;

public class KafkaFlinkApp {
    public static void main(String[] args) throws Exception {
        // 1. 获取流式执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 允许在IDEA里并行执行，本地调试更方便
        env.setParallelism(1);

        // 2. 开启 Checkpoint
        env.enableCheckpointing(30000);

        // 3. 构建 Kafka Source 源 (已修改为正确的API)
        KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
                // 替换为你刚才在截图里看到的 Kafka IP
                .setBootstrapServers("192.168.150.150:9092")
                // 替换为你刚才在截图里看到的业务 Topic
                .setTopics("order.create.topic")
                // 建议设置一个唯一的消费组ID
                .setGroupId("flink-test-group-001")
                // 从最新消息开始消费，防止历史数据太多刷屏
                .setStartingOffsets(OffsetsInitializer.latest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        // 4. 转化为数据流
        DataStream<String> kafkaStream = env
                .fromSource(kafkaSource, WatermarkStrategy.forBoundedOutOfOrderness(Duration.ZERO), "Kafka Source")
                .uid("kafka-source-uid");

        // 5. 打印消费结果
        kafkaStream.print("接收到业务消息 -> ");

        // 6. 执行任务
        env.execute("Flink 业务消息实时消费");
    }
}