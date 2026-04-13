# PRD — Product Requirements Document
## Novainvesa v2.0

**Versión:** 2.1  
**Fecha:** Abril 2026  
**Estado:** En desarrollo  
**Cambios v2.1:** Importación de productos desde Dropi + Seguridad con agente Claude Code

---

## 1. Visión del producto

Novainvesa es una tienda de dropshipping multi-proveedor con marca propia, dirigida a toda Latinoamérica. El proveedor principal es Dropi (Colombia). Los pedidos se procesan automáticamente a través de N8n y el catálogo se gestiona desde un panel de administración con importación directa desde Dropi.

---

## 2. Objetivos del MVP

### Objetivo primario
Generar las primeras 10 ventas reales con el flujo completo funcionando.

### Objetivo secundario  
Tienda 100% funcional: catálogo, carrito, checkout, pagos, automatización, notificaciones y panel de admin con importación de productos.

### Métricas de éxito
| Métrica | Meta |
|---------|------|
| Ventas completadas | 10 pedidos reales |
| Automatización Dropi | 100% via N8n |
| Tiempo importar producto | < 30 segundos |
| Tiempo de carga home | < 2 segundos |
| Uptime backend | > 99% |
| Países con pedidos exitosos | Mínimo 2 |

---

## 3. Público objetivo

### Compradores finales (B2C)
- Personas 18-45 años en Colombia, México, Chile, Perú, Argentina
- Prefieren COD en Colombia, digital en otros países
- Llegan vía Meta Ads, Instagram, TikTok

### Emprendedores dropshippers (B2B)
- Buscan productos para revender
- Quieren ver catálogo con precios sugeridos

---

## 4. Categorías de productos

### Categorías iniciales (MVP)
| Slug | Nombre | Ícono | Color |
|------|---------|-------|-------|
| `mascotas` | Mascotas | 🐾 | #F59E0B |
| `hogar` | Hogar | 🏠 | #10B981 |
| `tecnologia` | Tecnología | 📱 | #6366F1 |
| `belleza` | Belleza | 💄 | #EC4899 |
| `fitness` | Fitness | 💪 | #EF4444 |

> Sistema diseñado para agregar categorías nuevas sin cambios en código.

---

## 5. Funcionalidades requeridas

### 5.1 Catálogo de productos
- Grid por categoría con filtros y búsqueda
- Página de detalle con galería, descripción, beneficios, relacionados
- Badge de stock / agotado / oferta
- Precio sugerido y precio tachado

### 5.2 Carrito de compras
- Agregar/eliminar/cambiar cantidades (máx 10/producto, 20 items)
- Persistencia localStorage 7 días
- Drawer lateral deslizante

### 5.3 Checkout
- Formulario de datos personales y envío con validación en tiempo real
- Selector de método de pago: COD / Wompi / MercadoPago
- Verificación de cobertura COD por ciudad
- Resumen sticky del pedido

### 5.4 Pagos
- COD, Wompi, MercadoPago Checkout Pro
- Webhooks con verificación HMAC-SHA256

### 5.5 Automatización de pedidos (N8n)
- Backend → webhook → N8n → Dropi al confirmar pago
- Fallback: email/WhatsApp al admin si N8n falla
- Registro de intentos y errores en BD

### 5.6 Notificaciones
- Email confirmación de pedido (Hostinger SMTP)
- WhatsApp confirmación y seguimiento (Chatea Pro)

### 5.7 ⭐ NUEVA — Importación de productos desde Dropi

**Descripción:**  
El admin puede importar productos directamente desde Dropi pegando el ID del producto o la URL completa. El sistema detecta automáticamente si es un ID o URL, extrae todos los datos del producto usando el token de integración de Dropi, y los presenta en un formulario editable antes de publicar.

**Flujo completo:**
```
Admin pega ID o URL de Dropi
         ↓
Sistema detecta formato (ID numérico o URL)
         ↓
Backend llama a Dropi API con el token de integración
GET https://api.dropi.co/api/v1/products/{ID}
         ↓
Sistema extrae: nombre, precio, imágenes, descripción, beneficios
         ↓
Admin ve formulario pre-llenado y puede editar:
  - Nombre del producto
  - Precio de venta (sugerido por Dropi, editable)
  - Descripción
  - Categoría (seleccionar de las 5)
  - Imágenes (ver cuáles incluir)
         ↓
Admin hace clic en "Publicar" o "Guardar borrador"
         ↓
Si tiene toda la info → status = ACTIVE (publicado)
Si falta algo → status = DRAFT (borrador)
         ↓
Producto aparece en la tienda inmediatamente
```

**Importación masiva:**
- El admin puede pegar múltiples IDs o URLs separados por coma o salto de línea
- El sistema los procesa uno por uno en paralelo
- Muestra progreso en tiempo real (5/10 importados...)
- Al final muestra resumen: X publicados, Y borradores, Z errores

