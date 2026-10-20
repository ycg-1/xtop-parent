package com.xtop.common.util.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.slf4j.MDC;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * 统一响应模型。
 * 主要用在 Controller 的返回值上，也用在 RPC 调用的返回值上
 *
 * @author 杨春光
 * @since 1.0.0
 */
@Schema(description = "标准响应模型")
public class Result<T> implements Serializable {

    private static final long serialVersionUID = -8522056116424962576L;

    public static final int CODE_SUCCESS = 200;
    public static final int CODE_ERROR = 500;

    private static final String TRACE_MDC_KEY = "tradeNo";

    @Schema(description = "状态码，200是成功，其他是失败")
    private Integer code;

    @Schema(description = "错误提示")
    private String message;

    @Schema(description = "数据")
    private T data;

    @Schema(description = "全局ID")
    private String traceId;

    @Schema(description = "时间戳")
    private Long timestamp;

    private void init() {
        this.timestamp = System.currentTimeMillis();
        String mdcTrace = MDC.get(TRACE_MDC_KEY);
        this.traceId = (mdcTrace == null || mdcTrace.trim().isEmpty())
                ? UUID.randomUUID().toString().replace("-", "")
                : mdcTrace;
    }

    public Result() {
        this.init();
        this.code = CODE_SUCCESS;
    }

    public Result(T data) {
        this();
        this.data = data;
    }

    public Result(Integer code, String message) {
        this.init();
        this.code = code;
        this.message = message;
    }

    public static <T> Result<T> error(String message) {
        return new Result<T>(CODE_ERROR, message);
    }

    public static <T> Result<T> error(Integer code, String message) {
        return new Result<T>(code, message);
    }

    public static <T> Result<T> success(T data) {
        return new Result<T>(data);
    }

    public static <T> Result<T> success() {
        return new Result<T>();
    }

    public boolean ok() {
        return Objects.equals(CODE_SUCCESS, this.getCode());
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}