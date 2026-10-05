package critical_cache_demo.com.example.critical_cache_demo.service;

import critical_cache_demo.com.example.critical_cache_demo.entity.OutboxEvent;
import critical_cache_demo.com.example.critical_cache_demo.entity.Product;
import critical_cache_demo.com.example.critical_cache_demo.repository.OutboxEventRepository;
import critical_cache_demo.com.example.critical_cache_demo.repository.ProductRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final OutboxEventRepository outboxEventRepository;


    public ProductService(
            ProductRepository productRepository,
            OutboxEventRepository outboxEventRepository,
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper) {

        this.productRepository = productRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public Product create(Product product) {
        return productRepository.save(product);
    }

    public Product getById(Long id) {

        String key = "product:" + id;

        // 1. Önce Redis'e bak
        String cachedProduct = redisTemplate.opsForValue().get(key);

        if (cachedProduct != null) {
            System.out.println("CACHE HIT → " + key);

            try {
                Product product = objectMapper.readValue(cachedProduct, Product.class);
                return product;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        // 2. Redis'te yoksa PostgreSQL'e git
        System.out.println("CACHE MISS → " + key);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));

        // 3. DB'den gelen Product'ı JSON'a çevirip Redis'e koy
        try {
            String json = objectMapper.writeValueAsString(product);

            redisTemplate.opsForValue()
                    .set(key, json, 5, TimeUnit.MINUTES);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // 4. Ürünü kullanıcıya döndür
        return product;
    }

    @Transactional
    public Product update(Long id, Product newProduct) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));

        product.setName(newProduct.getName());
        product.setPrice(newProduct.getPrice());
        product.setStock(newProduct.getStock());

        // Önce source of truth olan PostgreSQL güncelleniyor
        Product updatedProduct = productRepository.save(product);

        OutboxEvent event = new OutboxEvent();
        event.setEventType("PRODUCT_UPDATED");
        event.setAggregateId(id);
        event.setCreatedAt(LocalDateTime.now());
        event.setProcessed(false);

        outboxEventRepository.save(event);


        return updatedProduct;
    }

}