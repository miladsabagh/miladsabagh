import type { JewelryProduct } from "../lib/invoice";

export const catalog: JewelryProduct[] = [
  {
    id: "ring-rose-18k",
    name: "انگشتر رزگلد نگین دار",
    category: "ring",
    purityKarat: 18,
    weightGram: 3.42,
    makingFeePercent: 7,
    makingFeePerGram: 1850000,
    sellerProfitPercent: 7,
    stoneFee: 4200000,
    description: "طلای ۱۸ عیار با نگین اتمی و مناسب هدیه."
  },
  {
    id: "necklace-classic-18k",
    name: "گردنبند کلاسیک ۱۸ عیار",
    category: "necklace",
    purityKarat: 18,
    weightGram: 6.8,
    makingFeePercent: 6,
    makingFeePerGram: 2150000,
    sellerProfitPercent: 7,
    stoneFee: 0,
    description: "زنجیر ظریف روزمره با قفل ایمن."
  },
  {
    id: "bracelet-modern-18k",
    name: "دستبند کارتیر ظریف",
    category: "bracelet",
    purityKarat: 18,
    weightGram: 5.15,
    makingFeePercent: 8,
    makingFeePerGram: 2450000,
    sellerProfitPercent: 7,
    stoneFee: 0,
    description: "طراحی مدرن برای استفاده روزانه و مراسم."
  },
  {
    id: "earrings-pearl-18k",
    name: "گوشواره مرواریدی",
    category: "earrings",
    purityKarat: 18,
    weightGram: 2.1,
    makingFeePercent: 9,
    makingFeePerGram: 2650000,
    sellerProfitPercent: 7,
    stoneFee: 7800000,
    description: "ترکیب طلای زرد و مروارید پرورشی."
  }
];
