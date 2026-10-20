package com.xtop.common.feign.context;

public final class CrossServiceContext {

    private static final ThreadLocal<String> TRACE_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> REQUEST_ID = new ThreadLocal<>();
    private static final ThreadLocal<Long> REQUEST_TIMESTAMP = new ThreadLocal<>();
    private static final ThreadLocal<String> SOURCE_SERVICE = new ThreadLocal<>();
    private static final ThreadLocal<String> RESPONSE_TRACE_ID = new ThreadLocal<>();
    private static final ThreadLocal<Long> RESPONSE_TIME = new ThreadLocal<>();
    private static final ThreadLocal<String> FLOW_NO = new ThreadLocal<>();

    private CrossServiceContext() {}

    public static void setTraceId(String traceId) {
        TRACE_ID.set(traceId);
    }

    public static String getTraceId() {
        return TRACE_ID.get();
    }

    public static void setRequestId(String requestId) {
        REQUEST_ID.set(requestId);
    }

    public static String getRequestId() {
        return REQUEST_ID.get();
    }

    public static void setRequestTimestamp(Long timestamp) {
        REQUEST_TIMESTAMP.set(timestamp);
    }

    public static Long getRequestTimestamp() {
        return REQUEST_TIMESTAMP.get();
    }

    public static void setSourceService(String sourceService) {
        SOURCE_SERVICE.set(sourceService);
    }

    public static String getSourceService() {
        return SOURCE_SERVICE.get();
    }

    public static void setResponseTraceId(String traceId) {
        RESPONSE_TRACE_ID.set(traceId);
    }

    public static String getResponseTraceId() {
        return RESPONSE_TRACE_ID.get();
    }

    public static void setResponseTime(Long time) {
        RESPONSE_TIME.set(time);
    }

    public static Long getResponseTime() {
        return RESPONSE_TIME.get();
    }

    public static void setFlowNo(String flowNo) {
        FLOW_NO.set(flowNo);
    }

    public static String getFlowNo() {
        return FLOW_NO.get();
    }

    public static void clear() {
        TRACE_ID.remove();
        REQUEST_ID.remove();
        REQUEST_TIMESTAMP.remove();
        SOURCE_SERVICE.remove();
        RESPONSE_TRACE_ID.remove();
        RESPONSE_TIME.remove();
        FLOW_NO.remove();
    }
}