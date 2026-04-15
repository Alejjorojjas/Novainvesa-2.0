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

const registroSchema = z
  .object({
    fullName: z.string().min(3, 'Nombre muy corto'),
    email: z.string().email('Email inválido'),
    phone: z.string().min(10, 'Teléfono inválido'),
    password: z
      .string()
      .min(8, 'Mínimo 8 caracteres')
      .regex(/[A-Z]/, 'Debe tener al menos una mayúscula')
      .regex(/[0-9]/, 'Debe tener al menos un número'),
    confirmPassword: z.string(),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: 'Las contraseñas no coinciden',
    path: ['confirmPassword'],
  })

type RegistroData = z.infer<typeof registroSchema>

const inputClass =
  'w-full bg-neutral-800 border border-neutral-700 text-neutral-50 placeholder:text-neutral-500 rounded-lg px-3 py-2.5 text-sm focus:border-blue-600 focus:outline-none transition-colors'
const labelClass = 'block text-sm font-medium text-neutral-300 mb-1.5'
const errorClass = 'text-red-400 text-xs mt-1'

const ERROR_CODES: Record<string, string> = {
  AUTH_003: 'Este email ya está registrado',
}

export default function RegistroPage() {
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirm, setShowConfirm] = useState(false)
  const [loading, setLoading] = useState(false)
  const { login } = useAuthStore()
  const router = useRouter()

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<RegistroData>({ resolver: zodResolver(registroSchema) })

  const onSubmit = async (data: RegistroData) => {
    setLoading(true)
    try {
      const res = await api.post('/api/v1/auth/register', {
        fullName: data.fullName,
        email: data.email,
        phone: data.phone,
        password: data.password,
      })
      const { token, user } = res.data?.data as { token: string; user: AuthUser }
      login(user, token)
      toast.success('¡Cuenta creada! Bienvenido a Novainvesa')
      router.push('/')
    } catch (err) {
      let msg = getErrorMessage(err)
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
          <h1 className="text-xl font-bold text-neutral-50 mb-6">Crear cuenta</h1>

          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div>
              <label className={labelClass}>Nombre completo</label>
              <input
                {...register('fullName')}
                placeholder="María García"
                className={inputClass}
              />
              {errors.fullName && <p className={errorClass}>{errors.fullName.message}</p>}
            </div>

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
              <label className={labelClass}>Teléfono</label>
              <input
                {...register('phone')}
                type="tel"
                placeholder="300 123 4567"
                className={inputClass}
              />
              {errors.phone && <p className={errorClass}>{errors.phone.message}</p>}
            </div>

            <div>
              <label className={labelClass}>Contraseña</label>
              <div className="relative">
                <input
                  {...register('password')}
                  type={showPassword ? 'text' : 'password'}
                  placeholder="Mínimo 8 caracteres"
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

            <div>
              <label className={labelClass}>Confirmar contraseña</label>
              <div className="relative">
                <input
                  {...register('confirmPassword')}
                  type={showConfirm ? 'text' : 'password'}
                  placeholder="Repite la contraseña"
                  className={`${inputClass} pr-10`}
                />
                <button
                  type="button"
                  onClick={() => setShowConfirm((v) => !v)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-neutral-400 hover:text-neutral-50 transition-colors"
                >
                  {showConfirm ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                </button>
              </div>
              {errors.confirmPassword && (
                <p className={errorClass}>{errors.confirmPassword.message}</p>
              )}
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full rounded-full bg-blue-600 hover:bg-blue-700 disabled:opacity-60 disabled:cursor-not-allowed text-white font-semibold py-3 transition-colors flex items-center justify-center gap-2 mt-2"
            >
              {loading ? (
                <>
                  <Loader2 className="w-4 h-4 animate-spin" />
                  Creando cuenta...
                </>
              ) : (
                'Crear cuenta'
              )}
            </button>
          </form>

          <p className="text-center text-sm text-neutral-400 mt-6">
            ¿Ya tienes cuenta?{' '}
            <Link
              href="/auth/login"
              className="text-blue-400 hover:text-blue-300 font-semibold transition-colors"
            >
              Inicia sesión
            </Link>
          </p>
        </div>
      </div>
    </div>
  )
}
