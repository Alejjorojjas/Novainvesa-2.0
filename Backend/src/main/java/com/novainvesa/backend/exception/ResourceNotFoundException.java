package com.novainvesa.backend.exception;

import lombok.Getter;

/**
 * Excepción lanzada cuando un recurso no existe en la base de datos.
 * Devuelve HTTP 404. Usada para PRODUCT_001, CATEGORY_001, etc.
 */
@Getter
public class ResourceNotFoundException extends RuntimeException {

    private final String code;

    public ResourceNotFoundException(String code, String message) {
        super(message);
        this.code = code;
    }

    // ─── Fábrica de errores estándar ────────────────────────────────────────

    public static ResourceNotFoundException product(String slug) {
        return new ResourceNotFoundException("PRODUCT_001", "Producto no encontrado: " + slug);
    }

    public static ResourceNotFoundException category(String slug) {
        return new ResourceNotFoundException("CATEGORY_001", "Categoría no encontrada: " + slug);
    }
}
