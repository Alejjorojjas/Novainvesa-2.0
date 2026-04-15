# Skill: nextjs-patterns

Patrones estándar de Next.js 15 App Router para Novainvesa.

## Estructura de carpetas

```
Frontend/
├── app/
│   └── [locale]/           ← i18n obligatorio en TODAS las páginas
│       ├── layout.tsx       ← layout con locale
│       ├── page.tsx         ← home
│       ├── producto/
│       │   └── [slug]/
│       │       └── page.tsx
│       ├── carrito/page.tsx
│       ├── checkout/page.tsx
│       ├── cuenta/page.tsx
│       └── admin/
│           └── productos/
│               └── importar/page.tsx
├── components/
│   ├── ui/                  ← Shadcn/UI (no tocar)
│   ├── layout/              ← Navbar, Footer, etc.
│   ├── product/             ← ProductCard, ProductGallery, etc.
│   ├── cart/                ← CartDrawer, CartItem, etc.
│   ├── checkout/            ← CheckoutForm, PaymentSelector, etc.
│   └── admin/               ← ImportadorInput, ProductoPreview, etc.
├── lib/
│   ├── axios.ts             ← instancia configurada de Axios
│   ├── utils.ts             ← cn(), formatPrice(), etc.
│   └── store/               ← Zustand stores
│       ├── cartStore.ts
│       └── authStore.ts
└── public/
    └── locales/             ← archivos de traducción
        ├── es.json
        ├── en.json
        └── pt.json
```

## Page con i18n — patrón estándar

```tsx
// app/[locale]/producto/[slug]/page.tsx
import { useTranslations } from 'next-intl'
import { unstable_setRequestLocale } from 'next-intl/server'

interface Props {
  params: { locale: string; slug: string }
}

export async function generateMetadata({ params }: Props) {
  // ver skill: seo-metadata
}

export default function ProductPage({ params }: Props) {
  unstable_setRequestLocale(params.locale)
  const t = useTranslations('product')

  return (
    <main>
      {/* ... */}
    </main>
  )
}
```

## Componente cliente con Zustand

```tsx
'use client'

import { useCartStore } from '@/lib/store/cartStore'
import { Button } from '@/components/ui/button'

export function AddToCartButton({ productId, price }: { productId: number; price: number }) {
  const addItem = useCartStore(state => state.addItem)

  return (
    <Button onClick={() => addItem({ productId, price, quantity: 1 })}>
      Agregar al carrito
    </Button>
  )
}
```

## Zustand store — patrón estándar

```tsx
// lib/store/cartStore.ts
import { create } from 'zustand'
import { persist } from 'zustand/middleware'

interface CartItem {
  productId: number
  price: number
  quantity: number
}

interface CartState {
  items: CartItem[]
  addItem: (item: CartItem) => void
  removeItem: (productId: number) => void
  clear: () => void
  total: () => number
}

export const useCartStore = create<CartState>()(
  persist(
    (set, get) => ({
      items: [],
      addItem: (item) => set(state => ({
        items: state.items.some(i => i.productId === item.productId)
          ? state.items.map(i => i.productId === item.productId
              ? { ...i, quantity: i.quantity + item.quantity }
              : i)
          : [...state.items, item]
      })),
      removeItem: (productId) => set(state => ({
        items: state.items.filter(i => i.productId !== productId)
      })),
      clear: () => set({ items: [] }),
      total: () => get().items.reduce((sum, i) => sum + i.price * i.quantity, 0),
    }),
    { name: 'novainvesa-cart' }
  )
)
```

## Axios — llamadas a la API

```tsx
// lib/axios.ts
import axios from 'axios'

export const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL,
  timeout: 10000,
})

// Interceptor para JWT
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})
```

## Shadcn/UI — componentes disponibles

Usar siempre de `@/components/ui/`. Instalados: Button, Card, Input, Label, Badge, Skeleton, Drawer, Sheet, Dialog, Toast/Sonner.

```tsx
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Badge } from '@/components/ui/badge'
```

## Formato de precio (COP)

```tsx
// lib/utils.ts
export function formatPrice(amount: number): string {
  return new Intl.NumberFormat('es-CO', {
    style: 'currency',
    currency: 'COP',
    minimumFractionDigits: 0,
  }).format(amount)
}
// Resultado: "$1.250.000"
```

## Reglas de Next.js en Novainvesa

- SIEMPRE usar `[locale]` en la ruta — nunca páginas sin locale
- `generateMetadata` en todas las páginas (ver skill seo-metadata)
- `'use client'` solo en componentes interactivos — los demás son Server Components
- `noindex` en: `/admin/`, `/cuenta/`, `/checkout/`, `/confirmacion/`
- Static export: no usar `useSearchParams` directamente sin Suspense
