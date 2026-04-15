'use client'

import { useEffect, useState } from 'react'
import { usePathname, useParams } from 'next/navigation'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { MapPin, Plus, Loader2, Trash2, Star } from 'lucide-react'
import { toast } from 'sonner'
import { AuthGuard } from '@/components/cuenta/AuthGuard'
import { AccountSidebar } from '@/components/cuenta/AccountSidebar'
import api, { getErrorMessage } from '@/lib/api'

interface Address {
  id: number
  fullName: string
  phone: string
  address: string
  city: string
  department: string
  neighborhood?: string
  notes?: string
  isDefault: boolean
}

const addressSchema = z.object({
  fullName: z.string().min(3, 'Mínimo 3 caracteres'),
  phone: z.string().min(7, 'Teléfono inválido').regex(/^[0-9+\s-]+$/, 'Solo números'),
  address: z.string().min(5, 'Dirección muy corta'),
  city: z.string().min(2, 'Ciudad requerida'),
  department: z.string().min(2, 'Departamento requerido'),
  neighborhood: z.string().optional(),
  notes: z.string().optional(),
  isDefault: z.boolean().optional(),
})

type AddressData = z.infer<typeof addressSchema>

const inputClass =
  'w-full bg-neutral-800 border border-neutral-700 text-neutral-50 placeholder:text-neutral-500 rounded-lg px-3 py-2.5 text-sm focus:border-blue-600 focus:outline-none transition-colors'
const labelClass = 'block text-sm font-medium text-neutral-300 mb-1.5'
const errorClass = 'text-red-400 text-xs mt-1'

