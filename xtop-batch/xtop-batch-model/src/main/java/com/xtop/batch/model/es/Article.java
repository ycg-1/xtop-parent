package com.xtop.batch.model.es;

/**
 * @Description:
 * @Since: 2026/10/22
 * @Author: 杨春光
 */


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * 文章实体，映射到 ES 的 article 索引
 * createIndex = true 表示项目启动时自动创建索引
 */
@Data
@Document(indexName = "article", createIndex = true)
public class Article {

    /** 文档唯一 ID，对应 ES 中的 _id */
    @Id
    private String id;

    /**
     * 标题字段
     * Text 类型会参与全文检索（会被分词）
     * analyzer = "ik_max_word"  写入时按最细粒度切分中文，保证召回率
     * searchAnalyzer = "ik_smart" 搜索时按粗粒度切分，减少误匹配
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String title;

    /** 正文，分词规则同上 */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String content;

    /**
     * 作者字段
     * Keyword 类型不会分词，适合精确匹配（如等值查询、聚合）
     */
    @Field(type = FieldType.Keyword)
    private String author;
}
