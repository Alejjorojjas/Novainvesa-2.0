package com.novainvesa.backend.repository;

import com.novainvesa.backend.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderCode(String orderCode);

    Page<Order> findByUserId(Long userId, Pageable pageable);

    Page<Order> findByOrderStatus(Order.OrderStatus status, Pageable pageable);

    /** Cuenta pedidos del día para generar el código secuencial diario */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :startOfDay AND o.createdAt < :endOfDay")
    long countOrdersForDay(LocalDateTime startOfDay, LocalDateTime endOfDay);

    /** Búsqueda por ID de pago MercadoPago (para webhooks) */
    Optional<Order> findByMpPaymentId(String mpPaymentId);

    // ─── Admin: últimos 5 pedidos para el dashboard ────────────────────────

    List<Order> findTop5ByOrderByCreatedAtDesc();

    // ─── Admin: estadísticas por período ──────────────────────────────────

    @Query("SELECT SUM(o.total) FROM Order o WHERE o.createdAt >= :start AND o.createdAt < :end AND o.paymentStatus = 'CONFIRMED'")
    BigDecimal sumRevenueByPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :start AND o.createdAt < :end")
    long countByPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :start AND o.createdAt < :end AND o.orderStatus = :status")
    long countByPeriodAndOrderStatus(@Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end,
                                     @Param("status") Order.OrderStatus status);

    // ─── Admin: filtros paginados ──────────────────────────────────────────

    Page<Order> findByOrderStatusAndPaymentStatus(
            Order.OrderStatus orderStatus, Order.PaymentStatus paymentStatus, Pageable pageable);

    Page<Order> findByPaymentStatus(Order.PaymentStatus paymentStatus, Pageable pageable);

    Page<Order> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);

    Page<Order> findByOrderStatusAndCreatedAtBetween(
            Order.OrderStatus orderStatus, LocalDateTime from, LocalDateTime to, Pageable pageable);
}
