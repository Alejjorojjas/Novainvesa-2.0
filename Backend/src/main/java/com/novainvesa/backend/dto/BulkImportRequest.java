package com.novainvesa.backend.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Solicitud de importación masiva de productos Dropi.
 * RN-005: máximo 50 productos por importación.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkImportRequest {

    @NotEmpty(message = "La lista de inputs no puede estar vacía")
    @Size(max = 50, message = "Máximo 50 productos por importación masiva")
    private List<String> inputs;
}
