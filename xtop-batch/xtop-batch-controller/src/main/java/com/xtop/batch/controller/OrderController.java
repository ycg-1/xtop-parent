package com.xtop.batch.controller;

/**
 * @Description:
 * @Since: 2026/11/16
 * @Author: 杨春光
 */
import com.xtop.batch.service.mq.OrderProducer;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
public class OrderController {

    private final OrderProducer orderProducer;

    public OrderController(OrderProducer orderProducer) {
        this.orderProducer = orderProducer;
    }

    @PostMapping("/send")
    public String send(@RequestParam String orderId, @RequestParam String payload) {
        orderProducer.send(orderId, payload);
        return "消息已发送, orderId: " + orderId;
    }
}
