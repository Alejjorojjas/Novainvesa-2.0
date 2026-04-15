'use client'

import { useEffect, useState } from 'react'
import { usePathname, useParams } from 'next/navigation'
import Link from 'next/link'
import { Package, Loader2 } from 'lucide-react'
import { toast } from 'sonner'
import { AuthGuard } from '@/components/cuenta/AuthGuard'
import { AccountSidebar } from '@/components/cuenta/AccountSidebar'
import api, { getErrorMessage } from '@/lib/api'

interface OrderItem {
  productName: string
  quantity: number
  unitPrice: number
}

interface Order {
  id: number
  orderCode: string
  status: 'PENDING' | 'CONFIRMED' | 'PROCESSING' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED'
  paymentMethod: 'COD' | 'WOMPI' | 'MERCADOPAGO'
  paymentStatus: 'PENDING' | 'CONFIRMED' | 'FAILED'
  total: number
  createdAt: string
  customerName: string
  items: OrderItem[]
}

const statusConfig: Record<Order['status'], { label: string; className: string }> = {
  PENDING: { label: 'Pendiente', className: 'bg-amber-500/20 text-amber-400 border border-amber-500/30' },
  CONFIRMED: { label: 'Confirmado', className: 'bg-blue-500/20 text-blue-400 border border-blue-500/30' },
  PROCESSING: { label: 'En proceso', className: 'bg-blue-500/20 text-blue-400 border border-blue-500/30' },
  SHIPPED: { label: 'Enviado', className: 'bg-indigo-500/20 text-indigo-400 border border-indigo-500/30' },
  DELIVERED: { label: 'Entregado', className: 'bg-green-500/20 text-green-400 border border-green-500/30' },
  CANCELLED: { label: 'Cancelado', className: 'bg-red-500/20 text-red-400 border border-red-500/30' },
}

const formatCOP = (price: number) =>
  new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 }).format(price)

export default function PedidosPage() {
  const pathname = usePathname()
  const params = useParams()
  const locale = (params?.locale as string) ?? 'es'
  const [orders, setOrders] = useState<Order[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const fetchOrders = async () => {
      try {
        const res = await api.get('/api/v1/users/me/orders')
        const data = res.data?.data
        setOrders(Array.isArray(data) ? data : (data?.content ?? []))
      } catch (err) {
        toast.error(getErrorMessage(err))
      } finally {
        setLoading(false)
      }
    }
    fetchOrders()
  }, [])

  return (
    <AuthGuard locale={locale}>
      <div className="min-h-screen bg-[#0A0A0A] pt-24 pb-16 px-4">
        <div className="max-w-5xl mx-auto flex gap-8">
          <AccountSidebar currentPath={pathname} locale={locale} />

          <div className="flex-1 min-w-0">
            <h1 className="text-2xl font-bold text-neutral-50 mb-6">Mis pedidos</h1>

            {loading ? (
              <div className="flex items-center justify-center py-20">
                <Loader2 className="w-8 h-8 animate-spin text-blue-500" />
              </div>
            ) : orders.length === 0 ? (
              <div className="bg-neutral-900 border border-neutral-700 rounded-xl p-12 text-center">
                <Package className="w-12 h-12 text-neutral-600 mx-auto mb-4" />
                <p className="text-neutral-400 text-sm mb-4">Aún no tienes pedidos</p>
                <Link
                  href={`/${locale}/productos`}
                  className="inline-block rounded-full bg-blue-600 hover:bg-blue-700 text-white font-semibold px-6 py-2 text-sm transition-colors"
                >
                  Ir a la tienda
                </Link>
              </div>
            ) : (
              <div className="space-y-4">
                {orders.map((order) => {
                  const sc = statusConfig[order.status]
                  return (
                    <div
                      key={order.id}
                      className="bg-neutral-900 border border-neutral-700 rounded-xl p-5"
                    >
                      <div className="flex items-start justify-between gap-4 mb-3">
                        <div>
                          <span className="font-mono text-blue-400 font-semibold text-sm">
                            #{order.orderCode}
                          </span>
                          <p className="text-neutral-400 text-xs mt-0.5">
                            {new Date(order.createdAt).toLocaleDateString('es-CO', {
                              year: 'numeric',
                              month: 'short',
                              day: 'numeric',
                            })}
                          </p>
                        </div>
                        <div className="flex items-center gap-2 flex-wrap justify-end">
                          <span
                            className={`rounded-full px-2 py-0.5 text-xs font-medium ${sc.className}`}
                          >
                            {sc.label}
                          </span>
                          <span className="text-neutral-50 font-semibold text-sm">
                            {formatCOP(order.total)}
                          </span>
                        </div>
                      </div>

                      {order.items && order.items.length > 0 && (
                        <div className="border-t border-neutral-800 pt-3 mt-3 space-y-1">
                          {order.items.map((item, i) => (
                            <div key={i} className="flex justify-between text-xs text-neutral-400">
                              <span>
                                {item.productName} × {item.quantity}
                              </span>
                              <span>{formatCOP(item.unitPrice * item.quantity)}</span>
                            </div>
                          ))}
                        </div>
                      )}

                      <div className="flex justify-end mt-3">
                        <Link
                          href={`/${locale}/rastrear?codigo=${order.orderCode}`}
                          className="text-xs text-blue-400 hover:text-blue-300 font-medium transition-colors"
                        >
                          Rastrear pedido →
                        </Link>
                      </div>
                    </div>
                  )
                })}
              </div>
            )}
          </div>
        </div>
      </div>
    </AuthGuard>
  )
}
