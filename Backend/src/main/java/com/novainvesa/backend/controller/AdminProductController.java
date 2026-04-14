package com.novainvesa.backend.controller;

import com.novainvesa.backend.dto.ApiResponse;
import com.novainvesa.backend.dto.BulkImportRequest;
import com.novainvesa.backend.dto.BulkImportResponse;
import com.novainvesa.backend.dto.DropiProductPreviewDto;
import com.novainvesa.backend.dto.ImportJobResponse;
import com.novainvesa.backend.dto.ImportProductRequest;
import com.novainvesa.backend.dto.PaginatedResponse;
import com.novainvesa.backend.dto.PreviewRequest;
import com.novainvesa.backend.dto.ProductAdminResponse;
import com.novainvesa.backend.dto.UpdateProductRequest;
import com.novainvesa.backend.entity.Product;
import com.novainvesa.backend.exception.ImportException;
import com.novainvesa.backend.service.AdminProductService;
import com.novainvesa.backend.service.DropiImportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Importador de productos Dropi y gestión del catálogo desde el panel de administración.
 * RN-043: Solo ADMIN o SUPER_ADMIN pueden importar y administrar productos.
 */
@RestController
@RequestMapping("/api/v1/admin/products")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
@RequiredArgsConstructor
public class AdminProductController {

    private final DropiImportService dropiImportService;
    private final AdminProductService adminProductService;

    // ─── Preview ───────────────────────────────────────────────────────────

    /**
     * POST /api/v1/admin/products/preview
     * Consulta el producto en Dropi por ID numérico o URL — no lo guarda.
     */
    @PostMapping("/preview")
    public ResponseEntity<ApiResponse<DropiProductPreviewDto>> preview(
            @Valid @RequestBody PreviewRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                dropiImportService.previewProduct(request.getInput())));
    }

    // ─── Importación individual ────────────────────────────────────────────

    /**
     * POST /api/v1/admin/products/import
     * Importa un producto individual. Queda en DRAFT si le faltan campos requeridos (RN-003).
     */
    @PostMapping("/import")
    public ResponseEntity<ApiResponse<ProductAdminResponse>> importOne(
            @Valid @RequestBody ImportProductRequest request) {
        Product product = dropiImportService.importProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(adminProductService.toAdminResponse(product)));
    }

    // ─── Importación masiva ────────────────────────────────────────────────

    /**
     * POST /api/v1/admin/products/import-bulk
     * Inicia importación asíncrona de hasta 50 productos (RN-005).
     * Retorna jobId para hacer polling del progreso.
     */
    @PostMapping("/import-bulk")
    public ResponseEntity<ApiResponse<BulkImportResponse>> importBulk(
            @Valid @RequestBody BulkImportRequest request,
            Authentication auth) {

        // RN-005: máximo 50 productos
        if (request.getInputs().size() > 50) {
            throw new ImportException("IMPORT_002", "Máximo 50 productos por importación masiva");
        }

        String jobId = adminProductService.startBulkImport(request.getInputs(), auth);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success(new BulkImportResponse(
                        jobId, request.getInputs().size(), "PROCESSING")));
    }

    /**
     * GET /api/v1/admin/products/import-bulk/{jobId}
     * Consulta el progreso de un job de importación masiva.
     */
    @GetMapping("/import-bulk/{jobId}")
    public ResponseEntity<ApiResponse<ImportJobResponse>> bulkStatus(
            @PathVariable String jobId) {
        return ResponseEntity.ok(ApiResponse.success(
                adminProductService.getJobStatus(jobId)));
    }

    // ─── Listado de productos ──────────────────────────────────────────────

    /**
     * GET /api/v1/admin/products
     * Lista todos los productos incluyendo DRAFT y ARCHIVED.
     * El catálogo público solo muestra ACTIVE.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<ProductAdminResponse>>> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                adminProductService.listAll(status, page, size)));
    }

    // ─── Edición ───────────────────────────────────────────────────────────

    /**
     * PUT /api/v1/admin/products/{id}
     * Actualiza campos de un producto. Recalcula missingFields automáticamente.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductAdminResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                adminProductService.update(id, request)));
    }

    // ─── Publicar / Archivar ───────────────────────────────────────────────

    /**
     * POST /api/v1/admin/products/{id}/publish
     * Publica un producto DRAFT si tiene todos los campos requeridos.
     */
    @PostMapping("/{id}/publish")
    public ResponseEntity<ApiResponse<ProductAdminResponse>> publish(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(adminProductService.publish(id)));
    }

    /**
     * POST /api/v1/admin/products/{id}/archive
     * Archiva un producto. Mantiene pedidos históricos intactos.
     */
    @PostMapping("/{id}/archive")
    public ResponseEntity<ApiResponse<ProductAdminResponse>> archive(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(adminProductService.archive(id)));
    }
}
