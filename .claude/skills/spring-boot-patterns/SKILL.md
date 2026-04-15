# Skill: spring-boot-patterns

Patrones estándar de Spring Boot 3 para Novainvesa.

## Arquitectura en capas

```
Controller → Service → Repository → Entity
               ↓
              DTO (request/response)
```

## Package structure

```
com.novainvesa.backend/
├── config/          ← SecurityConfig, CorsConfig, JacksonConfig
├── controller/      ← @RestController, mapea endpoints
├── service/         ← lógica de negocio
│   ├── payment/     ← WompiService, MercadoPagoService
│   ├── dropi/       ← DropiService, DropiImportService
│   └── notification/← EmailService, WhatsAppService
├── repository/      ← JpaRepository<Entity, Long>
├── entity/          ← @Entity, @Table
├── dto/             ← clases de request/response
├── exception/       ← excepciones de dominio + GlobalExceptionHandler
└── util/            ← JsonListConverter, etc.
```

## Controller — patrón estándar

```java
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<ProductDto>> getBySlug(@PathVariable String slug) {
        ProductDto product = productService.findBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductSummaryDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String category) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(productService.list(category, pageable)));
    }
}
```

## Service — patrón estándar

```java
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)  // default readOnly, override en métodos de escritura
public class ProductService {

    private final ProductRepository productRepository;

    public ProductDto findBySlug(String slug) {
        Product product = productRepository.findBySlugAndStatus(slug, Product.Status.ACTIVE)
            .orElseThrow(() -> new ResourceNotFoundException("PRODUCT_001", "Producto no encontrado: " + slug));
        return toDto(product);
    }

    @Transactional  // escritura
    public ProductDto create(CreateProductRequest request) {
        // lógica...
    }
}
```

## Repository — patrón estándar

```java
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySlugAndStatus(String slug, Product.Status status);

    @Query("SELECT p FROM Product p WHERE p.status = :status ORDER BY p.createdAt DESC")
    Page<Product> findByStatus(@Param("status") Product.Status status, Pageable pageable);

    boolean existsBySlug(String slug);
}
```

## Entity — patrón estándar

```java
@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)   // SIEMPRE STRING
    @Column(nullable = false)
    private Status status;

    @Convert(converter = JsonListConverter.class)
    @Column(columnDefinition = "json")
    private List<String> images;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() { this.createdAt = this.updatedAt = LocalDateTime.now(); }

    @PreUpdate
    protected void onUpdate() { this.updatedAt = LocalDateTime.now(); }

    public enum Status { ACTIVE, DRAFT, ARCHIVED }
}
```

## ApiResponse — uso correcto

```java
// Éxito
return ResponseEntity.ok(ApiResponse.success(dto));
return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(dto));

// Error de negocio (lanzar excepción, el handler la convierte)
throw new ResourceNotFoundException("PRODUCT_001", "Producto no encontrado");
throw new BusinessException("ORDER_002", "Stock insuficiente");

// ApiResponse.error se usa solo en el GlobalExceptionHandler
```

## GlobalExceptionHandler

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleNotFound(ResourceNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
            .map(f -> f.getField() + ": " + f.getDefaultMessage())
            .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(ApiResponse.error("VALIDATION_001", message));
    }
}
```

## Reglas de rendimiento (Render free tier)

- `FetchType.LAZY` en todas las relaciones — NUNCA EAGER
- `spring.jpa.open-in-view=false` — ya configurado
- HikariCP max 5 conexiones — ya configurado
- Nunca cargar listas sin `Pageable`
- `@Transactional(readOnly = true)` como default en services
