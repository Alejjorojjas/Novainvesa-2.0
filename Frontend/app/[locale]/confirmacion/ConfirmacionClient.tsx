'use client'

import { useEffect } from 'react'
import Link from 'next/link'
import { CheckCircle2, Truck, MessageCircle } from 'lucide-react'
import { useCartStore } from '@/lib/store/cartStore'

interface Props {
  orderCode: string
  paymentMethod?: string
}

const WHATSAPP_NUMBER = '573001234567' // TODO: mover a env

export function ConfirmacionClient({ orderCode, paymentMethod }: Props) {
  const { clearCart } = useCartStore()

  useEffect(() => {
    clearCart()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const isGateway =
    paymentMethod === 'WOMPI' || paymentMethod === 'MERCADOPAGO'

  const whatsappMsg = encodeURIComponent(
    `Hola, quiero consultar mi pedido ${orderCode}`
  )

  return (
    <div className="min-h-screen bg-[#0A0A0A] flex items-center justify-center px-4 py-16">
      <div className="max-w-lg w-full text-center space-y-6">
        {/* Ícono animado */}
        <div className="flex justify-center">
          <div className="relative">
            <CheckCircle2 className="w-20 h-20 text-green-400 animate-[scale-in_0.5s_ease-out]" />
          </div>
        </div>

        {/* Título */}
        <div>
          <h1 className="text-3xl font-bold text-neutral-50 mb-2">
            ¡Pedido confirmado!
          </h1>
          {isGateway && (
            <p className="text-neutral-400 text-sm">
              Tu pago está siendo procesado
            </p>
          )}
        </div>

        {/* Código de pedido */}
        {orderCode && (
          <div className="bg-neutral-900 border border-neutral-700 rounded-xl px-6 py-4">
            <p className="text-neutral-400 text-xs mb-1 uppercase tracking-wide">
              Código de pedido
            </p>
            <p className="font-mono text-blue-400 text-xl font-bold">{orderCode}</p>
          </div>
        )}

        {/* Mensaje */}
        <div className="bg-neutral-900 border border-neutral-700 rounded-xl p-4 text-sm text-neutral-400 space-y-3">
          <p>Recibirás un email con los detalles de tu pedido.</p>
          <div className="flex items-center justify-center gap-2 text-neutral-300">
            <Truck className="w-4 h-4 text-blue-400" />
            <span>Entrega estimada: 3-5 días hábiles</span>
          </div>
        </div>

        {/* CTAs */}
        <div className="flex flex-col gap-3">
          {orderCode && (
            <Link
              href={`/rastrear?codigo=${orderCode}`}
              className="w-full rounded-full bg-blue-600 hover:bg-blue-700 text-white font-semibold py-3 transition-colors flex items-center justify-center gap-2"
            >
              Rastrear pedido
            </Link>
          )}

          <a
            href={`https://wa.me/${WHATSAPP_NUMBER}?text=${whatsappMsg}`}
            target="_blank"
            rel="noopener noreferrer"
            className="w-full rounded-full font-semibold py-3 transition-colors flex items-center justify-center gap-2 text-white"
            style={{ backgroundColor: '#25D366' }}
          >
            <MessageCircle className="w-4 h-4" />
            Consultar por WhatsApp
          </a>

          <Link
            href="/"
            className="text-neutral-400 hover:text-neutral-50 text-sm transition-colors py-2"
          >
            Seguir comprando
          </Link>
        </div>
      </div>
    </div>
  )
}
