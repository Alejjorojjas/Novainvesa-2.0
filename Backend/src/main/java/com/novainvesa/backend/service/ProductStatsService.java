package com.novainvesa.backend.service;

import com.novainvesa.backend.repository.ProductStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para actualizar estadísticas de productos.
 * Usa queries directas (@Modifying) para evitar cargar las entidades completas.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductStatsService {

    private final ProductStatsRepository productStatsRepository;

    @Transactional
    public void incrementViewCount(Long productId) {
        try {
            productStatsRepository.incrementViewCount(productId);
        } catch (Exception e) {
            log.warn("No se pudo incrementar views para producto {}: {}", productId, e.getMessage());
        }
    }

    @Transactional
    public void incrementCartAdds(Long productId) {
        try {
            productStatsRepository.incrementCartAdds(productId);
        } catch (Exception e) {
            log.warn("No se pudo incrementar cartAdds para producto {}: {}", productId, e.getMessage());
        }
    }

    @Transactional
    public void incrementWishlistCount(Long productId) {
        try {
            productStatsRepository.incrementWishlistCount(productId);
        } catch (Exception e) {
            log.warn("No se pudo incrementar wishlistCount para producto {}: {}", productId, e.getMessage());
        }
    }
}
