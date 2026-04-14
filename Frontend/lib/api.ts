import axios, { AxiosError, type AxiosInstance } from 'axios'

const api: AxiosInstance = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:8080',
  timeout: 10_000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Request interceptor — agregar JWT si existe
api.interceptors.request.use(
  (config) => {
    if (typeof window !== 'undefined') {
      const token = localStorage.getItem('nova-token')
      if (token) {
        config.headers.Authorization = `Bearer ${token}`
      }
    }
    return config
  },
  (error) => Promise.reject(error)
)

// Response interceptor — manejo de errores estándar
api.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      if (typeof window !== 'undefined') {
        localStorage.removeItem('nova-token')
        localStorage.removeItem('nova-user')
        // Redirigir al login si es necesario (dejar al componente)
      }
    }
    return Promise.reject(error)
  }
)

export default api

// Helper para extraer datos de la respuesta estándar { success, data, error }
export function extractData<T>(response: {
  data: {
    success: boolean
    data: T
    error?: { code: string; message: string }
  }
}): T {
  if (!response.data.success) {
    throw new Error(response.data.error?.message ?? 'Error desconocido')
  }
  return response.data.data
}

// Helper para extraer el mensaje de error de una respuesta Axios
export function getErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    return (
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      (error.response?.data as any)?.error?.message ??
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      (error.response?.data as any)?.message ??
      error.message ??
      'Error de conexión'
    )
  }
  if (error instanceof Error) return error.message
  return 'Error desconocido'
}
