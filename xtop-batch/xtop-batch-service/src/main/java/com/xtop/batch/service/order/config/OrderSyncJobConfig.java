package com.xtop.batch.service.order.config;

import com.xtop.batch.model.OrderDTO;
import com.xtop.batch.service.config.tasklet.PreCheckTasklet;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
@RequiredArgsConstructor
public class OrderSyncJobConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    // 注入拆分出去的组件
    private final ItemProcessor<OrderDTO, OrderDTO> orderProcessor;
    private final ItemWriter<OrderDTO> orderWriter;
    private final JdbcPagingItemReader<OrderDTO> orderReader;


    /**
     * 在这里“组装”本业务专属的检查策略
     */
    @Bean
    public PreCheckTasklet orderPreCheckTasklet() {
//        return new PreCheckTasklet(Arrays.asList(
//                new FileExistCheckStrategy("/data/orders/20261019.dat"),
//                new FileDecryptStrategy("/data/orders/20261019.dat", "AES_KEY_123")
//        ));


        // 【情况二】：只有一个校验
        // return new PreCheckTasklet(Arrays.asList(
        //         new FileDecryptStrategy("/data/orders/20261019.dat", "AES_KEY_123")
        // ));

        // 【情况三】：一个校验都没有！
        return new PreCheckTasklet(Collections.emptyList());
    }


    /**
     * Step 1: 前置检查
     */
    /**
     * Step 1: 前置检查
     */
    @Bean
    public Step preCheckStep(PreCheckTasklet preCheckTasklet) {
        return stepBuilderFactory.get("orderPreCheckTasklet")
                .tasklet(preCheckTasklet)
                .build();
    }


    /**
     * Step 2: 订单同步
     */
    @Bean
    public Step orderSyncStep() {
        return stepBuilderFactory.get("orderSyncStep")
                .<OrderDTO, OrderDTO>chunk(500)
                .reader(orderReader)
                .processor(orderProcessor)
                .writer(orderWriter)
                .faultTolerant()
                .retryLimit(3)
                .retry(Exception.class)
                .noRetry(IllegalStateException.class)
                .skipLimit(10)
                .skip(Exception.class)
                .build();
    }

    /**
     * Job 整体编排
     */
    @Bean
    public Job orderSyncJob(Step preCheckStep, Step orderSyncStep) {
        return jobBuilderFactory.get("orderSyncJob")
                .start(preCheckStep)
                .next(orderSyncStep)
                .build();
    }
}