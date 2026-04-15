# Skill: novainvesa-context

Contexto completo del proyecto Novainvesa para orientar cualquier decisión técnica.

## Proyecto

**Novainvesa** — tienda dropshipping multi-proveedor con marca propia, dirigida a Latinoamérica.
Proveedor principal: **Dropi** (Colombia). Pagos: Wompi y MercadoPago.

## URLs de producción

- **Frontend:** https://www.novainvesa.com
- **Backend API:** https://api.novainvesa.com (Render.com)
- **Panel admin:** https://www.novainvesa.com/admin
- **N8n Cloud:** interno (no público)

## Stack

| Capa | Tecnología |
|------|------------|
| Frontend | Next.js 15, App Router, TypeScript, Tailwind CSS v4, Shadcn/UI (new-york, zinc) |
| Estado | Zustand |
| HTTP | Axios |
| i18n | next-intl (ES/EN/PT) |
| Backend | Spring Boot 3.5, Java 21, Maven |
| Base de datos | MySQL 8.0 (Hostinger) via Spring Data JPA |
| Autenticación | JWT — usuarios 7d, admins 24h, secretos separados |
| Automatización | N8n Cloud (Backend → N8n → Dropi) |
| Hosting FE | Hostinger `/public_html/` (static export) |
| Hosting BE | Render.com (free tier, 512MB RAM) |
| CI/CD | GitHub Actions |

## Estructura del monorepo

```
Novainvesa/
├── Frontend/        ← Next.js 15
├── Backend/         ← Spring Boot 3.5
├── docs/            ← documentación técnica
├── .claude/         ← agentes y skills de Claude Code
└── .github/         ← GitHub Actions workflows
```

## Locales soportados

- `es` — Español (por defecto, canonical)
- `en` — Inglés
- `pt` — Portugués

## Convenciones de código

- Commits: `tipo(scope): descripción en español` (máx 72 chars)
- Ramas: `feat/backend/nombre`, `feat/frontend/nombre`, `fix/...`
- Nunca a `main` directamente — siempre via `dev`
- API response: `{ "success": true, "data": {} }` o `{ "success": false, "error": { "code": "...", "message": "..." } }`
- Variables de entorno: nunca hardcodeadas, siempre desde `.env`

## Documentos clave

- `docs/PRD.md` — qué y por qué
- `docs/arquitectura.md` — decisiones técnicas
- `docs/api-contract.md` — todos los endpoints
- `docs/modelo-datos.md` — esquema MySQL
- `docs/reglas-negocio.md` — reglas de la tienda
- `docs/design-system.md` — UI tokens
- `docs/mvp.md` — scope del MVP
- `docs/system-design.md` — flujos del sistema
