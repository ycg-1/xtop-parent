package com.xtop.batch.service.flink.serialization;

/**
 * @Description:
 * @Since: 2026/11/23
 * @Author: 杨春光
 */
import com.alibaba.fastjson2.JSON;

/**
 * JSON 消息体反序列化器
 * 用法：new JsonKafkaMessageDeserializer<>(OrderDTO.class)
 */
public class JsonKafkaMessageDeserializer<T> extends AbstractKafkaMessageDeserializer<T> {

    private static final long serialVersionUID = 1L;

    public JsonKafkaMessageDeserializer(Class<T> clazz) {
        super(clazz);
    }

    @Override
    protected T parseValue(String raw) {
        if (raw == null || raw.isEmpty()) {
            return null;
        }
        return JSON.parseObject(raw, valueClass);
    }
}
