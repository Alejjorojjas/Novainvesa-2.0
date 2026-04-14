package com.novainvesa.backend.dto;

import java.time.LocalDateTime;

/**
 * Respuesta publica del perfil de usuario.
 * NUNCA incluir passwordHash.
 */
public record UserProfileResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        Boolean isActive,
        LocalDateTime createdAt
) {}
