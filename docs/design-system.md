# Design System — Novainvesa v2.0
## Estilo: Premium · Moderno · Shopify-inspired

**Versión:** 2.1  
**Fecha:** Abril 2026  
**Framework:** Next.js 15 + Tailwind CSS 4 + Shadcn/UI  
**Tipografía:** Geist (fuente oficial de Vercel/Next.js)  
**Estilo:** Premium, elegante, oscuro, redondeado  

---

## 1. Tipografía

### Fuentes

```css
/* Importar en layout.tsx */
import { Geist, Geist_Mono } from 'next/font/google'

const geist = Geist({
  subsets: ['latin'],
  variable: '--font-geist',
})

const geistMono = Geist_Mono({
  subsets: ['latin'],
  variable: '--font-geist-mono',
})
```

### Escala tipográfica

| Nombre | Clase Tailwind | Tamaño | Peso | Uso |
|--------|---------------|--------|------|-----|
| Display | `text-5xl font-bold` | 48px | 700 | Títulos hero |
| H1 | `text-4xl font-bold` | 36px | 700 | Títulos de página |
| H2 | `text-3xl font-semibold` | 30px | 600 | Secciones |
| H3 | `text-2xl font-semibold` | 24px | 600 | Subsecciones |
| H4 | `text-xl font-medium` | 20px | 500 | Tarjetas, items |
| Body Large | `text-base` | 16px | 400 | Texto principal |
| Body | `text-sm` | 14px | 400 | Texto secundario |
| Caption | `text-xs` | 12px | 400 | Etiquetas, badges |
| Precio | `text-2xl font-bold` | 24px | 700 | Precios de productos |

---

## 2. Paleta de colores

### Colores primarios

```css
/* globals.css */
:root {
  /* Azul principal — confianza, profesional */
  --color-primary-50:  #EFF6FF;
  --color-primary-100: #DBEAFE;
  --color-primary-200: #BFDBFE;
  --color-primary-300: #93C5FD;
  --color-primary-400: #60A5FA;
  --color-primary-500: #3B82F6;
  --color-primary-600: #2563EB;  /* ← Principal */
  --color-primary-700: #1D4ED8;
  --color-primary-800: #1E40AF;
  --color-primary-900: #1E3A8A;

  /* Dorado/Ámbar — acento premium */
  --color-accent-400:  #FBBF24;
  --color-accent-500:  #F59E0B;  /* ← Acento principal */
  --color-accent-600:  #D97706;

  /* Neutros */
  --color-neutral-50:  #FAFAFA;
  --color-neutral-100: #F4F4F5;
  --color-neutral-200: #E4E4E7;
  --color-neutral-300: #D4D4D8;
  --color-neutral-400: #A1A1AA;
  --color-neutral-500: #71717A;
  --color-neutral-600: #52525B;
  --color-neutral-700: #3F3F46;
  --color-neutral-800: #27272A;
  --color-neutral-900: #18181B;
  --color-neutral-950: #09090B;

  /* Semánticos */
  --color-success: #22C55E;
  --color-error:   #EF4444;
  --color-warning: #F59E0B;
  --color-info:    #3B82F6;
}
```

### Modo claro (Light)

```css
:root {
  --background:     #FFFFFF;
  --background-alt: #FAFAFA;
  --foreground:     #09090B;
  --foreground-alt: #71717A;
  --border:         #E4E4E7;
  --card:           #FFFFFF;
  --card-border:    #E4E4E7;
  --input:          #F4F4F5;
  --ring:           #2563EB;
}
```

### Modo oscuro (Dark) — Principal

```css
.dark {
  --background:     #0A0A0A;  /* Fondo principal muy oscuro */
  --background-alt: #111111;  /* Fondo de tarjetas */
  --foreground:     #FAFAFA;  /* Texto principal */
  --foreground-alt: #A1A1AA;  /* Texto secundario */
  --border:         #27272A;  /* Bordes */
  --card:           #18181B;  /* Fondo de tarjetas */
  --card-border:    #27272A;
  --input:          #27272A;  /* Fondo de inputs */
  --ring:           #3B82F6;
}
```

### Uso de colores por contexto

