package com.novainvesa.backend.controller;

import com.novainvesa.backend.dto.AdminOrderResponse;
import com.novainvesa.backend.dto.ApiResponse;
import com.novainvesa.backend.dto.PaginatedResponse;
import com.novainvesa.backend.dto.UpdateOrderStatusRequest;
import com.novainvesa.backend.service.AdminOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * Gestión de pedidos desde el panel de administración.
 */
@RestController
@RequestMapping("/api/v1/admin/orders")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
@RequiredArgsConstructor
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    /**
     * GET /api/v1/admin/orders
     * Listado paginado de pedidos con filtros opcionales.
     *
     * @param status        Filtro por orderStatus (PENDING, CONFIRMED, etc.)
     * @param paymentStatus Filtro por paymentStatus (PENDING, CONFIRMED, etc.)
     * @param from          Fecha inicio (ISO: 2026-04-01T00:00:00)
     * @param to            Fecha fin
     * @param page          Página (default 0)
     * @param size          Tamaño de página (default 20)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<AdminOrderResponse>>> listOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(ApiResponse.success(
                adminOrderService.listOrders(status, paymentStatus, from, to, page, size)));
    }

    /**
     * GET /api/v1/admin/orders/{id}
     * Detalle completo del pedido incluyendo items.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminOrderResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(adminOrderService.getOrderById(id)));
    }

    /**
     * PUT /api/v1/admin/orders/{id}/status
     * Actualiza el estado de un pedido. Valida transiciones permitidas (RN-042).
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<AdminOrderResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success(adminOrderService.updateStatus(id, request)));
    }
}
