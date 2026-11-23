package com.xtop.batch.service.flink.function;

/**
 * @Description:
 * @Since: 2026/11/23
 * @Author: 杨春光
 */


import com.xtop.batch.model.flinkJob.message.KafkaMessage;
import com.xtop.batch.service.flink.strategy.KafkaConsumerStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;

@Slf4j
public class StrategyProcessFunction extends ProcessFunction<KafkaMessage<?>, KafkaMessage<?>> {

    private static final long serialVersionUID = 1L;

    private final transient KafkaConsumerStrategy strategy;

    public StrategyProcessFunction(KafkaConsumerStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public void processElement(KafkaMessage<?> message,
                               Context ctx,
                               Collector<KafkaMessage<?>> out) throws Exception {
        try {
            strategy.handle(message);
            out.collect(message);
        } catch (Exception e) {
            log.error("处理消息失败 topic={} partition={} offset={}",
                    message.getTopic(), message.getPartition(), message.getOffset(), e);
            throw e;
        }
    }
}
