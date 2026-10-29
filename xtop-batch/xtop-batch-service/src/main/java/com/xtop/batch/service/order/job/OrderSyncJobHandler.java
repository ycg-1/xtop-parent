package com.xtop.batch.service.order.job;

import com.xtop.common.util.log.LogUtil;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderSyncJobHandler {

    private final JobLauncher jobLauncher;
    private final Job orderSyncJob;
    private final JobExplorer jobExplorer;

    @XxlJob("orderSyncJobHandler")
    public ReturnT<String> execute(String param) {
        LogUtil.INFO_LOG.info(">>> XXL-JOB 触发订单同步任务, 参数: {}", param);

        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("requestId", param == null || param.isEmpty() ? "fixed-order-sync" : param)
                    .toJobParameters();

            JobInstance lastInstance = jobExplorer.getLastJobInstance(orderSyncJob.getName());
            if (lastInstance != null) {
                List<JobExecution> executions = jobExplorer.getJobExecutions(lastInstance);
                JobExecution lastExecution = executions.get(executions.size() - 1);

                if (lastExecution.getStatus() == BatchStatus.FAILED
                        || lastExecution.getStatus() == BatchStatus.STOPPED) {
                    LogUtil.INFO_LOG.info(">>> 发现未完成的 Job，尝试断点续传...");
                    JobExecution execution = jobLauncher.run(orderSyncJob, lastExecution.getJobParameters());
                    return buildResult(execution);
                }

                if (lastExecution.getStatus() == BatchStatus.COMPLETED) {
                    LogUtil.INFO_LOG.error(">>> 幂等拦截：该任务已成功执行过，不可重复执行");
                    return new ReturnT<>(ReturnT.FAIL_CODE, "幂等拦截：任务已成功执行过");
                }
            }

            JobExecution execution = jobLauncher.run(orderSyncJob, jobParameters);
            return buildResult(execution);

        } catch (JobInstanceAlreadyCompleteException e) {
            LogUtil.INFO_LOG.error(">>> 幂等拦截: {}", e.getMessage());
            return new ReturnT<>(ReturnT.FAIL_CODE, "幂等拦截: " + e.getMessage());
        } catch (JobExecutionAlreadyRunningException e) {
            LogUtil.INFO_LOG.error(">>> 任务正在运行中: {}", e.getMessage());
            return new ReturnT<>(ReturnT.FAIL_CODE, "任务正在运行中: " + e.getMessage());
        } catch (JobRestartException e) {
            LogUtil.ERROR_LOG.error(">>> 任务重启失败: {}", e.getMessage());
            return new ReturnT<>(ReturnT.FAIL_CODE, "重启失败: " + e.getMessage());
        } catch (Exception e) {
            LogUtil.ERROR_LOG.error(">>> 订单同步任务异常", e);
            return new ReturnT<>(ReturnT.FAIL_CODE, e.getMessage());
        }
    }

    private ReturnT<String> buildResult(JobExecution execution) {
        if (execution.getStatus() == BatchStatus.COMPLETED) {
            LogUtil.INFO_LOG.info(">>> 订单同步任务执行成功");
            return ReturnT.SUCCESS;
        } else {
            LogUtil.ERROR_LOG.error(">>> 订单同步任务执行失败: {}", execution.getStatus());
            return new ReturnT<>(ReturnT.FAIL_CODE, "Batch Job Failed: " + execution.getStatus());
        }
    }
}