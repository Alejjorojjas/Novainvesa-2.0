package com.novainvesa.backend.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request para actualizar datos del perfil del usuario autenticado.
 * Solo campos editables por el usuario — el email no se puede cambiar aqui.
 */
@Data
public class UpdateProfileRequest {

    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String fullName;

    private String phone;
}
