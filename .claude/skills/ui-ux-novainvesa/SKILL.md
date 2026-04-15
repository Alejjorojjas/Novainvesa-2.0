# Skill: ui-ux-novainvesa

Guía completa de UI/UX para Novainvesa. Tienda e-commerce premium, dark mode, moderna e interactiva.
Stack: Next.js 15 · Tailwind CSS v4 · Shadcn/UI (new-york, zinc) · Framer Motion · Lucide React · Geist font.

---

## PRINCIPIOS DE DISEÑO

1. **Dark-first** — fondo #0A0A0A siempre. No hay modo claro.
2. **Premium con acento azul** — azul #2563EB como color primario, ámbar #F59E0B como acento CTA.
3. **Pill borders** — botones siempre `rounded-full`. Cards `rounded-2xl`.
4. **Micro-interacciones** — cada elemento interactivo tiene feedback visual inmediato.
5. **Mobile-first** — diseñar primero para 375px, luego escalar a desktop.
6. **Performance visual** — Skeleton en cada carga, no spinners solos.

---

## TOKENS DE COLOR (usar siempre estos, no hardcodear otros)

```tsx
// Fondos
bg-[#0A0A0A]     // página principal
bg-[#111111]     // secciones alternas
bg-[#18181B]     // cards, modales
bg-[#27272A]     // inputs, hover de items

// Texto
text-white / text-neutral-50   // texto principal
text-neutral-400               // texto secundario / placeholders
text-neutral-600               // texto muy secundario

// Marca
text-blue-600 / bg-blue-600    // primario #2563EB
text-amber-500 / bg-amber-500  // acento #F59E0B

// Bordes
border-[#27272A]               // borde estándar
border-blue-600/30             // borde de foco sutil
border-blue-600                // borde de foco activo

// Semánticos
text-green-500 / bg-green-500/10   // éxito / stock disponible
text-red-500 / bg-red-500/10       // error / agotado
text-amber-500 / bg-amber-500/10   // advertencia / oferta
```

---

## TIPOGRAFÍA — Geist

```tsx
// app/[locale]/layout.tsx
import { Geist, Geist_Mono } from 'next/font/google'

const geist = Geist({ subsets: ['latin'], variable: '--font-geist' })
const geistMono = Geist_Mono({ subsets: ['latin'], variable: '--font-geist-mono' })

// Aplicar:
<body className={`${geist.variable} ${geistMono.variable} font-sans`}>

// Escala de uso:
// text-5xl font-bold   → Hero display (48px)
// text-4xl font-bold   → H1 páginas (36px)
// text-3xl font-semibold → H2 secciones (30px)
// text-2xl font-semibold → H3 subsecciones / precios (24px)
// text-xl font-medium  → H4 cards (20px)
// text-base            → body principal (16px)
// text-sm              → body secundario (14px)
// text-xs              → badges, captions (12px)
```

---

## COMPONENTES BASE — Shadcn/UI customizados

### Botón primario (azul pill)
```tsx
<Button className="bg-blue-600 hover:bg-blue-700 text-white rounded-full px-6 py-2.5
  font-medium transition-all duration-200
  hover:shadow-[0_0_20px_rgba(37,99,235,0.5)]
  active:scale-95">
  Agregar al carrito
</Button>
```

### Botón CTA (ámbar — máxima conversión)
```tsx
<Button className="bg-amber-500 hover:bg-amber-400 text-black rounded-full px-8 py-3
  font-semibold text-base transition-all duration-200
  hover:scale-105 hover:shadow-[0_0_25px_rgba(245,158,11,0.5)]
  active:scale-95">
  Comprar ahora →
</Button>
```

### Botón outline (secundario)
```tsx
<Button variant="outline" className="rounded-full border-[#27272A] text-white
  hover:bg-white/5 hover:border-white/30 transition-all duration-200">
  Ver catálogo
</Button>
```

### Botón WhatsApp
```tsx
<Button className="bg-[#25D366] hover:bg-[#20BA5A] text-white rounded-full px-6 py-2.5
  font-medium gap-2 transition-all duration-200 hover:scale-105">
  <MessageCircle size={18} />
  Consultar por WhatsApp
</Button>
```

### Input estándar
```tsx
<div className="space-y-1.5">
  <Label className="text-sm font-medium text-neutral-200">
    {label} {required && <span className="text-red-500">*</span>}
  </Label>
  <Input className="rounded-xl bg-[#27272A] border-[#3F3F46] text-white
    placeholder:text-neutral-500 focus:border-blue-600 focus:ring-2
    focus:ring-blue-600/20 transition-all duration-200" />
  {error && <p className="text-xs text-red-400 flex items-center gap-1">
    <AlertCircle size={12} /> {error}
  </p>}
</div>
```