| Contexto | Color | Clase Tailwind |
|---------|-------|---------------|
| Botón primario | Azul 600 | `bg-blue-600 hover:bg-blue-700` |
| Botón acento / CTA | Ámbar 500 | `bg-amber-500 hover:bg-amber-600` |
| Precio | Texto negro/blanco | `text-foreground font-bold` |
| Precio tachado | Gris 400 | `text-neutral-400 line-through` |
| Badge "Oferta" | Ámbar 500 | `bg-amber-500 text-white` |
| Badge "Agotado" | Gris 600 | `bg-neutral-600 text-white` |
| Badge "Nuevo" | Azul 600 | `bg-blue-600 text-white` |
| Éxito | Verde 500 | `text-green-500` |
| Error | Rojo 500 | `text-red-500` |
| WhatsApp | Verde WhatsApp | `bg-[#25D366]` |

---

## 3. Espaciado y layout

### Grid del sitio
```css
/* Contenedor principal */
.container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 1rem; /* 16px móvil */
}

/* Breakpoints */
sm:  640px   /* Tablet pequeña */
md:  768px   /* Tablet */
lg:  1024px  /* Desktop */
xl:  1280px  /* Desktop grande */
2xl: 1536px  /* Ultra wide */
```

### Grid de productos
```
Móvil:   2 columnas  → grid-cols-2
Tablet:  3 columnas  → md:grid-cols-3
Desktop: 4 columnas  → lg:grid-cols-4
```

### Espaciado estándar
```
gap-4   = 16px  → entre elementos pequeños
gap-6   = 24px  → entre componentes
gap-8   = 32px  → entre secciones
gap-12  = 48px  → entre secciones grandes
py-16   = 64px  → padding de secciones
```

---

## 4. Bordes y sombras

### Border radius (pill style)
```css
rounded-full   → botones pill, badges, avatares
rounded-2xl    → tarjetas de producto, cards grandes
rounded-xl     → inputs, modales, drawers
rounded-lg     → elementos menores
rounded-md     → elementos pequeños
```

### Sombras
```css
/* Modo claro */
shadow-sm    → tarjetas en reposo
shadow-md    → tarjetas en hover
shadow-lg    → modales, dropdowns
shadow-xl    → drawers, sidebars

/* Modo oscuro — sombras con brillo azul sutil */
shadow-[0_0_15px_rgba(37,99,235,0.15)]   → tarjetas en hover dark
shadow-[0_0_30px_rgba(37,99,235,0.1)]    → modales dark
```

---

## 5. Componentes Shadcn/UI — Configuración

### Instalación de componentes necesarios
```bash
npx shadcn@latest add button
npx shadcn@latest add input
npx shadcn@latest add label
npx shadcn@latest add card
npx shadcn@latest add badge
npx shadcn@latest add sheet       # CartDrawer
npx shadcn@latest add dialog      # Modales
npx shadcn@latest add dropdown-menu
npx shadcn@latest add select
npx shadcn@latest add separator
npx shadcn@latest add skeleton    # Loading states
npx shadcn@latest add toast       # Notificaciones
npx shadcn@latest add progress    # Import progress
npx shadcn@latest add tabs
npx shadcn@latest add avatar
npx shadcn@latest add table       # Admin tables
```

### Configuración `components.json`
```json
{
  "style": "new-york",
  "rsc": true,
  "tsx": true,
  "tailwind": {
    "config": "tailwind.config.ts",
    "css": "app/globals.css",
    "baseColor": "zinc",
    "cssVariables": true
  },
  "aliases": {
    "components": "@/components",
    "utils": "@/lib/utils"
  }
}
```

---

## 6. Componentes personalizados

### Botón primario
```tsx
// Azul, pill, con hover y loading state
<Button className="bg-blue-600 hover:bg-blue-700 text-white 
  rounded-full px-6 py-2.5 font-medium transition-all 
  hover:shadow-[0_0_20px_rgba(37,99,235,0.4)]">
  Agregar al carrito
</Button>
```

### Botón CTA (Call to Action)
```tsx
// Ámbar/dorado para acciones de alta conversión
<Button className="bg-amber-500 hover:bg-amber-600 text-white 
  rounded-full px-8 py-3 font-semibold text-base 
  transition-all hover:scale-105 hover:shadow-lg">
  Comprar ahora
</Button>
```

### Botón WhatsApp
```tsx
<Button className="bg-[#25D366] hover:bg-[#128C7E] text-white 
  rounded-full px-6 py-2.5 font-medium gap-2">
  <WhatsAppIcon /> Consultar por WhatsApp
</Button>
```

