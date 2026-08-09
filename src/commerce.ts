import type { CartLine, InvoiceTotals, Product } from './types';

export const SERVICE_TAX_RATE = 0.1;
export const FREE_SHIPPING_THRESHOLD = 50_000_000;
export const STANDARD_SHIPPING = 250_000;

export function productPrice(product: Product): number {
  return product.goldValue + product.serviceFee + Math.round(product.serviceFee * SERVICE_TAX_RATE);
}

export function cartLines(
  quantities: Record<string, number>,
  products: Product[],
): CartLine[] {
  return products
    .filter((product) => (quantities[product.id] ?? 0) > 0)
    .map((product) => ({
      product,
      quantity: quantities[product.id] ?? 0,
    }));
}

export function calculateInvoice(lines: CartLine[]): InvoiceTotals {
  const goldValue = lines.reduce(
    (sum, line) => sum + line.product.goldValue * line.quantity,
    0,
  );
  const serviceFee = lines.reduce(
    (sum, line) => sum + line.product.serviceFee * line.quantity,
    0,
  );
  const tax = Math.round(serviceFee * SERVICE_TAX_RATE);
  const merchandiseTotal = goldValue + serviceFee + tax;
  const shipping =
    merchandiseTotal >= FREE_SHIPPING_THRESHOLD || merchandiseTotal === 0
      ? 0
      : STANDARD_SHIPPING;
  const discount = lines.length >= 3 ? 500_000 : 0;

  return {
    goldValue,
    serviceFee,
    tax,
    shipping,
    discount,
    grandTotal: merchandiseTotal + shipping - discount,
  };
}

export function formatToman(value: number): string {
  return `${new Intl.NumberFormat('fa-IR').format(value)} تومان`;
}

export function formatNumber(value: number): string {
  return new Intl.NumberFormat('fa-IR', {
    maximumFractionDigits: 2,
  }).format(value);
}