### Badge de descuento
```tsx
<span className="bg-amber-500 text-black text-xs font-bold px-2 py-0.5 rounded-full">
  -{discount}%
</span>
```

### Badge de estado de pedido
```tsx
const orderStatusConfig = {
  PENDING:    { label: 'Pendiente',    class: 'bg-neutral-700/50 text-neutral-300 border border-neutral-600' },
  CONFIRMED:  { label: 'Confirmado',   class: 'bg-blue-500/10 text-blue-400 border border-blue-500/30' },
  PROCESSING: { label: 'En proceso',   class: 'bg-amber-500/10 text-amber-400 border border-amber-500/30' },
  SHIPPED:    { label: 'En camino',    class: 'bg-purple-500/10 text-purple-400 border border-purple-500/30' },
  DELIVERED:  { label: 'Entregado',    class: 'bg-green-500/10 text-green-400 border border-green-500/30' },
  CANCELLED:  { label: 'Cancelado',    class: 'bg-red-500/10 text-red-400 border border-red-500/30' },
  RETURNED:   { label: 'Devuelto',     class: 'bg-orange-500/10 text-orange-400 border border-orange-500/30' },
}

<span className={`text-xs font-medium px-3 py-1 rounded-full ${orderStatusConfig[status].class}`}>
  {orderStatusConfig[status].label}
</span>
```

---

## PRODUCT CARD — Componente estrella

```tsx
'use client'
import Image from 'next/image'
import Link from 'next/link'
import { Heart, ShoppingCart } from 'lucide-react'
import { useState } from 'react'

interface ProductCardProps {
  id: number
  slug: string
  name: string
  price: number
  compareAtPrice?: number | null
  image: string | null
  categorySlug?: string
  inStock: boolean
  featured?: boolean
  locale: string
}

export function ProductCard({ id, slug, name, price, compareAtPrice,
  image, inStock, locale }: ProductCardProps) {

  const [wishlisted, setWishlisted] = useState(false)
  const [addingToCart, setAddingToCart] = useState(false)

  const discount = compareAtPrice && compareAtPrice > price
    ? Math.round((1 - price / compareAtPrice) * 100) : null

  const handleAddToCart = async (e: React.MouseEvent) => {
    e.preventDefault()
    e.stopPropagation()
    setAddingToCart(true)
    // llamar a cartStore.addItem(...)
    setTimeout(() => setAddingToCart(false), 1000)
  }

  return (
    <Link href={`/${locale}/producto/${slug}`}>
      <div className="group relative bg-[#18181B] border border-[#27272A] rounded-2xl
        overflow-hidden transition-all duration-300 cursor-pointer
        hover:border-blue-600/40 hover:-translate-y-1
        hover:shadow-[0_8px_30px_rgba(37,99,235,0.15)]">

        {/* Imagen */}
        <div className="relative aspect-square overflow-hidden bg-[#111111]">
          {image ? (
            <Image src={image} alt={name} fill className="object-cover
              transition-transform duration-500 group-hover:scale-110" />
          ) : (
            <div className="w-full h-full flex items-center justify-center">
              <ShoppingCart size={48} className="text-neutral-700" />
            </div>
          )}

          {/* Overlay degradado en hover */}
          <div className="absolute inset-0 bg-gradient-to-t from-black/60 via-transparent to-transparent
            opacity-0 group-hover:opacity-100 transition-opacity duration-300" />

          {/* Badges */}
          <div className="absolute top-2 left-2 flex flex-col gap-1">
            {discount && (
              <span className="bg-amber-500 text-black text-xs font-bold px-2.5 py-1 rounded-full">
                -{discount}%
              </span>
            )}
            {!inStock && (
              <span className="bg-neutral-800/90 text-neutral-300 text-xs font-medium px-2.5 py-1 rounded-full backdrop-blur-sm">
                Agotado
              </span>
            )}
          </div>

          {/* Wishlist button */}
          <button
            onClick={(e) => { e.preventDefault(); setWishlisted(!wishlisted) }}
            className="absolute top-2 right-2 p-2 rounded-full bg-black/40 backdrop-blur-sm
              text-white transition-all duration-200 hover:bg-black/60
              opacity-0 group-hover:opacity-100 hover:scale-110">
            <Heart size={16} className={wishlisted ? 'fill-red-500 text-red-500' : ''} />
          </button>

          {/* Add to cart — aparece en hover */}
          {inStock && (
            <div className="absolute bottom-2 left-2 right-2
              translate-y-2 opacity-0 group-hover:translate-y-0 group-hover:opacity-100
              transition-all duration-300">
              <button
                onClick={handleAddToCart}
                className="w-full bg-blue-600 hover:bg-blue-500 text-white text-sm font-medium
                  py-2 rounded-full transition-all duration-200
                  flex items-center justify-center gap-2">
                {addingToCart ? (
                  <span className="flex items-center gap-2">
                    <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                    Agregando...
                  </span>
                ) : (
                  <><ShoppingCart size={14} /> Agregar al carrito</>
                )}
              </button>
            </div>
          )}
        </div>

        {/* Info */}
        <div className="p-4">
          <h3 className="text-sm font-medium text-white line-clamp-2 leading-snug
            group-hover:text-blue-400 transition-colors duration-200">
            {name}
          </h3>
          <div className="flex items-center gap-2 mt-2.5">
            <span className="text-lg font-bold text-white">
              {new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP',
                minimumFractionDigits: 0 }).format(price)}
            </span>
            {compareAtPrice && (
              <span className="text-sm text-neutral-500 line-through">
                {new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP',
                  minimumFractionDigits: 0 }).format(compareAtPrice)}
              </span>
            )}
          </div>
        </div>
      </div>
    </Link>
  )
}
```

