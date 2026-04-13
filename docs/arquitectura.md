# Arquitectura Técnica — Novainvesa v2.0

**Versión:** 2.0  
**Fecha:** Abril 2026  
**Estado:** En diseño  

---

## 1. Visión general

```
┌─────────────────────────────────────────────────────────────────┐
│                        NOVAINVESA v2.0                          │
│                                                                 │
│  ┌──────────────┐     ┌──────────────┐     ┌────────────────┐  │
│  │   FRONTEND   │────▶│   BACKEND    │────▶│   BASE DATOS   │  │
│  │  Next.js 15  │     │ Spring Boot 3│     │  MySQL         │  │
│  │  Vercel      │     │ Render.com   │     │  Hostinger     │  │
│  └──────────────┘     └──────────────┘     └────────────────┘  │
│         │                    │                                  │
│         │             ┌──────▼──────┐                          │
│         │             │    N8n      │                          │
│         │             │  (Webhooks) │                          │
│         │             └──────┬──────┘                          │
│         │                    │                                  │
│         │             ┌──────▼──────┐                          │
│         │             │    DROPI    │                          │
│         │             │  (Proveedor)│                          │
│         │             └─────────────┘                          │
└─────────────────────────────────────────────────────────────────┘
```

---

## 2. Stack tecnológico

### Frontend
| Componente | Tecnología | Versión | Justificación |
|-----------|-----------|---------|---------------|
| Framework | Next.js | 15 (App Router) | SEO nativo, static export, rendimiento |
| Estilos | Tailwind CSS | 4 | Utilitario, dark mode, responsive |
| Componentes | Shadcn/UI | Latest | Componentes accesibles y modernos |
| Estado global | Zustand | 5 | Ligero, simple, sin boilerplate |
| HTTP | Axios | 1.x | Interceptores, manejo de errores |
| i18n | next-intl | 3.x | ES/EN/PT, integrado con App Router |
| Formularios | React Hook Form + Zod | Latest | Validación tipada |
| Hosting | Vercel | - | Gratis, optimizado para Next.js |
| Dominio | novainvesa.com | - | DNS en Hostinger → apunta a Vercel |

### Backend
| Componente | Tecnología | Versión | Justificación |
|-----------|-----------|---------|---------------|
| Framework | Spring Boot | 3.3 | Robusto, escalable, conocido por el equipo |
| Lenguaje | Java | 21 (LTS) | Estabilidad, rendimiento |
| ORM | Spring Data JPA + Hibernate | - | Estándar, productivo |
| BD | MySQL | 8.0 | Plan existente en Hostinger |
| Seguridad | Spring Security + JWT | - | Autenticación stateless |
| Validación | Jakarta Validation | - | Integrado en Spring Boot |
| Email | Spring Mail + SMTP | - | Hostinger SMTP |
| Hosting | Render.com | Free tier | Deploy automático desde GitHub |

### Automatización
| Componente | Tecnología | Notas |
|-----------|-----------|-------|
| Orquestador | N8n | Cloud o self-hosted (decidir) |
| Trigger | Webhook desde Spring Boot | POST al confirmar pedido |
| Acción | HTTP Request → Dropi API | Login + crear orden |
| Fallback | Email/WhatsApp al admin | Si N8n falla |

### Infraestructura
| Componente | Servicio | Plan |
|-----------|---------|------|
| Frontend hosting | Vercel | Gratuito |
| Backend hosting | Render.com | Free (512MB RAM) |
| Base de datos | Hostinger MySQL | Plan existente |
| Dominio | Hostinger | Plan existente |
| Email SMTP | Hostinger | Plan existente |
| CDN imágenes | Dropi CloudFront | URLs de Dropi |
| CI/CD | GitHub Actions | Gratuito |

---

## 3. Repositorio — Estructura monorepo

