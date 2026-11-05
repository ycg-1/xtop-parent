package com.xtop.batch.controller.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description:
 * @Since: 2026/11/5
 * @Author: 杨春光
 */
@RestController
@RequestMapping("/kafka")
public class KafkaTestController {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @GetMapping("/send")
    public String send(@RequestParam String msg) {
        kafkaTemplate.send("order.create.topic", "test-key", msg);
        return "消息已发送: " + msg;
    }
}
