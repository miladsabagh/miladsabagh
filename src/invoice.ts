export type InvoiceItem = {
  id: string
  name: string
  weight: number
  wagePercent: number
  quantity: number
}

export type InvoiceLine = InvoiceItem & {
  goldValue: number
  wage: number
  profit: number
  tax: number
  total: number
}

export type InvoiceTotals = {
  lines: InvoiceLine[]
  weight: number
  goldValue: number
  wage: number
  profit: number
  tax: number
  total: number
}

export const PROFIT_PERCENT = 7
export const TAX_PERCENT = 10

export function calculateInvoiceLine(
  item: InvoiceItem,
  goldPrice: number,
): InvoiceLine {
  const goldValue = Math.round(item.weight * goldPrice * item.quantity)
  const wage = Math.round(goldValue * (item.wagePercent / 100))
  const profit = Math.round((goldValue + wage) * (PROFIT_PERCENT / 100))
  const tax = Math.round((wage + profit) * (TAX_PERCENT / 100))

  return {
    ...item,
    goldValue,
    wage,
    profit,
    tax,
    total: goldValue + wage + profit + tax,
  }
}

export function calculateInvoice(
  items: InvoiceItem[],
  goldPrice: number,
): InvoiceTotals {
  const lines = items.map((item) => calculateInvoiceLine(item, goldPrice))

  return lines.reduce<InvoiceTotals>(
    (totals, line) => ({
      lines,
      weight: totals.weight + line.weight * line.quantity,
      goldValue: totals.goldValue + line.goldValue,
      wage: totals.wage + line.wage,
      profit: totals.profit + line.profit,
      tax: totals.tax + line.tax,
      total: totals.total + line.total,
    }),
    {
      lines,
      weight: 0,
      goldValue: 0,
      wage: 0,
      profit: 0,
      tax: 0,
      total: 0,
    },
  )
}

export const formatToman = (value: number) =>
  `${new Intl.NumberFormat('fa-IR').format(Math.round(value))} تومان`

export const toPersianNumber = (value: string | number) =>
  new Intl.NumberFormat('fa-IR', { useGrouping: false }).format(Number(value))
