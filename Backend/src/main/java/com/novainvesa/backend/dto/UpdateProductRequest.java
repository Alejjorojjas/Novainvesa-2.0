package com.novainvesa.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Solicitud de actualización de un producto desde el panel de administración.
 * Todos los campos son opcionales — solo se actualizan los que no son null.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequest {

    private String name;
    private String shortDescription;
    private String description;
    private BigDecimal price;
    private BigDecimal compareAtPrice;
    private List<String> images;
    private List<String> benefits;
    private String categorySlug;
    private Boolean featured;
    private Boolean inStock;
}
