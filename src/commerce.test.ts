import { describe, expect, it } from 'vitest';

import {
  FREE_SHIPPING_THRESHOLD,
  STANDARD_SHIPPING,
  calculateInvoice,
  cartLines,
  productPrice,
} from './commerce';
import type { Product } from './types';

const ring: Product = {
  id: 'ring',
  name: 'انگشتر تست',
  category: 'ring',
  categoryLabel: 'انگشتر',
  image: '',
  weight: 2,
  karat: 18,
  goldValue: 20_000_000,
  serviceFee: 2_000_000,
  rating: 5,
};

const necklace: Product = {
  ...ring,
  id: 'necklace',
  name: 'گردنبند تست',
  category: 'necklace',
  categoryLabel: 'گردنبند',
  goldValue: 30_000_000,
  serviceFee: 3_000_000,
};

describe('commerce calculations', () => {
  it('adds service tax to the displayed product price', () => {
    expect(productPrice(ring)).toBe(22_200_000);
  });

  it('creates cart lines only for products with a positive quantity', () => {
    expect(cartLines({ ring: 2, necklace: 0 }, [ring, necklace])).toEqual([
      { product: ring, quantity: 2 },
    ]);
  });

  it('calculates a transparent invoice and free insured shipping', () => {
    const totals = calculateInvoice([
      { product: ring, quantity: 1 },
      { product: necklace, quantity: 1 },
    ]);

    expect(totals).toEqual({
      goldValue: 50_000_000,
      serviceFee: 5_000_000,
      tax: 500_000,
      shipping: 0,
      discount: 0,
      grandTotal: 55_500_000,
    });
    expect(totals.grandTotal).toBeGreaterThan(FREE_SHIPPING_THRESHOLD);
  });

  it('charges standard shipping below the free-shipping threshold', () => {
    const totals = calculateInvoice([{ product: ring, quantity: 1 }]);

    expect(totals.shipping).toBe(STANDARD_SHIPPING);
    expect(totals.grandTotal).toBe(22_450_000);
  });
});
