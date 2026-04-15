package com.novainvesa.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {

    private String token;

    @Builder.Default
    private String tokenType = "Bearer";

    private long expiresIn;

    // Datos básicos del usuario autenticado
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
}
