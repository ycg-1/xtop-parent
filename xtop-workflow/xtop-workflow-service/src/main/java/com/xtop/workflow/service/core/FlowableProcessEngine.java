package com.xtop.workflow.service.core;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlowableProcessEngine {

    private final RuntimeService runtimeService;
    private final TaskService taskService;

    /**
     * 发起流程
     */
    public String startProcess(String processKey, String businessId, String initiatorId, Map<String, Object> variables) {
        Map<String, Object> vars = new HashMap<>(variables != null ? variables : new HashMap<>());
        vars.put("businessId", businessId);
        vars.put("initiatorId", initiatorId);
        vars.put("startTime", new Date());

        ProcessInstance instance = runtimeService.startProcessInstanceByKey(processKey, vars);
        log.info("流程发起成功 - InstanceId: {}, Key: {}", instance.getId(), processKey);
        return instance.getId();
    }

    /**
     * 审批任务（通过/驳回）
     */
    public void completeTask(String taskId, String userId, String comment, Map<String, Object> variables) {
        Map<String, Object> vars = variables != null ? variables : new HashMap<>();
        vars.put("approverId", userId);
        vars.put("approveTime", new Date());

        if (comment != null && !comment.isEmpty()) {
            taskService.addComment(taskId, null, comment);
        }
        taskService.complete(taskId, vars);
        log.info("审批完成 - TaskId: {}, UserId: {}", taskId, userId);
    }

    /**
     * 查询某人的待办任务
     */
    public List<Task> getMyTodoTasks(String userId) {
        return taskService.createTaskQuery()
                .taskAssignee(userId)
                .orderByTaskCreateTime().desc()
                .list();
    }
}
