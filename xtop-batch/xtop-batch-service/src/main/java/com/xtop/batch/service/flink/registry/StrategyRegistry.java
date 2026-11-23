package com.xtop.batch.service.flink.registry;



import com.xtop.batch.service.flink.strategy.KafkaConsumerStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class StrategyRegistry {

    private final ApplicationContext ctx;
    private final Map<String, KafkaConsumerStrategy> cache = new ConcurrentHashMap<>();

    public StrategyRegistry(ApplicationContext ctx) {
        this.ctx = ctx;
    }

    /**
     * 按 Spring Bean 名称获取策略，带缓存
     */
    public KafkaConsumerStrategy get(String beanName) {
        return cache.computeIfAbsent(beanName, n -> {
            Object bean = ctx.getBean(n);
            if (!(bean instanceof KafkaConsumerStrategy)) {
                throw new IllegalArgumentException("Bean 不是 KafkaConsumerStrategy 类型: " + n);
            }
            log.info("加载策略 beanName={} class={}", n, bean.getClass().getSimpleName());
            return (KafkaConsumerStrategy) bean;
        });
    }
}
