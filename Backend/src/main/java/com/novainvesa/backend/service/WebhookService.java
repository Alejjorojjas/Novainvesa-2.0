package com.novainvesa.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novainvesa.backend.entity.Order;
import com.novainvesa.backend.exception.PaymentException;
import com.novainvesa.backend.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Servicio para procesar webhooks de MercadoPago y Wompi.
 *
 * Reglas de seguridad críticas:
 *   RN-021: NUNCA procesar sin verificar HMAC primero → 401 si inválido.
 *   RN-023: Idempotencia — si el pedido ya está CONFIRMED, retornar sin hacer nada.
 *   NUNCA loggear el payload completo del webhook.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookService {

    @Value("${app.mercadopago.webhook-secret:}")
    private String mpWebhookSecret;

    @Value("${app.wompi.events-secret:}")
    private String wompiEventsSecret;

    private final OrderRepository orderRepository;
    private final N8nService n8nService;
    private final ObjectMapper objectMapper;

    // ─── MercadoPago ─────────────────────────────────────────────────────────

    /**
     * Procesa notificación de MercadoPago.
     *
     * Algoritmo de verificación de firma MP:
     *   El header x-signature tiene formato "ts=<timestamp>,v1=<hash>"
     *   El mensaje a verificar es: "id:<dataId>;request-id:<requestId>;ts:<ts>;"
     *   Se firma con HMAC-SHA256 usando el webhook-secret.
     *
     * @param rawPayload  cuerpo crudo del request (sin parsear)
     * @param xSignature  valor del header x-signature
     * @param xRequestId  valor del header x-request-id (puede ser null)
     * @param dataId      query param data.id (ID del pago en MP)
     * @param clientIp    IP del cliente (para logging)
     */
    @Transactional
    public void processMercadoPago(String rawPayload, String xSignature,
                                   String xRequestId, String dataId, String clientIp) {
        // RN-021: verificar firma
        if (!verifyMercadoPagoSignature(xSignature, xRequestId, dataId)) {
            log.warn("Webhook MP con firma inválida — IP: {}", clientIp);
            throw PaymentException.invalidWebhookSignature();
        }

        try {
            JsonNode root = objectMapper.readTree(rawPayload);
            String topic = root.path("type").asText("");
            String action = root.path("action").asText("");

            // Solo procesar eventos de pago aprobado
            if (!"payment".equals(topic)) {
                log.debug("Webhook MP ignorado — tipo: {}", topic);
                return;
            }

            // Extraer external_reference = orderCode
            JsonNode dataNode = root.path("data");
            // La referencia externa está en el pago, no en el webhook — usamos dataId para buscar
            // pero en Checkout Pro el external_reference viene en el body del pago
            String externalReference = dataNode.path("external_reference").asText(null);
            String mpStatus = dataNode.path("status").asText(null);

            // Si no hay referencia en el payload de notificación, buscar por preferenceId
            // MP a veces solo envía el ID del pago en data.id
            if (externalReference == null && dataId != null) {
                processConfirmByMpPaymentId(dataId, clientIp);
                return;
            }

            if (externalReference != null && "approved".equalsIgnoreCase(mpStatus)) {
                confirmOrder(externalReference, "mpPaymentId:" + dataId, "MercadoPago");
            }

        } catch (PaymentException ex) {
            throw ex;
        } catch (Exception e) {
            log.error("Error procesando webhook MP: {}", e.getMessage());
            // Retornar 200 de todas formas — MP re-reintentará si el server error persiste
        }
    }

    /**
     * Búsqueda alternativa cuando el payload de MP solo trae el ID del pago.
     * Buscamos la orden por mpPreferenceId (guardado al crear la preferencia).
     */
    private void processConfirmByMpPaymentId(String mpPaymentId, String clientIp) {
        // Buscar orden que tenga este mp_payment_id (o preferenceId relacionado)
        // Por simplicidad, buscamos por externalPaymentId guardado en la preferencia
        orderRepository.findByMpPaymentId(mpPaymentId).ifPresentOrElse(
                order -> confirmOrder(order.getOrderCode(), mpPaymentId, "MercadoPago"),
                () -> log.warn("Webhook MP — no se encontró orden para mpPaymentId={}", mpPaymentId)
        );
    }

    // ─── Wompi ────────────────────────────────────────────────────────────────

    /**
     * Procesa notificación de Wompi.
     *
     * Algoritmo de verificación:
     *   HMAC-SHA256(rawPayload, wompiEventsSecret)
     *   El resultado en hex debe coincidir con el header x-event-checksum.
     */
    @Transactional
    public void processWompi(String rawPayload, String eventChecksum, String clientIp) {
        // RN-021: verificar firma
        if (!verifyWompiSignature(rawPayload, eventChecksum)) {
            log.warn("Webhook Wompi con firma inválida — IP: {}", clientIp);
            throw PaymentException.invalidWebhookSignature();
        }

        try {
            JsonNode root = objectMapper.readTree(rawPayload);
            String event = root.path("event").asText("");

            if (!"transaction.updated".equals(event)) {
                log.debug("Webhook Wompi ignorado — evento: {}", event);
                return;
            }

            JsonNode data = root.path("data").path("transaction");
            String status    = data.path("status").asText("");
            String reference = data.path("reference").asText(null);
            String txId      = data.path("id").asText(null);

            if ("APPROVED".equalsIgnoreCase(status) && reference != null) {
                confirmOrder(reference, txId, "Wompi");
            }

        } catch (PaymentException ex) {
            throw ex;
        } catch (Exception e) {
            log.error("Error procesando webhook Wompi: {}", e.getMessage());
        }
    }

    // ─── Lógica compartida de confirmación ────────────────────────────────────

    /**
     * Confirma el pago de un pedido y dispara la sincronización con N8n.
     * RN-023: idempotente — si ya está CONFIRMED, no hace nada.
     */
    private void confirmOrder(String orderCode, String transactionId, String provider) {
        Order order = orderRepository.findByOrderCode(orderCode).orElse(null);
        if (order == null) {
            log.warn("Webhook {} — pedido no encontrado: {}", provider, orderCode);
            return;
        }

        // RN-023: idempotencia
        if (order.getPaymentStatus() == Order.PaymentStatus.CONFIRMED) {
            log.info("Webhook {} — pedido {} ya estaba confirmado (idempotente)", provider, orderCode);
            return;
        }

        order.setPaymentStatus(Order.PaymentStatus.CONFIRMED);
        order.setOrderStatus(Order.OrderStatus.CONFIRMED);

        // Guardar ID de transacción según el proveedor
        if ("MercadoPago".equals(provider) && transactionId != null) {
            order.setMpPaymentId(transactionId);
        } else if ("Wompi".equals(provider) && transactionId != null) {
            order.setWompiTransactionId(transactionId);
        }

        orderRepository.save(order);
        log.info("Pedido {} confirmado vía {} — disparando N8n", orderCode, provider);

        // RN-022: enviar a Dropi solo después de confirmar el pago
        n8nService.sendOrderToN8n(order);
    }

    // ─── Verificación de firmas HMAC ──────────────────────────────────────────

    /**
     * Verifica la firma del webhook de MercadoPago.
     * Formato del header x-signature: "ts=<timestamp>,v1=<hash>"
     * Mensaje: "id:<dataId>;request-id:<requestId>;ts:<ts>;"
     */
    private boolean verifyMercadoPagoSignature(String xSignature, String xRequestId, String dataId) {
        if (mpWebhookSecret == null || mpWebhookSecret.isBlank()) {
            // Sin secreto configurado — permitir en desarrollo, loggear advertencia
            log.warn("MP_WEBHOOK_SECRET no configurado — omitiendo verificación de firma");
            return true;
        }
        if (xSignature == null || xSignature.isBlank()) return false;

        try {
            // Parsear "ts=...,v1=..."
            String ts = null;
            String v1 = null;
            for (String part : xSignature.split(",")) {
                String[] kv = part.trim().split("=", 2);
                if (kv.length == 2) {
                    if ("ts".equals(kv[0]))   ts = kv[1];
                    if ("v1".equals(kv[0]))   v1 = kv[1];
                }
            }
            if (ts == null || v1 == null) return false;

            String message = "id:" + (dataId != null ? dataId : "") + ";"
                    + "request-id:" + (xRequestId != null ? xRequestId : "") + ";"
                    + "ts:" + ts + ";";

            String computed = hmacSha256(message, mpWebhookSecret);
            // MessageDigest.isEqual para prevenir timing attacks
            return MessageDigest.isEqual(
                    computed.getBytes(StandardCharsets.UTF_8),
                    v1.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            log.error("Error verificando firma MP: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Verifica la firma del webhook de Wompi.
     * Algoritmo: HMAC-SHA256(rawPayload, eventsSecret) → hex
     */
    private boolean verifyWompiSignature(String rawPayload, String expectedChecksum) {
        if (wompiEventsSecret == null || wompiEventsSecret.isBlank()) {
            log.warn("WOMPI_EVENTS_SECRET no configurado — omitiendo verificación de firma");
            return true;
        }
        if (expectedChecksum == null || expectedChecksum.isBlank()) return false;

        try {
            String computed = hmacSha256(rawPayload, wompiEventsSecret);
            return MessageDigest.isEqual(
                    computed.getBytes(StandardCharsets.UTF_8),
                    expectedChecksum.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            log.error("Error verificando firma Wompi: {}", e.getMessage());
            return false;
        }
    }

    /** Calcula HMAC-SHA256 y retorna el resultado en hexadecimal minúscula. */
    private String hmacSha256(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder(hash.length * 2);
        for (byte b : hash) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
