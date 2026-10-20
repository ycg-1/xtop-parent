package com.xtop.batch.service.config.strategy;


/**
 * 前置检查策略接口
 */

@FunctionalInterface // 可以配合 Lambda 快速扩展
public interface PreCheckStrategy {

    /**
     * 执行具体的检查或准备工作
     */
    void execute();
}