package com.xtop.batch.service.order.writer;

import com.xtop.batch.model.OrderDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class OrderItemWriter implements ItemWriter<OrderDTO> {

    private final JdbcTemplate jdbcTemplate;

    public OrderItemWriter( DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public void write(List<? extends OrderDTO> items) {
        String sql = "UPDATE t_order SET order_status = 'SYNCED', sync_time = ?, external_order_id = ? WHERE id = ?";

        for (OrderDTO item : items) {
            String externalId = "EXT-" + item.getOrderId();
            jdbcTemplate.update(sql, LocalDateTime.now(), externalId, item.getId());
        }
        log.info(">>> [Writer] 批量同步 {} 条订单到外部系统", items.size());
    }
}