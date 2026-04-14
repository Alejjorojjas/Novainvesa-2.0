# Skill: seguridad-web

Checklist de seguridad para Novainvesa (e-commerce con pagos reales).

## JWT — Reglas críticas

```java
// Dos secretos separados (usuarios vs admins)
@Value("${jwt.secret}")        private String userSecret;      // 7 días
@Value("${jwt.admin-secret}")  private String adminSecret;     // 24 horas

// Validar SIEMPRE:
// 1. Firma del token (HMAC-SHA256)
// 2. Expiración
// 3. Que el usuario/admin exista en BD y esté activo
// 4. Para admins: verificar que role sea ADMIN o SUPER_ADMIN

// NUNCA:
// - Guardar contraseñas en texto plano (usar BCrypt(12))
// - Guardar tokens completos en BD (solo revocar por expiración)
// - Retornar tokens en URLs (solo en body o header Authorization)
```

## Verificación HMAC — Webhooks

```java
// Para CUALQUIER webhook entrante (Wompi, MercadoPago, N8n callback)
private boolean verifyHmac(String payload, String secret, String expectedSignature) {
    try {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        String computed = HexFormat.of().formatHex(hash);
        // MessageDigest.isEqual previene timing attacks
        return MessageDigest.isEqual(computed.getBytes(), expectedSignature.getBytes());
    } catch (Exception e) {
        log.error("Error calculando HMAC: {}", e.getMessage());
        return false;
    }
}
// Si HMAC inválido → retornar 401, NO procesar, NO loggear el payload completo
```

## CORS — Configuración estricta

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of(allowedOrigin)); // solo FRONTEND_URL
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
    config.setAllowCredentials(true);
    config.setMaxAge(3600L);
    // NUNCA config.setAllowedOrigins(List.of("*")) en producción
}
```

## Endpoints admin — Protección en capas

```java
// Capa 1: SecurityConfig — solo con rol correcto
.requestMatchers("/api/v1/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")

// Capa 2: Anotación en el controller
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminProductController { }

// Capa 3: Validar en el service que el usuario del token coincide
```

## Prevención SQL Injection

```java
// NUNCA concatenar strings en queries
// ✅ Correcto: usar @Param o named parameters
@Query("SELECT p FROM Product p WHERE p.categorySlug = :slug")
List<Product> findByCategorySlug(@Param("slug") String slug);

// ✅ Correcto: métodos derivados de JPA
Optional<Product> findBySlugAndStatus(String slug, Product.Status status);

// ❌ Incorrecto:
@Query("SELECT * FROM products WHERE slug = '" + slug + "'")
```

## Prevención XSS

```tsx
// Next.js — nunca usar dangerouslySetInnerHTML con datos del usuario
// ✅ Permitido: solo para JSON-LD controlado por nosotros (SEO)
<script type="application/ld+json" dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }} />

// ❌ Nunca:
<div dangerouslySetInnerHTML={{ __html: product.description }} />
// Usar DOMPurify si se necesita HTML del usuario
```

## Rate Limiting

```java
// Aplicar en endpoints sensibles:
// - /api/v1/auth/login → máx 5 intentos por IP por minuto
// - /api/v1/auth/register → máx 3 por IP por hora
// - /api/v1/orders → máx 10 por usuario por minuto

// Implementar con Spring + Bucket4j o similar
// Retornar 429 Too Many Requests con Retry-After header
```

## Datos sensibles — Lo que NUNCA loggear

```java
// NUNCA loggear:
// - Contraseñas (ni hasheadas)
// - Tokens JWT completos
// - Números de tarjeta (Wompi/MP manejan esto, no nosotros)
// - Payloads completos de webhooks (pueden contener datos de pago)
// - Claves de API (dropiIntegrationKey, mpAccessToken, etc.)

// ✅ Loggear: orderCode, userId (anonimizado), slug de producto, código de error
log.info("Pedido {} confirmado para usuario {}", order.getOrderCode(), userId);
```

## Checklist antes de deploy a producción

- [ ] Todas las variables de entorno configuradas en Render.com / GitHub Secrets
- [ ] `spring.jpa.hibernate.ddl-auto=validate` (NO create, NO update)
- [ ] CORS restringido solo a `https://www.novainvesa.com`
- [ ] Webhooks verificando HMAC antes de procesar
- [ ] Endpoints `/admin/**` protegidos con rol en SecurityConfig Y @PreAuthorize
- [ ] No hay System.out.println ni credenciales en el código
- [ ] BCrypt(12) para todas las contraseñas
- [ ] HTTPS forzado (Render lo maneja; Hostinger también)
- [ ] `noindex` en páginas privadas (checkout, cuenta, admin, confirmacion)
