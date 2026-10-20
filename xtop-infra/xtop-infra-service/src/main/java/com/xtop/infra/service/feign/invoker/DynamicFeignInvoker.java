package com.xtop.infra.service.feign.invoker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xtop.common.feign.context.CrossServiceContext;
import com.xtop.common.feign.context.FeignInvokeContext;
import com.xtop.common.feign.contract.TypedContractFeignApi;
import com.xtop.common.feign.enums.InvokeMode;
import com.xtop.common.feign.header.CrossServiceHeader;
import com.xtop.common.feign.result.FeignInvokeResult;
import com.xtop.infra.dao.mapper.FeignRouteConfigMapper;
import com.xtop.infra.model.entity.FeignFieldMapping;
import com.xtop.infra.model.entity.FeignRouteConfig;

import com.xtop.infra.service.feign.strategy.FeignRouteStrategy;
import com.xtop.infra.service.feign.transform.FieldTransformer;
import feign.Client;
import feign.Feign;
import feign.Request;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.FeignClientBuilder;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class DynamicFeignInvoker {

    private final FeignRouteConfigMapper configMapper;
    private final List<FeignRouteStrategy> strategies;
    private final FeignClientBuilder feignClientBuilder;
    private final FieldTransformer fieldTransformer;
    private final ObjectMapper objectMapper;
    private final String applicationName;

    private final Map<String, TypedContractFeignApi> clientCache = new ConcurrentHashMap<>();

    public DynamicFeignInvoker(FeignRouteConfigMapper configMapper,
                               List<FeignRouteStrategy> strategies,
                               FeignClientBuilder feignClientBuilder,
                               FieldTransformer fieldTransformer,
                               ObjectMapper objectMapper,
                               @Value("${spring.application.name:unknown}") String applicationName) {
        this.configMapper = configMapper;
        this.strategies = strategies;
        this.feignClientBuilder = feignClientBuilder;
        this.fieldTransformer = fieldTransformer;
        this.objectMapper = objectMapper;
        this.applicationName = applicationName;
    }

    public <T> FeignInvokeResult<T> invoke(FeignInvokeContext context) {
        long invokeStart = System.currentTimeMillis();

        try {
            FeignRouteConfig config = configMapper.selectByRouteCode(context.getRouteCode());
            if (config == null) {
                throw new RuntimeException("未找到路由配置: " + context.getRouteCode());
            }

            FeignRouteStrategy strategy = strategies.stream()
                    .filter(s -> s.support(config))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("无匹配的路由策略"));
            String target = strategy.resolveTarget(config);

            String apiPath = config.getApiPath() != null ? config.getApiPath() : "";
            String fullUrl = target + apiPath;
            String httpMethod = (config.getHttpMethod() != null)
                    ? config.getHttpMethod().toUpperCase() : "POST";

            TypedContractFeignApi client = clientCache.computeIfAbsent(fullUrl, k -> {
                if (isDirectUrl(target)) {
                    return buildDirectClient(fullUrl, config);
                } else {
                    return feignClientBuilder.forType(TypedContractFeignApi.class, fullUrl).build();
                }
            });

            Object requestBody;
            if (context.getMode() == InvokeMode.DICT_DRIVEN) {
                requestBody = buildDynamicRequest(context.getRouteCode(), context.getDynamicFields());
            } else {
                requestBody = context.getRequestDto();
            }

            String traceId = CrossServiceContext.getTraceId();
            if (traceId == null || traceId.isEmpty()) {
                traceId = System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
                CrossServiceContext.setTraceId(traceId);
            }
            if (CrossServiceContext.getRequestId() == null) {
                CrossServiceContext.setRequestId(UUID.randomUUID().toString().replace("-", ""));
            }
            if (CrossServiceContext.getRequestTimestamp() == null) {
                CrossServiceContext.setRequestTimestamp(System.currentTimeMillis());
            }
            if (CrossServiceContext.getSourceService() == null) {
                CrossServiceContext.setSourceService(applicationName);
            }

            log.info("动态Feign调用: route={}, method={}, url={}, traceId={}",
                    context.getRouteCode(), httpMethod, fullUrl, traceId);

            Object rawResult = executeByMethod(client, httpMethod, requestBody);

            Object businessData = rawResult;
            if (rawResult instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) rawResult;
                if (map.containsKey("data") && map.get("data") != null) {
                    businessData = map.get("data");
                }
            }

            CrossServiceHeader header = buildHeader();

            if (businessData != null && context.getResponseType() != null) {
                T convertedResult = (T) objectMapper.convertValue(businessData, context.getResponseType());
                return FeignInvokeResult.success(convertedResult, header);
            }
            return FeignInvokeResult.success((T) businessData, header);

        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - invokeStart;
            log.error("动态Feign调用失败, traceId={}, elapsed={}ms",
                    CrossServiceContext.getTraceId(), elapsed, e);
            return FeignInvokeResult.fail(e.getMessage(), buildHeader(elapsed));
        } finally {
            CrossServiceContext.clear();
        }
    }

    private CrossServiceHeader buildHeader() {
        return buildHeader(CrossServiceContext.getResponseTime());
    }

    private CrossServiceHeader buildHeader(Long responseTime) {
        CrossServiceHeader header = new CrossServiceHeader();
        header.setTraceId(CrossServiceContext.getTraceId());
        header.setRequestId(CrossServiceContext.getRequestId());
        header.setRequestTimestamp(CrossServiceContext.getRequestTimestamp());
        header.setSourceService(CrossServiceContext.getSourceService());
        header.setResponseTime(responseTime);
        return header;
    }


    private Object executeByMethod(TypedContractFeignApi client, String method, Object body) {
        switch (method) {
            case "GET":
                return client.get(toQueryParams(body));
            case "POST":
                return client.post(body);
            case "PUT":
                return client.put(body);
            case "DELETE":
                return client.delete(toQueryParams(body));
            case "PATCH":
                return client.patch(body);
            default:
                throw new RuntimeException("不支持的HTTP方法: " + method);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toQueryParams(Object body) {
        if (body == null) {
            return java.util.Collections.emptyMap();
        }
        if (body instanceof Map) {
            return (Map<String, Object>) body;
        }
        return objectMapper.convertValue(body, Map.class);
    }

    private boolean isDirectUrl(String target) {
        return target != null && (target.startsWith("http://") || target.startsWith("https://"));
    }

    private TypedContractFeignApi buildDirectClient(String fullUrl, FeignRouteConfig config) {
        int connectTimeout = config.getConnectTimeout() != null ? config.getConnectTimeout() : 5000;
        int readTimeout = config.getReadTimeout() != null ? config.getReadTimeout() : 30000;
        Request.Options options = new Request.Options(connectTimeout, readTimeout);
        log.info("直连模式构建Feign Client, url={}", fullUrl);
        return Feign.builder()
                .contract(new SpringMvcContract())
                .client(new HeaderCapturingClient(new Client.Default(null, null)))
                .requestInterceptor(new GlobalHeaderInterceptor(applicationName))
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .options(options)
                .target(TypedContractFeignApi.class, fullUrl);
    }

    private Map<String, Object> buildDynamicRequest(String routeCode, Map<String, Object> sourceMap) {
        List<FeignFieldMapping> mappings = configMapper.selectFieldsByRouteAndType(routeCode, "REQUEST");
        Map<String, Object> targetMap = new LinkedHashMap<>();

        for (FeignFieldMapping m : mappings) {
            String sourcePath = m.getSourceField();
            String targetPath = m.getTargetField();

            if (sourcePath.contains(".")) {
                applyNestedMapping(sourceMap, targetMap, sourcePath, targetPath, m.getTransformRule());
            } else {
                Object val = sourceMap.get(sourcePath);
                targetMap.put(targetPath, fieldTransformer.transform(val, m.getTransformRule()));
            }
        }
        return targetMap;
    }

    private void applyNestedMapping(Map<String, Object> source, Map<String, Object> target,
                                    String sourcePath, String targetPath, String transformRule) {
        int lastDot = sourcePath.lastIndexOf('.');
        String parentPath = sourcePath.substring(0, lastDot);
        String sourceField = sourcePath.substring(lastDot + 1);

        int lastTargetDot = targetPath.lastIndexOf('.');
        String targetParentPath = targetPath.substring(0, lastTargetDot);
        String targetField = targetPath.substring(lastTargetDot + 1);

        Object sourceParent = resolvePath(source, parentPath);
        if (sourceParent instanceof List) {
            List<?> sourceList = (List<?>) sourceParent;
            List<Map<String, Object>> targetList = getOrCreateTargetList(target, targetParentPath);

            for (int i = 0; i < sourceList.size(); i++) {
                Object item = sourceList.get(i);
                if (!(item instanceof Map)) {
                    continue;
                }
                Map<String, Object> sourceItem = (Map<String, Object>) item;
                Map<String, Object> targetItem;
                if (i < targetList.size()) {
                    targetItem = targetList.get(i);
                } else {
                    targetItem = new LinkedHashMap<>();
                    targetList.add(targetItem);
                }
                Object val = sourceItem.get(sourceField);
                targetItem.put(targetField, fieldTransformer.transform(val, transformRule));
            }
        } else if (sourceParent instanceof Map) {
            Map<String, Object> sourceNested = (Map<String, Object>) sourceParent;
            Map<String, Object> targetNested = getOrCreateTargetMap(target, targetParentPath);
            Object val = sourceNested.get(sourceField);
            targetNested.put(targetField, fieldTransformer.transform(val, transformRule));
        }
    }




    private Object resolvePath(Map<String, Object> map, String path) {
        String[] parts = path.split("\\.");
        Object current = map;
        for (String part : parts) {
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(part);
            } else {
                return null;
            }
        }
        return current;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getOrCreateTargetList(Map<String, Object> target, String parentPath) {
        String[] parts = parentPath.split("\\.");
        Map<String, Object> current = target;
        for (int i = 0; i < parts.length - 1; i++) {
            Object next = current.get(parts[i]);
            if (!(next instanceof Map)) {
                next = new LinkedHashMap<>();
                current.put(parts[i], next);
            }
            current = (Map<String, Object>) next;
        }
        String lastKey = parts[parts.length - 1];
        Object existing = current.get(lastKey);
        if (existing instanceof List) {
            return (List<Map<String, Object>>) existing;
        }
        List<Map<String, Object>> newList = new java.util.ArrayList<>();
        current.put(lastKey, newList);
        return newList;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getOrCreateTargetMap(Map<String, Object> target, String parentPath) {
        String[] parts = parentPath.split("\\.");
        Map<String, Object> current = target;
        for (int i = 0; i < parts.length - 1; i++) {
            Object next = current.get(parts[i]);
            if (!(next instanceof Map)) {
                next = new LinkedHashMap<>();
                current.put(parts[i], next);
            }
            current = (Map<String, Object>) next;
        }
        String lastKey = parts[parts.length - 1];
        Object existing = current.get(lastKey);
        if (existing instanceof Map) {
            return (Map<String, Object>) existing;
        }
        Map<String, Object> newMap = new LinkedHashMap<>();
        current.put(lastKey, newMap);
        return newMap;
    }

    private static class GlobalHeaderInterceptor implements RequestInterceptor {
        private final String serviceName;

        GlobalHeaderInterceptor(String serviceName) {
            this.serviceName = serviceName;
        }

        @Override
        public void apply(RequestTemplate template) {
            String traceId = CrossServiceContext.getTraceId();
            String requestId = CrossServiceContext.getRequestId();
            if (requestId == null) {
                requestId = UUID.randomUUID().toString().replace("-", "");
            }
            template.header("X-Trace-Id", traceId);
            template.header("X-Request-Id", requestId);
            template.header("X-Request-Timestamp", String.valueOf(CrossServiceContext.getRequestTimestamp() != null
                    ? CrossServiceContext.getRequestTimestamp() : System.currentTimeMillis()));
            template.header("X-Source-Service", serviceName);
            log.info("注入请求头: X-Trace-Id={}, X-Source-Service={}", traceId, serviceName);
        }
    }
}