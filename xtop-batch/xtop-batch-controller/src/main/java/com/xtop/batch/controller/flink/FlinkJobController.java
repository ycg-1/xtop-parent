package com.xtop.batch.controller.flink;


import com.xtop.batch.dao.mapper.flinkJob.FlinkJobConfigMapper;
import com.xtop.batch.model.flinkJob.entity.FlinkJobConfig;
import com.xtop.batch.service.flink.manager.FlinkJobManager;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/flink/job")
@RequiredArgsConstructor
public class FlinkJobController {

    private final FlinkJobManager jobManager;
    private final FlinkJobConfigMapper mapper;

    /** 启动作业：POST /xtop-batch/flink/job/start?jobName=order-consumer */
    @PostMapping("/start")
    public Map<String, Object> start(@RequestParam String jobName) throws Exception {
        String jobId = jobManager.start(jobName);
        Map<String, Object> r = new HashMap<>();
        r.put("success", true);
        r.put("jobName", jobName);
        r.put("jobId", jobId);
        return r;
    }

    /** 停止作业：POST /xtop-batch/flink/job/stop?jobName=order-consumer&withSavepoint=true */
    @PostMapping("/stop")
    public Map<String, Object> stop(@RequestParam String jobName,
                                    @RequestParam(defaultValue = "true") boolean withSavepoint) throws Exception {
        jobManager.stop(jobName, withSavepoint);
        Map<String, Object> r = new HashMap<>();
        r.put("success", true);
        r.put("jobName", jobName);
        return r;
    }

    /** 查所有作业 */
    @GetMapping("/list")
    public List<FlinkJobConfig> list() {
        return mapper.selectList(null);
    }

    /** 查单个作业 */
    @GetMapping("/get")
    public FlinkJobConfig get(@RequestParam String jobName) {
        return mapper.getByName(jobName);
    }
}
