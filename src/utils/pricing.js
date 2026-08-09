export const DEFAULT_GOLD_GRAM_PRICE = 42000000;
export const DEFAULT_PROFIT_RATE = 0.07;
export const DEFAULT_TAX_RATE = 0.09;

const roundRial = (value) => Math.round(Number.isFinite(value) ? value : 0);

export function formatCurrencyFa(value) {
  return `${new Intl.NumberFormat("fa-IR").format(roundRial(value))} ریال`;
}

export function normalizeQuantity(value) {
  const quantity = Number(value);
  return Number.isFinite(quantity) && quantity > 0 ? Math.floor(quantity) : 0;
}

export function calculateLineItem(
  product,
  quantity,
  gramPrice18 = DEFAULT_GOLD_GRAM_PRICE,
  profitRate = DEFAULT_PROFIT_RATE,
  taxRate = DEFAULT_TAX_RATE
) {
  const itemQuantity = normalizeQuantity(quantity);
  const baseGoldPrice = product.weightGram * (product.karat / 18) * gramPrice18;
  const wage = baseGoldPrice * product.wagePercent;
  const profit = (baseGoldPrice + wage) * profitRate;
  const tax = (wage + profit) * taxRate;
  const unitTotal = roundRial(baseGoldPrice + wage + profit + tax);

  return {
    ...product,
    quantity: itemQuantity,
    baseGoldPrice: roundRial(baseGoldPrice),
    wage: roundRial(wage),
    profit: roundRial(profit),
    tax: roundRial(tax),
    unitTotal,
    lineTotal: unitTotal * itemQuantity
  };
}

export function calculateInvoice(cart, products, gramPrice18 = DEFAULT_GOLD_GRAM_PRICE) {
  const items = products
    .map((product) => calculateLineItem(product, cart[product.id] ?? 0, gramPrice18))
    .filter((item) => item.quantity > 0);

  const totals = items.reduce(
    (acc, item) => ({
      baseGoldPrice: acc.baseGoldPrice + item.baseGoldPrice * item.quantity,
      wage: acc.wage + item.wage * item.quantity,
      profit: acc.profit + item.profit * item.quantity,
      tax: acc.tax + item.tax * item.quantity,
      payable: acc.payable + item.lineTotal
    }),
    {
      baseGoldPrice: 0,
      wage: 0,
      profit: 0,
      tax: 0,
      payable: 0
    }
  );

  return {
    items,
    totals,
    issuedAt: new Date().toISOString()
  };
}

export function createInvoiceText(invoice, gramPrice18 = DEFAULT_GOLD_GRAM_PRICE) {
  const invoiceNumber = `MS-${new Date(invoice.issuedAt).getTime().toString().slice(-8)}`;
  const issuedDate = new Intl.DateTimeFormat("fa-IR", {
    dateStyle: "medium",
    timeStyle: "short"
  }).format(new Date(invoice.issuedAt));
  const lines = invoice.items.map(
    (item, index) =>
      `${index + 1}. ${item.title} | ${item.quantity} عدد | ${item.weightGram} گرم | ${formatCurrencyFa(item.lineTotal)}`
  );

  return [
    "فاکتور فروش گالری میلاد صباغ",
    `شماره فاکتور: ${invoiceNumber}`,
    `تاریخ صدور: ${issuedDate}`,
    `قیمت هر گرم طلای ۱۸ عیار: ${formatCurrencyFa(gramPrice18)}`,
    "",
    "اقلام:",
    ...lines,
    "",
    `جمع طلای خام: ${formatCurrencyFa(invoice.totals.baseGoldPrice)}`,
    `جمع اجرت ساخت: ${formatCurrencyFa(invoice.totals.wage)}`,
    `سود فروشنده: ${formatCurrencyFa(invoice.totals.profit)}`,
    `مالیات: ${formatCurrencyFa(invoice.totals.tax)}`,
    `مبلغ قابل پرداخت: ${formatCurrencyFa(invoice.totals.payable)}`,
    "",
    "با تشکر از خرید شما"
  ].join("\n");
}
