package com.xtop.infra.service.es.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchConfig {

    @Value("${spring.data.elasticsearch.cluster-nodes}")
    private String clusterNodes;

    @Bean(destroyMethod = "close")
    public RestHighLevelClient restHighLevelClient() {
        String[] parts = clusterNodes.split(":");
        String host = parts[0];
        int port = Integer.parseInt(parts[1]);

        RestClientBuilder builder = RestClient.builder(
                new HttpHost(host, port, "http")
        );

        // 核心修改：在这里开启兼容模式
        builder.setDefaultHeaders(new org.apache.http.message.BasicHeader[]{
                new org.apache.http.message.BasicHeader("Accept", "application/vnd.elasticsearch+json; compatible-with=7"),
                new org.apache.http.message.BasicHeader("Content-Type", "application/vnd.elasticsearch+json; compatible-with=7")
        });

        return new RestHighLevelClient(builder);
    }
}