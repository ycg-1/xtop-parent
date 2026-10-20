package com.xtop.batch.service.config.strategy.impl;

import com.xtop.batch.service.config.strategy.PreCheckStrategy;
import lombok.extern.slf4j.Slf4j;

/**
 * 策略二：跑批前对文件进行解密
 */
@Slf4j
public class FileDecryptStrategy implements PreCheckStrategy {

    private final String filePath;
    private final String decryptKey;

    public FileDecryptStrategy(String filePath, String decryptKey) {
        this.filePath = filePath;
        this.decryptKey = decryptKey;
    }

    @Override
    public void execute() {
        log.info("[解密检查] 开始对文件进行解密: {}", filePath);
        // 这里编写你的实际解密逻辑（比如使用 AES 解密）
        // 1. 读取文件
        // 2. 执行解密
        // 3. 保存回原路径

        log.info("[解密检查] 文件解密成功！");
    }
}
