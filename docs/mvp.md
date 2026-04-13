# MVP — Plan de Desarrollo
## Novainvesa v2.0

**Versión:** 2.1  
**Fecha inicio:** Abril 2026  
**Duración:** 3-4 semanas  
**País objetivo:** Colombia  
**Meta de productos al lanzar:** 25 (5 por categoría)  
**Meta de ventas:** 10 pedidos reales confirmados  

---

## Criterios de éxito del MVP

```
✅ Un cliente puede navegar, agregar al carrito y comprar
✅ Los pagos funcionan (COD + MercadoPago + Wompi)
✅ N8n crea el pedido en Dropi automáticamente
✅ El cliente recibe confirmación por email o WhatsApp
✅ El admin puede importar productos desde Dropi
✅ 25 productos activos en el catálogo
✅ Funciona en Colombia (cobertura COD verificada)
✅ 0 vulnerabilidades críticas o altas (revisión del agente seguridad)
```

---

## Lo que NO entra en el MVP

```
❌ App móvil
❌ Múltiples países (después de Colombia)
❌ Cupones y descuentos
❌ Sistema de reseñas
❌ Programa de afiliados
❌ Chat en vivo
❌ Facturación electrónica DIAN
❌ Múltiples monedas
❌ Integración con más proveedores (después de Dropi)
❌ Tests automatizados (se agregan en v2.1)
```

---

## Semanas de desarrollo

---

### SEMANA 1 — Base del proyecto + Backend core

**Objetivo:** Repositorio configurado, backend funcionando con BD conectada

#### Día 1-2 — Setup del proyecto
- [ ] Crear repositorio nuevo en GitHub con estructura monorepo
- [ ] Crear ramas: `main`, `dev`
- [ ] Subir todos los documentos de `docs/` al repositorio
- [ ] Configurar `CLAUDE.md` y agentes en `.claude/agents/`
- [ ] Crear proyecto Spring Boot 3 en `Backend/`
  - [ ] Dependencias: Spring Web, Spring Security, Spring Data JPA, MySQL Driver, JWT, Validation, Mail
  - [ ] Configurar `application.properties` con variables de entorno
  - [ ] Conectar a MySQL de Hostinger (verificar conexión)
- [ ] Crear proyecto Next.js 15 en `Frontend/`
  - [ ] Instalar: Tailwind, Shadcn/UI, Zustand, Axios, next-intl
  - [ ] Configurar `next.config.ts` con static export
  - [ ] Configurar next-intl para ES/EN/PT

#### Día 3-4 — Backend: Autenticación + Productos
- [ ] Entidades JPA: `User`, `AdminUser`, `Product`, `Category`
- [ ] Repositorios JPA para cada entidad
- [ ] `AuthController`: POST /auth/register, POST /auth/login
- [ ] `JwtService`: generar y validar tokens
- [ ] `SecurityConfig`: CORS, JWT filter, rutas públicas vs protegidas
- [ ] `ProductController`: GET /products, GET /products/{slug}
- [ ] `ProductService`: leer de MySQL con filtros (categoría, featured, búsqueda)
- [ ] Script SQL: crear todas las tablas (ver `docs/modelo-datos.md`)
- [ ] Ejecutar script en phpMyAdmin de Hostinger

#### Día 5 — Backend: Pedidos básicos + Deploy inicial
- [ ] Entidades: `Order`, `OrderItem`
- [ ] `OrderController`: POST /orders (COD), GET /orders/{code}
- [ ] `OrderService`: generar código NOVA-YYYYMMDD-NNNN, guardar en MySQL
- [ ] Deploy inicial en Render.com
  - [ ] Conectar repositorio GitHub
  - [ ] Configurar variables de entorno en Render
  - [ ] Verificar que `GET /api/health` responde en producción
- [ ] GitHub Actions: workflow `deploy-backend.yml` (push a main → Render)

---

### SEMANA 2 — Frontend core + Pagos

**Objetivo:** Tienda visible con productos reales, checkout funcionando

#### Día 6-7 — Frontend: Layout + Catálogo
- [ ] Componentes de layout: `Navbar`, `Footer`, `WhatsAppFloat`
- [ ] Modo oscuro con toggle (Zustand store)
- [ ] Selector de idioma (next-intl)
- [ ] `HomePage`: HeroBanner, CategoryGrid, FeaturedProducts
- [ ] `CategoryPage`: grid de productos con filtros
- [ ] `ProductCard`: imagen, precio, badge stock/oferta
- [ ] `ProductPage`: galería, detalle, beneficios, relacionados
- [ ] Conectar con backend: GET /products, GET /products/{slug}