### Skeleton de ProductCard
```tsx
import { Skeleton } from '@/components/ui/skeleton'

export function ProductCardSkeleton() {
  return (
    <div className="bg-[#18181B] border border-[#27272A] rounded-2xl overflow-hidden">
      <Skeleton className="aspect-square w-full bg-[#27272A]" />
      <div className="p-4 space-y-2.5">
        <Skeleton className="h-4 w-full bg-[#27272A]" />
        <Skeleton className="h-4 w-2/3 bg-[#27272A]" />
        <Skeleton className="h-6 w-1/2 bg-[#27272A] mt-1" />
      </div>
    </div>
  )
}

// Grid de skeletons para loading state:
export function ProductGridSkeleton({ count = 8 }: { count?: number }) {
  return (
    <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4 md:gap-6">
      {Array.from({ length: count }).map((_, i) => (
        <ProductCardSkeleton key={i} />
      ))}
    </div>
  )
}
```

---

## HERO SECTION — Diseño premium

```tsx
export function HeroSection({ locale }: { locale: string }) {
  return (
    <section className="relative min-h-[85vh] flex items-center overflow-hidden
      bg-[#0A0A0A]">

      {/* Fondo con gradiente y orbes decorativos */}
      <div className="absolute inset-0">
        <div className="absolute top-0 right-0 w-[600px] h-[600px] rounded-full
          bg-blue-600/10 blur-[120px] pointer-events-none" />
        <div className="absolute bottom-0 left-1/4 w-[400px] h-[400px] rounded-full
          bg-amber-500/5 blur-[100px] pointer-events-none" />
        {/* Grid pattern sutil */}
        <div className="absolute inset-0 opacity-[0.03]"
          style={{ backgroundImage: 'linear-gradient(#fff 1px, transparent 1px), linear-gradient(90deg, #fff 1px, transparent 1px)',
            backgroundSize: '60px 60px' }} />
      </div>

      <div className="container mx-auto px-4 relative z-10">
        <div className="max-w-3xl">
          {/* Badge de valor */}
          <div className="inline-flex items-center gap-2 bg-amber-500/10 border border-amber-500/20
            text-amber-400 text-sm font-medium px-4 py-2 rounded-full mb-6
            animate-in fade-in slide-in-from-bottom-4 duration-500">
            <span className="w-2 h-2 bg-amber-500 rounded-full animate-pulse" />
            🚀 Envíos gratis a toda Colombia
          </div>

          {/* Título con gradiente */}
          <h1 className="text-5xl md:text-6xl lg:text-7xl font-bold text-white leading-[1.1]
            animate-in fade-in slide-in-from-bottom-6 duration-700 delay-100">
            Todo lo que necesitas,{' '}
            <span className="bg-gradient-to-r from-blue-400 to-amber-400
              bg-clip-text text-transparent">
              en un solo lugar.
            </span>
          </h1>

          <p className="text-lg md:text-xl text-neutral-400 mt-6 leading-relaxed max-w-xl
            animate-in fade-in slide-in-from-bottom-6 duration-700 delay-200">
            Productos de calidad con entrega rápida. Más de 500 referencias
            en electrónica, hogar, mascotas, fitness y belleza.
          </p>

          {/* CTAs */}
          <div className="flex flex-col sm:flex-row gap-4 mt-10
            animate-in fade-in slide-in-from-bottom-6 duration-700 delay-300">
            <Link href={`/${locale}/productos`}>
              <button className="bg-amber-500 hover:bg-amber-400 text-black font-semibold
                px-8 py-4 rounded-full text-base transition-all duration-200
                hover:scale-105 hover:shadow-[0_0_30px_rgba(245,158,11,0.4)]
                flex items-center gap-2 group">
                Comprar ahora
                <ArrowRight size={18} className="transition-transform group-hover:translate-x-1" />
              </button>
            </Link>
            <Link href={`/${locale}/productos`}>
              <button className="border border-[#3F3F46] hover:border-white/30 text-white
                font-medium px-8 py-4 rounded-full text-base transition-all duration-200
                hover:bg-white/5 backdrop-blur-sm">
                Ver catálogo
              </button>
            </Link>
          </div>

          {/* Social proof */}
          <div className="flex items-center gap-6 mt-12
            animate-in fade-in duration-700 delay-500">
            <div className="flex -space-x-2">
              {[1,2,3,4].map(i => (
                <div key={i} className="w-8 h-8 rounded-full bg-gradient-to-br
                  from-blue-400 to-blue-600 border-2 border-[#0A0A0A]" />
              ))}
            </div>
            <div>
              <p className="text-white font-semibold text-sm">+2,000 clientes felices</p>
              <div className="flex items-center gap-1 mt-0.5">
                {[1,2,3,4,5].map(i => (
                  <Star key={i} size={12} className="fill-amber-400 text-amber-400" />
                ))}
                <span className="text-xs text-neutral-400 ml-1">4.9 / 5</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  )
}
```

