# API Contract — Novainvesa v2.0
## Spring Boot 3 REST API

**Versión:** 2.1  
**Fecha:** Abril 2026  
**Base URL producción:** `https://api-novainvesa.onrender.com`  
**Base URL local:** `http://localhost:8080`  
**Prefijo:** `/api/v1`  

---

## Formato estándar de respuestas

### Éxito
```json
{
  "success": true,
  "data": { ... }
}
```

### Error
```json
{
  "success": false,
  "error": {
    "code": "AUTH_001",
    "message": "Token inválido o expirado"
  }
}
```

### Lista paginada
```json
{
  "success": true,
  "data": {
    "items": [ ... ],
    "total": 100,
    "page": 1,
    "limit": 20,
    "totalPages": 5,
    "hasNext": true,
    "hasPrev": false
  }
}
```

---

## Autenticación

Los endpoints protegidos requieren header:
```
Authorization: Bearer {jwt_token}
```

Los endpoints de admin requieren token de admin (JWT_ADMIN_SECRET diferente).

---

## 1. Health Check

### GET /api/health
**Descripción:** Verifica que el servidor está activo  
**Auth:** Ninguna  
**Response 200:**
```json
{
  "success": true,
  "data": {
    "status": "ok",
    "timestamp": "2026-04-13T20:00:00Z",
    "uptime": 3600,
    "environment": "production"
  }
}
```

---

## 2. Autenticación de usuarios

### POST /api/v1/auth/register
**Descripción:** Registrar nuevo usuario  
**Auth:** Ninguna  
**Rate limit:** 10 intentos / 15min / IP  

**Request:**
```json
{
  "email": "usuario@ejemplo.com",
  "password": "password123",
  "fullName": "Juan Pérez",
  "phone": "3001234567"
}
```

**Response 201:**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGc...",
    "user": {
      "id": 1,
      "email": "usuario@ejemplo.com",
      "fullName": "Juan Pérez",
      "phone": "3001234567"
    }
  }
}
```

**Errores:**
- `AUTH_004` — Email ya registrado
- `VALIDATION_001` — Campos inválidos

---

### POST /api/v1/auth/login
**Descripción:** Iniciar sesión  
**Auth:** Ninguna  
**Rate limit:** 5 intentos / 15min / IP  

**Request:**
```json
{
  "email": "usuario@ejemplo.com",
  "password": "password123"
}
```

**Response 200:**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGc...",
    "user": {
      "id": 1,
      "email": "usuario@ejemplo.com",
      "fullName": "Juan Pérez"
    }
  }
}
```

**Errores:**
- `AUTH_002` — Credenciales incorrectas
- `AUTH_003` — Cuenta inactiva

---

## 3. Productos

### GET /api/v1/products
**Descripción:** Listar productos activos  
**Auth:** Ninguna  

**Query params:**
| Param | Tipo | Descripción |
|-------|------|-------------|
| `category` | string | Slug de categoría |
| `featured` | boolean | Solo productos destacados |
| `search` | string | Búsqueda por nombre |
| `page` | int | Página (default: 1) |
| `limit` | int | Items por página (default: 20, max: 50) |
| `sort` | string | `price_asc`, `price_desc`, `newest` |

**Response 200:**
```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": 1,
        "dropiProductId": "1865251",
        "name": "Ejercitador Multi",
        "slug": "ejercitador-multi",
        "categorySlug": "fitness",
        "shortDescription": "Kit completo para fortalecer la mano",
        "price": 36500,
        "compareAtPrice": 45000,
        "currency": "COP",
        "images": ["https://cdn.dropi.co/..."],
        "inStock": true,
        "featured": true
      }
    ],
    "total": 25,
    "page": 1,
    "limit": 20,
    "totalPages": 2,
    "hasNext": true,
    "hasPrev": false
  }
}
```

---

### GET /api/v1/products/{slug}
**Descripción:** Detalle de un producto  
**Auth:** Ninguna  

