package com.xtop.batch.service.config.strategy.impl;

/**
 * @Description:
 * @Since: 2026/10/19
 * @Author: 杨春光
 */


import com.xtop.batch.service.config.strategy.PreCheckStrategy;
import lombok.extern.slf4j.Slf4j;
import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 策略一：等待文件存在（带重试机制，例如每5秒查一次，最多查60秒）
 */
@Slf4j
public class FileExistCheckStrategy implements PreCheckStrategy {

    private final String filePath;

    public FileExistCheckStrategy(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void execute() {
        log.info("[文件检查] 开始轮询检查文件是否存在: {}", filePath);

        LocalDateTime startTime = LocalDateTime.now();
        while (true) {
            File file = new File(filePath);
            if (file.exists() && file.isFile()) {
                log.info("[文件检查] 文件已就绪，耗时: {}s", Duration.between(startTime, LocalDateTime.now()).getSeconds());
                return; // 文件存在，检查通过
            }

            // 如果超时（比如等了60秒还没来），直接抛异常阻断任务
            if (Duration.between(startTime, LocalDateTime.now()).getSeconds() > 60) {
                throw new RuntimeException("[文件检查] 超时: 60秒内未找到文件 " + filePath);
            }

            try {
                Thread.sleep(5000); // 每5秒查一次
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("[文件检查] 线程被中断，停止等待文件: " + filePath, e);
            }
        }
    }
}
