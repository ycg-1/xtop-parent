package com.xtop.ddd.order.domain.repository;

import com.xtop.ddd.order.domain.model.OrderEntity;

/**
 * 订单仓储接口
 * 由 Infrastructure 层负责具体实现（如 MyBatis）
 */
public interface OrderRepository {
    /**
     * 保存或更新订单
     */
    void save(OrderEntity order);

    /**
     * 根据订单ID查询
     */
    OrderEntity findById(String orderId);
}