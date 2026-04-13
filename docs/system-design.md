# System Design — Novainvesa v2.0

**Versión:** 2.1  
**Fecha:** Abril 2026  

---

## 1. Arquitectura general

```
┌─────────────────────────────────────────────────────────────────────┐
│                         USUARIO FINAL                               │
│                    (navegador / móvil)                              │
└────────────────────────────┬────────────────────────────────────────┘
                             │ HTTPS
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    FRONTEND — Next.js 15                            │
│                  https://www.novainvesa.com                         │
│                    Vercel (CDN global)                              │
└────────────────────────────┬────────────────────────────────────────┘
                             │ REST API / HTTPS
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                  BACKEND — Spring Boot 3                            │
│              https://api.novainvesa.com                             │
│                    Render.com                                       │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────────────┐   │
│  │  Auth    │  │ Products │  │  Orders  │  │  Admin           │   │
│  │  JWT     │  │  Import  │  │  Payments│  │  Importer        │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────────────┘   │
└──────┬──────────────────────────────────┬───────────────────────────┘
       │                                  │
       ▼                                  ▼
┌──────────────┐                 ┌────────────────────┐
│   MySQL      │                 │   Servicios externos│
│  Hostinger   │                 │                    │
│              │                 │  Wompi    Webhooks  │
│  products    │                 │  MercadoPago        │
│  orders      │                 │  Dropi API          │
│  users       │                 │  N8n Cloud          │
│  admin_users │                 │  Hostinger SMTP     │
└──────────────┘                 │  Chatea Pro         │
                                 │  Meta Pixel CAPI    │
                                 └────────────────────┘
```

---

## 2. Flujo 1 — Compra como invitado

```
CLIENTE                    FRONTEND                   BACKEND                 EXTERNO
   │                          │                          │                       │
   │── Navega catálogo ───────▶│                          │                       │
   │                          │── GET /products ─────────▶│                       │
   │                          │◀─ productos ─────────────│                       │
   │◀─ muestra productos ─────│                          │                       │
   │                          │                          │                       │
   │── Agrega al carrito ─────▶│ (localStorage)           │                       │
   │                          │                          │                       │
   │── Va al checkout ────────▶│                          │                       │
   │◀─ muestra formulario ────│                          │                       │
   │   + banner "Regístrate   │                          │                       │
   │     para rastrear tu     │                          │                       │
   │     pedido fácilmente"   │                          │                       │
   │                          │                          │                       │
   │── Llena datos y confirma ▶│                          │                       │
   │                          │── POST /orders ──────────▶│                       │
   │                          │   (sin userId, guest=true)│                       │
   │                          │                          │── guarda en MySQL ────▶│
   │                          │◀─ { orderCode, total } ──│                       │
   │◀─ página confirmación ───│                          │                       │
   │   NOVA-20260401-0001      │                          │                       │
   │   "Guarda este código     │                          │                       │
   │    para rastrear tu pedido"│                         │                       │
```

---

## 3. Flujo 2 — Compra con cuenta registrada

```
CLIENTE                    FRONTEND                   BACKEND
   │                          │                          │
   │── Inicia sesión ─────────▶│                          │
   │                          │── POST /auth/login ───────▶│
   │                          │◀─ { token, user } ────────│
   │◀─ token guardado ────────│   (localStorage)          │
   │                          │                          │
   │── Agrega al carrito ─────▶│                          │
   │── Va al checkout ────────▶│                          │
   │                          │── POST /orders ──────────▶│
   │                          │   (con JWT token)         │── pedido vinculado
   │                          │◀─ { orderCode } ──────────│   al userId
   │◀─ confirmación ──────────│                          │
   │                          │                          │
   │── Va a "Mis pedidos" ────▶│                          │
   │                          │── GET /users/me/orders ───▶│
   │◀─ historial de pedidos ──│                          │
   │                          │                          │
   │── Rastrea pedido ────────▶│                          │
   │                          │── GET /orders/{code}/     │
   │                          │   tracking ──────────────▶│── consulta Dropi
   │◀─ estado del pedido ─────│                          │
```

---

## 4. Flujo 3 — Registro de usuario con incentivo

