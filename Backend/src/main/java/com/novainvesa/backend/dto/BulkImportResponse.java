package com.novainvesa.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Respuesta inicial al disparar una importación masiva asíncrona.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkImportResponse {

    private String jobId;
    private int total;
    private String status;
}
