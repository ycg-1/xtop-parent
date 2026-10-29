package com.xtop.batch.service.es;

/**
 * @Description:
 * @Since: 2026/10/22
 * @Author: 杨春光
 */


import com.xtop.batch.dao.mapper.es.ArticleRepository;
import com.xtop.batch.model.es.Article;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

/** 文章业务层，封装对 ES 的读写操作 */
@Service
@RequiredArgsConstructor  // Lombok 自动生成包含所有 final 字段的构造器，方便依赖注入
public class ArticleService {

    private final ArticleRepository articleRepository;

    /** 保存或更新一篇文章（id 存在则更新，不存在则新建） */
    public Article save(Article article) {
        return articleRepository.save(article);
    }

    /** 根据 ID 删除一篇文章 */
    public void delete(String id) {
        articleRepository.deleteById(id);
    }

    /** 按关键词搜索标题，返回全部结果 */
    public List<Article> search(String keyword) {
        return articleRepository.findByTitleContaining(keyword);
    }

    /** 按关键词分页搜索标题 */
    public Page<Article> searchPage(String keyword, int page, int size) {
        return articleRepository.findByTitleContaining(keyword, PageRequest.of(page, size));
    }
}
