package com.novainvesa.backend.controller;

import com.novainvesa.backend.dto.ApiResponse;
import com.novainvesa.backend.dto.PaymentCreateResponse;
import com.novainvesa.backend.dto.PaymentRequest;
import com.novainvesa.backend.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador de pagos.
 *
 * POST /api/v1/payments/mercadopago/create — crear preferencia de pago MP
 * POST /api/v1/payments/wompi/create       — crear sesión de pago Wompi
 */
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Crea una preferencia de pago en MercadoPago Checkout Pro.
     * Retorna la URL de redirección al checkout de MP.
     */
    @PostMapping("/mercadopago/create")
    public ResponseEntity<ApiResponse<PaymentCreateResponse>> createMercadoPago(
            @Valid @RequestBody PaymentRequest request) {

        PaymentCreateResponse response = paymentService.createMercadoPagoPreference(request.getOrderCode());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Crea una sesión de pago en Wompi.
     * Retorna la URL de redirección al checkout de Wompi.
     */
    @PostMapping("/wompi/create")
    public ResponseEntity<ApiResponse<PaymentCreateResponse>> createWompi(
            @Valid @RequestBody PaymentRequest request) {

        PaymentCreateResponse response = paymentService.createWompiSession(request.getOrderCode());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
