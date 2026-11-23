package com.xtop.batch.service.flink.serialization;



import com.xtop.batch.model.flinkJob.message.KafkaMessage;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.flink.util.Collector;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.nio.charset.StandardCharsets;

/**
 * 泛型 Kafka 反序列化器，把 partition/offset/timestamp 一起带出来
 * @param <T> 消息体类型
 */
public abstract class AbstractKafkaMessageDeserializer<T>
        implements KafkaRecordDeserializationSchema<KafkaMessage<T>> {

    private static final long serialVersionUID = 1L;

    /** 消息体类型，用于构造 TypeInformation */
    protected final Class<T> valueClass;

    protected AbstractKafkaMessageDeserializer(Class<T> valueClass) {
        this.valueClass = valueClass;
    }

    @Override
    public void deserialize(ConsumerRecord<byte[], byte[]> record,
                            Collector<KafkaMessage<T>> out) {
        String raw = record.value() == null
                ? null
                : new String(record.value(), StandardCharsets.UTF_8);

        T value = parseValue(raw);

        out.collect(new KafkaMessage<>(
                value,
                record.topic(),
                record.partition(),
                record.offset(),
                record.timestamp()
        ));
    }

    /**
     * 解析消息体，子类实现
     */
    protected abstract T parseValue(String raw);

    /**
     * 返回 KafkaMessage<T> 的 TypeInformation
     * 用 TypeInformation.of(Class) 构造外层，内层泛型由 Flink 自动推断为 PojoTypeInfo
     */
    /**
     * 返回 KafkaMessage<T> 的 TypeInformation
     */
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public TypeInformation<KafkaMessage<T>> getProducedType() {
        // 先拿到 raw TypeInformation，再强转，绕过泛型不变性检查
        return (TypeInformation<KafkaMessage<T>>) (TypeInformation) TypeInformation.of(KafkaMessage.class);
    }
}
