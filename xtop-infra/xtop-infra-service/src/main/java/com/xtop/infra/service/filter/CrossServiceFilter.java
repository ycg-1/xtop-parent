package com.xtop.infra.service.filter;

import com.xtop.common.feign.context.CrossServiceContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@Order(Integer.MIN_VALUE + 10)
public class CrossServiceFilter implements Filter {

    private static final String[] EXCLUDED_PATHS = {
            "/swagger-ui", "/v3/api-docs", "/swagger-resources", "/webjars", "/actuator"
    };

    private static final String[] STREAM_KEYWORDS = {
            "/sse", "/stream", "/download", "/export"
    };

    private static final long MAX_CACHE_BODY = 64 * 1024;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String uri = httpRequest.getRequestURI();
        String contextPath = StringUtils.defaultString(httpRequest.getContextPath());

        if (match(uri, contextPath, EXCLUDED_PATHS)) {
            chain.doFilter(request, response);
            return;
        }

        long startTime = System.currentTimeMillis();
        String traceId = StringUtils.defaultIfBlank(httpRequest.getHeader(TraceConst.HEADER_TRACE_ID),
                System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8));
        String flowNo = httpRequest.getHeader(TraceConst.HEADER_FLOW_NO);
        String sourceService = httpRequest.getHeader(TraceConst.HEADER_SOURCE_SERVICE);
        ServletRequest actualRequest = httpRequest;

        try {
            String contentType = StringUtils.defaultString(httpRequest.getContentType());
            if (StringUtils.isBlank(flowNo)
                    && contentType.contains("application/json")
                    && httpRequest.getContentLength() > 0
                    && httpRequest.getContentLength() < MAX_CACHE_BODY) {
                RepeatableReadRequestWrapper wrapper = new RepeatableReadRequestWrapper(httpRequest);
                actualRequest = wrapper;
                flowNo = parseFlowNo(wrapper.getBodyStr());
            }

            MDC.put(TraceConst.MDC_TRACE_ID, traceId);
            if (StringUtils.isNotBlank(flowNo)) {
                MDC.put(TraceConst.MDC_FLOW_NO, flowNo);
            }

            CrossServiceContext.setTraceId(traceId);
            CrossServiceContext.setRequestId(traceId);
            CrossServiceContext.setFlowNo(flowNo);
            CrossServiceContext.setSourceService(sourceService);

            String timestamp = httpRequest.getHeader(TraceConst.HEADER_REQUEST_TIMESTAMP);
            if (StringUtils.isNotBlank(timestamp)) {
                try {
                    CrossServiceContext.setRequestTimestamp(Long.parseLong(timestamp));
                } catch (NumberFormatException ignored) {
                }
            }

            log.info("收到请求 source={} method={} uri={} flowNo={}", sourceService, httpRequest.getMethod(), uri, flowNo);

            boolean cacheResponse = !matchAny(uri, STREAM_KEYWORDS);
            ContentCachingResponseWrapper cachedResponse = cacheResponse
                    ? new ContentCachingResponseWrapper(httpResponse) : null;

            try {
                chain.doFilter(actualRequest, cachedResponse == null ? httpResponse : cachedResponse);
            } finally {
                long cost = System.currentTimeMillis() - startTime;
                CrossServiceContext.setResponseTime(cost);
                httpResponse.setHeader(TraceConst.HEADER_TRACE_ID, traceId);
                httpResponse.setHeader(TraceConst.HEADER_RESPONSE_TIME, String.valueOf(cost));

                log.info("请求结束 traceId={} flowNo={} uri={} status={} cost={}ms",
                        traceId, flowNo, uri, httpResponse.getStatus(), cost);

                if (cachedResponse != null) {
                    cachedResponse.copyBodyToResponse();
                }
                }
        } catch (Exception e) {
            log.error("请求异常 traceId={} flowNo={} uri={}", traceId, flowNo, uri, e);
            throw e;
        } finally {
            MDC.clear();
            CrossServiceContext.clear();
        }
    }

    private boolean match(String uri, String contextPath, String[] paths) {
        for (String p : paths) {
            if (uri.startsWith(contextPath + p) || uri.startsWith(p)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchAny(String uri, String[] keywords) {
        for (String k : keywords) {
            if (uri.contains(k)) {
                return true;
            }
        }
        return false;
    }

    /** 按你实际报文字段改，这里假设报文里有 flowNo */
    private String parseFlowNo(String body) {
        if (StringUtils.isBlank(body)) {
            return null;
        }
        try {
            return com.alibaba.fastjson2.JSON.parseObject(body).getString("flowNo");
        } catch (Exception e) {
            return null;
        }
    }
}