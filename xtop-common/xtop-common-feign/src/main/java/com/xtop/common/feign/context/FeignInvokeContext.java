package com.xtop.common.feign.context;

import com.xtop.common.feign.enums.InvokeMode;
import lombok.Data;

import java.util.Map;

/**
 * @Description:
 * @Since: 2026/9/23
 * @Author: 杨春光
 */
@Data
public class FeignInvokeContext {
    private InvokeMode mode = InvokeMode.TYPED_CONTRACT; // 默认为强类型模式
    private String routeCode;

    /** 强类型模式：直接传入 DTO 对象 */
    private Object requestDto;

    /** 字典模式：传入动态字段 Map */
    private Map<String, Object> dynamicFields;

    /** 期望响应类型 */
    private Class<?> responseType;
}