---

## NAVBAR — Sticky con blur

```tsx
'use client'
import { useEffect, useState } from 'react'
import Link from 'next/link'
import { ShoppingCart, Search, Menu, X, User } from 'lucide-react'
import { useUIStore } from '@/lib/store/uiStore'
import { useCartStore } from '@/lib/store/cartStore'

export function Navbar({ locale }: { locale: string }) {
  const [scrolled, setScrolled] = useState(false)
  const [mobileOpen, setMobileOpen] = useState(false)
  const { toggleCart } = useUIStore()
  const itemCount = useCartStore(s => s.getItemCount())

  useEffect(() => {
    const handler = () => setScrolled(window.scrollY > 20)
    window.addEventListener('scroll', handler)
    return () => window.removeEventListener('scroll', handler)
  }, [])

  return (
    <nav className={`fixed top-0 left-0 right-0 z-50 transition-all duration-300
      ${scrolled
        ? 'bg-[#0A0A0A]/90 backdrop-blur-xl border-b border-[#27272A] shadow-[0_4px_30px_rgba(0,0,0,0.3)]'
        : 'bg-transparent'}`}>
      <div className="container mx-auto px-4">
        <div className="flex items-center justify-between h-16 md:h-18">

          {/* Logo */}
          <Link href={`/${locale}`} className="flex items-center gap-1 shrink-0">
            <span className="text-xl font-bold">
              <span className="text-blue-500">NOVA</span>
              <span className="text-white">INVESA</span>
            </span>
          </Link>

          {/* Links desktop */}
          <div className="hidden md:flex items-center gap-1">
            {['Mascotas', 'Hogar', 'Tecnología', 'Belleza', 'Fitness'].map(cat => (
              <Link key={cat}
                href={`/${locale}/productos?category=${cat.toLowerCase()}`}
                className="text-neutral-400 hover:text-white text-sm font-medium
                  px-3 py-2 rounded-lg hover:bg-white/5 transition-all duration-200">
                {cat}
              </Link>
            ))}
          </div>

          {/* Acciones */}
          <div className="flex items-center gap-2">
            {/* Búsqueda */}
            <button className="p-2 text-neutral-400 hover:text-white rounded-full
              hover:bg-white/5 transition-all duration-200">
              <Search size={20} />
            </button>

            {/* Usuario */}
            <Link href={`/${locale}/cuenta`}
              className="p-2 text-neutral-400 hover:text-white rounded-full
              hover:bg-white/5 transition-all duration-200 hidden sm:flex">
              <User size={20} />
            </Link>

            {/* Carrito con badge */}
            <button onClick={toggleCart}
              className="relative p-2 text-neutral-400 hover:text-white rounded-full
              hover:bg-white/5 transition-all duration-200">
              <ShoppingCart size={20} />
              {itemCount > 0 && (
                <span className="absolute -top-0.5 -right-0.5 w-5 h-5 bg-blue-600
                  text-white text-xs font-bold rounded-full flex items-center justify-center
                  animate-in zoom-in-50 duration-200">
                  {itemCount > 9 ? '9+' : itemCount}
                </span>
              )}
            </button>

            {/* Hamburger móvil */}
            <button onClick={() => setMobileOpen(!mobileOpen)}
              className="md:hidden p-2 text-neutral-400 hover:text-white rounded-full
              hover:bg-white/5 transition-all duration-200">
              {mobileOpen ? <X size={20} /> : <Menu size={20} />}
            </button>
          </div>
        </div>

        {/* Menú móvil */}
        {mobileOpen && (
          <div className="md:hidden border-t border-[#27272A] py-4 space-y-1
            animate-in slide-in-from-top-2 duration-200">
            {['Mascotas', 'Hogar', 'Tecnología', 'Belleza', 'Fitness'].map(cat => (
              <Link key={cat}
                href={`/${locale}/productos?category=${cat.toLowerCase()}`}
                onClick={() => setMobileOpen(false)}
                className="block text-neutral-300 hover:text-white text-sm font-medium
                  px-4 py-2.5 rounded-lg hover:bg-white/5 transition-all">
                {cat}
              </Link>
            ))}
          </div>
        )}
      </div>
    </nav>
  )
}
```

