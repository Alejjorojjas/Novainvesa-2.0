import { create } from 'zustand'
import { persist, createJSONStorage } from 'zustand/middleware'

export interface AuthUser {
  id: number
  email: string
  fullName: string
  role?: string
}

interface AuthState {
  user: AuthUser | null
  token: string | null
  isAuthenticated: boolean
  login: (user: AuthUser, token: string) => void
  logout: () => void
  setToken: (token: string) => void
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      token: null,
      isAuthenticated: false,

      login: (user, token) => {
        if (typeof window !== 'undefined') {
          localStorage.setItem('nova-token', token)
        }
        set({ user, token, isAuthenticated: true })
      },

      logout: () => {
        if (typeof window !== 'undefined') {
          localStorage.removeItem('nova-token')
        }
        set({ user: null, token: null, isAuthenticated: false })
      },

      setToken: (token) => {
        if (typeof window !== 'undefined') {
          localStorage.setItem('nova-token', token)
        }
        set({ token })
      },
    }),
    {
      name: 'novainvesa-auth',
      storage: createJSONStorage(() =>
        typeof window !== 'undefined' ? localStorage : ({} as Storage)
      ),
    }
  )
)
