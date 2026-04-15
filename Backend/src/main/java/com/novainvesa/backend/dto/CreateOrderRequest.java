package com.novainvesa.backend.dto;

import com.novainvesa.backend.entity.Order;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

/**
 * DTO para la creación de un nuevo pedido.
 * Válido para usuarios autenticados y guest.
 */
@Data
public class CreateOrderRequest {

    // ─── Datos del cliente ───────────────────────────────────────────────────

    @NotBlank(message = "El nombre del cliente es obligatorio")
    private String customerName;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato válido")
    private String customerEmail;

    /** Opcional */
    private String customerPhone;

    /** Opcional — número de identificación */
    private String customerIdNumber;

    // ─── Dirección de envío ──────────────────────────────────────────────────

    @NotBlank(message = "La dirección de envío es obligatoria")
    private String shippingAddress;

    @NotBlank(message = "La ciudad de envío es obligatoria")
    private String shippingCity;

    @NotBlank(message = "El departamento de envío es obligatorio")
    private String shippingDepartment;

    /** Opcional */
    private String shippingNeighborhood;

    /** Opcional — notas adicionales de envío */
    private String shippingNotes;

    // ─── Ítems ───────────────────────────────────────────────────────────────

    @NotNull(message = "Los ítems del pedido son obligatorios")
    @Size(min = 1, message = "El pedido debe tener al menos un ítem")
    @Valid
    private List<OrderItemRequest> items;

    // ─── Pago ────────────────────────────────────────────────────────────────

    @NotNull(message = "El método de pago es obligatorio")
    private Order.PaymentMethod paymentMethod;

    // ─── Clase interna ───────────────────────────────────────────────────────

    @Data
    public static class OrderItemRequest {

        @NotBlank(message = "El slug del producto es obligatorio")
        private String productSlug;

        @Min(value = 1, message = "La cantidad mínima es 1")
        private int quantity;
    }
}
