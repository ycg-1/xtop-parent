package com.xtop.batch.service.mq;

/**
 * @Description:
 * @Since: 2026/11/16
 * @Author: 杨春光
 */
import com.rabbitmq.client.Channel;
import com.xtop.infra.service.mq.config.QuorumConfig;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;

@Component
public class OrderConsumer {

    private final StringRedisTemplate redis;

    public OrderConsumer(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @RabbitListener(queues = QuorumConfig.ORDER_QUEUE)
    public void handle(String body, Message message, Channel channel) throws Exception {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String orderId = (String) message.getMessageProperties().getHeaders().get("orderId");

        try {
            // 1) 幂等：Redis SETNX
            Boolean first = redis.opsForValue()
                    .setIfAbsent("mq:idempotent:" + orderId, "1", 24, TimeUnit.HOURS);
            if (Boolean.FALSE.equals(first)) {
                channel.basicAck(deliveryTag, false);
                return;
            }

            // 2) 分布式锁：防止同一订单并发处理
            String lockKey = "lock:order:" + orderId;
            Boolean locked = redis.opsForValue().setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);
            if (Boolean.FALSE.equals(locked)) {
                channel.basicNack(deliveryTag, false, true);
                return;
            }

            // 3) 业务处理
            processOrder(orderId, body);

            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            // 业务异常：重回队列重试，超过 TTL 自动进死信队列
            channel.basicNack(deliveryTag, false, true);
        }
    }

    private void processOrder(String orderId, String body) {
        // TODO: 落库、扣库存、发通知等
        System.out.println("处理订单: " + orderId + ", 内容: " + body);
    }
}
