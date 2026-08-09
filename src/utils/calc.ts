import { InvoiceItem, Product, Settings } from '../types';

export interface DraftLine {
  product: Product;
  quantity: number;
}

export function pricePerGram(pricePerGram18: number, karat: number): number {
  return (pricePerGram18 * karat) / 18;
}

// Iranian gold invoice model:
//  goldValue = weight * pricePerGram(karat) * qty
//  wage      = goldValue * wagePercent
//  profit    = (goldValue + wage) * profitPercent
//  tax (VAT) = (wage + profit) * taxPercent     -> VAT applies to wage + profit only
//  lineTotal = goldValue + wage + profit + tax
export function computeLine(
  product: Product,
  quantity: number,
  settings: Settings,
): InvoiceItem {
  const unitGold = product.weight * pricePerGram(settings.pricePerGram18, product.karat);
  const goldValue = unitGold * quantity;
  const wage = goldValue * (product.wagePercent / 100);
  const profit = (goldValue + wage) * (settings.profitPercent / 100);
  const tax = (wage + profit) * (settings.taxPercent / 100);
  const lineTotal = goldValue + wage + profit + tax;

  return {
    productId: product.id,
    name: product.name,
    category: product.category,
    karat: product.karat,
    weight: product.weight,
    wagePercent: product.wagePercent,
    quantity,
    goldValue,
    wage,
    profit,
    tax,
    lineTotal,
  };
}

export interface InvoiceTotals {
  items: InvoiceItem[];
  subtotalGold: number;
  totalWage: number;
  totalProfit: number;
  totalTax: number;
  itemsTotal: number;
  grandTotal: number;
}

export function computeInvoice(
  lines: DraftLine[],
  settings: Settings,
  discount: number,
): InvoiceTotals {
  const items = lines
    .filter((l) => l.quantity > 0)
    .map((l) => computeLine(l.product, l.quantity, settings));

  const subtotalGold = items.reduce((a, b) => a + b.goldValue, 0);
  const totalWage = items.reduce((a, b) => a + b.wage, 0);
  const totalProfit = items.reduce((a, b) => a + b.profit, 0);
  const totalTax = items.reduce((a, b) => a + b.tax, 0);
  const itemsTotal = items.reduce((a, b) => a + b.lineTotal, 0);
  const grandTotal = Math.max(0, itemsTotal - discount);

  return {
    items,
    subtotalGold,
    totalWage,
    totalProfit,
    totalTax,
    itemsTotal,
    grandTotal,
  };
}
