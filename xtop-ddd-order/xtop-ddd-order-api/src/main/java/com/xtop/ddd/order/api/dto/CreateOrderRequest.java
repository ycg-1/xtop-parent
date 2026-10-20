package com.xtop.ddd.order.api.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 创建订单请求体（对外暴露的 API 契约 - 发送端）
 * 由 Infra 动态引擎负责序列化后发往第三方
 */
@Data
public class CreateOrderRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 业务订单号 */
    private String bizOrderNo;

    /** 订单总金额（单位：分） */
    private Long totalAmount;

    /** 创建时间 */
    private Date createTime;

    /** 客户备注 */
    private String remark;
}