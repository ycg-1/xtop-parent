package com.xtop.infra.service.feign.config;

import org.springframework.cloud.openfeign.FeignClientBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignClientBuilderConfig {

    @Bean
    public FeignClientBuilder feignClientBuilder(ApplicationContext applicationContext) {
        return new FeignClientBuilder(applicationContext);
    }
}