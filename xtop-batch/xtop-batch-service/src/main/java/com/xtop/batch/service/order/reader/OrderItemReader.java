package com.xtop.batch.service.order.reader;

import com.xtop.batch.model.OrderDTO;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class OrderItemReader {

    @Bean
    @StepScope
    public JdbcPagingItemReader<OrderDTO> orderReader(DataSource dataSource) throws Exception {
        JdbcPagingItemReader<OrderDTO> reader = new JdbcPagingItemReader<>();
        reader.setDataSource(dataSource);
        reader.setPageSize(500);

        SqlPagingQueryProviderFactoryBean providerFactory = new SqlPagingQueryProviderFactoryBean();
        providerFactory.setDataSource(dataSource);
        providerFactory.setSelectClause("id, biz_order_no, total_amount, sync_status");
        providerFactory.setFromClause("t_order");
        providerFactory.setWhereClause("sync_status = 0");
        providerFactory.setSortKey("id");

        reader.setQueryProvider(providerFactory.getObject());

        reader.setRowMapper((rs, rowNum) -> {
            OrderDTO dto = new OrderDTO();
            dto.setId(rs.getLong("id"));
            dto.setBizOrderNo(rs.getString("biz_order_no"));
            dto.setTotalAmount(rs.getDouble("total_amount"));
            dto.setSyncStatus(rs.getInt("sync_status"));
            return dto;
        });
        return reader;
    }
}