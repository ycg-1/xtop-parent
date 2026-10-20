package com.xtop.batch.model;
import lombok.Data;
import java.io.Serializable;

@Data
public class OrderDTO implements Serializable {
    private Long id;
    private String bizOrderNo;
    private Double totalAmount;
    private Integer syncStatus;
}