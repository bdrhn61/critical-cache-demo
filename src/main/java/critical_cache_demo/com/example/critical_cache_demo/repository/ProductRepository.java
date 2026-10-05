package critical_cache_demo.com.example.critical_cache_demo.repository;
import critical_cache_demo.com.example.critical_cache_demo.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}