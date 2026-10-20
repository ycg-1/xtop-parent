package com.xtop.infra.service.feign.strategy;

import com.xtop.infra.model.entity.FeignRouteConfig;

public interface FeignRouteStrategy {
    boolean support(FeignRouteConfig config);

    String resolveTarget(FeignRouteConfig config);
}
