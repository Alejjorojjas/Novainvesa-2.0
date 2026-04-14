package com.novainvesa.backend.exception;

/**
 * Excepción para errores del importador Dropi.
 * Códigos:
 *   IMPORT_001 — Formato de ID/URL no reconocido
 *   IMPORT_002 — Límite de 50 productos por importación masiva excedido
 *   IMPORT_003 — Producto no encontrado en Dropi  (→ 404)
 *   IMPORT_004 — Error de conexión con Dropi API
 *   IMPORT_005 — Job de importación no encontrado (→ 404)
 */
public class ImportException extends RuntimeException {

    private final String code;

    public ImportException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
