package com.xtop.batch.service.order;

/**
 * @Description:
 * @Since: 2026/10/22
 * @Author: 杨春光
 */

import com.xtop.batch.dao.mapper.order.OrderRepository;
import com.xtop.batch.model.order.OrderDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单业务层
 * 封装对 ES 的读写操作，不直接暴露 Repository 给 Controller
 */
@Service
@RequiredArgsConstructor // Lombok 自动为 final 字段生成全参构造器，配合 Spring 注入
public class OrderEsService {

    private final OrderRepository orderRepository;

    /**
     * 保存订单到 ES
     * 将文档 ID（_id）设置为 orderId，避免重复生成，且保证业务唯一性
     */
    public OrderDocument save(OrderDocument doc) {
        // 用业务订单号作为 ES 的 _id，重复写入时会自动覆盖（幂等性）
        doc.setId(doc.getOrderId());
        return orderRepository.save(doc);
    }

    /** 根据订单号删除订单 */
    public void delete(String orderId) {
        orderRepository.deleteById(orderId);
    }

    /** 根据订单状态查询 */
    public List<OrderDocument> findByStatus(String status) {
        return orderRepository.findByOrderStatus(status);
    }

    /** 根据订单状态分页查询 */
    public Page<OrderDocument> findByStatusPage(String status, int page, int size) {
        // 页码从 0 开始，第 1 页 page=0，第 2 页 page=1
        return orderRepository.findByOrderStatus(status, PageRequest.of(page, size));
    }

    /** 根据订单号精确查询 */
    public OrderDocument findByOrderId(String orderId) {
        return orderRepository.findByOrderId(orderId).orElse(null);
    }

    /** 根据金额范围查询 */
    public List<OrderDocument> findByAmountRange(BigDecimal min, BigDecimal max) {
        return orderRepository.findByTotalAmountBetween(min, max);
    }

    /** 生成一条测试订单数据，方便调试 */
    public OrderDocument buildTestOrder() {
        OrderDocument doc = new OrderDocument();
        doc.setOrderId("ORD202610220001");
      //  doc.setBizOrderNo("BIZ202610220001");
        doc.setTotalAmount(new BigDecimal("299.00"));
        doc.setOrderStatus("PENDING");
        doc.setExternalOrderId("EXT_998877");
        LocalDateTime now = LocalDateTime.now();
        doc.setCreateTime(now);
        doc.setUpdateTime(now);
        return doc;
    }
}
