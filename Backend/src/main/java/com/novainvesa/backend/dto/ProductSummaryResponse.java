package com.novainvesa.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO de producto para listados y búsquedas.
 * No expone campos internos como dropiProductId o missingFields.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSummaryResponse {

    private Long id;
    private String slug;
    private String name;
    private String shortDescription;
    private BigDecimal price;
    private BigDecimal compareAtPrice;
    /** Primera imagen del array de imágenes */
    private String primaryImage;
    private String categorySlug;
    private boolean featured;
    private boolean inStock;
    /**
     * Porcentaje de descuento calculado.
     * null si compareAtPrice es nulo o menor/igual a price.
     */
    private Integer discountPercentage;
}
