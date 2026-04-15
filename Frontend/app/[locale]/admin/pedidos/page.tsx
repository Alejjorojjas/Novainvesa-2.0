'use client'

import { useEffect, useState } from 'react'
import { useParams } from 'next/navigation'
import { Loader2, ChevronLeft, ChevronRight } from 'lucide-react'
import { toast } from 'sonner'
import { AdminLayout } from '@/components/admin/AdminLayout'
import { getErrorMessage } from '@/lib/api'

interface Order {
  id: number
  orderCode: string
  status: string
  paymentMethod: string
  total: number
  createdAt: string
  customerName: string
  shippingCity?: string
}

const ALL_STATUSES = ['PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED']

const statusLabel: Record<string, string> = {
  PENDING: 'Pendiente',
  CONFIRMED: 'Confirmado',
  PROCESSING: 'En proceso',
  SHIPPED: 'Enviado',
  DELIVERED: 'Entregado',
  CANCELLED: 'Cancelado',
}

const statusBadgeClass: Record<string, string> = {
  PENDING: 'bg-amber-500/20 text-amber-400 border border-amber-500/30',
  CONFIRMED: 'bg-blue-500/20 text-blue-400 border border-blue-500/30',
  PROCESSING: 'bg-blue-500/20 text-blue-400 border border-blue-500/30',
  SHIPPED: 'bg-indigo-500/20 text-indigo-400 border border-indigo-500/30',
  DELIVERED: 'bg-green-500/20 text-green-400 border border-green-500/30',
  CANCELLED: 'bg-red-500/20 text-red-400 border border-red-500/30',
}

const paymentLabel: Record<string, string> = {
  COD: 'Contraentrega',
  WOMPI: 'Wompi',
  MERCADOPAGO: 'MercadoPago',
}

const formatCOP = (price: number) =>
  new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 }).format(price)

function getAdminToken(): string {
  if (typeof window === 'undefined') return ''
  return localStorage.getItem('nova-admin-token') ?? ''
}

async function adminFetch<T>(path: string): Promise<T> {
  const token = getAdminToken()
  const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL ?? ''}${path}`, {
    headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
  })
  const json = await res.json() as { success: boolean; data: T }
  if (!json.success) throw new Error('Error al obtener datos')
  return json.data
}

async function adminPut(path: string, body: unknown): Promise<void> {
  const token = getAdminToken()
  const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL ?? ''}${path}`, {
    method: 'PUT',
    headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })
  const json = await res.json() as { success: boolean }
  if (!json.success) throw new Error('Error al actualizar')
}

interface OrdersPage {
  content: Order[]
  totalPages: number
}

