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



    // 启动流程并检查任务
    public void testStartProcess() {
        // 1. 启动我们刚刚画的流程（注意 Key 必须是 multi_level_approval）
        runtimeService.startProcessInstanceByKey("multi_level_approval");

        // 2. 查询当前待办任务
        List<Task> tasks = taskService.createTaskQuery().list();

        // 3. 打印出来看看
        System.out.println("当前待办任务数量: " + tasks.size());
        for (Task task : tasks) {
            System.out.println("任务名称: " + task.getName());
        }
    }

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
