package com.novainvesa.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta al crear una sesión de pago.
 * Para COD: redirectUrl es null, el pedido queda CONFIRMED directamente.
 * Para MP/Wompi: redirectUrl apunta al checkout del proveedor de pago.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCreateResponse {

    private String orderCode;

    /**
     * URL de redirección al checkout externo.
     * null para pedidos COD.
     */
    private String redirectUrl;

    /** COD | WOMPI | MERCADOPAGO */
    private String paymentMethod;

    /** Mensaje informativo (útil para COD) */
    private String message;
}
