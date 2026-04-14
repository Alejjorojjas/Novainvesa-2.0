import { getRequestConfig } from 'next-intl/server'
import { defineRouting } from 'next-intl/routing'

export const locales = ['es', 'en', 'pt'] as const
export const defaultLocale = 'es' as const

export type Locale = (typeof locales)[number]

export const routing = defineRouting({
  locales,
  defaultLocale,
  localePrefix: 'always',
})

export default getRequestConfig(async ({ requestLocale }) => {
  // En next-intl v4, se usa requestLocale (Promise<string | undefined>)
  let locale = await requestLocale

  // Validar que el locale sea uno de los soportados
  if (!locale || !locales.includes(locale as Locale)) {
    locale = defaultLocale
  }

  return {
    locale,
    messages: (await import(`./messages/${locale}.json`)).default,
  }
})
