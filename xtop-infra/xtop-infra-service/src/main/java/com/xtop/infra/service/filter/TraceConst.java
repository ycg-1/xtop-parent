package com.xtop.infra.service.filter;

public final class TraceConst {

    private TraceConst() {}

    public static final String MDC_TRACE_ID = "traceId";
    public static final String MDC_FLOW_NO = "flowNo";

    public static final String HEADER_TRACE_ID = "X-Trace-Id";
    public static final String HEADER_FLOW_NO = "X-Flow-No";
    public static final String HEADER_SOURCE_SERVICE = "X-Source-Service";
    public static final String HEADER_REQUEST_TIMESTAMP = "X-Request-Timestamp";
    public static final String HEADER_RESPONSE_TIME = "X-Response-Time";
}