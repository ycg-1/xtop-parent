package com.xtop.infra.service.es;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class ElasticsearchService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    public List<Map<String, Object>> queryAll(String indexName) {
        return query(indexName, null, null, null, null);
    }

    public List<Map<String, Object>> query(String indexName, Map<String, Object> conditions) {
        return query(indexName, conditions, null, null, null);
    }

    public List<Map<String, Object>> query(String indexName,
                                           Map<String, Object> conditions,
                                           String sortField,
                                           String sortOrder,
                                           Integer limit) {
        try {
            // 1. 构建查询条件
            BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
            if (conditions != null && !conditions.isEmpty()) {
                for (Map.Entry<String, Object> entry : conditions.entrySet()) {
                    boolBuilder.must(QueryBuilders.termQuery(entry.getKey(), entry.getValue()));
                }
            }

            // 2. 构建 SearchRequest
            SearchRequest searchRequest = new SearchRequest(indexName);
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            sourceBuilder.query(boolBuilder);
            sourceBuilder.size(limit != null ? limit : 10000);

            // 3. 处理排序
            if (sortField != null) {
                sourceBuilder.sort(SortBuilders.fieldSort(sortField)
                        .order("DESC".equalsIgnoreCase(sortOrder) ? SortOrder.DESC : SortOrder.ASC));
            }

            searchRequest.source(sourceBuilder);

            // 4. 执行查询
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

            // 5. 解析结果
            List<Map<String, Object>> results = new ArrayList<>();
            for (SearchHit hit : searchResponse.getHits().getHits()) {
                results.add(hit.getSourceAsMap());
            }

            log.info("ES 查询成功 | Index:{} | 返回:{} 条", indexName, results.size());
            return results;
        } catch (Exception e) {
            log.error("ES 查询失败 | Index:{}", indexName, e);
            return Collections.emptyList();
        }
    }
}
