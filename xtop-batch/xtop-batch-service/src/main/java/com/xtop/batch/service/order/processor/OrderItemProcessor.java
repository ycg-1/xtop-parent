package com.xtop.batch.service.order.processor;

import com.xtop.batch.model.OrderDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderItemProcessor implements ItemProcessor<OrderDTO, OrderDTO> {

    @Override
    public OrderDTO process(OrderDTO item) {
        log.info(">>> [Processor] 正在处理订单: {}", item.getBizOrderNo());
        return item;
    }
}