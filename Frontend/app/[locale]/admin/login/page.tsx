'use client'

import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useRouter, useParams } from 'next/navigation'
import { Eye, EyeOff, Loader2 } from 'lucide-react'
import api, { getErrorMessage } from '@/lib/api'

const loginSchema = z.object({
  email: z.string().email('Email inválido'),
  password: z.string().min(6, 'Mínimo 6 caracteres'),
})

type LoginData = z.infer<typeof loginSchema>

const inputClass =
  'w-full bg-neutral-800 border border-neutral-700 text-neutral-50 placeholder:text-neutral-500 rounded-lg px-3 py-2.5 text-sm focus:border-blue-600 focus:outline-none transition-colors'
const labelClass = 'block text-sm font-medium text-neutral-300 mb-1.5'
const errorClass = 'text-red-400 text-xs mt-1'

interface AdminLoginResponse {
  token: string
  admin: {
    id: number
    email: string
    fullName: string
    role: string
  }
}

export default function AdminLoginPage() {
  const [showPassword, setShowPassword] = useState(false)
  const [loading, setLoading] = useState(false)
  const [errorMsg, setErrorMsg] = useState('')
  const router = useRouter()
  const params = useParams()
  const locale = (params?.locale as string) ?? 'es'

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginData>({ resolver: zodResolver(loginSchema) })

  const onSubmit = async (data: LoginData) => {
    setLoading(true)
    setErrorMsg('')
    try {
      const res = await api.post('/api/v1/admin/auth/login', data)
      const { token, admin } = res.data?.data as AdminLoginResponse
      localStorage.setItem('nova-admin-token', token)
      localStorage.setItem('nova-admin-user', JSON.stringify(admin))
      router.push(`/${locale}/admin/dashboard`)
    } catch (err) {
      setErrorMsg(getErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="fixed inset-0 bg-[#0A0A0A] flex items-center justify-center px-4 z-50">
      <div className="max-w-md w-full">
        {/* Logo */}
        <div className="text-center mb-8">
          <h2 className="text-3xl font-black tracking-tight text-neutral-50">
            NOVA<span className="font-light text-blue-400">INVESA</span>
          </h2>
          <p className="text-neutral-500 text-sm mt-1">Panel de administración</p>
        </div>

        {/* Card */}
        <div className="bg-neutral-900 border border-neutral-700 rounded-xl p-8">
          <h1 className="text-xl font-bold text-neutral-50 mb-6">Acceder al panel</h1>

          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div>
              <label className={labelClass}>Email</label>
              <input
                {...register('email')}
                type="email"
                placeholder="admin@novainvesa.com"
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

            {errorMsg && (
              <div className="bg-red-500/10 border border-red-500/30 rounded-lg px-4 py-2.5">
                <p className="text-red-400 text-sm">{errorMsg}</p>
              </div>
            )}

            <button
              type="submit"
              disabled={loading}
              className="w-full rounded-full bg-blue-600 hover:bg-blue-700 disabled:opacity-60 disabled:cursor-not-allowed text-white font-semibold py-3 transition-colors flex items-center justify-center gap-2 mt-2"
            >
              {loading ? (
                <>
                  <Loader2 className="w-4 h-4 animate-spin" />
                  Verificando...
                </>
              ) : (
                'Acceder al panel'
              )}
            </button>
          </form>
        </div>
      </div>
    </div>
  )
}
