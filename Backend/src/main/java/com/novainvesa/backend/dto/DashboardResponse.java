package com.novainvesa.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Respuesta del dashboard de administración.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {

    private DashboardStats today;
    private DashboardStats thisMonth;
    private List<OrderSummary> recentOrders;
    private List<ProductStatSummary> topProducts;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DashboardStats {
        private long totalOrders;
        private BigDecimal totalRevenue;
        private long pendingOrders;
        private long confirmedOrders;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderSummary {
        private Long id;
        private String orderCode;
        private String customerName;
        private BigDecimal total;
        private String orderStatus;
        private String paymentStatus;
        private java.time.LocalDateTime createdAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductStatSummary {
        private Long productId;
        private String productName;
        private Long views;
        private Long cartAdds;
        private Integer unitsSold;
        private BigDecimal totalRevenue;
    }
}
