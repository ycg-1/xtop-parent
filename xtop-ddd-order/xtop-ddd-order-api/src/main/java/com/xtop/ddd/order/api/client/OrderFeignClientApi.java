package com.xtop.ddd.order.api.client;
import com.xtop.ddd.order.api.dto.CreateOrderRequest;
import com.xtop.ddd.order.api.dto.CreateOrderResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 订单服务 Feign 接口契约
 * 其他微服务调用本订单系统时，统一依赖此模块
 */
public interface OrderFeignClientApi {

    @PostMapping("/order/create")
    CreateOrderResponse createOrder(@RequestBody CreateOrderRequest request);
}
