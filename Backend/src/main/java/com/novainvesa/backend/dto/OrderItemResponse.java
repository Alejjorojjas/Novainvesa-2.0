package com.novainvesa.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO de ítem individual dentro de una respuesta de pedido.
 */
@Data
@Builder
public class OrderItemResponse {

    private String productName;
    private String productImage;
    private String productSlug;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal subtotal;
}
