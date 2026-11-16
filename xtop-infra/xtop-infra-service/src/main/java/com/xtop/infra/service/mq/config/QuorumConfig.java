package com.xtop.infra.service.mq.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuorumConfig {
    //  声明仲裁队列 + 死信队列

    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String ORDER_QUEUE = "order.quorum.queue";
    public static final String ORDER_DLX_EXCHANGE = "order.dlx.exchange";
    public static final String ORDER_DLQ = "order.dlq";

    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(ORDER_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange orderDlxExchange() {
        return new DirectExchange(ORDER_DLX_EXCHANGE, true, false);
    }

    @Bean
    public Queue orderDlq() {
        return QueueBuilder.durable(ORDER_DLQ)
                .withArgument("x-queue-type", "quorum")
                .build();
    }

    @Bean
    public Queue orderQuorumQueue() {
        return QueueBuilder.durable(ORDER_QUEUE)
                .quorum()
                .ttl(60000)
                .deadLetterExchange(ORDER_DLX_EXCHANGE)
                .deadLetterRoutingKey("dl")
                .build();
    }

    @Bean
    public Binding orderBinding(Queue orderQuorumQueue, DirectExchange orderExchange) {
        return BindingBuilder.bind(orderQuorumQueue).to(orderExchange).with("order.create");
    }

    @Bean
    public Binding orderDlqBinding(Queue orderDlq, DirectExchange orderDlxExchange) {
        return BindingBuilder.bind(orderDlq).to(orderDlxExchange).with("dl");
    }
}
