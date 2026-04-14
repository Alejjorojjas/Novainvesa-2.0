package com.novainvesa.backend.service;

import com.novainvesa.backend.dto.PixelEventRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

/**
 * Servicio para enviar eventos al Meta Conversions API (CAPI).
 *
 * IMPORTANTE: email, telefono y nombre SIEMPRE se hashean con SHA-256
 * antes de enviar a Meta — nunca se envian en texto plano.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PixelService {

    @Value("${app.meta.capi-token:}")
    private String capiToken;

    @Value("${app.meta.pixel-id:}")
    private String pixelId;

    private final RestTemplate restTemplate;

    private static final String META_CAPI_URL = "https://graph.facebook.com/v19.0/";

    public void sendEvent(PixelEventRequest request) {
        if (capiToken == null || capiToken.isBlank() || pixelId == null || pixelId.isBlank()) {
            log.debug("Meta Pixel no configurado — omitiendo evento {}", request.getEventName());
            return;
        }
        try {
            Map<String, Object> userData = new HashMap<>();
            if (request.getEmail() != null) {
                userData.put("em", sha256(request.getEmail().toLowerCase().trim()));
            }
            if (request.getPhone() != null) {
                String ph = request.getPhone().replaceAll("[^0-9]", "");
                if (ph.length() == 10) ph = "57" + ph;
                userData.put("ph", sha256(ph));
            }
            if (request.getFirstName() != null) {
                userData.put("fn", sha256(request.getFirstName().toLowerCase().trim()));
            }
            if (request.getClientIp() != null) {
                userData.put("client_ip_address", request.getClientIp());
            }
            if (request.getUserAgent() != null) {
                userData.put("client_user_agent", request.getUserAgent());
            }

            Map<String, Object> eventData = new HashMap<>();
            eventData.put("event_name", request.getEventName());
            eventData.put("event_time", Instant.now().getEpochSecond());
            eventData.put("action_source", "website");
            eventData.put("user_data", userData);
            if (request.getCustomData() != null) {
                eventData.put("custom_data", request.getCustomData());
            }

            Map<String, Object> body = new HashMap<>();
            body.put("data", List.of(eventData));
            body.put("access_token", capiToken);

            restTemplate.postForEntity(
                    META_CAPI_URL + pixelId + "/events",
                    body, Map.class);

            log.debug("Evento Meta Pixel {} enviado correctamente", request.getEventName());

        } catch (Exception e) {
            log.warn("Error enviando evento Meta Pixel {}: {}", request.getEventName(), e.getMessage());
        }
    }

    // ─── SHA-256 ──────────────────────────────────────────────────────────────

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            log.error("Error calculando SHA-256: {}", e.getMessage());
            return "";
        }
    }
}
