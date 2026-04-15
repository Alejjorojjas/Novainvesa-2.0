import { setRequestLocale } from 'next-intl/server'
import { CheckoutPageClient } from './CheckoutPageClient'

interface Props {
  params: Promise<{ locale: string }>
}

export default async function CheckoutPage({ params }: Props) {
  const { locale } = await params
  setRequestLocale(locale)
  return <CheckoutPageClient />
}
