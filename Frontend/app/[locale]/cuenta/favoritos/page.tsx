'use client'

import { useEffect, useState } from 'react'
import { usePathname, useParams } from 'next/navigation'
import Link from 'next/link'
import Image from 'next/image'
import { Heart, Loader2, ShoppingCart } from 'lucide-react'
import { toast } from 'sonner'
import { AuthGuard } from '@/components/cuenta/AuthGuard'
import { AccountSidebar } from '@/components/cuenta/AccountSidebar'
import { useCartStore } from '@/lib/store/cartStore'
import api, { getErrorMessage } from '@/lib/api'

interface Product {
  id: number
  name: string
  slug: string
  price: number
  images: string[]
  categoryName: string
  inStock: boolean
}

const formatCOP = (price: number) =>
  new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 }).format(price)

export default function FavoritosPage() {
  const pathname = usePathname()
  const params = useParams()
  const locale = (params?.locale as string) ?? 'es'
  const [products, setProducts] = useState<Product[]>([])
  const [loading, setLoading] = useState(true)
  const { addItem } = useCartStore()

  useEffect(() => {
    const fetchWishlist = async () => {
      try {
        const res = await api.get('/api/v1/users/me/wishlist')
        const data = res.data?.data
        setProducts(Array.isArray(data) ? data : [])
      } catch (err) {
        toast.error(getErrorMessage(err))
      } finally {
        setLoading(false)
      }
    }
    fetchWishlist()
  }, [])

  const handleRemove = async (productId: number) => {
    try {
      await api.delete(`/api/v1/users/me/wishlist/${productId}`)
      setProducts((prev) => prev.filter((p) => p.id !== productId))
      toast.success('Eliminado de favoritos')
    } catch (err) {
      toast.error(getErrorMessage(err))
    }
  }

  const handleAddToCart = (product: Product) => {
    addItem({
      productId: product.id,
      name: product.name,
      price: product.price,
      image: product.images[0] ?? null,
      slug: product.slug,
    })
    toast.success('Agregado al carrito')
  }

  return (
    <AuthGuard locale={locale}>
      <div className="min-h-screen bg-[#0A0A0A] pt-24 pb-16 px-4">
        <div className="max-w-5xl mx-auto flex gap-8">
          <AccountSidebar currentPath={pathname} locale={locale} />

          <div className="flex-1 min-w-0">
            <h1 className="text-2xl font-bold text-neutral-50 mb-6">Favoritos</h1>

            {loading ? (
              <div className="flex items-center justify-center py-20">
                <Loader2 className="w-8 h-8 animate-spin text-blue-500" />
              </div>
            ) : products.length === 0 ? (
              <div className="bg-neutral-900 border border-neutral-700 rounded-xl p-12 text-center">
                <Heart className="w-12 h-12 text-neutral-600 mx-auto mb-4" />
                <p className="text-neutral-400 text-sm mb-4">No tienes favoritos aún</p>
                <Link
                  href={`/${locale}/productos`}
                  className="inline-block rounded-full bg-blue-600 hover:bg-blue-700 text-white font-semibold px-6 py-2 text-sm transition-colors"
                >
                  Explorar productos
                </Link>
              </div>
            ) : (
              <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
                {products.map((product) => (
                  <div
                    key={product.id}
                    className="bg-neutral-900 border border-neutral-700 rounded-xl overflow-hidden group"
                  >
                    {/* Imagen */}
                    <div className="relative aspect-square bg-neutral-800">
                      {product.images[0] ? (
                        <Image
                          src={product.images[0]}
                          alt={product.name}
                          fill
                          className="object-cover group-hover:scale-105 transition-transform duration-300"
                          sizes="(max-width: 768px) 50vw, 33vw"
                        />
                      ) : (
                        <div className="w-full h-full flex items-center justify-center text-neutral-600">
                          <Heart className="w-8 h-8" />
                        </div>
                      )}
                      {!product.inStock && (
                        <div className="absolute inset-0 bg-black/60 flex items-center justify-center">
                          <span className="text-xs text-neutral-300 font-medium">Sin stock</span>
                        </div>
                      )}
                    </div>

                    {/* Info */}
                    <div className="p-3">
                      <Link href={`/${locale}/productos/${product.slug}`}>
                        <p className="text-neutral-50 text-sm font-medium line-clamp-2 hover:text-blue-400 transition-colors mb-1">
                          {product.name}
                        </p>
                      </Link>
                      <p className="text-neutral-400 text-xs mb-2">{product.categoryName}</p>
                      <p className="text-neutral-50 font-bold text-sm mb-3">{formatCOP(product.price)}</p>

                      <div className="flex gap-2">
                        <button
                          onClick={() => handleAddToCart(product)}
                          disabled={!product.inStock}
                          className="flex-1 flex items-center justify-center gap-1.5 rounded-full bg-blue-600 hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed text-white text-xs font-semibold py-2 transition-colors"
                        >
                          <ShoppingCart className="w-3.5 h-3.5" />
                          Agregar
                        </button>
                        <button
                          onClick={() => handleRemove(product.id)}
                          className="rounded-full bg-red-500/10 hover:bg-red-500/20 text-red-400 p-2 transition-colors"
                        >
                          <Heart className="w-3.5 h-3.5 fill-current" />
                        </button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </AuthGuard>
  )
}
