'use client'

import { cn } from '@/lib/utils'

function Skeleton({ className }: { className?: string }) {
  return (
    <div
      className={cn(
        'animate-pulse rounded-md bg-neutral-800',
        className
      )}
    />
  )
}

export function ProductSkeleton() {
  return (
    <div className="bg-neutral-900 rounded-xl border border-neutral-800 overflow-hidden flex flex-col">
      {/* Imagen */}
      <Skeleton className="w-full aspect-square rounded-none" />

      {/* Contenido */}
      <div className="p-4 flex flex-col gap-3 flex-1">
        {/* Categoría */}
        <Skeleton className="h-3 w-20 rounded-full" />

        {/* Nombre */}
        <div className="space-y-2">
          <Skeleton className="h-4 w-full" />
          <Skeleton className="h-4 w-3/4" />
        </div>

        {/* Precio */}
        <div className="flex items-center gap-2 mt-auto pt-2">
          <Skeleton className="h-5 w-24" />
          <Skeleton className="h-4 w-16" />
        </div>

        {/* Botón */}
        <Skeleton className="h-9 w-full rounded-full" />
      </div>
    </div>
  )
}

export function ProductSkeletonGrid({ count = 8 }: { count?: number }) {
  return (
    <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
      {Array.from({ length: count }).map((_, i) => (
        <ProductSkeleton key={i} />
      ))}
    </div>
  )
}
