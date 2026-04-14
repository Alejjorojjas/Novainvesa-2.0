package com.novainvesa.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request para agregar una nueva direccion de envio al perfil del usuario.
 * Campos mapeados a los campos reales de la entidad UserAddress.
 */
@Data
public class AddressRequest {

    /** Nombre completo del destinatario */
    @NotBlank(message = "El nombre completo es obligatorio")
    private String fullName;

    /** Telefono de contacto para la entrega */
    private String phone;

    @NotBlank(message = "El departamento es obligatorio")
    private String department;

    @NotBlank(message = "La ciudad es obligatoria")
    private String city;

    @NotBlank(message = "La direccion es obligatoria")
    private String address;

    private String neighborhood;

    /** Instrucciones adicionales de entrega */
    private String notes;

    private boolean isDefault;
}
