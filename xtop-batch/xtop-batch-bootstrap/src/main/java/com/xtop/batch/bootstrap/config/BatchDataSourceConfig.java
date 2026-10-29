package com.xtop.batch.bootstrap.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.xtop.common.util.log.LogUtil;
import org.springframework.batch.core.configuration.annotation.BatchConfigurer;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.SQLException;

import static jdk.nashorn.internal.runtime.regexp.joni.Config.log;

@Configuration
public class BatchDataSourceConfig {

    @Value("${spring.datasource.dynamic.datasource.tran-nosharding-db.url}")
    private String url;

    @Value("${spring.datasource.dynamic.datasource.tran-nosharding-db.username}")
    private String username;

    @Value("${spring.datasource.dynamic.datasource.tran-nosharding-db.password}")
    private String password;

    @Value("${spring.datasource.dynamic.datasource.tran-nosharding-db.driver-class-name}")
    private String driverClassName;

    @Value("${spring.datasource.dynamic.datasource.tran-nosharding-db.druid.initial-size}")
    private int initialSize;

    @Value("${spring.datasource.dynamic.datasource.tran-nosharding-db.druid.max-active}")
    private int maxActive;

    @Value("${spring.datasource.dynamic.datasource.tran-nosharding-db.druid.min-idle}")
    private int minIdle;

    @Value("${spring.datasource.dynamic.datasource.tran-nosharding-db.druid.max-wait}")
    private long maxWait;

    @Value("${spring.datasource.dynamic.datasource.tran-nosharding-db.druid.validation-query}")
    private String validationQuery;

    @Bean
    public BatchConfigurer batchConfigurer(@Qualifier("batchDataSource") DataSource dataSource) {
        return new DefaultBatchConfigurer(dataSource);
    }


//    @Bean
//    public BatchConfigurer batchConfigurer(@Qualifier("batchDataSource") DataSource dataSource) {
//        DefaultBatchConfigurer configurer = new DefaultBatchConfigurer(dataSource) {
//            @Override
//            protected JobRepository createJobRepository() throws Exception {
//                JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
//                factory.setDataSource(dataSource);
//                factory.setTransactionManager(batchTransactionManager(dataSource));
//                factory.setDatabaseType("MYSQL");
//
//                // 关键：在这个版本中，直接调用 afterPropertiesSet() 就会触发初始化建表逻辑
//                factory.afterPropertiesSet();
//
//                return factory.getObject();
//            }
//        };
//        return configurer;
//    }

    @Bean(name = "batchDataSource")
    public DataSource batchDataSource() throws SQLException {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(driverClassName);

        dataSource.setInitialSize(initialSize);
        dataSource.setMaxActive(maxActive);
        dataSource.setMinIdle(minIdle);
        dataSource.setMaxWait(maxWait);

        dataSource.setValidationQuery(validationQuery);
        dataSource.setTestWhileIdle(true);
        dataSource.setTestOnBorrow(false);
        dataSource.setTestOnReturn(false);

        dataSource.init();
        LogUtil.INFO_LOG.info("Spring Batch 元数据库使用 tran-nosharding-db -> {}", url);
        return dataSource;
    }

    @Bean(name = "batchTransactionManager")
    public PlatformTransactionManager batchTransactionManager(@Qualifier("batchDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}