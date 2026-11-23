package com.xtop.batch.service.flink.strategy;


import com.xtop.batch.model.flinkJob.message.KafkaMessage;

/**
 * Kafka 消费策略接口
 * 实现类用 @Component("xxxHandler") 注册，Bean 名写进数据库 handler_bean 字段
 *
 * 注意：策略统一接收 KafkaMessage<?>，业务侧自行判断 value 类型
 */
public interface KafkaConsumerStrategy {

    /**
     * 处理消息
     */
    void handle(KafkaMessage<?> message);
}