**Response 200:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "dropiProductId": "1865251",
    "name": "Ejercitador Multi",
    "slug": "ejercitador-multi",
    "categorySlug": "fitness",
    "shortDescription": "Kit completo...",
    "description": "Descripción completa...",
    "price": 36500,
    "compareAtPrice": 45000,
    "currency": "COP",
    "images": ["https://cdn.dropi.co/..."],
    "benefits": ["Beneficio 1", "Beneficio 2"],
    "inStock": true,
    "featured": true,
    "related": [
      { "id": 2, "name": "...", "slug": "...", "price": 25000, "images": ["..."] }
    ]
  }
}
```

**Errores:**
- `PRODUCT_001` — Producto no encontrado

---

### GET /api/v1/products/search
**Descripción:** Buscar productos  
**Auth:** Ninguna  

**Query params:**
| Param | Tipo | Descripción |
|-------|------|-------------|
| `q` | string | Texto a buscar (mínimo 2 caracteres) |
| `limit` | int | Resultados (default: 20) |

**Response 200:**
```json
{
  "success": true,
  "data": {
    "query": "ejercitador",
    "items": [ ... ],
    "total": 3
  }
}
```

---

## 4. Categorías

### GET /api/v1/categories
**Descripción:** Listar categorías activas  
**Auth:** Ninguna  

**Response 200:**
```json
{
  "success": true,
  "data": [
    {
      "slug": "fitness",
      "name": "Fitness",
      "icon": "💪",
      "color": "#EF4444",
      "productCount": 5
    }
  ]
}
```

---

## 5. Cobertura COD

### GET /api/v1/coverage/cod
**Descripción:** Verificar si una ciudad tiene cobertura COD  
**Auth:** Ninguna  

**Query params:**
| Param | Tipo | Descripción |
|-------|------|-------------|
| `city` | string | Nombre de la ciudad |
| `department` | string | Nombre del departamento |

**Response 200:**
```json
{
  "success": true,
  "data": {
    "city": "Bogotá",
    "department": "Cundinamarca",
    "codAvailable": true,
    "estimatedDelivery": "2-3 días hábiles"
  }
}
```

---

## 6. Pedidos

### POST /api/v1/orders
**Descripción:** Crear nuevo pedido  
**Auth:** Opcional (si hay token, se vincula al usuario)  

**Request:**
```json
{
  "customer": {
    "fullName": "Juan Pérez",
    "email": "juan@ejemplo.com",
    "phone": "3001234567",
    "idNumber": "12345678"
  },
  "shipping": {
    "department": "Cundinamarca",
    "city": "Bogotá",
    "address": "Calle 123 # 45-67",
    "neighborhood": "Chapinero",
    "notes": "Apto 301"
  },
  "items": [
    {
      "productId": 1,
      "dropiProductId": "1865251",
      "quantity": 1,
      "unitPrice": 36500
    }
  ],
  "payment": {
    "method": "COD"
  }
}
```

**Response 201:**
```json
{
  "success": true,
  "data": {
    "order": {
      "id": 1,
      "orderCode": "NOVA-20260413-0001",
      "status": "CONFIRMED",
      "paymentMethod": "COD",
      "total": 36500,
      "currency": "COP",
      "estimatedDelivery": "3-5 días hábiles",
      "createdAt": "2026-04-13T20:00:00Z"
    }
  }
}
```

**Errores:**
- `ORDER_001` — Carrito vacío
- `ORDER_002` — Producto sin stock
- `ORDER_003` — Ciudad sin cobertura COD
- `ORDER_004` — Total excede límite COD ($500.000 COP)

---

### GET /api/v1/orders/{orderCode}
**Descripción:** Ver detalle de un pedido  
**Auth:** Ninguna (acceso por código público)  

**Response 200:**
```json
{
  "success": true,
  "data": {
    "orderCode": "NOVA-20260413-0001",
    "status": "PROCESSING",
    "paymentMethod": "COD",
    "customer": {
      "fullName": "Juan Pérez",
      "email": "juan@***",
      "city": "Bogotá"
    },
    "items": [
      {
        "name": "Ejercitador Multi",
        "image": "https://...",
        "quantity": 1,
        "unitPrice": 36500,
        "subtotal": 36500
      }
    ],
    "total": 36500,
    "createdAt": "2026-04-13T20:00:00Z"
  }
}
```

---

### GET /api/v1/orders/{orderCode}/tracking
**Descripción:** Rastrear estado del pedido  
**Auth:** Ninguna  

**Response 200:**
```json
{
  "success": true,
  "data": {
    "orderCode": "NOVA-20260413-0001",
    "currentStatus": "SHIPPED",
    "dropiOrderId": "DR-987654",
    "carrier": "Coordinadora",
    "trackingNumber": "1234567890",
    "estimatedDelivery": "15/04/2026",
    "timeline": [
      {
        "status": "CONFIRMED",
        "label": "Pedido confirmado",
        "description": "Tu pedido fue recibido y confirmado",
        "date": "2026-04-13T20:00:00Z",
        "completed": true
      },
      {
        "status": "PROCESSING",
        "label": "En preparación",
        "description": "El proveedor está preparando tu pedido",
        "date": "2026-04-14T08:00:00Z",
        "completed": true
      },
      {
        "status": "SHIPPED",
        "label": "En camino",
        "description": "Tu pedido está en camino con Coordinadora",
        "date": "2026-04-14T14:00:00Z",
        "completed": true
      },
      {
        "status": "DELIVERED",
        "label": "Entregado",
        "description": "Tu pedido será entregado pronto",
        "date": null,
        "completed": false
      }
    ]
  }
}
```

---

## 7. Pagos

### POST /api/v1/payments/mercadopago/create
**Descripción:** Crear preferencia de pago en MercadoPago  
**Auth:** Ninguna  

**Request:**
```json
{
  "orderCode": "NOVA-20260413-0001"
}
```

**Response 200:**
```json
{
  "success": true,
  "data": {
    "redirectUrl": "https://www.mercadopago.com.co/checkout/v1/redirect?pref_id=...",
    "preferenceId": "123456789-abc",
    "expiresAt": "2026-04-13T20:30:00Z"
  }
}
```

---

### POST /api/v1/payments/wompi/create
**Descripción:** Crear sesión de pago en Wompi  
**Auth:** Ninguna  

**Request:**
```json
{
  "orderCode": "NOVA-20260413-0001"
}
```

**Response 200:**
```json
{
  "success": true,
  "data": {
    "redirectUrl": "https://checkout.wompi.co/p/?public-key=...",
    "transactionId": "wompi-abc123",
    "expiresAt": "2026-04-13T20:30:00Z"
  }
}
```

---

## 8. Webhooks (solo reciben datos, no son llamados por el frontend)

### POST /api/v1/webhooks/mercadopago
**Descripción:** Recibe notificaciones de MercadoPago  
**Auth:** HMAC-SHA256 en header `x-signature`  
**Llamado por:** MercadoPago (no por el frontend)  

---

### POST /api/v1/webhooks/wompi
**Descripción:** Recibe notificaciones de Wompi  
**Auth:** HMAC-SHA256 en header `x-event-checksum`  
**Llamado por:** Wompi (no por el frontend)  

---

## 9. Usuario autenticado

### GET /api/v1/users/me
**Auth:** Bearer token usuario  

**Response 200:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "email": "juan@ejemplo.com",
    "fullName": "Juan Pérez",
    "phone": "3001234567",
    "createdAt": "2026-04-01T10:00:00Z"
  }
}
```

