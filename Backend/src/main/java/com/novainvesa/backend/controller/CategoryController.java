package com.novainvesa.backend.controller;

import com.novainvesa.backend.dto.ApiResponse;
import com.novainvesa.backend.dto.CategoryResponse;
import com.novainvesa.backend.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints públicos de categorías.
 * GET /api/v1/categories
 */
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Listar todas las categorías activas, ordenadas por sortOrder.
     * GET /api/v1/categories
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(
                categoryService.getActiveCategories()));
    }
}
