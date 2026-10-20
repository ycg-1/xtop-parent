package com.xtop.infra.service.feign.config;

/**
 * @Description:
 * @Since: 2026/10/15
 * @Author: 杨春光
 */


import com.xtop.common.feign.context.CrossServiceContext;
import com.xtop.infra.service.filter.TraceConst;
import feign.RequestInterceptor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TraceFeignConfig {

    @Value("${spring.application.name:unknown}")
    private String appName;

    @Bean
    public RequestInterceptor traceFeignInterceptor() {
        return template -> {
            String traceId = StringUtils.defaultIfBlank(CrossServiceContext.getTraceId(),
                    MDC.get(TraceConst.MDC_TRACE_ID));
            if (StringUtils.isNotBlank(traceId)) {
                template.header(TraceConst.HEADER_TRACE_ID, traceId);
            }

            String flowNo = StringUtils.defaultIfBlank(CrossServiceContext.getFlowNo(),
                    MDC.get(TraceConst.MDC_FLOW_NO));
            if (StringUtils.isNotBlank(flowNo)) {
                template.header(TraceConst.HEADER_FLOW_NO, flowNo);
            }

            template.header(TraceConst.HEADER_SOURCE_SERVICE, appName);
        };
    }
}