### Tarjeta de producto (ProductCard)
```tsx
<div className="group bg-card border border-card-border 
  rounded-2xl overflow-hidden transition-all duration-300
  hover:shadow-[0_0_25px_rgba(37,99,235,0.15)] 
  hover:-translate-y-1 cursor-pointer">
  
  {/* Imagen con overlay en hover */}
  <div className="relative aspect-square overflow-hidden">
    <Image className="object-cover transition-transform duration-300
      group-hover:scale-105" />
    
    {/* Badges */}
    <div className="absolute top-2 left-2 flex gap-1">
      {oferta && <Badge className="bg-amber-500 rounded-full">Oferta</Badge>}
      {agotado && <Badge className="bg-neutral-600 rounded-full">Agotado</Badge>}
    </div>
    
    {/* Botón rápido en hover */}
    <div className="absolute bottom-2 left-0 right-0 px-2
      opacity-0 group-hover:opacity-100 transition-opacity">
      <Button className="w-full rounded-full bg-blue-600 text-white">
        Agregar al carrito
      </Button>
    </div>
  </div>
  
  {/* Info del producto */}
  <div className="p-4">
    <p className="text-sm text-foreground-alt line-clamp-1">{categoria}</p>
    <h3 className="font-medium text-foreground line-clamp-2 mt-1">{nombre}</h3>
    <div className="flex items-center gap-2 mt-2">
      <span className="text-xl font-bold text-foreground">${precio}</span>
      {precioAnterior && 
        <span className="text-sm text-neutral-400 line-through">${precioAnterior}</span>}
    </div>
  </div>
</div>
```

### Input estándar
```tsx
<div className="space-y-1.5">
  <Label className="text-sm font-medium text-foreground">
    Nombre completo <span className="text-red-500">*</span>
  </Label>
  <Input className="rounded-xl bg-input border-border 
    focus:ring-2 focus:ring-blue-600 focus:border-transparent
    placeholder:text-neutral-500" />
  {error && <p className="text-xs text-red-500">{error}</p>}
</div>
```

### Badge de estado de pedido
```tsx
const statusStyles = {
  PENDING:    'bg-neutral-700 text-neutral-200',
  CONFIRMED:  'bg-blue-900 text-blue-200',
  PROCESSING: 'bg-amber-900 text-amber-200',
  SHIPPED:    'bg-purple-900 text-purple-200',
  DELIVERED:  'bg-green-900 text-green-200',
  RETURNED:   'bg-red-900 text-red-200',
  CANCELLED:  'bg-neutral-800 text-neutral-400',
}

<Badge className={`rounded-full px-3 py-1 text-xs font-medium
  ${statusStyles[status]}`}>
  {statusLabel[status]}
</Badge>
```

---

## 7. Navbar

```
Estructura:
┌────────────────────────────────────────────────────────┐
│ [Logo NOVAINVESA]  [Mascotas Hogar Tech Belleza Fitness]│
│                   [🔍 Buscar...]  [ES] [👤] [🛒 2]    │
└────────────────────────────────────────────────────────┘

Modo oscuro: bg-[#0A0A0A] border-b border-[#27272A]
Modo claro:  bg-white border-b border-neutral-100
Sticky: top-0 z-50 backdrop-blur-md bg-opacity-95

Logo: fuente Geist, font-bold, text-xl
      "NOVA" en azul-600 + "INVESA" en texto normal
```

---

## 8. Hero Banner

```
Fondo: gradiente diagonal
  dark:  from-[#0A0A0A] via-[#0F172A] to-[#0A0A0A]
  light: from-slate-950 via-blue-950 to-slate-950

Elemento decorativo: orbe azul difuso en esquina superior derecha
  bg-blue-600/20 blur-[120px] rounded-full w-96 h-96

Texto:
  Badge: "🚀 Envíos a toda Colombia" → pill ámbar
  H1: "Todo lo que necesitas," (blanco)
      "en un solo lugar." (gradiente azul→ámbar)
  Subtítulo: gris 400, text-lg
  
CTA:
  [Comprar ahora →]   → ámbar, pill, grande
  [Ver catálogo]      → outline blanco, pill, grande
```

---

## 9. Modo oscuro — Reglas generales

```css
/* Fondo de página */
dark:bg-[#0A0A0A]

/* Fondo de tarjetas y secciones */
dark:bg-[#111111]
dark:bg-[#18181B]

/* Bordes */
dark:border-[#27272A]

/* Texto principal */
dark:text-white
dark:text-neutral-50

/* Texto secundario */
dark:text-neutral-400

/* Inputs */
dark:bg-[#27272A] dark:border-[#3F3F46]

/* Hover en tarjetas */
dark:hover:bg-[#1C1C1E]
dark:hover:border-blue-900

/* Sombra azul premium en dark mode */
dark:hover:shadow-[0_0_25px_rgba(37,99,235,0.15)]
```

