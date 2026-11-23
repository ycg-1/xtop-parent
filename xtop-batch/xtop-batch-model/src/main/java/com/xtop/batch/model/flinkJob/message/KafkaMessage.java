package com.xtop.batch.model.flinkJob.message;

/**
 * @Description:
 * @Since: 2026/11/23
 * @Author: 杨春光
 */



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Kafka 消息包装类，携带元数据
 * @param <T> 消息体类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KafkaMessage<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 消息体 */
    private T value;

    /** topic */
    private String topic;

    /** 分区 */
    private int partition;

    /** 偏移量 */
    private long offset;

    /** 消息时间戳 */
    private long timestamp;
}