---

## CART DRAWER — Sheet lateral

```tsx
'use client'
import { Sheet, SheetContent, SheetHeader, SheetTitle } from '@/components/ui/sheet'
import { useCartStore } from '@/lib/store/cartStore'
import { useUIStore } from '@/lib/store/uiStore'
import { Trash2, Plus, Minus, ShoppingBag, ArrowRight } from 'lucide-react'
import Image from 'next/image'
import Link from 'next/link'

export function CartDrawer({ locale }: { locale: string }) {
  const { cartOpen, closeCart } = useUIStore()
  const { items, removeItem, updateQuantity, getTotal, getItemCount } = useCartStore()
  const total = getTotal()

  return (
    <Sheet open={cartOpen} onOpenChange={closeCart}>
      <SheetContent className="bg-[#111111] border-l border-[#27272A] text-white
        flex flex-col w-full sm:max-w-md p-0">

        {/* Header */}
        <SheetHeader className="px-6 py-5 border-b border-[#27272A]">
          <SheetTitle className="text-white flex items-center gap-2">
            <ShoppingBag size={20} className="text-blue-500" />
            Carrito ({getItemCount()} {getItemCount() === 1 ? 'item' : 'items'})
          </SheetTitle>
        </SheetHeader>

        {/* Items */}
        <div className="flex-1 overflow-y-auto px-6 py-4 space-y-4">
          {items.length === 0 ? (
            <div className="flex flex-col items-center justify-center h-full
              text-center py-16 gap-4">
              <ShoppingBag size={56} className="text-neutral-700" />
              <div>
                <p className="text-neutral-300 font-medium">Tu carrito está vacío</p>
                <p className="text-neutral-500 text-sm mt-1">Agrega productos para comenzar</p>
              </div>
              <button onClick={closeCart}
                className="bg-blue-600 hover:bg-blue-500 text-white px-6 py-2.5
                rounded-full text-sm font-medium transition-all duration-200
                hover:shadow-[0_0_20px_rgba(37,99,235,0.4)] mt-2">
                Explorar productos
              </button>
            </div>
          ) : (
            items.map(item => (
              <div key={item.productId}
                className="flex gap-4 bg-[#18181B] rounded-xl p-3 border border-[#27272A]
                  transition-all duration-200 hover:border-[#3F3F46]">
                {/* Imagen */}
                <div className="relative w-20 h-20 rounded-lg overflow-hidden bg-[#27272A] shrink-0">
                  {item.image && (
                    <Image src={item.image} alt={item.name} fill className="object-cover" />
                  )}
                </div>

                {/* Info */}
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-white line-clamp-2 leading-snug">
                    {item.name}
                  </p>
                  <p className="text-blue-400 font-semibold mt-1.5">
                    {new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP',
                      minimumFractionDigits: 0 }).format(item.price)}
                  </p>

                  {/* Cantidad + eliminar */}
                  <div className="flex items-center justify-between mt-2.5">
                    <div className="flex items-center gap-2 bg-[#27272A] rounded-full p-1">
                      <button onClick={() => updateQuantity(item.productId, item.quantity - 1)}
                        className="w-6 h-6 flex items-center justify-center rounded-full
                          hover:bg-white/10 transition-colors text-neutral-300 hover:text-white">
                        <Minus size={12} />
                      </button>
                      <span className="text-sm font-medium text-white w-5 text-center">
                        {item.quantity}
                      </span>
                      <button onClick={() => updateQuantity(item.productId, item.quantity + 1)}
                        className="w-6 h-6 flex items-center justify-center rounded-full
                          hover:bg-white/10 transition-colors text-neutral-300 hover:text-white">
                        <Plus size={12} />
                      </button>
                    </div>
                    <button onClick={() => removeItem(item.productId)}
                      className="text-neutral-600 hover:text-red-400 transition-colors p-1">
                      <Trash2 size={14} />
                    </button>
                  </div>
                </div>
              </div>
            ))
          )}
        </div>

        {/* Footer con total y checkout */}
        {items.length > 0 && (
          <div className="px-6 py-5 border-t border-[#27272A] space-y-4">
            <div className="flex items-center justify-between">
              <span className="text-neutral-400">Subtotal</span>
              <span className="text-white font-bold text-lg">
                {new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP',
                  minimumFractionDigits: 0 }).format(total)}
              </span>
            </div>
            <div className="flex items-center gap-2 text-green-400 text-sm">
              <span className="w-2 h-2 bg-green-400 rounded-full animate-pulse" />
              Envío gratis incluido
            </div>
            <Link href={`/${locale}/checkout`} onClick={closeCart}
              className="flex items-center justify-center gap-2 w-full bg-blue-600
              hover:bg-blue-500 text-white font-semibold py-3.5 rounded-full
              transition-all duration-200 hover:shadow-[0_0_25px_rgba(37,99,235,0.5)]
              group">
              Ir al checkout
              <ArrowRight size={18} className="transition-transform group-hover:translate-x-1" />
            </Link>
          </div>
        )}
      </SheetContent>
    </Sheet>
  )
}
```

