import { setRequestLocale } from 'next-intl/server'
import { getTranslations } from 'next-intl/server'
import Link from 'next/link'
import { ArrowRight, Package, ShieldCheck, Headphones, RotateCcw, Star, Zap } from 'lucide-react'
import { type Locale } from '@/i18n'
import { ProductGrid } from '@/components/product/ProductGrid'
import type { Product } from '@/components/product/ProductCard'

// ─── Tipos ───────────────────────────────────────────────────────────────────

interface Category {
  id: number
  name: string
  slug: string
  description?: string
  icon?: string
  color?: string
  productCount?: number
}

interface ApiResponse<T> {
  success: boolean
  data: T
  error?: { code: string; message: string }
}

interface PagedResponse<T> {
  content: T[]
  totalPages: number
  totalElements: number
  number: number
}

interface Props {
  params: Promise<{ locale: string }>
}

// ─── Fetch server-side ────────────────────────────────────────────────────────

const API_URL = process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:8080'

async function getFeaturedProducts(): Promise<Product[]> {
  try {
    const res = await fetch(
      `${API_URL}/api/v1/products?page=0&size=4&sortBy=createdAt&sortDir=desc`,
      { next: { revalidate: 300 } }
    )
    if (!res.ok) return []
    const json: ApiResponse<PagedResponse<Product>> = await res.json()
    return json.success ? json.data.content.filter((p) => p.isFeatured).slice(0, 4) : []
  } catch {
    return []
  }
}

async function getNewArrivals(): Promise<Product[]> {
  try {
    const res = await fetch(
      `${API_URL}/api/v1/products?page=0&size=8&sortBy=createdAt&sortDir=desc`,
      { next: { revalidate: 300 } }
    )
    if (!res.ok) return []
    const json: ApiResponse<PagedResponse<Product>> = await res.json()
    return json.success ? json.data.content.slice(0, 8) : []
  } catch {
    return []
  }
}

async function getCategories(): Promise<Category[]> {
  try {
    const res = await fetch(`${API_URL}/api/v1/categories`, {
      next: { revalidate: 600 },
    })
    if (!res.ok) return []
    const json: ApiResponse<Category[]> = await res.json()
    return json.success ? json.data : []
  } catch {
    return []
  }
}

// Categorías de fallback si el backend no responde
const FALLBACK_CATEGORIES: Category[] = [
  { id: 1, name: 'Mascotas', slug: 'mascotas', icon: '🐾', color: 'amber' },
  { id: 2, name: 'Hogar', slug: 'hogar', icon: '🏠', color: 'blue' },
  { id: 3, name: 'Tecnología', slug: 'tecnologia', icon: '📱', color: 'violet' },
  { id: 4, name: 'Belleza', slug: 'belleza', icon: '✨', color: 'pink' },
  { id: 5, name: 'Fitness', slug: 'fitness', icon: '💪', color: 'green' },
]

const CATEGORY_GRADIENTS: Record<string, string> = {
  amber: 'from-amber-500/20 to-amber-500/5 border-amber-500/20 hover:border-amber-500/50',
  blue: 'from-blue-600/20 to-blue-600/5 border-blue-600/20 hover:border-blue-600/50',
  violet: 'from-violet-600/20 to-violet-600/5 border-violet-600/20 hover:border-violet-600/50',
  pink: 'from-pink-500/20 to-pink-500/5 border-pink-500/20 hover:border-pink-500/50',
  green: 'from-green-500/20 to-green-500/5 border-green-500/20 hover:border-green-500/50',
  default: 'from-neutral-700/20 to-neutral-700/5 border-neutral-700/20 hover:border-neutral-700/50',
}

// ─── Página ───────────────────────────────────────────────────────────────────

