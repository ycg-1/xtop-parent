package com.xtop.infra.service.feign.invoker;
import com.xtop.common.feign.context.CrossServiceContext;
import feign.Client;
import feign.Request;
import feign.Response;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

@Slf4j
public class HeaderCapturingClient implements Client {

    private final Client delegate;

    public HeaderCapturingClient(Client delegate) {
        this.delegate = delegate;
    }

    @Override
    public Response execute(Request request, Request.Options options) throws IOException {
        Response response = delegate.execute(request, options);
        Map<String, Collection<String>> headers = response.headers();

        Collection<String> traceIds = headers.get("X-Trace-Id");
        if (traceIds != null && !traceIds.isEmpty()) {
            CrossServiceContext.setResponseTraceId(traceIds.iterator().next());
        }

        Collection<String> responseTimes = headers.get("X-Response-Time");
        if (responseTimes != null && !responseTimes.isEmpty()) {
            try {
                CrossServiceContext.setResponseTime(Long.parseLong(responseTimes.iterator().next()));
            } catch (NumberFormatException ignored) {
            }
        }

        log.info("捕获响应头: X-Trace-Id={}, X-Response-Time={}ms",
                CrossServiceContext.getResponseTraceId(), CrossServiceContext.getResponseTime());

        return response;
    }
}