#### Día 8 — Frontend: Carrito
- [ ] `CartStore` (Zustand): agregar, eliminar, cambiar cantidad
- [ ] Persistencia en localStorage (7 días)
- [ ] `CartDrawer`: slide lateral con items y subtotal
- [ ] `CartPage`: vista completa del carrito
- [ ] Límites: máx 10 por producto, máx 20 items

#### Día 9-10 — Frontend: Checkout + Pagos backend
- [ ] `CheckoutPage`: formulario con validaciones en tiempo real
- [ ] `PaymentSelector`: COD / Wompi / MercadoPago
- [ ] Verificación cobertura COD: GET /coverage/cod?city=...
- [ ] Incentivo de registro en checkout (banner con beneficios)
- [ ] Backend: `PaymentController`
  - [ ] POST /payments/mercadopago/create
  - [ ] POST /payments/wompi/create
- [ ] Backend: `WebhookController`
  - [ ] POST /webhooks/mercadopago (verificación HMAC)
  - [ ] POST /webhooks/wompi (verificación HMAC)
- [ ] Configurar webhooks en MercadoPago y Wompi
- [ ] `ConfirmationPage`: código pedido, resumen, botones

---

### SEMANA 3 — Automatización + Admin + Importador

**Objetivo:** Pedidos llegan a Dropi automáticamente, admin puede gestionar productos

#### Día 11-12 — N8n: Automatización Dropi
- [ ] Crear cuenta en N8n Cloud (n8n.io)
- [ ] Workflow 1: "Crear pedido en Dropi"
  - [ ] Trigger: Webhook desde Spring Boot
  - [ ] Verificar HMAC del webhook
  - [ ] HTTP Request: POST login Dropi
  - [ ] HTTP Request: POST crear pedido Dropi
  - [ ] HTTP Request: callback a Spring Boot con dropiOrderId
  - [ ] Error handler: email al admin si falla
- [ ] Agregar URL del webhook de N8n en variables de entorno de Render
- [ ] Prueba end-to-end: pedido COD → llega a Dropi ✅

#### Día 13-14 — Backend: Admin + Importador Dropi
- [ ] `AdminController`: login, dashboard, pedidos, usuarios
- [ ] `AdminProductController`:
  - [ ] POST /admin/products/preview (buscar en Dropi)
  - [ ] POST /admin/products/import (importar individual)
  - [ ] POST /admin/products/import-bulk (importar masivo)
  - [ ] PUT /admin/products/{id} (editar)
  - [ ] POST /admin/products/{id}/publish (publicar borrador)
  - [ ] POST /admin/products/{id}/archive (archivar)
- [ ] `DropiImportService`: detectar ID/URL, llamar a Dropi API, mapear campos
- [ ] `BulkImportJob`: procesar múltiples productos en paralelo
- [ ] Tabla `import_jobs` para seguimiento de importaciones masivas

#### Día 15 — Frontend: Panel de admin
- [ ] `AdminLoginPage`: formulario con JWT admin separado
- [ ] `AdminDashboard`: métricas básicas (ventas hoy, pedidos recientes)
- [ ] `AdminProductosPage`: lista de productos con filtros
- [ ] `AdminImportadorPage`:
  - [ ] Campo para pegar ID o URL
  - [ ] Formulario editable pre-llenado
  - [ ] Textarea para importación masiva
  - [ ] Barra de progreso
  - [ ] Resumen de resultados

---

### SEMANA 4 — Notificaciones + SEO + QA + Lanzamiento

**Objetivo:** Tienda lista para recibir tráfico real, 25 productos cargados

#### Día 16-17 — Notificaciones + Rastreo
- [ ] Backend: `EmailService` con Hostinger SMTP
  - [ ] Plantilla HTML: confirmación de pedido
  - [ ] Lógica: enviar email siempre si tiene email
- [ ] Backend: `WhatsAppService` con Chatea Pro API
  - [ ] Enviar WhatsApp si tiene número, si no solo email
- [ ] Backend: GET /orders/{code}/tracking
  - [ ] Consulta MySQL + Dropi para estado actualizado
- [ ] Frontend: `TrackingPage` (/rastrear)
  - [ ] Campo para pegar código NOVA-...
  - [ ] Timeline visual del pedido

