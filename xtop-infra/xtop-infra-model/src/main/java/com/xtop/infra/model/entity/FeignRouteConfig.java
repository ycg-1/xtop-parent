package com.xtop.infra.model.entity;
import lombok.Data;
import java.util.Date;

/**
 * @Description:
 * @Since: 2026/9/23
 * @Author: 杨春光
 */

@Data
public class FeignRouteConfig {
    private Long id;
    private String routeCode;
    private String serviceName;
    private String url;
    private String httpMethod;
    private String apiPath;
    private Integer connectTimeout;
    private Integer readTimeout;
    private Integer status;
    private Date updateTime;
}
