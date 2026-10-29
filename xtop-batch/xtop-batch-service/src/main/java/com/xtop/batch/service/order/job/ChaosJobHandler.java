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
public class ChaosJobHandler {

    private final JobLauncher jobLauncher;
    private final Job chaosTestJob;
    private final JobExplorer jobExplorer;

    @XxlJob("chaosTestJob")
    public ReturnT<String> execute(String param) {
        try {
            JobParametersBuilder builder = new JobParametersBuilder();

            String actionType = param == null || param.isEmpty() ? "normal" : param;
            builder.addString("actionType", actionType);

            if (actionType.equals("normal") || actionType.equals("fail")) {
                builder.addLong("fixedTime", 123456789L);
            } else {
                builder.addLong("fixedTime", System.currentTimeMillis());
            }

            JobParameters params = builder.toJobParameters();

            JobInstance lastInstance = jobExplorer.getLastJobInstance(chaosTestJob.getName());
            if (lastInstance != null) {
                List<JobExecution> executions = jobExplorer.getJobExecutions(lastInstance);
                JobExecution lastExecution = executions.get(executions.size() - 1);

                if (lastExecution.getStatus() == BatchStatus.FAILED
                        || lastExecution.getStatus() == BatchStatus.STOPPED) {
                    LogUtil.INFO_LOG.info(">>> 混沌测试：发现失败任务，尝试断点续传...");
                    JobExecution execution = jobLauncher.run(chaosTestJob, lastExecution.getJobParameters());
                    return new ReturnT<>(execution.getStatus().toString());
                }

                if (lastExecution.getStatus() == BatchStatus.COMPLETED) {
                    LogUtil.INFO_LOG.error(">>> 混沌测试：幂等拦截，任务已完成");
                    return new ReturnT<>(500, "幂等拦截：任务已成功执行过");
                }
            }

            JobExecution execution = jobLauncher.run(chaosTestJob, params);
            return new ReturnT<>(execution.getStatus().toString());

        } catch (JobInstanceAlreadyCompleteException e) {
            return new ReturnT<>(500, "幂等拦截：" + e.getMessage());
        } catch (JobExecutionAlreadyRunningException e) {
            return new ReturnT<>(500, "任务正在运行：" + e.getMessage());
        } catch (JobRestartException e) {
            return new ReturnT<>(500, "重启失败：" + e.getMessage());
        } catch (Exception e) {
            return new ReturnT<>(500, "启动失败: " + e.getMessage());
        }
    }
}