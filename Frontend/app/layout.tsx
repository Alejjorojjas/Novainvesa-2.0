// Layout raíz mínimo — el HTML/body real lo provee app/[locale]/layout.tsx
// Next.js requiere este archivo pero delegamos toda la estructura al locale layout
export default function RootLayout({ children }: { children: React.ReactNode }) {
  return children
}
