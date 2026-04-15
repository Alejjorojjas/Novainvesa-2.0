package com.novainvesa.backend.controller;

import com.novainvesa.backend.dto.ApiResponse;
import com.novainvesa.backend.dto.CreateOrderRequest;
import com.novainvesa.backend.dto.OrderResponse;
import com.novainvesa.backend.dto.TrackingResponse;
import com.novainvesa.backend.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador de pedidos.
 *
 * POST /api/v1/orders               — crear pedido (público, guest o autenticado)
 * GET  /api/v1/orders/{orderCode}   — detalle de pedido por código
 * GET  /api/v1/orders/{orderCode}/tracking — tracking del pedido
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * Crea un nuevo pedido.
     * Si hay JWT válido, el pedido se vincula al usuario autenticado.
     * Si no hay JWT (o es inválido), se crea como pedido guest.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> create(
            @Valid @RequestBody CreateOrderRequest request,
            Authentication authentication) {

        String userEmail = extractEmail(authentication);
        OrderResponse response = orderService.createOrder(request, userEmail);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * Devuelve el detalle de un pedido por código (acceso público).
     */
    @GetMapping("/{orderCode}")
    public ResponseEntity<ApiResponse<OrderResponse>> getByCode(@PathVariable String orderCode) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getByCode(orderCode)));
    }

    /**
     * Devuelve la línea de tiempo de tracking de un pedido.
     */
    @GetMapping("/{orderCode}/tracking")
    public ResponseEntity<ApiResponse<TrackingResponse>> tracking(@PathVariable String orderCode) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getTracking(orderCode)));
    }

    // ─── Privados ─────────────────────────────────────────────────────────────

    /** Extrae el email del Principal JWT. Retorna null si no está autenticado. */
    private String extractEmail(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return null;
        Object principal = authentication.getPrincipal();
        return (principal instanceof String email) ? email : null;
    }
}
