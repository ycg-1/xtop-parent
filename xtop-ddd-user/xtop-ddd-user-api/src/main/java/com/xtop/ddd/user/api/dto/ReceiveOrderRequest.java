package com.xtop.ddd.user.api.dto;

/**
 * @Description:
 * @Since: 2026/10/12
 * @Author: 杨春光
 */
// 文件路径: xtop-ddd-user-api/src/main/java/com/xtop/ddd/user/api/dto/ReceiveOrderRequest.java


import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class ReceiveOrderRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String bizOrderNo;
    private Long totalAmount;
    private Date createTime;
    private String remark;
}
