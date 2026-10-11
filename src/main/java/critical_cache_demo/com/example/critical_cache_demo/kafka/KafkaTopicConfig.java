package critical_cache_demo.com.example.critical_cache_demo.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic productUpdatedTopic() {
        return new NewTopic("product-updated", 1, (short) 1);
    }

    @Bean
    public NewTopic productUpdatedDltTopic() {
        return new NewTopic("product-updated-dlt", 1, (short) 1);
    }
}