export default function AdminPedidosPage() {
  const params = useParams()
  const locale = (params?.locale as string) ?? 'es'

  const [orders, setOrders] = useState<Order[]>([])
  const [loading, setLoading] = useState(true)
  const [statusFilter, setStatusFilter] = useState('')
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(1)
  const [updatingId, setUpdatingId] = useState<number | null>(null)

  const fetchOrders = async (status: string, p: number) => {
    setLoading(true)
    try {
      const query = `?status=${status}&page=${p}`
      const data = await adminFetch<OrdersPage>(`/api/v1/admin/orders${query}`)
      setOrders(Array.isArray(data) ? data : (data.content ?? []))
      setTotalPages(typeof data === 'object' && !Array.isArray(data) ? (data.totalPages ?? 1) : 1)
    } catch (err) {
      toast.error(getErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchOrders(statusFilter, page)
  }, [statusFilter, page])

  const handleStatusChange = async (orderCode: string, orderId: number, newStatus: string) => {
    setUpdatingId(orderId)
    try {
      await adminPut(`/api/v1/admin/orders/${orderCode}/status`, { status: newStatus })
      setOrders((prev) =>
        prev.map((o) => (o.id === orderId ? { ...o, status: newStatus } : o))
      )
      toast.success('Estado actualizado')
    } catch (err) {
      toast.error(getErrorMessage(err))
    } finally {
      setUpdatingId(null)
    }
  }

  return (
    <AdminLayout locale={locale}>
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <h1 className="text-2xl font-bold text-neutral-50">Pedidos</h1>
          <select
            value={statusFilter}
            onChange={(e) => { setStatusFilter(e.target.value); setPage(0) }}
            className="bg-neutral-800 border border-neutral-700 text-neutral-50 text-sm rounded-lg px-3 py-2 focus:border-blue-600 focus:outline-none"
          >
            <option value="">Todos los estados</option>
            {ALL_STATUSES.map((s) => (
              <option key={s} value={s}>{statusLabel[s]}</option>
            ))}
          </select>
        </div>

        {loading ? (
          <div className="flex items-center justify-center py-20">
            <Loader2 className="w-8 h-8 animate-spin text-blue-500" />
          </div>
        ) : orders.length === 0 ? (
          <div className="bg-neutral-900 border border-neutral-700 rounded-xl p-12 text-center">
            <p className="text-neutral-400 text-sm">No hay pedidos con este filtro</p>
          </div>
        ) : (
          <>
            <div className="bg-neutral-900 border border-neutral-700 rounded-xl overflow-hidden">
              <table className="w-full">
                <thead>
                  <tr className="border-b border-neutral-800">
                    <th className="text-left text-xs text-neutral-500 font-medium px-4 py-3">Código</th>
                    <th className="text-left text-xs text-neutral-500 font-medium px-4 py-3">Cliente</th>
                    <th className="text-left text-xs text-neutral-500 font-medium px-4 py-3 hidden md:table-cell">Ciudad</th>
                    <th className="text-left text-xs text-neutral-500 font-medium px-4 py-3">Total</th>
                    <th className="text-left text-xs text-neutral-500 font-medium px-4 py-3 hidden lg:table-cell">Pago</th>
                    <th className="text-left text-xs text-neutral-500 font-medium px-4 py-3">Estado</th>
                    <th className="text-left text-xs text-neutral-500 font-medium px-4 py-3 hidden lg:table-cell">Fecha</th>
                  </tr>
                </thead>
                <tbody>
                  {orders.map((order) => (
                    <tr
                      key={order.id}
                      className="border-b border-neutral-800 last:border-0 hover:bg-neutral-800/40 transition-colors"
                    >
                      <td className="px-4 py-3">
                        <span className="font-mono text-blue-400 text-sm font-semibold">
                          #{order.orderCode}
                        </span>
                      </td>
                      <td className="px-4 py-3">
                        <p className="text-neutral-50 text-sm truncate max-w-[140px]">{order.customerName}</p>
                      </td>
                      <td className="px-4 py-3 hidden md:table-cell">
                        <p className="text-neutral-400 text-sm">{order.shippingCity ?? '—'}</p>
                      </td>
                      <td className="px-4 py-3">
                        <p className="text-neutral-50 text-sm font-semibold">{formatCOP(order.total)}</p>
                      </td>
                      <td className="px-4 py-3 hidden lg:table-cell">
                        <p className="text-neutral-400 text-xs">{paymentLabel[order.paymentMethod] ?? order.paymentMethod}</p>
                      </td>
                      <td className="px-4 py-3">
                        <div className="flex items-center gap-2">
                          <span
                            className={`rounded-full px-2 py-0.5 text-xs font-medium whitespace-nowrap ${statusBadgeClass[order.status] ?? 'bg-neutral-700 text-neutral-400'}`}
                          >
                            {statusLabel[order.status] ?? order.status}
                          </span>
                          {updatingId === order.id ? (
                            <Loader2 className="w-3.5 h-3.5 animate-spin text-blue-400" />
                          ) : (
                            <select
                              value={order.status}
                              onChange={(e) => handleStatusChange(order.orderCode, order.id, e.target.value)}
                              className="bg-neutral-800 border border-neutral-700 text-neutral-300 text-xs rounded px-1.5 py-1 focus:border-blue-600 focus:outline-none"
                            >
                              {ALL_STATUSES.map((s) => (
                                <option key={s} value={s}>{statusLabel[s]}</option>
                              ))}
                            </select>
                          )}
                        </div>
                      </td>
                      <td className="px-4 py-3 hidden lg:table-cell">
                        <p className="text-neutral-500 text-xs">
                          {new Date(order.createdAt).toLocaleDateString('es-CO', {
                            day: 'numeric',
                            month: 'short',
                            year: 'numeric',
                          })}
                        </p>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {/* Paginación */}
            {totalPages > 1 && (
              <div className="flex items-center justify-center gap-3">
                <button
                  onClick={() => setPage((p) => Math.max(0, p - 1))}
                  disabled={page === 0}
                  className="p-2 rounded-lg bg-neutral-800 hover:bg-neutral-700 disabled:opacity-40 disabled:cursor-not-allowed text-neutral-300 transition-colors"
                >
                  <ChevronLeft className="w-4 h-4" />
                </button>
                <span className="text-neutral-400 text-sm">
                  Página {page + 1} de {totalPages}
                </span>
                <button
                  onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                  disabled={page >= totalPages - 1}
                  className="p-2 rounded-lg bg-neutral-800 hover:bg-neutral-700 disabled:opacity-40 disabled:cursor-not-allowed text-neutral-300 transition-colors"
                >
                  <ChevronRight className="w-4 h-4" />
                </button>
              </div>
            )}
          </>
        )}
      </div>
    </AdminLayout>
  )
}
