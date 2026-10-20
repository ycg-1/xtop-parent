package com.xtop.batch.service.order.job;

/**
 * @Description:
 * @Since: 2026/10/19
 * @Author: 杨春光
 */

import com.xtop.common.util.log.LogUtil;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;


@Component
public class TestJobHandler {
    @XxlJob("test1Job")
    public ReturnT<String> testJob() throws Exception {
        LogUtil.INFO_LOG.info("【测试成功】当前服务器时间: " + LocalDateTime.now().toString());
        return ReturnT.SUCCESS;
    }
}
