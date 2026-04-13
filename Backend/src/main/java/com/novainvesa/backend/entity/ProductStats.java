package com.novainvesa.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_stats")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductStats {

    /** La PK es también FK a products */
    @Id
    @Column(name = "product_id")
    private Long productId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    @Builder.Default
    private Long views = 0L;

    @Column(name = "cart_adds", nullable = false)
    @Builder.Default
    private Long cartAdds = 0L;

    @Column(name = "wishlist_count", nullable = false)
    @Builder.Default
    private Integer wishlistCount = 0;

    @Column(name = "orders_count", nullable = false)
    @Builder.Default
    private Integer ordersCount = 0;

    @Column(name = "units_sold", nullable = false)
    @Builder.Default
    private Integer unitsSold = 0;

    @Column(name = "total_revenue", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    @PrePersist
    @PreUpdate
    void updateTimestamp() {
        lastUpdated = LocalDateTime.now();
    }
}
