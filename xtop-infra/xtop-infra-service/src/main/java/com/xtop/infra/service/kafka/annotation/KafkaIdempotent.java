package com.xtop.infra.service.kafka.annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Kafka 幂等性消费注解
 * 标注在 @KafkaListener 方法上，开启防重复消费校验
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface KafkaIdempotent {

    /**
     * 分布式锁/幂等Key的前缀
     * 默认: kafka:idem:
     */
    String lockPrefix() default "kafka:idem:";
}
