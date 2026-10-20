package com.xtop.common.feign.enums;

/**
 * @Description:
 * @Since: 2026/9/24
 * @Author: 杨春光
 */


public enum InvokeMode {
    /** 强类型契约模式：代码定义 DTO */
    TYPED_CONTRACT,
    /** 字典驱动模式：数据库配置字段映射 */
    DICT_DRIVEN
}
