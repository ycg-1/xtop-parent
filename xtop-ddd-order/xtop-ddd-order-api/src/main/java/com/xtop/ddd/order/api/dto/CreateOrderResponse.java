package com.xtop.ddd.order.api.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 创建订单响应体（对外暴露的 API 契约 - 接收端）
 * 由 Infra 动态引擎负责反序列化解析
 */
@Data
public class CreateOrderResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 外部系统生成的订单ID */
    private String externalOrderId;

    /** 外部系统订单状态 */
    private String orderStatus;

    /** 错误信息 */
    private String errorMsg;
}