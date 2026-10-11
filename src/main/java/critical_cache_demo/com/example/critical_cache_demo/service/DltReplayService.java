package critical_cache_demo.com.example.critical_cache_demo.service;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

@Service
public class DltReplayService {

    private static final String DLT_TOPIC = "product-updated-dlt";
    private static final String ORIGINAL_TOPIC = "product-updated";

    private final KafkaTemplate<String, String> kafkaTemplate;

    public DltReplayService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void replay() {

        Properties properties = new Properties();

        properties.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                "localhost:9092"
        );

        properties.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "product-updated-dlt-replay-group"
        );

        properties.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class
        );

        properties.put(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class
        );

        properties.put(
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                false
        );

        properties.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest"
        );

        try (KafkaConsumer<String, String> consumer =
                     new KafkaConsumer<>(properties)) {

            consumer.subscribe(List.of(DLT_TOPIC));

            var records = consumer.poll(Duration.ofSeconds(10));

            if (records.isEmpty()) {
                System.out.println("Replay edilecek DLT mesajı yok.");
                return;
            }

            for (ConsumerRecord<String, String> record : records) {

                String productId = record.value();

                System.out.println(
                        "DLT mesajı bulundu → " +
                                "offset: " + record.offset() +
                                " | Product ID: " + productId
                );

                kafkaTemplate
                        .send(ORIGINAL_TOPIC, productId)
                        .get();

                System.out.println(
                        "Ana topic'e gönderildi → Product ID: "
                                + productId
                );
            }

            consumer.commitSync();

            System.out.println(
                    "DLT replay tamamlandı ve offset commit edildi."
            );

        } catch (Exception e) {

            System.err.println(
                    "DLT replay başarısız → " + e.getMessage()
            );

            throw new RuntimeException(
                    "DLT replay işlemi başarısız.",
                    e
            );
        }
    }
}