import { ImageSourcePropType } from 'react-native';

export type Product = {
  id: string;
  name: string;
  subtitle: string;
  category: 'انگشتر' | 'گردنبند' | 'گوشواره' | 'دستبند';
  purity: string;
  weight: number;
  price: number;
  previousPrice?: number;
  image: ImageSourcePropType;
  badge?: string;
};

export const products: Product[] = [
  {
    id: 'aurora-ring',
    name: 'انگشتر آورورا',
    subtitle: 'طراحی مینیمال با نگین برلیان',
    category: 'انگشتر',
    purity: '۱۸ عیار',
    weight: 4.2,
    price: 46_780_000,
    previousPrice: 49_200_000,
    image: require('../assets/ring.jpg'),
    badge: 'پرفروش',
  },
  {
    id: 'luna-necklace',
    name: 'گردنبند لونا',
    subtitle: 'زنجیر ظریف با آویز دست‌ساز',
    category: 'گردنبند',
    purity: '۱۸ عیار',
    weight: 7.8,
    price: 82_640_000,
    image: require('../assets/necklace.jpg'),
    badge: 'جدید',
  },
  {
    id: 'celine-earrings',
    name: 'گوشواره سلین',
    subtitle: 'فرم کلاسیک با درخشش ماندگار',
    category: 'گوشواره',
    purity: '۱۸ عیار',
    weight: 5.6,
    price: 61_390_000,
    image: require('../assets/earrings.jpg'),
  },
  {
    id: 'royal-bracelet',
    name: 'دستبند رویال',
    subtitle: 'بافت ایتالیایی و قفل ایمن',
    category: 'دستبند',
    purity: '۱۸ عیار',
    weight: 9.4,
    price: 98_750_000,
    previousPrice: 104_300_000,
    image: require('../assets/bracelet.jpg'),
    badge: 'ویژه',
  },
];

export const categories = [
  { id: 'all', title: 'همه محصولات', icon: '✦' },
  { id: 'انگشتر', title: 'انگشتر', icon: '◉' },
  { id: 'گردنبند', title: 'گردنبند', icon: '♢' },
  { id: 'گوشواره', title: 'گوشواره', icon: '◌' },
  { id: 'دستبند', title: 'دستبند', icon: '∞' },
] as const;

export const formatNumber = (value: number) =>
  new Intl.NumberFormat('fa-IR').format(Math.round(value));

export const formatToman = (value: number) => `${formatNumber(value)} تومان`;