---

### PUT /api/v1/users/me
**Auth:** Bearer token usuario  

**Request:**
```json
{
  "fullName": "Juan Carlos Pérez",
  "phone": "3009876543"
}
```

---

### GET /api/v1/users/me/orders
**Auth:** Bearer token usuario  

**Response 200:**
```json
{
  "success": true,
  "data": {
    "items": [
      {
        "orderCode": "NOVA-20260413-0001",
        "status": "DELIVERED",
        "total": 36500,
        "itemCount": 1,
        "createdAt": "2026-04-13T20:00:00Z"
      }
    ],
    "total": 3,
    "page": 1,
    "limit": 10,
    "totalPages": 1
  }
}
```

---

### GET /api/v1/users/me/addresses
**Auth:** Bearer token usuario  

**Response 200:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "fullName": "Juan Pérez",
      "phone": "3001234567",
      "department": "Cundinamarca",
      "city": "Bogotá",
      "address": "Calle 123 # 45-67",
      "neighborhood": "Chapinero",
      "isDefault": true
    }
  ]
}
```

---

### POST /api/v1/users/me/addresses
**Auth:** Bearer token usuario  

**Request:**
```json
{
  "fullName": "Juan Pérez",
  "phone": "3001234567",
  "department": "Cundinamarca",
  "city": "Bogotá",
  "address": "Calle 123 # 45-67",
  "neighborhood": "Chapinero",
  "isDefault": true
}
```

---

### GET /api/v1/users/me/wishlist
**Auth:** Bearer token usuario  

### POST /api/v1/users/me/wishlist/{productId}
**Auth:** Bearer token usuario  

### DELETE /api/v1/users/me/wishlist/{productId}
**Auth:** Bearer token usuario  

---

## 10. Admin — Autenticación

### POST /api/v1/admin/auth/login
**Auth:** Ninguna  
**Rate limit:** 3 intentos / 15min / IP  

**Request:**
```json
{
  "email": "admin@novainvesa.com",
  "password": "adminPassword123"
}
```

**Response 200:**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGc...",
    "admin": {
      "id": 1,
      "email": "admin@novainvesa.com",
      "fullName": "Alejandro",
      "role": "SUPER_ADMIN"
    }
  }
}
```

