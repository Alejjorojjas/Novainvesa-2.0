'use client'

import { useEffect, useState } from 'react'
import { useParams } from 'next/navigation'
import Link from 'next/link'
import Image from 'next/image'
import { Loader2, ChevronLeft, ChevronRight, Download } from 'lucide-react'
import { toast } from 'sonner'
import { AdminLayout } from '@/components/admin/AdminLayout'
import { getErrorMessage } from '@/lib/api'

interface AdminProduct {
  id: number
  name: string
  slug: string
  price: number
  status: 'ACTIVE' | 'DRAFT' | 'ARCHIVED'
  categoryName: string
  stockQuantity: number
  images: string[]
  dropiProductId?: string
}

const statusLabel: Record<string, string> = {
  ACTIVE: 'Activo',
  DRAFT: 'Borrador',
  ARCHIVED: 'Archivado',
}

const statusBadgeClass: Record<string, string> = {
  ACTIVE: 'bg-green-500/20 text-green-400 border border-green-500/30',
  DRAFT: 'bg-amber-500/20 text-amber-400 border border-amber-500/30',
  ARCHIVED: 'bg-neutral-700 text-neutral-400 border border-neutral-600',
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

interface ProductsPage {
  content: AdminProduct[]
  totalPages: number
}

const ALL_STATUSES = ['ACTIVE', 'DRAFT', 'ARCHIVED']

export default function AdminProductosPage() {
  const params = useParams()
  const locale = (params?.locale as string) ?? 'es'

  const [products, setProducts] = useState<AdminProduct[]>([])
  const [loading, setLoading] = useState(true)
  const [statusFilter, setStatusFilter] = useState('')
  const [categoryFilter, setCategoryFilter] = useState('')
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(1)
  const [updatingId, setUpdatingId] = useState<number | null>(null)

  const fetchProducts = async (status: string, category: string, p: number) => {
    setLoading(true)
    try {
      const query = `?page=${p}&size=20&status=${status}&categorySlug=${category}`
      const data = await adminFetch<ProductsPage>(`/api/v1/admin/products${query}`)
      setProducts(Array.isArray(data) ? data : (data.content ?? []))
      setTotalPages(typeof data === 'object' && !Array.isArray(data) ? (data.totalPages ?? 1) : 1)
    } catch (err) {
      toast.error(getErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchProducts(statusFilter, categoryFilter, page)
  }, [statusFilter, categoryFilter, page])

  const handleStatusChange = async (productId: number, newStatus: string) => {
    setUpdatingId(productId)
    try {
      await adminPut(`/api/v1/admin/products/${productId}/status`, { status: newStatus })
      setProducts((prev) =>
        prev.map((p) =>
          p.id === productId ? { ...p, status: newStatus as AdminProduct['status'] } : p
        )
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
        <div className="flex items-center justify-between flex-wrap gap-3">
          <h1 className="text-2xl font-bold text-neutral-50">Productos</h1>
          <div className="flex items-center gap-3 flex-wrap">
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
            <input
              value={categoryFilter}
              onChange={(e) => { setCategoryFilter(e.target.value); setPage(0) }}
              placeholder="Categoría (slug)"
              className="bg-neutral-800 border border-neutral-700 text-neutral-50 placeholder:text-neutral-500 text-sm rounded-lg px-3 py-2 focus:border-blue-600 focus:outline-none"
            />
            <Link
              href={`/${locale}/admin/productos/importar`}
              className="flex items-center gap-2 rounded-full bg-blue-600 hover:bg-blue-700 text-white font-semibold px-4 py-2 text-sm transition-colors"
            >
              <Download className="w-4 h-4" />
              Importar
            </Link>
          </div>
        </div>

        {loading ? (
          <div className="flex items-center justify-center py-20">
            <Loader2 className="w-8 h-8 animate-spin text-blue-500" />
          </div>
        ) : products.length === 0 ? (
          <div className="bg-neutral-900 border border-neutral-700 rounded-xl p-12 text-center">
            <p className="text-neutral-400 text-sm">No hay productos con este filtro</p>
          </div>
        ) : (
          <>
            <div className="bg-neutral-900 border border-neutral-700 rounded-xl overflow-hidden">
              <table className="w-full">
                <thead>
                  <tr className="border-b border-neutral-800">
                    <th className="text-left text-xs text-neutral-500 font-medium px-4 py-3 w-12"></th>
                    <th className="text-left text-xs text-neutral-500 font-medium px-4 py-3">Producto</th>
                    <th className="text-left text-xs text-neutral-500 font-medium px-4 py-3 hidden md:table-cell">Categoría</th>
                    <th className="text-left text-xs text-neutral-500 font-medium px-4 py-3">Precio</th>
                    <th className="text-left text-xs text-neutral-500 font-medium px-4 py-3 hidden lg:table-cell">Stock</th>
                    <th className="text-left text-xs text-neutral-500 font-medium px-4 py-3">Estado</th>
                  </tr>
                </thead>
                <tbody>
                  {products.map((product) => (
                    <tr
                      key={product.id}
                      className="border-b border-neutral-800 last:border-0 hover:bg-neutral-800/40 transition-colors"
                    >
                      {/* Thumbnail */}
                      <td className="px-4 py-3">
                        <div className="w-10 h-10 rounded-lg bg-neutral-800 overflow-hidden shrink-0">
                          {product.images[0] ? (
                            <Image
                              src={product.images[0]}
                              alt={product.name}
                              width={40}
                              height={40}
                              className="w-full h-full object-cover"
                            />
                          ) : (
                            <div className="w-full h-full bg-neutral-700" />
                          )}
                        </div>
                      </td>
                      <td className="px-4 py-3">
                        <p className="text-neutral-50 text-sm font-medium truncate max-w-[200px]">{product.name}</p>
                        {product.dropiProductId && (
                          <p className="text-neutral-500 text-xs font-mono">Dropi: {product.dropiProductId}</p>
                        )}
                      </td>
                      <td className="px-4 py-3 hidden md:table-cell">
                        <p className="text-neutral-400 text-sm">{product.categoryName}</p>
                      </td>
                      <td className="px-4 py-3">
                        <p className="text-neutral-50 text-sm font-semibold">{formatCOP(product.price)}</p>
                      </td>
                      <td className="px-4 py-3 hidden lg:table-cell">
                        <p className={`text-sm ${product.stockQuantity > 0 ? 'text-green-400' : 'text-red-400'}`}>
                          {product.stockQuantity}
                        </p>
                      </td>
                      <td className="px-4 py-3">
                        <div className="flex items-center gap-2">
                          <span
                            className={`rounded-full px-2 py-0.5 text-xs font-medium whitespace-nowrap ${statusBadgeClass[product.status]}`}
                          >
                            {statusLabel[product.status]}
                          </span>
                          {updatingId === product.id ? (
                            <Loader2 className="w-3.5 h-3.5 animate-spin text-blue-400" />
                          ) : (
                            <select
                              value={product.status}
                              onChange={(e) => handleStatusChange(product.id, e.target.value)}
                              className="bg-neutral-800 border border-neutral-700 text-neutral-300 text-xs rounded px-1.5 py-1 focus:border-blue-600 focus:outline-none"
                            >
                              {ALL_STATUSES.map((s) => (
                                <option key={s} value={s}>{statusLabel[s]}</option>
                              ))}
                            </select>
                          )}
                        </div>
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
