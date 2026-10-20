package com.xtop.ddd.order.infrastructure.repository;

import com.xtop.ddd.order.domain.model.OrderEntity;
import com.xtop.ddd.order.domain.repository.OrderRepository;
import org.springframework.stereotype.Repository;

/**
 * 订单领域模型的持久化实现（Infrastructure 实现 Domain 定义的接口）
 */
@Repository
public class OrderRepositoryImpl implements OrderRepository {

    // 这里注入具体的 Mapper，比如 @Autowired private OrderMapper orderMapper;

    @Override
    public void save(OrderEntity order) {
        // 1. 将领域模型转换为 DO (Data Object)
        // 2. 调用 Mapper 写入数据库
        // orderMapper.insert(orderDO);
        System.out.println("基础设施层：订单已持久化到数据库, id=" + order.getOrderId());
    }

    @Override
    public OrderEntity findById(String orderId) {
        // 1. 调用 Mapper 查询数据库
        // 2. 将 DO 转换为领域模型
        // return orderDO.toDomain();
        return new OrderEntity();
    }
}