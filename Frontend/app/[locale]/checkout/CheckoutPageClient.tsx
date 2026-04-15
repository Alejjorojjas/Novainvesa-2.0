'use client'

import { useEffect } from 'react'
import { useRouter } from 'next/navigation'
import Link from 'next/link'
import { ChevronRight } from 'lucide-react'
import { CheckoutForm } from '@/components/checkout/CheckoutForm'
import { OrderSummary } from '@/components/checkout/OrderSummary'
import { useCartStore } from '@/lib/store/cartStore'

export function CheckoutPageClient() {
  const { items } = useCartStore()
  const router = useRouter()

  useEffect(() => {
    if (items.length === 0) {
      router.replace('/')
    }
  }, [items.length, router])

  if (items.length === 0) return null

  return (
    <div className="min-h-screen bg-[#0A0A0A]">
      <div className="max-w-6xl mx-auto px-4 py-8">
        {/* Breadcrumb */}
        <nav className="flex items-center gap-1.5 text-sm text-neutral-400 mb-8">
          <Link href="/" className="hover:text-neutral-50 transition-colors">
            Inicio
          </Link>
          <ChevronRight className="w-3.5 h-3.5" />
          <span className="text-neutral-50">Checkout</span>
        </nav>

        <h1 className="text-2xl font-bold text-neutral-50 mb-8">Finalizar compra</h1>

        {/* Layout 2 columnas */}
        <div className="flex flex-col-reverse md:grid md:grid-cols-5 gap-8">
          {/* Formulario — izq (60%) */}
          <div className="md:col-span-3">
            <CheckoutForm />
          </div>

          {/* Resumen — der (40%), sticky */}
          <div className="md:col-span-2">
            <OrderSummary items={items} />
          </div>
        </div>
      </div>
    </div>
  )
}
