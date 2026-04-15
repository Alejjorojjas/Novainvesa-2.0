# Skill: dropi-api

Integración con la API de Dropi para Novainvesa.

## Autenticación

```java
// Header requerido en TODAS las llamadas a Dropi
headers.set("dropi-integracion-key", dropiIntegrationKey);
// Variable: ${app.dropi.integration-key} en application.properties
// Valor en producción: Render.com Environment Variables → DROPI_INTEGRATION_KEY
```

## Endpoints disponibles

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `https://api.dropi.co/api/v1/products/{id}` | Obtener producto por ID |
| POST | `https://api.dropi.co/api/v1/orders/store` | Crear pedido en Dropi |
| GET | `https://api.dropi.co/api/v1/coverage?city={city}` | Verificar cobertura COD |

## Detección ID vs URL

```java
// Entrada: "1865251" → ID numérico
// Entrada: "https://app.dropi.co/dashboard/products/1865251" → extraer ID
public String extractDropiId(String input) {
    input = input.trim();
    if (input.matches("\\d+")) {
        return input;  // Ya es ID numérico
    }
    // Extraer último segmento numérico de la URL
    Pattern p = Pattern.compile("/(\\d+)(?:[/?#]|$)");
    Matcher m = p.matcher(input);
    if (m.find()) {
        return m.group(1);
    }
    throw new InvalidDropiInputException("Formato no reconocido: " + input);
}
```

## Obtener producto por ID

```java
public DropiProductDto getProductById(String dropiId) {
    try {
        ResponseEntity<Map> response = restTemplate.exchange(
            dropiApiUrl + "/products/" + dropiId,
            HttpMethod.GET,
            new HttpEntity<>(buildHeaders()),
            Map.class
        );
        return mapToDto(response.getBody());
    } catch (HttpClientErrorException e) {
        if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
            throw new DropiProductNotFoundException("DROPI_001",
                "Producto " + dropiId + " no encontrado en Dropi");
        }
        throw new DropiApiException("DROPI_002", "Error de conexión con Dropi");
    }
}
```

## Timeout — CRÍTICO para importación masiva

```java
// Timeout de 10 segundos por producto (RN-005)
RestTemplate dropiRestTemplate = new RestTemplate();
dropiRestTemplate.setRequestFactory(new SimpleClientHttpRequestFactory() {{
    setConnectTimeout(10_000);
    setReadTimeout(10_000);
}});
```

## Flujo N8n para crear pedido

```
Novainvesa Backend
    ↓  POST webhook con HMAC firmado
N8n Cloud
    ↓  login en Dropi + crear pedido
    ↓  callback a Novainvesa con dropiOrderId
Novainvesa Backend
    ↓  actualiza order.dropiOrderId + dropiSyncStatus = SUCCESS
```

```java
// Enviar webhook a N8n (NO llamar Dropi directamente para pedidos)
public void triggerOrderCreation(Order order) {
    String payload = buildN8nPayload(order);
    String signature = hmacSha256(payload, n8nSecret);

    HttpHeaders headers = new HttpHeaders();
    headers.set("X-Novainvesa-Signature", signature);
    headers.setContentType(MediaType.APPLICATION_JSON);

    restTemplate.postForEntity(n8nWebhookUrl,
        new HttpEntity<>(payload, headers), Void.class);
}
```

## Verificar cobertura COD

```java
// RN-018: COD solo si ciudad tiene cobertura Dropi Y total < $500.000 COP
public boolean hasCodCoverage(String city) {
    try {
        ResponseEntity<Map> response = restTemplate.exchange(
            dropiApiUrl + "/coverage?city=" + URLEncoder.encode(city, StandardCharsets.UTF_8),
            HttpMethod.GET,
            new HttpEntity<>(buildHeaders()),
            Map.class
        );
        return Boolean.TRUE.equals(response.getBody().get("hasCoverage"));
    } catch (Exception e) {
        log.warn("No se pudo verificar cobertura COD para {}: {}", city, e.getMessage());
        return false;  // Ante la duda, no ofrecer COD
    }
}
```

## Reglas críticas

- **RN-022:** Crear pedido en Dropi SOLO después de confirmación de pago (excepción: COD → inmediatamente)
- **RN-025:** Si N8n falla 3 veces → email automático al admin
- **RN-026:** Admin puede reintentar desde panel; pedido queda en `dropi_sync_status = FAILED`
- **RN-005:** Timeout de 10s por producto en importación masiva
- Las imágenes de Dropi son URLs de CloudFront — guardar como URL, no descargar
- Si `dropi_product_id` ya existe → advertencia, no error (permitir actualizar)
