package com.novainvesa.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders",
        indexes = {
                @Index(name = "idx_order_code",     columnList = "order_code"),
                @Index(name = "idx_user",           columnList = "user_id"),
                @Index(name = "idx_order_status",   columnList = "order_status"),
                @Index(name = "idx_payment_status", columnList = "payment_status"),
                @Index(name = "idx_created_at",     columnList = "created_at")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    public enum PaymentMethod  { COD, WOMPI, MERCADOPAGO }
    public enum PaymentStatus  { PENDING, CONFIRMED, FAILED, REFUNDED }
    public enum OrderStatus    { PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, RETURNED, CANCELLED }
    public enum DropiSyncStatus { PENDING, SUCCESS, FAILED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Formato NOVA-YYYYMMDD-NNNN */
    @Column(name = "order_code", nullable = false, unique = true, length = 30)
    private String orderCode;

    /** NULL para pedidos de invitados */
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    // ── Datos del cliente (copiados al momento del pedido) ──────────────

    @Column(name = "customer_name", nullable = false, length = 255)
    private String customerName;

    @Column(name = "customer_email", nullable = false, length = 255)
    private String customerEmail;

    @Column(name = "customer_phone", length = 20)
    private String customerPhone;

    @Column(name = "customer_id_number", length = 20)
    private String customerIdNumber;

    // ── Dirección de envío ───────────────────────────────────────────────

    @Column(name = "shipping_department", nullable = false, length = 100)
    private String shippingDepartment;

    @Column(name = "shipping_city", nullable = false, length = 100)
    private String shippingCity;

    @Column(name = "shipping_address", nullable = false, length = 255)
    private String shippingAddress;

    @Column(name = "shipping_neighborhood", length = 100)
    private String shippingNeighborhood;

    @Column(name = "shipping_notes", length = 255)
    private String shippingNotes;

    // ── Totales ──────────────────────────────────────────────────────────

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "shipping_cost", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal shippingCost = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "COP";

    // ── Pago ─────────────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "wompi_transaction_id", length = 100)
    private String wompiTransactionId;

    @Column(name = "mp_payment_id", length = 100)
    private String mpPaymentId;

    @Column(name = "mp_preference_id", length = 100)
    private String mpPreferenceId;

    // ── Estado del pedido ────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 20)
    @Builder.Default
    private OrderStatus orderStatus = OrderStatus.PENDING;

    // ── Integración Dropi / N8n ──────────────────────────────────────────

    @Column(name = "dropi_order_id", length = 100)
    private String dropiOrderId;

    @Column(name = "n8n_job_id", length = 100)
    private String n8nJobId;

    @Enumerated(EnumType.STRING)
    @Column(name = "dropi_sync_status", length = 10)
    @Builder.Default
    private DropiSyncStatus dropiSyncStatus = DropiSyncStatus.PENDING;

    @Column(name = "dropi_sync_attempts", nullable = false)
    @Builder.Default
    private Integer dropiSyncAttempts = 0;

    @Column(name = "dropi_sync_error", columnDefinition = "TEXT")
    private String dropiSyncError;

    // ── Metadatos ────────────────────────────────────────────────────────

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
