import type { CategoryId, Product } from './types';

export const categories: Array<{
  id: CategoryId;
  label: string;
  icon: string;
}> = [
  { id: 'all', label: 'همه', icon: '✦' },
  { id: 'ring', label: 'انگشتر', icon: '◌' },
  { id: 'necklace', label: 'گردنبند', icon: '⌁' },
  { id: 'bracelet', label: 'دستبند', icon: '⊂' },
  { id: 'earring', label: 'گوشواره', icon: '♢' },
];

export const products: Product[] = [
  {
    id: 'p1',
    name: 'انگشتر طلای آترین',
    category: 'ring',
    categoryLabel: 'انگشتر',
    image:
      'https://images.unsplash.com/photo-1605100804763-247f67b3557e?auto=format&fit=crop&w=900&q=88',
    weight: 3.24,
    karat: 18,
    goldValue: 40435200,
    serviceFee: 6750000,
    rating: 4.9,
    badge: 'پرفروش',
  },
  {
    id: 'p2',
    name: 'گردنبند مینیمال ماه',
    category: 'necklace',
    categoryLabel: 'گردنبند',
    image:
      'https://images.unsplash.com/photo-1599643478518-a784e5dc4c8f?auto=format&fit=crop&w=900&q=88',
    weight: 5.12,
    karat: 18,
    goldValue: 63897600,
    serviceFee: 8240000,
    rating: 4.8,
    badge: 'جدید',
  },
  {
    id: 'p3',
    name: 'دستبند زنجیری کارتیه',
    category: 'bracelet',
    categoryLabel: 'دستبند',
    image:
      'https://images.unsplash.com/photo-1611591437281-460bfbe1220a?auto=format&fit=crop&w=900&q=88',
    weight: 7.4,
    karat: 18,
    goldValue: 92352000,
    serviceFee: 10850000,
    rating: 4.7,
  },
  {
    id: 'p4',
    name: 'گوشواره حلقه‌ای لیا',
    category: 'earring',
    categoryLabel: 'گوشواره',
    image:
      'https://images.unsplash.com/photo-1535632066927-ab7c9ab60908?auto=format&fit=crop&w=900&q=88',
    weight: 2.86,
    karat: 18,
    goldValue: 35692800,
    serviceFee: 5490000,
    rating: 4.9,
    badge: 'محبوب',
  },
  {
    id: 'p5',
    name: 'انگشتر نگین‌دار رومیسا',
    category: 'ring',
    categoryLabel: 'انگشتر',
    image:
      'https://images.unsplash.com/photo-1603561596112-db1d128282d5?auto=format&fit=crop&w=900&q=88',
    weight: 4.1,
    karat: 18,
    goldValue: 51168000,
    serviceFee: 7350000,
    rating: 4.6,
  },
  {
    id: 'p6',
    name: 'گردنبند مروارید و طلا',
    category: 'necklace',
    categoryLabel: 'گردنبند',
    image:
      'https://images.unsplash.com/photo-1599459183200-59c7687a0275?auto=format&fit=crop&w=900&q=88',
    weight: 4.82,
    karat: 18,
    goldValue: 60153600,
    serviceFee: 9720000,
    rating: 4.8,
  },
];
