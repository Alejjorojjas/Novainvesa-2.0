package com.novainvesa.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO de request para crear una sesión de pago (MP o Wompi).
 */
@Data
public class PaymentRequest {

    @NotBlank(message = "El código de pedido es obligatorio")
    private String orderCode;
}
