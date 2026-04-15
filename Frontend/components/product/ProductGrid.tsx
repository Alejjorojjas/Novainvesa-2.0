'use client'

import { ProductCard, type Product } from './ProductCard'
import { ProductSkeletonGrid } from './ProductSkeleton'

interface ProductGridProps {
  products: Product[]
  isLoading?: boolean
  locale: string
  columns?: 2 | 3 | 4
  skeletonCount?: number
}

export function ProductGrid({
  products,
  isLoading = false,
  locale,
  columns = 4,
  skeletonCount = 8,
}: ProductGridProps) {
  if (isLoading) {
    return <ProductSkeletonGrid count={skeletonCount} />
  }

  if (products.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-24 text-center">
        <div className="w-16 h-16 rounded-full bg-neutral-800 flex items-center justify-center mb-4">
          <span className="text-2xl">📦</span>
        </div>
        <p className="text-neutral-400 text-base font-medium">No hay productos disponibles</p>
        <p className="text-neutral-600 text-sm mt-1">Prueba con otros filtros o categorías</p>
      </div>
    )
  }

  const gridCols = {
    2: 'grid-cols-2',
    3: 'grid-cols-2 sm:grid-cols-3',
    4: 'grid-cols-2 sm:grid-cols-3 lg:grid-cols-4',
  }[columns]

  return (
    <div className={`grid ${gridCols} gap-4`}>
      {products.map((product, index) => (
        <ProductCard
          key={product.id}
          product={product}
          locale={locale}
          priority={index < 4}
        />
      ))}
    </div>
  )
}
