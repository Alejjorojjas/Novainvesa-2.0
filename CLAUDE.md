# CLAUDE.md — Novainvesa

Este archivo configura el comportamiento de Claude Code para el proyecto Novainvesa.
Lee este archivo al inicio de cada sesión antes de hacer cualquier cosa.

---

## Idioma y comunicación

- Responde **siempre en español**
- Tutea al usuario en todo momento
- Sé directo y conciso — no expliques lo que ya es obvio
- Cuando termines una tarea, resume en máximo 5 líneas qué hiciste
- Si encuentras un problema, explícalo claramente antes de proponer solución

---

## Contexto del proyecto

**Novainvesa** es una tienda dropshipping multi-proveedor con marca propia
dirigida a toda Latinoamérica. El proveedor principal es Dropi (Colombia).

Antes de implementar cualquier funcionalidad, lee los documentos relevantes en `docs/`:
- `docs/PRD.md` — qué construimos y por qué
- `docs/arquitectura.md` — stack y decisiones técnicas
- `docs/system-design.md` — flujos del sistema
- `docs/api-contract.md` — contrato de la API
- `docs/modelo-datos.md` — esquema de la base de datos
- `docs/design-system.md` — colores, tipografía, componentes
- `docs/reglas-negocio.md` — reglas de la tienda
- `docs/mvp.md` — scope del MVP

---

## Stack tecnológico

### Frontend
- **Framework:** Next.js 15 (App Router, static export)
- **UI:** Tailwind CSS + Shadcn/UI
- **Estado:** Zustand
- **HTTP:** Axios
- **i18n:** next-intl (ES/EN/PT)
- **Hosting:** Hostinger `/public_html/`
- **Carpeta:** `Frontend/`

### Backend
- **Framework:** Java Spring Boot 3
- **Base de datos:** MySQL (Hostinger) via Spring Data JPA
- **Autenticación:** JWT (Spring Security)
- **Hosting:** Render.com
- **Carpeta:** `Backend/`

### Automatización
- **N8n Cloud** — automatiza pedidos hacia Dropi
- Webhook: Backend → N8n → Dropi

### Infraestructura
- **CI/CD:** GitHub Actions
- **Repositorio:** github.com/Alejjorojjas/Novainvesa (privado, monorepo)

---

## Estrategia de ramas

```
main          ← producción (protegida, solo merge desde dev)
└── dev       ← integración (base para nuevas ramas)
    ├── feat/frontend/nombre-feature
    ├── feat/backend/nombre-feature
    ├── fix/frontend/nombre-bug
    └── fix/backend/nombre-bug
```

### Reglas de ramas
- **Nunca** hacer commits directos a `main`
- Siempre crear rama desde `dev`
- Nombrar ramas en español con kebab-case: `feat/backend/autenticacion-jwt`
- Hacer merge a `dev` cuando la feature esté completa y probada
- Solo mergear `dev` → `main` cuando todo esté estable

---

## Convenciones de commits

**Formato:** `tipo(scope): descripción en español, imperativo, máx 72 chars`

### Tipos permitidos
| Tipo | Cuándo usarlo |
|------|---------------|
| `feat` | Nueva funcionalidad |
| `fix` | Corrección de bug |
| `docs` | Cambios en documentación |
| `chore` | Dependencias, config, tareas de mantenimiento |
| `refactor` | Refactorización sin cambio funcional |
| `test` | Agregar o corregir tests |
| `style` | Cambios de formato/estilo sin lógica |
| `config` | Configuración de entorno, CI/CD, herramientas |
| `security` | Cambios relacionados con seguridad |
| `perf` | Mejoras de rendimiento |

### Scopes
- `frontend` — cambios en `Frontend/`
- `backend` — cambios en `Backend/`
- `docs` — cambios en `docs/`
- `ci` — cambios en `.github/`
- `config` — cambios en configuración raíz

### Ejemplos correctos
```
feat(frontend): agregar página de carrito con persistencia
fix(backend): corregir validación de stock en endpoint de pedidos
docs(docs): actualizar contrato de API con endpoint de pagos
chore(backend): actualizar dependencias de Spring Boot a 3.2
config(ci): agregar workflow de deploy para rama frontend
security(backend): agregar rate limiting en endpoints de autenticación
```

---

## Auto-push después de commits

Después de cada `git commit`, ejecutar automáticamente:
```bash
git push origin $(git branch --show-current)
```

