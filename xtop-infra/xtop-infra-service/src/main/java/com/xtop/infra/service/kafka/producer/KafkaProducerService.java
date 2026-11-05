package com.xtop.infra.service.kafka.producer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Component
@Slf4j
public class KafkaProducerService {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendOrderMessage(String orderId, String orderJson) {
        ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send("order.create.topic", orderId, orderJson);

        future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
            @Override
            public void onFailure(Throwable ex) {
                // 消息发送失败，记录日志并报警，后续可存入数据库等待定时补偿
                log.error("Kafka 消息发送失败，订单ID:{}, 原因:{}", orderId, ex.getMessage(), ex);
            }

            @Override
            public void onSuccess(SendResult<String, String> result) {
                long offset = result.getRecordMetadata().offset();
                log.info("Kafka 消息发送成功，订单ID:{}, 偏移量:{}", orderId, offset);
            }
        });
    }
}