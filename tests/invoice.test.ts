import assert from "node:assert/strict";
import { describe, it } from "node:test";

import {
  buildInvoiceText,
  calculateInvoice,
  calculateInvoiceLine,
  createInvoiceNumber,
  type JewelryProduct
} from "../src/lib/invoice";

const ring: JewelryProduct = {
  id: "ring-test",
  name: "انگشتر تست",
  category: "ring",
  purityKarat: 18,
  weightGram: 2,
  makingFeePercent: 10,
  makingFeePerGram: 100,
  sellerProfitPercent: 5,
  stoneFee: 500,
  description: "نمونه تست"
};

describe("invoice pricing", () => {
  it("calculates gold value, making fee, profit, VAT and total per cart line", () => {
    const line = calculateInvoiceLine({ product: ring, quantity: 2 }, 1000, 10);

    assert.equal(line.weightGram, 4);
    assert.equal(line.goldValue, 4000);
    assert.equal(line.makingFee, 800);
    assert.equal(line.stoneFee, 1000);
    assert.equal(line.sellerProfit, 290);
    assert.equal(line.vat, 109);
    assert.equal(line.total, 6199);
  });

  it("caps invoice discount at subtotal and keeps payable non-negative", () => {
    const invoice = calculateInvoice([{ product: ring, quantity: 1 }], {
      goldPricePerGram: 1000,
      vatPercent: 10,
      discount: 999999,
      customer: { name: "مشتری", phone: "09120000000" }
    });

    assert.equal(invoice.subtotal, 3100);
    assert.equal(invoice.discount, 3100);
    assert.equal(invoice.payable, 0);
  });

  it("builds a shareable Persian invoice text", () => {
    const invoice = calculateInvoice([{ product: ring, quantity: 1 }], {
      goldPricePerGram: 1000,
      vatPercent: 10,
      discount: 100,
      customer: { name: "میلاد", phone: "0912" }
    });
    const text = buildInvoiceText(invoice);

    assert.match(text, /فاکتور فروش طلا و جواهر/);
    assert.match(text, /میلاد - 0912/);
    assert.match(text, /انگشتر تست x1/);
    assert.match(text, /مبلغ قابل پرداخت/);
  });

  it("generates deterministic invoice numbers for a provided date", () => {
    const date = new Date("2026-08-09T20:29:05Z");

    assert.equal(createInvoiceNumber(date), "GJ-20260809-73745");
  });
});
