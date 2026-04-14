import { cn } from '@/lib/utils'

interface LoadingSpinnerProps {
  size?: 'sm' | 'md' | 'lg'
  className?: string
  label?: string
}

const sizeClasses = {
  sm: 'w-4 h-4 border-2',
  md: 'w-8 h-8 border-2',
  lg: 'w-12 h-12 border-[3px]',
}

export function LoadingSpinner({ size = 'md', className, label }: LoadingSpinnerProps) {
  return (
    <div
      className={cn('flex flex-col items-center justify-center gap-3', className)}
      role="status"
      aria-label={label ?? 'Cargando'}
    >
      <div
        className={cn(
          'rounded-full border-white/10 border-t-blue-500 animate-spin',
          sizeClasses[size]
        )}
      />
      {label && (
        <p className="text-sm text-neutral-500 animate-pulse">{label}</p>
      )}
      <span className="sr-only">{label ?? 'Cargando...'}</span>
    </div>
  )
}

// Variante para pantalla completa
export function FullPageSpinner() {
  return (
    <div className="min-h-screen bg-[#0A0A0A] flex items-center justify-center">
      <LoadingSpinner size="lg" label="Cargando..." />
    </div>
  )
}