---

## 11. Admin — Dashboard

### GET /api/v1/admin/dashboard
**Auth:** Bearer token admin  

**Response 200:**
```json
{
  "success": true,
  "data": {
    "salesToday": 3,
    "revenueToday": 109500,
    "salesThisMonth": 10,
    "revenueThisMonth": 365000,
    "pendingOrders": 2,
    "recentOrders": [ ... ],
    "topProducts": [ ... ],
    "salesLast30Days": [
      { "date": "2026-04-01", "sales": 1, "revenue": 36500 }
    ]
  }
}
```

---

## 12. Admin — Gestión de pedidos

### GET /api/v1/admin/orders
**Auth:** Bearer token admin  

**Query params:** `status`, `paymentMethod`, `search`, `page`, `limit`

### GET /api/v1/admin/orders/{id}
**Auth:** Bearer token admin  

### PUT /api/v1/admin/orders/{id}/status
**Auth:** Bearer token admin  

**Request:**
```json
{
  "status": "SHIPPED",
  "notes": "Enviado con Coordinadora guía 1234567890"
}
```

---

## 13. Admin — Importador de productos Dropi

### POST /api/v1/admin/products/preview
**Auth:** Bearer token admin  
**Descripción:** Buscar producto en Dropi por ID o URL  

**Request:**
```json
{
  "input": "1865251"
}
```

**Response 200:**
```json
{
  "success": true,
  "data": {
    "dropiId": "1865251",
    "name": "Ejercitador Multi",
    "suggestedPrice": 36500,
    "images": ["https://cdn.dropi.co/..."],
    "description": "...",
    "benefits": ["..."],
    "inStock": true,
    "alreadyImported": false
  }
}
```

**Errores:**
- `IMPORT_001` — Formato no reconocido
- `DROPI_001` — Producto no encontrado en Dropi

---

### POST /api/v1/admin/products/import
**Auth:** Bearer token admin  

**Request:**
```json
{
  "dropiProductId": "1865251",
  "name": "Ejercitador Multi — Kit 7 en 1",
  "price": 39900,
  "categorySlug": "fitness",
  "description": "...",
  "images": ["https://cdn.dropi.co/..."],
  "benefits": ["..."],
  "featured": false,
  "publishImmediately": true
}
```

