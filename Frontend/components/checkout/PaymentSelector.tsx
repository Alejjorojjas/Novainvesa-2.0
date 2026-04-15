'use client'

import { useEffect, useState, useCallback } from 'react'
import { CheckCircle2, XCircle, Loader2 } from 'lucide-react'
import api from '@/lib/api'

type PaymentMethod = 'COD' | 'WOMPI' | 'MERCADOPAGO'

interface PaymentSelectorProps {
  value: PaymentMethod
  onChange: (method: PaymentMethod) => void
  city: string
  department: string
}

interface CoverageState {
  loading: boolean
  covered: boolean | null
}

const options: {
  id: PaymentMethod
  icon: string
  label: string
  description: string
  badge?: string
}[] = [
  {
    id: 'COD',
    icon: '💵',
    label: 'Pago contraentrega',
    description: 'Paga cuando recibas tu pedido',
  },
  {
    id: 'MERCADOPAGO',
    icon: '💳',
    label: 'MercadoPago',
    description: 'Tarjeta, PSE, Nequi y más',
    badge: 'Más popular',
  },
  {
    id: 'WOMPI',
    icon: '🏦',
    label: 'Wompi',
    description: 'Tarjeta crédito/débito',
  },
]

export function PaymentSelector({ value, onChange, city, department }: PaymentSelectorProps) {
  const [coverage, setCoverage] = useState<CoverageState>({ loading: false, covered: null })

  const checkCoverage = useCallback(async (c: string, d: string) => {
    if (!c.trim() || !d.trim()) {
      setCoverage({ loading: false, covered: null })
      return
    }
    setCoverage({ loading: true, covered: null })
    try {
      const res = await api.post('/api/v1/orders/coverage', { city: c, department: d })
      const covered = res.data?.data?.covered ?? false
      setCoverage({ loading: false, covered })
    } catch {
      setCoverage({ loading: false, covered: false })
    }
  }, [])

  useEffect(() => {
    const timeout = setTimeout(() => {
      checkCoverage(city, department)
    }, 600)
    return () => clearTimeout(timeout)
  }, [city, department, checkCoverage])

  return (
    <div className="space-y-3">
      {options.map((opt) => {
        const isSelected = value === opt.id
        const isCOD = opt.id === 'COD'

        return (
          <button
            key={opt.id}
            type="button"
            onClick={() => onChange(opt.id)}
            className={`w-full text-left rounded-xl border p-4 transition-colors ${
              isSelected
                ? 'border-blue-600 bg-blue-600/5'
                : 'border-neutral-700 hover:border-neutral-500'
            }`}
          >
            <div className="flex items-start gap-3">
              {/* Radio visual */}
              <div
                className={`mt-0.5 w-4 h-4 rounded-full border-2 shrink-0 flex items-center justify-center ${
                  isSelected ? 'border-blue-600' : 'border-neutral-600'
                }`}
              >
                {isSelected && (
                  <div className="w-2 h-2 rounded-full bg-blue-600" />
                )}
              </div>

              {/* Contenido */}
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2 flex-wrap">
                  <span className="text-lg">{opt.icon}</span>
                  <span className="text-neutral-50 font-semibold text-sm">{opt.label}</span>
                  {opt.badge && (
                    <span className="text-xs bg-amber-500/20 text-amber-400 border border-amber-500/30 rounded-full px-2 py-0.5 font-medium">
                      {opt.badge}
                    </span>
                  )}
                </div>
                <p className="text-neutral-400 text-xs mt-0.5">{opt.description}</p>

                {/* Indicador de cobertura para COD */}
                {isCOD && (
                  <div className="mt-2">
                    {!city.trim() || !department.trim() ? (
                      <p className="text-neutral-500 text-xs">Ingresa tu ciudad primero</p>
                    ) : coverage.loading ? (
                      <div className="flex items-center gap-1.5 text-neutral-400 text-xs">
                        <Loader2 className="w-3 h-3 animate-spin" />
                        <span>Verificando cobertura...</span>
                      </div>
                    ) : coverage.covered === true ? (
                      <div className="flex items-center gap-1.5 text-green-400 text-xs">
                        <CheckCircle2 className="w-3 h-3" />
                        <span>Disponible en tu zona</span>
                      </div>
                    ) : coverage.covered === false ? (
                      <div className="flex items-center gap-1.5 text-red-400 text-xs">
                        <XCircle className="w-3 h-3" />
                        <span>No disponible en tu zona</span>
                      </div>
                    ) : null}
                  </div>
                )}
              </div>
            </div>
          </button>
        )
      })}
    </div>
  )
}
