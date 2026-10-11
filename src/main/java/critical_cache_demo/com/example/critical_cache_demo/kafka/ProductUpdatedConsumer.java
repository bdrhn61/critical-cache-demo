package critical_cache_demo.com.example.critical_cache_demo.kafka;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ProductUpdatedConsumer {

    private final StringRedisTemplate redisTemplate;

    public ProductUpdatedConsumer(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @KafkaListener(
            topics = "product-updated",
            groupId = "cache-invalidation-group"
    )
    public void consume(String productId) {

        System.out.println(
                "Kafka mesajı alındı → Product ID: " + productId
        );

        String key = "product:" + productId;

        redisTemplate.delete(key);

        System.out.println(
                "CACHE INVALIDATED → " + key
        );
    }
}