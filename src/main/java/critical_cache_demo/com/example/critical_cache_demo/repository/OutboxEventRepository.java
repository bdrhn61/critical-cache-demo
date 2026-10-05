package critical_cache_demo.com.example.critical_cache_demo.repository;

import critical_cache_demo.com.example.critical_cache_demo.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventRepository
        extends JpaRepository<OutboxEvent, Long> {
}