package com.xtop.batch.service.mq;

import com.xtop.infra.service.mq.config.QuorumConfig;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class OrderProducer implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnCallback {

    private final RabbitTemplate rabbitTemplate;

    public OrderProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitTemplate.setConfirmCallback(this);
        this.rabbitTemplate.setReturnCallback(this);
    }

    public void send(String orderId, String payload) {
        String messageId = UUID.randomUUID().toString();
        CorrelationData correlationData = new CorrelationData(messageId);

        rabbitTemplate.convertAndSend(
                QuorumConfig.ORDER_EXCHANGE,
                "order.create",
                payload,
                msg -> {
                    msg.getMessageProperties().setMessageId(messageId);
                    msg.getMessageProperties().setHeader("orderId", orderId);
                    return msg;
                },
                correlationData
        );
    }

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        if (ack) {
            System.out.println("消息发送成功, messageId: " + correlationData.getId());
        } else {
            System.err.println("消息发送失败, messageId: " + correlationData.getId() + ", cause: " + cause);
        }
    }

    @Override
    public void returnedMessage(org.springframework.amqp.core.Message message, int replyCode, String replyText, String exchange, String routingKey) {
        System.err.println("消息被退回: " + new String(message.getBody()) + ", exchange: " + exchange + ", routingKey: " + routingKey);
    }
}