export default function DireccionesPage() {
  const pathname = usePathname()
  const params = useParams()
  const locale = (params?.locale as string) ?? 'es'
  const [addresses, setAddresses] = useState<Address[]>([])
  const [loading, setLoading] = useState(true)
  const [showForm, setShowForm] = useState(false)
  const [submitting, setSubmitting] = useState(false)

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<AddressData>({ resolver: zodResolver(addressSchema) })

  const fetchAddresses = async () => {
    try {
      const res = await api.get('/api/v1/users/me/addresses')
      const data = res.data?.data
      setAddresses(Array.isArray(data) ? data : [])
    } catch (err) {
      toast.error(getErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchAddresses()
  }, [])

  const onSubmit = async (data: AddressData) => {
    setSubmitting(true)
    try {
      await api.post('/api/v1/users/me/addresses', data)
      toast.success('Dirección agregada')
      reset()
      setShowForm(false)
      await fetchAddresses()
    } catch (err) {
      toast.error(getErrorMessage(err))
    } finally {
      setSubmitting(false)
    }
  }

  const handleDelete = async (id: number) => {
    try {
      await api.delete(`/api/v1/users/me/addresses/${id}`)
      toast.success('Dirección eliminada')
      setAddresses((prev) => prev.filter((a) => a.id !== id))
    } catch (err) {
      toast.error(getErrorMessage(err))
    }
  }

  const handleSetDefault = async (id: number) => {
    try {
      await api.put(`/api/v1/users/me/addresses/${id}/default`)
      toast.success('Dirección predeterminada actualizada')
      await fetchAddresses()
    } catch (err) {
      toast.error(getErrorMessage(err))
    }
  }

  return (
    <AuthGuard locale={locale}>
      <div className="min-h-screen bg-[#0A0A0A] pt-24 pb-16 px-4">
        <div className="max-w-5xl mx-auto flex gap-8">
          <AccountSidebar currentPath={pathname} locale={locale} />

          <div className="flex-1 min-w-0">
            <div className="flex items-center justify-between mb-6">
              <h1 className="text-2xl font-bold text-neutral-50">Direcciones</h1>
              <button
                onClick={() => setShowForm((v) => !v)}
                className="flex items-center gap-2 rounded-full bg-blue-600 hover:bg-blue-700 text-white font-semibold px-4 py-2 text-sm transition-colors"
              >
                <Plus className="w-4 h-4" />
                {showForm ? 'Cancelar' : 'Agregar'}
              </button>
            </div>

            {/* Formulario nueva dirección */}
            {showForm && (
              <div className="bg-neutral-900 border border-neutral-700 rounded-xl p-6 mb-6">
                <h2 className="text-base font-semibold text-neutral-50 mb-4">Nueva dirección</h2>
                <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                  <div className="grid grid-cols-2 gap-3">
                    <div>
                      <label className={labelClass}>Nombre completo</label>
                      <input {...register('fullName')} placeholder="María García" className={inputClass} />
                      {errors.fullName && <p className={errorClass}>{errors.fullName.message}</p>}
                    </div>
                    <div>
                      <label className={labelClass}>Teléfono</label>
                      <input {...register('phone')} type="tel" placeholder="300 123 4567" className={inputClass} />
                      {errors.phone && <p className={errorClass}>{errors.phone.message}</p>}
                    </div>
                  </div>

                  <div>
                    <label className={labelClass}>Dirección</label>
                    <input {...register('address')} placeholder="Cra 10 # 20-30, Apto 401" className={inputClass} />
                    {errors.address && <p className={errorClass}>{errors.address.message}</p>}
                  </div>

                  <div className="grid grid-cols-2 gap-3">
                    <div>
                      <label className={labelClass}>Ciudad</label>
                      <input {...register('city')} placeholder="Bogotá" className={inputClass} />
                      {errors.city && <p className={errorClass}>{errors.city.message}</p>}
                    </div>
                    <div>
                      <label className={labelClass}>Departamento</label>
                      <input {...register('department')} placeholder="Cundinamarca" className={inputClass} />
                      {errors.department && <p className={errorClass}>{errors.department.message}</p>}
                    </div>
                  </div>

                  <div>
                    <label className={labelClass}>Barrio <span className="text-neutral-500">(opcional)</span></label>
                    <input {...register('neighborhood')} placeholder="Chapinero" className={inputClass} />
                  </div>

                  <div>
                    <label className={labelClass}>Notas <span className="text-neutral-500">(opcional)</span></label>
                    <input {...register('notes')} placeholder="Casa azul, portón de madera" className={inputClass} />
                  </div>

                  <label className="flex items-center gap-2 cursor-pointer">
                    <input
                      {...register('isDefault')}
                      type="checkbox"
                      className="w-4 h-4 rounded border-neutral-600 bg-neutral-800 accent-blue-600"
                    />
                    <span className="text-sm text-neutral-300">Marcar como predeterminada</span>
                  </label>

                  <button
                    type="submit"
                    disabled={submitting}
                    className="rounded-full bg-blue-600 hover:bg-blue-700 disabled:opacity-60 text-white font-semibold px-6 py-2.5 text-sm transition-colors flex items-center gap-2"
                  >
                    {submitting ? <Loader2 className="w-4 h-4 animate-spin" /> : null}
                    Guardar dirección
                  </button>
                </form>
              </div>
            )}

            {/* Lista de direcciones */}
            {loading ? (
              <div className="flex items-center justify-center py-20">
                <Loader2 className="w-8 h-8 animate-spin text-blue-500" />
              </div>
            ) : addresses.length === 0 ? (
              <div className="bg-neutral-900 border border-neutral-700 rounded-xl p-12 text-center">
                <MapPin className="w-12 h-12 text-neutral-600 mx-auto mb-4" />
                <p className="text-neutral-400 text-sm">No tienes direcciones guardadas</p>
              </div>
            ) : (
              <div className="space-y-4">
                {addresses.map((addr) => (
                  <div
                    key={addr.id}
                    className={`bg-neutral-900 border rounded-xl p-5 ${addr.isDefault ? 'border-blue-500/50' : 'border-neutral-700'}`}
                  >
                    <div className="flex items-start justify-between gap-3">
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center gap-2 mb-1">
                          <p className="text-neutral-50 font-semibold text-sm">{addr.fullName}</p>
                          {addr.isDefault && (
                            <span className="bg-blue-500/20 text-blue-400 border border-blue-500/30 rounded-full px-2 py-0.5 text-xs">
                              Predeterminada
                            </span>
                          )}
                        </div>
                        <p className="text-neutral-400 text-xs">{addr.phone}</p>
                        <p className="text-neutral-400 text-xs mt-1">
                          {addr.address}, {addr.city}, {addr.department}
                          {addr.neighborhood && ` — ${addr.neighborhood}`}
                        </p>
                        {addr.notes && (
                          <p className="text-neutral-500 text-xs mt-1 italic">{addr.notes}</p>
                        )}
                      </div>
                      <div className="flex items-center gap-2 shrink-0">
                        {!addr.isDefault && (
                          <button
                            onClick={() => handleSetDefault(addr.id)}
                            className="flex items-center gap-1 text-xs text-neutral-400 hover:text-blue-400 transition-colors px-2 py-1 rounded-lg hover:bg-blue-500/10"
                          >
                            <Star className="w-3.5 h-3.5" />
                            Predeterminar
                          </button>
                        )}
                        <button
                          onClick={() => handleDelete(addr.id)}
                          className="flex items-center gap-1 text-xs text-red-400 hover:text-red-300 transition-colors px-2 py-1 rounded-lg hover:bg-red-500/10"
                        >
                          <Trash2 className="w-3.5 h-3.5" />
                          Eliminar
                        </button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </AuthGuard>
  )
}
