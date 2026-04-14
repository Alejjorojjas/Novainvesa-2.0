package com.novainvesa.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Estadísticas de producto para el panel de administración.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductStatAdminResponse {

    private Long productId;
    private String productName;
    private String productSlug;
    private Long views;
    private Long cartAdds;
    private Integer wishlistCount;
    private Integer ordersCount;
    private Integer unitsSold;
    private BigDecimal totalRevenue;
}