**Response 201:**
```json
{
  "success": true,
  "data": {
    "id": 5,
    "slug": "ejercitador-multi-kit-7-en-1",
    "status": "ACTIVE",
    "missingFields": []
  }
}
```

---

### POST /api/v1/admin/products/import-bulk
**Auth:** Bearer token admin  

**Request:**
```json
{
  "inputs": ["1865251", "1923456", "https://app.dropi.co/.../1734521"]
}
```

**Response 202:**
```json
{
  "success": true,
  "data": {
    "jobId": "bulk-abc123",
    "total": 3,
    "status": "PROCESSING"
  }
}
```

---

### GET /api/v1/admin/products/import-bulk/{jobId}
**Auth:** Bearer token admin  

**Response 200:**
```json
{
  "success": true,
  "data": {
    "jobId": "bulk-abc123",
    "total": 3,
    "processed": 3,
    "published": 2,
    "drafts": 1,
    "errors": 0,
    "status": "COMPLETED",
    "results": [
      { "dropiId": "1865251", "status": "PUBLISHED", "productId": 5 },
      { "dropiId": "1923456", "status": "DRAFT", "missingFields": ["categorySlug"] },
      { "dropiId": "1734521", "status": "PUBLISHED", "productId": 7 }
    ]
  }
}
```

---

### GET /api/v1/admin/products
**Auth:** Bearer token admin  

**Query params:** `status` (ACTIVE/DRAFT/ARCHIVED), `category`, `search`, `page`, `limit`

### PUT /api/v1/admin/products/{id}
**Auth:** Bearer token admin  

### POST /api/v1/admin/products/{id}/publish
**Auth:** Bearer token admin  

### POST /api/v1/admin/products/{id}/archive
**Auth:** Bearer token admin  

---

## 14. Admin — Métricas

### GET /api/v1/admin/product-stats
**Auth:** Bearer token admin  

**Response 200:**
```json
{
  "success": true,
  "data": [
    {
      "productId": 1,
      "name": "Ejercitador Multi",
      "views": 245,
      "cartAdds": 32,
      "ordersCount": 8,
      "unitsSold": 10,
      "totalRevenue": 365000
    }
  ]
}
```

---

## 15. Pixel (Meta Conversions API)

### POST /api/v1/pixel/event
**Auth:** Ninguna  

**Request:**
```json
{
  "eventName": "Purchase",
  "eventId": "uuid-único",
  "value": 36500,
  "currency": "COP",
  "userData": {
    "email": "hash-sha256-del-email",
    "phone": "hash-sha256-del-telefono"
  },
  "customData": {
    "orderId": "NOVA-20260413-0001"
  }
}
```

---

## Códigos de error completos

| Código | Descripción |
|--------|-------------|
| `AUTH_001` | Token inválido o expirado |
| `AUTH_002` | Credenciales incorrectas |
| `AUTH_003` | Cuenta inactiva |
| `AUTH_004` | Email ya registrado |
| `ORDER_001` | Carrito vacío |
| `ORDER_002` | Producto sin stock |
| `ORDER_003` | Ciudad sin cobertura COD |
| `ORDER_004` | Total excede límite COD |
| `PAYMENT_001` | Error al crear preferencia de pago |
| `PAYMENT_002` | Webhook con firma inválida |
| `PRODUCT_001` | Producto no encontrado |
| `DROPI_001` | Producto no encontrado en Dropi |
| `DROPI_002` | Error de conexión con Dropi |
| `DROPI_003` | Producto ya importado |
| `IMPORT_001` | Formato de ID/URL no reconocido |
| `IMPORT_002` | Límite de importación masiva excedido (50) |
| `VALIDATION_001` | Campos requeridos faltantes o inválidos |
| `DB_001` | Error de base de datos |
| `NOT_FOUND` | Recurso no encontrado |
| `FORBIDDEN` | Sin permisos para este recurso |