```
CHECKOUT (sin login)
       │
       │── El sistema muestra banner:
       │   ┌──────────────────────────────────────────────┐
       │   │ 📦 Registrate y obtén:                       │
       │   │ ✅ Rastrea tus pedidos en tiempo real         │
       │   │ ✅ Guarda tu dirección para la próxima compra │
       │   │ ✅ Lista de favoritos                         │
       │   │                                              │
       │   │ [Crear cuenta] [Continuar sin registro]      │
       │   └──────────────────────────────────────────────┘
       │
       ├── Si hace clic en "Crear cuenta":
       │   → Modal de registro rápido (solo email + password)
       │   → Datos del checkout se pre-llenan automáticamente
       │   → Al terminar la compra, cuenta queda creada y vinculada al pedido
       │
       └── Si hace clic en "Continuar sin registro":
           → Checkout normal como invitado
           → Al confirmar: "¿Quieres guardar tus datos para la próxima vez?
             Crea una cuenta con este email" (un clic)
```

---

## 5. Flujo 4 — Pago con MercadoPago

```
CLIENTE           FRONTEND              BACKEND              MP / N8N / DROPI
   │                 │                     │                       │
   │─ Confirmar ────▶│                     │                       │
   │  (MP)           │── POST /payments/   │                       │
   │                 │   mercadopago/create▶│                       │
   │                 │                     │── Crea preference ────▶ MercadoPago
   │                 │                     │◀─ { init_point } ─────│
   │                 │◀─ { redirectUrl } ──│                       │
   │◀─ redirige ─────│                     │                       │
   │  a MP           │                     │                       │
   │                 │                     │                       │
   │─ Paga en MP ───────────────────────────────────────────────────▶
   │                 │                     │                       │
   │◀─ MP redirige a ▶│                    │                       │
   │  /confirmacion  │                     │                       │
   │                 │                     │◀─ Webhook MP ─────────│
   │                 │                     │   payment.approved    │
   │                 │                     │── Verifica HMAC ──────│
   │                 │                     │── Actualiza orden ────▶ MySQL
   │                 │                     │   status = CONFIRMED  │
   │                 │                     │── Webhook → N8n ──────▶ N8n
   │                 │                     │                       │── Login Dropi
   │                 │                     │                       │── Crea pedido
   │                 │                     │                       │── status = PROCESSING
   │                 │                     │── Envía email/WA ─────▶ SMTP / Chatea Pro
```

---

## 6. Flujo 5 — Pago COD (Contra Entrega)

```
CLIENTE           FRONTEND              BACKEND              N8N / DROPI
   │                 │                     │                    │
   │─ Selecciona COD ▶│                    │                    │
   │                 │── GET /coverage/cod  │                    │
   │                 │   ?city=Bogotá ─────▶│                    │
   │                 │                     │── Consulta Dropi ──▶
   │                 │                     │◀─ { available: true}│
   │                 │◀─ COD disponible ───│                    │
   │                 │                     │                    │
   │─ Confirma COD ──▶│                    │                    │
   │                 │── POST /orders ─────▶│                    │
   │                 │   paymentMethod=COD  │── Guarda en MySQL ─▶
   │                 │                     │   status = CONFIRMED│
   │                 │                     │── Webhook → N8n ───▶ N8n
   │                 │◀─ { orderCode } ────│                    │── Crea en Dropi
   │◀─ confirmación ─│                     │                    │
   │                 │                     │── Email/WA ────────▶ SMTP/Chatea Pro
```

---

## 7. Flujo 6 — N8n automatiza pedido en Dropi

