package critical_cache_demo.com.example.critical_cache_demo.service;

import critical_cache_demo.com.example.critical_cache_demo.entity.OutboxEvent;
import critical_cache_demo.com.example.critical_cache_demo.repository.OutboxEventRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OutboxWorker {

    private final OutboxEventRepository outboxEventRepository;

    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxWorker(
            OutboxEventRepository outboxEventRepository,
            KafkaTemplate<String, String> kafkaTemplate) {

        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 5000)
    public void processOutboxEvents() {

        List<OutboxEvent> events =
                outboxEventRepository.findByProcessedFalse();

        System.out.println(
                "Bekleyen Outbox event sayısı: " + events.size()
        );

        for (OutboxEvent event : events) {
            System.out.println(
                    "Kafka'ya gönderiliyor → " +
                            event.getEventType() +
                            " | Product ID: " +
                            event.getAggregateId()
            );

            kafkaTemplate.send(
                    "product-updated",
                    event.getAggregateId().toString()
            ).whenComplete((result, exception) -> {

                if (exception == null) {

                    System.out.println(
                            "Kafka gönderimi başarılı → Event ID: " + event.getId()
                    );

                    event.setProcessed(true);
                    outboxEventRepository.save(event);

                } else {

                    System.out.println(
                            "Kafka gönderimi başarısız → Event ID: " + event.getId()
                    );

                    exception.printStackTrace();
                }
            });

        }
    }
}