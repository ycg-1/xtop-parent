// 文件路径: xtop-common-model/src/main/java/com/xtop/common/model/ServiceResponse.java
package com.xtop.common.model;

import lombok.Data;

@Data
public class ServiceResponse<T> {

    /** 全链路追踪ID */
    private String traceId;
    /** 单次请求唯一ID */
    private String requestId;
    /** 请求发起时间戳 */
    private Long requestTimestamp;
    /** 调用方服务名 */
    private String sourceService;
    /** 接收方处理耗时(ms) */
    private Long responseTime;

    /** 业务状态码 */
    private int code;
    /** 业务消息 */
    private String message;
    /** 业务数据 */
    private T data;

    public static <T> ServiceResponse<T> ok(T data) {
        ServiceResponse<T> r = new ServiceResponse<>();
        r.setCode(200);
        r.setMessage("success");
        r.setData(data);
        return r;
    }

    public static <T> ServiceResponse<T> fail(int code, String message) {
        ServiceResponse<T> r = new ServiceResponse<>();
        r.setCode(code);
        r.setMessage(message);
        return r;
    }
}