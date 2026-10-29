package com.xtop.batch.service.order.reader;

import com.xtop.batch.model.OrderDTO;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
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
        providerFactory.setSelectClause("id, order_id, biz_order_no, total_amount, order_status, sync_time, external_order_id, create_time, update_time");
        providerFactory.setFromClause("t_order");
        providerFactory.setWhereClause("order_status = 'PENDING'");
        providerFactory.setSortKey("id");

        reader.setQueryProvider(providerFactory.getObject());

        reader.setRowMapper((rs, rowNum) -> {
            OrderDTO dto = new OrderDTO();
            dto.setId(rs.getLong("id"));
            dto.setOrderId(rs.getString("order_id"));
            dto.setBizOrderNo(rs.getString("biz_order_no"));
            dto.setTotalAmount(rs.getBigDecimal("total_amount"));
            dto.setOrderStatus(rs.getString("order_status"));

            java.sql.Timestamp syncTimeTs = rs.getTimestamp("sync_time");
            dto.setSyncTime(syncTimeTs != null ? syncTimeTs.toLocalDateTime() : null);

            dto.setExternalOrderId(rs.getString("external_order_id"));

            java.sql.Timestamp createTimeTs = rs.getTimestamp("create_time");
            dto.setCreateTime(createTimeTs != null ? createTimeTs.toLocalDateTime() : null);

            java.sql.Timestamp updateTimeTs = rs.getTimestamp("update_time");
            dto.setUpdateTime(updateTimeTs != null ? updateTimeTs.toLocalDateTime() : null);

            return dto;
        });
        return reader;
    }
}