```
BACKEND (Spring Boot)              N8N CLOUD                    DROPI
         │                             │                            │
         │── POST webhook ────────────▶│                            │
         │   {                         │                            │
         │     orderId: "NOVA-...",    │                            │
         │     customer: {...},        │                            │
         │     items: [...],           │                            │
         │     secret: "hmac-hash"     │                            │
         │   }                         │                            │
         │                             │── Verifica HMAC secret ───│
         │                             │                            │
         │                             │── POST /api/v1/login ─────▶│
         │                             │◀─ { token } ──────────────│
         │                             │                            │
         │                             │── POST /api/v1/orders ────▶│
         │                             │   dropi-integracion-key   │
         │                             │◀─ { dropiOrderId } ───────│
         │                             │                            │
         │◀── Callback exitoso ────────│                            │
         │    { dropiOrderId }         │                            │
         │── Actualiza MySQL ──────────▶                            │
         │   dropi_order_id, status=   │                            │
         │   PROCESSING                │                            │
         │                             │                            │
         │  [Si N8n falla:]            │                            │
         │── Email al admin ───────────▶ SMTP                       │
         │   "Pedido NOVA-... necesita │                            │
         │    creación manual en Dropi"│                            │
```

---

## 8. Flujo 7 — Importar producto desde Dropi (Admin)

```
ADMIN             FRONTEND ADMIN          BACKEND              DROPI API
  │                    │                     │                     │
  │─ Pega ID/URL ─────▶│                     │                     │
  │                    │── POST /admin/       │                     │
  │                    │   products/preview ─▶│                     │
  │                    │                     │── Detecta ID/URL ───│
  │                    │                     │── GET /products/{id}▶│
  │                    │                     │◀─ datos producto ───│
  │                    │◀─ formulario ────────│                     │
  │                    │   pre-llenado        │                     │
  │◀─ ve datos del ────│                     │                     │
  │   producto          │                     │                     │
  │─ Edita precio/cat ▶│                     │                     │
  │─ Clic Publicar ───▶│                     │                     │
  │                    │── POST /admin/       │                     │
  │                    │   products/import ──▶│                     │
  │                    │                     │── Valida campos ────│
  │                    │                     │── Guarda en MySQL ──│
  │                    │◀─ { status: ACTIVE } │                     │
  │◀─ "Producto        │                     │                     │
  │   publicado ✅"    │                     │                     │
```

---

## 9. Flujo 8 — Notificaciones al cliente

```
PEDIDO CONFIRMADO
       │
       │── ¿Tiene número de WhatsApp?
       │      │
       │      ├── SÍ ──▶ Spring Boot → Chatea Pro API
       │      │          Chatea Pro envía WhatsApp:
       │      │          "✅ Tu pedido NOVA-... fue confirmado
       │      │           Total: $36.500 COP
       │      │           Entrega estimada: 3-5 días hábiles"
       │      │
       │      └── NO ──▶ Spring Boot → Hostinger SMTP
       │                 Email: confirmación con código y resumen
       │
       │── ¿Tiene email?
              │
              └── SÍ ──▶ Email de confirmación SIEMPRE
                         (incluso si ya se envió WhatsApp)
```

---

## 10. Flujo 9 — Rastreo de pedido

```
CLIENTE con código NOVA-...
          │
          ├── CON CUENTA:
          │   Inicia sesión → "Mis pedidos" → ve todos sus pedidos
          │   → Clic en pedido → ve timeline completo
          │
          └── SIN CUENTA:
              Va a /rastrear → pega código NOVA-...
              → Sistema busca el pedido
              → Muestra timeline:
                ┌─────────────────────────────────┐
                │ NOVA-20260401-0001               │
                │                                 │
                │ ✅ Pedido confirmado — 01/04     │
                │ ✅ En preparación — 01/04        │
                │ 🔄 En camino — 02/04             │
                │ ⏳ Entregado — pendiente         │
                └─────────────────────────────────┘

BACKEND:
GET /api/v1/orders/{orderCode}/tracking
→ Consulta MySQL (estado local)
→ Si tiene dropiOrderId → consulta Dropi para estado actualizado
→ Retorna timeline combinado
```

---

## 11. Modelo de datos — Tablas principales

