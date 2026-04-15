package com.novainvesa.backend.dto;

import com.novainvesa.backend.entity.Order;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Solicitud para actualizar el estado de un pedido desde el panel de administración.
 * notes es opcional — se usa para guardar el número de guía cuando el estado es SHIPPED.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderStatusRequest {

    @NotNull(message = "El estado es obligatorio")
    private Order.OrderStatus status;

    /** Notas opcionales — para número de guía cuando status = SHIPPED */
    private String notes;
}
