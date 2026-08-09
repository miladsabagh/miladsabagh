export type CategoryId = 'all' | 'ring' | 'necklace' | 'bracelet' | 'earring';

export type Product = {
  id: string;
  name: string;
  category: Exclude<CategoryId, 'all'>;
  categoryLabel: string;
  image: string;
  weight: number;
  karat: 18;
  goldValue: number;
  serviceFee: number;
  rating: number;
  badge?: string;
};

export type CartLine = {
  product: Product;
  quantity: number;
};

export type InvoiceTotals = {
  goldValue: number;
  serviceFee: number;
  tax: number;
  shipping: number;
  discount: number;
  grandTotal: number;
};
