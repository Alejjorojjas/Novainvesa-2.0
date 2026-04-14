package com.novainvesa.backend.service;

import com.novainvesa.backend.dto.*;
import com.novainvesa.backend.entity.Order;
import com.novainvesa.backend.entity.OrderItem;
import com.novainvesa.backend.entity.Product;
import com.novainvesa.backend.entity.User;
import com.novainvesa.backend.exception.OrderException;
import com.novainvesa.backend.repository.OrderItemRepository;
import com.novainvesa.backend.repository.OrderRepository;
import com.novainvesa.backend.repository.ProductRepository;
import com.novainvesa.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de gestión de pedidos.
 * Maneja la creación, consulta y construcción de respuestas de pedidos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderService {

    private static final BigDecimal COD_LIMIT = new BigDecimal("500000");

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final ProductStatsService productStatsService;
    private final UserRepository userRepository;
    private final N8nService n8nService;

    // ─── Creación de pedido ───────────────────────────────────────────────────

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, String userEmail) {

        // 1. Resolver productos y validar disponibilidad
        List<ResolvedItem> resolvedItems = resolveItems(request.getItems());

        // 2. Calcular totales
        BigDecimal subtotal = resolvedItems.stream()
                .map(ri -> ri.product().getPrice().multiply(BigDecimal.valueOf(ri.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal shippingCost = BigDecimal.ZERO; // gratis por ahora — RN-016
        BigDecimal total = subtotal.add(shippingCost);

        // 3. Validar COD
        if (request.getPaymentMethod() == Order.PaymentMethod.COD
                && total.compareTo(COD_LIMIT) > 0) {
            throw OrderException.codLimitExceeded();
        }

        // 4. Resolver usuario autenticado (puede ser null para guest)
        User user = null;
        if (userEmail != null) {
            user = userRepository.findByEmail(userEmail).orElse(null);
        }

        // 5. Generar código de pedido
        String orderCode = generateOrderCode();

        // 6. Crear y guardar la orden
        Order order = Order.builder()
                .orderCode(orderCode)
                .user(user)
                .customerName(request.getCustomerName())
                .customerEmail(request.getCustomerEmail())
                .customerPhone(request.getCustomerPhone())
                .customerIdNumber(request.getCustomerIdNumber())
                .shippingAddress(request.getShippingAddress())
                .shippingCity(request.getShippingCity())
                .shippingDepartment(request.getShippingDepartment())
                .shippingNeighborhood(request.getShippingNeighborhood())
                .shippingNotes(request.getShippingNotes())
                .subtotal(subtotal)
                .shippingCost(shippingCost)
                .total(total)
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(Order.PaymentStatus.PENDING)
                .orderStatus(Order.OrderStatus.PENDING)
                .dropiSyncStatus(Order.DropiSyncStatus.PENDING)
                .dropiSyncAttempts(0)
                .build();

        orderRepository.save(order);

        // 7. Crear ítems
        List<OrderItem> items = new ArrayList<>();
        for (ResolvedItem ri : resolvedItems) {
            Product p = ri.product();
            BigDecimal itemSubtotal = p.getPrice().multiply(BigDecimal.valueOf(ri.quantity()));
            String firstImage = (p.getImages() != null && !p.getImages().isEmpty())
                    ? p.getImages().get(0) : null;

            OrderItem item = OrderItem.builder()
                    .order(order)
                    .product(p)
                    .dropiProductId(p.getDropiProductId())
                    .productName(p.getName())
                    .productImage(firstImage)
                    .productSlug(p.getSlug())
                    .unitPrice(p.getPrice())
                    .quantity(ri.quantity())
                    .subtotal(itemSubtotal)
                    .build();
            items.add(item);
        }
        orderItemRepository.saveAll(items);

        // 8. Incrementar estadísticas de carrito
        for (ResolvedItem ri : resolvedItems) {
            productStatsService.incrementCartAdds(ri.product().getId());
        }

        // 9. Si es COD → confirmar y enviar a N8n inmediatamente (async via N8nService)
        //    Para MP/Wompi se envía después de confirmar el webhook de pago
        if (request.getPaymentMethod() == Order.PaymentMethod.COD) {
            order.setPaymentStatus(Order.PaymentStatus.CONFIRMED);
            order.setOrderStatus(Order.OrderStatus.CONFIRMED);
            orderRepository.save(order);
            // N8nService.sendOrderToN8n está anotado con @Async → ejecuta en thread pool
            n8nService.sendOrderToN8n(order);
        }

        log.info("Pedido {} creado — método: {}, total: {}", orderCode, request.getPaymentMethod(), total);
        return toResponse(order, items);
    }

    // ─── Consultas ────────────────────────────────────────────────────────────

    public OrderResponse getByCode(String orderCode) {
        Order order = findByCode(orderCode);
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        return toResponse(order, items);
    }

    public TrackingResponse getTracking(String orderCode) {
        Order order = findByCode(orderCode);
        return toTracking(order);
    }

    // ─── Privados ─────────────────────────────────────────────────────────────

    private Order findByCode(String orderCode) {
        return orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> OrderException.notFound(orderCode));
    }

    private String generateOrderCode() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        long count = orderRepository.countOrdersForDay(startOfDay, endOfDay);
        return String.format("NOVA-%s-%04d", date, count + 1);
    }

    private List<ResolvedItem> resolveItems(List<CreateOrderRequest.OrderItemRequest> requests) {
        List<ResolvedItem> resolved = new ArrayList<>();
        for (CreateOrderRequest.OrderItemRequest req : requests) {
            Product product = productRepository.findBySlugAndStatus(req.getProductSlug(), Product.Status.ACTIVE)
                    .orElseThrow(() -> OrderException.productNotAvailable(req.getProductSlug()));

            if (!Boolean.TRUE.equals(product.getInStock())) {
                throw OrderException.insufficientStock(product.getName());
            }

            resolved.add(new ResolvedItem(product, req.getQuantity()));
        }
        return resolved;
    }

    // ─── Mappers ──────────────────────────────────────────────────────────────

    private OrderResponse toResponse(Order order, List<OrderItem> items) {
        List<OrderItemResponse> itemResponses = items.stream()
                .map(i -> OrderItemResponse.builder()
                        .productName(i.getProductName())
                        .productImage(i.getProductImage())
                        .productSlug(i.getProductSlug())
                        .unitPrice(i.getUnitPrice())
                        .quantity(i.getQuantity())
                        .subtotal(i.getSubtotal())
                        .build())
                .toList();

        return OrderResponse.builder()
                .orderCode(order.getOrderCode())
                .customerName(order.getCustomerName())
                .customerEmail(maskEmail(order.getCustomerEmail()))
                .customerPhone(order.getCustomerPhone())
                .shippingAddress(order.getShippingAddress())
                .shippingCity(order.getShippingCity())
                .shippingDepartment(order.getShippingDepartment())
                .shippingNeighborhood(order.getShippingNeighborhood())
                .items(itemResponses)
                .subtotal(order.getSubtotal())
                .shippingCost(order.getShippingCost())
                .total(order.getTotal())
                .currency(order.getCurrency())
                .paymentMethod(order.getPaymentMethod().name())
                .paymentStatus(order.getPaymentStatus().name())
                .orderStatus(order.getOrderStatus().name())
                .createdAt(order.getCreatedAt())
                .build();
    }

    private TrackingResponse toTracking(Order order) {
        List<TrackingResponse.TrackingEvent> timeline = buildTimeline(order);

        return TrackingResponse.builder()
                .orderCode(order.getOrderCode())
                .orderStatus(order.getOrderStatus().name())
                .paymentStatus(order.getPaymentStatus().name())
                .customerName(order.getCustomerName())
                .dropiOrderId(order.getDropiOrderId())
                .timeline(timeline)
                .build();
    }

    private List<TrackingResponse.TrackingEvent> buildTimeline(Order order) {
        // Orden de estados en la línea de tiempo
        record StatusMeta(Order.OrderStatus status, String label, String description) {}
        List<StatusMeta> pipeline = List.of(
                new StatusMeta(Order.OrderStatus.CONFIRMED,  "Pedido confirmado",   "Tu pedido fue recibido y confirmado"),
                new StatusMeta(Order.OrderStatus.PROCESSING, "En preparación",      "El proveedor está preparando tu pedido"),
                new StatusMeta(Order.OrderStatus.SHIPPED,    "En camino",           "Tu pedido está en camino"),
                new StatusMeta(Order.OrderStatus.DELIVERED,  "Entregado",           "Tu pedido fue entregado")
        );

        Order.OrderStatus current = order.getOrderStatus();
        boolean reached = false;

        List<TrackingResponse.TrackingEvent> events = new ArrayList<>();

        // Agregar evento de pedido creado siempre
        events.add(TrackingResponse.TrackingEvent.builder()
                .status("PENDING")
                .label("Pedido recibido")
                .description("Tu pedido fue recibido")
                .date(order.getCreatedAt())
                .completed(true)
                .build());

        // Cancelado / Devuelto — caso especial
        if (current == Order.OrderStatus.CANCELLED || current == Order.OrderStatus.RETURNED) {
            events.add(TrackingResponse.TrackingEvent.builder()
                    .status(current.name())
                    .label(current == Order.OrderStatus.CANCELLED ? "Cancelado" : "Devuelto")
                    .description(current == Order.OrderStatus.CANCELLED
                            ? "El pedido fue cancelado"
                            : "El pedido fue devuelto")
                    .date(order.getUpdatedAt())
                    .completed(true)
                    .build());
            return events;
        }

        for (StatusMeta meta : pipeline) {
            boolean isCurrentOrPast = isStatusReachedOrPast(current, meta.status());
            events.add(TrackingResponse.TrackingEvent.builder()
                    .status(meta.status().name())
                    .label(meta.label())
                    .description(meta.description())
                    .date(isCurrentOrPast ? order.getUpdatedAt() : null)
                    .completed(isCurrentOrPast)
                    .build());
        }

        return events;
    }

    /**
     * Determina si un estado objetivo ya fue alcanzado o superado en el flujo normal del pedido.
     * Flujo normal: CONFIRMED → PROCESSING → SHIPPED → DELIVERED
     */
    private boolean isStatusReachedOrPast(Order.OrderStatus current, Order.OrderStatus target) {
        List<Order.OrderStatus> flow = List.of(
                Order.OrderStatus.CONFIRMED,
                Order.OrderStatus.PROCESSING,
                Order.OrderStatus.SHIPPED,
                Order.OrderStatus.DELIVERED
        );
        int currentIdx = flow.indexOf(current);
        int targetIdx  = flow.indexOf(target);
        if (currentIdx < 0 || targetIdx < 0) return false;
        return currentIdx >= targetIdx;
    }

    /** Enmascara el email: "juan@ejemplo.com" → "j***@ejemplo.com" */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        String[] parts = email.split("@");
        String local = parts[0];
        String masked = (local.length() > 1) ? local.charAt(0) + "***" : "***";
        return masked + "@" + parts[1];
    }

    // ─── Record interno ───────────────────────────────────────────────────────

    private record ResolvedItem(Product product, int quantity) {}
}
