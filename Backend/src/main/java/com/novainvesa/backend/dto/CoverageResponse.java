package com.novainvesa.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para verificación de cobertura COD.
 * Por ahora retorna hasCoverage=true para todas las ciudades.
 * La integración real con Dropi la implementa el agente dropi-integration.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoverageResponse {

    private boolean hasCoverage;
    private String city;
    private String department;
    private String message;
    private String estimatedDelivery;
}
