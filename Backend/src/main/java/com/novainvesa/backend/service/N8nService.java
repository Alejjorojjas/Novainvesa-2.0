package com.novainvesa.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novainvesa.backend.entity.Order;
import com.novainvesa.backend.entity.OrderItem;
import com.novainvesa.backend.repository.OrderItemRepository;
import com.novainvesa.backend.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio para enviar pedidos confirmados al webhook de N8n.
 * N8n se encarga de crear el pedido en Dropi.
 *
 * RN-025: tras 3 fallos consecutivos → marcar dropiSyncStatus = FAILED y loggear para alerta manual.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class N8nService {

    @Value("${app.n8n.webhook-url:}")
    private String n8nWebhookUrl;

    @Value("${app.n8n.secret:}")
    private String n8nSecret;

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // ─── Envío asíncrono a N8n ────────────────────────────────────────────────

    /**
     * Envía el pedido confirmado al webhook de N8n.
     * Ejecutado en un hilo separado (@Async) para no bloquear la respuesta al cliente.
     * Reintenta hasta 3 veces con backoff simple.
     */
    @Async
    public void sendOrderToN8n(Order order) {
        if (n8nWebhookUrl == null || n8nWebhookUrl.isBlank()) {
            log.warn("N8N_WEBHOOK_URL no configurado — omitiendo envío para pedido {}", order.getOrderCode());
            return;
        }

        int maxAttempts = 3;
        String lastError = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                String payload = buildPayload(order);
                String signature = hmacSha256(payload, n8nSecret);

                HttpHeaders headers = new HttpHeaders();
                headers.set("X-Novainvesa-Signature", signature);
                headers.setContentType(MediaType.APPLICATION_JSON);

                restTemplate.postForEntity(
                        n8nWebhookUrl,
                        new HttpEntity<>(payload, headers),
                        Void.class
                );

                // Éxito — actualizar intentos (el status lo actualiza N8n vía webhook de respuesta)
                order.setDropiSyncAttempts(attempt);
                orderRepository.save(order);
                log.info("Pedido {} enviado a N8n (intento {})", order.getOrderCode(), attempt);
                return;

            } catch (Exception e) {
                lastError = e.getMessage();
                log.error("Error enviando a N8n — pedido {} (intento {}/{}): {}",
                        order.getOrderCode(), attempt, maxAttempts, e.getMessage());

                // Espera progresiva entre reintentos (no dormir más de 5s para no bloquear el thread pool)
                if (attempt < maxAttempts) {
                    try { Thread.sleep(1000L * attempt); } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        // Todos los intentos fallaron — RN-025
        order.setDropiSyncStatus(Order.DropiSyncStatus.FAILED);
        order.setDropiSyncAttempts(maxAttempts);
        order.setDropiSyncError(lastError);
        orderRepository.save(order);
        log.error("DROPI_SYNC_FAILED: Todos los intentos fallaron para pedido {}. Intervención manual requerida.",
                order.getOrderCode());
        // TODO: notificar admin por email cuando EmailService esté disponible
    }

    // ─── Construcción del payload ─────────────────────────────────────────────

    private String buildPayload(Order order) throws Exception {
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());

        Map<String, Object> payload = new HashMap<>();
        payload.put("orderCode",      order.getOrderCode());
        payload.put("paymentMethod",  order.getPaymentMethod().name());
        payload.put("total",          order.getTotal());
        payload.put("currency",       order.getCurrency());

        Map<String, Object> customer = new HashMap<>();
        customer.put("name",  order.getCustomerName());
        customer.put("email", order.getCustomerEmail());
        customer.put("phone", order.getCustomerPhone());
        customer.put("idNumber", order.getCustomerIdNumber());
        payload.put("customer", customer);

        Map<String, Object> shipping = new HashMap<>();
        shipping.put("address",      order.getShippingAddress());
        shipping.put("neighborhood", order.getShippingNeighborhood());
        shipping.put("city",         order.getShippingCity());
        shipping.put("department",   order.getShippingDepartment());
        shipping.put("notes",        order.getShippingNotes());
        payload.put("shipping", shipping);

        List<Map<String, Object>> itemsList = items.stream().map(i -> {
            Map<String, Object> m = new HashMap<>();
            m.put("dropiProductId", i.getDropiProductId());
            m.put("productName",    i.getProductName());
            m.put("quantity",       i.getQuantity());
            m.put("unitPrice",      i.getUnitPrice());
            m.put("subtotal",       i.getSubtotal());
            return m;
        }).toList();
        payload.put("items", itemsList);

        return objectMapper.writeValueAsString(payload);
    }

    // ─── HMAC-SHA256 ─────────────────────────────────────────────────────────

    String hmacSha256(String data, String secret) {
        try {
            if (secret == null || secret.isBlank()) return "";
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            // Convertir a hex manualmente (HexFormat disponible en Java 17+)
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            log.error("Error calculando HMAC: {}", e.getMessage());
            return "";
        }
    }
}
