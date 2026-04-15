package com.novainvesa.backend.service;

import com.novainvesa.backend.dto.CategoryResponse;
import com.novainvesa.backend.entity.Category;
import com.novainvesa.backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de categorías para la API pública.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * Retorna todas las categorías activas ordenadas por sortOrder ascendente.
     */
    public List<CategoryResponse> getActiveCategories() {
        return categoryRepository.findByActiveOrderBySortOrderAsc(true)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private CategoryResponse toResponse(Category c) {
        return CategoryResponse.builder()
                .id(c.getId())
                .slug(c.getSlug())
                .name(c.getName())
                .description(c.getDescription())
                .icon(c.getIcon())
                .color(c.getColor())
                .sortOrder(c.getSortOrder())
                .build();
    }
}
