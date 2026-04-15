package com.novainvesa.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Respuesta de pedido para el panel de administración.
 * Incluye campos adicionales no visibles en la vista pública.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminOrderResponse {

    private Long id;
    private String orderCode;
    private Long userId;

    // Datos del cliente
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String customerIdNumber;

    // Dirección de envío
    private String shippingDepartment;
    private String shippingCity;
    private String shippingAddress;
    private String shippingNeighborhood;
    private String shippingNotes;

    // Totales
    private BigDecimal subtotal;
    private BigDecimal shippingCost;
    private BigDecimal total;
    private String currency;

    // Pago
    private String paymentMethod;
    private String paymentStatus;

    // Estado del pedido
    private String orderStatus;

    // Integración Dropi
    private String dropiOrderId;
    private String dropiSyncStatus;

    // Items del pedido
    private List<OrderItemAdminDto> items;

    // Metadatos
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemAdminDto {
        private Long id;
        private Long productId;
        private String dropiProductId;
        private String productName;
        private String productImage;
        private String productSlug;
        private BigDecimal unitPrice;
        private Integer quantity;
        private BigDecimal subtotal;
    }
}
