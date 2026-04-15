package com.novainvesa.backend.service;

import com.novainvesa.backend.dto.PaginatedResponse;
import com.novainvesa.backend.dto.ProductDetailResponse;
import com.novainvesa.backend.dto.ProductSummaryResponse;
import com.novainvesa.backend.entity.Product;
import com.novainvesa.backend.exception.ResourceNotFoundException;
import com.novainvesa.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * Servicio de productos para la API pública.
 * readOnly = true por defecto; solo los métodos que escriben usan @Transactional sin readonly.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductService {

    private static final Set<String> VALID_SORTS = Set.of("newest", "price_asc", "price_desc");

    private final ProductRepository productRepository;
    private final ProductStatsService productStatsService;

    /**
     * Listado paginado con filtros opcionales de categoría, destacado y orden.
     */
    public PaginatedResponse<ProductSummaryResponse> getProducts(
            String category, Boolean featured, String sort,
            int page, int size) {

        // Validar parámetros
        page = Math.max(0, page);
        size = Math.min(Math.max(1, size), 100);
        if (sort == null || !VALID_SORTS.contains(sort)) {
            sort = "newest";
        }

        Pageable pageable = buildPageable(page, size, sort);
        Page<Product> result = queryProducts(category, featured, pageable);

        return toPaginatedResponse(result, page, size);
    }

    /**
     * Búsqueda de productos por texto.
     */
    public List<ProductSummaryResponse> searchProducts(String query, int limit) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        int safeLimit = Math.min(Math.max(1, limit), 50);
        log.debug("Búsqueda de productos con query='{}'", query.trim());
        Pageable pageable = PageRequest.of(0, safeLimit);
        return productRepository.searchProducts(query.trim(), pageable)
                .stream()
                .map(this::toSummary)
                .toList();
    }

    /**
     * Detalle de un producto por slug. Incrementa el viewCount de forma transaccional.
     */
    @Transactional
    public ProductDetailResponse getBySlug(String slug) {
        log.debug("Buscando producto por slug='{}'", slug);
        Product product = productRepository.findBySlugAndStatus(slug, Product.Status.ACTIVE)
                .orElseThrow(() -> ResourceNotFoundException.product(slug));

        // Incrementar viewCount en product_stats y en products
        productStatsService.incrementViewCount(product.getId());
        productRepository.incrementViewCount(product.getId());

        return toDetail(product);
    }

    // ─── Helpers privados ────────────────────────────────────────────────────

    private Page<Product> queryProducts(String category, Boolean featured, Pageable pageable) {
        boolean hasCategory = category != null && !category.isBlank();
        boolean hasFeatured = featured != null;

        if (hasCategory && hasFeatured) {
            return productRepository.findByStatusAndCategorySlugAndFeaturedAndActiveTrue(
                    Product.Status.ACTIVE, category, featured, pageable);
        } else if (hasCategory) {
            return productRepository.findByStatusAndCategorySlugAndActiveTrue(
                    Product.Status.ACTIVE, category, pageable);
        } else if (hasFeatured) {
            return productRepository.findByStatusAndFeaturedAndActiveTrue(
                    Product.Status.ACTIVE, featured, pageable);
        } else {
            return productRepository.findByStatusAndActiveTrue(Product.Status.ACTIVE, pageable);
        }
    }

    private Pageable buildPageable(int page, int size, String sort) {
        Sort sortObj = switch (sort) {
            case "price_asc"  -> Sort.by(Sort.Direction.ASC,  "price");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "price");
            default           -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
        return PageRequest.of(page, size, sortObj);
    }

    private PaginatedResponse<ProductSummaryResponse> toPaginatedResponse(
            Page<Product> result, int page, int size) {
        List<ProductSummaryResponse> items = result.getContent()
                .stream()
                .map(this::toSummary)
                .toList();

        return PaginatedResponse.<ProductSummaryResponse>builder()
                .items(items)
                .page(page)
                .size(size)
                .totalItems(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .hasNext(result.hasNext())
                .hasPrevious(result.hasPrevious())
                .build();
    }

    private ProductSummaryResponse toSummary(Product p) {
        return ProductSummaryResponse.builder()
                .id(p.getId())
                .slug(p.getSlug())
                .name(p.getName())
                .shortDescription(p.getShortDescription())
                .price(p.getPrice())
                .compareAtPrice(p.getCompareAtPrice())
                .primaryImage(primaryImage(p))
                .categorySlug(p.getCategorySlug())
                .featured(Boolean.TRUE.equals(p.getFeatured()))
                .inStock(Boolean.TRUE.equals(p.getInStock()))
                .discountPercentage(calculateDiscount(p.getPrice(), p.getCompareAtPrice()))
                .build();
    }

    private ProductDetailResponse toDetail(Product p) {
        return ProductDetailResponse.builder()
                .id(p.getId())
                .slug(p.getSlug())
                .name(p.getName())
                .shortDescription(p.getShortDescription())
                .price(p.getPrice())
                .compareAtPrice(p.getCompareAtPrice())
                .primaryImage(primaryImage(p))
                .categorySlug(p.getCategorySlug())
                .featured(Boolean.TRUE.equals(p.getFeatured()))
                .inStock(Boolean.TRUE.equals(p.getInStock()))
                .discountPercentage(calculateDiscount(p.getPrice(), p.getCompareAtPrice()))
                .description(p.getDescription())
                .images(p.getImages())
                .benefits(p.getBenefits())
                .viewCount(p.getViewCount())
                .createdAt(p.getCreatedAt())
                .build();
    }

    private String primaryImage(Product p) {
        return (p.getImages() != null && !p.getImages().isEmpty())
                ? p.getImages().get(0)
                : null;
    }

    private Integer calculateDiscount(BigDecimal price, BigDecimal compareAtPrice) {
        if (compareAtPrice == null || compareAtPrice.compareTo(price) <= 0) {
            return null;
        }
        double discount = (1.0 - price.doubleValue() / compareAtPrice.doubleValue()) * 100;
        return (int) Math.round(discount);
    }
}
