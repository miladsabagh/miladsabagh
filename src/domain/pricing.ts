export type InvoicePricingInput = {
  weightGrams: number;
  goldPricePerGram: number;
  wagePercent: number;
  profitPercent?: number;
  vatPercent?: number;
  quantity?: number;
};

export type InvoicePricing = {
  pureGoldValue: number;
  wage: number;
  sellerProfit: number;
  taxableAmount: number;
  vat: number;
  total: number;
};

const roundRials = (value: number) => Math.round(value);

/**
 * Calculates an Iranian jewelry invoice line.
 * Gold principal is VAT-exempt; VAT applies only to wage and seller profit.
 */
export function calculateInvoicePricing({
  weightGrams,
  goldPricePerGram,
  wagePercent,
  profitPercent = 7,
  vatPercent = 10,
  quantity = 1,
}: InvoicePricingInput): InvoicePricing {
  const pureGoldValue = roundRials(weightGrams * goldPricePerGram * quantity);
  const wage = roundRials(pureGoldValue * (wagePercent / 100));
  const sellerProfit = roundRials(
    (pureGoldValue + wage) * (profitPercent / 100),
  );
  const taxableAmount = wage + sellerProfit;
  const vat = roundRials(taxableAmount * (vatPercent / 100));

  return {
    pureGoldValue,
    wage,
    sellerProfit,
    taxableAmount,
    vat,
    total: pureGoldValue + taxableAmount + vat,
  };
}

export function sumInvoicePricing(lines: InvoicePricing[]): InvoicePricing {
  return lines.reduce<InvoicePricing>(
    (total, line) => ({
      pureGoldValue: total.pureGoldValue + line.pureGoldValue,
      wage: total.wage + line.wage,
      sellerProfit: total.sellerProfit + line.sellerProfit,
      taxableAmount: total.taxableAmount + line.taxableAmount,
      vat: total.vat + line.vat,
      total: total.total + line.total,
    }),
    {
      pureGoldValue: 0,
      wage: 0,
      sellerProfit: 0,
      taxableAmount: 0,
      vat: 0,
      total: 0,
    },
  );
}
