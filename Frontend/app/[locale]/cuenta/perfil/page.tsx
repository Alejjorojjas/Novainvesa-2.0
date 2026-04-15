'use client'

import { useEffect, useState } from 'react'
import { usePathname, useParams } from 'next/navigation'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Loader2 } from 'lucide-react'
import { toast } from 'sonner'
import { AuthGuard } from '@/components/cuenta/AuthGuard'
import { AccountSidebar } from '@/components/cuenta/AccountSidebar'
import api, { getErrorMessage } from '@/lib/api'

interface UserProfile {
  id: number
  fullName: string
  email: string
  phone: string
  createdAt: string
}

const profileSchema = z.object({
  fullName: z.string().min(3, 'Mínimo 3 caracteres'),
  phone: z.string().min(7, 'Teléfono inválido').regex(/^[0-9+\s-]+$/, 'Solo números'),
})

type ProfileData = z.infer<typeof profileSchema>

const inputClass =
  'w-full bg-neutral-800 border border-neutral-700 text-neutral-50 placeholder:text-neutral-500 rounded-lg px-3 py-2.5 text-sm focus:border-blue-600 focus:outline-none transition-colors'
const labelClass = 'block text-sm font-medium text-neutral-300 mb-1.5'
const errorClass = 'text-red-400 text-xs mt-1'

export default function PerfilPage() {
  const pathname = usePathname()
  const params = useParams()
  const locale = (params?.locale as string) ?? 'es'
  const [profile, setProfile] = useState<UserProfile | null>(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<ProfileData>({ resolver: zodResolver(profileSchema) })

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const res = await api.get('/api/v1/users/me')
        const data = res.data?.data as UserProfile
        setProfile(data)
        reset({ fullName: data.fullName, phone: data.phone ?? '' })
      } catch (err) {
        toast.error(getErrorMessage(err))
      } finally {
        setLoading(false)
      }
    }
    fetchProfile()
  }, [reset])

  const onSubmit = async (data: ProfileData) => {
    setSaving(true)
    try {
      await api.put('/api/v1/users/me', data)
      toast.success('Perfil actualizado')
      setProfile((prev) => prev ? { ...prev, ...data } : prev)
    } catch (err) {
      toast.error(getErrorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  return (
    <AuthGuard locale={locale}>
      <div className="min-h-screen bg-[#0A0A0A] pt-24 pb-16 px-4">
        <div className="max-w-5xl mx-auto flex gap-8">
          <AccountSidebar currentPath={pathname} locale={locale} />

          <div className="flex-1 min-w-0">
            <h1 className="text-2xl font-bold text-neutral-50 mb-6">Mi perfil</h1>

            {loading ? (
              <div className="flex items-center justify-center py-20">
                <Loader2 className="w-8 h-8 animate-spin text-blue-500" />
              </div>
            ) : (
              <div className="bg-neutral-900 border border-neutral-700 rounded-xl p-6">
                <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
                  <div>
                    <label className={labelClass}>Nombre completo</label>
                    <input
                      {...register('fullName')}
                      placeholder="Tu nombre"
                      className={inputClass}
                    />
                    {errors.fullName && <p className={errorClass}>{errors.fullName.message}</p>}
                  </div>

                  <div>
                    <label className={labelClass}>Email</label>
                    <input
                      type="email"
                      value={profile?.email ?? ''}
                      disabled
                      className={`${inputClass} opacity-50 cursor-not-allowed`}
                    />
                    <p className="text-neutral-500 text-xs mt-1">El email no se puede cambiar</p>
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

                  {profile?.createdAt && (
                    <p className="text-neutral-500 text-xs">
                      Cuenta creada el{' '}
                      {new Date(profile.createdAt).toLocaleDateString('es-CO', {
                        year: 'numeric',
                        month: 'long',
                        day: 'numeric',
                      })}
                    </p>
                  )}

                  <button
                    type="submit"
                    disabled={saving}
                    className="rounded-full bg-blue-600 hover:bg-blue-700 disabled:opacity-60 disabled:cursor-not-allowed text-white font-semibold px-6 py-2.5 text-sm transition-colors flex items-center gap-2"
                  >
                    {saving ? (
                      <>
                        <Loader2 className="w-4 h-4 animate-spin" />
                        Guardando...
                      </>
                    ) : (
                      'Guardar cambios'
                    )}
                  </button>
                </form>
              </div>
            )}
          </div>
        </div>
      </div>
    </AuthGuard>
  )
}
