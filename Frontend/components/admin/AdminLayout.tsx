'use client'

import { useEffect, useState } from 'react'
import Link from 'next/link'
import { usePathname, useRouter } from 'next/navigation'
import { LayoutDashboard, ShoppingBag, Package, Download, LogOut, Loader2 } from 'lucide-react'

interface AdminLayoutProps {
  children: React.ReactNode
  locale?: string
}

interface NavItem {
  href: string
  icon: React.ReactNode
  label: string
}

export function AdminLayout({ children, locale = 'es' }: AdminLayoutProps) {
  const [mounted, setMounted] = useState(false)
  const [adminName, setAdminName] = useState('')
  const [adminEmail, setAdminEmail] = useState('')
  const router = useRouter()
  const pathname = usePathname()

  useEffect(() => {
    setMounted(true)
    const token = localStorage.getItem('nova-admin-token')
    if (!token) {
      router.push(`/${locale}/admin/login`)
      return
    }
    const adminStr = localStorage.getItem('nova-admin-user')
    if (adminStr) {
      try {
        const admin = JSON.parse(adminStr) as { fullName?: string; email?: string }
        setAdminName(admin.fullName ?? '')
        setAdminEmail(admin.email ?? '')
      } catch {
        // ignore
      }
    }
  }, [router, locale])

  const handleLogout = () => {
    localStorage.removeItem('nova-admin-token')
    localStorage.removeItem('nova-admin-user')
    router.push(`/${locale}/admin/login`)
  }

  const navItems: NavItem[] = [
    { href: `/${locale}/admin/dashboard`, icon: <LayoutDashboard className="w-4 h-4" />, label: 'Dashboard' },
    { href: `/${locale}/admin/pedidos`, icon: <ShoppingBag className="w-4 h-4" />, label: 'Pedidos' },
    { href: `/${locale}/admin/productos`, icon: <Package className="w-4 h-4" />, label: 'Productos' },
    { href: `/${locale}/admin/productos/importar`, icon: <Download className="w-4 h-4" />, label: 'Importar' },
  ]

  if (!mounted) {
    return (
      <div className="min-h-screen bg-[#0A0A0A] flex items-center justify-center">
        <Loader2 className="w-8 h-8 animate-spin text-blue-500" />
      </div>
    )
  }

  return (
    <div className="flex min-h-screen bg-[#0A0A0A]">
      {/* Sidebar */}
      <aside className="w-64 bg-neutral-900 border-r border-neutral-800 min-h-screen flex flex-col fixed left-0 top-0 bottom-0 z-40">
        {/* Logo */}
        <div className="p-5 border-b border-neutral-800">
          <Link href={`/${locale}/admin/dashboard`} className="text-lg font-black tracking-tight text-neutral-50">
            NOVA<span className="font-light text-blue-400">INVESA</span>{' '}
            <span className="text-xs font-normal text-neutral-500 ml-1">Admin</span>
          </Link>
        </div>

        {/* Nav */}
        <nav className="flex-1 p-3 space-y-1">
          {navItems.map((item) => {
            const isActive = pathname === item.href || (item.href !== `/${locale}/admin/dashboard` && pathname.startsWith(item.href))
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

        {/* Admin info + logout */}
        <div className="p-4 border-t border-neutral-800">
          {adminName && (
            <div className="mb-3">
              <p className="text-neutral-50 text-sm font-semibold truncate">{adminName}</p>
              <p className="text-neutral-400 text-xs truncate">{adminEmail}</p>
            </div>
          )}
          <button
            onClick={handleLogout}
            className="w-full flex items-center gap-2 px-3 py-2 rounded-lg text-sm text-red-400 hover:text-red-300 hover:bg-red-500/10 transition-colors"
          >
            <LogOut className="w-4 h-4" />
            Cerrar sesión
          </button>
        </div>
      </aside>

      {/* Content */}
      <main className="flex-1 ml-64 bg-[#0A0A0A] p-8 min-h-screen">
        {children}
      </main>
    </div>
  )
}
