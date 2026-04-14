package com.novainvesa.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Solicitud para previsualizar un producto Dropi antes de importarlo.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreviewRequest {

    @NotBlank(message = "El input es obligatorio (ID numérico o URL de Dropi)")
    private String input;
}
