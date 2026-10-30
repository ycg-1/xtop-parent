package com.xtop.workflow.controller;


import com.xtop.workflow.service.core.FlowableProcessEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.task.api.Task;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/workflow")
@RequiredArgsConstructor
public class WorkflowController {

    private final FlowableProcessEngine workflowEngine;

    @PostMapping("/start")
    public String startProcess(@RequestParam String processKey,
                               @RequestParam String businessId,
                               @RequestParam String initiatorId,
                               @RequestBody(required = false) Map<String, Object> variables) {
        return workflowEngine.startProcess(processKey, businessId, initiatorId, variables);
    }

    @PostMapping("/complete")
    public String completeTask(@RequestParam String taskId,
                               @RequestParam String userId,
                               @RequestParam String comment,
                               @RequestBody(required = false) Map<String, Object> variables) {
        workflowEngine.completeTask(taskId, userId, comment, variables);
        return "操作成功";
    }

    @GetMapping("/todo/{userId}")
    public List<Task> getTodo(@PathVariable String userId) {
        return workflowEngine.getMyTodoTasks(userId);
    }
}
