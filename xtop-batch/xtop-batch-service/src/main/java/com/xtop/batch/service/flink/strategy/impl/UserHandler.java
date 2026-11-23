package com.xtop.batch.service.flink.strategy.impl;

/**
 * @Description:
 * @Since: 2026/11/23
 * @Author: 杨春光
 */



import com.xtop.batch.model.flinkJob.message.KafkaMessage;
import com.xtop.batch.service.flink.strategy.KafkaConsumerStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("userHandler")
public class UserHandler implements KafkaConsumerStrategy {

    @Override
    public void handle(KafkaMessage<?> message) {
        log.info("用户消息 topic={} partition={} offset={} body={}",
                message.getTopic(), message.getPartition(),
                message.getOffset(), message.getValue());
        // TODO 业务逻辑
    }
}