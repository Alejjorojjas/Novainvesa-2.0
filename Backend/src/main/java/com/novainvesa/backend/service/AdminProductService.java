package com.novainvesa.backend.service;

import com.novainvesa.backend.dto.BulkImportResponse;
import com.novainvesa.backend.dto.ImportJobResponse;
import com.novainvesa.backend.dto.PaginatedResponse;
import com.novainvesa.backend.dto.ProductAdminResponse;
import com.novainvesa.backend.dto.UpdateProductRequest;
import com.novainvesa.backend.entity.AdminUser;
import com.novainvesa.backend.entity.ImportJob;
import com.novainvesa.backend.entity.Product;
import com.novainvesa.backend.exception.ImportException;
import com.novainvesa.backend.exception.ResourceNotFoundException;
import com.novainvesa.backend.repository.AdminUserRepository;
import com.novainvesa.backend.repository.ImportJobRepository;
import com.novainvesa.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Lógica de negocio para la gestión de productos en el panel de administración.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminProductService {

    private final ProductRepository productRepository;
    private final ImportJobRepository importJobRepository;
    private final AdminUserRepository adminUserRepository;
    private final DropiImportService dropiImportService;

    // ─── Importación masiva ────────────────────────────────────────────────

    /**
     * Crea el ImportJob, lo persiste y lanza el proceso asíncrono.
     * Retorna el jobId para que el cliente pueda hacer polling.
     */
    @Transactional
    public String startBulkImport(List<String> inputs, Authentication auth) {
        String adminEmail = auth.getName();
        AdminUser admin = adminUserRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("NOT_FOUND", "Admin no encontrado"));

        String jobId = "bulk-" + UUID.randomUUID().toString().substring(0, 8);

        ImportJob job = ImportJob.builder()
                .jobId(jobId)
                .admin(admin)
                .total(inputs.size())
                .processed(0)
                .published(0)
                .drafts(0)
                .errors(0)
                .status(ImportJob.Status.PENDING)
                .build();

        importJobRepository.save(job);

        // Lanzar asíncronamente (@Async en DropiImportService)
        dropiImportService.processBulkImport(jobId, inputs, admin.getId());

        log.info("Job de importación masiva {} iniciado por admin {} con {} inputs",
                jobId, adminEmail, inputs.size());

        return jobId;
    }

    /**
     * Consulta el estado actual de un job de importación masiva.
     */
    public ImportJobResponse getJobStatus(String jobId) {
        ImportJob job = importJobRepository.findByJobId(jobId)
                .orElseThrow(() -> new ImportException("IMPORT_005", "Job no encontrado: " + jobId));

        return ImportJobResponse.builder()
                .jobId(job.getJobId())
                .total(job.getTotal())
                .processed(job.getProcessed())
                .published(job.getPublished())
                .drafts(job.getDrafts())
                .errors(job.getErrors())
                .status(job.getStatus().name())
                .createdAt(job.getCreatedAt())
                .completedAt(job.getCompletedAt())
                .build();
    }

    // ─── Listado de productos (admin) ──────────────────────────────────────

    /**
     * Lista todos los productos (incluye DRAFT y ARCHIVED).
     * El admin ve todos los estados a diferencia del catálogo público.
     */
    public PaginatedResponse<ProductAdminResponse> listAll(String statusFilter, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Product> productsPage;

        if (statusFilter != null && !statusFilter.isBlank()) {
            try {
                Product.Status status = Product.Status.valueOf(statusFilter.toUpperCase());
                productsPage = productRepository.findByStatus(status, pageable);
            } catch (IllegalArgumentException e) {
                productsPage = productRepository.findAll(pageable);
            }
        } else {
            productsPage = productRepository.findAll(pageable);
        }

        List<ProductAdminResponse> items = productsPage.getContent()
                .stream()
                .map(this::toAdminResponse)
                .toList();

        return PaginatedResponse.<ProductAdminResponse>builder()
                .items(items)
                .page(page)
                .size(size)
                .totalItems(productsPage.getTotalElements())
                .totalPages(productsPage.getTotalPages())
                .hasNext(productsPage.hasNext())
                .hasPrevious(productsPage.hasPrevious())
                .build();
    }

    // ─── Actualización de producto ─────────────────────────────────────────

    /**
     * Actualiza los campos de un producto.
     * Recalcula missingFields y actualiza el status automáticamente.
     */
    @Transactional
    public ProductAdminResponse update(Long id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PRODUCT_001", "Producto no encontrado: " + id));

        // Actualizar solo los campos no nulos
        if (request.getName() != null) product.setName(request.getName());
        if (request.getShortDescription() != null) product.setShortDescription(request.getShortDescription());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getCompareAtPrice() != null) product.setCompareAtPrice(request.getCompareAtPrice());
        if (request.getImages() != null) product.setImages(request.getImages());
        if (request.getBenefits() != null) product.setBenefits(request.getBenefits());
        if (request.getCategorySlug() != null) product.setCategorySlug(request.getCategorySlug());
        if (request.getFeatured() != null) product.setFeatured(request.getFeatured());
        if (request.getInStock() != null) product.setInStock(request.getInStock());

        // Recalcular campos faltantes
        List<String> missingFields = recalculateMissingFields(product);

        // Si el producto era DRAFT y ahora tiene todo → pasar a ACTIVE automáticamente
        if (product.getStatus() == Product.Status.DRAFT && missingFields.isEmpty()) {
            product.setStatus(Product.Status.ACTIVE);
            log.info("Producto {} promovido automáticamente a ACTIVE tras actualización", id);
        }

        product.setMissingFields(missingFields.isEmpty() ? null : missingFields);
        return toAdminResponse(productRepository.save(product));
    }

    // ─── Publicar / Archivar ───────────────────────────────────────────────

    /**
     * Publica un producto DRAFT.
     * Verifica que todos los campos requeridos estén completos (RN-003).
     */
    @Transactional
    public ProductAdminResponse publish(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PRODUCT_001", "Producto no encontrado: " + id));

        List<String> missingFields = recalculateMissingFields(product);
        if (!missingFields.isEmpty()) {
            throw new ImportException("IMPORT_001",
                    "No se puede publicar — campos faltantes: " + String.join(", ", missingFields));
        }

        product.setStatus(Product.Status.ACTIVE);
        product.setMissingFields(null);
        log.info("Producto {} publicado manualmente", id);
        return toAdminResponse(productRepository.save(product));
    }

    /**
     * Archiva un producto. Mantiene los pedidos históricos intactos (RN-002).
     */
    @Transactional
    public ProductAdminResponse archive(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PRODUCT_001", "Producto no encontrado: " + id));

        product.setStatus(Product.Status.ARCHIVED);
        log.info("Producto {} archivado", id);
        return toAdminResponse(productRepository.save(product));
    }

    // ─── Mapeo ─────────────────────────────────────────────────────────────

    public ProductAdminResponse toAdminResponse(Product product) {
        return ProductAdminResponse.builder()
                .id(product.getId())
                .dropiProductId(product.getDropiProductId())
                .name(product.getName())
                .slug(product.getSlug())
                .categorySlug(product.getCategorySlug())
                .shortDescription(product.getShortDescription())
                .description(product.getDescription())
                .price(product.getPrice())
                .compareAtPrice(product.getCompareAtPrice())
                .currency(product.getCurrency())
                .images(product.getImages())
                .benefits(product.getBenefits())
                .status(product.getStatus().name())
                .missingFields(product.getMissingFields())
                .inStock(product.getInStock())
                .featured(product.getFeatured())
                .importedAt(product.getImportedAt())
                .viewCount(product.getViewCount())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    // ─── Privados ──────────────────────────────────────────────────────────

    /** Recalcula campos faltantes según RN-003. */
    private List<String> recalculateMissingFields(Product product) {
        List<String> missing = new ArrayList<>();

        if (product.getName() == null || product.getName().isBlank()) {
            missing.add("name");
        }
        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            missing.add("price");
        }
        if (product.getImages() == null || product.getImages().isEmpty()) {
            missing.add("images");
        }
        if (product.getCategorySlug() == null || product.getCategorySlug().isBlank()
                || product.getCategorySlug().equals("sin-categoria")) {
            missing.add("categorySlug");
        }
        if (product.getDropiProductId() == null || product.getDropiProductId().isBlank()) {
            missing.add("dropiProductId");
        }

        return missing;
    }
}
