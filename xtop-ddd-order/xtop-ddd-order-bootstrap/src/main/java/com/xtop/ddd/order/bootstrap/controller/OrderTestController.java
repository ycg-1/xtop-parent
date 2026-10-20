package com.xtop.ddd.order.bootstrap.controller;

import com.xtop.common.feign.context.FeignInvokeContext;
import com.xtop.common.feign.enums.InvokeMode;
import com.xtop.common.feign.result.FeignInvokeResult;
import com.xtop.common.model.Result;
import com.xtop.ddd.order.api.dto.CreateOrderRequest;
import com.xtop.ddd.order.api.dto.CreateOrderResponse;
import com.xtop.ddd.order.application.service.OrderApplicationService;
import com.xtop.infra.service.filter.TraceConst;
import com.xtop.infra.service.feign.invoker.DynamicFeignInvoker;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@Tag(name = "订单控制层")
@RequestMapping("/api/order")
public class OrderTestController {

    @Autowired
    private OrderApplicationService orderApplicationService;

    @Autowired
    private DynamicFeignInvoker dynamicFeignInvoker;

    /**
     * 测试接口1：强类型契约模式调用
     * 验证：Application层编排 + Infra强类型引擎调用
     */
    @Operation(summary = "create-typed")
    @PostMapping("/create-typed")
    public Result<CreateOrderResponse> createOrderTyped(@RequestBody CreateOrderRequest request) {
        log.info("收到强类型创建订单请求: {}", request);
        MDC.put(TraceConst.MDC_FLOW_NO, "12345674");

        CreateOrderResponse response = orderApplicationService.handleCreateOrder(request);
        return Result.success(response);
    }

    /**
     * 测试接口2：字典驱动动态字段模式调用 (直接调用 Infra 引擎)
     */
    @Operation(summary = "create-dynamic")
    @PostMapping("/create-dynamic")
    public Result<Object> createOrderDynamic(@RequestBody Map<String, Object> request) {
        log.info("收到字典动态调用请求: {}", request);

        FeignInvokeContext ctx = new FeignInvokeContext();
        ctx.setMode(InvokeMode.DICT_DRIVEN);
        ctx.setRouteCode("EXT_ORDER_CREATE");
        ctx.setDynamicFields(request);

        FeignInvokeResult<Map<String, Object>> result = dynamicFeignInvoker.invoke(ctx);

        if (result.isSuccess()) {
            return Result.success(result.getData());
        } else {
            return Result.error("动态调用失败: " + result.getErrorMsg());
        }
    }
}