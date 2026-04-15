package com.novainvesa.backend.controller;

import com.novainvesa.backend.service.WebhookService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador de webhooks para MercadoPago y Wompi.
 *
 * IMPORTANTE: Lee el body como String crudo para que la verificación HMAC
 * opere sobre el payload original sin modificar. No usa @RequestBody con DTO.
 *
 * RN-021: NUNCA procesar sin verificar HMAC.
 * Devuelve 200 inmediatamente si la firma es válida — los proveedores re-intentan si reciben 5xx.
 */
@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final WebhookService webhookService;

    // ─── MercadoPago ─────────────────────────────────────────────────────────

    /**
     * POST /api/v1/webhooks/mercadopago
     *
     * Headers relevantes:
     *   x-signature:   "ts=<timestamp>,v1=<hmac>"
     *   x-request-id:  ID único del request de MP
     *
     * Query params:
     *   data.id:       ID del pago en MercadoPago
     *
     * MP espera una respuesta rápida (< 5s). La lógica pesada se ejecuta después.
     */
    @PostMapping("/mercadopago")
    public ResponseEntity<Void> mercadoPago(
            @RequestBody String rawPayload,
            @RequestHeader(value = "x-signature",   required = false) String xSignature,
            @RequestHeader(value = "x-request-id",  required = false) String xRequestId,
            @RequestParam(value = "data.id",         required = false) String dataId,
            HttpServletRequest request) {

        String clientIp = getClientIp(request);
        webhookService.processMercadoPago(rawPayload, xSignature, xRequestId, dataId, clientIp);
        return ResponseEntity.ok().build();
    }

    // ─── Wompi ────────────────────────────────────────────────────────────────

    /**
     * POST /api/v1/webhooks/wompi
     *
     * Headers relevantes:
     *   x-event-checksum:  HMAC-SHA256(payload, wompiEventsSecret) en hex
     */
    @PostMapping("/wompi")
    public ResponseEntity<Void> wompi(
            @RequestBody String rawPayload,
            @RequestHeader(value = "x-event-checksum", required = false) String eventChecksum,
            HttpServletRequest request) {

        String clientIp = getClientIp(request);
        webhookService.processWompi(rawPayload, eventChecksum, clientIp);
        return ResponseEntity.ok().build();
    }

    // ─── Privados ─────────────────────────────────────────────────────────────

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
        return request.getRemoteAddr();
    }
}
