package com.novainvesa.backend.exception;

import lombok.Getter;

/**
 * Excepción para errores en el módulo de pagos y webhooks.
 *
 * Códigos:
 *   PAYMENT_001 — Error creando preferencia / sesión de pago
 *   PAYMENT_002 — Firma HMAC de webhook inválida
 *   PAYMENT_003 — Pago / pedido no encontrado
 */
@Getter
public class PaymentException extends RuntimeException {

    private final String code;

    public PaymentException(String code, String message) {
        super(message);
        this.code = code;
    }

    // ─── Fábrica de errores estándar ────────────────────────────────────────

    public static PaymentException createError(String detail) {
        return new PaymentException("PAYMENT_001", "Error al crear sesión de pago: " + detail);
    }

    public static PaymentException invalidWebhookSignature() {
        return new PaymentException("PAYMENT_002", "Firma del webhook inválida");
    }

    public static PaymentException notFound(String orderCode) {
        return new PaymentException("PAYMENT_003", "Pedido no encontrado: " + orderCode);
    }
}
