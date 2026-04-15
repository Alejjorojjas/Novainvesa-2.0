package com.novainvesa.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Resumen de pedido para el historial del usuario autenticado.
 */
public record OrderSummaryResponse(
        String orderCode,
        BigDecimal total,
        String orderStatus,
        String paymentStatus,
        LocalDateTime createdAt
) {}
