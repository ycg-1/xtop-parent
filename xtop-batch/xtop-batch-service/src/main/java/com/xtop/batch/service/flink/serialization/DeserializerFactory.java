package com.xtop.batch.service.flink.serialization;

/**
 * @Description:
 * @Since: 2026/11/23
 * @Author: 杨春光
 */




import com.xtop.batch.model.flinkJob.entity.FlinkJobConfig;
import com.xtop.batch.model.flinkJob.message.KafkaMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.springframework.stereotype.Component;

/**
 * 根据数据库配置动态创建反序列化器
 */
@Slf4j
@Component
public class DeserializerFactory {

    /**
     * 根据配置创建反序列化器
     * valueType=STRING -> StringKafkaMessageDeserializer
     * valueType=JSON   -> JsonKafkaMessageDeserializer（需要 valueClass）
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public KafkaRecordDeserializationSchema<KafkaMessage<?>> create(FlinkJobConfig cfg) {
        String valueType = cfg.getValueType() == null ? "STRING" : cfg.getValueType().toUpperCase();

        switch (valueType) {
            case "JSON":
                if (cfg.getValueClass() == null || cfg.getValueClass().isEmpty()) {
                    throw new IllegalArgumentException(
                            "valueType=JSON 时必须配置 value_class, jobName=" + cfg.getJobName());
                }
                try {
                    Class<?> clazz = Class.forName(cfg.getValueClass());
                    log.info("创建 JSON 反序列化器 jobName={} class={}", cfg.getJobName(), clazz.getName());
                    // 用 raw type 绕过泛型检查，运行时类型由 valueClass 保证
                    return (KafkaRecordDeserializationSchema) new JsonKafkaMessageDeserializer(clazz);
                } catch (ClassNotFoundException e) {
                    throw new IllegalArgumentException(
                            "value_class 类不存在: " + cfg.getValueClass(), e);
                }
            case "STRING":
            default:
                log.info("创建 String 反序列化器 jobName={}", cfg.getJobName());
                // 用 raw type 绕过泛型检查
                return (KafkaRecordDeserializationSchema) new StringKafkaMessageDeserializer();
        }
    }
}
