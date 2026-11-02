package com.xtop;

import com.xtop.workflow.service.core.FlowableProcessEngine;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @Since: 2026/11/2
 * @Author: 杨春光
 */
@Slf4j
@SpringBootTest(classes = com.xtop.workflow.bootstrap.WorkflowAppRun.class)
@DisplayName("FlowAbleTest 集成测试")
public class FlowAbleTest {
    @Autowired
    FlowableProcessEngine flowableProcessEngine;
    @Test
    @DisplayName("测试1：")
    public void testQueryAll() {
        flowableProcessEngine.testStartProcess();
        // Assertions.assertEquals(3, results.size(), "查询结果数量应为3条");
        System.out.println("测试完成");
    }
}




