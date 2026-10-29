package com.xtop.batch.service.order.job;

import com.xtop.common.util.log.LogUtil;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MultiJobHandler {

    private final JobLauncher jobLauncher;
    private final Job userSyncJob;
    private final Job inventorySyncJob;

    @XxlJob("userSyncJobHandler")
    public ReturnT<String> executeUserSync(String param) throws Exception {
        LogUtil.INFO_LOG.info(">>> XXL-JOB 触发用户同步任务");
        JobParameters params = new JobParametersBuilder()
                .addString("requestId", "user-sync-" + System.currentTimeMillis())
                .toJobParameters();
        JobExecution execution = jobLauncher.run(userSyncJob, params);
        return execution.getStatus() == BatchStatus.COMPLETED ? ReturnT.SUCCESS
                : new ReturnT<>(ReturnT.FAIL_CODE, execution.getStatus().toString());
    }

    @XxlJob("inventorySyncJobHandler")
    public ReturnT<String> executeInventorySync(String param) throws Exception {
        LogUtil.INFO_LOG.info(">>> XXL-JOB 触发库存同步任务");
        JobParameters params = new JobParametersBuilder()
                .addString("requestId", "inventory-sync-" + System.currentTimeMillis())
                .toJobParameters();
        JobExecution execution = jobLauncher.run(inventorySyncJob, params);
        return execution.getStatus() == BatchStatus.COMPLETED ? ReturnT.SUCCESS
                : new ReturnT<>(ReturnT.FAIL_CODE, execution.getStatus().toString());
    }
}