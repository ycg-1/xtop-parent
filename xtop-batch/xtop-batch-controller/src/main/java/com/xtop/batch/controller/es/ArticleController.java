package com.xtop.batch.controller.es;

/**
 * @Description:
 * @Since: 2026/10/22
 * @Author: 杨春光
 */
import com.xtop.batch.service.es.ArticleService;
import com.xtop.batch.model.es.Article;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 文章 REST 接口 */
@RestController
@RequestMapping("/article")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    /** POST /article  新增或更新文章 */
    @PostMapping
    public Article save(@RequestBody Article article) {
        return articleService.save(article);
    }

    /** DELETE /article/{id}  删除文章 */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        articleService.delete(id);
    }

    /** GET /article/search?keyword=xxx  搜索文章 */
    @GetMapping("/search")
    public List<Article> search(@RequestParam String keyword) {
        return articleService.search(keyword);
    }

    /** GET /article/page?keyword=xxx&page=0&size=10  分页搜索文章 */
    @GetMapping("/page")
    public Page<Article> page(@RequestParam String keyword,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size) {
        return articleService.searchPage(keyword, page, size);
    }
}