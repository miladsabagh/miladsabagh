import assert from "node:assert/strict";
import test from "node:test";

import {
  calculateInvoice,
  calculateLineItem,
  createInvoiceText,
  formatCurrencyFa,
  normalizeQuantity
} from "./pricing.js";

const sampleProduct = {
  id: "sample-ring",
  title: "انگشتر تست",
  category: "انگشتر",
  karat: 18,
  weightGram: 2,
  wagePercent: 0.1
};

test("calculateLineItem applies gold price, wage, profit, tax, and quantity", () => {
  const line = calculateLineItem(sampleProduct, 2, 1000);

  assert.equal(line.baseGoldPrice, 2000);
  assert.equal(line.wage, 200);
  assert.equal(line.profit, 154);
  assert.equal(line.tax, 32);
  assert.equal(line.unitTotal, 2386);
  assert.equal(line.lineTotal, 4772);
});

test("calculateInvoice skips zero quantity products and totals selected items", () => {
  const invoice = calculateInvoice({ "sample-ring": 2, missing: 3 }, [sampleProduct], 1000);

  assert.equal(invoice.items.length, 1);
  assert.equal(invoice.items[0].quantity, 2);
  assert.deepEqual(invoice.totals, {
    baseGoldPrice: 4000,
    wage: 400,
    profit: 308,
    tax: 64,
    payable: 4772
  });
});

test("normalizeQuantity prevents negative and fractional quantities", () => {
  assert.equal(normalizeQuantity(-1), 0);
  assert.equal(normalizeQuantity(2.8), 2);
  assert.equal(normalizeQuantity("3"), 3);
});

test("createInvoiceText includes customer-facing invoice details", () => {
  const invoice = calculateInvoice({ "sample-ring": 1 }, [sampleProduct], 1000);
  const invoiceText = createInvoiceText(invoice, 1000);

  assert.match(invoiceText, /فاکتور فروش گالری میلاد صباغ/);
  assert.match(invoiceText, /انگشتر تست/);
  assert.match(invoiceText, /مبلغ قابل پرداخت/);
  assert.match(invoiceText, /ریال/);
});

test("formatCurrencyFa returns a Persian rial label", () => {
  assert.match(formatCurrencyFa(123456), /ریال$/);
});
