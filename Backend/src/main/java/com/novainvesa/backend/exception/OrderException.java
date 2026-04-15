package com.novainvesa.backend.exception;

import lombok.Getter;

/**
 * Excepción para errores en el módulo de pedidos.
 *
 * Códigos:
 *   ORDER_001 — Producto no disponible (no existe o no está ACTIVE)
 *   ORDER_002 — Stock insuficiente
 *   ORDER_003 — COD no disponible en esta ciudad
 *   ORDER_004 — Total excede límite COD ($500.000 COP)
 *   ORDER_005 — Método de pago no válido
 *   ORDER_006 — Pedido no encontrado
 */
@Getter
public class OrderException extends RuntimeException {

    private final String code;

    public OrderException(String code, String message) {
        super(message);
        this.code = code;
    }

    // ─── Fábrica de errores estándar ────────────────────────────────────────

    public static OrderException productNotAvailable(String slug) {
        return new OrderException("ORDER_001", "Producto no disponible: " + slug);
    }

    public static OrderException insufficientStock(String productName) {
        return new OrderException("ORDER_002", "Stock insuficiente para: " + productName);
    }

    public static OrderException codNotAvailable(String city) {
        return new OrderException("ORDER_003", "Pago contraentrega no disponible en: " + city);
    }

    public static OrderException codLimitExceeded() {
        return new OrderException("ORDER_004", "El total del pedido excede el límite de $500.000 COP para pago contraentrega");
    }

    public static OrderException invalidPaymentMethod() {
        return new OrderException("ORDER_005", "Método de pago no válido");
    }

    public static OrderException notFound(String orderCode) {
        return new OrderException("ORDER_006", "Pedido no encontrado: " + orderCode);
    }
}
