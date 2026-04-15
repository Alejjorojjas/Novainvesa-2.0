'use client'

import { useEffect, useState } from 'react'
import { useParams } from 'next/navigation'
import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from 'recharts'
import { TrendingUp, Clock, Users, Package, Loader2 } from 'lucide-react'
import { toast } from 'sonner'
import { AdminLayout } from '@/components/admin/AdminLayout'
import { getErrorMessage } from '@/lib/api'

interface DashboardStats {
  todaySales: number
  monthSales: number
  pendingOrders: number
  totalProducts: number
  newCustomers: number
}

interface SalesChartData {
  labels: string[]
  values: number[]
}

interface RecentOrder {
  id: number
  orderCode: string
  status: string
  total: number
  createdAt: string
  customerName: string
}

interface TopProduct {
  productName: string
  sales: number
  revenue: number
}

const formatCOP = (price: number) =>
  new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 }).format(price)

const statusBadge: Record<string, string> = {
  PENDING: 'bg-amber-500/20 text-amber-400 border border-amber-500/30',
  CONFIRMED: 'bg-blue-500/20 text-blue-400 border border-blue-500/30',
  PROCESSING: 'bg-blue-500/20 text-blue-400 border border-blue-500/30',
  SHIPPED: 'bg-indigo-500/20 text-indigo-400 border border-indigo-500/30',
  DELIVERED: 'bg-green-500/20 text-green-400 border border-green-500/30',
  CANCELLED: 'bg-red-500/20 text-red-400 border border-red-500/30',
}

const statusLabel: Record<string, string> = {
  PENDING: 'Pendiente',
  CONFIRMED: 'Confirmado',
  PROCESSING: 'En proceso',
  SHIPPED: 'Enviado',
  DELIVERED: 'Entregado',
  CANCELLED: 'Cancelado',
}

