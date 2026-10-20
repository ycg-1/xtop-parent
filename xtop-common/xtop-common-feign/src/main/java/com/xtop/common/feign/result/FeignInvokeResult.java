package com.xtop.common.feign.result;
import com.xtop.common.feign.header.CrossServiceHeader;
import lombok.Data;

@Data
public class FeignInvokeResult<T> {
    private boolean success;
    private int httpStatus;
    private T data;
    private String errorMsg;
    private CrossServiceHeader header;

    public static <T> FeignInvokeResult<T> success(T data) {
        FeignInvokeResult<T> r = new FeignInvokeResult<>();
        r.setSuccess(true);
        r.setHttpStatus(200);
        r.setData(data);
        return r;
    }

    public static <T> FeignInvokeResult<T> success(T data, CrossServiceHeader header) {
        FeignInvokeResult<T> r = new FeignInvokeResult<>();
        r.setSuccess(true);
        r.setHttpStatus(200);
        r.setData(data);
        r.setHeader(header);
        return r;
    }

    public static <T> FeignInvokeResult<T> fail(String errorMsg) {
        FeignInvokeResult<T> r = new FeignInvokeResult<>();
        r.setSuccess(false);
        r.setErrorMsg(errorMsg);
        return r;
    }

    public static <T> FeignInvokeResult<T> fail(String errorMsg, CrossServiceHeader header) {
        FeignInvokeResult<T> r = new FeignInvokeResult<>();
        r.setSuccess(false);
        r.setErrorMsg(errorMsg);
        r.setHeader(header);
        return r;
    }
}