'use client'

import { useState, useEffect, Suspense } from 'react'
import { useSearchParams } from 'next/navigation'
import { Loader2, Search, CheckCircle2, Circle, Truck } from 'lucide-react'
import api, { getErrorMessage } from '@/lib/api'

interface TrackingStep {
  step: number
  label: string
  date?: string
  completed: boolean
}

interface TrackingData {
  orderCode: string
  status: 'PENDING' | 'CONFIRMED' | 'PROCESSING' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED'
  steps: TrackingStep[]
  estimatedDelivery?: string
}

function RastrearContent() {
  const searchParams = useSearchParams()
  const [codigo, setCodigo] = useState(searchParams.get('codigo') ?? '')
  const [inputValue, setInputValue] = useState(searchParams.get('codigo') ?? '')
  const [loading, setLoading] = useState(false)
  const [data, setData] = useState<TrackingData | null>(null)
  const [error, setError] = useState<string | null>(null)

  const handleSearch = async (code: string) => {
    const trimmed = code.trim()
    if (!trimmed) return
    setLoading(true)
    setError(null)
    setData(null)
    try {
      const res = await api.get(`/api/v1/orders/${trimmed}/tracking`)
      setData(res.data?.data as TrackingData)
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  // Auto-buscar si hay código en URL
  useEffect(() => {
    const urlCodigo = searchParams.get('codigo')
    if (urlCodigo) {
      handleSearch(urlCodigo)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const onSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    setCodigo(inputValue)
    handleSearch(inputValue)
  }

  // Determinar el índice del paso activo
  const activeStepIndex = data
    ? data.steps.findLastIndex((s) => s.completed)
    : -1

  return (
    <div className="min-h-screen bg-[#0A0A0A] py-16 px-4">
      <div className="max-w-lg mx-auto">
        <div className="text-center mb-10">
          <div className="flex justify-center mb-4">
            <Truck className="w-10 h-10 text-blue-400" />
          </div>
          <h1 className="text-2xl font-bold text-neutral-50">Rastrear pedido</h1>
          <p className="text-neutral-400 text-sm mt-2">
            Ingresa tu código para ver el estado de tu envío
          </p>
        </div>

        {/* Formulario de búsqueda */}
        <form onSubmit={onSubmit} className="flex gap-2 mb-8">
          <input
            value={inputValue}
            onChange={(e) => setInputValue(e.target.value)}
            placeholder="NOVA-20260413-0001"
            className="flex-1 bg-neutral-800 border border-neutral-700 text-neutral-50 placeholder:text-neutral-500 rounded-lg px-4 py-2.5 text-sm focus:border-blue-600 focus:outline-none transition-colors font-mono"
          />
          <button
            type="submit"
            disabled={loading || !inputValue.trim()}
            className="rounded-full bg-blue-600 hover:bg-blue-700 disabled:opacity-60 disabled:cursor-not-allowed text-white font-semibold px-5 py-2.5 transition-colors flex items-center gap-2 shrink-0"
          >
            {loading ? (
              <Loader2 className="w-4 h-4 animate-spin" />
            ) : (
              <Search className="w-4 h-4" />
            )}
            Rastrear
          </button>
        </form>

        {/* Spinner */}
        {loading && (
          <div className="flex justify-center py-12">
            <Loader2 className="w-8 h-8 animate-spin text-blue-400" />
          </div>
        )}

        {/* Error */}
        {error && !loading && (
          <div className="bg-neutral-900 border border-red-500/30 rounded-xl p-6 text-center">
            <p className="text-red-400 font-medium">No se encontró el pedido</p>
            <p className="text-neutral-400 text-sm mt-1">{error}</p>
          </div>
        )}

        {/* Timeline */}
        {data && !loading && (
          <div className="bg-neutral-900 border border-neutral-700 rounded-xl p-6">
            <div className="flex items-center justify-between mb-6">
              <div>
                <p className="text-neutral-400 text-xs uppercase tracking-wide">Pedido</p>
                <p className="font-mono text-blue-400 font-bold">{data.orderCode}</p>
              </div>
              <span
                className={`text-xs font-medium px-3 py-1 rounded-full border ${
                  data.status === 'DELIVERED'
                    ? 'bg-green-500/10 text-green-400 border-green-500/30'
                    : data.status === 'CANCELLED'
                    ? 'bg-red-500/10 text-red-400 border-red-500/30'
                    : data.status === 'SHIPPED'
                    ? 'bg-blue-500/10 text-blue-400 border-blue-500/30'
                    : 'bg-amber-500/10 text-amber-400 border-amber-500/30'
                }`}
              >
                {data.status}
              </span>
            </div>

            {/* Pasos */}
            <div className="relative">
              {data.steps.map((step, idx) => {
                const isCompleted = step.completed
                const isActive = idx === activeStepIndex && !isCompleted

                return (
                  <div key={step.step} className="flex gap-4 relative">
                    {/* Línea conectora */}
                    {idx < data.steps.length - 1 && (
                      <div
                        className={`absolute left-3.5 top-7 w-0.5 h-full -translate-x-1/2 ${
                          isCompleted ? 'bg-blue-600' : 'bg-neutral-700'
                        }`}
                        style={{ height: 'calc(100% - 0px)' }}
                      />
                    )}

                    {/* Ícono */}
                    <div className="relative z-10 shrink-0">
                      {isCompleted ? (
                        <CheckCircle2 className="w-7 h-7 text-blue-600 bg-[#0A0A0A] rounded-full" />
                      ) : isActive ? (
                        <div className="w-7 h-7 rounded-full border-2 border-blue-600 bg-[#0A0A0A] flex items-center justify-center">
                          <div className="w-2.5 h-2.5 rounded-full bg-blue-600 animate-pulse" />
                        </div>
                      ) : (
                        <Circle className="w-7 h-7 text-neutral-600 bg-[#0A0A0A]" />
                      )}
                    </div>

                    {/* Contenido */}
                    <div className="pb-6 flex-1">
                      <p
                        className={`text-sm font-medium ${
                          isCompleted || isActive ? 'text-neutral-50' : 'text-neutral-500'
                        }`}
                      >
                        {step.label}
                      </p>
                      {step.date && (
                        <p className="text-neutral-500 text-xs mt-0.5">{step.date}</p>
                      )}
                    </div>
                  </div>
                )
              })}
            </div>

            {/* Estimado de entrega */}
            {data.estimatedDelivery && (
              <div className="mt-2 pt-4 border-t border-neutral-800 flex items-center gap-2 text-sm text-neutral-400">
                <Truck className="w-4 h-4 text-blue-400" />
                <span>Entrega estimada: <span className="text-neutral-50 font-medium">{data.estimatedDelivery}</span></span>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  )
}

export default function RastrearPage() {
  return (
    <Suspense
      fallback={
        <div className="min-h-screen bg-[#0A0A0A] flex items-center justify-center">
          <Loader2 className="w-8 h-8 animate-spin text-blue-400" />
        </div>
      }
    >
      <RastrearContent />
    </Suspense>
  )
}
