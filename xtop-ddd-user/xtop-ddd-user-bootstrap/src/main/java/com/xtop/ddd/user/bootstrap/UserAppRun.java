package com.xtop.ddd.user.bootstrap;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author 杨春光
 * xtop-user   服务启动类     swagger请求地址 http://localhost:8083/userXtop/swagger-ui/index.html#/
 */
@Slf4j
@SpringBootApplication(
        scanBasePackages = {"com.xtop"}
)
@EnableScheduling
@ConfigurationPropertiesScan
@EnableAsync
@EnableFeignClients(basePackages = {"com.xtop"})
@EnableDiscoveryClient
@MapperScan({"com.xtop.ddd.user.infrastructure.mapper", "com.xtop.infra.dao.mapper"})

public class UserAppRun {
    public static void main(String[] args) {
        SpringApplication.run(UserAppRun.class, args);
        log.info("----启动成功----");
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onStartupSuccess() {
        log.info("=====================================");
        log.info("   xtop-user 服务启动成功！      ");
        log.info("=====================================\n");
    }
}
