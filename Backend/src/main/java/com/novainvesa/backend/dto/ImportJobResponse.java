package com.novainvesa.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Estado completo de un job de importación masiva.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportJobResponse {

    private String jobId;
    private int total;
    private int processed;
    private int published;
    private int drafts;
    private int errors;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
