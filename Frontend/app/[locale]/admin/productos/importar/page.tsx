'use client'

import { useEffect, useRef, useState } from 'react'
import { useParams } from 'next/navigation'
import Image from 'next/image'
import { Loader2, Eye, Download, Package } from 'lucide-react'
import { toast } from 'sonner'
import { AdminLayout } from '@/components/admin/AdminLayout'
import { getErrorMessage } from '@/lib/api'

interface PreviewProduct {
  name: string
  price: number
  images: string[]
  description: string
  dropiProductId: string
}

interface ImportForm {
  name: string
  price: number | ''
  categorySlug: string
  description: string
}

const inputClass =
  'w-full bg-neutral-800 border border-neutral-700 text-neutral-50 placeholder:text-neutral-500 rounded-lg px-3 py-2.5 text-sm focus:border-blue-600 focus:outline-none transition-colors'
const labelClass = 'block text-sm font-medium text-neutral-300 mb-1.5'

const formatCOP = (price: number) =>
  new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 }).format(price)

function getAdminToken(): string {
  if (typeof window === 'undefined') return ''
  return localStorage.getItem('nova-admin-token') ?? ''
}

async function adminPost<T>(path: string, body: unknown): Promise<T> {
  const token = getAdminToken()
  const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL ?? ''}${path}`, {
    method: 'POST',
    headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })
  const json = await res.json() as { success: boolean; data: T; error?: { message: string } }
  if (!json.success) throw new Error(json.error?.message ?? 'Error')
  return json.data
}

async function adminGet<T>(path: string): Promise<T> {
  const token = getAdminToken()
  const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL ?? ''}${path}`, {
    headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
  })
  const json = await res.json() as { success: boolean; data: T }
  if (!json.success) throw new Error('Error')
  return json.data
}

interface BulkProgress {
  processed: number
  total: number
  status: string
}