---

## 10. Animaciones

```css
/* Transiciones estándar */
transition-all duration-200    /* Botones, links */
transition-all duration-300    /* Tarjetas, drawers */
transition-all duration-500    /* Elementos de página */

/* Hover en tarjetas */
hover:-translate-y-1 hover:scale-[1.01]

/* Hover en botones CTA */
hover:scale-105

/* Hover en imágenes de producto */
group-hover:scale-105

/* Entrada de modales */
animate-in fade-in-0 zoom-in-95

/* Salida de modales */
animate-out fade-out-0 zoom-out-95

/* Slide del CartDrawer */
animate-in slide-in-from-right
```

---

## 11. Iconos

Usar **Lucide React** (integrado con Shadcn/UI):

```tsx
import { 
  ShoppingCart, Search, User, Menu, X,
  ChevronRight, ChevronLeft, ChevronDown,
  Heart, Share2, Star, StarHalf,
  Truck, Shield, RotateCcw, CreditCard,
  Check, CheckCircle2, AlertCircle, Info,
  Sun, Moon,           // toggle dark mode
  Package, MapPin,     // pedidos y envíos
  Phone, Mail,         // contacto
  Instagram, Facebook, // redes sociales (SVG inline)
  ArrowRight, ArrowLeft,
  Plus, Minus, Trash2,
  Upload, Download, RefreshCw,
  BarChart2, Users, Settings, LogOut
} from 'lucide-react'

/* Tamaños estándar */
size={16}   // Inline, en botones pequeños
size={20}   // Navbar, botones medios
size={24}   // Iconos estándar
size={32}   // Iconos de características
size={48}   // Iconos de secciones vacías
```

---

## 12. Skeleton Loading (estados de carga)

```tsx
/* ProductCard skeleton */
<div className="rounded-2xl overflow-hidden border border-border">
  <Skeleton className="aspect-square w-full" />
  <div className="p-4 space-y-2">
    <Skeleton className="h-3 w-1/3" />
    <Skeleton className="h-4 w-full" />
    <Skeleton className="h-4 w-2/3" />
    <Skeleton className="h-6 w-1/2 mt-2" />
  </div>
</div>
```

---

## 13. Toast (notificaciones)

```tsx
// Producto agregado al carrito
toast({
  title: "✅ Agregado al carrito",
  description: "Ejercitador Multi × 1",
  duration: 2000,
})

// Error
toast({
  variant: "destructive",
  title: "❌ Error",
  description: "No hay suficiente stock",
})

// Pedido confirmado
toast({
  title: "🎉 ¡Pedido confirmado!",
  description: "NOVA-20260413-0001",
  duration: 5000,
})
```

---

## 14. Responsive — Breakpoints clave

```
Móvil (< 640px):
  - Navbar: logo + hamburger + carrito
  - Grid productos: 2 columnas
  - Checkout: 1 columna
  - Hero: texto centrado, CTA apilados

Tablet (640px - 1024px):
  - Navbar: logo + categorías colapsadas + iconos
  - Grid productos: 3 columnas
  - Checkout: formulario + resumen apilados

Desktop (> 1024px):
  - Navbar: completo con categorías visibles
  - Grid productos: 4 columnas
  - Checkout: formulario (2/3) + resumen sticky (1/3)
  - Hero: texto a la izquierda, imagen a la derecha
```

---

## 15. Checklist de accesibilidad

```
✅ Contraste mínimo 4.5:1 en texto normal
✅ Contraste mínimo 3:1 en texto grande
✅ Focus visible en todos los elementos interactivos
✅ Alt text en todas las imágenes
✅ Labels en todos los inputs
✅ aria-label en botones de solo ícono
✅ Roles semánticos (nav, main, section, article)
✅ Skip to content link
✅ Soporte completo de teclado en modales y drawers
```

---

## Historial de cambios

| Versión | Fecha | Cambios |
|---------|-------|---------|
| 1.0 | Oct 2025 | Sistema inicial con React + Tailwind |
| 2.0 | Abr 2026 | Migración a Next.js + Shadcn/UI |
| 2.1 | Abr 2026 | Estilo premium dark, Geist, pill borders |