#### Día 18 — SEO + Meta Pixel
- [ ] Next.js: meta tags dinámicos por página (generateMetadata)
- [ ] Sitemap automático (next-sitemap)
- [ ] Open Graph para compartir en redes
- [ ] Meta Pixel: eventos PageView, ViewContent, AddToCart, Purchase
- [ ] GA4: configurar y verificar eventos

#### Día 19 — Revisión de seguridad
- [ ] Invocar agente de seguridad en Claude Code:
  ```
  Use the seguridad-novainvesa agent to review 
  the complete backend for security vulnerabilities
  ```
- [ ] Corregir todos los hallazgos CRÍTICOS y ALTOS
- [ ] Segunda revisión: confirmar 0 críticos y 0 altos
- [ ] Hacer merge `dev` → `main`

#### Día 20 — Cargar productos + Deploy final
- [ ] Usar el importador del panel admin para cargar los 25 productos:
  ```
  Mascotas:   5 productos ← importar desde Dropi
  Hogar:      5 productos ← importar desde Dropi
  Tecnología: 5 productos ← importar desde Dropi
  Belleza:    5 productos ← importar desde Dropi
  Fitness:    5 productos ← importar desde Dropi
  ```
- [ ] Verificar que todas las imágenes cargan correctamente
- [ ] Configurar DNS: apuntar novainvesa.com a Vercel
- [ ] Deploy frontend en Vercel (conectar repositorio GitHub)
- [ ] Verificar que `www.novainvesa.com` carga en producción
- [ ] Prueba end-to-end completa en producción:
  - [ ] Navegar catálogo ✅
  - [ ] Agregar al carrito ✅
  - [ ] Checkout COD ✅
  - [ ] Checkout MercadoPago ✅
  - [ ] Pedido llega a Dropi ✅
  - [ ] Email/WhatsApp recibido ✅
  - [ ] Admin puede importar producto ✅

---

## GitHub Actions — Workflows

### deploy-frontend.yml
```yaml
# Trigger: push a main con cambios en Frontend/
# Acción: Vercel despliega automáticamente (sin workflow manual)
# Vercel detecta el push y despliega solo
```

### deploy-backend.yml
```yaml
# Trigger: push a main con cambios en Backend/
# Acción: Render redespliega automáticamente
# Render detecta el push y redespliega solo
```

---

## Checklist de lanzamiento

### Infraestructura
- [ ] DNS novainvesa.com apuntando a Vercel
- [ ] Backend respondiendo en api.novainvesa.com o api-novainvesa.onrender.com
- [ ] MySQL conectado y con datos
- [ ] Variables de entorno configuradas en Vercel y Render
- [ ] GitHub Actions funcionando para ambos deploys

### Funcionalidad
- [ ] 25 productos activos (5 por categoría)
- [ ] Checkout COD funcionando en Bogotá
- [ ] MercadoPago procesando pagos reales
- [ ] Wompi configurado y activo
- [ ] N8n creando pedidos en Dropi automáticamente
- [ ] Emails de confirmación llegando
- [ ] WhatsApp funcionando con Chatea Pro

### Seguridad
- [ ] 0 vulnerabilidades CRÍTICAS
- [ ] 0 vulnerabilidades ALTAS
- [ ] HTTPS en todos los endpoints
- [ ] Variables de entorno fuera del código

### Marketing
- [ ] Meta Pixel activo y recibiendo eventos
- [ ] GA4 configurado
- [ ] Imágenes Open Graph para compartir en redes

---

## Orden de prioridad si el tiempo es corto

Si hay que recortar, priorizar en este orden:
```
1. Catálogo + carrito + checkout COD     ← sin esto no hay ventas
2. MercadoPago                           ← pago más usado en LATAM
3. N8n → Dropi                           ← automatización core
4. Email de confirmación                 ← confianza del cliente
5. Panel admin + importador              ← gestión del catálogo
6. Wompi                                 ← pago secundario
7. WhatsApp (Chatea Pro)                 ← mejora la experiencia
8. Rastreo de pedidos                    ← nice to have
9. SEO + Meta Pixel                      ← para cuando haya tráfico
10. Revisión de seguridad               ← antes de escalar
```

---

## Historial de versiones

| Versión | Fecha | Cambios |
|---------|-------|---------|
| 1.0 | Oct 2025 | Plan inicial |
| 2.0 | Abr 2026 | Rediseño completo Next.js + Spring Boot |
| 2.1 | Abr 2026 | Agregado importador Dropi + seguridad |

