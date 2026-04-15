'use client'

import Image from 'next/image'
import { Truck } from 'lucide-react'
import type { CartItem } from '@/lib/store/cartStore'

const formatPrice = (price: number) =>
  new Intl.NumberFormat('es-CO', {
    style: 'currency',
    currency: 'COP',
    maximumFractionDigits: 0,
  }).format(price)

interface OrderSummaryProps {
  items: CartItem[]
}

export function OrderSummary({ items }: OrderSummaryProps) {
  const subtotal = items.reduce((sum, item) => sum + item.price * item.quantity, 0)

  return (
    <div className="bg-neutral-900 border border-neutral-700 rounded-xl p-6 sticky top-24">
      <h2 className="text-lg font-semibold text-neutral-50 mb-4">Resumen del pedido</h2>

      {/* Badge envío gratis */}
      <div className="flex items-center gap-2 bg-green-500/10 border border-green-500/20 rounded-lg px-3 py-2 mb-4">
        <Truck className="w-4 h-4 text-green-400 shrink-0" />
        <span className="text-green-400 text-sm font-medium">Envío gratis a todo Colombia</span>
      </div>

      {/* Lista de items */}
      <div className="space-y-3 mb-4">
        {items.map((item) => (
          <div key={item.productId} className="flex gap-3">
            <div className="relative w-14 h-14 rounded-lg overflow-hidden bg-neutral-800 shrink-0">
              {item.image ? (
                <Image
                  src={item.image}
                  alt={item.name}
                  fill
                  className="object-cover"
                  sizes="56px"
                />
              ) : (
                <div className="w-full h-full flex items-center justify-center text-neutral-500 text-xs">
                  Sin img
                </div>
              )}
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-neutral-50 text-sm font-medium leading-tight line-clamp-2">
                {item.name}
              </p>
              <p className="text-neutral-400 text-xs mt-0.5">
                {item.quantity} × {formatPrice(item.price)}
              </p>
            </div>
            <p className="text-neutral-50 text-sm font-semibold shrink-0">
              {formatPrice(item.price * item.quantity)}
            </p>
          </div>
        ))}
      </div>

      <div className="border-t border-neutral-800 pt-4 space-y-2">
        <div className="flex justify-between text-sm">
          <span className="text-neutral-400">Subtotal</span>
          <span className="text-neutral-50">{formatPrice(subtotal)}</span>
        </div>
        <div className="flex justify-between text-sm">
          <span className="text-neutral-400">Envío</span>
          <span className="text-green-400 font-medium">GRATIS</span>
        </div>
        <div className="border-t border-neutral-800 pt-2 flex justify-between">
          <span className="text-neutral-50 font-semibold">Total</span>
          <span className="text-neutral-50 font-bold text-lg">{formatPrice(subtotal)}</span>
        </div>
      </div>
    </div>
  )
}
