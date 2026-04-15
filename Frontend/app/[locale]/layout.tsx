import type { Metadata } from 'next'
import { Geist, Geist_Mono } from 'next/font/google'
import { NextIntlClientProvider } from 'next-intl'
import { getMessages, setRequestLocale } from 'next-intl/server'
import { Toaster } from 'sonner'
import { ThemeProvider } from '@/components/providers/ThemeProvider'
import { Navbar } from '@/components/layout/Navbar'
import { Footer } from '@/components/layout/Footer'
import { CartDrawer } from '@/components/layout/CartDrawer'
import { WhatsAppFloat } from '@/components/common/WhatsAppFloat'
import { locales, type Locale } from '@/i18n'
import '../globals.css'

const geistSans = Geist({
  variable: '--font-geist-sans',
  subsets: ['latin'],
  display: 'swap',
})

const geistMono = Geist_Mono({
  variable: '--font-geist-mono',
  subsets: ['latin'],
  display: 'swap',
})

interface Props {
  children: React.ReactNode
  params: Promise<{ locale: string }>
}

export function generateStaticParams() {
  return locales.map((locale) => ({ locale }))
}

export async function generateMetadata({
  params,
}: {
  params: Promise<{ locale: string }>
}): Promise<Metadata> {
  const { locale } = await params

  return {
    title: {
      default: 'Novainvesa — Tu tienda online',
      template: '%s — Novainvesa',
    },
    description:
      'Productos de calidad con envío gratis a toda Colombia. Electrónica, hogar, mascotas, fitness y más.',
    metadataBase: new URL('https://www.novainvesa.com'),
    openGraph: {
      siteName: 'Novainvesa',
      locale,
      type: 'website',
    },
    robots: { index: true, follow: true },
  }
}

export default async function LocaleLayout({ children, params }: Props) {
  const { locale } = await params

  // Habilitar renderizado estático para este locale (next-intl v4)
  setRequestLocale(locale as Locale)

  const messages = await getMessages()

  return (
    <html
      lang={locale}
      className={`dark ${geistSans.variable} ${geistMono.variable}`}
      suppressHydrationWarning
    >
      <body className="min-h-full flex flex-col antialiased bg-[#0A0A0A] text-white">
        <ThemeProvider
          attribute="class"
          defaultTheme="dark"
          enableSystem={false}
          disableTransitionOnChange
        >
          <NextIntlClientProvider messages={messages} locale={locale}>

            {/* Navbar fijo */}
            <Navbar locale={locale} />

            {/* Cart Drawer — global */}
            <CartDrawer locale={locale} />

            {/* Contenido principal */}
            <main id="main-content" className="flex-1">
              {children}
            </main>

            {/* Footer */}
            <Footer locale={locale} />

            {/* WhatsApp flotante */}
            <WhatsAppFloat />

            {/* Toasts */}
            <Toaster
              richColors
              theme="dark"
              position="bottom-right"
              offset={80}
              toastOptions={{
                style: {
                  background: '#18181B',
                  border: '1px solid #27272A',
                  color: '#FAFAFA',
                },
              }}
            />
          </NextIntlClientProvider>
        </ThemeProvider>
      </body>
    </html>
  )
}
