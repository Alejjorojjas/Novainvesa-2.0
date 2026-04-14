package com.novainvesa.backend.controller;

import com.novainvesa.backend.dto.ApiResponse;
import com.novainvesa.backend.dto.CoverageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint público para verificar cobertura COD.
 * GET /api/v1/coverage/cod
 *
 * NOTA: Por ahora retorna hasCoverage=true para todas las ciudades.
 * La integración real con la API de Dropi la implementa el agente dropi-integration.
 */
@RestController
@RequestMapping("/api/v1/coverage")
@Slf4j
public class CoverageController {

    /**
     * Verifica si una ciudad tiene cobertura COD.
     * GET /api/v1/coverage/cod?city=Bogotá&department=Cundinamarca
     */
    @GetMapping("/cod")
    public ResponseEntity<ApiResponse<CoverageResponse>> checkCod(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String department) {

        log.debug("Verificando cobertura COD para city='{}', department='{}'", city, department);

        // Stub: siempre retorna cobertura disponible
        // TODO: integrar con Dropi API (agente dropi-integration)
        CoverageResponse response = CoverageResponse.builder()
                .hasCoverage(true)
                .city(city)
                .department(department)
                .message("Cobertura disponible en tu ciudad")
                .estimatedDelivery("2-3 días hábiles")
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
