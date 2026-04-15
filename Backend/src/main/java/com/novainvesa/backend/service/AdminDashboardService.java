package com.novainvesa.backend.service;

import com.novainvesa.backend.dto.DashboardResponse;
import com.novainvesa.backend.entity.Order;
import com.novainvesa.backend.entity.ProductStats;
import com.novainvesa.backend.repository.OrderRepository;
import com.novainvesa.backend.repository.ProductRepository;
import com.novainvesa.backend.repository.ProductStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio del dashboard de administración.
 * Calcula estadísticas de hoy y del mes actual.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminDashboardService {

    private final OrderRepository orderRepository;
    private final ProductStatsRepository productStatsRepository;
    private final ProductRepository productRepository;

    /**
     * Retorna todas las métricas del dashboard:
     * - stats de hoy y del mes
     * - últimos 5 pedidos
     * - top 5 productos por vistas
     */
    public DashboardResponse getDashboard() {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime endOfToday = startOfToday.plusDays(1);
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        DashboardResponse.DashboardStats todayStats = buildStats(startOfToday, endOfToday);
        DashboardResponse.DashboardStats monthStats = buildStats(startOfMonth, endOfToday);

        List<DashboardResponse.OrderSummary> recentOrders = orderRepository
                .findTop5ByOrderByCreatedAtDesc()
                .stream()
                .map(this::toOrderSummary)
                .toList();

        List<DashboardResponse.ProductStatSummary> topProducts = productStatsRepository
                .findAllByOrderByViewsDesc(PageRequest.of(0, 5))
                .stream()
                .map(this::toProductStatSummary)
                .toList();

        return DashboardResponse.builder()
                .today(todayStats)
                .thisMonth(monthStats)
                .recentOrders(recentOrders)
                .topProducts(topProducts)
                .build();
    }

    // ─── Privados ──────────────────────────────────────────────────────────

    private DashboardResponse.DashboardStats buildStats(LocalDateTime start, LocalDateTime end) {
        long totalOrders = orderRepository.countByPeriod(start, end);
        long pendingOrders = orderRepository.countByPeriodAndOrderStatus(start, end, Order.OrderStatus.PENDING);
        long confirmedOrders = orderRepository.countByPeriodAndOrderStatus(start, end, Order.OrderStatus.CONFIRMED);

        BigDecimal revenue = orderRepository.sumRevenueByPeriod(start, end);
        if (revenue == null) revenue = BigDecimal.ZERO;

        return DashboardResponse.DashboardStats.builder()
                .totalOrders(totalOrders)
                .totalRevenue(revenue)
                .pendingOrders(pendingOrders)
                .confirmedOrders(confirmedOrders)
                .build();
    }

    private DashboardResponse.OrderSummary toOrderSummary(Order order) {
        return DashboardResponse.OrderSummary.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .customerName(order.getCustomerName())
                .total(order.getTotal())
                .orderStatus(order.getOrderStatus().name())
                .paymentStatus(order.getPaymentStatus().name())
                .createdAt(order.getCreatedAt())
                .build();
    }

    private DashboardResponse.ProductStatSummary toProductStatSummary(ProductStats stats) {
        String productName = "";
        if (stats.getProduct() != null) {
            productName = stats.getProduct().getName();
        } else {
            productName = productRepository.findById(stats.getProductId())
                    .map(p -> p.getName())
                    .orElse("Producto #" + stats.getProductId());
        }

        return DashboardResponse.ProductStatSummary.builder()
                .productId(stats.getProductId())
                .productName(productName)
                .views(stats.getViews())
                .cartAdds(stats.getCartAdds())
                .unitsSold(stats.getUnitsSold())
                .totalRevenue(stats.getTotalRevenue())
                .build();
    }
}