---

## FEATURES STRIP — Propuesta de valor

```tsx
const features = [
  { icon: Truck,      title: 'Envío gratis',     desc: 'A toda Colombia' },
  { icon: Shield,     title: 'Compra segura',     desc: 'Pagos cifrados SSL' },
  { icon: RotateCcw,  title: 'Garantía 30 días',  desc: 'Sin preguntas' },
  { icon: Phone,      title: 'Soporte 24/7',      desc: 'Por WhatsApp' },
]

export function FeaturesStrip() {
  return (
    <section className="border-y border-[#27272A] bg-[#111111]">
      <div className="container mx-auto px-4">
        <div className="grid grid-cols-2 lg:grid-cols-4 divide-x divide-y lg:divide-y-0 divide-[#27272A]">
          {features.map(({ icon: Icon, title, desc }) => (
            <div key={title} className="flex items-center gap-3 px-6 py-5
              hover:bg-white/[0.02] transition-colors duration-200">
              <div className="w-10 h-10 rounded-xl bg-blue-600/10 flex items-center
                justify-center shrink-0">
                <Icon size={20} className="text-blue-400" />
              </div>
              <div>
                <p className="text-white text-sm font-semibold">{title}</p>
                <p className="text-neutral-500 text-xs">{desc}</p>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  )
}
```

---

## SEARCH BAR — Con debounce

```tsx
'use client'
import { useState, useEffect, useRef } from 'react'
import { Search, X, Loader2 } from 'lucide-react'
import { useRouter } from 'next/navigation'
import api from '@/lib/api'

export function SearchBar({ locale }: { locale: string }) {
  const [query, setQuery] = useState('')
  const [results, setResults] = useState<any[]>([])
  const [loading, setLoading] = useState(false)
  const [open, setOpen] = useState(false)
  const router = useRouter()
  const ref = useRef<HTMLDivElement>(null)

  // Debounce
  useEffect(() => {
    if (query.length < 2) { setResults([]); return }
    const timer = setTimeout(async () => {
      setLoading(true)
      try {
        const res = await api.get(`/api/v1/products/search?q=${encodeURIComponent(query)}&limit=5`)
        setResults(res.data.data ?? [])
      } catch { setResults([]) }
      finally { setLoading(false) }
    }, 300)
    return () => clearTimeout(timer)
  }, [query])

  // Cerrar al click afuera
  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false)
    }
    document.addEventListener('mousedown', handler)
    return () => document.removeEventListener('mousedown', handler)
  }, [])

  return (
    <div ref={ref} className="relative w-full max-w-xl">
      <div className="relative flex items-center">
        <Search size={18} className="absolute left-4 text-neutral-500 pointer-events-none" />
        <input
          value={query}
          onChange={e => { setQuery(e.target.value); setOpen(true) }}
          onFocus={() => setOpen(true)}
          placeholder="Buscar productos..."
          className="w-full bg-[#27272A] border border-[#3F3F46] text-white text-sm
            pl-10 pr-10 py-2.5 rounded-full outline-none
            focus:border-blue-600 focus:ring-2 focus:ring-blue-600/20
            placeholder:text-neutral-500 transition-all duration-200" />
        {query && (
          <button onClick={() => { setQuery(''); setResults([]) }}
            className="absolute right-3 text-neutral-500 hover:text-white transition-colors">
            <X size={16} />
          </button>
        )}
      </div>

      {/* Dropdown de resultados */}
      {open && (query.length >= 2) && (
        <div className="absolute top-full left-0 right-0 mt-2 bg-[#18181B] border border-[#27272A]
          rounded-2xl overflow-hidden shadow-[0_8px_30px_rgba(0,0,0,0.5)] z-50
          animate-in fade-in-0 slide-in-from-top-2 duration-150">
          {loading ? (
            <div className="flex items-center justify-center py-6 gap-2 text-neutral-500 text-sm">
              <Loader2 size={16} className="animate-spin" />
              Buscando...
            </div>
          ) : results.length === 0 ? (
            <div className="py-6 text-center text-neutral-500 text-sm">
              Sin resultados para "{query}"
            </div>
          ) : (
            <div className="py-2">
              {results.map((p: any) => (
                <button key={p.slug} onClick={() => {
                  router.push(`/${locale}/producto/${p.slug}`)
                  setOpen(false); setQuery('')
                }}
                  className="w-full flex items-center gap-3 px-4 py-3
                    hover:bg-white/5 transition-colors text-left">
                  <div className="w-10 h-10 rounded-lg bg-[#27272A] overflow-hidden shrink-0">
                    {p.primaryImage && <img src={p.primaryImage} className="w-full h-full object-cover" alt="" />}
                  </div>
                  <div className="min-w-0">
                    <p className="text-sm text-white font-medium line-clamp-1">{p.name}</p>
                    <p className="text-xs text-neutral-500">
                      {new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP',
                        minimumFractionDigits: 0 }).format(p.price)}
                    </p>
                  </div>
                </button>
              ))}
              <div className="border-t border-[#27272A] mt-1 pt-1">
                <button onClick={() => {
                  router.push(`/${locale}/productos?search=${encodeURIComponent(query)}`)
                  setOpen(false)
                }}
                  className="w-full px-4 py-2.5 text-sm text-blue-400 hover:text-blue-300
                    text-left transition-colors">
                  Ver todos los resultados para "{query}" →
                </button>
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  )
}
```

