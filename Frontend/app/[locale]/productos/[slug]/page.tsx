export const dynamic = 'force-dynamic'

import { notFound } from 'next/navigation'
import { setRequestLocale } from 'next-intl/server'
import type { Metadata } from 'next'
import { type Locale } from '@/i18n'
import { ProductDetailClient } from '@/components/product/ProductDetailClient'
import type { Product } from '@/components/product/ProductCard'

// ─── Tipos ────────────────────────────────────────────────────────────────────

interface ApiResponse<T> {
  success: boolean
  data: T
  error?: { code: string; message: string }
}

interface Props {
  params: Promise<{ locale: string; slug: string }>
}

const API_URL = process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:8080'

// ─── Fetch helpers ────────────────────────────────────────────────────────────

async function getProduct(slug: string): Promise<Product | null> {
  try {
    const res = await fetch(`${API_URL}/api/v1/products/${slug}`, {
      next: { revalidate: 300 },
    })
    if (!res.ok) return null
    const json: ApiResponse<Product> = await res.json()
    return json.success ? json.data : null
  } catch {
    return null
  }
}

async function getRelatedProducts(slug: string): Promise<Product[]> {
  try {
    const res = await fetch(`${API_URL}/api/v1/products/${slug}/related`, {
      next: { revalidate: 300 },
    })
    if (!res.ok) return []
    const json: ApiResponse<Product[]> = await res.json()
    return json.success ? json.data : []
  } catch {
    return []
  }
}

// ─── Metadata ─────────────────────────────────────────────────────────────────

export async function generateMetadata({ params }: Props): Promise<Metadata> {
  const { slug } = await params
  const product = await getProduct(slug)

  if (!product) {
    return {
      title: 'Producto no encontrado',
      description: 'El producto que buscas no existe o fue eliminado.',
    }
  }

  const mainImage = product.images?.[0]
  const price = new Intl.NumberFormat('es-CO', {
    style: 'currency',
    currency: 'COP',
    maximumFractionDigits: 0,
  }).format(product.price)

  return {
    title: `${product.name} — ${price}`,
    description: product.description
      ? product.description.slice(0, 160)
      : `Compra ${product.name} en Novainvesa. Envío gratis a toda Colombia.`,
    openGraph: {
      title: product.name,
      description: product.description?.slice(0, 160) ?? `Compra ${product.name} en Novainvesa`,
      images: mainImage ? [{ url: mainImage, width: 800, height: 800 }] : [],
      type: 'website',
    },
    twitter: {
      card: 'summary_large_image',
      title: product.name,
      description: product.description?.slice(0, 160),
      images: mainImage ? [mainImage] : [],
    },
  }
}

// ─── Página ───────────────────────────────────────────────────────────────────

export default async function ProductoDetallePage({ params }: Props) {
  const { locale, slug } = await params
  setRequestLocale(locale as Locale)

  const [product, relatedProducts] = await Promise.all([
    getProduct(slug),
    getRelatedProducts(slug),
  ])

  if (!product) {
    notFound()
  }

  return (
    <ProductDetailClient
      product={product}
      relatedProducts={relatedProducts}
      locale={locale}
    />
  )
}
