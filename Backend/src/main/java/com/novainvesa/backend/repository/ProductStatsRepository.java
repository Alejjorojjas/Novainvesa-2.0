package com.novainvesa.backend.repository;

import com.novainvesa.backend.entity.ProductStats;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductStatsRepository extends JpaRepository<ProductStats, Long> {

    Optional<ProductStats> findByProductId(Long productId);

    List<ProductStats> findAllByOrderByUnitsSoldDesc(Pageable pageable);

    List<ProductStats> findAllByOrderByViewsDesc(Pageable pageable);
}
