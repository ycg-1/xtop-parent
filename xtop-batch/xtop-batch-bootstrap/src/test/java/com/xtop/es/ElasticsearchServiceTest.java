package com.xtop.es;






import com.xtop.infra.service.es.ElasticsearchService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.search.SearchHit;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

@Slf4j
@SpringBootTest(classes = com.xtop.batch.bootstrap.BatchAppRun.class)
@DisplayName("ElasticsearchService 集成测试")
public class ElasticsearchServiceTest {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private ElasticsearchService elasticsearchService;

    private final String TEST_INDEX = "t_order";

    // 1. 测试开始前，自动创建索引并插入数据，防止 404 报错
//    @BeforeEach
//    public void initESData() throws Exception {
//        // 如果索引不存在，则创建
//        if (!restHighLevelClient.indices().exists(new GetIndexRequest(TEST_INDEX), RequestOptions.DEFAULT)) {
//            restHighLevelClient.indices().create(new CreateIndexRequest(TEST_INDEX), RequestOptions.DEFAULT);
//            log.info(">>> 自动创建测试索引: {}", TEST_INDEX);
//        }
//
//        // 写入 3 条测试数据
//        BulkRequest bulkRequest = new BulkRequest();
//        bulkRequest.add(new IndexRequest(TEST_INDEX).id("1").source("order_no", "1001", "product_name", "iPhone 15", "price", 7999.0));
//        bulkRequest.add(new IndexRequest(TEST_INDEX).id("2").source("order_no", "1002", "product_name", "MacBook Pro", "price", 16999.0));
//        bulkRequest.add(new IndexRequest(TEST_INDEX).id("3").source("order_no", "1003", "product_name", "iPad Air", "price", 5999.0));
//        restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
//        log.info(">>> 测试数据已写入，准备开始测试！");
//    }

    // 2. 测试 ES 基础查询（无条件，查全部）
    @Test
    @DisplayName("测试1：无条件全量查询")
    public void testQueryAll() {
        List<Map<String, Object>> results = elasticsearchService.queryAll(TEST_INDEX);
       // Assertions.assertEquals(3, results.size(), "查询结果数量应为3条");
        log.info("测试结果: {}", results);
    }

    // 3. 测试 ES 带条件查询
    @Test
    @DisplayName("测试2：带条件查询")
    public void testQueryWithCondition() {
        Map<String, Object> conditions = new HashMap<>();
        conditions.put("order_no", "1001");

        List<Map<String, Object>> results = elasticsearchService.query(TEST_INDEX, conditions, null, null, 10);
        Assertions.assertEquals(1, results.size(), "按条件查询结果应为1条");
        Assertions.assertEquals("iPhone 15", results.get(0).get("product_name"));
        log.info("测试结果: {}", results);
    }

    // 4. 测试 ES 排序查询
    @Test
    @DisplayName("测试3：带排序查询")
    public void testQueryWithSort() {
        List<Map<String, Object>> results = elasticsearchService.query(TEST_INDEX, null, "price", "DESC", 10);
        Assertions.assertEquals(3, results.size(), "排序查询结果应为3条");
        Assertions.assertEquals(16999.0, results.get(0).get("price"), "第一条数据的价格应是最高的");
        log.info("测试结果: {}", results);
    }

    // 5. 测试完成后，清理测试数据
    @AfterEach
    public void clearESData() throws Exception {
        // 注意：这里只删除了文档，保留了索引。如果想彻底删除索引，可以改成 deleteIndex
        SearchResponse response = restHighLevelClient.search(new SearchRequest(TEST_INDEX), RequestOptions.DEFAULT);
        log.info(">>> 测试结束，ES中共有 {} 条数据", response.getHits().getTotalHits().value);
    }
}


