package com.xtop.ddd.order.application.service;
import com.xtop.common.feign.context.FeignInvokeContext;
import com.xtop.common.feign.enums.InvokeMode;
import com.xtop.common.feign.result.FeignInvokeResult;
import com.xtop.ddd.order.api.dto.CreateOrderRequest;
import com.xtop.ddd.order.api.dto.CreateOrderResponse;
import com.xtop.infra.service.feign.invoker.DynamicFeignInvoker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;


@Slf4j
@Service
public class OrderApplicationService {

    @Autowired
     private DynamicFeignInvoker dynamicFeignInvoker;

    /**
     * 订单创建及同步应用服务
     */
  //  @Transactional
    public CreateOrderResponse handleCreateOrder(CreateOrderRequest request) {
        log.info("应用层开始处理订单创建, bizNo={}", request.getBizOrderNo());

        // 1. 组装上下文，调用 Infra 层的动态 Feign 引擎
        FeignInvokeContext ctx = new FeignInvokeContext();
        ctx.setMode(InvokeMode.TYPED_CONTRACT);
        ctx.setRouteCode("EXT_ORDER_CREATE");
        ctx.setRequestDto(request);
        ctx.setResponseType(CreateOrderResponse.class);

        // 2. 发起动态调用
        FeignInvokeResult<CreateOrderResponse> result = dynamicFeignInvoker.invoke(ctx);;

        // 3. 处理结果
        if (result.isSuccess()) {
            log.info("第三方订单同步成功, response={}", result.getData());
            // 在此处调用 Domain 层的领域服务，更新本地领域模型状态
            return result.getData();
        } else {
            log.error("第三方订单同步失败, error={}", result.getErrorMsg());
            throw new RuntimeException("订单同步失败: " + result.getErrorMsg());
        }
    }
}