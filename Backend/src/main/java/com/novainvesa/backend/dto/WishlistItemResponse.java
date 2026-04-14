package com.novainvesa.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Respuesta de un item en la lista de deseos del usuario.
 */
public record WishlistItemResponse(
        Long productId,
        String slug,
        String name,
        BigDecimal price,
        String image,
        boolean inStock,
        LocalDateTime addedAt
) {}
