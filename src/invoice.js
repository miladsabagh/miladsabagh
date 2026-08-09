export const DEFAULT_GOLD_RATE = 45200000;

export const catalog = [
  {
    id: 'ring-solitaire',
    title: 'انگشتر نگین‌دار رزگلد',
    category: 'انگشتر',
    weightGram: 3.82,
    karat: 18,
    makingFeePercent: 12,
    profitPercent: 7,
    taxPercent: 10,
    imageTone: '#F9D9B7',
  },
  {
    id: 'bracelet-cuban',
    title: 'دستبند کارتیر کلاسیک',
    category: 'دستبند',
    weightGram: 8.45,
    karat: 18,
    makingFeePercent: 10,
    profitPercent: 7,
    taxPercent: 10,
    imageTone: '#F7C873',
  },
  {
    id: 'necklace-pearl',
    title: 'گردنبند مروارید و طلا',
    category: 'گردنبند',
    weightGram: 5.6,
    karat: 18,
    makingFeePercent: 14,
    profitPercent: 7,
    taxPercent: 10,
    imageTone: '#FFF4D7',
  },
];

export function roundToRial(value) {
  return Math.round(Number(value || 0));
}

export function calculateItem(product, goldRatePerGram = DEFAULT_GOLD_RATE, quantity = 1) {
  const safeQuantity = Math.max(1, Number(quantity) || 1);
  const goldValue = roundToRial(product.weightGram * goldRatePerGram * safeQuantity);
  const makingFee = roundToRial(goldValue * (product.makingFeePercent / 100));
  const profit = roundToRial((goldValue + makingFee) * (product.profitPercent / 100));
  const taxableAmount = makingFee + profit;
  const tax = roundToRial(taxableAmount * (product.taxPercent / 100));
  const total = goldValue + makingFee + profit + tax;

  return {
    ...product,
    quantity: safeQuantity,
    goldValue,
    makingFee,
    profit,
    taxableAmount,
    tax,
    total,
  };
}

export function calculateInvoice(items, goldRatePerGram = DEFAULT_GOLD_RATE) {
  const lines = items.map((item) => calculateItem(item, goldRatePerGram, item.quantity));
  const subtotalGold = lines.reduce((sum, item) => sum + item.goldValue, 0);
  const makingFee = lines.reduce((sum, item) => sum + item.makingFee, 0);
  const profit = lines.reduce((sum, item) => sum + item.profit, 0);
  const tax = lines.reduce((sum, item) => sum + item.tax, 0);
  const grandTotal = subtotalGold + makingFee + profit + tax;

  return {
    lines,
    subtotalGold,
    makingFee,
    profit,
    tax,
    grandTotal,
  };
}

export function formatRial(value) {
  return `${new Intl.NumberFormat('fa-IR').format(roundToRial(value))} ریال`;
}

export function makeInvoiceNumber(date = new Date()) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `MG-${year}${month}${day}-001`;
}
