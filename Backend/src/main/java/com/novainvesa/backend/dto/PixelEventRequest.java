package com.novainvesa.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

/**
 * Request para enviar un evento al Meta Conversions API.
 * Los datos de usuario (email, phone, firstName) se hashean con SHA-256
 * antes de enviarse a Meta.
 *
 * clientIp y userAgent son seteados por el controller — no vienen del body JSON.
 */
@Data
public class PixelEventRequest {

    @NotBlank(message = "El nombre del evento es obligatorio")
    private String eventName;

    /** Email del usuario — se hashea con SHA-256 antes de enviar */
    private String email;

    /** Telefono del usuario — se hashea con SHA-256 antes de enviar */
    private String phone;

    /** Nombre del usuario — se hashea con SHA-256 antes de enviar */
    private String firstName;

    /** Datos adicionales del evento (value, currency, content_ids, etc.) */
    private Map<String, Object> customData;

    // Seteados por el controller desde los headers HTTP — NO deserializar del body
    private String clientIp;
    private String userAgent;
}
