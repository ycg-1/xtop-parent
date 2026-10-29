package com.xtop.batch.dao.mapper.es;

/**
 * @Description:
 * @Since: 2026/10/22
 * @Author: 杨春光
 */


import com.xtop.batch.model.es.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

/**
 * 继承 ElasticsearchRepository 后，框架自动实现基础的 CRUD 方法
 * 方法名符合 Spring Data 命名规范时，框架会自动生成查询，无需手写 SQL
 */
public interface ArticleRepository extends ElasticsearchRepository<Article, String> {

    /** 根据标题包含的关键词查询，返回所有匹配结果 */
    List<Article> findByTitleContaining(String keyword);

    /** 根据标题包含的关键词分页查询 */
    Page<Article> findByTitleContaining(String keyword, Pageable pageable);
}
