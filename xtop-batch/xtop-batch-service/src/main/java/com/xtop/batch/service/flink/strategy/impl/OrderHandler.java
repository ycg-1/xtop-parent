package com.xtop.batch.service.flink.strategy.impl;
import com.xtop.batch.model.flinkJob.message.KafkaMessage;
import com.xtop.batch.service.flink.strategy.KafkaConsumerStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @Description:
 * @Since: 2026/11/23
 * @Author: 杨春光
 */


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("orderHandler")
public class OrderHandler implements KafkaConsumerStrategy {

    @Override
    public void handle(KafkaMessage<?> message) {
        log.info("订单消息 topic={} partition={} offset={} ts={} body={}",
                message.getTopic(), message.getPartition(),
                message.getOffset(), message.getTimestamp(), message.getValue());
        // TODO 业务逻辑
    }
}