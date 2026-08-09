import assert from 'node:assert/strict';
import test from 'node:test';

import { calculateInvoice, calculateItem, formatRial, makeInvoiceNumber } from '../src/invoice.js';

const sampleProduct = {
  id: 'sample',
  title: 'نیم ست تست',
  category: 'نیم ست',
  weightGram: 2,
  karat: 18,
  makingFeePercent: 10,
  profitPercent: 5,
  taxPercent: 10,
};

test('calculateItem applies gold value, making fee, profit, and tax', () => {
  const item = calculateItem(sampleProduct, 1000, 2);

  assert.equal(item.goldValue, 4000);
  assert.equal(item.makingFee, 400);
  assert.equal(item.profit, 220);
  assert.equal(item.tax, 62);
  assert.equal(item.total, 4682);
});

test('calculateInvoice sums line totals and invoice sections', () => {
  const invoice = calculateInvoice([{ ...sampleProduct, quantity: 1 }], 1000);

  assert.equal(invoice.lines.length, 1);
  assert.equal(invoice.subtotalGold, 2000);
  assert.equal(invoice.makingFee, 200);
  assert.equal(invoice.profit, 110);
  assert.equal(invoice.tax, 31);
  assert.equal(invoice.grandTotal, 2341);
});

test('formatRial and makeInvoiceNumber produce customer-facing values', () => {
  assert.equal(formatRial(1234567), '۱٬۲۳۴٬۵۶۷ ریال');
  assert.equal(makeInvoiceNumber(new Date('2026-08-09T12:00:00Z')), 'MG-20260809-001');
});
