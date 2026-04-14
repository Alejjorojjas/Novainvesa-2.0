'use client'

import Image from 'next/image'
import Link from 'next/link'
import { ShoppingCart, Eye } from 'lucide-react'
import { toast } from 'sonner'
import { useCartStore } from '@/lib/store/cartStore'
import { useUIStore } from '@/lib/store/uiStore'
import { formatPrice, calcDiscount, cn } from '@/lib/utils'

export interface Product {
  id: number
  name: string
  slug: string
  description: string
  price: number
  compareAtPrice?: number
  images: string[]
  categorySlug: string
  categoryName: string
  inStock: boolean
  stockQuantity: number
  isFeatured: boolean
  rating?: number
  reviewCount?: number
  dropiProductId?: string
}

interface ProductCardProps {
  product: Product
  locale: string
  priority?: boolean
}

export function ProductCard({ product, locale, priority = false }: ProductCardProps) {
  const addItem = useCartStore((s) => s.addItem)
  const openCart = useUIStore((s) => s.openCart)

  const mainImage = product.images?.[0] ?? 'https://via.placeholder.com/400x400'
  const discount = calcDiscount(product.price, product.compareAtPrice ?? null)

  const handleAddToCart = (e: React.MouseEvent) => {
    e.preventDefault()
    e.stopPropagation()

    if (!product.inStock) return

    addItem({
      productId: product.id,
      slug: product.slug,
      name: product.name,
      price: product.price,
      image: mainImage,
      quantity: 1,
    })

    toast.success('Agregado al carrito', {
      description: product.name,
      action: {
        label: 'Ver carrito',
        onClick: openCart,
      },
    })
  }

  return (
    <Link
      href={`/${locale}/productos/${product.slug}`}
      className={cn(
        'group relative bg-neutral-900 rounded-xl border border-neutral-800 overflow-hidden flex flex-col',
        'transition-all duration-300 hover:-translate-y-1 hover:border-blue-600/60',
        'hover:shadow-[0_8px_30px_rgba(37,99,235,0.15)]'
      )}
    >
      {/* Imagen */}
      <div className="relative aspect-square overflow-hidden bg-neutral-800">
        <Image
          src={mainImage}
          alt={product.name}
          fill
          sizes="(max-width: 640px) 50vw, (max-width: 1024px) 33vw, 25vw"
          className="object-cover transition-transform duration-500 group-hover:scale-105"
          priority={priority}
        />

        {/* Badges */}
        <div className="absolute top-2.5 left-2.5 flex flex-col gap-1.5">
          {discount && (
            <span className="bg-amber-500 text-black text-[11px] font-bold px-2 py-0.5 rounded-full">
              -{discount}%
            </span>
          )}
          {!product.inStock && (
            <span className="bg-neutral-700/90 text-neutral-300 text-[11px] font-semibold px-2 py-0.5 rounded-full backdrop-blur-sm">
              Agotado
            </span>
          )}
          {product.isFeatured && product.inStock && (
            <span className="bg-blue-600/90 text-white text-[11px] font-semibold px-2 py-0.5 rounded-full backdrop-blur-sm">
              Destacado
            </span>
          )}
        </div>

        {/* Overlay hover — botón ver */}
        <div
          className={cn(
            'absolute inset-0 bg-black/40 backdrop-blur-[1px] flex items-center justify-center',
            'opacity-0 group-hover:opacity-100 transition-all duration-300'
          )}
        >
          <span
            className="flex items-center gap-2 bg-white/15 border border-white/20 text-white
              text-xs font-semibold px-3 py-1.5 rounded-full backdrop-blur-sm
              translate-y-2 group-hover:translate-y-0 transition-transform duration-300"
          >
            <Eye size={13} />
            Ver producto
          </span>
        </div>
      </div>

      {/* Contenido */}
      <div className="p-3.5 flex flex-col gap-2 flex-1">
        {/* Categoría */}
        <span className="text-[11px] font-medium text-neutral-500 uppercase tracking-wider">
          {product.categoryName}
        </span>

        {/* Nombre */}
        <h3 className="text-sm font-semibold text-neutral-50 line-clamp-2 leading-snug flex-1">
          {product.name}
        </h3>

        {/* Rating */}
        {product.rating && product.reviewCount && (
          <div className="flex items-center gap-1.5">
            <div className="flex gap-0.5">
              {Array.from({ length: 5 }).map((_, i) => (
                <svg
                  key={i}
                  className={cn(
                    'w-3 h-3',
                    i < Math.round(product.rating!) ? 'text-amber-400' : 'text-neutral-700'
                  )}
                  fill="currentColor"
                  viewBox="0 0 20 20"
                >
                  <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
                </svg>
              ))}
            </div>
            <span className="text-[11px] text-neutral-500">
              ({product.reviewCount})
            </span>
          </div>
        )}

        {/* Precio */}
        <div className="flex items-baseline gap-2 mt-auto">
          <span className="text-base font-bold text-white">
            {formatPrice(product.price)}
          </span>
          {product.compareAtPrice && product.compareAtPrice > product.price && (
            <span className="text-xs text-neutral-500 line-through">
              {formatPrice(product.compareAtPrice)}
            </span>
          )}
        </div>

        {/* Botón agregar */}
        <button
          onClick={handleAddToCart}
          disabled={!product.inStock}
          className={cn(
            'flex items-center justify-center gap-2 w-full py-2 rounded-full text-sm font-semibold',
            'transition-all duration-200',
            product.inStock
              ? 'bg-blue-600 hover:bg-blue-700 text-white hover:shadow-[0_0_15px_rgba(37,99,235,0.4)] active:scale-[0.98]'
              : 'bg-neutral-800 text-neutral-600 cursor-not-allowed'
          )}
        >
          <ShoppingCart size={14} />
          {product.inStock ? 'Agregar' : 'Agotado'}
        </button>
      </div>
    </Link>
  )
}
