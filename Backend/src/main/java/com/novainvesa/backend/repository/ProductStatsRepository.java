package com.novainvesa.backend.repository;

import com.novainvesa.backend.entity.ProductStats;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductStatsRepository extends JpaRepository<ProductStats, Long> {

    Optional<ProductStats> findByProductId(Long productId);

    List<ProductStats> findAllByOrderByUnitsSoldDesc(Pageable pageable);

    List<ProductStats> findAllByOrderByViewsDesc(Pageable pageable);

    // ─── Incrementar contadores directamente (sin cargar la entidad) ─────────

    @Modifying
    @Query("UPDATE ProductStats ps SET ps.views = ps.views + 1, ps.lastUpdated = CURRENT_TIMESTAMP WHERE ps.productId = :id")
    void incrementViewCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE ProductStats ps SET ps.cartAdds = ps.cartAdds + 1, ps.lastUpdated = CURRENT_TIMESTAMP WHERE ps.productId = :id")
    void incrementCartAdds(@Param("id") Long id);

    @Modifying
    @Query("UPDATE ProductStats ps SET ps.wishlistCount = ps.wishlistCount + 1, ps.lastUpdated = CURRENT_TIMESTAMP WHERE ps.productId = :id")
    void incrementWishlistCount(@Param("id") Long id);
}
