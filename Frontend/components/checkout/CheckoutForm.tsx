'use client'

import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useRouter } from 'next/navigation'
import Link from 'next/link'
import { Loader2 } from 'lucide-react'
import { toast } from 'sonner'
import { PaymentSelector } from './PaymentSelector'
import { useAuthStore } from '@/lib/store/authStore'
import { useCartStore } from '@/lib/store/cartStore'
import api, { getErrorMessage } from '@/lib/api'

const checkoutSchema = z.object({
  customerName: z.string().min(3, 'Mínimo 3 caracteres'),
  customerEmail: z.string().email('Email inválido'),
  customerPhone: z
    .string()
    .min(10, 'Teléfono inválido')
    .regex(/^[0-9+\s-]+$/, 'Solo números'),
  shippingAddress: z.string().min(5, 'Dirección muy corta'),
  shippingCity: z.string().min(2, 'Ciudad requerida'),
  shippingDepartment: z.string().min(2, 'Departamento requerido'),
  paymentMethod: z.enum(['COD', 'WOMPI', 'MERCADOPAGO']),
})

type CheckoutData = z.infer<typeof checkoutSchema>

const inputClass =
  'w-full bg-neutral-800 border border-neutral-700 text-neutral-50 placeholder:text-neutral-500 rounded-lg px-3 py-2.5 text-sm focus:border-blue-600 focus:outline-none transition-colors'
const labelClass = 'block text-sm font-medium text-neutral-300 mb-1.5'
const errorClass = 'text-red-400 text-xs mt-1'

export function CheckoutForm() {
  const [loading, setLoading] = useState(false)
  const { isAuthenticated } = useAuthStore()
  const { items, clearCart } = useCartStore()
  const router = useRouter()

  const {
    register,
    handleSubmit,
    watch,
    setValue,
    formState: { errors },
  } = useForm<CheckoutData>({
    resolver: zodResolver(checkoutSchema),
    defaultValues: { paymentMethod: 'MERCADOPAGO' },
  })

  const paymentMethod = watch('paymentMethod')
  const shippingCity = watch('shippingCity')
  const shippingDepartment = watch('shippingDepartment')

  const onSubmit = async (data: CheckoutData) => {
    if (items.length === 0) {
      toast.error('Tu carrito está vacío')
      return
    }
    setLoading(true)
    try {
      const body = {
        customerName: data.customerName,
        customerEmail: data.customerEmail,
        customerPhone: data.customerPhone,
        shippingAddress: data.shippingAddress,
        shippingCity: data.shippingCity,
        shippingDepartment: data.shippingDepartment,
        paymentMethod: data.paymentMethod,
        items: items.map((i) => ({
          productId: i.productId,
          quantity: i.quantity,
          unitPrice: i.price,
        })),
      }
      const res = await api.post('/api/v1/orders', body)
      const orderData = res.data?.data as {
        orderCode: string
        paymentUrl?: string
        total: number
        paymentMethod: string
      }

      clearCart()

      if (orderData.paymentUrl) {
        window.location.href = orderData.paymentUrl
      } else {
        router.push(`/confirmacion?orderCode=${orderData.orderCode}`)
      }
    } catch (err) {
      toast.error(getErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-8">
      {/* Sección 1: Datos personales */}
      <section>
        <h2 className="text-base font-semibold text-neutral-50 mb-4 pb-2 border-b border-neutral-800">
          Datos personales
        </h2>
        <div className="space-y-4">
          <div>
            <label className={labelClass}>Nombre completo</label>
            <input
              {...register('customerName')}
              placeholder="María García"
              className={inputClass}
            />
            {errors.customerName && (
              <p className={errorClass}>{errors.customerName.message}</p>
            )}
          </div>
          <div>
            <label className={labelClass}>Email</label>
            <input
              {...register('customerEmail')}
              type="email"
              placeholder="maria@ejemplo.com"
              className={inputClass}
            />
            {errors.customerEmail && (
              <p className={errorClass}>{errors.customerEmail.message}</p>
            )}
          </div>
          <div>
            <label className={labelClass}>Teléfono</label>
            <input
              {...register('customerPhone')}
              type="tel"
              placeholder="300 123 4567"
              className={inputClass}
            />
            {errors.customerPhone && (
              <p className={errorClass}>{errors.customerPhone.message}</p>
            )}
          </div>
        </div>
      </section>

      {/* Sección 2: Dirección de envío */}
      <section>
        <h2 className="text-base font-semibold text-neutral-50 mb-4 pb-2 border-b border-neutral-800">
          Dirección de envío
        </h2>
        <div className="space-y-4">
          <div>
            <label className={labelClass}>Dirección</label>
            <input
              {...register('shippingAddress')}
              placeholder="Cra 10 # 20-30, Apto 401"
              className={inputClass}
            />
            {errors.shippingAddress && (
              <p className={errorClass}>{errors.shippingAddress.message}</p>
            )}
          </div>
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className={labelClass}>Ciudad</label>
              <input
                {...register('shippingCity')}
                placeholder="Bogotá"
                className={inputClass}
              />
              {errors.shippingCity && (
                <p className={errorClass}>{errors.shippingCity.message}</p>
              )}
            </div>
            <div>
              <label className={labelClass}>Departamento</label>
              <input
                {...register('shippingDepartment')}
                placeholder="Cundinamarca"
                className={inputClass}
              />
              {errors.shippingDepartment && (
                <p className={errorClass}>{errors.shippingDepartment.message}</p>
              )}
            </div>
          </div>
        </div>
      </section>

      {/* Sección 3: Método de pago */}
      <section>
        <h2 className="text-base font-semibold text-neutral-50 mb-4 pb-2 border-b border-neutral-800">
          Método de pago
        </h2>
        <PaymentSelector
          value={paymentMethod}
          onChange={(m) => setValue('paymentMethod', m)}
          city={shippingCity ?? ''}
          department={shippingDepartment ?? ''}
        />
        {errors.paymentMethod && (
          <p className={errorClass}>{errors.paymentMethod.message}</p>
        )}
      </section>

      {/* Banner de registro */}
      {!isAuthenticated && (
        <div className="bg-neutral-900 border border-neutral-700 rounded-xl p-4">
          <div className="flex items-start gap-3">
            <span className="text-xl shrink-0">💡</span>
            <div className="flex-1">
              <p className="text-neutral-50 text-sm font-medium">
                ¿Quieres rastrear tu pedido?
              </p>
              <p className="text-neutral-400 text-xs mt-0.5">
                Regístrate para seguir tu envío en tiempo real.
              </p>
            </div>
            <Link
              href="/auth/registro"
              className="shrink-0 text-xs text-blue-400 hover:text-blue-300 font-medium whitespace-nowrap"
            >
              Registrarme →
            </Link>
          </div>
        </div>
      )}

      {/* Botón submit */}
      <button
        type="submit"
        disabled={loading}
        className="w-full rounded-full bg-blue-600 hover:bg-blue-700 disabled:opacity-60 disabled:cursor-not-allowed text-white font-semibold py-3 transition-colors flex items-center justify-center gap-2"
      >
        {loading ? (
          <>
            <Loader2 className="w-4 h-4 animate-spin" />
            Procesando...
          </>
        ) : (
          'Confirmar pedido →'
        )}
      </button>
    </form>
  )
}
