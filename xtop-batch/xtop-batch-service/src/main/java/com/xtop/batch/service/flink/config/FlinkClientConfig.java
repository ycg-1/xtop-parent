package com.xtop.batch.service.flink.config;

/**
 * @Description:
 * @Since: 2026/11/23
 * @Author: 杨春光
 */


import org.apache.flink.client.program.rest.RestClusterClient;
import org.apache.flink.configuration.RestOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlinkClientConfig {

    @Value("${flink.rest.address:localhost}")
    private String restAddress;

    @Value("${flink.rest.port:8081}")
    private int restPort;

    @Value("${flink.rest.connection-timeout:10000}")
    private long connectionTimeout;

    @Bean
    public RestClusterClient<String> restClusterClient() throws Exception {
        org.apache.flink.configuration.Configuration conf = new org.apache.flink.configuration.Configuration();
        conf.set(RestOptions.ADDRESS, restAddress);
        conf.set(RestOptions.PORT, restPort);
        // 用字符串 key，避免常量在不同小版本里位置不一致
        conf.setString("rest.connection.timeout", String.valueOf(connectionTimeout));
        // 构造器抛的是 Exception，不是 ConfigurationException
        return new RestClusterClient<>(conf, "xtop-batch-client");
    }
}