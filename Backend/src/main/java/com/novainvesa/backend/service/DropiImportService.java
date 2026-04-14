package com.novainvesa.backend.service;

import com.novainvesa.backend.dto.DropiProductPreviewDto;
import com.novainvesa.backend.dto.ImportProductRequest;
import com.novainvesa.backend.entity.ImportJob;
import com.novainvesa.backend.entity.Product;
import com.novainvesa.backend.exception.ImportException;
import com.novainvesa.backend.repository.ImportJobRepository;
import com.novainvesa.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Servicio de importación de productos desde la API de Dropi.
 * Implementa el importador descrito en RN-003, RN-005, RN-043.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DropiImportService {

    @Value("${app.dropi.api-url:https://api.dropi.co}")
    private String dropiApiUrl;

    @Value("${app.dropi.integration-key:}")
    private String dropiIntegrationKey;

    private final ProductRepository productRepository;
    private final ImportJobRepository importJobRepository;
    private final RestTemplate restTemplate;

    // ─── Extracción del ID de Dropi ────────────────────────────────────────

    /**
     * Detecta si el input es un ID numérico o una URL de Dropi y extrae el ID.
     * Lanza ImportException IMPORT_001 si el formato no es reconocido.
     */
    public String extractDropiId(String input) {
        input = input.trim();

        // ID numérico puro
        if (input.matches("\\d+")) {
            return input;
        }

        // URL con segmento numérico: .../1865251 o .../1865251/... o .../1865251?...
        Pattern pattern = Pattern.compile("/(\\d+)(?:[/?#]|$)");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return matcher.group(1);
        }

        throw new ImportException("IMPORT_001", "Formato no reconocido: " + input);
    }

    // ─── Preview (consulta sin guardar) ───────────────────────────────────

    /**
     * Consulta la API de Dropi y retorna un preview del producto.
     * Si no hay credenciales configuradas, retorna un stub para desarrollo.
     */
    public DropiProductPreviewDto previewProduct(String input) {
        String dropiId = extractDropiId(input);

        if (dropiIntegrationKey == null || dropiIntegrationKey.isBlank()) {
            log.warn("DROPI_INTEGRATION_KEY no configurado — retornando preview stub para {}", dropiId);
            return buildStubPreview(dropiId);
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("dropi-integracion-key", dropiIntegrationKey);

            ResponseEntity<Map> response = restTemplate.exchange(
                    dropiApiUrl + "/api/v1/products/" + dropiId,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Map.class
            );

            return mapToPreviewDto(response.getBody(), dropiId);

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new ImportException("IMPORT_003", "Producto " + dropiId + " no encontrado en Dropi");
            }
            log.error("Error consultando Dropi para {}: {}", dropiId, e.getMessage());
            throw new ImportException("IMPORT_004", "Error de conexión con Dropi API");
        } catch (Exception e) {
            log.error("Error inesperado consultando Dropi para {}: {}", dropiId, e.getMessage());
            throw new ImportException("IMPORT_004", "Error de conexión con Dropi API");
        }
    }

    // ─── Importación individual ────────────────────────────────────────────

    /**
     * Importa un producto individual.
     * RN-003: si le faltan campos requeridos → DRAFT; si los tiene todos → ACTIVE.
     * Si el dropiProductId ya existe → log de advertencia pero se permite.
     */
    @Transactional
    public Product importProduct(ImportProductRequest request) {
        // Advertencia si ya existe (RN-005: no es error)
        if (productRepository.existsByDropiProductId(request.getDropiProductId())) {
            log.warn("dropiProductId {} ya existe en BD — se creará un producto adicional",
                    request.getDropiProductId());
        }

        String slug = generateUniqueSlug(request.getName());
        List<String> missingFields = determineMissingFields(request);
        Product.Status status = missingFields.isEmpty() ? Product.Status.ACTIVE : Product.Status.DRAFT;

        Product product = Product.builder()
                .name(request.getName())
                .slug(slug)
                .shortDescription(request.getShortDescription())
                .description(request.getDescription())
                .price(request.getPrice())
                .compareAtPrice(request.getCompareAtPrice())
                .categorySlug(request.getCategorySlug() != null ? request.getCategorySlug() : "sin-categoria")
                .images(request.getImages())
                .benefits(request.getBenefits())
                .dropiProductId(request.getDropiProductId())
                .featured(request.isFeatured())
                .inStock(true)
                .status(status)
                .missingFields(missingFields.isEmpty() ? null : missingFields)
                .importedAt(LocalDateTime.now())
                .viewCount(0L)
                .build();

        return productRepository.save(product);
    }

    // ─── Importación masiva asíncrona ──────────────────────────────────────

    /**
     * Procesa una importación masiva de forma asíncrona.
     * Actualiza el ImportJob con el progreso en tiempo real.
     * Los errores no detienen el proceso (RN-005).
     */
    @Async
    public void processBulkImport(String jobId, List<String> inputs, Long adminId) {
        ImportJob job = importJobRepository.findByJobId(jobId)
                .orElseThrow(() -> new ImportException("IMPORT_005", "Job no encontrado: " + jobId));

        job.setStatus(ImportJob.Status.PROCESSING);
        importJobRepository.save(job);

        for (String input : inputs) {
            try {
                DropiProductPreviewDto preview = previewProduct(input);

                ImportProductRequest req = ImportProductRequest.builder()
                        .dropiProductId(preview.getDropiProductId())
                        .name(preview.getName())
                        .shortDescription(preview.getShortDescription())
                        .description(preview.getDescription())
                        .price(preview.getPrice())
                        .compareAtPrice(preview.getCompareAtPrice())
                        .images(preview.getImages())
                        .benefits(preview.getBenefits())
                        // categorySlug nulo en bulk → DRAFT (admin asigna después)
                        .build();

                Product saved = importProduct(req);
                job.setProcessed(job.getProcessed() + 1);

                if (saved.getStatus() == Product.Status.ACTIVE) {
                    job.setPublished(job.getPublished() + 1);
                } else {
                    job.setDrafts(job.getDrafts() + 1);
                }

            } catch (Exception e) {
                log.error("Error importando {} en job {}: {}", input, jobId, e.getMessage());
                job.setErrors(job.getErrors() + 1);
                job.setProcessed(job.getProcessed() + 1);
            }

            // Guardar progreso en tiempo real para que el frontend pueda polling
            importJobRepository.save(job);
        }

        job.setStatus(ImportJob.Status.COMPLETED);
        job.setCompletedAt(LocalDateTime.now());
        importJobRepository.save(job);

        log.info("Job {} completado: {}/{} procesados, {} publicados, {} borradores, {} errores",
                jobId, job.getProcessed(), job.getTotal(),
                job.getPublished(), job.getDrafts(), job.getErrors());
    }

    // ─── Privados ──────────────────────────────────────────────────────────

    /**
     * Determina campos faltantes según RN-003.
     * Requeridos para ACTIVE: name, price > 0, images, categorySlug, dropiProductId.
     */
    private List<String> determineMissingFields(ImportProductRequest req) {
        List<String> missing = new ArrayList<>();

        if (req.getName() == null || req.getName().isBlank()) {
            missing.add("name");
        }
        if (req.getPrice() == null || req.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            missing.add("price");
        }
        if (req.getImages() == null || req.getImages().isEmpty()) {
            missing.add("images");
        }
        if (req.getCategorySlug() == null || req.getCategorySlug().isBlank()) {
            missing.add("categorySlug");
        }
        if (req.getDropiProductId() == null || req.getDropiProductId().isBlank()) {
            missing.add("dropiProductId");
        }

        return missing;
    }

    /**
     * Genera un slug único a partir del nombre del producto.
     * Normaliza acentos, espacios y caracteres especiales.
     * Si el slug base ya existe, agrega sufijo numérico.
     */
    private String generateUniqueSlug(String name) {
        String base = name.toLowerCase()
                .replaceAll("[áàäâã]", "a")
                .replaceAll("[éèëê]", "e")
                .replaceAll("[íìïî]", "i")
                .replaceAll("[óòöôõ]", "o")
                .replaceAll("[úùüû]", "u")
                .replaceAll("[ñ]", "n")
                .replaceAll("[ç]", "c")
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("[\\s-]+", "-");

        if (!productRepository.existsBySlug(base)) {
            return base;
        }

        int suffix = 2;
        while (productRepository.existsBySlug(base + "-" + suffix)) {
            suffix++;
        }
        return base + "-" + suffix;
    }

    /**
     * Mapea la respuesta JSON de Dropi al DTO de preview.
     * Adaptado al esquema de respuesta conocido de la API de Dropi.
     */
    @SuppressWarnings("unchecked")
    private DropiProductPreviewDto mapToPreviewDto(Map<?, ?> body, String dropiId) {
        if (body == null) {
            throw new ImportException("IMPORT_004", "Respuesta vacía de Dropi para ID " + dropiId);
        }

        // La API de Dropi anida el producto en "product" o devuelve el objeto directamente
        Map<String, Object> product = body.containsKey("product")
                ? (Map<String, Object>) body.get("product")
                : (Map<String, Object>) body;

        String name = String.valueOf(product.getOrDefault("name", ""));
        String description = String.valueOf(product.getOrDefault("description", ""));
        String shortDescription = String.valueOf(product.getOrDefault("short_description",
                product.getOrDefault("shortDescription", "")));

        BigDecimal price = BigDecimal.ZERO;
        Object priceObj = product.getOrDefault("price", product.get("suggested_price"));
        if (priceObj != null) {
            price = new BigDecimal(String.valueOf(priceObj));
        }

        List<String> images = new ArrayList<>();
        Object imagesObj = product.get("images");
        if (imagesObj instanceof List<?> imgList) {
            for (Object img : imgList) {
                if (img instanceof String s) {
                    images.add(s);
                } else if (img instanceof Map<?, ?> imgMap) {
                    Object url = ((Map<String, Object>) imgMap).getOrDefault("url",
                            ((Map<String, Object>) imgMap).get("src"));
                    if (url != null) images.add(String.valueOf(url));
                }
            }
        }

        boolean inStock = Boolean.TRUE.equals(product.getOrDefault("in_stock",
                product.getOrDefault("inStock", true)));
        boolean alreadyImported = productRepository.existsByDropiProductId(dropiId);

        return DropiProductPreviewDto.builder()
                .dropiProductId(dropiId)
                .name(name)
                .shortDescription(shortDescription.isBlank() ? null : shortDescription)
                .description(description.isBlank() ? null : description)
                .price(price)
                .images(images)
                .inStock(inStock)
                .alreadyImported(alreadyImported)
                .build();
    }

    /**
     * Stub para desarrollo sin credenciales Dropi configuradas.
     */
    private DropiProductPreviewDto buildStubPreview(String dropiId) {
        return DropiProductPreviewDto.builder()
                .dropiProductId(dropiId)
                .name("Producto Dropi #" + dropiId + " (stub)")
                .description("Descripción de prueba para desarrollo sin credenciales Dropi.")
                .price(new BigDecimal("99900"))
                .images(List.of("https://via.placeholder.com/800x800"))
                .inStock(true)
                .alreadyImported(productRepository.existsByDropiProductId(dropiId))
                .build();
    }
}