export default function ImportarPage() {
  const params = useParams()
  const locale = (params?.locale as string) ?? 'es'

  // ── Importación individual ──
  const [dropiId, setDropiId] = useState('')
  const [previewing, setPreviewing] = useState(false)
  const [preview, setPreview] = useState<PreviewProduct | null>(null)
  const [importForm, setImportForm] = useState<ImportForm>({
    name: '',
    price: '',
    categorySlug: '',
    description: '',
  })
  const [importing, setImporting] = useState(false)

  // ── Importación masiva ──
  const [bulkText, setBulkText] = useState('')
  const [bulkLoading, setBulkLoading] = useState(false)
  const [bulkProgress, setBulkProgress] = useState<BulkProgress | null>(null)
  const [jobId, setJobId] = useState<string | null>(null)
  const pollingRef = useRef<ReturnType<typeof setInterval> | null>(null)

  const handlePreview = async () => {
    if (!dropiId.trim()) return
    setPreviewing(true)
    setPreview(null)
    try {
      const data = await adminPost<PreviewProduct>('/api/v1/admin/products/dropi/preview', {
        dropiId: dropiId.trim(),
      })
      setPreview(data)
      setImportForm({
        name: data.name,
        price: data.price,
        categorySlug: '',
        description: data.description,
      })
    } catch (err) {
      toast.error(getErrorMessage(err))
    } finally {
      setPreviewing(false)
    }
  }

  const handleImport = async () => {
    if (!preview) return
    if (!importForm.categorySlug.trim()) {
      toast.error('La categoría es requerida')
      return
    }
    setImporting(true)
    try {
      await adminPost('/api/v1/admin/products/dropi/import', {
        dropiProductId: preview.dropiProductId,
        name: importForm.name,
        price: Number(importForm.price),
        categorySlug: importForm.categorySlug,
        description: importForm.description,
      })
      toast.success('Producto importado correctamente')
      setPreview(null)
      setDropiId('')
      setImportForm({ name: '', price: '', categorySlug: '', description: '' })
    } catch (err) {
      toast.error(getErrorMessage(err))
    } finally {
      setImporting(false)
    }
  }

  const handleBulkImport = async () => {
    const lines = bulkText
      .split('\n')
      .map((l) => l.trim())
      .filter(Boolean)
      .slice(0, 50)

    if (lines.length === 0) {
      toast.error('Agrega al menos un ID o URL')
      return
    }

    setBulkLoading(true)
    setBulkProgress(null)
    try {
      const data = await adminPost<{ jobId: string }>('/api/v1/admin/products/dropi/bulk-import', {
        items: lines,
      })
      setJobId(data.jobId)
      toast.success(`Importación iniciada: ${lines.length} productos`)
    } catch (err) {
      toast.error(getErrorMessage(err))
      setBulkLoading(false)
    }
  }

  // Polling de progreso
  useEffect(() => {
    if (!jobId) return

    const poll = async () => {
      try {
        const data = await adminGet<BulkProgress>(
          `/api/v1/admin/products/dropi/bulk-import/${jobId}/progress`
        )
        setBulkProgress(data)
        if (data.status === 'COMPLETED' || data.status === 'FAILED') {
          if (pollingRef.current) clearInterval(pollingRef.current)
          setBulkLoading(false)
          if (data.status === 'COMPLETED') toast.success('Importación masiva completada')
          else toast.error('La importación falló')
          setJobId(null)
        }
      } catch {
        // ignore poll errors
      }
    }

    pollingRef.current = setInterval(poll, 2000)
    return () => {
      if (pollingRef.current) clearInterval(pollingRef.current)
    }
  }, [jobId])

  const progressPct =
    bulkProgress && bulkProgress.total > 0
      ? Math.round((bulkProgress.processed / bulkProgress.total) * 100)
      : 0

  return (
    <AdminLayout locale={locale}>
      <div className="max-w-3xl space-y-8">
        <h1 className="text-2xl font-bold text-neutral-50">Importar productos de Dropi</h1>

        {/* ── Sección 1: Individual ── */}
        <div className="bg-neutral-900 border border-neutral-700 rounded-xl p-6 space-y-5">
          <h2 className="text-base font-semibold text-neutral-50 flex items-center gap-2">
            <Package className="w-4 h-4 text-blue-400" />
            Importación individual
          </h2>

          <div className="flex gap-3">
            <input
              value={dropiId}
              onChange={(e) => setDropiId(e.target.value)}
              placeholder="ID o URL del producto en Dropi"
              className={`${inputClass} flex-1`}
              onKeyDown={(e) => { if (e.key === 'Enter') handlePreview() }}
            />
            <button
              onClick={handlePreview}
              disabled={previewing || !dropiId.trim()}
              className="flex items-center gap-2 rounded-full bg-neutral-700 hover:bg-neutral-600 disabled:opacity-50 disabled:cursor-not-allowed text-neutral-50 font-semibold px-5 py-2.5 text-sm transition-colors shrink-0"
            >
              {previewing ? (
                <Loader2 className="w-4 h-4 animate-spin" />
              ) : (
                <Eye className="w-4 h-4" />
              )}
              {previewing ? 'Cargando...' : 'Previsualizar'}
            </button>
          </div>

          {/* Preview */}
          {preview && (
            <div className="border border-neutral-700 rounded-xl p-5 space-y-5">
              <div className="flex gap-4">
                {preview.images[0] && (
                  <div className="w-20 h-20 rounded-lg overflow-hidden bg-neutral-800 shrink-0">
                    <Image
                      src={preview.images[0]}
                      alt={preview.name}
                      width={80}
                      height={80}
                      className="w-full h-full object-cover"
                    />
                  </div>
                )}
                <div>
                  <p className="text-neutral-50 font-semibold">{preview.name}</p>
                  <p className="text-blue-400 font-bold mt-1">{formatCOP(preview.price)}</p>
                  <p className="text-neutral-500 text-xs mt-1 font-mono">ID: {preview.dropiProductId}</p>
                </div>
              </div>

              {/* Formulario editable */}
              <div className="space-y-4">
                <div>
                  <label className={labelClass}>Nombre del producto</label>
                  <input
                    value={importForm.name}
                    onChange={(e) => setImportForm((f) => ({ ...f, name: e.target.value }))}
                    className={inputClass}
                  />
                </div>
                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <label className={labelClass}>Precio (COP)</label>
                    <input
                      type="number"
                      value={importForm.price}
                      onChange={(e) => setImportForm((f) => ({ ...f, price: e.target.value === '' ? '' : Number(e.target.value) }))}
                      className={inputClass}
                    />
                  </div>
                  <div>
                    <label className={labelClass}>Categoría (slug)</label>
                    <input
                      value={importForm.categorySlug}
                      onChange={(e) => setImportForm((f) => ({ ...f, categorySlug: e.target.value }))}
                      placeholder="electronica"
                      className={inputClass}
                    />
                  </div>
                </div>
                <div>
                  <label className={labelClass}>Descripción</label>
                  <textarea
                    value={importForm.description}
                    onChange={(e) => setImportForm((f) => ({ ...f, description: e.target.value }))}
                    rows={3}
                    className={`${inputClass} resize-none`}
                  />
                </div>
              </div>

              <button
                onClick={handleImport}
                disabled={importing}
                className="flex items-center gap-2 rounded-full bg-blue-600 hover:bg-blue-700 disabled:opacity-60 disabled:cursor-not-allowed text-white font-semibold px-6 py-2.5 text-sm transition-colors"
              >
                {importing ? (
                  <>
                    <Loader2 className="w-4 h-4 animate-spin" />
                    Importando...
                  </>
                ) : (
                  <>
                    <Download className="w-4 h-4" />
                    Importar producto
                  </>
                )}
              </button>
            </div>
          )}
        </div>

        <hr className="border-neutral-800" />

        {/* ── Sección 2: Masiva ── */}
        <div className="bg-neutral-900 border border-neutral-700 rounded-xl p-6 space-y-5">
          <h2 className="text-base font-semibold text-neutral-50 flex items-center gap-2">
            <Download className="w-4 h-4 text-blue-400" />
            Importación masiva
          </h2>
          <p className="text-neutral-400 text-sm">
            Pega un ID o URL por línea (máximo 50 productos).
          </p>

          <textarea
            value={bulkText}
            onChange={(e) => setBulkText(e.target.value)}
            rows={8}
            placeholder={'12345\nhttps://dropi.co/producto/67890\n...'}
            className={`${inputClass} resize-none`}
          />

          <button
            onClick={handleBulkImport}
            disabled={bulkLoading || !bulkText.trim()}
            className="flex items-center gap-2 rounded-full bg-blue-600 hover:bg-blue-700 disabled:opacity-60 disabled:cursor-not-allowed text-white font-semibold px-6 py-2.5 text-sm transition-colors"
          >
            {bulkLoading ? (
              <>
                <Loader2 className="w-4 h-4 animate-spin" />
                Procesando...
              </>
            ) : (
              <>
                <Download className="w-4 h-4" />
                Importar lote
              </>
            )}
          </button>

          {/* Barra de progreso */}
          {bulkProgress && (
            <div className="space-y-2">
              <div className="flex justify-between text-xs text-neutral-400">
                <span>
                  {bulkProgress.processed} / {bulkProgress.total} productos procesados
                </span>
                <span>{progressPct}%</span>
              </div>
              <div className="w-full h-2 bg-neutral-800 rounded-full overflow-hidden">
                <div
                  className="h-full bg-blue-600 rounded-full transition-all duration-500"
                  style={{ width: `${progressPct}%` }}
                />
              </div>
              <p className="text-xs text-neutral-500 capitalize">{bulkProgress.status.toLowerCase()}</p>
            </div>
          )}
        </div>
      </div>
    </AdminLayout>
  )
}