export default async function HomePage({ params }: Props) {
  const { locale } = await params
  setRequestLocale(locale as Locale)

  const t = await getTranslations('home')
  const tCommon = await getTranslations('common')

  const [featuredProducts, newArrivals, categories] = await Promise.all([
    getFeaturedProducts(),
    getNewArrivals(),
    getCategories(),
  ])

  const displayCategories = categories.length > 0 ? categories : FALLBACK_CATEGORIES

  return (
    <div className="min-h-screen bg-[#0A0A0A]">

      {/* ═══ HERO ═══════════════════════════════════════════════════════════ */}
      <section className="relative overflow-hidden pt-6 pb-20 sm:pt-10 sm:pb-28">
        {/* Fondo decorativo */}
        <div className="absolute inset-0 pointer-events-none">
          <div className="absolute top-0 left-1/4 w-[600px] h-[600px] bg-blue-600/10 rounded-full blur-3xl -translate-y-1/2" />
          <div className="absolute top-1/2 right-0 w-[400px] h-[400px] bg-amber-500/8 rounded-full blur-3xl" />
          <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_center,rgba(37,99,235,0.05)_0%,transparent_70%)]" />
        </div>

        <div className="relative max-w-7xl mx-auto px-4 sm:px-6 text-center">
          {/* Badge */}
          <div className="inline-flex items-center gap-2 mb-8 px-4 py-1.5 rounded-full
            bg-amber-500/10 text-amber-400 text-sm font-medium border border-amber-500/20
            backdrop-blur-sm">
            <Zap size={14} className="shrink-0" />
            {t('hero.badge')}
          </div>

          {/* Título */}
          <h1 className="text-4xl sm:text-5xl md:text-6xl lg:text-7xl font-bold tracking-tight text-white leading-[1.1] mb-6">
            {t('hero.title')}
            <br />
            <span className="bg-gradient-to-r from-blue-400 via-blue-500 to-violet-500 bg-clip-text text-transparent">
              {t('hero.titleAccent')}
            </span>
          </h1>

          <p className="text-neutral-400 text-lg sm:text-xl max-w-2xl mx-auto mb-10 leading-relaxed">
            {t('hero.subtitle')}
          </p>

          {/* CTAs */}
          <div className="flex flex-col sm:flex-row items-center justify-center gap-4 mb-14">
            <Link
              href={`/${locale}/productos`}
              className="flex items-center gap-2 px-8 py-3.5 rounded-full bg-blue-600
                hover:bg-blue-700 text-white font-semibold text-base
                transition-all duration-200 hover:scale-105
                hover:shadow-[0_0_30px_rgba(37,99,235,0.4)] group"
            >
              {t('hero.cta')}
              <ArrowRight size={16} className="transition-transform group-hover:translate-x-1" />
            </Link>
            <Link
              href={`/${locale}/productos`}
              className="flex items-center gap-2 px-8 py-3.5 rounded-full
                bg-white/5 hover:bg-white/10 text-white font-semibold text-base
                border border-neutral-700 hover:border-neutral-600
                transition-all duration-200"
            >
              {t('hero.ctaSecondary')}
            </Link>
          </div>

          {/* Social proof */}
          <div className="flex flex-col sm:flex-row items-center justify-center gap-6 text-sm text-neutral-500">
            <div className="flex items-center gap-2">
              <div className="flex -space-x-2">
                {['bg-blue-500', 'bg-violet-500', 'bg-amber-500', 'bg-green-500'].map((color, i) => (
                  <div
                    key={i}
                    className={`w-8 h-8 rounded-full ${color} border-2 border-[#0A0A0A] flex items-center justify-center`}
                  >
                    <span className="text-white text-xs font-bold">
                      {['A', 'M', 'C', 'P'][i]}
                    </span>
                  </div>
                ))}
              </div>
              <span>{t('hero.socialProof')}</span>
            </div>
            <div className="flex items-center gap-1.5">
              <div className="flex gap-0.5">
                {[1, 2, 3, 4, 5].map((i) => (
                  <Star key={i} size={14} className="text-amber-400 fill-amber-400" />
                ))}
              </div>
              <span className="font-semibold text-neutral-300">{t('hero.rating')}</span>
            </div>
          </div>
        </div>
      </section>

      {/* ═══ FEATURES BAR ════════════════════════════════════════════════════ */}
      <section className="border-t border-neutral-800 bg-neutral-900/50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 py-8">
          <div className="grid grid-cols-2 lg:grid-cols-4 gap-6">
            {[
              {
                icon: <Package size={20} className="text-blue-400" />,
                title: t('features.shipping.title'),
                desc: t('features.shipping.desc'),
                bg: 'bg-blue-600/10',
              },
              {
                icon: <ShieldCheck size={20} className="text-green-400" />,
                title: t('features.secure.title'),
                desc: t('features.secure.desc'),
                bg: 'bg-green-500/10',
              },
              {
                icon: <RotateCcw size={20} className="text-amber-400" />,
                title: t('features.guarantee.title'),
                desc: t('features.guarantee.desc'),
                bg: 'bg-amber-500/10',
              },
              {
                icon: <Headphones size={20} className="text-violet-400" />,
                title: t('features.support.title'),
                desc: t('features.support.desc'),
                bg: 'bg-violet-600/10',
              },
            ].map((feature, i) => (
              <div key={i} className="flex items-center gap-4">
                <div className={`w-10 h-10 rounded-xl ${feature.bg} flex items-center justify-center shrink-0`}>
                  {feature.icon}
                </div>
                <div>
                  <p className="text-sm font-semibold text-neutral-100">{feature.title}</p>
                  <p className="text-xs text-neutral-500 mt-0.5">{feature.desc}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ═══ CATEGORÍAS ══════════════════════════════════════════════════════ */}
      <section className="py-16 border-t border-neutral-800">
        <div className="max-w-7xl mx-auto px-4 sm:px-6">
          <div className="flex items-end justify-between mb-10">
            <div>
              <p className="text-xs font-semibold text-blue-400 uppercase tracking-widest mb-2">
                Explorar
              </p>
              <h2 className="text-2xl sm:text-3xl font-bold text-white">{t('categories')}</h2>
            </div>
            <Link
              href={`/${locale}/productos`}
              className="text-sm text-neutral-400 hover:text-white transition-colors flex items-center gap-1 group"
            >
              {tCommon('seeAll')}
              <ArrowRight size={14} className="transition-transform group-hover:translate-x-0.5" />
            </Link>
          </div>

          <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-4">
            {displayCategories.map((cat) => {
              const gradient = CATEGORY_GRADIENTS[cat.color ?? 'default']
              return (
                <Link
                  key={cat.id}
                  href={`/${locale}/productos?category=${cat.slug}`}
                  className={`group relative bg-gradient-to-br ${gradient}
                    border rounded-2xl p-5 flex flex-col items-center text-center gap-3
                    transition-all duration-300 hover:-translate-y-1
                    hover:shadow-[0_8px_25px_rgba(0,0,0,0.3)]`}
                >
                  <span className="text-3xl">{cat.icon ?? '📦'}</span>
                  <div>
                    <p className="text-sm font-semibold text-neutral-100 group-hover:text-white transition-colors">
                      {cat.name}
                    </p>
                    {cat.productCount !== undefined && (
                      <p className="text-xs text-neutral-500 mt-0.5">
                        {cat.productCount} productos
                      </p>
                    )}
                  </div>
                </Link>
              )
            })}
          </div>
        </div>
      </section>

      {/* ═══ PRODUCTOS DESTACADOS ════════════════════════════════════════════ */}
      <section className="py-16 border-t border-neutral-800">
        <div className="max-w-7xl mx-auto px-4 sm:px-6">
          <div className="flex items-end justify-between mb-10">
            <div>
              <p className="text-xs font-semibold text-amber-400 uppercase tracking-widest mb-2">
                Lo mejor
              </p>
              <h2 className="text-2xl sm:text-3xl font-bold text-white">{t('featured')}</h2>
            </div>
            <Link
              href={`/${locale}/productos`}
              className="text-sm text-neutral-400 hover:text-white transition-colors flex items-center gap-1 group"
            >
              {tCommon('seeAll')}
              <ArrowRight size={14} className="transition-transform group-hover:translate-x-0.5" />
            </Link>
          </div>

          {featuredProducts.length > 0 ? (
            <ProductGrid
              products={featuredProducts}
              locale={locale}
              columns={4}
            />
          ) : (
            <div className="text-center py-12 text-neutral-600 text-sm">
              Productos en camino...
            </div>
          )}
        </div>
      </section>

      {/* ═══ NOVEDADES ════════════════════════════════════════════════════════ */}
      <section className="py-16 border-t border-neutral-800">
        <div className="max-w-7xl mx-auto px-4 sm:px-6">
          <div className="flex items-end justify-between mb-10">
            <div>
              <p className="text-xs font-semibold text-green-400 uppercase tracking-widest mb-2">
                Recién llegados
              </p>
              <h2 className="text-2xl sm:text-3xl font-bold text-white">{t('newArrivals')}</h2>
            </div>
            <Link
              href={`/${locale}/productos?sortBy=createdAt&sortDir=desc`}
              className="text-sm text-neutral-400 hover:text-white transition-colors flex items-center gap-1 group"
            >
              {tCommon('seeAll')}
              <ArrowRight size={14} className="transition-transform group-hover:translate-x-0.5" />
            </Link>
          </div>

          {newArrivals.length > 0 ? (
            <ProductGrid
              products={newArrivals}
              locale={locale}
              columns={4}
            />
          ) : (
            <div className="text-center py-12 text-neutral-600 text-sm">
              Productos en camino...
            </div>
          )}
        </div>
      </section>

      {/* ═══ CTA BANNER ══════════════════════════════════════════════════════ */}
      <section className="py-16 border-t border-neutral-800">
        <div className="max-w-7xl mx-auto px-4 sm:px-6">
          <div
            className="relative overflow-hidden rounded-3xl bg-gradient-to-br
              from-blue-600/25 via-blue-600/10 to-transparent
              border border-blue-600/20 p-10 sm:p-16 text-center"
          >
            {/* Fondo decorativo */}
            <div className="absolute top-0 right-0 w-72 h-72 bg-blue-600/15 rounded-full blur-3xl pointer-events-none" />
            <div className="absolute bottom-0 left-0 w-48 h-48 bg-violet-600/10 rounded-full blur-3xl pointer-events-none" />

            <div className="relative">
              <span className="inline-flex items-center gap-2 bg-blue-600/20 text-blue-300
                border border-blue-600/30 rounded-full px-4 py-1 text-sm font-medium mb-6">
                <Zap size={13} />
                Envíos a toda Colombia
              </span>

              <h2 className="text-3xl sm:text-4xl md:text-5xl font-bold text-white mb-4 leading-tight">
                ¿Listo para descubrir{' '}
                <span className="bg-gradient-to-r from-blue-400 to-violet-400 bg-clip-text text-transparent">
                  productos increíbles?
                </span>
              </h2>

              <p className="text-neutral-400 text-lg max-w-xl mx-auto mb-10 leading-relaxed">
                Explora nuestro catálogo completo. Calidad garantizada, entrega rápida y soporte 24/7.
              </p>

              <div className="flex flex-col sm:flex-row items-center justify-center gap-4">
                <Link
                  href={`/${locale}/productos`}
                  className="flex items-center gap-2 px-8 py-4 rounded-full bg-blue-600
                    hover:bg-blue-700 text-white font-bold text-base
                    transition-all duration-200 hover:scale-105
                    hover:shadow-[0_0_40px_rgba(37,99,235,0.5)] group"
                >
                  Ver catálogo completo
                  <ArrowRight size={16} className="transition-transform group-hover:translate-x-1" />
                </Link>
                <a
                  href="https://wa.me/573001234567"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="flex items-center gap-2 px-8 py-4 rounded-full
                    bg-green-600/20 hover:bg-green-600/30 text-green-400
                    border border-green-600/30 font-semibold text-base
                    transition-all duration-200"
                >
                  💬 Hablar por WhatsApp
                </a>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>
  )
}
