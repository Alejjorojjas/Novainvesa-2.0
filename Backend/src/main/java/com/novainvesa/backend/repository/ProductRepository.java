package com.novainvesa.backend.repository;

import com.novainvesa.backend.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    // ─── Nuevos métodos para filtros combinados ─────────────────────────────

    Page<Product> findByStatusAndCategorySlugAndFeaturedAndActiveTrue(
            Product.Status status, String categorySlug, Boolean featured, Pageable pageable);

    Page<Product> findByStatusAndCategorySlugAndActiveTrue(
            Product.Status status, String categorySlug, Pageable pageable);

    Page<Product> findByStatusAndFeaturedAndActiveTrue(
            Product.Status status, Boolean featured, Pageable pageable);

    Optional<Product> findBySlugAndStatus(String slug, Product.Status status);

    // ─── Búsqueda full-text (LIKE por ahora) ───────────────────────────────

    @Query("SELECT p FROM Product p WHERE p.status = 'ACTIVE' AND p.active = true AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.shortDescription) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Product> searchProducts(@Param("query") String query, Pageable pageable);

    // ─── Incrementar viewCount directamente en BD ──────────────────────────

    @Modifying
    @Query("UPDATE Product p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id")
    void incrementViewCount(@Param("id") Long id);

    // ─── Admin: verificar slug único para importación ──────────────────────

    boolean existsBySlug(String slug);

    // ─── Admin: listado por estado (incluye todos los estados) ───────────

    Page<Product> findByStatus(Product.Status status, Pageable pageable);
}
