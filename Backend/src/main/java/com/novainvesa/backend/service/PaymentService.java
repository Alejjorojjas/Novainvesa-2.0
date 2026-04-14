package com.novainvesa.backend.service;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.resources.preference.Preference;
import com.novainvesa.backend.dto.PaymentCreateResponse;
import com.novainvesa.backend.entity.Order;
import com.novainvesa.backend.exception.PaymentException;
import com.novainvesa.backend.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Servicio de creación de sesiones de pago para MercadoPago y Wompi.
 *
 * Para ambos: si las claves no están configuradas, retorna una URL stub
 * para facilitar el desarrollo local.
 *
 * RN-020: las preferencias de pago expiran a los 30 minutos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    @Value("${app.mercadopago.access-token:}")
    private String mpAccessToken;

    @Value("${app.wompi.private-key:}")
    private String wompiPrivateKey;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate;

    // ─── MercadoPago — Checkout Pro ───────────────────────────────────────────

    @Transactional
    public PaymentCreateResponse createMercadoPagoPreference(String orderCode) {
        Order order = findOrder(orderCode);

        // Modo stub para desarrollo (sin credenciales)
        if (mpAccessToken == null || mpAccessToken.isBlank()) {
            log.warn("MP_ACCESS_TOKEN no configurado — retornando URL stub para pedido {}", orderCode);
            return PaymentCreateResponse.builder()
                    .orderCode(orderCode)
                    .redirectUrl(frontendUrl + "/confirmacion?order=" + orderCode + "&stub=true")
                    .paymentMethod("MERCADOPAGO")
                    .message("Modo desarrollo: pago no procesado")
                    .build();
        }

        try {
            MercadoPagoConfig.setAccessToken(mpAccessToken);

            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .title("Pedido Novainvesa " + orderCode)
                    .quantity(1)
                    .unitPrice(order.getTotal())
                    .currencyId("COP")
                    .build();

            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success(frontendUrl + "/confirmacion?order=" + orderCode)
                    .failure(frontendUrl + "/checkout?error=pago_fallido&order=" + orderCode)
                    .pending(frontendUrl + "/confirmacion?order=" + orderCode + "&status=pending")
                    .build();

            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(List.of(item))
                    .externalReference(orderCode)
                    .backUrls(backUrls)
                    .autoReturn("approved")
                    // RN-020: expirar en 30 minutos
                    .expires(true)
                    .build();

            Preference preference = new PreferenceClient().create(preferenceRequest);

            // Guardar el ID de la preferencia en la orden
            order.setMpPreferenceId(preference.getId());
            orderRepository.save(order);

            log.info("Preferencia MP creada para pedido {}: {}", orderCode, preference.getId());
            return PaymentCreateResponse.builder()
                    .orderCode(orderCode)
                    .redirectUrl(preference.getInitPoint())
                    .paymentMethod("MERCADOPAGO")
                    .build();

        } catch (Exception e) {
            log.error("Error creando preferencia MercadoPago para pedido {}: {}", orderCode, e.getMessage());
            throw PaymentException.createError(e.getMessage());
        }
    }

    // ─── Wompi — Link de pago ────────────────────────────────────────────────

    @Transactional
    public PaymentCreateResponse createWompiSession(String orderCode) {
        Order order = findOrder(orderCode);

        // Modo stub para desarrollo
        if (wompiPrivateKey == null || wompiPrivateKey.isBlank()) {
            log.warn("WOMPI_PRIVATE_KEY no configurado — retornando URL stub para pedido {}", orderCode);
            return PaymentCreateResponse.builder()
                    .orderCode(orderCode)
                    .redirectUrl(frontendUrl + "/confirmacion?order=" + orderCode + "&stub=true")
                    .paymentMethod("WOMPI")
                    .message("Modo desarrollo: pago no procesado")
                    .build();
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + wompiPrivateKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Wompi usa centavos de COP
            long amountInCents = order.getTotal()
                    .multiply(BigDecimal.valueOf(100))
                    .longValue();

            Map<String, Object> body = Map.of(
                    "currency",       "COP",
                    "amount_in_cents", amountInCents,
                    "reference",       orderCode,
                    "redirect_url",   frontendUrl + "/confirmacion?order=" + orderCode
            );

            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(
                    "https://sandbox.wompi.co/v1/payment_links",
                    new HttpEntity<>(body, headers),
                    (Class<Map<String, Object>>) (Class<?>) Map.class
            );

            String redirectUrl = extractWompiUrl(response.getBody(), orderCode);

            // Wompi usa la referencia como identificador de la transacción
            order.setWompiTransactionId(orderCode);
            orderRepository.save(order);

            log.info("Sesión Wompi creada para pedido {}", orderCode);
            return PaymentCreateResponse.builder()
                    .orderCode(orderCode)
                    .redirectUrl(redirectUrl)
                    .paymentMethod("WOMPI")
                    .build();

        } catch (Exception e) {
            log.error("Error creando sesión Wompi para pedido {}: {}", orderCode, e.getMessage());
            throw PaymentException.createError("Error al conectar con Wompi");
        }
    }

    // ─── Privados ─────────────────────────────────────────────────────────────

    private Order findOrder(String orderCode) {
        return orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> PaymentException.notFound(orderCode));
    }

    @SuppressWarnings("unchecked")
    private String extractWompiUrl(Map<String, Object> body, String orderCode) {
        if (body == null) {
            throw new IllegalStateException("Respuesta vacía de Wompi");
        }
        // Wompi devuelve { "data": { "id": "...", "url": "..." } }
        Object data = body.get("data");
        if (data instanceof Map<?, ?> dataMap) {
            Object url = dataMap.get("url");
            if (url instanceof String s) return s;
        }
        // Fallback: redirigir al checkout público de Wompi con la referencia
        return "https://checkout.wompi.co/p/?reference=" + orderCode;
    }
}
