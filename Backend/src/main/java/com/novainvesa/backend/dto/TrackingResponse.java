package com.novainvesa.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para el tracking público de un pedido.
 * Simula una línea de tiempo basada en el estado actual del pedido.
 */
@Data
@Builder
public class TrackingResponse {

    private String orderCode;
    private String orderStatus;
    private String paymentStatus;
    private String customerName;
    private String dropiOrderId;

    /** Línea de tiempo generada desde el estado actual del pedido */
    private List<TrackingEvent> timeline;

    // ─── Evento de tracking ───────────────────────────────────────────────────

    @Data
    @Builder
    public static class TrackingEvent {
        private String status;
        private String label;
        private String description;
        private LocalDateTime date;
        private boolean completed;
    }
}
