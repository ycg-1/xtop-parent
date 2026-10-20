package com.xtop.batch.service.order.writer;

import com.xtop.batch.model.OrderDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class OrderItemWriter implements ItemWriter<OrderDTO> {

    private final JdbcTemplate jdbcTemplate;

    public OrderItemWriter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void write(List<? extends OrderDTO> items) {
        for (OrderDTO item : items) {
            String sql = "UPDATE t_order SET sync_status = 1 WHERE id = ?";
            jdbcTemplate.update(sql, item.getId());
        }
        log.info(">>> [Writer] 批量写入 {} 条数据。", items.size());
    }
}