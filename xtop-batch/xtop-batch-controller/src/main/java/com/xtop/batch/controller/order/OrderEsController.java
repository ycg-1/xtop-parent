package com.xtop.batch.controller.order;

/**
 * @Description:
 * @Since: 2026/10/22
 * @Author: 杨春光
 */

import com.xtop.batch.model.order.OrderDocument;
import com.xtop.batch.service.order.OrderEsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单 ES 测试接口
 */
@RestController
@RequestMapping("/order/es")
@RequiredArgsConstructor
public class OrderEsController {

    private final OrderEsService orderEsService;

    /**
     * 写入一条测试订单
     * 调用示例：POST http://localhost:8080/order/es/save
     */
    @PostMapping("/save")
    public OrderDocument save() {
        return orderEsService.save(orderEsService.buildTestOrder());
    }

    /**
     * 根据订单号查询
     * 调用示例：GET http://localhost:8080/order/es/get/ORD202610220001
     */
    @GetMapping("/get/{orderId}")
    public OrderDocument get(@PathVariable String orderId) {
        return orderEsService.findByOrderId(orderId);
    }

    /**
     * 按状态查询订单列表
     * 调用示例：GET http://localhost:8080/order/es/listByStatus?status=PENDING
     */
    @GetMapping("/listByStatus")
    public List<OrderDocument> listByStatus(@RequestParam String status) {
        return orderEsService.findByStatus(status);
    }

    /**
     * 按状态分页查询
     * 调用示例：GET http://localhost:8080/order/es/pageByStatus?status=PENDING&page=0&size=10
     */
    @GetMapping("/pageByStatus")
    public Page<OrderDocument> pageByStatus(@RequestParam String status,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        return orderEsService.findByStatusPage(status, page, size);
    }

    /**
     * 按金额范围查询
     * 调用示例：GET http://localhost:8080/order/es/range?min=100&max=500
     */
    @GetMapping("/range")
    public List<OrderDocument> range(@RequestParam BigDecimal min, @RequestParam BigDecimal max) {
        return orderEsService.findByAmountRange(min, max);
    }

    /**
     * 删除订单
     * 调用示例：DELETE http://localhost:8080/order/es/ORD202610220001
     */
    @DeleteMapping("/{orderId}")
    public void delete(@PathVariable String orderId) {
        orderEsService.delete(orderId);
    }
}