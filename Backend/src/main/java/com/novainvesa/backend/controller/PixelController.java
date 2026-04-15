package com.novainvesa.backend.controller;

import com.novainvesa.backend.dto.ApiResponse;
import com.novainvesa.backend.dto.PixelEventRequest;
import com.novainvesa.backend.service.PixelService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador para el Meta Conversions API (server-side pixel).
 *
 * POST /api/v1/pixel/event — enviar evento a Meta CAPI (publico)
 *
 * Publica porque el frontend lo llama directamente desde el navegador
 * del cliente, incluso para usuarios no autenticados.
 * La ruta esta cubierta como permitida en SecurityConfig.
 */
@RestController
@RequestMapping("/api/v1/pixel")
@RequiredArgsConstructor
public class PixelController {

    private final PixelService pixelService;

    @PostMapping("/event")
    public ResponseEntity<ApiResponse<Void>> sendEvent(
            @Valid @RequestBody PixelEventRequest request,
            HttpServletRequest httpRequest) {

        // Setear IP y User-Agent desde los headers HTTP — no del body JSON
        request.setClientIp(getClientIp(httpRequest));
        request.setUserAgent(httpRequest.getHeader("User-Agent"));

        pixelService.sendEvent(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return (forwarded != null && !forwarded.isBlank())
                ? forwarded.split(",")[0].trim()
                : request.getRemoteAddr();
    }
}
