package com.xtop.batch.service.order.job;

import com.xtop.common.util.log.LogUtil;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Component;

/**
 * 订单同步定时任务处理器
 * 职责：接收 XXL-JOB 调度，启动 Spring Batch Job
 */
@Component
@RequiredArgsConstructor // Lombok 自动注入构造函数
public class OrderSyncJobHandler {

    private final JobLauncher jobLauncher;
    private final Job orderSyncJob; // 注入你在 Config 中定义的 Job Bean

    @XxlJob("orderSyncJobHandler") // XXL-JOB 后台配置的 JobHandler 名称
    public ReturnT<String> execute(String param) throws Exception {
        LogUtil.INFO_LOG.info(">>> XXL-JOB 触发订单同步任务, 参数: {}", param);

        try {
            // 1. 构建 Job 参数 (可选，如果需要从 XXL-JOB 传参给 Batch)
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis()) // 必须加个时间戳，保证每次运行都是新的实例
                    .addString("requestId", param)
                    .toJobParameters();

            // 2. 启动 Batch Job
            JobExecution execution = jobLauncher.run(orderSyncJob, jobParameters);

            // 3. 根据 Batch 的执行结果返回状态给 XXL-JOB
            if (execution.getStatus() == BatchStatus.COMPLETED) {
                LogUtil.INFO_LOG.info(">>> 订单同步任务执行成功");
                return ReturnT.SUCCESS;
            } else {
                LogUtil.ERROR_LOG.error(">>> 订单同步任务执行失败: {}", execution.getStatus());
                return new ReturnT<>(ReturnT.FAIL_CODE, "Batch Job Failed: " + execution.getStatus());
            }
        } catch (Exception e) {
            LogUtil.ERROR_LOG.error(">>> 订单同步任务异常", e);
            return new ReturnT<>(ReturnT.FAIL_CODE, e.getMessage());
        }
    }
}