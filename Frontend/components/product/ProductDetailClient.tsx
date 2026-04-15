'use client'

import { useState } from 'react'
import Image from 'next/image'
import Link from 'next/link'
import { ShoppingCart, Zap, CheckCircle2, XCircle, ChevronRight, Minus, Plus, Share2, Star } from 'lucide-react'
import { toast } from 'sonner'
import { useCartStore } from '@/lib/store/cartStore'
import { useUIStore } from '@/lib/store/uiStore'
import { ProductGrid } from '@/components/product/ProductGrid'
import { formatPrice, calcDiscount, cn } from '@/lib/utils'
import type { Product } from '@/components/product/ProductCard'

interface ProductDetailClientProps {
  product: Product
  relatedProducts: Product[]
  locale: string
}

export function ProductDetailClient({ product, relatedProducts, locale }: ProductDetailClientProps) {
  const [selectedImage, setSelectedImage] = useState(0)
  const [quantity, setQuantity] = useState(1)
  const [activeTab, setActiveTab] = useState<'descripcion' | 'beneficios'>('descripcion')

  const addItem = useCartStore((s) => s.addItem)
  const openCart = useUIStore((s) => s.openCart)

  const images = product.images?.length ? product.images : ['https://via.placeholder.com/600x600']
  const discount = calcDiscount(product.price, product.compareAtPrice ?? null)

  const handleAddToCart = () => {
    if (!product.inStock) return
    addItem({
      productId: product.id,
      slug: product.slug,
      name: product.name,
      price: product.price,
      image: images[0],
      quantity,
    })
    toast.success('Agregado al carrito', {
      description: `${quantity}x ${product.name}`,
      action: { label: 'Ver carrito', onClick: openCart },
    })
  }

  const handleBuyNow = () => {
    if (!product.inStock) return
    addItem({
      productId: product.id,
      slug: product.slug,
      name: product.name,
      price: product.price,
      image: images[0],
      quantity,
    })
    window.location.href = `/${locale}/checkout`
  }

  const handleShare = async () => {
    try {
      await navigator.share({
        title: product.name,
        url: window.location.href,
      })
    } catch {
      await navigator.clipboard.writeText(window.location.href)
      toast.success('Enlace copiado al portapapeles')
    }
  }

  return (
    <div className="min-h-screen bg-[#0A0A0A]">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 py-8">

        {/* ── Breadcrumb ── */}
        <nav className="flex items-center gap-1.5 text-sm text-neutral-500 mb-8 flex-wrap">
          <Link href={`/${locale}`} className="hover:text-white transition-colors">
            Inicio
          </Link>
          <ChevronRight size={13} className="shrink-0" />
          <Link
            href={`/${locale}/productos?category=${product.categorySlug}`}
            className="hover:text-white transition-colors"
          >
            {product.categoryName}
          </Link>
          <ChevronRight size={13} className="shrink-0" />
          <span className="text-neutral-300 line-clamp-1 max-w-[200px]">{product.name}</span>
        </nav>

        {/* ── Contenido principal ── */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-10 xl:gap-16 mb-16">

          {/* ── Galería ── */}
          <div className="space-y-3">
            {/* Imagen principal */}
            <div className="relative aspect-square bg-neutral-900 rounded-2xl overflow-hidden
              border border-neutral-800 group">
              <Image
                src={images[selectedImage]}
                alt={product.name}
                fill
                sizes="(max-width: 1024px) 100vw, 50vw"
                className="object-cover transition-transform duration-500 group-hover:scale-105"
                priority
              />
              {discount && (
                <div className="absolute top-4 left-4">
                  <span className="bg-amber-500 text-black text-sm font-bold px-3 py-1 rounded-full">
                    -{discount}% OFF
                  </span>
                </div>
              )}
            </div>

            {/* Thumbnails */}
            {images.length > 1 && (
              <div className="flex gap-2 overflow-x-auto pb-1">
                {images.map((img, i) => (
                  <button
                    key={i}
                    onClick={() => setSelectedImage(i)}
                    className={cn(
                      'relative w-20 h-20 rounded-xl overflow-hidden shrink-0 border-2 transition-all duration-200',
                      selectedImage === i
                        ? 'border-blue-600 ring-2 ring-blue-600/30'
                        : 'border-neutral-800 hover:border-neutral-600'
                    )}
                  >
                    <Image
                      src={img}
                      alt={`${product.name} ${i + 1}`}
                      fill
                      sizes="80px"
                      className="object-cover"
                    />
                  </button>
                ))}
              </div>
            )}
          </div>

          {/* ── Info del producto ── */}
          <div className="flex flex-col gap-5">
            {/* Categoría + share */}
            <div className="flex items-center justify-between">
              <Link
                href={`/${locale}/productos?category=${product.categorySlug}`}
                className="text-xs font-semibold text-blue-400 uppercase tracking-widest
                  hover:text-blue-300 transition-colors"
              >
                {product.categoryName}
              </Link>
              <button
                onClick={handleShare}
                className="p-2 text-neutral-500 hover:text-white rounded-full
                  hover:bg-white/5 transition-all"
                aria-label="Compartir"
              >
                <Share2 size={16} />
              </button>
            </div>

            {/* Nombre */}
            <h1 className="text-2xl sm:text-3xl font-bold text-white leading-snug">
              {product.name}
            </h1>

            {/* Rating */}
            {product.rating && (
              <div className="flex items-center gap-2">
                <div className="flex gap-0.5">
                  {Array.from({ length: 5 }).map((_, i) => (
                    <Star
                      key={i}
                      size={15}
                      className={i < Math.round(product.rating!) ? 'text-amber-400 fill-amber-400' : 'text-neutral-700'}
                    />
                  ))}
                </div>
                <span className="text-sm text-neutral-400">
                  {product.rating.toFixed(1)}
                  {product.reviewCount && ` (${product.reviewCount} reseñas)`}
                </span>
              </div>
            )}

            {/* Precio */}
            <div className="flex items-baseline gap-3">
              <span className="text-3xl font-bold text-white">
                {formatPrice(product.price)}
              </span>
              {product.compareAtPrice && product.compareAtPrice > product.price && (
                <>
                  <span className="text-lg text-neutral-500 line-through">
                    {formatPrice(product.compareAtPrice)}
                  </span>
                  {discount && (
                    <span className="bg-amber-500/15 text-amber-400 text-sm font-semibold
                      px-2 py-0.5 rounded-full border border-amber-500/20">
                      Ahorra {discount}%
                    </span>
                  )}
                </>
              )}
            </div>

            {/* Stock badge */}
            <div className={cn(
              'inline-flex items-center gap-2 px-3 py-1.5 rounded-full text-sm font-medium w-fit',
              product.inStock
                ? 'bg-green-500/10 text-green-400 border border-green-500/20'
                : 'bg-red-500/10 text-red-400 border border-red-500/20'
            )}>
              {product.inStock
                ? <CheckCircle2 size={14} />
                : <XCircle size={14} />
              }
              {product.inStock
                ? `En stock${product.stockQuantity > 0 ? ` (${product.stockQuantity} disponibles)` : ''}`
                : 'Agotado'
              }
            </div>

            {/* Envío gratis */}
            <div className="flex items-center gap-2 text-sm text-neutral-400">
              <span className="text-green-400">✓</span>
              Envío gratis a toda Colombia
              <span className="text-neutral-600">•</span>
              <span>3-5 días hábiles</span>
            </div>

            {/* Separador */}
            <div className="border-t border-neutral-800" />

            {/* Cantidad */}
            <div>
              <label className="text-sm font-semibold text-neutral-300 mb-3 block">
                Cantidad
              </label>
              <div className="flex items-center gap-4">
                <div className="flex items-center bg-neutral-900 border border-neutral-700
                  rounded-full px-1 py-1 gap-1">
                  <button
                    onClick={() => setQuantity((q) => Math.max(1, q - 1))}
                    disabled={quantity <= 1}
                    className="w-8 h-8 flex items-center justify-center rounded-full
                      hover:bg-white/10 text-neutral-400 hover:text-white
                      disabled:opacity-30 disabled:cursor-not-allowed transition-all"
                  >
                    <Minus size={14} />
                  </button>
                  <span className="w-10 text-center text-white font-bold tabular-nums">
                    {quantity}
                  </span>
                  <button
                    onClick={() => setQuantity((q) => Math.min(product.stockQuantity || 99, q + 1))}
                    disabled={quantity >= (product.stockQuantity || 99)}
                    className="w-8 h-8 flex items-center justify-center rounded-full
                      hover:bg-white/10 text-neutral-400 hover:text-white
                      disabled:opacity-30 disabled:cursor-not-allowed transition-all"
                  >
                    <Plus size={14} />
                  </button>
                </div>
                <span className="text-sm text-neutral-500">
                  Total: <span className="text-white font-semibold">{formatPrice(product.price * quantity)}</span>
                </span>
              </div>
            </div>

            {/* Botones */}
            <div className="flex flex-col sm:flex-row gap-3">
              <button
                onClick={handleAddToCart}
                disabled={!product.inStock}
                className={cn(
                  'flex-1 flex items-center justify-center gap-2.5 py-3.5 rounded-full font-semibold text-sm transition-all duration-200',
                  product.inStock
                    ? 'bg-blue-600 hover:bg-blue-700 text-white hover:shadow-[0_0_25px_rgba(37,99,235,0.4)] active:scale-[0.98]'
                    : 'bg-neutral-800 text-neutral-600 cursor-not-allowed'
                )}
              >
                <ShoppingCart size={17} />
                Agregar al carrito
              </button>

              <button
                onClick={handleBuyNow}
                disabled={!product.inStock}
                className={cn(
                  'flex-1 flex items-center justify-center gap-2.5 py-3.5 rounded-full font-semibold text-sm transition-all duration-200',
                  product.inStock
                    ? 'bg-amber-500 hover:bg-amber-600 text-black hover:shadow-[0_0_25px_rgba(245,158,11,0.35)] active:scale-[0.98]'
                    : 'bg-neutral-800 text-neutral-600 cursor-not-allowed'
                )}
              >
                <Zap size={17} />
                Comprar ahora
              </button>
            </div>

            {/* Garantías */}
            <div className="grid grid-cols-3 gap-3 pt-2">
              {[
                { emoji: '🔒', label: 'Pago seguro' },
                { emoji: '↩️', label: 'Devolución 30 días' },
                { emoji: '💬', label: 'Soporte 24/7' },
              ].map((item) => (
                <div
                  key={item.label}
                  className="flex flex-col items-center gap-1.5 bg-neutral-900/50
                    border border-neutral-800 rounded-xl p-3 text-center"
                >
                  <span className="text-lg">{item.emoji}</span>
                  <span className="text-[11px] text-neutral-500 leading-tight">{item.label}</span>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* ── Tabs descripción ── */}
        <div className="mb-16 border-t border-neutral-800 pt-12">
          <div className="flex gap-1 mb-8 bg-neutral-900/50 border border-neutral-800 p-1 rounded-full w-fit">
            {(['descripcion', 'beneficios'] as const).map((tab) => (
              <button
                key={tab}
                onClick={() => setActiveTab(tab)}
                className={cn(
                  'px-5 py-2 rounded-full text-sm font-medium transition-all duration-200 capitalize',
                  activeTab === tab
                    ? 'bg-blue-600 text-white shadow-[0_0_15px_rgba(37,99,235,0.3)]'
                    : 'text-neutral-400 hover:text-white'
                )}
              >
                {tab === 'descripcion' ? 'Descripción' : 'Beneficios'}
              </button>
            ))}
          </div>

          <div className="max-w-3xl">
            {activeTab === 'descripcion' ? (
              <div className="text-neutral-300 leading-relaxed whitespace-pre-line">
                {product.description || 'Sin descripción disponible.'}
              </div>
            ) : (
              <ul className="space-y-3">
                {[
                  'Calidad garantizada — probado y verificado',
                  'Envío gratis a toda Colombia',
                  'Devolución sin preguntas en 30 días',
                  'Soporte por WhatsApp 24 horas al día',
                  'Pago seguro con cifrado SSL',
                ].map((benefit) => (
                  <li key={benefit} className="flex items-start gap-3 text-neutral-300">
                    <CheckCircle2 size={18} className="text-green-400 mt-0.5 shrink-0" />
                    {benefit}
                  </li>
                ))}
              </ul>
            )}
          </div>
        </div>

        {/* ── Productos relacionados ── */}
        {relatedProducts.length > 0 && (
          <div className="border-t border-neutral-800 pt-12">
            <div className="flex items-end justify-between mb-8">
              <h2 className="text-xl sm:text-2xl font-bold text-white">
                Productos relacionados
              </h2>
              <Link
                href={`/${locale}/productos?category=${product.categorySlug}`}
                className="text-sm text-neutral-400 hover:text-white transition-colors flex items-center gap-1 group"
              >
                Ver más
                <ChevronRight size={14} className="transition-transform group-hover:translate-x-0.5" />
              </Link>
            </div>
            <ProductGrid
              products={relatedProducts.slice(0, 4)}
              locale={locale}
              columns={4}
            />
          </div>
        )}
      </div>
    </div>
  )
}
