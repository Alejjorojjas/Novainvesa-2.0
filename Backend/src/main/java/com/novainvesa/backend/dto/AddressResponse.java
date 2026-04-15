package com.novainvesa.backend.dto;

/**
 * Respuesta de direccion de envio del usuario.
 * Campos mapeados a los campos reales de la entidad UserAddress.
 */
public record AddressResponse(
        Long id,
        String fullName,
        String phone,
        String department,
        String city,
        String address,
        String neighborhood,
        String notes,
        Boolean isDefault
) {}
