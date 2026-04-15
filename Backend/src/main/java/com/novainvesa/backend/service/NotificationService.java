package com.novainvesa.backend.service;

import com.novainvesa.backend.entity.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Orquestador de notificaciones.
 * Coordina el envio de email (RN-030) y WhatsApp (RN-031/RN-032).
 *
 * Ambos metodos son @Async en sus respectivos servicios — ningun fallo
 * afecta el flujo principal del pedido.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final EmailService emailService;
    private final WhatsAppService whatsAppService;

    /**
     * Notifica la confirmacion de un pedido.
     * RN-030: email siempre (si tiene email configurado).
     * RN-031: WhatsApp solo si el pedido tiene telefono.
     */
    public void notifyOrderConfirmation(Order order) {
        emailService.sendOrderConfirmation(order);
        whatsAppService.sendOrderConfirmation(order);
    }

    /**
     * Notifica que el pedido fue enviado.
     * RN-032: email + WhatsApp con numero de guia.
     */
    public void notifyOrderShipped(Order order, String trackingNumber, String carrier) {
        emailService.sendShippedNotification(order, trackingNumber, carrier);
        whatsAppService.sendShippedNotification(order, trackingNumber);
    }
}
