package com.xtop.ddd.user.bootstrap.controller;

/**
 * @Description:
 * @Since: 2026/10/12
 * @Author: 杨春光
 */
import com.alibaba.fastjson2.JSON;
import com.xtop.common.model.Result;
import com.xtop.ddd.user.api.dto.ReceiveOrderRequest;
import com.xtop.ddd.user.api.dto.ReceiveOrderResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserOrderController {

    @PostMapping("/order-receive")
    public Result<ReceiveOrderResponse> receiveOrder(@RequestBody ReceiveOrderRequest request) {
        log.info("User服务收到Feign订单请求: bizOrderNo={}, totalAmount={}", request.getBizOrderNo(), request.getTotalAmount());
        ReceiveOrderResponse response = new ReceiveOrderResponse();
        response.setExternalOrderId("EXT-" + System.currentTimeMillis());
        response.setOrderStatus("RECEIVED");
        response.setErrorMsg(null);
        log.info("User服务处理完成, externalOrderId={}", response.getExternalOrderId());
        return Result.success(response);
    }


    @PostMapping("/order-receive-batch")
    public Result<ReceiveOrderResponse> receiveOrderBatch(@RequestBody Map<String, Object> request) {
        log.info("User服务收到Feign订单请求:request={}, totalAmount={}", JSON.toJSONString(request));
        ReceiveOrderResponse response = new ReceiveOrderResponse();
        response.setExternalOrderId("EXT-" + System.currentTimeMillis());
        response.setOrderStatus("RECEIVED-BATCH");
        response.setErrorMsg(null);
        log.info("User服务处理完成, externalOrderId={}", response.getExternalOrderId());
        return Result.success(response);
    }
}