Si el push falla, reportar el error sin intentar forzar.

---

## Auto-actualización de documentación

Cuando implementes o modifiques algo significativo:
1. Identifica qué documento de `docs/` corresponde al cambio
2. Actualiza ese documento para reflejar el estado actual
3. Haz commit separado del código: `docs(docs): actualizar [nombre-doc] con [cambio]`

Ejemplos:
- Nuevo endpoint → actualizar `docs/api-contract.md`
- Nueva tabla → actualizar `docs/modelo-datos.md`
- Cambio de arquitectura → actualizar `docs/arquitectura.md`
- Nueva regla de negocio → actualizar `docs/reglas-negocio.md`

---

## Cuándo invocar cada agente

Claude Code invoca agentes automáticamente según la tarea:

### Agentes principales

| Tarea | Agente |
|-------|--------|
| Decisión de arquitectura, nuevo módulo, cambio estructural | `arquitecto-novainvesa` |
| Endpoint, servicio, repositorio Java, query SQL | `backend-novainvesa` |
| Componente React, página Next.js, estilo, i18n | `frontend-novainvesa` |
| Probar funcionalidad, buscar bugs, validar flujo | `qa-novainvesa` |
| GitHub Actions, Dockerfile, deploy, variables de entorno | `devops-novainvesa` |
| Revisión de seguridad (solo cuando se indique) | `seguridad-novainvesa` |

### Subagentes especializados

| Tarea | Subagente |
|-------|-----------|
| Integración con Dropi API, N8n, importación de productos | `dropi-integration` |
| Wompi, MercadoPago, verificación de webhooks HMAC | `payments-integration` |
| Emails SMTP, WhatsApp via Chatea Pro | `notifications` |
| Importador de productos Dropi (UI admin + backend) | `product-importer` |
| generateMetadata, sitemap, robots.txt, JSON-LD | `seo-metadata` |

### Skills disponibles (contexto automático)

| Skill | Cuándo se aplica |
|-------|-----------------|
| `novainvesa-context` | Contexto general del proyecto, URLs, stack, convenciones |
| `spring-boot-patterns` | Cualquier tarea de backend Java |
| `nextjs-patterns` | Cualquier tarea de frontend Next.js |
| `dropi-api` | Integración con Dropi o N8n |
| `n8n-workflows` | Flujos de automatización N8n |
| `seguridad-web` | Seguridad, JWT, HMAC, CORS, rate limiting |
| `deployment` | Deploy, CI/CD, GitHub Actions, DNS |
| `design-tokens` | Componentes UI, colores, tipografía, Shadcn |

---

## Estructura de carpetas

```
Novainvesa/                      ← raíz del monorepo
├── CLAUDE.md                    ← este archivo
├── .claude/
│   ├── agents/                  ← agentes Claude Code
│   └── skills/                  ← skills reutilizables
├── docs/                        ← documentación técnica
│   ├── PRD.md
│   ├── arquitectura.md
│   ├── system-design.md
│   ├── mvp.md
│   ├── api-contract.md
│   ├── modelo-datos.md
│   ├── design-system.md
│   └── reglas-negocio.md
├── Frontend/                    ← Next.js 15
│   ├── app/
│   ├── components/
│   ├── lib/
│   └── public/
├── Backend/                     ← Spring Boot 3
│   ├── src/main/java/
│   └── src/main/resources/
└── .github/
    └── workflows/
        ├── deploy-frontend.yml
        └── deploy-backend.yml
```

---

## Reglas de desarrollo

1. **Nunca** hardcodear credenciales — siempre usar variables de entorno
2. **Nunca** modificar `main` directamente
3. Cada feature va en su propia rama
4. Actualizar `docs/` cuando cambie algo significativo
5. Los errores del sistema deben loggearse, no silenciarse
6. Toda respuesta de la API sigue el formato estándar:
   ```json
   { "success": true, "data": {} }
   { "success": false, "error": { "code": "...", "message": "..." } }
   ```
7. Los commits de documentación van separados de los commits de código

---

## Variables de entorno

**Nunca** subir archivos `.env` al repositorio.
Variables en:
- **Frontend:** GitHub Secrets → inyectadas en el build de GitHub Actions
- **Backend:** Render.com → Environment Variables
- **Local:** archivos `.env` en `Frontend/` y `Backend/` (en `.gitignore`)

