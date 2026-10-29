package com.xtop.batch.bootstrap;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.event.EventListener;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author 杨春光
 * xtop-batch   服务启动类     swagger请求地址 http://localhost:8084/xtop-batch/swagger-ui/index.html#/
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
@MapperScan({"com.xtop.batch.dao.mapper", "com.xtop.infra.dao.mapper"})
@EnableElasticsearchRepositories(basePackages = "com.xtop.batch.dao.mapper.es")
@EnableBatchProcessing
public class BatchAppRun
{
    public static void main(String[] args) {
        SpringApplication.run(BatchAppRun.class, args);
        log.info("----启动成功----");
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onStartupSuccess() {
        log.info("=====================================");
        log.info("   xtop-batch 服务启动成功！         ");
        log.info("=====================================\n");
    }
}
