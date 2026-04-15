import { setRequestLocale } from 'next-intl/server'
import { useTranslations } from 'next-intl'
import { type Locale } from '@/i18n'

interface Props {
  params: Promise<{ locale: string }>
}

export default async function HomePage({ params }: Props) {
  const { locale } = await params
  setRequestLocale(locale as Locale)

  return <HomeContent />
}

// Componente cliente separado para usar useTranslations
function HomeContent() {
  const t = useTranslations('home.hero')

  return (
    <main className="min-h-screen bg-[#0A0A0A]">
      <div className="container mx-auto px-4 py-16 text-center">
        {/* Badge */}
        <span className="inline-block mb-6 px-4 py-1.5 rounded-full bg-amber-500/10 text-amber-400 text-sm font-medium border border-amber-500/20">
          Envíos a toda Colombia
        </span>

        {/* Título */}
        <h1 className="text-4xl md:text-5xl font-bold text-white mb-4">
          {t('title')}
        </h1>
        <p className="text-neutral-400 text-lg mb-8">{t('subtitle')}</p>

        {/* CTA */}
        <a
          href="#productos"
          className="inline-flex items-center gap-2 px-8 py-3 rounded-full bg-amber-500 hover:bg-amber-600 text-white font-semibold text-base transition-all hover:scale-105 hover:shadow-lg"
        >
          {t('cta')}
        </a>
      </div>
    </main>
  )
}
