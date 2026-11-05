package com.xtop.batch.controller.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * @Description:
 * @Since: 2026/11/5
 * @Author: 杨春光
 */
@Component
@Slf4j
public class OrderKafkaConsumer {

    @KafkaListener(topics = "order.create.topic",
            groupId = "order-consumer-group")
    public void onOrderCreate(ConsumerRecord<String, String> record,
                              Acknowledgment ack) {
        log.info("收到消息 key={}, value={}, partition={}, offset={}",
                record.key(), record.value(),
                record.partition(), record.offset());
        ack.acknowledge();
    }
}