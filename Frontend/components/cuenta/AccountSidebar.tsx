'use client'

import { useEffect, useState } from 'react'
import Link from 'next/link'
import { useRouter } from 'next/navigation'
import { User, Package, MapPin, Heart, LogOut } from 'lucide-react'
import { useAuthStore } from '@/lib/store/authStore'

interface NavItem {
  href: string
  icon: React.ReactNode
  label: string
}

interface AccountSidebarProps {
  currentPath: string
  locale?: string
}

export function AccountSidebar({ currentPath, locale = 'es' }: AccountSidebarProps) {
  const { user, logout } = useAuthStore()
  const router = useRouter()
  const [mounted, setMounted] = useState(false)

  useEffect(() => {
    setMounted(true)
  }, [])

  const navItems: NavItem[] = [
    { href: `/${locale}/cuenta/perfil`, icon: <User className="w-4 h-4" />, label: 'Mi perfil' },
    { href: `/${locale}/cuenta/pedidos`, icon: <Package className="w-4 h-4" />, label: 'Mis pedidos' },
    { href: `/${locale}/cuenta/direcciones`, icon: <MapPin className="w-4 h-4" />, label: 'Direcciones' },
    { href: `/${locale}/cuenta/favoritos`, icon: <Heart className="w-4 h-4" />, label: 'Favoritos' },
  ]

  const handleLogout = () => {
    logout()
    router.push(`/${locale}`)
  }

  const initials = mounted && user?.fullName
    ? user.fullName.slice(0, 2).toUpperCase()
    : '--'

  const displayName = mounted ? (user?.fullName ?? '') : ''
  const displayEmail = mounted ? (user?.email ?? '') : ''

  return (
    <aside className="w-64 shrink-0">
      <div className="bg-neutral-900 border border-neutral-700 rounded-xl p-4 sticky top-24">
        {/* Avatar */}
        <div className="flex items-center gap-3 mb-6 pb-4 border-b border-neutral-800">
          <div className="w-12 h-12 rounded-full bg-blue-600 flex items-center justify-center text-white font-bold text-sm shrink-0">
            {initials}
          </div>
          <div className="min-w-0">
            <p className="text-neutral-50 font-semibold text-sm truncate">{displayName}</p>
            <p className="text-neutral-400 text-xs truncate">{displayEmail}</p>
          </div>
        </div>

        {/* Navegación */}
        <nav className="space-y-1 mb-6">
          {navItems.map((item) => {
            const isActive = currentPath.startsWith(item.href)
            return (
              <Link
                key={item.href}
                href={item.href}
                className={`flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                  isActive
                    ? 'bg-blue-600/10 text-blue-400 border-l-2 border-blue-600 pl-[10px]'
                    : 'text-neutral-400 hover:text-neutral-50 hover:bg-neutral-800'
                }`}
              >
                {item.icon}
                {item.label}
              </Link>
            )
          })}
        </nav>

        {/* Cerrar sesión */}
        <button
          onClick={handleLogout}
          className="w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium text-red-400 hover:text-red-300 hover:bg-red-500/10 transition-colors"
        >
          <LogOut className="w-4 h-4" />
          Cerrar sesión
        </button>
      </div>
    </aside>
  )
}
