'use client'

import { useState, useEffect, useCallback, use } from 'react'
import { useSearchParams, useRouter } from 'next/navigation'
import { SlidersHorizontal, X, ChevronLeft, ChevronRight, ArrowUpDown } from 'lucide-react'
import api, { extractData } from '@/lib/api'
import { ProductGrid } from '@/components/product/ProductGrid'
import type { Product } from '@/components/product/ProductCard'
import { cn } from '@/lib/utils'
import { formatPrice } from '@/lib/utils'

// ─── Tipos ────────────────────────────────────────────────────────────────────

interface Category {
  id: number
  name: string
  slug: string
  icon?: string
}

interface PagedData {
  content: Product[]
  totalPages: number
  totalElements: number
  number: number
}

const SORT_OPTIONS = [
  { value: 'createdAt_desc', label: 'Más recientes' },
  { value: 'price_asc', label: 'Precio: menor a mayor' },
  { value: 'price_desc', label: 'Precio: mayor a menor' },
  { value: 'name_asc', label: 'Nombre A-Z' },
]

// ─── Componente ───────────────────────────────────────────────────────────────

interface Props {
  params: Promise<{ locale: string }>
}

export default function ProductosPage({ params }: Props) {
  const { locale } = use(params)
  const searchParams = useSearchParams()
  const router = useRouter()

  // Estado de filtros
  const [categorySlug, setCategorySlug] = useState(searchParams.get('category') ?? '')
  const [search, setSearch] = useState(searchParams.get('search') ?? '')
  const [sortKey, setSortKey] = useState(
    searchParams.get('sortBy')
      ? `${searchParams.get('sortBy')}_${searchParams.get('sortDir') ?? 'desc'}`
      : 'createdAt_desc'
  )
  const [minPrice, setMinPrice] = useState('')
  const [maxPrice, setMaxPrice] = useState('')
  const [onlyInStock, setOnlyInStock] = useState(false)
  const [page, setPage] = useState(0)
  const [sidebarOpen, setSidebarOpen] = useState(false)

  // Estado de datos
  const [products, setProducts] = useState<Product[]>([])
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [isLoading, setIsLoading] = useState(true)
  const [categories, setCategories] = useState<Category[]>([])

  // Cargar categorías una vez
  useEffect(() => {
    api.get('/api/v1/categories')
      .then((res) => {
        const data = extractData<Category[]>(res)
        setCategories(data)
      })
      .catch(() => setCategories([]))
  }, [])

  // Cargar productos
  const fetchProducts = useCallback(async () => {
    setIsLoading(true)
    try {
      const [sortBy, sortDir] = sortKey.split('_')
      const params: Record<string, string | number> = {
        page,
        size: 12,
        sortBy: sortBy ?? 'createdAt',
        sortDir: sortDir ?? 'desc',
      }
      if (categorySlug) params.categorySlug = categorySlug
      if (search) params.search = search

      const res = await api.get('/api/v1/products', { params })
      const data = extractData<PagedData>(res)

      let filtered = data.content
      if (minPrice) filtered = filtered.filter((p) => p.price >= Number(minPrice))
      if (maxPrice) filtered = filtered.filter((p) => p.price <= Number(maxPrice))
      if (onlyInStock) filtered = filtered.filter((p) => p.inStock)

      setProducts(filtered)
      setTotalPages(data.totalPages)
      setTotalElements(data.totalElements)
    } catch {
      setProducts([])
    } finally {
      setIsLoading(false)
    }
  }, [categorySlug, search, sortKey, minPrice, maxPrice, onlyInStock, page])

  useEffect(() => {
    fetchProducts()
  }, [fetchProducts])

  // Sincronizar URL
  useEffect(() => {
    const [sortBy, sortDir] = sortKey.split('_')
    const url = new URLSearchParams()
    if (categorySlug) url.set('category', categorySlug)
    if (search) url.set('search', search)
    if (sortKey !== 'createdAt_desc') {
      url.set('sortBy', sortBy)
      url.set('sortDir', sortDir)
    }
    if (page > 0) url.set('page', String(page))
    router.replace(`/${locale}/productos${url.toString() ? `?${url}` : ''}`, { scroll: false })
  }, [categorySlug, search, sortKey, page, locale, router])

  const handleCategoryChange = (slug: string) => {
    setCategorySlug(slug)
    setPage(0)
    setSidebarOpen(false)
  }

  const handleSortChange = (val: string) => {
    setSortKey(val)
    setPage(0)
  }

  const clearFilters = () => {
    setCategorySlug('')
    setSearch('')
    setSortKey('createdAt_desc')
    setMinPrice('')
    setMaxPrice('')
    setOnlyInStock(false)
    setPage(0)
  }

  const hasFilters = !!(categorySlug || search || minPrice || maxPrice || onlyInStock)

  return (
    <div className="min-h-screen bg-[#0A0A0A]">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 py-8">

        {/* ── Header ── */}
        <div className="mb-8">
          <h1 className="text-2xl sm:text-3xl font-bold text-white mb-1">
            {search ? `Resultados para "${search}"` : categorySlug
              ? (categories.find((c) => c.slug === categorySlug)?.name ?? 'Productos')
              : 'Catálogo de productos'}
          </h1>
          {!isLoading && (
            <p className="text-sm text-neutral-500">
              {totalElements} {totalElements === 1 ? 'producto encontrado' : 'productos encontrados'}
            </p>
          )}
        </div>

        <div className="flex gap-8">

          {/* ── Sidebar ── */}
          {/* Overlay móvil */}
          {sidebarOpen && (
            <div
              className="fixed inset-0 bg-black/60 z-40 lg:hidden"
              onClick={() => setSidebarOpen(false)}
            />
          )}

          <aside
            className={cn(
              'fixed top-0 left-0 h-full w-72 bg-[#111] border-r border-neutral-800 z-50 p-6 overflow-y-auto',
              'transition-transform duration-300',
              'lg:static lg:h-auto lg:w-64 lg:shrink-0 lg:translate-x-0 lg:bg-transparent lg:border-none lg:z-auto lg:p-0',
              sidebarOpen ? 'translate-x-0' : '-translate-x-full'
            )}
          >
            <div className="flex items-center justify-between mb-6 lg:mb-0">
              <h2 className="text-white font-semibold text-base flex items-center gap-2">
                <SlidersHorizontal size={16} />
                Filtros
              </h2>
              <button
                onClick={() => setSidebarOpen(false)}
                className="lg:hidden text-neutral-500 hover:text-white"
              >
                <X size={18} />
              </button>
            </div>

            {hasFilters && (
              <button
                onClick={clearFilters}
                className="flex items-center gap-1.5 text-xs text-red-400 hover:text-red-300
                  transition-colors mb-4 mt-2"
              >
                <X size={12} />
                Limpiar filtros
              </button>
            )}

            <div className="space-y-6 lg:mt-4">
              {/* Categorías */}
              <div>
                <p className="text-xs font-semibold text-neutral-400 uppercase tracking-wider mb-3">
                  Categoría
                </p>
                <div className="space-y-1">
                  <button
                    onClick={() => handleCategoryChange('')}
                    className={cn(
                      'w-full text-left text-sm px-3 py-2 rounded-lg transition-colors',
                      !categorySlug
                        ? 'bg-blue-600/20 text-blue-400 font-medium'
                        : 'text-neutral-400 hover:text-white hover:bg-white/5'
                    )}
                  >
                    Todas las categorías
                  </button>
                  {categories.map((cat) => (
                    <button
                      key={cat.slug}
                      onClick={() => handleCategoryChange(cat.slug)}
                      className={cn(
                        'w-full text-left text-sm px-3 py-2 rounded-lg transition-colors flex items-center gap-2',
                        categorySlug === cat.slug
                          ? 'bg-blue-600/20 text-blue-400 font-medium'
                          : 'text-neutral-400 hover:text-white hover:bg-white/5'
                      )}
                    >
                      {cat.icon && <span>{cat.icon}</span>}
                      {cat.name}
                    </button>
                  ))}
                </div>
              </div>

              {/* Precio */}
              <div className="border-t border-neutral-800 pt-6">
                <p className="text-xs font-semibold text-neutral-400 uppercase tracking-wider mb-3">
                  Rango de precio
                </p>
                <div className="flex gap-2">
                  <div className="flex-1">
                    <label className="text-xs text-neutral-600 mb-1 block">Mínimo</label>
                    <input
                      type="number"
                      placeholder="0"
                      value={minPrice}
                      onChange={(e) => { setMinPrice(e.target.value); setPage(0) }}
                      className="w-full bg-neutral-900 border border-neutral-700 text-white text-sm
                        px-3 py-2 rounded-lg outline-none focus:border-blue-600
                        placeholder:text-neutral-700"
                    />
                  </div>
                  <div className="flex-1">
                    <label className="text-xs text-neutral-600 mb-1 block">Máximo</label>
                    <input
                      type="number"
                      placeholder="∞"
                      value={maxPrice}
                      onChange={(e) => { setMaxPrice(e.target.value); setPage(0) }}
                      className="w-full bg-neutral-900 border border-neutral-700 text-white text-sm
                        px-3 py-2 rounded-lg outline-none focus:border-blue-600
                        placeholder:text-neutral-700"
                    />
                  </div>
                </div>
              </div>

              {/* En stock */}
              <div className="border-t border-neutral-800 pt-6">
                <label className="flex items-center gap-3 cursor-pointer group">
                  <div
                    onClick={() => { setOnlyInStock(!onlyInStock); setPage(0) }}
                    className={cn(
                      'w-5 h-5 rounded border-2 flex items-center justify-center transition-all',
                      onlyInStock
                        ? 'bg-blue-600 border-blue-600'
                        : 'border-neutral-600 group-hover:border-neutral-400'
                    )}
                  >
                    {onlyInStock && (
                      <svg className="w-3 h-3 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3} d="M5 13l4 4L19 7" />
                      </svg>
                    )}
                  </div>
                  <span className="text-sm text-neutral-300 group-hover:text-white transition-colors">
                    Solo en stock
                  </span>
                </label>
              </div>
            </div>
          </aside>

          {/* ── Contenido principal ── */}
          <div className="flex-1 min-w-0">

            {/* Toolbar */}
            <div className="flex items-center justify-between gap-4 mb-6">
              {/* Abrir filtros móvil */}
              <button
                onClick={() => setSidebarOpen(true)}
                className="lg:hidden flex items-center gap-2 text-sm text-neutral-400
                  hover:text-white border border-neutral-700 hover:border-neutral-600
                  px-3 py-2 rounded-full transition-colors"
              >
                <SlidersHorizontal size={14} />
                Filtros
                {hasFilters && (
                  <span className="bg-blue-600 text-white text-xs rounded-full w-4 h-4 flex items-center justify-center">
                    •
                  </span>
                )}
              </button>

              {/* Filtros activos pills */}
              <div className="flex-1 flex flex-wrap gap-2 overflow-hidden">
                {categorySlug && (
                  <span className="flex items-center gap-1.5 bg-blue-600/15 text-blue-400
                    border border-blue-600/30 text-xs px-2.5 py-1 rounded-full">
                    {categories.find((c) => c.slug === categorySlug)?.name ?? categorySlug}
                    <button onClick={() => handleCategoryChange('')}>
                      <X size={11} />
                    </button>
                  </span>
                )}
                {search && (
                  <span className="flex items-center gap-1.5 bg-neutral-700/50 text-neutral-300
                    border border-neutral-700 text-xs px-2.5 py-1 rounded-full">
                    &quot;{search}&quot;
                    <button onClick={() => { setSearch(''); setPage(0) }}>
                      <X size={11} />
                    </button>
                  </span>
                )}
              </div>

              {/* Sort */}
              <div className="flex items-center gap-2 shrink-0">
                <ArrowUpDown size={14} className="text-neutral-500 hidden sm:block" />
                <select
                  value={sortKey}
                  onChange={(e) => handleSortChange(e.target.value)}
                  className="bg-neutral-900 border border-neutral-700 text-neutral-300 text-sm
                    px-3 py-2 rounded-full outline-none focus:border-blue-600
                    hover:border-neutral-600 cursor-pointer transition-colors"
                >
                  {SORT_OPTIONS.map((o) => (
                    <option key={o.value} value={o.value}>
                      {o.label}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            {/* Grid */}
            <ProductGrid
              products={products}
              isLoading={isLoading}
              locale={locale}
              columns={3}
              skeletonCount={12}
            />

            {/* Paginación */}
            {!isLoading && totalPages > 1 && (
              <div className="flex items-center justify-center gap-2 mt-10">
                <button
                  onClick={() => setPage((p) => Math.max(0, p - 1))}
                  disabled={page === 0}
                  className="flex items-center gap-1.5 px-4 py-2 rounded-full border border-neutral-700
                    text-sm text-neutral-400 hover:text-white hover:border-neutral-600
                    disabled:opacity-40 disabled:cursor-not-allowed transition-all"
                >
                  <ChevronLeft size={15} />
                  Anterior
                </button>

                <div className="flex gap-1">
                  {Array.from({ length: Math.min(totalPages, 7) }).map((_, i) => {
                    let pageNum = i
                    if (totalPages > 7) {
                      if (page < 4) {
                        pageNum = i
                      } else if (page > totalPages - 4) {
                        pageNum = totalPages - 7 + i
                      } else {
                        pageNum = page - 3 + i
                      }
                    }
                    return (
                      <button
                        key={pageNum}
                        onClick={() => setPage(pageNum)}
                        className={cn(
                          'w-9 h-9 rounded-full text-sm font-medium transition-all',
                          page === pageNum
                            ? 'bg-blue-600 text-white shadow-[0_0_15px_rgba(37,99,235,0.4)]'
                            : 'text-neutral-500 hover:text-white hover:bg-white/5'
                        )}
                      >
                        {pageNum + 1}
                      </button>
                    )
                  })}
                </div>

                <button
                  onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                  disabled={page >= totalPages - 1}
                  className="flex items-center gap-1.5 px-4 py-2 rounded-full border border-neutral-700
                    text-sm text-neutral-400 hover:text-white hover:border-neutral-600
                    disabled:opacity-40 disabled:cursor-not-allowed transition-all"
                >
                  Siguiente
                  <ChevronRight size={15} />
                </button>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}
