package com.xtop.batch.service.flink.serialization;

/**
 * @Description:
 * @Since: 2026/11/23
 * @Author: 杨春光
 */
/**
 * String 消息体反序列化器
 */
public class StringKafkaMessageDeserializer extends AbstractKafkaMessageDeserializer<String> {

    private static final long serialVersionUID = 1L;

    public StringKafkaMessageDeserializer() {
        super(String.class);
    }

    @Override
    protected String parseValue(String raw) {
        return raw;
    }
}