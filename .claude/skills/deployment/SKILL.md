# Skill: deployment

Guía de deploy para Novainvesa: Frontend (Hostinger) + Backend (Render.com).

## Estrategia de ramas

```
main          ← producción (protegida, solo merge desde dev)
└── dev       ← integración (base para nuevas ramas)
    ├── feat/frontend/nombre-feature
    ├── feat/backend/nombre-feature
    ├── fix/frontend/nombre-bug
    └── fix/backend/nombre-bug
```

**Regla:** Nunca commit directo a `main`. Solo merge dev → main cuando todo está estable.

## GitHub Actions — Frontend

```yaml
# .github/workflows/deploy-frontend.yml
name: Deploy Frontend

on:
  push:
    branches: [main]
    paths: ['Frontend/**']

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'
          cache-dependency-path: Frontend/package-lock.json

      - name: Install dependencies
        working-directory: Frontend
        run: npm ci

      - name: Build
        working-directory: Frontend
        run: npm run build
        env:
          NEXT_PUBLIC_API_URL: ${{ secrets.NEXT_PUBLIC_API_URL }}
          NEXT_PUBLIC_MP_PUBLIC_KEY: ${{ secrets.NEXT_PUBLIC_MP_PUBLIC_KEY }}
          NEXT_PUBLIC_META_PIXEL_ID: ${{ secrets.NEXT_PUBLIC_META_PIXEL_ID }}
          NEXT_PUBLIC_GA4_ID: ${{ secrets.NEXT_PUBLIC_GA4_ID }}

      - name: Deploy to Hostinger via FTP
        uses: SamKirkland/FTP-Deploy-Action@v4.3.4
        with:
          server: ${{ secrets.FTP_SERVER }}
          username: ${{ secrets.FTP_USERNAME }}
          password: ${{ secrets.FTP_PASSWORD }}
          local-dir: ./Frontend/out/
          server-dir: /public_html/
```

## GitHub Actions — Backend

```yaml
# .github/workflows/deploy-backend.yml
name: Deploy Backend

on:
  push:
    branches: [main]
    paths: ['Backend/**']

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: 'maven'

      - name: Build JAR
        working-directory: Backend
        run: ./mvnw package -DskipTests -q

      - name: Deploy to Render
        run: |
          curl -X POST ${{ secrets.RENDER_DEPLOY_HOOK_URL }}
```

## Render.com — Variables de entorno

Configurar en: Dashboard → Service → Environment → Environment Variables

```
PORT=8080
DB_HOST=<mysql-hostinger-host>
DB_PORT=3306
DB_NAME=novainvesa_prod
DB_USERNAME=<user>
DB_PASSWORD=<pass>
JWT_SECRET=<64-chars-random>
JWT_ADMIN_SECRET=<64-chars-random-diferente>
JWT_EXPIRATION=604800000
JWT_ADMIN_EXPIRATION=86400000
FRONTEND_URL=https://www.novainvesa.com
SMTP_HOST=smtp.hostinger.com
SMTP_PORT=465
SMTP_USERNAME=noreply@novainvesa.com
SMTP_PASSWORD=<pass>
WOMPI_PUBLIC_KEY=<key>
WOMPI_PRIVATE_KEY=<key>
WOMPI_EVENTS_SECRET=<secret>
MP_ACCESS_TOKEN=<token>
MP_WEBHOOK_SECRET=<secret>
DROPI_INTEGRATION_KEY=<key>
N8N_WEBHOOK_URL=https://...
N8N_SECRET=<secret>
CHATEAPRO_API_KEY=<key>
```

## GitHub Secrets — Frontend

Configurar en: Repo → Settings → Secrets and variables → Actions

```
NEXT_PUBLIC_API_URL
NEXT_PUBLIC_MP_PUBLIC_KEY
NEXT_PUBLIC_META_PIXEL_ID
NEXT_PUBLIC_GA4_ID
FTP_SERVER
FTP_USERNAME
FTP_PASSWORD
```

## DNS — Hostinger

| Tipo | Nombre | Valor |
|------|--------|-------|
| A | @ | IP de Hostinger |
| CNAME | www | @ |
| CNAME | api | cname.render.com |

`api.novainvesa.com` → apunta a Render (custom domain configurado en Render)

## Next.js — Static Export

```js
// Frontend/next.config.ts
const nextConfig = {
  output: 'export',        // genera carpeta out/
  trailingSlash: true,     // compatibilidad con Hostinger
  images: {
    unoptimized: true,     // static export no soporta Image Optimization
  },
}
```

## Checklist de deploy

- [ ] Variables de entorno configuradas en ambas plataformas
- [ ] `ddl-auto=validate` en producción (no create/update)
- [ ] SQL script ejecutado en MySQL de Hostinger antes del primer deploy
- [ ] CORS apunta a `https://www.novainvesa.com` (con www)
- [ ] Dominio custom configurado en Render para `api.novainvesa.com`
- [ ] SSL activo en ambos dominios
- [ ] GitHub Actions workflows en `.github/workflows/`
- [ ] Rama `main` protegida (require PR + review)
