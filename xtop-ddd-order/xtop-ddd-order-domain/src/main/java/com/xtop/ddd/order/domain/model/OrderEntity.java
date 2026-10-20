package com.xtop.ddd.order.domain.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单聚合根 (Aggregate Root)
 * 它是领域模型的核心，封装了订单的所有业务规则和行为
 */
@Data
public class OrderEntity {
    private String orderId;
    private String bizOrderNo;
    private BigDecimal totalAmount;
    private String status; // PENDING, PAID, SYNCED, FAILED
    private LocalDateTime createTime;

    /**
     * 领域行为：标记为已同步
     * 业务规则：只有待同步的订单才能被标记为已同步
     */
    public void markAsSynced(String externalOrderId) {
        if (!"PENDING".equals(this.status)) {
            throw new IllegalStateException("只有待同步状态的订单才能执行同步操作");
        }
        this.status = "SYNCED";
        // 此处可发布领域事件，如 DomainEventPublisher.publish(new OrderSyncedEvent(this.orderId));
    }

    /**
     * 领域行为：标记为同步失败
     */
    public void markAsSyncFailed(String reason) {
        this.status = "FAILED";
        // 记录失败原因到领域模型中
    }
}