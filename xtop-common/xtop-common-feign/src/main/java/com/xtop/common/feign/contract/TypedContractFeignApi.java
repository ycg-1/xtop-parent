package com.xtop.common.feign.contract;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 通用强类型 Feign 契约接口，不绑定具体服务。
 * <p>
 * 设计规范：
 * 1. POST/PUT/PATCH 携带 JSON 请求体，显式声明 consumes/produces
 * 2. GET/DELETE 使用查询参数（@RequestParam），符合 HTTP 语义规范
 * 3. 所有方法统一返回 Object，由调用方做类型转换
 *
 * @Since: 2026/9/23
 * @Author: 杨春光
 */
public interface TypedContractFeignApi {

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    Object get(@RequestParam Map<String, Object> params);

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    Object post(@RequestBody Object body);

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    Object put(@RequestBody Object body);

    @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    Object delete(@RequestParam Map<String, Object> params);

    @PatchMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    Object patch(@RequestBody Object body);
}