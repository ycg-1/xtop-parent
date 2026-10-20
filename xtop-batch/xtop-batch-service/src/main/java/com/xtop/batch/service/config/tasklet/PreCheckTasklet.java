package com.xtop.batch.service.config.tasklet;

import com.xtop.batch.service.config.strategy.PreCheckStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.util.List;

/**
 * 无状态的前置守卫 Tasklet（只负责执行传进来的策略列表）
 */
@Slf4j
@RequiredArgsConstructor
public class PreCheckTasklet implements Tasklet {

    private final List<PreCheckStrategy> strategies;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        if (strategies == null || strategies.isEmpty()) {
            log.info("[前置守卫] 无前置检查策略，直接放行。");
            return RepeatStatus.FINISHED;
        }

        log.info("[前置守卫] 开始执行 [{}] 个前置检查策略...", strategies.size());
        for (PreCheckStrategy strategy : strategies) {
            strategy.execute(); // 按顺序执行，任何一步抛异常都会导致 Batch 失败
        }

        log.info("[前置守卫] 所有前置检查全部通过！进入正式批处理。");
        return RepeatStatus.FINISHED;
    }
}