---

## TOAST — Notificaciones con Sonner

```tsx
// Instalar: npm install sonner
// En layout.tsx agregar: <Toaster richColors theme="dark" position="bottom-right" />

import { toast } from 'sonner'

// Producto agregado al carrito
toast.success('Agregado al carrito', {
  description: `${productName} × ${quantity}`,
  duration: 2000,
  action: { label: 'Ver carrito', onClick: openCart },
})

// Error de stock
toast.error('Sin stock disponible', {
  description: 'Este producto está agotado',
})

// Pedido confirmado
toast.success('¡Pedido confirmado!', {
  description: `Código: ${orderCode}`,
  duration: 6000,
  action: { label: 'Rastrear', onClick: () => router.push(`/${locale}/rastrear?order=${orderCode}`) },
})

// Loading de proceso largo
const toastId = toast.loading('Procesando tu pedido...')
// Después:
toast.success('¡Pedido creado!', { id: toastId })
```

---

## TRACKING PAGE — Timeline visual

```tsx
const steps = [
  { key: 'RECEIVED', icon: Package,     label: 'Pedido recibido' },
  { key: 'CONFIRMED', icon: CreditCard, label: 'Pago confirmado' },
  { key: 'SHIPPED',   icon: Truck,      label: 'En camino' },
  { key: 'DELIVERED', icon: CheckCircle2, label: 'Entregado' },
]

export function TrackingTimeline({ events }: { events: TrackingEvent[] }) {
  return (
    <div className="relative">
      {/* Línea de progreso */}
      <div className="absolute left-5 top-5 bottom-5 w-0.5 bg-[#27272A]" />

      <div className="space-y-8">
        {steps.map(({ key, icon: Icon, label }) => {
          const event = events.find(e => e.type === key)
          const completed = event?.completed ?? false
          return (
            <div key={key} className="relative flex gap-4">
              {/* Círculo indicador */}
              <div className={`relative z-10 w-10 h-10 rounded-full flex items-center
                justify-center border-2 shrink-0 transition-all duration-500
                ${completed
                  ? 'bg-blue-600 border-blue-600 shadow-[0_0_15px_rgba(37,99,235,0.5)]'
                  : 'bg-[#18181B] border-[#3F3F46]'}`}>
                <Icon size={18} className={completed ? 'text-white' : 'text-neutral-600'} />
              </div>

              {/* Texto */}
              <div className="flex-1 pb-2">
                <p className={`font-semibold ${completed ? 'text-white' : 'text-neutral-600'}`}>
                  {label}
                </p>
                {event?.description && (
                  <p className="text-sm text-neutral-500 mt-0.5">{event.description}</p>
                )}
                {completed && event?.completedAt && (
                  <p className="text-xs text-neutral-600 mt-1">
                    {new Intl.DateTimeFormat('es-CO', {
                      dateStyle: 'medium', timeStyle: 'short'
                    }).format(new Date(event.completedAt))}
                  </p>
                )}
              </div>
            </div>
          )
        })}
      </div>
    </div>
  )
}
```

---

## EMPTY STATES — Cuando no hay contenido

```tsx
// Sin resultados de búsqueda
<div className="flex flex-col items-center justify-center py-24 gap-4 text-center">
  <div className="w-20 h-20 rounded-full bg-[#18181B] flex items-center justify-center">
    <Search size={32} className="text-neutral-700" />
  </div>
  <div>
    <p className="text-white font-semibold text-lg">Sin resultados</p>
    <p className="text-neutral-500 text-sm mt-1">Intenta con otras palabras clave</p>
  </div>
  <button onClick={clearSearch}
    className="text-blue-400 hover:text-blue-300 text-sm transition-colors">
    Limpiar búsqueda
  </button>
</div>

// Wishlist vacía
<div className="flex flex-col items-center justify-center py-24 gap-4 text-center">
  <Heart size={48} className="text-neutral-700" />
  <p className="text-white font-semibold">Tu lista de deseos está vacía</p>
  <p className="text-neutral-500 text-sm">Guarda los productos que te gusten</p>
</div>
```

