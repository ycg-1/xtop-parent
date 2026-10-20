package com.xtop.infra.service.feign.strategy;

import com.xtop.common.util.log.LogUtil;
import com.xtop.infra.model.entity.FeignRouteConfig;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class ServiceDiscoveryStrategy implements FeignRouteStrategy {
    @Override
    public boolean support(FeignRouteConfig config) {
        return config.getServiceName() != null && !config.getServiceName().isEmpty();
    }
    @Override
    public String resolveTarget(FeignRouteConfig config) {
        LogUtil.INFO_LOG.info("ServiceDiscoveryStrategy【服务发现模式】Feign调用目标服务: {}", config.getServiceName());
        return config.getServiceName();
    }
}