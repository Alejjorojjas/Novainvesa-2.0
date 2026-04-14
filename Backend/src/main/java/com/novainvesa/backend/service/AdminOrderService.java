package com.novainvesa.backend.service;

import com.novainvesa.backend.dto.AdminOrderResponse;
import com.novainvesa.backend.dto.PaginatedResponse;
import com.novainvesa.backend.dto.UpdateOrderStatusRequest;
import com.novainvesa.backend.entity.Order;
import com.novainvesa.backend.entity.OrderItem;
import com.novainvesa.backend.exception.OrderException;
import com.novainvesa.backend.exception.ResourceNotFoundException;
import com.novainvesa.backend.repository.OrderItemRepository;
import com.novainvesa.backend.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Lógica de negocio para la gestión de pedidos en el panel de administración.
 * Incluye validación de transiciones de estado (RN-042).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminOrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    /**
     * Transiciones de estado permitidas según RN-042.
     */
    private static final Map<Order.OrderStatus, Set<Order.OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
            Order.OrderStatus.PENDING,    EnumSet.of(Order.OrderStatus.CONFIRMED, Order.OrderStatus.CANCELLED),
            Order.OrderStatus.CONFIRMED,  EnumSet.of(Order.OrderStatus.PROCESSING, Order.OrderStatus.CANCELLED),
            Order.OrderStatus.PROCESSING, EnumSet.of(Order.OrderStatus.SHIPPED, Order.OrderStatus.CANCELLED),
            Order.OrderStatus.SHIPPED,    EnumSet.of(Order.OrderStatus.DELIVERED, Order.OrderStatus.RETURNED),
            Order.OrderStatus.DELIVERED,  EnumSet.noneOf(Order.OrderStatus.class),
            Order.OrderStatus.RETURNED,   EnumSet.noneOf(Order.OrderStatus.class),
            Order.OrderStatus.CANCELLED,  EnumSet.noneOf(Order.OrderStatus.class)
    );

    // ─── Listado paginado con filtros ──────────────────────────────────────

    public PaginatedResponse<AdminOrderResponse> listOrders(
            String statusFilter, String paymentStatusFilter,
            LocalDateTime from, LocalDateTime to,
            int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> ordersPage;

        Order.OrderStatus orderStatus = parseOrderStatus(statusFilter);
        Order.PaymentStatus paymentStatus = parsePaymentStatus(paymentStatusFilter);

        if (orderStatus != null && paymentStatus != null) {
            ordersPage = orderRepository.findByOrderStatusAndPaymentStatus(orderStatus, paymentStatus, pageable);
        } else if (orderStatus != null && from != null && to != null) {
            ordersPage = orderRepository.findByOrderStatusAndCreatedAtBetween(orderStatus, from, to, pageable);
        } else if (orderStatus != null) {
            ordersPage = orderRepository.findByOrderStatus(orderStatus, pageable);
        } else if (paymentStatus != null) {
            ordersPage = orderRepository.findByPaymentStatus(paymentStatus, pageable);
        } else if (from != null && to != null) {
            ordersPage = orderRepository.findByCreatedAtBetween(from, to, pageable);
        } else {
            ordersPage = orderRepository.findAll(pageable);
        }

        List<AdminOrderResponse> items = ordersPage.getContent()
                .stream()
                .map(o -> toAdminResponse(o, false))
                .toList();

        return PaginatedResponse.<AdminOrderResponse>builder()
                .items(items)
                .page(page)
                .size(size)
                .totalItems(ordersPage.getTotalElements())
                .totalPages(ordersPage.getTotalPages())
                .hasNext(ordersPage.hasNext())
                .hasPrevious(ordersPage.hasPrevious())
                .build();
    }

    // ─── Detalle de un pedido ──────────────────────────────────────────────

    public AdminOrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("NOT_FOUND", "Pedido no encontrado: " + id));
        return toAdminResponse(order, true);
    }

    // ─── Actualizar estado ─────────────────────────────────────────────────

    /**
     * Actualiza el estado de un pedido validando la transición permitida (RN-042).
     * Si el nuevo estado es SHIPPED y hay notes, se guardan en shippingNotes.
     */
    @Transactional
    public AdminOrderResponse updateStatus(Long id, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("NOT_FOUND", "Pedido no encontrado: " + id));

        Order.OrderStatus currentStatus = order.getOrderStatus();
        Order.OrderStatus newStatus = request.getStatus();

        // Validar transición permitida
        Set<Order.OrderStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(
                currentStatus, EnumSet.noneOf(Order.OrderStatus.class));

        if (!allowed.contains(newStatus)) {
            throw new OrderException("ORDER_007",
                    "Transición de estado no permitida: " + currentStatus + " → " + newStatus);
        }

        order.setOrderStatus(newStatus);

        // Guardar notas de envío (número de guía) cuando pasa a SHIPPED
        if (newStatus == Order.OrderStatus.SHIPPED && request.getNotes() != null) {
            order.setShippingNotes(request.getNotes());
        }

        log.info("Pedido {} actualizado de {} a {} por admin", id, currentStatus, newStatus);
        return toAdminResponse(orderRepository.save(order), true);
    }

    // ─── Mapeo ─────────────────────────────────────────────────────────────

    private AdminOrderResponse toAdminResponse(Order order, boolean includeItems) {
        List<AdminOrderResponse.OrderItemAdminDto> items = null;
        if (includeItems) {
            List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
            items = orderItems.stream()
                    .map(item -> AdminOrderResponse.OrderItemAdminDto.builder()
                            .id(item.getId())
                            .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                            .dropiProductId(item.getDropiProductId())
                            .productName(item.getProductName())
                            .productImage(item.getProductImage())
                            .productSlug(item.getProductSlug())
                            .unitPrice(item.getUnitPrice())
                            .quantity(item.getQuantity())
                            .subtotal(item.getSubtotal())
                            .build())
                    .toList();
        }

        return AdminOrderResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .userId(order.getUser() != null ? order.getUser().getId() : null)
                .customerName(order.getCustomerName())
                .customerEmail(order.getCustomerEmail())
                .customerPhone(order.getCustomerPhone())
                .customerIdNumber(order.getCustomerIdNumber())
                .shippingDepartment(order.getShippingDepartment())
                .shippingCity(order.getShippingCity())
                .shippingAddress(order.getShippingAddress())
                .shippingNeighborhood(order.getShippingNeighborhood())
                .shippingNotes(order.getShippingNotes())
                .subtotal(order.getSubtotal())
                .shippingCost(order.getShippingCost())
                .total(order.getTotal())
                .currency(order.getCurrency())
                .paymentMethod(order.getPaymentMethod().name())
                .paymentStatus(order.getPaymentStatus().name())
                .orderStatus(order.getOrderStatus().name())
                .dropiOrderId(order.getDropiOrderId())
                .dropiSyncStatus(order.getDropiSyncStatus() != null ? order.getDropiSyncStatus().name() : null)
                .items(items)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    // ─── Helpers ───────────────────────────────────────────────────────────

    private Order.OrderStatus parseOrderStatus(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Order.OrderStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private Order.PaymentStatus parsePaymentStatus(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Order.PaymentStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
