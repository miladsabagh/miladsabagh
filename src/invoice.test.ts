import { describe, expect, it } from 'vitest'
import {
  calculateInvoice,
  calculateInvoiceLine,
  formatToman,
  type InvoiceItem,
} from './invoice'

const item: InvoiceItem = {
  id: 'test-ring',
  name: 'انگشتر تست',
  weight: 2,
  wagePercent: 10,
  quantity: 1,
}

describe('invoice calculations', () => {
  it('calculates gold, wage, profit, and tax independently', () => {
    const line = calculateInvoiceLine(item, 5_000_000)

    expect(line.goldValue).toBe(10_000_000)
    expect(line.wage).toBe(1_000_000)
    expect(line.profit).toBe(770_000)
    expect(line.tax).toBe(177_000)
    expect(line.total).toBe(11_947_000)
  })

  it('respects quantity and aggregates multiple invoice lines', () => {
    const totals = calculateInvoice(
      [
        { ...item, quantity: 2 },
        {
          id: 'test-necklace',
          name: 'گردنبند تست',
          weight: 1,
          wagePercent: 20,
          quantity: 1,
        },
      ],
      5_000_000,
    )

    expect(totals.lines).toHaveLength(2)
    expect(totals.weight).toBe(5)
    expect(totals.goldValue).toBe(25_000_000)
    expect(totals.total).toBe(30_456_000)
  })

  it('formats totals as Persian toman values', () => {
    expect(formatToman(5_842_000)).toContain('تومان')
    expect(formatToman(5_842_000)).toContain('۵')
  })
})
