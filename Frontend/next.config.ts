import type { NextConfig } from 'next'
import createNextIntlPlugin from 'next-intl/plugin'

const withNextIntl = createNextIntlPlugin('./i18n.ts')

const nextConfig: NextConfig = {
  // TODO: reactivar output: 'export' para deploy en Hostinger cuando next-intl
  // soporte static export completo con middleware de i18n
  // output: 'export',
  trailingSlash: true,
  images: {
    unoptimized: true, // requerido para static export
    remotePatterns: [
      { protocol: 'https', hostname: '**.dropi.co' },
      { protocol: 'https', hostname: '**.cloudfront.net' },
      { protocol: 'https', hostname: '**.novainvesa.com' },
      { protocol: 'https', hostname: 'via.placeholder.com' },
    ],
  },
  env: {
    NEXT_PUBLIC_API_URL: process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:8080',
  },
}

export default withNextIntl(nextConfig)