```
Novainvesa/                          ← raíz del monorepo
├── CLAUDE.md                        ← configuración Claude Code
├── .claude/
│   ├── agents/                      ← agentes especializados
│   │   ├── arquitecto.md
│   │   ├── backend.md
│   │   ├── frontend.md
│   │   ├── qa.md
│   │   ├── devops.md
│   │   └── seguridad.md
│   └── skills/                      ← skills reutilizables
│       ├── novainvesa-context.md
│       ├── spring-boot-patterns.md
│       ├── nextjs-patterns.md
│       ├── dropi-api.md
│       ├── n8n-workflows.md
│       ├── seguridad-web.md
│       └── deployment.md
├── docs/                            ← documentación técnica
│   ├── PRD.md
│   ├── arquitectura.md              ← este archivo
│   ├── system-design.md
│   ├── mvp.md
│   ├── api-contract.md
│   ├── modelo-datos.md
│   ├── design-system.md
│   └── reglas-negocio.md
├── Frontend/                        ← Next.js 15
│   ├── app/                         ← App Router
│   │   ├── [locale]/                ← rutas con idioma
│   │   │   ├── page.tsx             ← Home
│   │   │   ├── categoria/[slug]/
│   │   │   ├── producto/[slug]/
│   │   │   ├── carrito/
│   │   │   ├── checkout/
│   │   │   ├── confirmacion/
│   │   │   ├── cuenta/
│   │   │   └── admin/
│   │   └── api/                     ← API routes (si se necesitan)
│   ├── components/
│   │   ├── ui/                      ← Shadcn/UI base
│   │   ├── layout/                  ← Navbar, Footer
│   │   ├── product/                 ← ProductCard, ProductDetail
│   │   ├── cart/                    ← CartDrawer, CartItem
│   │   ├── checkout/                ← CheckoutForm, PaymentSelector
│   │   └── common/                  ← LoadingSpinner, ErrorMessage
│   ├── lib/
│   │   ├── api.ts                   ← cliente Axios
│   │   ├── store/                   ← Zustand stores
│   │   └── utils/                   ← helpers, formatters
│   ├── messages/                    ← traducciones i18n
│   │   ├── es.json
│   │   ├── en.json
│   │   └── pt.json
│   ├── public/
│   └── next.config.ts
├── Backend/                         ← Spring Boot 3
│   └── src/main/java/com/novainvesa/
│       ├── NovainvesaApplication.java
│       ├── config/                  ← SecurityConfig, CorsConfig
│       ├── controller/              ← REST controllers
│       ├── service/                 ← lógica de negocio
│       ├── repository/              ← JPA repositories
│       ├── entity/                  ← entidades JPA
│       ├── dto/                     ← Data Transfer Objects
│       ├── exception/               ← manejo de errores
│       └── util/                    ← helpers
└── .github/
    └── workflows/
        ├── deploy-frontend.yml      ← Vercel deploy (automático)
        └── deploy-backend.yml       ← Render deploy (automático)
```

---

## 4. Estrategia de ramas

```
main                    ← producción (protegida)
└── dev                 ← integración
    ├── feat/frontend/nombre-feature
    ├── feat/backend/nombre-feature
    ├── fix/frontend/nombre-bug
    └── fix/backend/nombre-bug
```

**Flujo de trabajo:**
1. Crear rama desde `dev`: `git checkout -b feat/backend/autenticacion-jwt dev`
2. Desarrollar y hacer commits en la rama
3. Merge a `dev` cuando esté listo y probado
4. Merge `dev` → `main` para deploy a producción

---

## 5. Flujo de deploy

### Frontend (Next.js → Vercel)
```
git push origin feat/frontend/nombre
       ↓
Pull Request → dev
       ↓
Merge a dev (preview deploy en Vercel automático)
       ↓
Merge dev → main
       ↓
Vercel despliega a producción automáticamente
novainvesa.com actualizado ✅
```

### Backend (Spring Boot → Render)
```
git push origin feat/backend/nombre
       ↓
Pull Request → dev
       ↓
Merge a dev
       ↓
Merge dev → main
       ↓
Render detecta cambios en Backend/ y redespliega
api.novainvesa.com actualizado ✅
```

---

## 6. Flujo de pedido completo

