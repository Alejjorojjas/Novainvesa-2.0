# Skill: design-tokens

Sistema de diseño visual de Novainvesa. Usar estos tokens en TODOS los componentes.

## Colores de marca

| Token | Hex | Uso |
|-------|-----|-----|
| `blue-600` | `#2563EB` | Primario — CTAs, links, botones principales |
| `amber-500` | `#F59E0B` | Acento — badges de oferta, highlights |
| `neutral-950` | `#0A0A0A` | Fondo oscuro principal |
| `neutral-900` | `#171717` | Cards, superficies elevadas |
| `neutral-700` | `#404040` | Bordes, separadores |
| `neutral-400` | `#A3A3A3` | Texto secundario, placeholders |
| `neutral-50` | `#FAFAFA` | Texto sobre fondo oscuro |
| `green-500` | `#22C55E` | Éxito, stock disponible |
| `red-500` | `#EF4444` | Error, stock agotado |

## Configuración Shadcn/UI

- **Estilo:** new-york
- **Base color:** zinc
- **CSS variables:** activadas
- **Radius:** `0.5rem`

## Tipografía

```css
/* Font: Inter (Google Fonts) */
font-family: 'Inter', sans-serif;

/* Escala */
--text-xs: 0.75rem;    /* 12px — etiquetas */
--text-sm: 0.875rem;   /* 14px — body pequeño */
--text-base: 1rem;     /* 16px — body */
--text-lg: 1.125rem;   /* 18px — subtítulos */
--text-xl: 1.25rem;    /* 20px — títulos de card */
--text-2xl: 1.5rem;    /* 24px — títulos de sección */
--text-3xl: 1.875rem;  /* 30px — títulos de página */
--text-4xl: 2.25rem;   /* 36px — hero */
```

## Bordes — Estilo pill

```css
/* Novainvesa usa bordes redondeados tipo pill en buttons y badges */
--radius-pill: 9999px;    /* buttons, badges */
--radius-card: 0.75rem;   /* cards de producto */
--radius-input: 0.5rem;   /* inputs, selects */
```

## Sombras

```css
--shadow-card: 0 2px 8px rgba(0, 0, 0, 0.4);      /* cards en modo oscuro */
--shadow-elevated: 0 4px 16px rgba(0, 0, 0, 0.6); /* modales, drawers */
```

## Componentes — Clases Tailwind estándar

### Botón primario
```tsx
<Button className="rounded-full bg-blue-600 hover:bg-blue-700 text-white font-semibold px-6 py-2">
  Agregar al carrito
</Button>
```

### Badge de oferta
```tsx
<Badge className="rounded-full bg-amber-500 text-black font-bold text-xs px-2 py-0.5">
  -30%
</Badge>
```

### Card de producto
```tsx
<Card className="bg-neutral-900 border border-neutral-700 rounded-xl overflow-hidden hover:border-blue-600 transition-colors">
  <CardContent className="p-4">
    {/* ... */}
  </CardContent>
</Card>
```

### Input de formulario
```tsx
<Input
  className="bg-neutral-800 border-neutral-700 text-neutral-50 placeholder:text-neutral-400 rounded-lg focus:border-blue-600"
/>
```

## Animaciones

```css
/* Transiciones suaves */
transition: all 0.2s ease-in-out;

/* Hover en cards */
.product-card:hover { transform: translateY(-2px); }

/* Loading skeleton */
@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.5; } }
```

## Skeleton de carga

```tsx
import { Skeleton } from '@/components/ui/skeleton'

// Card de producto cargando
<div className="space-y-3">
  <Skeleton className="h-48 w-full rounded-xl bg-neutral-800" />
  <Skeleton className="h-4 w-3/4 bg-neutral-800" />
  <Skeleton className="h-4 w-1/2 bg-neutral-800" />
</div>
```

## Modo oscuro

Novainvesa usa **modo oscuro por defecto** (no toggle claro/oscuro).
El fondo principal siempre es `#0A0A0A`. No implementar modo claro.

## Iconos

Usar `lucide-react` (ya incluido con Shadcn):
```tsx
import { ShoppingCart, Heart, Search, ChevronRight, Star } from 'lucide-react'
```

## Reglas de consistencia

- Nunca usar colores hardcodeados — siempre clases Tailwind o CSS variables
- Botones principales: siempre `rounded-full` (pill)
- Cards: siempre `rounded-xl` con borde `neutral-700`
- Texto sobre fondo oscuro: `text-neutral-50` o `text-neutral-400` (secundario)
- Nunca fondo blanco — la tienda es dark by default