```sql
-- Usuarios (compradores)
users
  id, email, password_hash, full_name, phone,
  is_active, last_login_at, created_at

-- Direcciones guardadas
user_addresses
  id, user_id, full_name, phone, department,
  city, address, neighborhood, is_default

-- Administradores (panel admin)
admin_users
  id, email, password_hash, full_name,
  role (SUPER_ADMIN / ADMIN), is_active, created_at

-- Productos del catálogo
products
  id, dropi_product_id, name, slug, category_slug,
  short_description, description, price, compare_at_price,
  currency, images (JSON), benefits (JSON),
  status (ACTIVE/DRAFT/ARCHIVED), missing_fields (JSON),
  in_stock, featured, weight, active,
  imported_at, created_at, updated_at

-- Categorías (para expansión futura)
categories
  id, slug, name, icon, color, active, sort_order

-- Pedidos
orders
  id, order_code (NOVA-YYYYMMDD-NNNN), user_id (nullable),
  customer_name, customer_email, customer_phone,
  shipping_department, shipping_city, shipping_address,
  shipping_neighborhood, shipping_notes,
  subtotal, shipping_cost, total, currency,
  payment_method (COD/WOMPI/MERCADOPAGO),
  payment_status (PENDING/CONFIRMED/FAILED/REFUNDED),
  order_status (PENDING/CONFIRMED/PROCESSING/SHIPPED/DELIVERED/RETURNED),
  dropi_order_id, n8n_job_id,
  wompi_transaction_id, mp_payment_id,
  created_at, updated_at

-- Items del pedido
order_items
  id, order_id, product_id, dropi_product_id,
  product_name, product_image, unit_price, quantity, subtotal

-- Métricas de productos
product_stats
  product_id, views, cart_adds, wishlist_count,
  orders_count, units_sold, total_revenue, last_updated

-- Búsquedas (para analítica)
product_searches
  id, query, results_count, user_id (nullable),
  session_id, created_at

-- Wishlist
wishlist
  id, user_id, product_id, created_at

-- Jobs de importación masiva
import_jobs
  id, job_id, admin_id, total, processed,
  published, drafts, errors, status,
  results (JSON), created_at, completed_at
```

---

## 12. Seguridad del sistema

```
┌─────────────────────────────────────────────────────┐
│                  CAPAS DE SEGURIDAD                 │
├─────────────────────────────────────────────────────┤
│ 1. HTTPS en todos los endpoints (Vercel + Render)   │
│ 2. CORS: solo novainvesa.com + localhost:3000       │
│ 3. JWT usuarios: 7 días, JWT admin: 24 horas        │
│ 4. BCrypt 12 rounds para contraseñas               │
│ 5. Rate limit: 200 req/15min general               │
│              5 intentos/15min en login             │
│ 6. Webhooks: verificación HMAC-SHA256              │
│ 7. BD: acceso remoto solo desde IP de Render       │
│ 8. Variables de entorno: nunca en el código        │
│ 9. Spring Actuator: deshabilitado en producción    │
│ 10. Agente seguridad: revisa antes de merge→main   │
└─────────────────────────────────────────────────────┘
```

---

## 13. Manejo de errores

### Códigos de error estándar del backend
```
AUTH_001 — Token inválido o expirado
AUTH_002 — Credenciales incorrectas
AUTH_003 — Cuenta inactiva
ORDER_001 — Carrito vacío
ORDER_002 — Producto sin stock
ORDER_003 — Ciudad sin cobertura COD
ORDER_004 — Total excede límite COD ($500.000 COP)
PAYMENT_001 — Error al crear preferencia de pago
PAYMENT_002 — Webhook firma inválida
DROPI_001 — Producto no encontrado en Dropi
DROPI_002 — Error de conexión con Dropi
DROPI_003 — Producto ya importado
IMPORT_001 — Formato de ID/URL no reconocido
IMPORT_002 — Límite de importación masiva excedido (50)
DB_001 — Error de base de datos
```

---

## 14. Rendimiento y escalabilidad

### Estrategia para Render free tier (512MB RAM)
```
Spring Boot optimizaciones:
- spring.jpa.open-in-view=false (evita N+1 queries)
- Lazy loading en relaciones JPA
- Connection pool: máximo 10 conexiones MySQL
- Caché en memoria para categorías y productos destacados
- Timeout de 30s en llamadas a Dropi/N8n
```

### Estrategia Next.js en Vercel
```
- Static Generation (SSG) para Home y páginas de categorías
- Incremental Static Regeneration (ISR) cada 60s para productos
- Dynamic rendering solo para checkout y cuenta
- Imágenes optimizadas con next/image
- CDN global de Vercel para assets
```

