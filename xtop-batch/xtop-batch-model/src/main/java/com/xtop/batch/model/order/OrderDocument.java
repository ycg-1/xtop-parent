package com.xtop.batch.model.order;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单 ES 文档，对应索引 t_order
 * createIndex = true 启动时自动建索引
 */
@Data
@Document(indexName = "t_order", createIndex = true)
public class OrderDocument {

    /** ES 文档 ID，用业务订单号 order_id 作为唯一标识 */
    @Id
    private String id;

    /** 全局唯一业务订单号，精确匹配用 Keyword */
    @Field(type = FieldType.Keyword)
    private String orderId;

    /** 内部业务流水号，精确匹配 */
    // 模糊搜索用这个，使用 IK 分词器
    @Field(type = FieldType.Text,
            analyzer = "ik_max_word",
            searchAnalyzer = "ik_smart",
            copyTo = "bizOrderNoSearch") // 复制一份给模糊搜索字段
    private String bizOrderNoSearch;

    /** 订单总金额，用 scaled_float 保留两位小数 */
    @Field(type = FieldType.Scaled_Float, scalingFactor = 100)
    private BigDecimal totalAmount;

    /**
     * 订单状态
     * Keyword 不分词，适合等值过滤（如 orderStatus = "PENDING"）
     */
    @Field(type = FieldType.Keyword)
    private String orderStatus;

    /** 同步到外部系统的时间 */
    @Field(type = FieldType.Date, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime syncTime;

    /** 外部系统返回的唯一 ID */
    @Field(type = FieldType.Keyword)
    private String externalOrderId;

    /** 创建时间 */
    @Field(type = FieldType.Date, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @Field(type = FieldType.Date, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
