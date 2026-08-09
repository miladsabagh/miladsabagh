import { describe, expect, it } from 'vitest';

import { calculateInvoicePricing, sumInvoicePricing } from './pricing';

describe('calculateInvoicePricing', () => {
  it('applies VAT only to wage and seller profit', () => {
    const pricing = calculateInvoicePricing({
      weightGrams: 10,
      goldPricePerGram: 1_000_000,
      wagePercent: 10,
      profitPercent: 7,
      vatPercent: 10,
    });

    expect(pricing).toEqual({
      pureGoldValue: 10_000_000,
      wage: 1_000_000,
      sellerProfit: 770_000,
      taxableAmount: 1_770_000,
      vat: 177_000,
      total: 11_947_000,
    });
  });

  it('accounts for multiple pieces and sums invoice lines', () => {
    const first = calculateInvoicePricing({
      weightGrams: 2,
      goldPricePerGram: 1_000,
      wagePercent: 10,
      quantity: 2,
    });
    const second = calculateInvoicePricing({
      weightGrams: 1,
      goldPricePerGram: 1_000,
      wagePercent: 0,
    });

    expect(sumInvoicePricing([first, second])).toEqual({
      pureGoldValue: 5_000,
      wage: 400,
      sellerProfit: 378,
      taxableAmount: 778,
      vat: 78,
      total: 5_856,
    });
  });
});
