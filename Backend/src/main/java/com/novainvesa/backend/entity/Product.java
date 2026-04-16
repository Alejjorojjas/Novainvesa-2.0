package com.novainvesa.backend.entity;

import com.novainvesa.backend.util.JsonListConverter;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "products",
        indexes = {
                @Index(name = "idx_category", columnList = "category_slug"),
                @Index(name = "idx_status",   columnList = "status"),
                @Index(name = "idx_featured",  columnList = "featured"),
                @Index(name = "idx_active",    columnList = "active"),
                @Index(name = "idx_slug",      columnList = "slug")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    public enum Status { ACTIVE, DRAFT, ARCHIVED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT UNSIGNED")
    private Long id;

    /** ID del producto en Dropi (ej: "1865251") */
    @Column(name = "dropi_product_id", nullable = false, unique = true, length = 100)
    private String dropiProductId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, unique = true, length = 255)
    private String slug;

    @Column(name = "category_slug", nullable = false, length = 100)
    private String categorySlug;

    @Column(name = "short_description", length = 300)
    private String shortDescription;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** Precio de venta en COP */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    /** Precio tachado (oferta) */
    @Column(name = "compare_at_price", precision = 12, scale = 2)
    private BigDecimal compareAtPrice;

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "COP";

    /** Array de URLs de imágenes almacenado como JSON */
    @Convert(converter = JsonListConverter.class)
    @Column(columnDefinition = "json")
    private List<String> images;

    /** Array de beneficios del producto almacenado como JSON */
    @Convert(converter = JsonListConverter.class)
    @Column(columnDefinition = "json")
    private List<String> benefits;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.ACTIVE;

    /** Campos faltantes cuando el producto está en DRAFT */
    @Convert(converter = JsonListConverter.class)
    @Column(name = "missing_fields", columnDefinition = "json")
    private List<String> missingFields;

    @Column(name = "in_stock", nullable = false)
    @Builder.Default
    private Boolean inStock = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean featured = false;

    /** Peso en kg */
    @Column(precision = 6, scale = 2)
    private BigDecimal weight;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "imported_at")
    private LocalDateTime importedAt;

    /** Contador de vistas acumuladas (sincronizado con product_stats.views) */
    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private Long viewCount = 0L;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
