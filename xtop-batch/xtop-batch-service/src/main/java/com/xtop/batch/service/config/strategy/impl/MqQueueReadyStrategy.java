package com.xtop.batch.service.config.strategy.impl;

import com.xtop.batch.service.config.strategy.PreCheckStrategy;
import lombok.extern.slf4j.Slf4j;

/**
 * 策略三：检查 MQ 队列消息是否就绪
 */
@Slf4j
public class MqQueueReadyStrategy implements PreCheckStrategy {

    private final String queueName;

    public MqQueueReadyStrategy(String queueName) {
        this.queueName = queueName;
    }

    @Override
    public void execute() {
        log.info("[MQ检查] 正在监听队列: {}", queueName);

        // TODO: 这里编写实际检查 MQ 的逻辑
        // 例如：通过 RabbitMQAdmin 检查队列中是否有积压消息
        // boolean hasMessage = rabbitAdmin.getMessageCount(queueName) > 0;

        // 模拟逻辑：如果 MQ 队列没就绪，直接抛出异常，阻断后续批处理
        // if (!hasMessage) {
        //     throw new RuntimeException("MQ队列 " + queueName + " 暂无消息，批处理任务取消");
        // }

        log.info("[MQ检查] 队列 [{}] 消息准备就绪！通过。", queueName);
    }
}