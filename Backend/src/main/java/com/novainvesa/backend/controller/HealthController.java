package com.novainvesa.backend.controller;

import com.novainvesa.backend.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    @Value("${spring.profiles.active:development}")
    private String activeProfile;

    /**
     * GET /api/health
     * Health check público — Render.com lo usa para verificar que el servicio está vivo.
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        long uptimeSeconds = ManagementFactory.getRuntimeMXBean().getUptime() / 1000;

        Map<String, Object> data = Map.of(
                "status",      "ok",
                "timestamp",   Instant.now().toString(),
                "uptime",      uptimeSeconds,
                "environment", activeProfile
        );

        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
