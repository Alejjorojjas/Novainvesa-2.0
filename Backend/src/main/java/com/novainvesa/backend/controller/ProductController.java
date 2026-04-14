package com.novainvesa.backend.controller;

import com.novainvesa.backend.dto.ApiResponse;
import com.novainvesa.backend.dto.PaginatedResponse;
import com.novainvesa.backend.dto.ProductDetailResponse;
import com.novainvesa.backend.dto.ProductSummaryResponse;
import com.novainvesa.backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints públicos de productos.
 * GET /api/v1/products
 * GET /api/v1/products/search
 * GET /api/v1/products/{slug}
 */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    /**
     * Listar productos activos con filtros opcionales.
     * GET /api/v1/products?category=fitness&featured=true&sort=newest&page=0&size=20
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<ProductSummaryResponse>>> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(ApiResponse.success(
                productService.getProducts(category, featured, sort, page, size)));
    }

    /**
     * Buscar productos por texto.
     * GET /api/v1/products/search?q=ejercitador&limit=10
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProductSummaryResponse>>> search(
            @RequestParam("q") String query,
            @RequestParam(defaultValue = "10") int limit) {

        return ResponseEntity.ok(ApiResponse.success(
                productService.searchProducts(query, limit)));
    }

    /**
     * Detalle de un producto por slug.
     * GET /api/v1/products/{slug}
     */
    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getBySlug(
            @PathVariable String slug) {

        return ResponseEntity.ok(ApiResponse.success(
                productService.getBySlug(slug)));
    }
}
