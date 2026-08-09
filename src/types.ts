export type Category = 'gold' | 'coin' | 'jewelry';

export interface Product {
  id: string;
  code: string;
  name: string;
  category: Category;
  karat: number;
  weight: number;
  wagePercent: number;
  stock: number;
  accent: string;
}

export interface Settings {
  shopName: string;
  shopPhone: string;
  shopAddress: string;
  pricePerGram18: number;
  profitPercent: number;
  taxPercent: number;
}

export interface InvoiceItem {
  productId: string;
  name: string;
  category: Category;
  karat: number;
  weight: number;
  wagePercent: number;
  quantity: number;
  goldValue: number;
  wage: number;
  profit: number;
  tax: number;
  lineTotal: number;
}

export interface Invoice {
  id: string;
  number: number;
  createdAt: number;
  customerName: string;
  customerPhone: string;
  goldPricePerGram18: number;
  items: InvoiceItem[];
  discount: number;
  subtotalGold: number;
  totalWage: number;
  totalProfit: number;
  totalTax: number;
  grandTotal: number;
}

export const CATEGORY_LABELS: Record<Category, string> = {
  gold: 'طلا',
  coin: 'سکه',
  jewelry: 'جواهر',
};
