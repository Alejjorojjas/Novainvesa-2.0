package com.novainvesa.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Datos de vista previa de un producto Dropi antes de importarlo.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DropiProductPreviewDto {

    private String dropiProductId;
    private String name;
    private String shortDescription;
    private String description;
    private BigDecimal price;
    private BigDecimal compareAtPrice;
    private List<String> images;
    private List<String> benefits;
    private boolean inStock;
    private boolean alreadyImported;
}
