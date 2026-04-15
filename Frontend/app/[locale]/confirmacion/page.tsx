import { setRequestLocale } from 'next-intl/server'
import { ConfirmacionClient } from './ConfirmacionClient'

interface Props {
  params: Promise<{ locale: string }>
  searchParams: Promise<{ orderCode?: string; paymentMethod?: string }>
}

export default async function ConfirmacionPage({ params, searchParams }: Props) {
  const { locale } = await params
  const { orderCode, paymentMethod } = await searchParams
  setRequestLocale(locale)

  return <ConfirmacionClient orderCode={orderCode ?? ''} paymentMethod={paymentMethod} />
}
