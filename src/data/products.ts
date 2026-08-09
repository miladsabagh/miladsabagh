export type ProductIcon = 'gem' | 'ring' | 'sparkles' | 'watch';

export type Product = {
  id: string;
  code: string;
  name: string;
  category: 'انگشتر' | 'دستبند' | 'گردنبند' | 'ساعت';
  weightGrams: number;
  karat: 18;
  wagePercent: number;
  icon: ProductIcon;
  colors: readonly [string, string];
  featured?: boolean;
};

export const products: Product[] = [
  {
    id: 'ring-aurora',
    code: 'ZR-1024',
    name: 'انگشتر اورورا',
    category: 'انگشتر',
    weightGrams: 4.26,
    karat: 18,
    wagePercent: 12,
    icon: 'ring',
    colors: ['#F3E4BC', '#D3A847'],
    featured: true,
  },
  {
    id: 'necklace-luna',
    code: 'ZR-2088',
    name: 'گردنبند لونا',
    category: 'گردنبند',
    weightGrams: 7.84,
    karat: 18,
    wagePercent: 14,
    icon: 'gem',
    colors: ['#E8E5E1', '#B9A68C'],
    featured: true,
  },
  {
    id: 'bracelet-ava',
    code: 'ZR-3157',
    name: 'دستبند آوا',
    category: 'دستبند',
    weightGrams: 9.12,
    karat: 18,
    wagePercent: 10,
    icon: 'sparkles',
    colors: ['#F6E2C9', '#C98D5C'],
    featured: true,
  },
  {
    id: 'watch-royal',
    code: 'ZR-4071',
    name: 'ساعت رویال',
    category: 'ساعت',
    weightGrams: 18.35,
    karat: 18,
    wagePercent: 16,
    icon: 'watch',
    colors: ['#DCE1DF', '#86958F'],
  },
  {
    id: 'ring-sahra',
    code: 'ZR-1083',
    name: 'انگشتر صحرا',
    category: 'انگشتر',
    weightGrams: 5.09,
    karat: 18,
    wagePercent: 11,
    icon: 'ring',
    colors: ['#F1DDCF', '#B97960'],
  },
  {
    id: 'necklace-niloufar',
    code: 'ZR-2120',
    name: 'گردنبند نیلوفر',
    category: 'گردنبند',
    weightGrams: 6.48,
    karat: 18,
    wagePercent: 13,
    icon: 'gem',
    colors: ['#E7E2F0', '#9780B1'],
  },
];

export const categories = [
  'همه',
  'انگشتر',
  'دستبند',
  'گردنبند',
  'ساعت',
] as const;
