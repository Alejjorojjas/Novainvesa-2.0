package com.novainvesa.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Solicitud para importar un producto individual desde Dropi.
 * categorySlug es requerido para ACTIVE; si está vacío, el producto quedará en DRAFT.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportProductRequest {

    @NotBlank(message = "El dropiProductId es obligatorio")
    private String dropiProductId;

    @NotBlank(message = "El nombre del producto es obligatorio")
    private String name;

    private String shortDescription;
    private String description;

    @NotNull(message = "El precio es obligatorio")
    private BigDecimal price;

    private BigDecimal compareAtPrice;

    @NotEmpty(message = "Se requiere al menos una imagen")
    private List<String> images;

    private List<String> benefits;

    /** Requerido para status ACTIVE; si está vacío el producto se crea como DRAFT */
    private String categorySlug;

    private boolean featured;
    private boolean publishImmediately;
}
