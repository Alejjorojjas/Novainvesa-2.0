// proxy.ts — Next.js 16 renombró middleware.ts a proxy.ts
// next-intl v4 sigue usando createMiddleware internamente
import createMiddleware from 'next-intl/middleware'
import { routing } from './i18n'

// Crear el handler de next-intl
const handleI18nRouting = createMiddleware(routing)

// Next.js 16 requiere exportar como función `proxy` (ya no `default` anónimo)
export function proxy(request: Parameters<typeof handleI18nRouting>[0]) {
  return handleI18nRouting(request)
}

export const config = {
  matcher: [
    // Aplicar a todas las rutas excepto archivos estáticos y _next
    '/((?!_next|_vercel|.*\\..*).*)',
  ],
}
