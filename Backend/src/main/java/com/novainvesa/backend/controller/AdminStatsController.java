package com.novainvesa.backend.controller;

import com.novainvesa.backend.dto.ApiResponse;
import com.novainvesa.backend.dto.PaginatedResponse;
import com.novainvesa.backend.dto.ProductStatAdminResponse;
import com.novainvesa.backend.entity.Product;
import com.novainvesa.backend.entity.ProductStats;
import com.novainvesa.backend.repository.ProductRepository;
import com.novainvesa.backend.repository.ProductStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Métricas de productos para el panel de administración.
 */
@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
@RequiredArgsConstructor
public class AdminStatsController {

    private final ProductStatsRepository productStatsRepository;
    private final ProductRepository productRepository;

    /**
     * GET /api/v1/admin/product-stats
     * Listado de estadísticas de productos ordenadas por vistas descendentes.
     */
    @GetMapping("/product-stats")
    public ResponseEntity<ApiResponse<PaginatedResponse<ProductStatAdminResponse>>> getProductStats(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<ProductStats> statsList = productStatsRepository
                .findAllByOrderByViewsDesc(PageRequest.of(page, size));

        long totalItems = productStatsRepository.count();
        int totalPages = (int) Math.ceil((double) totalItems / size);

        List<ProductStatAdminResponse> items = statsList.stream()
                .map(this::toResponse)
                .toList();

        PaginatedResponse<ProductStatAdminResponse> response = PaginatedResponse.<ProductStatAdminResponse>builder()
                .items(items)
                .page(page)
                .size(size)
                .totalItems(totalItems)
                .totalPages(totalPages)
                .hasNext((page + 1) * size < totalItems)
                .hasPrevious(page > 0)
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ─── Mapeo ─────────────────────────────────────────────────────────────

    private ProductStatAdminResponse toResponse(ProductStats stats) {
        String productName = "";
        String productSlug = "";

        if (stats.getProduct() != null) {
            productName = stats.getProduct().getName();
            productSlug = stats.getProduct().getSlug();
        } else {
            Product product = productRepository.findById(stats.getProductId()).orElse(null);
            if (product != null) {
                productName = product.getName();
                productSlug = product.getSlug();
            }
        }

        return ProductStatAdminResponse.builder()
                .productId(stats.getProductId())
                .productName(productName)
                .productSlug(productSlug)
                .views(stats.getViews())
                .cartAdds(stats.getCartAdds())
                .wishlistCount(stats.getWishlistCount())
                .ordersCount(stats.getOrdersCount())
                .unitsSold(stats.getUnitsSold())
                .totalRevenue(stats.getTotalRevenue())
                .build();
    }
}
