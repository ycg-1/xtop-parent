package com.xtop.infra.service.feign.strategy;

import com.xtop.common.util.log.LogUtil;
import com.xtop.infra.model.entity.FeignRouteConfig;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class DirectUrlStrategy implements FeignRouteStrategy {
    @Override
    public boolean support(FeignRouteConfig config) {
        return config.getUrl() != null && !config.getUrl().isEmpty();
    }
    @Override
    public String resolveTarget(FeignRouteConfig config) {
        LogUtil.INFO_LOG.info("DirectUrlStrategy【直连模式】Feign调用目标地址: {}", config.getUrl());
        return config.getUrl();
    }
}