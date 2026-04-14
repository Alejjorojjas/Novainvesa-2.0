package com.novainvesa.backend.repository;

import com.novainvesa.backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findBySlug(String slug);

    List<Category> findByActiveTrueOrderBySortOrderAsc();

    // ─── Nuevos métodos ─────────────────────────────────────────────────────

    List<Category> findByActiveOrderBySortOrderAsc(boolean active);

    Optional<Category> findBySlugAndActive(String slug, boolean active);
}
