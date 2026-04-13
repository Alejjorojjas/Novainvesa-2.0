package com.novainvesa.backend.repository;

import com.novainvesa.backend.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySlug(String slug);

    Optional<Product> findByDropiProductId(String dropiProductId);

    boolean existsByDropiProductId(String dropiProductId);

    Page<Product> findByCategorySlugAndStatusAndActiveTrue(
            String categorySlug, Product.Status status, Pageable pageable);

    Page<Product> findByStatusAndActiveTrue(Product.Status status, Pageable pageable);

    Page<Product> findByFeaturedTrueAndStatusAndActiveTrue(
            Product.Status status, Pageable pageable);
}
