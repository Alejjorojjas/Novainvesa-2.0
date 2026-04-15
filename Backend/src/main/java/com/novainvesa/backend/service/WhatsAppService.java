package com.novainvesa.backend.service;

import com.novainvesa.backend.entity.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Servicio de notificaciones por WhatsApp via Chatea Pro.
 *
 * RN-031: Solo si el pedido tiene numero de telefono. Sin reintentos.
 * RN-032: Notificacion de envio cuando pasa a SHIPPED.
 *
 * NUNCA loggear el contenido del mensaje enviado.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WhatsAppService {

    @Value("${app.chateapro.api-key:}")
    private String apiKey;

    @Value("${app.chateapro.api-url:https://api.chateapro.co}")
    private String apiUrl;

    private final RestTemplate restTemplate;

    // ─── RN-031: Confirmación de pedido ──────────────────────────────────────

    @Async
    public void sendOrderConfirmation(Order order) {
        if (order.getCustomerPhone() == null || order.getCustomerPhone().isBlank()) {
            log.info("Pedido {} sin telefono — omitiendo WhatsApp", order.getOrderCode());
            return;
        }
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("CHATEAPRO_API_KEY no configurado — omitiendo WhatsApp pedido {}", order.getOrderCode());
            return;
        }
        String message = String.format(
                "Tu pedido fue confirmado\n\nCodigo: *%s*\nTotal: *$%s COP*\nEntrega: 3-5 dias habiles\n\nRastrear: https://www.novainvesa.com/rastrear?order=%s",
                order.getOrderCode(), formatPrice(order.getTotal()), order.getOrderCode());
        sendMessage(order.getCustomerPhone(), message, order.getOrderCode());
    }

    // ─── RN-032: Notificación de envío ───────────────────────────────────────

    @Async
    public void sendShippedNotification(Order order, String trackingNumber) {
        if (order.getCustomerPhone() == null || order.getCustomerPhone().isBlank()) return;
        if (apiKey == null || apiKey.isBlank()) return;
        String message = String.format(
                "Tu pedido esta en camino\n\nCodigo: *%s*\nGuia: *%s*\n\nhttps://www.novainvesa.com/rastrear?order=%s",
                order.getOrderCode(), trackingNumber, order.getOrderCode());
        sendMessage(order.getCustomerPhone(), message, order.getOrderCode());
    }

    // ─── Privado ──────────────────────────────────────────────────────────────

    private void sendMessage(String phone, String message, String orderCode) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, Object> body = Map.of(
                    "phone", normalizePhone(phone),
                    "message", message);
            restTemplate.postForEntity(
                    apiUrl + "/api/v1/messages/send",
                    new HttpEntity<>(body, headers),
                    Void.class);
            log.info("WhatsApp enviado para pedido {}", orderCode);
        } catch (Exception e) {
            log.error("Error enviando WhatsApp para pedido {}: {}", orderCode, e.getMessage());
        }
    }

    private String normalizePhone(String phone) {
        phone = phone.replaceAll("[^0-9]", "");
        if (phone.length() == 10) phone = "57" + phone;
        return "+" + phone;
    }

    private String formatPrice(BigDecimal price) {
        if (price == null) return "0";
        return String.format("%,.0f", price).replace(",", ".");
    }
}
