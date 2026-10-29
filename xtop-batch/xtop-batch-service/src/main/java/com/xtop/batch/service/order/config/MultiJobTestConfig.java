package com.xtop.batch.service.order.config;

import com.xtop.batch.model.OrderDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.time.LocalDateTime;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MultiJobTestConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    // ==================== Job 2: 用户同步 Job（处理 ID 1-1000）====================

    @Bean
    public Job userSyncJob(Step userPreCheckStep, Step userSyncStep) {
        return jobBuilderFactory.get("userSyncJob")
                .start(userPreCheckStep)
                .next(userSyncStep)
                .build();
    }

    @Bean
    public Step userPreCheckStep() {
        return stepBuilderFactory.get("userPreCheckStep")
                .tasklet((contribution, chunkContext) -> {
                    log.info("[用户同步] 前置检查通过，写入上下文");
                    chunkContext.getStepContext()
                            .getStepExecution()
                            .getJobExecution()
                            .getExecutionContext()
                            .put("userSyncStartTime", System.currentTimeMillis());
                    chunkContext.getStepContext()
                            .getStepExecution()
                            .getJobExecution()
                            .getExecutionContext()
                            .put("batchSource", "XXL-JOB-TEST");
                    chunkContext.getStepContext()
                            .getStepExecution()
                            .getJobExecution()
                            .getExecutionContext()
                            .put("operator", "test-user");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    private JdbcPagingItemReader<OrderDTO> createUserReader(DataSource dataSource) throws Exception {
        JdbcPagingItemReader<OrderDTO> reader = new JdbcPagingItemReader<>();
        reader.setDataSource(dataSource);
        reader.setPageSize(200);

        SqlPagingQueryProviderFactoryBean providerFactory = new SqlPagingQueryProviderFactoryBean();
        providerFactory.setDataSource(dataSource);
        providerFactory.setSelectClause("id, order_id, biz_order_no, total_amount, order_status");
        providerFactory.setFromClause("t_order");
        providerFactory.setWhereClause("order_status = 'PENDING' AND id <= 1000");
        providerFactory.setSortKey("id");

        reader.setQueryProvider(providerFactory.getObject());
        reader.setRowMapper((rs, rowNum) -> {
            OrderDTO dto = new OrderDTO();
            dto.setId(rs.getLong("id"));
            dto.setOrderId(rs.getString("order_id"));
            dto.setBizOrderNo(rs.getString("biz_order_no"));
            dto.setTotalAmount(rs.getBigDecimal("total_amount"));
            dto.setOrderStatus(rs.getString("order_status"));
            return dto;
        });
      //  reader.afterPropertiesSet();
        return reader;
    }

    @Bean
    public Step userSyncStep(DataSource dataSource) throws Exception {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        JdbcPagingItemReader<OrderDTO> reader = createUserReader(dataSource);

        ItemProcessor<OrderDTO, OrderDTO> processor = item -> {
            log.info("[用户同步-Processor] 处理: {}", item.getBizOrderNo());
            return item;
        };

        ItemWriter<OrderDTO> writer = items -> {
            String sql = "UPDATE t_order SET order_status = 'SYNCED', sync_time = ?, external_order_id = ? WHERE id = ?";
            for (OrderDTO item : items) {
                jdbcTemplate.update(sql, LocalDateTime.now(), "EXT-" + item.getOrderId(), item.getId());
            }
            log.info("[用户同步-Writer] 同步 {} 条", items.size());
        };

        return stepBuilderFactory.get("userSyncStep")
                .<OrderDTO, OrderDTO>chunk(200)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    // ==================== Job 3: 库存同步 Job（处理 ID 1001-2000）====================

    @Bean
    public Job inventorySyncJob(Step inventorySyncStep) {
        return jobBuilderFactory.get("inventorySyncJob")
                .start(inventorySyncStep)
                .build();
    }

    private JdbcPagingItemReader<OrderDTO> createInventoryReader(DataSource dataSource) throws Exception {
        JdbcPagingItemReader<OrderDTO> reader = new JdbcPagingItemReader<>();
        reader.setDataSource(dataSource);
        reader.setPageSize(300);

        SqlPagingQueryProviderFactoryBean providerFactory = new SqlPagingQueryProviderFactoryBean();
        providerFactory.setDataSource(dataSource);
        providerFactory.setSelectClause("id, order_id, biz_order_no, total_amount, order_status");
        providerFactory.setFromClause("t_order");
        providerFactory.setWhereClause("order_status = 'PENDING' AND id > 1000 AND id <= 2000");
        providerFactory.setSortKey("id");

        reader.setQueryProvider(providerFactory.getObject());
        reader.setRowMapper((rs, rowNum) -> {
            OrderDTO dto = new OrderDTO();
            dto.setId(rs.getLong("id"));
            dto.setOrderId(rs.getString("order_id"));
            dto.setBizOrderNo(rs.getString("biz_order_no"));
            dto.setTotalAmount(rs.getBigDecimal("total_amount"));
            dto.setOrderStatus(rs.getString("order_status"));
            return dto;
        });
       // reader.afterPropertiesSet();
        return reader;
    }

    @Bean
    public Step inventorySyncStep(DataSource dataSource) throws Exception {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        JdbcPagingItemReader<OrderDTO> reader = createInventoryReader(dataSource);

        ItemProcessor<OrderDTO, OrderDTO> processor = item -> {
            log.info("[库存同步-Processor] 处理: {}", item.getBizOrderNo());
            return item;
        };

        ItemWriter<OrderDTO> writer = items -> {
            String sql = "UPDATE t_order SET order_status = 'SYNCED', sync_time = ?, external_order_id = ? WHERE id = ?";
            for (OrderDTO item : items) {
                jdbcTemplate.update(sql, LocalDateTime.now(), "EXT-" + item.getOrderId(), item.getId());
            }
            log.info("[库存同步-Writer] 同步 {} 条", items.size());
        };

        return stepBuilderFactory.get("inventorySyncStep")
                .<OrderDTO, OrderDTO>chunk(300)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }


    // ==================== Job 4: 混沌测试 Job（失败/重试/跳过，处理 ID 2001-3000）====================

    @Bean
    public Job chaosTestJob(Step chaosStep) {
        return jobBuilderFactory.get("chaosTestJob")
                .start(chaosStep)
                .build();
    }

    private JdbcPagingItemReader<OrderDTO> createChaosReader(DataSource dataSource) throws Exception {
        JdbcPagingItemReader<OrderDTO> reader = new JdbcPagingItemReader<>();
        reader.setDataSource(dataSource);
        reader.setPageSize(100);

        SqlPagingQueryProviderFactoryBean providerFactory = new SqlPagingQueryProviderFactoryBean();
        providerFactory.setDataSource(dataSource);
        providerFactory.setSelectClause("id, order_id, biz_order_no, total_amount, order_status");
        providerFactory.setFromClause("t_order");
        providerFactory.setWhereClause("order_status = 'PENDING' AND id > 2000");
        providerFactory.setSortKey("id");

        reader.setQueryProvider(providerFactory.getObject());
        reader.setRowMapper((rs, rowNum) -> {
            OrderDTO dto = new OrderDTO();
            dto.setId(rs.getLong("id"));
            dto.setOrderId(rs.getString("order_id"));
            dto.setBizOrderNo(rs.getString("biz_order_no"));
            dto.setTotalAmount(rs.getBigDecimal("total_amount"));
            dto.setOrderStatus(rs.getString("order_status"));
            return dto;
        });
       // reader.afterPropertiesSet();
        return reader;
    }

    @Bean
    public Step chaosStep(DataSource dataSource) throws Exception {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        JdbcPagingItemReader<OrderDTO> reader = createChaosReader(dataSource);

        ItemProcessor<OrderDTO, OrderDTO> processor = item -> {
            log.info("[混沌测试-Processor] 处理: {}", item.getBizOrderNo());
            return item;
        };

        ItemWriter<OrderDTO> writer = items -> {
            String sql = "UPDATE t_order SET order_status = 'SYNCED', sync_time = ?, external_order_id = ? WHERE id = ?";
            for (OrderDTO item : items) {
                jdbcTemplate.update(sql, LocalDateTime.now(), "EXT-" + item.getOrderId(), item.getId());
            }
            log.info("[混沌测试-Writer] 同步 {} 条", items.size());
        };

        return stepBuilderFactory.get("chaosStep")
                .<OrderDTO, OrderDTO>chunk(100)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .retryLimit(3)
                .retry(Exception.class)
                .noRetry(IllegalStateException.class)
                .skipLimit(10)
                .skip(Exception.class)
                .build();
    }
}