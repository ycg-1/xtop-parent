package com.xtop.common.util.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 业务异常类
 * 用于处理业务逻辑层面的异常情况
 *
 * @Description:
 * @Since: 2026/1/9
 * @Author: 杨春光
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private String errorCode;

    /**
     * 构造函数
     *
     * @param message 异常信息
     */
    public BusinessException(String message) {
        super(message);
    }

    /**
     * 构造函数
     *
     * @param message   异常信息
     * @param throwable 异常对象
     */
    public BusinessException(String message, Throwable throwable) {
        super(message, throwable);
    }

    /**
     * 构造函数
     *
     * @param errorCode 错误码
     * @param message   异常信息
     */
    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 构造函数
     *
     * @param errorCode 错误码
     * @param message   异常信息
     * @param throwable 异常对象
     */
    public BusinessException(String errorCode, String message, Throwable throwable) {
        super(message, throwable);
        this.errorCode = errorCode;
    }
}