```
1. Cliente confirma pedido en checkout
          ↓
2. Frontend → POST /api/v1/orders (Spring Boot)
          ↓
3. Spring Boot valida, guarda en MySQL
   status = PENDING_PAYMENT
          ↓
4a. COD → status = CONFIRMED inmediatamente
4b. Wompi/MP → redirige al cliente a pasarela de pago
          ↓
5. Webhook de pasarela → Spring Boot
   Spring Boot verifica HMAC → status = CONFIRMED
          ↓
6. Spring Boot dispara webhook → N8n
          ↓
7. N8n hace login en Dropi → crea pedido en Dropi
   Si OK → status = PROCESSING
   Si falla → status = DROPI_ERROR + email al admin
          ↓
8. Dropi gestiona empaque y envío
          ↓
9. Chatea Pro notifica al cliente por WhatsApp
```

---

## 7. Seguridad

| Capa | Medida |
|------|--------|
| API | CORS restringido a novainvesa.com |
| Auth | JWT con expiración 7 días (usuarios) / 24h (admin) |
| Webhooks | Verificación HMAC-SHA256 |
| BD | Conexión remota solo desde IP de Render |
| Contraseñas | BCrypt con 12 rounds |
| Rate limiting | 200 req/15min general, 5 intentos/15min en login |
| Variables | Nunca en código, solo en env vars |
| Admin | JWT separado con JWT_ADMIN_SECRET distinto |

---

## 8. Variables de entorno

### Frontend (Vercel)
```
NEXT_PUBLIC_API_URL=https://api.novainvesa.com
NEXT_PUBLIC_MP_PUBLIC_KEY=APP_USR-...
NEXT_PUBLIC_META_PIXEL_ID=...
NEXT_PUBLIC_GA4_ID=...
NEXT_PUBLIC_WHATSAPP_NUMBER=...
```

### Backend (Render)
```
# Servidor
PORT=8080
SPRING_PROFILES_ACTIVE=production
FRONTEND_URL=https://www.novainvesa.com

# Base de datos
DB_HOST=srv1070.hstgr.io
DB_PORT=3306
DB_NAME=u228070604_novainvesa_db
DB_USER=u228070604_Alejjorojjas
DB_PASSWORD=...

# Seguridad
JWT_SECRET=...
JWT_ADMIN_SECRET=...

# Pagos
WOMPI_PUBLIC_KEY=...
WOMPI_PRIVATE_KEY=...
WOMPI_EVENTS_SECRET=...
MP_ACCESS_TOKEN=...
MP_WEBHOOK_SECRET=...

# Email
SMTP_HOST=smtp.hostinger.com
SMTP_PORT=465
SMTP_USER=pedidos@novainvesa.com
SMTP_PASSWORD=...

# Dropi
DROPI_EMAIL=...
DROPI_PASSWORD=...

# N8n
N8N_WEBHOOK_URL=...
N8N_SECRET=...

# Meta
META_PIXEL_ID=...
META_CAPI_TOKEN=...
```

---

## 9. Decisiones de arquitectura

| Decisión | Opción elegida | Alternativa descartada | Razón |
|---------|---------------|----------------------|-------|
| Frontend framework | Next.js 15 | React + Vite | SEO, rendimiento, Vercel |
| Backend language | Java Spring Boot | Node.js Express | Conocimiento del equipo, robustez |
| Frontend hosting | Vercel | Hostinger static | Gratis, optimizado para Next.js |
| ORM | JPA + Hibernate | JOOQ, JDBC puro | Estándar, productivo |
| Automatización Dropi | N8n | Integración directa | API Dropi no pública para tiendas propias |
| Estado frontend | Zustand | Redux, Context | Ligero, sin boilerplate |
| i18n | next-intl | i18next | Integración nativa con App Router |

---

## 10. Historial de cambios

| Versión | Fecha | Cambios |
|---------|-------|---------|
| 1.0 | Oct 2025 | React + Vite + Node.js + Express |
| 2.0 | Abr 2026 | Migración a Next.js + Spring Boot, Vercel, N8n |

