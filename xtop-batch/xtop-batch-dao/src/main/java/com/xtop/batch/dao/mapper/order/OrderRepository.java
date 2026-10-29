package com.xtop.batch.dao.mapper.order;

/**
 * @Description:
 * @Since: 2026/10/22
 * @Author: 杨春光
 */



import com.xtop.batch.model.order.OrderDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 订单 ES 仓库
 * 继承了 ElasticsearchRepository，Spring Data 会自动在运行时生成实现类，无需手写代码
 * 方法名必须符合 Spring Data 规范（如 findBy、Between、OrderBy 等）
 */
public interface OrderRepository extends ElasticsearchRepository<OrderDocument, String> {

    /**
     * 根据订单状态查询所有订单
     * @param orderStatus 订单状态（如 PENDING）
     */
    List<OrderDocument> findByOrderStatus(String orderStatus);

    /**
     * 根据订单状态分页查询
     * @param pageable 分页参数（页码、每页大小、排序等）
     */
    Page<OrderDocument> findByOrderStatus(String orderStatus, Pageable pageable);

    /**
     * 根据业务订单号精确查询（因为 orderId 是 Keyword 类型，精确匹配效率极高）
     * @param orderId 全局唯一业务订单号
     */
    Optional<OrderDocument> findByOrderId(String orderId);

    /**
     * 根据订单总金额范围查询
     * @param min 最小金额
     * @param max 最大金额
     */
    List<OrderDocument> findByTotalAmountBetween(BigDecimal min, BigDecimal max);
}