// Redirigir al locale por defecto — el middleware de next-intl
// maneja esto automáticamente, pero esta página sirve como fallback
import { redirect } from 'next/navigation'

export default function RootPage() {
  redirect('/es')
}
