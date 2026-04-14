'use client'

import Image from 'next/image'
import Link from 'next/link'
import { ShoppingBag, Trash2, Plus, Minus, ArrowRight, Package } from 'lucide-react'
import { Sheet, SheetContent, SheetHeader, SheetTitle } from '@/components/ui/sheet'
import { useCartStore } from '@/lib/store/cartStore'
import { useUIStore } from '@/lib/store/uiStore'
import { formatPrice } from '@/lib/utils'

interface CartDrawerProps {
  locale: string
}

export function CartDrawer({ locale }: CartDrawerProps) {
  const { cartOpen, closeCart } = useUIStore()
  const { items, removeItem, updateQuantity, getTotal, getItemCount } = useCartStore()
  const total = getTotal()
  const count = getItemCount()

  return (
    <Sheet open={cartOpen} onOpenChange={closeCart}>
      <SheetContent
        side="right"
        className="bg-[#111111] border-l border-[#27272A] text-white
          flex flex-col w-full sm:max-w-[420px] p-0 gap-0"
      >
        {/* Header */}
        <SheetHeader className="px-5 py-4 border-b border-[#27272A] shrink-0">
          <SheetTitle className="text-white flex items-center gap-2.5 text-base">
            <div className="w-8 h-8 rounded-full bg-blue-600/15 flex items-center justify-center">
              <ShoppingBag size={16} className="text-blue-400" />
            </div>
            Carrito
            {count > 0 && (
              <span
                className="bg-blue-600/20 text-blue-400 text-xs font-semibold
                  px-2 py-0.5 rounded-full border border-blue-600/30"
              >
                {count} {count === 1 ? 'item' : 'items'}
              </span>
            )}
          </SheetTitle>
        </SheetHeader>

        {/* Items */}
        <div className="flex-1 overflow-y-auto px-5 py-4">
          {items.length === 0 ? (
            /* Empty state */
            <div className="flex flex-col items-center justify-center h-full gap-5 text-center py-16">
              <div className="w-20 h-20 rounded-full bg-[#27272A] flex items-center justify-center">
                <Package size={36} className="text-neutral-600" />
              </div>
              <div>
                <p className="text-white font-semibold text-base">Tu carrito está vacío</p>
                <p className="text-neutral-500 text-sm mt-1.5">
                  Agrega productos para comenzar tu compra
                </p>
              </div>
              <button
                onClick={closeCart}
                className="bg-blue-600 hover:bg-blue-500 text-white px-6 py-2.5
                  rounded-full text-sm font-medium transition-all duration-200
                  hover:shadow-[0_0_20px_rgba(37,99,235,0.4)] mt-1"
              >
                Explorar productos
              </button>
            </div>
          ) : (
            <div className="space-y-3">
              {items.map((item) => (
                <div
                  key={item.productId}
                  className="flex gap-3.5 bg-[#18181B] rounded-2xl p-3.5
                    border border-[#27272A] hover:border-[#3F3F46]
                    transition-colors duration-200 group"
                >
                  {/* Imagen */}
                  <div
                    className="relative w-[72px] h-[72px] rounded-xl overflow-hidden
                      bg-[#27272A] shrink-0 border border-[#3F3F46]"
                  >
                    {item.image ? (
                      <Image
                        src={item.image}
                        alt={item.name}
                        fill
                        className="object-cover"
                        sizes="72px"
                      />
                    ) : (
                      <div className="w-full h-full flex items-center justify-center">
                        <Package size={24} className="text-neutral-700" />
                      </div>
                    )}
                  </div>

                  {/* Info */}
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium text-white line-clamp-2 leading-snug pr-1">
                      {item.name}
                    </p>
                    <p className="text-blue-400 font-semibold text-sm mt-1.5">
                      {formatPrice(item.price)}
                    </p>

                    {/* Controles */}
                    <div className="flex items-center justify-between mt-2.5">
                      {/* Selector de cantidad */}
                      <div
                        className="flex items-center gap-1.5 bg-[#27272A]
                          rounded-full px-1 py-1 border border-[#3F3F46]"
                      >
                        <button
                          onClick={() => updateQuantity(item.productId, item.quantity - 1)}
                          className="w-6 h-6 flex items-center justify-center rounded-full
                            hover:bg-white/10 transition-colors text-neutral-400 hover:text-white"
                          aria-label="Reducir cantidad"
                        >
                          <Minus size={11} />
                        </button>
                        <span className="text-sm font-semibold text-white w-5 text-center tabular-nums">
                          {item.quantity}
                        </span>
                        <button
                          onClick={() => updateQuantity(item.productId, item.quantity + 1)}
                          className="w-6 h-6 flex items-center justify-center rounded-full
                            hover:bg-white/10 transition-colors text-neutral-400 hover:text-white"
                          aria-label="Aumentar cantidad"
                        >
                          <Plus size={11} />
                        </button>
                      </div>

                      {/* Subtotal + eliminar */}
                      <div className="flex items-center gap-2">
                        <span className="text-xs text-neutral-500 tabular-nums">
                          {formatPrice(item.price * item.quantity)}
                        </span>
                        <button
                          onClick={() => removeItem(item.productId)}
                          className="p-1.5 text-neutral-600 hover:text-red-400
                            hover:bg-red-500/10 rounded-lg transition-all duration-200"
                          aria-label={`Eliminar ${item.name}`}
                        >
                          <Trash2 size={13} />
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Footer */}
        {items.length > 0 && (
          <div
            className="px-5 py-5 border-t border-[#27272A] space-y-4 shrink-0
              bg-gradient-to-t from-[#111111] to-transparent"
          >
            {/* Envío gratis */}
            <div
              className="flex items-center gap-2 bg-green-500/10 border border-green-500/20
                rounded-xl px-3 py-2"
            >
              <span className="w-2 h-2 bg-green-400 rounded-full animate-pulse shrink-0" />
              <span className="text-xs text-green-400 font-medium">
                ¡Envío gratis incluido en tu pedido!
              </span>
            </div>

            {/* Total */}
            <div className="flex items-center justify-between">
              <div>
                <p className="text-xs text-neutral-500 uppercase tracking-wider">Total</p>
                <p className="text-2xl font-bold text-white tabular-nums">
                  {formatPrice(total)}
                </p>
              </div>
              <div className="text-right">
                <p className="text-xs text-neutral-500">Envío</p>
                <p className="text-sm font-semibold text-green-400">Gratis</p>
              </div>
            </div>

            {/* Botón checkout */}
            <Link
              href={`/${locale}/checkout`}
              onClick={closeCart}
              className="flex items-center justify-center gap-2 w-full bg-blue-600
                hover:bg-blue-500 text-white font-semibold py-3.5 rounded-full
                transition-all duration-200
                hover:shadow-[0_0_25px_rgba(37,99,235,0.5)]
                group text-sm"
            >
              Finalizar compra
              <ArrowRight
                size={16}
                className="transition-transform duration-200 group-hover:translate-x-1"
              />
            </Link>

            {/* Seguir comprando */}
            <button
              onClick={closeCart}
              className="w-full text-center text-sm text-neutral-500
                hover:text-neutral-300 transition-colors py-1"
            >
              ← Seguir comprando
            </button>
          </div>
        )}
      </SheetContent>
    </Sheet>
  )
}
