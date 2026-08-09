export type ProductCategory = "ring" | "necklace" | "bracelet" | "earrings" | "coin" | "gemstone";

export interface JewelryProduct {
  id: string;
  name: string;
  category: ProductCategory;
  purityKarat: number;
  weightGram: number;
  makingFeePercent: number;
  makingFeePerGram: number;
  sellerProfitPercent: number;
  stoneFee: number;
  description: string;
}

export interface CartItem {
  product: JewelryProduct;
  quantity: number;
}

export interface InvoiceCustomer {
  name: string;
  phone: string;
}

export interface InvoiceOptions {
  goldPricePerGram: number;
  vatPercent: number;
  discount: number;
  customer: InvoiceCustomer;
}

export interface InvoiceLine {
  id: string;
  name: string;
  quantity: number;
  weightGram: number;
  goldValue: number;
  makingFee: number;
  sellerProfit: number;
  stoneFee: number;
  vat: number;
  total: number;
}

export interface InvoiceSummary {
  invoiceNumber: string;
  issuedAt: string;
  customer: InvoiceCustomer;
  lines: InvoiceLine[];
  subtotal: number;
  discount: number;
  payable: number;
  totalWeightGram: number;
  goldPricePerGram: number;
  vatPercent: number;
}

const DEFAULT_LOCALE = "fa-IR";

export function roundRial(value: number): number {
  return Math.round(value);
}

export function sanitizeMoney(value: number): number {
  if (!Number.isFinite(value) || value < 0) {
    return 0;
  }

  return value;
}

export function calculateInvoiceLine(item: CartItem, goldPricePerGram: number, vatPercent: number): InvoiceLine {
  const quantity = Math.max(1, Math.floor(item.quantity));
  const product = item.product;
  const goldValuePerItem = product.weightGram * sanitizeMoney(goldPricePerGram);
  const makingFeePerItem = product.weightGram * product.makingFeePerGram + goldValuePerItem * (product.makingFeePercent / 100);
  const profitBase = goldValuePerItem + makingFeePerItem + product.stoneFee;
  const sellerProfitPerItem = profitBase * (product.sellerProfitPercent / 100);

  // Current Iranian jewelry invoices normally apply VAT to making fee and seller profit, not raw gold value.
  const vatPerItem = (makingFeePerItem + sellerProfitPerItem) * (sanitizeMoney(vatPercent) / 100);
  const lineTotalPerItem = goldValuePerItem + product.stoneFee + makingFeePerItem + sellerProfitPerItem + vatPerItem;

  return {
    id: product.id,
    name: product.name,
    quantity,
    weightGram: roundRial(product.weightGram * quantity * 1000) / 1000,
    goldValue: roundRial(goldValuePerItem * quantity),
    makingFee: roundRial(makingFeePerItem * quantity),
    sellerProfit: roundRial(sellerProfitPerItem * quantity),
    stoneFee: roundRial(product.stoneFee * quantity),
    vat: roundRial(vatPerItem * quantity),
    total: roundRial(lineTotalPerItem * quantity)
  };
}

export function calculateInvoice(cart: CartItem[], options: InvoiceOptions): InvoiceSummary {
  const lines = cart
    .filter((item) => item.quantity > 0)
    .map((item) => calculateInvoiceLine(item, options.goldPricePerGram, options.vatPercent));
  const subtotal = roundRial(lines.reduce((sum, line) => sum + line.total, 0));
  const discount = Math.min(roundRial(sanitizeMoney(options.discount)), subtotal);
  const payable = roundRial(subtotal - discount);
  const totalWeightGram = roundRial(lines.reduce((sum, line) => sum + line.weightGram, 0) * 1000) / 1000;

  return {
    invoiceNumber: createInvoiceNumber(),
    issuedAt: new Date().toISOString(),
    customer: options.customer,
    lines,
    subtotal,
    discount,
    payable,
    totalWeightGram,
    goldPricePerGram: sanitizeMoney(options.goldPricePerGram),
    vatPercent: sanitizeMoney(options.vatPercent)
  };
}

export function createInvoiceNumber(date = new Date()): string {
  const y = date.getFullYear().toString();
  const m = String(date.getMonth() + 1).padStart(2, "0");
  const d = String(date.getDate()).padStart(2, "0");
  const seconds = String(date.getHours() * 3600 + date.getMinutes() * 60 + date.getSeconds()).padStart(5, "0");

  return `GJ-${y}${m}${d}-${seconds}`;
}

export function formatRial(value: number, locale = DEFAULT_LOCALE): string {
  return `${new Intl.NumberFormat(locale).format(roundRial(value))} ریال`;
}

export function formatGram(value: number, locale = DEFAULT_LOCALE): string {
  return `${new Intl.NumberFormat(locale, { maximumFractionDigits: 3 }).format(value)} گرم`;
}

export function buildInvoiceText(invoice: InvoiceSummary): string {
  const lines = invoice.lines
    .map((line, index) => {
      return `${index + 1}. ${line.name} x${line.quantity} | وزن ${formatGram(line.weightGram)} | مبلغ ${formatRial(line.total)}`;
    })
    .join("\n");

  return [
    "فاکتور فروش طلا و جواهر",
    `شماره: ${invoice.invoiceNumber}`,
    `مشتری: ${invoice.customer.name || "ثبت نشده"} - ${invoice.customer.phone || "بدون تلفن"}`,
    `نرخ هر گرم طلا: ${formatRial(invoice.goldPricePerGram)}`,
    lines || "سبد خرید خالی است.",
    `جمع کل: ${formatRial(invoice.subtotal)}`,
    `تخفیف: ${formatRial(invoice.discount)}`,
    `مبلغ قابل پرداخت: ${formatRial(invoice.payable)}`
  ].join("\n");
}
