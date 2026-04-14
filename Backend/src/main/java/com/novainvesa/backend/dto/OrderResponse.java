package com.novainvesa.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de respuesta para un pedido.
 * No incluye datos sensibles de pago (IDs externos, tokens).
 */
@Data
@Builder
public class OrderResponse {

    private String orderCode;

    // ─── Cliente (email parcialmente enmascarado) ────────────────────────────
    private String customerName;
    private String customerEmail;   // ej: "j***@ejemplo.com"
    private String customerPhone;

    // ─── Dirección ───────────────────────────────────────────────────────────
    private String shippingAddress;
    private String shippingCity;
    private String shippingDepartment;
    private String shippingNeighborhood;

    // ─── Ítems ───────────────────────────────────────────────────────────────
    private List<OrderItemResponse> items;

    // ─── Totales ─────────────────────────────────────────────────────────────
    private BigDecimal subtotal;
    private BigDecimal shippingCost;
    private BigDecimal total;
    private String currency;

    // ─── Estado ──────────────────────────────────────────────────────────────
    private String paymentMethod;
    private String paymentStatus;
    private String orderStatus;

    private LocalDateTime createdAt;
}