**Reglas de publicación automática:**
| Campo | Requerido para publicar |
|-------|------------------------|
| Nombre | ✅ Sí |
| Precio > 0 | ✅ Sí |
| Al menos 1 imagen | ✅ Sí |
| Categoría asignada | ✅ Sí |
| Descripción | ❌ No (puede publicar sin ella) |

**Endpoint nuevo:**
- `POST /api/v1/admin/products/import` — importar por ID o URL
- `POST /api/v1/admin/products/import-bulk` — importación masiva
- `PUT /api/v1/admin/products/{id}` — editar antes de publicar
- `POST /api/v1/admin/products/{id}/publish` — publicar borrador

### 5.8 Panel de administración
- Login separado con JWT admin
- Dashboard con métricas
- **Importador de productos desde Dropi** (nueva sección)
- Gestión de productos: ver, editar, publicar, archivar
- Gestión de pedidos con filtros y cambio de estado
- Métricas de productos más vistos/vendidos

### 5.9 Autenticación de usuarios
- Registro e inicio de sesión con JWT
- Historial de pedidos, dirección guardada, wishlist

### 5.10 SEO y Marketing
- Meta tags dinámicos, sitemap, Meta Pixel, GA4, Open Graph

### 5.11 Internacionalización
- ES/EN/PT con next-intl, persistencia en localStorage

---

## 6. ⭐ NUEVA — Seguridad con agente Claude Code

**Descripción:**  
Al finalizar el desarrollo de cada módulo importante, el agente de seguridad de Claude Code realiza una revisión completa del código buscando vulnerabilidades específicas de una tienda e-commerce.

**Cuándo se ejecuta:**
- Cuando se termina el módulo de autenticación
- Cuando se termina el módulo de pagos y webhooks
- Cuando se termina el módulo de admin
- Antes de hacer merge de `dev` → `main` (versión de producción)
- Cuando el usuario lo pide explícitamente

**Qué revisa el agente:**
| Área | Vulnerabilidades que busca |
|------|---------------------------|
| Autenticación | JWT mal configurado, bcrypt débil, tokens sin expiración |
| Pagos | HMAC no verificado, webhooks sin autenticación |
| Admin | Endpoints admin accesibles sin token, IDOR |
| SQL | Queries con concatenación de strings (SQL injection) |
| API | CORS demasiado permisivo, rate limiting ausente |
| Datos | Contraseñas o tokens en logs, env vars en código |
| Spring Boot | Actuator expuesto, configuraciones inseguras por defecto |
| Frontend | XSS, datos sensibles en localStorage, API keys expuestas |

**Cómo invocarlo:**
```
Use the seguridad-novainvesa agent to review 
the authentication module for security vulnerabilities
```

**Output del agente:**
- Lista de vulnerabilidades por severidad: CRÍTICA / ALTA / MEDIA / BAJA
- Archivo y línea exacta donde está el problema
- Cómo corregirlo
- Verificación final: "0 vulnerabilidades críticas o altas"

---

## 7. Funcionalidades fuera del alcance del MVP

- App móvil nativa
- Sistema de reseñas
- Programa de afiliados
- Chat en vivo
- Cupones y descuentos
- Múltiples monedas
- Facturación electrónica DIAN
- Integración automática con proveedores adicionales

---

## 8. Proveedores e integraciones

| Servicio | Propósito | Estado |
|---------|-----------|--------|
| Dropi | Catálogo + fulfillment pedidos | Token de integración activo |
| N8n | Automatización pedidos → Dropi | Por configurar |
| Wompi | Pagos Colombia | Por configurar |
| MercadoPago | Pagos LATAM | Credenciales activas |
| Chatea Pro | WhatsApp bot | Por configurar |
| Hostinger SMTP | Emails transaccionales | Activo |
| Meta Pixel + CAPI | Conversiones Meta Ads | Por configurar |
| GA4 | Analítica web | Por configurar |

---

## 9. Restricciones

- Frontend en Vercel (Next.js static/SSG)
- Backend en Render free tier (512MB RAM — optimizar Spring Boot)
- BD MySQL en Hostinger (conexión remota habilitada)
- Catálogo gestionado manualmente via panel admin (API Dropi no pública)
- Precios en COP para el MVP
- COD solo en ciudades con cobertura Dropi

---

## 10. Flujo principal del usuario

```
Descubre Novainvesa → Navega categorías → Ve producto
→ Agrega al carrito → Checkout → Paga
→ Confirmación NOVA-YYYYMMDD-NNNN
→ N8n crea pedido en Dropi
→ Dropi envía producto
→ Cliente recibe en 24-72h
```

---

## 11. Historial de versiones

| Versión | Fecha | Cambios |
|---------|-------|---------|
| 1.0 | Oct 2025 | PRD inicial React + Node.js |
| 2.0 | Abr 2026 | Migración Next.js + Spring Boot, N8n |
| 2.1 | Abr 2026 | Importador Dropi + agente seguridad |

