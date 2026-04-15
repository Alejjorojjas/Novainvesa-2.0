'use client'

import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useRouter } from 'next/navigation'
import Link from 'next/link'
import { Eye, EyeOff, Loader2 } from 'lucide-react'
import { toast } from 'sonner'
import { useAuthStore } from '@/lib/store/authStore'
import api, { getErrorMessage } from '@/lib/api'
import type { AuthUser } from '@/lib/store/authStore'

const loginSchema = z.object({
  email: z.string().email('Email inválido'),
  password: z.string().min(6, 'Mínimo 6 caracteres'),
})

type LoginData = z.infer<typeof loginSchema>

const inputClass =
  'w-full bg-neutral-800 border border-neutral-700 text-neutral-50 placeholder:text-neutral-500 rounded-lg px-3 py-2.5 text-sm focus:border-blue-600 focus:outline-none transition-colors'
const labelClass = 'block text-sm font-medium text-neutral-300 mb-1.5'
const errorClass = 'text-red-400 text-xs mt-1'

const ERROR_CODES: Record<string, string> = {
  AUTH_001: 'Email o contraseña incorrectos',
  AUTH_002: 'Cuenta desactivada. Contacta soporte.',
}

export default function LoginPage() {
  const [showPassword, setShowPassword] = useState(false)
  const [loading, setLoading] = useState(false)
  const { login } = useAuthStore()
  const router = useRouter()

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginData>({ resolver: zodResolver(loginSchema) })

  const onSubmit = async (data: LoginData) => {
    setLoading(true)
    try {
      const res = await api.post('/api/v1/auth/login', data)
      const { token, user } = res.data?.data as { token: string; user: AuthUser }
      login(user, token)
      toast.success(`Bienvenido, ${user.fullName}`)
      router.push('/')
    } catch (err) {
      let msg = getErrorMessage(err)
      // Mapear códigos de error conocidos
      if (typeof err === 'object' && err !== null) {
        const axiosErr = err as { response?: { data?: { error?: { code?: string } } } }
        const code = axiosErr.response?.data?.error?.code
        if (code && ERROR_CODES[code]) msg = ERROR_CODES[code]
      }
      toast.error(msg)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-[#0A0A0A] flex items-center justify-center px-4 py-16">
      <div className="max-w-md w-full">
        {/* Logo */}
        <div className="text-center mb-8">
          <h2 className="text-3xl font-black tracking-tight text-neutral-50">
            NOVA<span className="font-light">INVESA</span>
          </h2>
        </div>

        {/* Card */}
        <div className="bg-neutral-900 border border-neutral-700 rounded-xl p-8">
          <h1 className="text-xl font-bold text-neutral-50 mb-6">Iniciar sesión</h1>

          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div>
              <label className={labelClass}>Email</label>
              <input
                {...register('email')}
                type="email"
                placeholder="tu@email.com"
                className={inputClass}
              />
              {errors.email && <p className={errorClass}>{errors.email.message}</p>}
            </div>

            <div>
              <label className={labelClass}>Contraseña</label>
              <div className="relative">
                <input
                  {...register('password')}
                  type={showPassword ? 'text' : 'password'}
                  placeholder="••••••••"
                  className={`${inputClass} pr-10`}
                />
                <button
                  type="button"
                  onClick={() => setShowPassword((v) => !v)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-neutral-400 hover:text-neutral-50 transition-colors"
                >
                  {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                </button>
              </div>
              {errors.password && <p className={errorClass}>{errors.password.message}</p>}
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full rounded-full bg-blue-600 hover:bg-blue-700 disabled:opacity-60 disabled:cursor-not-allowed text-white font-semibold py-3 transition-colors flex items-center justify-center gap-2 mt-2"
            >
              {loading ? (
                <>
                  <Loader2 className="w-4 h-4 animate-spin" />
                  Entrando...
                </>
              ) : (
                'Entrar'
              )}
            </button>
          </form>

          <p className="text-center text-sm text-neutral-400 mt-6">
            ¿No tienes cuenta?{' '}
            <Link
              href="/auth/registro"
              className="text-blue-400 hover:text-blue-300 font-semibold transition-colors"
            >
              Regístrate
            </Link>
          </p>
        </div>
      </div>
    </div>
  )
}