async function adminFetch<T>(path: string): Promise<T> {
  const token = typeof window !== 'undefined' ? localStorage.getItem('nova-admin-token') : null
  const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL ?? ''}${path}`, {
    headers: { Authorization: `Bearer ${token ?? ''}`, 'Content-Type': 'application/json' },
  })
  const json = await res.json() as { success: boolean; data: T }
  if (!json.success) throw new Error('Error al obtener datos')
  return json.data
}

export default function DashboardPage() {
  const params = useParams()
  const locale = (params?.locale as string) ?? 'es'

  const [stats, setStats] = useState<DashboardStats | null>(null)
  const [chartData, setChartData] = useState<{ name: string; value: number }[]>([])
  const [recentOrders, setRecentOrders] = useState<RecentOrder[]>([])
  const [topProducts, setTopProducts] = useState<TopProduct[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const fetchAll = async () => {
      try {
        const [s, chart, orders, top] = await Promise.all([
          adminFetch<DashboardStats>('/api/v1/admin/dashboard/stats'),
          adminFetch<SalesChartData>('/api/v1/admin/dashboard/sales-chart'),
          adminFetch<RecentOrder[]>('/api/v1/admin/dashboard/recent-orders'),
          adminFetch<TopProduct[]>('/api/v1/admin/dashboard/top-products'),
        ])
        setStats(s)
        setChartData(
          chart.labels.map((label, i) => ({ name: label, value: chart.values[i] ?? 0 }))
        )
        setRecentOrders(Array.isArray(orders) ? orders : [])
        setTopProducts(Array.isArray(top) ? top : [])
      } catch (err) {
        toast.error(getErrorMessage(err))
      } finally {
        setLoading(false)
      }
    }
    fetchAll()
  }, [])

  const metricCards = stats
    ? [
        { label: 'Ventas hoy', value: formatCOP(stats.todaySales), icon: <TrendingUp className="w-5 h-5 text-green-400" /> },
        { label: 'Ventas del mes', value: formatCOP(stats.monthSales), icon: <TrendingUp className="w-5 h-5 text-blue-400" /> },
        { label: 'Pedidos pendientes', value: String(stats.pendingOrders), icon: <Clock className="w-5 h-5 text-amber-400" /> },
        { label: 'Nuevos clientes', value: String(stats.newCustomers), icon: <Users className="w-5 h-5 text-purple-400" /> },
      ]
    : []

  return (
    <AdminLayout locale={locale}>
      <div className="space-y-8">
        <h1 className="text-2xl font-bold text-neutral-50">Dashboard</h1>

        {loading ? (
          <div className="flex items-center justify-center py-20">
            <Loader2 className="w-8 h-8 animate-spin text-blue-500" />
          </div>
        ) : (
          <>
            {/* Métricas */}
            <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
              {metricCards.map((card) => (
                <div
                  key={card.label}
                  className="bg-neutral-900 border border-neutral-700 rounded-xl p-5"
                >
                  <div className="flex items-center justify-between mb-2">
                    <p className="text-neutral-400 text-xs font-medium">{card.label}</p>
                    {card.icon}
                  </div>
                  <p className="text-neutral-50 text-xl font-bold">{card.value}</p>
                </div>
              ))}
            </div>

            {/* Gráfico de ventas */}
            {chartData.length > 0 && (
              <div className="bg-neutral-900 border border-neutral-700 rounded-xl p-6">
                <h2 className="text-base font-semibold text-neutral-50 mb-4">Ventas últimos 30 días</h2>
                <ResponsiveContainer width="100%" height={240}>
                  <AreaChart data={chartData} margin={{ top: 4, right: 4, left: 0, bottom: 0 }}>
                    <defs>
                      <linearGradient id="colorGradient" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="5%" stopColor="#2563EB" stopOpacity={0.3} />
                        <stop offset="95%" stopColor="#2563EB" stopOpacity={0} />
                      </linearGradient>
                    </defs>
                    <CartesianGrid strokeDasharray="3 3" stroke="#262626" />
                    <XAxis
                      dataKey="name"
                      tick={{ fill: '#737373', fontSize: 11 }}
                      axisLine={false}
                      tickLine={false}
                    />
                    <YAxis
                      tick={{ fill: '#737373', fontSize: 11 }}
                      axisLine={false}
                      tickLine={false}
                      tickFormatter={(v: number) => `$${(v / 1000).toFixed(0)}k`}
                    />
                    <Tooltip
                      contentStyle={{
                        backgroundColor: '#171717',
                        border: '1px solid #404040',
                        borderRadius: '8px',
                        color: '#fafafa',
                        fontSize: '12px',
                      }}
                      formatter={(value) => [formatCOP(Number(value ?? 0)), 'Ventas']}
                    />
                    <Area
                      type="monotone"
                      dataKey="value"
                      stroke="#2563EB"
                      strokeWidth={2}
                      fill="url(#colorGradient)"
                    />
                  </AreaChart>
                </ResponsiveContainer>
              </div>
            )}

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              {/* Pedidos recientes */}
              <div className="bg-neutral-900 border border-neutral-700 rounded-xl p-6">
                <h2 className="text-base font-semibold text-neutral-50 mb-4">Pedidos recientes</h2>
                {recentOrders.length === 0 ? (
                  <p className="text-neutral-500 text-sm">Sin pedidos recientes</p>
                ) : (
                  <div className="space-y-3">
                    {recentOrders.map((order) => (
                      <div
                        key={order.id}
                        className="flex items-center justify-between gap-3 py-2 border-b border-neutral-800 last:border-0"
                      >
                        <div className="min-w-0">
                          <p className="font-mono text-blue-400 text-sm font-semibold truncate">
                            #{order.orderCode}
                          </p>
                          <p className="text-neutral-400 text-xs truncate">{order.customerName}</p>
                        </div>
                        <div className="text-right shrink-0">
                          <p className="text-neutral-50 text-sm font-semibold">{formatCOP(order.total)}</p>
                          <span
                            className={`rounded-full px-2 py-0.5 text-xs font-medium ${statusBadge[order.status] ?? 'bg-neutral-700 text-neutral-400'}`}
                          >
                            {statusLabel[order.status] ?? order.status}
                          </span>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>

              {/* Top productos */}
              <div className="bg-neutral-900 border border-neutral-700 rounded-xl p-6">
                <h2 className="text-base font-semibold text-neutral-50 mb-4">
                  <Package className="w-4 h-4 inline mr-2 text-blue-400" />
                  Top productos
                </h2>
                {topProducts.length === 0 ? (
                  <p className="text-neutral-500 text-sm">Sin datos</p>
                ) : (
                  <div className="space-y-3">
                    {topProducts.slice(0, 5).map((product, i) => (
                      <div
                        key={product.productName}
                        className="flex items-center gap-3 py-2 border-b border-neutral-800 last:border-0"
                      >
                        <span className="text-neutral-600 text-sm font-mono w-4 shrink-0">
                          {i + 1}
                        </span>
                        <p className="text-neutral-300 text-sm flex-1 truncate">{product.productName}</p>
                        <div className="text-right shrink-0">
                          <p className="text-neutral-50 text-sm font-semibold">{formatCOP(product.revenue)}</p>
                          <p className="text-neutral-500 text-xs">{product.sales} ventas</p>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          </>
        )}
      </div>
    </AdminLayout>
  )
}
