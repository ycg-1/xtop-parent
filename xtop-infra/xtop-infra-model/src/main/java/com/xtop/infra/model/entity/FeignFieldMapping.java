package com.xtop.infra.model.entity;

import lombok.Data;

/**
 * @Description:
 * @Since: 2026/9/24
 * @Author: 杨春光
 */


@Data
public class FeignFieldMapping {
    private Long id;
    private String routeCode;
    private String fieldType; // REQUEST / RESPONSE
    private String sourceField;
    private String targetField;
    private String transformRule;
    private Integer sortOrder;
}
