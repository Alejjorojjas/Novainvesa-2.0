package com.novainvesa.backend.repository;

import com.novainvesa.backend.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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
    java.util.Optional<Order> findByMpPaymentId(String mpPaymentId);
}
