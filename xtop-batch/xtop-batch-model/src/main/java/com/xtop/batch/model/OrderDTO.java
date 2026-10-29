package com.xtop.batch.model;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderDTO implements Serializable {
    private Long id;
    private String orderId;
    private String bizOrderNo;
    private BigDecimal totalAmount;
    private String orderStatus;
    private LocalDateTime syncTime;
    private String externalOrderId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}