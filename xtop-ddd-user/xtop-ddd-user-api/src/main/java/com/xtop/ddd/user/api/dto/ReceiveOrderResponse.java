package com.xtop.ddd.user.api.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class ReceiveOrderResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private String externalOrderId;
    private String orderStatus;
    private String errorMsg;
}