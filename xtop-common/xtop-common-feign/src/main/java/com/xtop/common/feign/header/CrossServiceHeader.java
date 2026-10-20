package com.xtop.common.feign.header;

import lombok.Data;

@Data
public class CrossServiceHeader {
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
}