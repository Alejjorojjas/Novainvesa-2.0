package com.novainvesa.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de producto completo para la página de detalle.
 * Incluye todos los campos de ProductSummaryResponse más descripción,
 * imágenes completas, beneficios y estadísticas.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDetailResponse {

    // ─── Campos compartidos con ProductSummaryResponse ───────────────────────
    private Long id;
    private String slug;
    private String name;
    private String shortDescription;
    private BigDecimal price;
    private BigDecimal compareAtPrice;
    private String primaryImage;
    private String categorySlug;
    private boolean featured;
    private boolean inStock;
    private Integer discountPercentage;

    // ─── Campos exclusivos del detalle ───────────────────────────────────────
    private String description;
    private List<String> images;
    private List<String> benefits;
    private Long viewCount;
    private LocalDateTime createdAt;
}
