'use client'

import { useState, useEffect, useRef } from 'react'
import Link from 'next/link'
import { useRouter } from 'next/navigation'
import {
  ShoppingCart, Search, User, Menu, X, ChevronDown,
  Globe, LogOut, Package, Heart,
} from 'lucide-react'
import { useUIStore } from '@/lib/store/uiStore'
import { useCartStore } from '@/lib/store/cartStore'
import { useAuthStore } from '@/lib/store/authStore'
import { DarkModeToggle } from '@/components/common/DarkModeToggle'
import {
  DropdownMenu, DropdownMenuContent, DropdownMenuItem,
  DropdownMenuSeparator, DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { cn } from '@/lib/utils'

const CATEGORIES = [
  { label: 'Mascotas',    slug: 'mascotas',   emoji: '🐾' },
  { label: 'Hogar',       slug: 'hogar',       emoji: '🏠' },
  { label: 'Tecnología',  slug: 'tecnologia',  emoji: '📱' },
  { label: 'Belleza',     slug: 'belleza',     emoji: '✨' },
  { label: 'Fitness',     slug: 'fitness',     emoji: '💪' },
]

const LOCALES = [
  { code: 'es', label: 'Español',   flag: '🇨🇴' },
  { code: 'en', label: 'English',   flag: '🇺🇸' },
  { code: 'pt', label: 'Português', flag: '🇧🇷' },
]

interface NavbarProps {
  locale: string
}

export function Navbar({ locale }: NavbarProps) {
  const [scrolled, setScrolled]         = useState(false)
  const [mobileOpen, setMobileOpen]     = useState(false)
  const [searchOpen, setSearchOpen]     = useState(false)
  const [searchQuery, setSearchQuery]   = useState('')
  const [searchResults, setSearchResults] = useState<any[]>([])
  const [searchLoading, setSearchLoading] = useState(false)
  const searchRef      = useRef<HTMLDivElement>(null)
  const searchInputRef = useRef<HTMLInputElement>(null)

  const { toggleCart } = useUIStore()
  const itemCount = useCartStore((s) => s.getItemCount())
  const { user, isAuthenticated, logout } = useAuthStore()
  const router = useRouter()

  // Scroll effect
  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 20)
    window.addEventListener('scroll', onScroll, { passive: true })
    return () => window.removeEventListener('scroll', onScroll)
  }, [])

  // Cerrar búsqueda al hacer click afuera
  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (searchRef.current && !searchRef.current.contains(e.target as Node)) {
        setSearchOpen(false)
      }
    }
    document.addEventListener('mousedown', handler)
    return () => document.removeEventListener('mousedown', handler)
  }, [])

  // Debounce search
  useEffect(() => {
    if (searchQuery.length < 2) { setSearchResults([]); return }
    const timer = setTimeout(async () => {
      setSearchLoading(true)
      try {
        const apiUrl = process.env.NEXT_PUBLIC_API_URL ?? ''
        const res = await fetch(
          `${apiUrl}/api/v1/products/search?q=${encodeURIComponent(searchQuery)}&limit=5`
        )
        const json = await res.json()
        setSearchResults(json.data ?? [])
      } catch {
        setSearchResults([])
      } finally {
        setSearchLoading(false)
      }
    }, 300)
    return () => clearTimeout(timer)
  }, [searchQuery])

  const handleLocaleChange = (newLocale: string) => {
    router.push(`/${newLocale}`)
  }

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (searchQuery.trim()) {
      router.push(`/${locale}/productos?search=${encodeURIComponent(searchQuery.trim())}`)
      setSearchOpen(false)
      setSearchQuery('')
    }
  }

  return (
    <>
      <nav
        className={cn(
          'fixed top-0 left-0 right-0 z-50 transition-all duration-300',
          scrolled
            ? 'bg-[#0A0A0A]/95 backdrop-blur-xl border-b border-[#27272A] shadow-[0_4px_20px_rgba(0,0,0,0.4)]'
            : 'bg-[#0A0A0A]/80 backdrop-blur-md'
        )}
      >
        <div className="max-w-7xl mx-auto px-4 sm:px-6">
          <div className="flex items-center justify-between h-16">

            {/* ── Logo ── */}
            <Link href={`/${locale}`} className="flex items-center shrink-0 group">
              <span className="text-xl font-bold tracking-tight">
                <span className="text-blue-500 group-hover:text-blue-400 transition-colors">NOVA</span>
                <span className="text-white">INVESA</span>
              </span>
            </Link>

            {/* ── Categorías desktop ── */}
            <div className="hidden lg:flex items-center gap-0.5 mx-6">
              {CATEGORIES.map((cat) => (
                <Link
                  key={cat.slug}
                  href={`/${locale}/productos?category=${cat.slug}`}
                  className="flex items-center gap-1.5 text-neutral-400 hover:text-white
                    text-sm font-medium px-3 py-2 rounded-lg hover:bg-white/5
                    transition-all duration-200 group"
                >
                  <span className="text-base">{cat.emoji}</span>
                  {cat.label}
                </Link>
              ))}
            </div>

            {/* ── Barra de búsqueda desktop ── */}
            <div ref={searchRef} className="hidden md:flex flex-1 max-w-sm mx-4 relative">
              <form onSubmit={handleSearchSubmit} className="w-full">
                <div className="relative flex items-center">
                  <Search size={16} className="absolute left-3.5 text-neutral-500 pointer-events-none" />
                  <input
                    ref={searchInputRef}
                    value={searchQuery}
                    onChange={(e) => { setSearchQuery(e.target.value); setSearchOpen(true) }}
                    onFocus={() => setSearchOpen(true)}
                    placeholder="Buscar productos..."
                    className="w-full bg-[#27272A] border border-[#3F3F46] text-white text-sm
                      pl-9 pr-4 py-2 rounded-full outline-none transition-all duration-200
                      placeholder:text-neutral-500
                      focus:border-blue-600 focus:ring-2 focus:ring-blue-600/20
                      hover:border-[#52525B]"
                  />
                  {searchQuery && (
                    <button
                      type="button"
                      onClick={() => { setSearchQuery(''); setSearchResults([]) }}
                      className="absolute right-3 text-neutral-500 hover:text-white transition-colors"
                    >
                      <X size={14} />
                    </button>
                  )}
                </div>
              </form>

              {/* Dropdown resultados */}
              {searchOpen && searchQuery.length >= 2 && (
                <div
                  className="absolute top-full left-0 right-0 mt-2 bg-[#18181B]
                    border border-[#27272A] rounded-2xl overflow-hidden
                    shadow-[0_8px_30px_rgba(0,0,0,0.5)] z-50
                    animate-in fade-in-0 slide-in-from-top-2 duration-150"
                >
                  {searchLoading ? (
                    <div className="flex items-center justify-center gap-2 py-5 text-neutral-500 text-sm">
                      <span className="w-4 h-4 border-2 border-neutral-600 border-t-blue-500 rounded-full animate-spin" />
                      Buscando...
                    </div>
                  ) : searchResults.length === 0 ? (
                    <div className="py-5 text-center text-neutral-500 text-sm">
                      Sin resultados para &quot;{searchQuery}&quot;
                    </div>
                  ) : (
                    <div className="py-1.5">
                      {searchResults.map((p: any) => (
                        <button
                          key={p.slug}
                          onClick={() => {
                            router.push(`/${locale}/producto/${p.slug}`)
                            setSearchOpen(false)
                            setSearchQuery('')
                          }}
                          className="w-full flex items-center gap-3 px-4 py-2.5
                            hover:bg-white/5 transition-colors text-left"
                        >
                          <div className="w-10 h-10 rounded-lg bg-[#27272A] overflow-hidden shrink-0 border border-[#3F3F46]">
                            {p.primaryImage && (
                              // eslint-disable-next-line @next/next/no-img-element
                              <img
                                src={p.primaryImage}
                                alt={p.name}
                                className="w-full h-full object-cover"
                              />
                            )}
                          </div>
                          <div className="min-w-0 flex-1">
                            <p className="text-sm text-white font-medium line-clamp-1">{p.name}</p>
                            <p className="text-xs text-blue-400 mt-0.5">
                              {new Intl.NumberFormat('es-CO', {
                                style: 'currency', currency: 'COP', minimumFractionDigits: 0,
                              }).format(p.price)}
                            </p>
                          </div>
                        </button>
                      ))}
                      <div className="border-t border-[#27272A] mt-1 pt-1">
                        <button
                          onClick={() => {
                            router.push(`/${locale}/productos?search=${encodeURIComponent(searchQuery)}`)
                            setSearchOpen(false)
                          }}
                          className="w-full px-4 py-2.5 text-sm text-blue-400
                            hover:text-blue-300 text-left transition-colors flex items-center gap-1"
                        >
                          Ver todos los resultados para &quot;{searchQuery}&quot; →
                        </button>
                      </div>
                    </div>
                  )}
                </div>
              )}
            </div>

            {/* ── Acciones ── */}
            <div className="flex items-center gap-1">
              {/* Búsqueda móvil */}
              <button
                onClick={() => setSearchOpen(!searchOpen)}
                className="md:hidden p-2 text-neutral-400 hover:text-white rounded-full
                  hover:bg-white/5 transition-all duration-200"
                aria-label="Buscar"
              >
                <Search size={20} />
              </button>

              {/* Dark mode toggle */}
              <DarkModeToggle />

              {/* Selector de idioma */}
              <DropdownMenu>
                <DropdownMenuTrigger
                  render={
                    <button
                      className="hidden sm:flex items-center gap-1 p-2 text-neutral-400
                        hover:text-white rounded-full hover:bg-white/5 transition-all duration-200"
                      aria-label="Cambiar idioma"
                    />
                  }
                >
                  <Globe size={18} />
                  <span className="text-xs font-medium uppercase">{locale}</span>
                  <ChevronDown size={12} />
                </DropdownMenuTrigger>
                <DropdownMenuContent align="end" className="bg-[#18181B] border-[#27272A] text-white w-36">
                  {LOCALES.map((l) => (
                    <DropdownMenuItem
                      key={l.code}
                      onClick={() => handleLocaleChange(l.code)}
                      className={cn(
                        'flex items-center gap-2 cursor-pointer hover:bg-white/5',
                        locale === l.code && 'text-blue-400'
                      )}
                    >
                      <span>{l.flag}</span>
                      <span className="text-sm">{l.label}</span>
                      {locale === l.code && <span className="ml-auto text-blue-400">✓</span>}
                    </DropdownMenuItem>
                  ))}
                </DropdownMenuContent>
              </DropdownMenu>

              {/* Usuario */}
              {isAuthenticated ? (
                <DropdownMenu>
                  <DropdownMenuTrigger
                    render={
                      <button
                        className="p-2 text-neutral-400 hover:text-white rounded-full
                          hover:bg-white/5 transition-all duration-200 hidden sm:flex"
                        aria-label="Mi cuenta"
                      />
                    }
                  >
                    <div className="w-7 h-7 rounded-full bg-blue-600/20 border border-blue-600/40
                      flex items-center justify-center">
                      <span className="text-blue-400 text-xs font-bold uppercase">
                        {user?.fullName?.charAt(0) ?? 'U'}
                      </span>
                    </div>
                  </DropdownMenuTrigger>
                  <DropdownMenuContent align="end" className="bg-[#18181B] border-[#27272A] text-white w-48">
                    <div className="px-3 py-2.5 border-b border-[#27272A]">
                      <p className="text-sm font-medium text-white truncate">{user?.fullName}</p>
                      <p className="text-xs text-neutral-500 truncate">{user?.email}</p>
                    </div>
                    <DropdownMenuItem
                      render={<Link href={`/${locale}/cuenta`} />}
                      className="flex items-center gap-2 cursor-pointer hover:bg-white/5"
                    >
                      <User size={14} /> Mi perfil
                    </DropdownMenuItem>
                    <DropdownMenuItem
                      render={<Link href={`/${locale}/cuenta/pedidos`} />}
                      className="flex items-center gap-2 cursor-pointer hover:bg-white/5"
                    >
                      <Package size={14} /> Mis pedidos
                    </DropdownMenuItem>
                    <DropdownMenuItem
                      render={<Link href={`/${locale}/cuenta/wishlist`} />}
                      className="flex items-center gap-2 cursor-pointer hover:bg-white/5"
                    >
                      <Heart size={14} /> Lista de deseos
                    </DropdownMenuItem>
                    <DropdownMenuSeparator className="bg-[#27272A]" />
                    <DropdownMenuItem
                      onClick={logout}
                      className="flex items-center gap-2 cursor-pointer text-red-400
                        hover:bg-red-500/10 hover:text-red-300"
                    >
                      <LogOut size={14} /> Cerrar sesión
                    </DropdownMenuItem>
                  </DropdownMenuContent>
                </DropdownMenu>
              ) : (
                <Link
                  href={`/${locale}/auth/login`}
                  className="hidden sm:flex p-2 text-neutral-400 hover:text-white rounded-full
                    hover:bg-white/5 transition-all duration-200"
                  aria-label="Iniciar sesión"
                >
                  <User size={20} />
                </Link>
              )}

              {/* Carrito con badge */}
              <button
                onClick={toggleCart}
                className="relative p-2 text-neutral-400 hover:text-white rounded-full
                  hover:bg-white/5 transition-all duration-200"
                aria-label={`Carrito, ${itemCount} items`}
              >
                <ShoppingCart size={20} />
                {itemCount > 0 && (
                  <span
                    className="absolute -top-0.5 -right-0.5 min-w-[18px] h-[18px]
                      bg-blue-600 text-white text-[10px] font-bold rounded-full
                      flex items-center justify-center px-1
                      animate-in zoom-in-50 duration-200
                      shadow-[0_0_8px_rgba(37,99,235,0.6)]"
                  >
                    {itemCount > 9 ? '9+' : itemCount}
                  </span>
                )}
              </button>

              {/* Hamburguesa móvil */}
              <button
                onClick={() => setMobileOpen(!mobileOpen)}
                className="lg:hidden p-2 text-neutral-400 hover:text-white rounded-full
                  hover:bg-white/5 transition-all duration-200"
                aria-label="Menú"
              >
                {mobileOpen ? <X size={20} /> : <Menu size={20} />}
              </button>
            </div>
          </div>
        </div>

        {/* ── Menú móvil ── */}
        {mobileOpen && (
          <div
            className="lg:hidden border-t border-[#27272A] bg-[#0A0A0A]
              animate-in slide-in-from-top-2 duration-200"
          >
            {/* Barra de búsqueda móvil */}
            <div className="px-4 pt-4 pb-2">
              <form onSubmit={handleSearchSubmit}>
                <div className="relative flex items-center">
                  <Search size={16} className="absolute left-3.5 text-neutral-500 pointer-events-none" />
                  <input
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    placeholder="Buscar productos..."
                    className="w-full bg-[#27272A] border border-[#3F3F46] text-white text-sm
                      pl-9 pr-4 py-2.5 rounded-full outline-none
                      placeholder:text-neutral-500 focus:border-blue-600"
                  />
                </div>
              </form>
            </div>

            {/* Categorías móvil */}
            <div className="px-2 py-2 space-y-0.5">
              {CATEGORIES.map((cat) => (
                <Link
                  key={cat.slug}
                  href={`/${locale}/productos?category=${cat.slug}`}
                  onClick={() => setMobileOpen(false)}
                  className="flex items-center gap-3 text-neutral-300 hover:text-white text-sm
                    font-medium px-4 py-3 rounded-xl hover:bg-white/5 transition-all"
                >
                  <span className="text-lg">{cat.emoji}</span>
                  {cat.label}
                </Link>
              ))}
            </div>

            {/* Links de usuario móvil */}
            <div className="border-t border-[#27272A] px-2 py-2 space-y-0.5 mb-2">
              {isAuthenticated ? (
                <>
                  <Link
                    href={`/${locale}/cuenta`}
                    onClick={() => setMobileOpen(false)}
                    className="flex items-center gap-3 text-neutral-300 hover:text-white text-sm
                      font-medium px-4 py-3 rounded-xl hover:bg-white/5 transition-all"
                  >
                    <User size={18} /> Mi cuenta
                  </Link>
                  <button
                    onClick={() => { logout(); setMobileOpen(false) }}
                    className="w-full flex items-center gap-3 text-red-400 hover:text-red-300
                      text-sm font-medium px-4 py-3 rounded-xl hover:bg-red-500/10 transition-all"
                  >
                    <LogOut size={18} /> Cerrar sesión
                  </button>
                </>
              ) : (
                <Link
                  href={`/${locale}/auth/login`}
                  onClick={() => setMobileOpen(false)}
                  className="flex items-center gap-3 text-neutral-300 hover:text-white text-sm
                    font-medium px-4 py-3 rounded-xl hover:bg-white/5 transition-all"
                >
                  <User size={18} /> Iniciar sesión
                </Link>
              )}
            </div>
          </div>
        )}
      </nav>

      {/* Spacer para el contenido debajo del nav fixed */}
      <div className="h-16" />
    </>
  )
}
