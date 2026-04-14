# Skill: n8n-workflows

Flujos de N8n para Novainvesa. N8n actúa como intermediario entre el backend y Dropi.

## Por qué N8n (no llamada directa a Dropi)

Dropi requiere login con sesión para crear pedidos. N8n maneja esta sesión sin exponerla en el backend de Spring Boot.

## Flujo principal: Crear pedido en Dropi

```
[Webhook Trigger] ← POST /webhook/novainvesa/create-order
      ↓
[Verificar HMAC]  ← header X-Novainvesa-Signature
      ↓ (si firma válida)
[HTTP Request]    ← POST login Dropi (obtener token de sesión)
      ↓
[HTTP Request]    ← POST api.dropi.co/api/v1/orders/store
      ↓ (si éxito)
[HTTP Request]    ← POST callback a Novainvesa con dropiOrderId
      ↓ (si error, retry x3)
[HTTP Request]    ← POST callback a Novainvesa con status=FAILED
```

## Payload que el backend envía a N8n

```json
{
  "orderCode": "NOVA-20260413-0001",
  "customer": {
    "name": "Juan Pérez",
    "phone": "+573001234567",
    "email": "juan@example.com",
    "address": "Calle 123 #45-67",
    "city": "Bogotá",
    "department": "Cundinamarca"
  },
  "items": [
    {
      "dropiProductId": "1865251",
      "quantity": 2,
      "unitPrice": 125000
    }
  ],
  "total": 250000,
  "paymentMethod": "WOMPI"
}
```

## Callback de N8n al backend (éxito)

```
POST https://api.novainvesa.com/api/v1/internal/dropi-callback
Headers: X-Novainvesa-Signature: <HMAC-SHA256 del payload>

{
  "orderCode": "NOVA-20260413-0001",
  "status": "SUCCESS",
  "dropiOrderId": "DRP-987654"
}
```

## Callback de N8n al backend (fallo)

```json
{
  "orderCode": "NOVA-20260413-0001",
  "status": "FAILED",
  "error": "Dropi API timeout after 3 attempts"
}
```

## Variables de entorno en N8n Cloud

```
NOVAINVESA_API_URL      = https://api.novainvesa.com
NOVAINVESA_HMAC_SECRET  = (compartido con backend → N8N_SECRET)
DROPI_USERNAME          = (credenciales del proveedor)
DROPI_PASSWORD          = (credenciales del proveedor)
DROPI_API_URL           = https://api.dropi.co
```

## Configuración del backend para N8n

```java
// application.properties
@Value("${app.n8n.webhook-url}")   private String n8nWebhookUrl;
@Value("${app.n8n.secret}")        private String n8nSecret;

// .env.example
# N8n Cloud
N8N_WEBHOOK_URL=https://n8n.novainvesa.com/webhook/create-order
N8N_SECRET=secreto_compartido_con_n8n
```

## Reglas críticas

- **RN-025:** Si N8n falla 3 veces en crear el pedido → notificar al admin por email
- **RN-026:** El pedido queda marcado `dropi_sync_status = FAILED` para reintento manual
- Nunca exponer las credenciales de Dropi fuera de N8n
- Siempre verificar firma HMAC antes de procesar el webhook (en ambas direcciones)
- El timeout por intento en N8n debe ser 15 segundos (Dropi puede ser lento)
