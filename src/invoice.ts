import { Product, formatNumber } from './catalog';

export type CartLine = {
  product: Product;
  quantity: number;
};

export type InvoiceTotals = {
  subtotal: number;
  makingFee: number;
  tax: number;
  total: number;
};

export const calculateInvoiceTotals = (lines: CartLine[]): InvoiceTotals => {
  const subtotal = lines.reduce(
    (sum, line) => sum + line.product.price * line.quantity,
    0,
  );
  const makingFee = Math.round(subtotal * 0.07);
  const tax = Math.round(makingFee * 0.1);

  return {
    subtotal,
    makingFee,
    tax,
    total: subtotal + makingFee + tax,
  };
};

export const createInvoiceNumber = () => {
  const stamp = Date.now().toString().slice(-7);
  return `AR-${stamp}`;
};

type InvoiceHtmlOptions = {
  lines: CartLine[];
  totals: InvoiceTotals;
  invoiceNumber: string;
  customerName: string;
  customerPhone: string;
  dateLabel: string;
};

export const buildInvoiceHtml = ({
  lines,
  totals,
  invoiceNumber,
  customerName,
  customerPhone,
  dateLabel,
}: InvoiceHtmlOptions) => {
  const lineRows = lines
    .map(
      ({ product, quantity }) => `
        <tr>
          <td>
            <strong>${product.name}</strong>
            <small>${product.purity} • ${formatNumber(product.weight)} گرم</small>
          </td>
          <td>${formatNumber(quantity)}</td>
          <td>${formatNumber(product.price)}</td>
          <td>${formatNumber(product.price * quantity)}</td>
        </tr>`,
    )
    .join('');

  return `<!doctype html>
  <html lang="fa" dir="rtl">
    <head>
      <meta charset="utf-8" />
      <meta name="viewport" content="width=device-width, initial-scale=1" />
      <style>
        @page { margin: 28px; }
        * { box-sizing: border-box; }
        body {
          margin: 0;
          color: #26191b;
          background: #ffffff;
          font-family: Tahoma, Arial, sans-serif;
          direction: rtl;
        }
        .sheet {
          max-width: 760px;
          margin: 0 auto;
          border: 1px solid #ded5c4;
          border-radius: 18px;
          overflow: hidden;
        }
        header {
          color: #fffaf0;
          background: #401522;
          padding: 28px 32px;
          display: flex;
          justify-content: space-between;
          align-items: center;
        }
        .brand { font-size: 28px; font-weight: 800; }
        .brand small { display: block; font-size: 11px; font-weight: 400; margin-top: 4px; color: #d8bd82; }
        .invoice-title { text-align: left; }
        .invoice-title h1 { margin: 0 0 5px; font-size: 20px; }
        .invoice-title span { color: #d8bd82; font-size: 12px; }
        main { padding: 28px 32px; }
        .meta {
          display: grid;
          grid-template-columns: 1fr 1fr;
          gap: 12px;
          padding: 18px;
          background: #f8f4ec;
          border-radius: 12px;
          margin-bottom: 24px;
        }
        .meta span { display: block; color: #786d6e; font-size: 11px; margin-bottom: 5px; }
        .meta strong { font-size: 13px; }
        table { width: 100%; border-collapse: collapse; }
        th {
          color: #7b6e70;
          font-size: 11px;
          font-weight: 600;
          text-align: right;
          border-bottom: 1px solid #ded5c4;
          padding: 12px 8px;
        }
        td {
          font-size: 12px;
          border-bottom: 1px solid #eee8dd;
          padding: 16px 8px;
        }
        td small { display: block; color: #8b7f80; margin-top: 5px; }
        .totals {
          width: 290px;
          margin: 24px 0 0 auto;
        }
        .total-row {
          display: flex;
          justify-content: space-between;
          padding: 7px 0;
          color: #685d5e;
          font-size: 12px;
        }
        .total-row.final {
          border-top: 1px solid #c9b998;
          color: #401522;
          font-size: 16px;
          font-weight: 800;
          margin-top: 5px;
          padding-top: 14px;
        }
        footer {
          padding: 18px 32px;
          text-align: center;
          color: #807476;
          background: #f8f4ec;
          font-size: 10px;
          line-height: 1.8;
        }
      </style>
    </head>
    <body>
      <section class="sheet">
        <header>
          <div class="brand">آتلیه زرین<small>طلا و جواهرات اصیل</small></div>
          <div class="invoice-title">
            <h1>فاکتور فروش</h1>
            <span>${invoiceNumber}</span>
          </div>
        </header>
        <main>
          <div class="meta">
            <div><span>خریدار</span><strong>${customerName || 'مشتری گرامی'}</strong></div>
            <div><span>شماره تماس</span><strong>${customerPhone || '—'}</strong></div>
            <div><span>تاریخ صدور</span><strong>${dateLabel}</strong></div>
            <div><span>عیار مورد معامله</span><strong>طلای ۱۸ عیار (۷۵۰)</strong></div>
          </div>
          <table>
            <thead>
              <tr><th>شرح کالا</th><th>تعداد</th><th>فی واحد (تومان)</th><th>مبلغ (تومان)</th></tr>
            </thead>
            <tbody>${lineRows}</tbody>
          </table>
          <div class="totals">
            <div class="total-row"><span>جمع کالاها</span><strong>${formatNumber(totals.subtotal)}</strong></div>
            <div class="total-row"><span>اجرت ساخت (۷٪)</span><strong>${formatNumber(totals.makingFee)}</strong></div>
            <div class="total-row"><span>مالیات اجرت (۱۰٪)</span><strong>${formatNumber(totals.tax)}</strong></div>
            <div class="total-row final"><span>مبلغ قابل پرداخت</span><strong>${formatNumber(totals.total)} تومان</strong></div>
          </div>
        </main>
        <footer>
          اصالت و عیار تمامی کالاها تضمین شده است. این سند به‌صورت الکترونیکی صادر شده و معتبر است.<br />
          آتلیه زرین • تهران، الهیه • ۰۲۱-۲۲۶۶ ۴۸۲۰
        </footer>
      </section>
    </body>
  </html>`;
};