---

## RESPONSIVE — Reglas grid

```tsx
// Grid de productos
<div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-3 sm:gap-4 lg:gap-6">

// Checkout layout
<div className="grid grid-cols-1 lg:grid-cols-[1fr_400px] gap-8">
  <div>{/* Formulario */}</div>
  <div className="lg:sticky lg:top-24 h-fit">{/* Resumen */}</div>
</div>

// Hero
<div className="grid grid-cols-1 lg:grid-cols-2 items-center gap-12">

// Admin dashboard
<div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
```

---

## ANIMACIONES — Clases de entrada

```tsx
// Usadas con Tailwind animate-in (integrado en Shadcn)
'animate-in fade-in-0 duration-300'                    // fade suave
'animate-in fade-in slide-in-from-bottom-4 duration-500'  // desde abajo
'animate-in slide-in-from-right duration-300'           // desde derecha (drawer)
'animate-in zoom-in-95 duration-200'                    // zoom modal
'animate-in fade-in-0 slide-in-from-top-2 duration-150' // dropdown

// Stagger — para listas (usar delay incremental)
items.map((item, i) => (
  <div key={i} style={{ animationDelay: `${i * 75}ms` }}
    className="animate-in fade-in slide-in-from-bottom-4 duration-500">
    ...
  </div>
))

// Hover suaves en cards
'transition-all duration-300 hover:-translate-y-1 hover:scale-[1.02]'

// Spinner de carga inline
<span className="w-4 h-4 border-2 border-white/20 border-t-white rounded-full animate-spin" />
```

---

## ACCESIBILIDAD — Checklist obligatorio

```tsx
// ✅ Imágenes — siempre alt descriptivo
<Image alt={`Foto de ${productName}`} ... />

// ✅ Botones de solo ícono — aria-label obligatorio
<button aria-label="Cerrar carrito"><X /></button>
<button aria-label={`Eliminar ${item.name} del carrito`}><Trash2 /></button>

// ✅ Focus visible — no remover outline sin reemplazar
// En globals.css:
// *:focus-visible { outline: 2px solid #2563EB; outline-offset: 2px; }

// ✅ Loading states — anunciar a screen readers
<div role="status" aria-live="polite">
  {loading && <span className="sr-only">Cargando productos...</span>}
</div>

// ✅ Estructura semántica
<main>, <nav>, <section aria-labelledby="...">, <article>, <header>, <footer>

// ✅ Skip to content
<a href="#main-content"
  className="sr-only focus:not-sr-only focus:fixed focus:top-4 focus:left-4
    focus:z-[100] focus:bg-blue-600 focus:text-white focus:px-4 focus:py-2 focus:rounded-full">
  Saltar al contenido
</a>
```

---

## FORMATEO — Helpers estándar

```tsx
// Precio COP
export const formatPrice = (n: number) =>
  new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', minimumFractionDigits: 0 }).format(n)
// → "$1.250.000"

// Fecha en español
export const formatDate = (d: string | Date) =>
  new Intl.DateTimeFormat('es-CO', { dateStyle: 'long' }).format(new Date(d))
// → "13 de abril de 2026"

// Truncar texto
export const truncate = (s: string, n: number) =>
  s.length > n ? s.slice(0, n).trimEnd() + '…' : s

// Descuento
export const calcDiscount = (price: number, compare: number | null) =>
  compare && compare > price ? Math.round((1 - price / compare) * 100) : null
```

---

## REGLAS DE ORO

1. **Dark mode siempre** — `bg-[#0A0A0A]` en body, `#18181B` en cards. Nunca `bg-white`.
2. **Skeleton en cada carga** — nunca dejar contenido vacío sin feedback.
3. **`'use client'` solo donde hay interactividad** — hooks, eventos, stores.
4. **Botones con feedback inmediato** — `active:scale-95` + estado de loading.
5. **Imágenes con `fill` + `object-cover`** — siempre dentro de un contenedor con `relative` y aspecto definido.
6. **Hover en cards** — siempre `-translate-y-1` + `shadow-[0_8px_30px_rgba(37,99,235,0.15)]`.
7. **Textos secundarios** — `text-neutral-400`, no `text-gray-*`.
8. **Links internos** — siempre con `/${locale}/ruta`, nunca hardcoded `/es/`.
9. **Errores de formulario** — rojo, texto pequeño, con ícono `AlertCircle size={12}`.
10. **Precio siempre con `Intl.NumberFormat`** — nunca concatenación manual de `$`.
