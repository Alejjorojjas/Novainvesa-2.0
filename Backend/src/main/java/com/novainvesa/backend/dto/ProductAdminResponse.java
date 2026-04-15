package com.novainvesa.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Respuesta de producto para el panel de administración.
 * Incluye campos sensibles/internos que no se exponen en el catálogo público.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductAdminResponse {

    private Long id;
    private String dropiProductId;
    private String name;
    private String slug;
    private String categorySlug;
    private String shortDescription;
    private String description;
    private BigDecimal price;
    private BigDecimal compareAtPrice;
    private String currency;
    private List<String> images;
    private List<String> benefits;
    private String status;
    private List<String> missingFields;
    private Boolean inStock;
    private Boolean featured;
    private Integer stockQuantity;
    private LocalDateTime importedAt;
    private Long viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
