import {
  Vazirmatn_400Regular,
  Vazirmatn_500Medium,
  Vazirmatn_600SemiBold,
  Vazirmatn_700Bold,
  Vazirmatn_800ExtraBold,
  useFonts,
} from '@expo-google-fonts/vazirmatn';
import { LinearGradient } from 'expo-linear-gradient';
import * as Print from 'expo-print';
import * as Sharing from 'expo-sharing';
import { StatusBar } from 'expo-status-bar';
import {
  BadgeCheck,
  Bell,
  Calculator,
  ChevronLeft,
  Circle as CircleIcon,
  Clock3,
  FileText,
  Gem,
  HandCoins,
  Home,
  Minus,
  Package,
  Plus,
  ReceiptText,
  ScanLine,
  Search,
  Share2,
  ShoppingBag,
  Sparkles,
  Store,
  Trash2,
  TrendingUp,
  UserRound,
  WalletCards,
  Watch,
  X,
} from 'lucide-react-native';
import { useEffect, useMemo, useState } from 'react';
import type { ComponentProps, ReactNode } from 'react';
import {
  ActivityIndicator,
  Alert,
  Modal,
  Platform,
  Pressable,
  SafeAreaView,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';
import Svg, {
  Circle as SvgCircle,
  Defs,
  LinearGradient as SvgLinearGradient,
  Path,
  Stop,
} from 'react-native-svg';

import { categories, products } from './src/data/products';
import type { Product, ProductIcon } from './src/data/products';
import {
  calculateInvoicePricing,
  sumInvoicePricing,
} from './src/domain/pricing';
import type { InvoicePricing } from './src/domain/pricing';

const palette = {
  background: '#F6F5F1',
  surface: '#FFFFFF',
  forest: '#173C2E',
  forestSoft: '#24513F',
  ink: '#183229',
  muted: '#7B8881',
  gold: '#D5AA4B',
  goldDark: '#AE822A',
  goldPale: '#F7F0DE',
  border: '#E8E5DD',
  positive: '#39785D',
  negative: '#C56D65',
  cream: '#FBFAF7',
} as const;

const GOLD_PRICE_PER_GRAM = 75_420_000;
const INVOICE_NUMBER = '۱۴۰۵-۰۱۸۷';

type Tab = 'home' | 'products' | 'invoice' | 'profile';

type CartLine = {
  product: Product;
  quantity: number;
};

type AppTextProps = ComponentProps<typeof Text> & {
  children?: ReactNode;
};

function AppText({ style, ...props }: AppTextProps) {
  return <Text {...props} style={[styles.text, style]} />;
}

const formatInteger = (value: number) =>
  new Intl.NumberFormat('fa-IR', { maximumFractionDigits: 0 }).format(value);

const formatWeight = (value: number) =>
  new Intl.NumberFormat('fa-IR', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(value);

const formatRials = (value: number) => `${formatInteger(value)} ریال`;

const linePricing = (line: CartLine) =>
  calculateInvoicePricing({
    weightGrams: line.product.weightGrams,
    goldPricePerGram: GOLD_PRICE_PER_GRAM,
    wagePercent: line.product.wagePercent,
    quantity: line.quantity,
  });

function ProductGlyph({
  icon,
  color = palette.forest,
  size = 38,
}: {
  icon: ProductIcon;
  color?: string;
  size?: number;
}) {
  if (icon === 'gem') {
    return <Gem color={color} size={size} strokeWidth={1.35} />;
  }
  if (icon === 'sparkles') {
    return <Sparkles color={color} size={size} strokeWidth={1.35} />;
  }
  if (icon === 'watch') {
    return <Watch color={color} size={size} strokeWidth={1.35} />;
  }
  return <CircleIcon color={color} size={size} strokeWidth={1.35} />;
}

function PriceChart() {
  return (
    <View style={styles.chart}>
      <Svg height="74" viewBox="0 0 300 74" width="100%">
        <Defs>
          <SvgLinearGradient id="chartFill" x1="0" x2="0" y1="0" y2="1">
            <Stop offset="0" stopColor={palette.gold} stopOpacity="0.28" />
            <Stop offset="1" stopColor={palette.gold} stopOpacity="0" />
          </SvgLinearGradient>
        </Defs>
        <Path
          d="M0 58 C22 54 34 62 54 50 C75 37 84 44 104 35 C126 25 139 41 159 31 C182 19 196 31 219 18 C239 7 258 20 281 10 C288 7 294 6 300 4 L300 74 L0 74 Z"
          fill="url(#chartFill)"
        />
        <Path
          d="M0 58 C22 54 34 62 54 50 C75 37 84 44 104 35 C126 25 139 41 159 31 C182 19 196 31 219 18 C239 7 258 20 281 10 C288 7 294 6 300 4"
          fill="none"
          stroke={palette.gold}
          strokeLinecap="round"
          strokeWidth="3"
        />
        <SvgCircle cx="281" cy="10" fill={palette.surface} r="4.5" />
        <SvgCircle cx="281" cy="10" fill={palette.gold} r="3" />
      </Svg>
    </View>
  );
}

function Header() {
  return (
    <View style={styles.header}>
      <View style={styles.brand}>
        <LinearGradient
          colors={[palette.gold, '#B9872D']}
          style={styles.logoMark}
        >
          <Gem color={palette.surface} size={21} strokeWidth={1.6} />
        </LinearGradient>
        <View>
          <AppText style={styles.brandName}>گالری زرین</AppText>
          <AppText style={styles.brandDate}>یکشنبه، ۱۸ مرداد</AppText>
        </View>
      </View>
      <Pressable
        accessibilityLabel="اعلان‌ها"
        style={({ pressed }) => [
          styles.headerIcon,
          pressed && styles.pressed,
        ]}
      >
        <Bell color={palette.ink} size={21} strokeWidth={1.8} />
        <View style={styles.notificationDot} />
      </Pressable>
    </View>
  );
}

function GoldPriceCard({ onCreateInvoice }: { onCreateInvoice: () => void }) {
  return (
    <LinearGradient
      colors={[palette.forest, '#102C22']}
      end={{ x: 0, y: 1 }}
      start={{ x: 1, y: 0 }}
      style={styles.goldCard}
    >
      <View style={styles.goldOrbLarge} />
      <View style={styles.goldOrbSmall} />
      <View style={styles.priceTopRow}>
        <View>
          <AppText style={styles.priceEyebrow}>
            قیمت لحظه‌ای طلای ۱۸ عیار
          </AppText>
          <View style={styles.liveRow}>
            <View style={styles.liveDot} />
            <AppText style={styles.liveText}>به‌روزرسانی زنده</AppText>
          </View>
        </View>
        <View style={styles.priceTrendPill}>
          <TrendingUp color="#5FB58D" size={14} strokeWidth={2.2} />
          <AppText style={styles.priceTrendText}>۱٫۸٪</AppText>
        </View>
      </View>
      <View style={styles.priceValueRow}>
        <AppText style={styles.priceValue}>{formatInteger(75_420_000)}</AppText>
        <AppText style={styles.priceUnit}>ریال / گرم</AppText>
      </View>
      <PriceChart />
      <View style={styles.goldCardFooter}>
        <View style={styles.marketItem}>
          <AppText style={styles.marketLabel}>کمترین امروز</AppText>
          <AppText style={styles.marketValue}>۷۴٬۸۶۰٬۰۰۰</AppText>
        </View>
        <View style={styles.marketDivider} />
        <View style={styles.marketItem}>
          <AppText style={styles.marketLabel}>بیشترین امروز</AppText>
          <AppText style={styles.marketValue}>۷۵٬۹۱۰٬۰۰۰</AppText>
        </View>
      </View>
      <Pressable
        accessibilityRole="button"
        onPress={onCreateInvoice}
        style={({ pressed }) => [
          styles.primaryAction,
          pressed && styles.primaryActionPressed,
        ]}
      >
        <ReceiptText color={palette.forest} size={20} strokeWidth={2} />
        <AppText style={styles.primaryActionText}>صدور فاکتور جدید</AppText>
      </Pressable>
    </LinearGradient>
  );
}

function SummaryCards() {
  return (
    <View style={styles.summaryRow}>
      <View style={styles.summaryCard}>
        <View style={[styles.summaryIcon, styles.summaryIconGold]}>
          <HandCoins color={palette.goldDark} size={21} strokeWidth={1.8} />
        </View>
        <AppText style={styles.summaryLabel}>فروش امروز</AppText>
        <AppText style={styles.summaryValue}>۲۴۸٫۶ گرم</AppText>
        <View style={styles.summaryDeltaRow}>
          <TrendingUp color={palette.positive} size={13} />
          <AppText style={styles.summaryDeltaPositive}>۱۲٪ بیشتر</AppText>
        </View>
      </View>
      <View style={styles.summaryCard}>
        <View style={[styles.summaryIcon, styles.summaryIconGreen]}>
          <WalletCards color={palette.positive} size={21} strokeWidth={1.8} />
        </View>
        <AppText style={styles.summaryLabel}>درآمد امروز</AppText>
        <AppText style={styles.summaryValue}>۱۸٫۷ میلیارد</AppText>
        <AppText style={styles.summaryCaption}>ریال فروش خالص</AppText>
      </View>
    </View>
  );
}

function SectionHeader({
  title,
  action,
  onAction,
}: {
  title: string;
  action?: string;
  onAction?: () => void;
}) {
  return (
    <View style={styles.sectionHeader}>
      <AppText style={styles.sectionTitle}>{title}</AppText>
      {action ? (
        <Pressable
          accessibilityRole="button"
          onPress={onAction}
          style={({ pressed }) => pressed && styles.pressed}
        >
          <View style={styles.sectionAction}>
            <AppText style={styles.sectionActionText}>{action}</AppText>
            <ChevronLeft color={palette.goldDark} size={16} />
          </View>
        </Pressable>
      ) : null}
    </View>
  );
}

function FeaturedProductCard({
  product,
  onAdd,
}: {
  product: Product;
  onAdd: (product: Product) => void;
}) {
  const price = linePricing({ product, quantity: 1 });
  return (
    <View style={styles.featuredCard}>
      <LinearGradient colors={[...product.colors]} style={styles.featuredVisual}>
        <View style={styles.productHalo} />
        <ProductGlyph color="rgba(24,50,41,0.74)" icon={product.icon} size={49} />
        <View style={styles.karatPill}>
          <AppText style={styles.karatPillText}>۱۸ عیار</AppText>
        </View>
      </LinearGradient>
      <View style={styles.featuredBody}>
        <AppText numberOfLines={1} style={styles.productName}>
          {product.name}
        </AppText>
        <AppText style={styles.productMeta}>
          {formatWeight(product.weightGrams)} گرم
        </AppText>
        <View style={styles.productPriceRow}>
          <View>
            <AppText style={styles.productPrice}>
              {formatInteger(price.total / 1_000_000)}
            </AppText>
            <AppText style={styles.productPriceUnit}>میلیون ریال</AppText>
          </View>
          <Pressable
            accessibilityLabel={`افزودن ${product.name} به فاکتور`}
            onPress={() => onAdd(product)}
            style={({ pressed }) => [
              styles.addButton,
              pressed && styles.primaryActionPressed,
            ]}
          >
            <Plus color={palette.surface} size={18} strokeWidth={2.2} />
          </Pressable>
        </View>
      </View>
    </View>
  );
}

const recentInvoices = [
  {
    id: '#۱۴۰۵-۰۱۸۶',
    customer: 'سارا احمدی',
    time: '۱۲:۴۵',
    total: '۱٬۲۸۴٬۵۰۰٬۰۰۰',
  },
  {
    id: '#۱۴۰۵-۰۱۸۵',
    customer: 'مهدی کریمی',
    time: '۱۱:۲۰',
    total: '۸۹۶٬۳۲۰٬۰۰۰',
  },
  {
    id: '#۱۴۰۵-۰۱۸۴',
    customer: 'نیلوفر محمدی',
    time: '۱۰:۰۵',
    total: '۲٬۱۰۵٬۸۰۰٬۰۰۰',
  },
];

function RecentInvoices({ onOpen }: { onOpen: () => void }) {
  return (
    <View style={styles.invoiceList}>
      {recentInvoices.map((invoice, index) => (
        <Pressable
          key={invoice.id}
          onPress={onOpen}
          style={({ pressed }) => [
            styles.invoiceRow,
            index !== recentInvoices.length - 1 && styles.invoiceRowBorder,
            pressed && styles.rowPressed,
          ]}
        >
          <View style={styles.invoiceIdentity}>
            <View style={styles.invoiceIcon}>
              <FileText color={palette.goldDark} size={20} strokeWidth={1.8} />
            </View>
            <View>
              <AppText style={styles.invoiceCustomer}>
                {invoice.customer}
              </AppText>
              <View style={styles.invoiceMetaRow}>
                <AppText style={styles.invoiceMeta}>{invoice.id}</AppText>
                <View style={styles.metaDot} />
                <AppText style={styles.invoiceMeta}>{invoice.time}</AppText>
              </View>
            </View>
          </View>
          <View style={styles.invoiceAmount}>
            <AppText style={styles.invoiceAmountText}>{invoice.total}</AppText>
            <AppText style={styles.invoiceAmountUnit}>ریال</AppText>
          </View>
        </Pressable>
      ))}
    </View>
  );
}

function HomeScreen({
  onAdd,
  onCreateInvoice,
  onShowProducts,
}: {
  onAdd: (product: Product) => void;
  onCreateInvoice: () => void;
  onShowProducts: () => void;
}) {
  return (
    <ScrollView
      contentContainerStyle={styles.scrollContent}
      showsVerticalScrollIndicator={false}
    >
      <Header />
      <GoldPriceCard onCreateInvoice={onCreateInvoice} />
      <SummaryCards />
      <SectionHeader
        action="مشاهده همه"
        onAction={onShowProducts}
        title="محصولات پرفروش"
      />
      <ScrollView
        contentContainerStyle={styles.horizontalProducts}
        horizontal
        showsHorizontalScrollIndicator={false}
      >
        {products
          .filter((product) => product.featured)
          .map((product) => (
            <FeaturedProductCard
              key={product.id}
              onAdd={onAdd}
              product={product}
            />
          ))}
      </ScrollView>
      <SectionHeader
        action="گزارش فروش"
        onAction={onCreateInvoice}
        title="آخرین فاکتورها"
      />
      <RecentInvoices onOpen={onCreateInvoice} />
    </ScrollView>
  );
}

function SearchField({
  value,
  onChangeText,
}: {
  value: string;
  onChangeText: (value: string) => void;
}) {
  return (
    <View style={styles.searchRow}>
      <View style={styles.searchBox}>
        <Search color={palette.muted} size={19} strokeWidth={1.8} />
        <TextInput
          accessibilityLabel="جست‌وجوی محصول"
          onChangeText={onChangeText}
          placeholder="نام یا کد محصول..."
          placeholderTextColor="#9BA39E"
          style={styles.searchInput}
          value={value}
        />
      </View>
      <Pressable
        accessibilityLabel="اسکن کد محصول"
        style={({ pressed }) => [
          styles.scanButton,
          pressed && styles.primaryActionPressed,
        ]}
      >
        <ScanLine color={palette.surface} size={21} strokeWidth={1.8} />
      </Pressable>
    </View>
  );
}

function CatalogProductCard({
  product,
  onAdd,
}: {
  product: Product;
  onAdd: (product: Product) => void;
}) {
  const price = linePricing({ product, quantity: 1 });
  return (
    <View style={styles.catalogCard}>
      <LinearGradient colors={[...product.colors]} style={styles.catalogVisual}>
        <View style={styles.productHaloSmall} />
        <ProductGlyph color="rgba(24,50,41,0.72)" icon={product.icon} size={44} />
        <View style={styles.catalogCodePill}>
          <AppText style={styles.catalogCode}>{product.code}</AppText>
        </View>
      </LinearGradient>
      <View style={styles.catalogBody}>
        <AppText numberOfLines={1} style={styles.catalogName}>
          {product.name}
        </AppText>
        <AppText style={styles.catalogMeta}>
          {formatWeight(product.weightGrams)} گرم · {formatInteger(product.karat)}{' '}
          عیار
        </AppText>
        <AppText style={styles.catalogPrice}>
          {formatInteger(price.total / 1_000_000)} میلیون
        </AppText>
        <Pressable
          accessibilityRole="button"
          onPress={() => onAdd(product)}
          style={({ pressed }) => [
            styles.catalogAddButton,
            pressed && styles.rowPressed,
          ]}
        >
          <Plus color={palette.forest} size={17} strokeWidth={2.2} />
          <AppText style={styles.catalogAddText}>افزودن به فاکتور</AppText>
        </Pressable>
      </View>
    </View>
  );
}

function ProductsScreen({ onAdd }: { onAdd: (product: Product) => void }) {
  const [query, setQuery] = useState('');
  const [category, setCategory] =
    useState<(typeof categories)[number]>('همه');

  const filteredProducts = products.filter((product) => {
    const matchesCategory =
      category === 'همه' || product.category === category;
    const normalizedQuery = query.trim().toLocaleLowerCase('fa');
    const matchesQuery =
      !normalizedQuery ||
      product.name.toLocaleLowerCase('fa').includes(normalizedQuery) ||
      product.code.toLocaleLowerCase('fa').includes(normalizedQuery);
    return matchesCategory && matchesQuery;
  });

  return (
    <ScrollView
      contentContainerStyle={styles.scrollContent}
      showsVerticalScrollIndicator={false}
    >
      <View style={styles.screenTitleRow}>
        <View>
          <AppText style={styles.screenTitle}>محصولات</AppText>
          <AppText style={styles.screenSubtitle}>
            {formatInteger(filteredProducts.length)} محصول موجود
          </AppText>
        </View>
        <View style={styles.inventoryPill}>
          <Package color={palette.positive} size={15} />
          <AppText style={styles.inventoryText}>موجودی به‌روز</AppText>
        </View>
      </View>
      <SearchField onChangeText={setQuery} value={query} />
      <ScrollView
        contentContainerStyle={styles.categories}
        horizontal
        showsHorizontalScrollIndicator={false}
      >
        {categories.map((item) => {
          const active = category === item;
          return (
            <Pressable
              key={item}
              onPress={() => setCategory(item)}
              style={({ pressed }) => [
                styles.categoryPill,
                active && styles.categoryPillActive,
                pressed && styles.pressed,
              ]}
            >
              <AppText
                style={[
                  styles.categoryText,
                  active && styles.categoryTextActive,
                ]}
              >
                {item}
              </AppText>
            </Pressable>
          );
        })}
      </ScrollView>
      <View style={styles.catalogGrid}>
        {filteredProducts.map((product) => (
          <CatalogProductCard key={product.id} onAdd={onAdd} product={product} />
        ))}
      </View>
      {filteredProducts.length === 0 ? (
        <View style={styles.emptySearch}>
          <Search color={palette.muted} size={34} strokeWidth={1.4} />
          <AppText style={styles.emptyTitle}>محصولی پیدا نشد</AppText>
          <AppText style={styles.emptySubtitle}>
            عبارت دیگری را جست‌وجو کنید.
          </AppText>
        </View>
      ) : null}
    </ScrollView>
  );
}

function InvoiceLine({
  line,
  onDecrease,
  onIncrease,
  onRemove,
}: {
  line: CartLine;
  onDecrease: () => void;
  onIncrease: () => void;
  onRemove: () => void;
}) {
  const pricing = linePricing(line);
  return (
    <View style={styles.cartLine}>
      <LinearGradient
        colors={[...line.product.colors]}
        style={styles.cartProductVisual}
      >
        <ProductGlyph
          color="rgba(24,50,41,0.72)"
          icon={line.product.icon}
          size={30}
        />
      </LinearGradient>
      <View style={styles.cartLineBody}>
        <View style={styles.cartLineHeader}>
          <View style={styles.cartProductIdentity}>
            <AppText style={styles.cartProductName}>
              {line.product.name}
            </AppText>
            <AppText style={styles.cartProductMeta}>
              {formatWeight(line.product.weightGrams)} گرم · اجرت{' '}
              {formatInteger(line.product.wagePercent)}٪
            </AppText>
          </View>
          <Pressable
            accessibilityLabel={`حذف ${line.product.name}`}
            onPress={onRemove}
            style={({ pressed }) => pressed && styles.pressed}
          >
            <Trash2 color={palette.negative} size={18} strokeWidth={1.7} />
          </Pressable>
        </View>
        <View style={styles.cartLineFooter}>
          <View style={styles.quantityControl}>
            <Pressable
              accessibilityLabel="افزایش تعداد"
              onPress={onIncrease}
              style={({ pressed }) => [
                styles.quantityButton,
                pressed && styles.rowPressed,
              ]}
            >
              <Plus color={palette.forest} size={15} strokeWidth={2.2} />
            </Pressable>
            <AppText style={styles.quantityValue}>
              {formatInteger(line.quantity)}
            </AppText>
            <Pressable
              accessibilityLabel="کاهش تعداد"
              onPress={onDecrease}
              style={({ pressed }) => [
                styles.quantityButton,
                pressed && styles.rowPressed,
              ]}
            >
              <Minus color={palette.forest} size={15} strokeWidth={2.2} />
            </Pressable>
          </View>
          <View style={styles.cartPrice}>
            <AppText style={styles.cartPriceValue}>
              {formatInteger(pricing.total)}
            </AppText>
            <AppText style={styles.cartPriceUnit}>ریال</AppText>
          </View>
        </View>
      </View>
    </View>
  );
}

function TotalRow({
  label,
  value,
  emphasized,
}: {
  label: string;
  value: number;
  emphasized?: boolean;
}) {
  return (
    <View style={[styles.totalRow, emphasized && styles.grandTotalRow]}>
      <AppText
        style={[styles.totalLabel, emphasized && styles.grandTotalLabel]}
      >
        {label}
      </AppText>
      <View style={styles.totalValueRow}>
        <AppText
          style={[styles.totalValue, emphasized && styles.grandTotalValue]}
        >
          {formatInteger(value)}
        </AppText>
        <AppText
          style={[styles.totalUnit, emphasized && styles.grandTotalUnit]}
        >
          ریال
        </AppText>
      </View>
    </View>
  );
}

function InvoiceScreen({
  cart,
  customerName,
  customerPhone,
  onCustomerNameChange,
  onCustomerPhoneChange,
  onDecrease,
  onIncrease,
  onOpenPreview,
  onRemove,
  totals,
}: {
  cart: CartLine[];
  customerName: string;
  customerPhone: string;
  onCustomerNameChange: (value: string) => void;
  onCustomerPhoneChange: (value: string) => void;
  onDecrease: (id: string) => void;
  onIncrease: (id: string) => void;
  onOpenPreview: () => void;
  onRemove: (id: string) => void;
  totals: InvoicePricing;
}) {
  return (
    <ScrollView
      contentContainerStyle={styles.scrollContent}
      keyboardShouldPersistTaps="handled"
      showsVerticalScrollIndicator={false}
    >
      <View style={styles.screenTitleRow}>
        <View>
          <AppText style={styles.screenTitle}>فاکتور فروش</AppText>
          <AppText style={styles.screenSubtitle}>شماره {INVOICE_NUMBER}</AppText>
        </View>
        <View style={styles.draftPill}>
          <Clock3 color={palette.goldDark} size={15} />
          <AppText style={styles.draftText}>پیش‌نویس</AppText>
        </View>
      </View>
      <View style={styles.invoiceSection}>
        <View style={styles.invoiceSectionTitleRow}>
          <View style={styles.sectionIconSmall}>
            <UserRound color={palette.goldDark} size={18} />
          </View>
          <AppText style={styles.invoiceSectionTitle}>اطلاعات خریدار</AppText>
        </View>
        <View style={styles.customerForm}>
          <View style={styles.fieldGroup}>
            <AppText style={styles.fieldLabel}>نام و نام خانوادگی</AppText>
            <TextInput
              onChangeText={onCustomerNameChange}
              placeholder="نام خریدار"
              placeholderTextColor="#A0A7A3"
              style={styles.formInput}
              value={customerName}
            />
          </View>
          <View style={styles.fieldGroup}>
            <AppText style={styles.fieldLabel}>شماره موبایل</AppText>
            <TextInput
              keyboardType="phone-pad"
              onChangeText={onCustomerPhoneChange}
              placeholder="۰۹۱۲۱۲۳۴۵۶۷"
              placeholderTextColor="#A0A7A3"
              style={styles.formInput}
              value={customerPhone}
            />
          </View>
        </View>
      </View>
      <View style={styles.invoiceSection}>
        <View style={styles.invoiceSectionTitleRow}>
          <View style={styles.sectionIconSmall}>
            <ShoppingBag color={palette.goldDark} size={18} />
          </View>
          <AppText style={styles.invoiceSectionTitle}>اقلام فاکتور</AppText>
          <View style={styles.itemsCount}>
            <AppText style={styles.itemsCountText}>
              {formatInteger(cart.length)} قلم
            </AppText>
          </View>
        </View>
        {cart.length ? (
          <View style={styles.cartLines}>
            {cart.map((line, index) => (
              <View key={line.product.id}>
                <InvoiceLine
                  line={line}
                  onDecrease={() => onDecrease(line.product.id)}
                  onIncrease={() => onIncrease(line.product.id)}
                  onRemove={() => onRemove(line.product.id)}
                />
                {index !== cart.length - 1 ? (
                  <View style={styles.cartDivider} />
                ) : null}
              </View>
            ))}
          </View>
        ) : (
          <View style={styles.emptyCart}>
            <ShoppingBag color={palette.muted} size={34} strokeWidth={1.4} />
            <AppText style={styles.emptyTitle}>فاکتور هنوز خالی است</AppText>
            <AppText style={styles.emptySubtitle}>
              از بخش محصولات، طلا یا جواهر اضافه کنید.
            </AppText>
          </View>
        )}
      </View>
      <View style={styles.invoiceSection}>
        <View style={styles.invoiceSectionTitleRow}>
          <View style={styles.sectionIconSmall}>
            <Calculator color={palette.goldDark} size={18} />
          </View>
          <AppText style={styles.invoiceSectionTitle}>خلاصه محاسبات</AppText>
        </View>
        <View style={styles.totals}>
          <TotalRow label="اصل ارزش طلا" value={totals.pureGoldValue} />
          <TotalRow label="اجرت ساخت" value={totals.wage} />
          <TotalRow label="سود فروشنده (۷٪)" value={totals.sellerProfit} />
          <TotalRow label="مالیات ارزش افزوده" value={totals.vat} />
          <TotalRow emphasized label="مبلغ قابل پرداخت" value={totals.total} />
        </View>
        <View style={styles.taxNote}>
          <BadgeCheck color={palette.positive} size={17} />
          <AppText style={styles.taxNoteText}>
            مالیات فقط روی اجرت و سود محاسبه شده است.
          </AppText>
        </View>
      </View>
      <Pressable
        accessibilityRole="button"
        disabled={!cart.length}
        onPress={onOpenPreview}
        style={({ pressed }) => [
          styles.issueButton,
          !cart.length && styles.issueButtonDisabled,
          pressed && cart.length > 0 && styles.primaryActionPressed,
        ]}
      >
        <ReceiptText color={palette.surface} size={21} strokeWidth={1.9} />
        <AppText style={styles.issueButtonText}>پیش‌نمایش و صدور فاکتور</AppText>
      </Pressable>
    </ScrollView>
  );
}

function ProfileScreen() {
  const items = [
    {
      icon: Store,
      title: 'اطلاعات فروشگاه',
      description: 'نام، آدرس و شناسه مالیاتی',
    },
    {
      icon: Calculator,
      title: 'تنظیمات محاسبات',
      description: 'اجرت، سود و نرخ مالیات',
    },
    {
      icon: FileText,
      title: 'قالب فاکتور',
      description: 'لوگو، امضا و شماره‌گذاری',
    },
  ];

  return (
    <ScrollView
      contentContainerStyle={styles.scrollContent}
      showsVerticalScrollIndicator={false}
    >
      <AppText style={styles.screenTitle}>حساب کاربری</AppText>
      <LinearGradient
        colors={[palette.forest, '#214E3C']}
        style={styles.profileCard}
      >
        <View style={styles.profileAvatar}>
          <Store color={palette.goldDark} size={28} />
        </View>
        <AppText style={styles.profileName}>گالری زرین</AppText>
        <AppText style={styles.profileRole}>مدیریت فروشگاه · تهران</AppText>
        <View style={styles.profileBadge}>
          <BadgeCheck color="#79C49F" size={16} />
          <AppText style={styles.profileBadgeText}>حساب تأیید شده</AppText>
        </View>
      </LinearGradient>
      <SectionHeader title="تنظیمات فروشگاه" />
      <View style={styles.settingsList}>
        {items.map(({ icon: Icon, title, description }, index) => (
          <Pressable
            key={title}
            style={({ pressed }) => [
              styles.settingsRow,
              index !== items.length - 1 && styles.invoiceRowBorder,
              pressed && styles.rowPressed,
            ]}
          >
            <View style={styles.settingsIdentity}>
              <View style={styles.settingsIcon}>
                <Icon color={palette.goldDark} size={20} strokeWidth={1.8} />
              </View>
              <View>
                <AppText style={styles.settingsTitle}>{title}</AppText>
                <AppText style={styles.settingsDescription}>
                  {description}
                </AppText>
              </View>
            </View>
            <ChevronLeft color={palette.muted} size={20} />
          </Pressable>
        ))}
      </View>
      <View style={styles.supportCard}>
        <View>
          <AppText style={styles.supportTitle}>نیاز به راهنمایی دارید؟</AppText>
          <AppText style={styles.supportDescription}>
            پشتیبانی زرین هر روز پاسخ‌گوست.
          </AppText>
        </View>
        <Pressable style={styles.supportButton}>
          <AppText style={styles.supportButtonText}>تماس با ما</AppText>
        </Pressable>
      </View>
    </ScrollView>
  );
}

function BottomNavigation({
  activeTab,
  cartCount,
  onChange,
}: {
  activeTab: Tab;
  cartCount: number;
  onChange: (tab: Tab) => void;
}) {
  const tabs: {
    key: Tab;
    label: string;
    icon: typeof Home;
  }[] = [
    { key: 'home', label: 'خانه', icon: Home },
    { key: 'products', label: 'محصولات', icon: Gem },
    { key: 'invoice', label: 'فاکتور', icon: ReceiptText },
    { key: 'profile', label: 'حساب', icon: UserRound },
  ];

  return (
    <View style={styles.bottomNav}>
      {tabs.map(({ key, label, icon: Icon }) => {
        const active = activeTab === key;
        return (
          <Pressable
            accessibilityRole="button"
            key={key}
            onPress={() => onChange(key)}
            style={({ pressed }) => [
              styles.navItem,
              pressed && styles.pressed,
            ]}
          >
            <View style={[styles.navIconWrap, active && styles.navIconActive]}>
              <Icon
                color={active ? palette.surface : palette.muted}
                size={20}
                strokeWidth={active ? 2.2 : 1.8}
              />
              {key === 'invoice' && cartCount > 0 ? (
                <View style={styles.cartBadge}>
                  <AppText style={styles.cartBadgeText}>
                    {formatInteger(cartCount)}
                  </AppText>
                </View>
              ) : null}
            </View>
            <AppText style={[styles.navLabel, active && styles.navLabelActive]}>
              {label}
            </AppText>
          </Pressable>
        );
      })}
    </View>
  );
}

const escapeHtml = (value: string) =>
  value
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#039;');

function buildInvoiceHtml(
  cart: CartLine[],
  customerName: string,
  customerPhone: string,
  totals: InvoicePricing,
) {
  const rows = cart
    .map((line, index) => {
      const pricing = linePricing(line);
      return `
        <tr>
          <td>${formatInteger(index + 1)}</td>
          <td><strong>${escapeHtml(line.product.name)}</strong><small>${escapeHtml(line.product.code)}</small></td>
          <td>${formatWeight(line.product.weightGrams * line.quantity)}</td>
          <td>${formatInteger(line.product.wagePercent)}٪</td>
          <td>${formatInteger(pricing.total)}</td>
        </tr>`;
    })
    .join('');

  return `<!doctype html>
    <html dir="rtl" lang="fa">
      <head>
        <meta charset="utf-8" />
        <style>
          * { box-sizing: border-box; }
          body { color: #183229; font-family: Tahoma, Arial, sans-serif; margin: 0; padding: 36px; direction: rtl; }
          .header { align-items: center; border-bottom: 3px solid #d5aa4b; display: flex; justify-content: space-between; padding-bottom: 20px; }
          .brand h1 { font-size: 26px; margin: 0 0 4px; }
          .brand p, .meta p { color: #66766e; font-size: 12px; margin: 4px 0; }
          .title { margin: 26px 0 18px; text-align: center; }
          .title h2 { margin: 0 0 6px; }
          .customer { background: #f7f4eb; border-radius: 12px; display: flex; gap: 44px; padding: 14px 18px; }
          .customer span { color: #66766e; font-size: 11px; }
          .customer strong { display: block; font-size: 13px; margin-top: 4px; }
          table { border-collapse: collapse; margin-top: 22px; width: 100%; }
          th { background: #173c2e; color: white; font-size: 11px; padding: 11px; }
          td { border-bottom: 1px solid #e6e3dc; font-size: 11px; padding: 12px 9px; text-align: center; }
          td small { color: #7b8881; display: block; margin-top: 3px; }
          .totals { margin-right: auto; margin-top: 20px; width: 48%; }
          .total-row { display: flex; font-size: 11px; justify-content: space-between; padding: 7px 0; }
          .grand { border-top: 2px solid #173c2e; color: #173c2e; font-size: 14px; font-weight: bold; margin-top: 6px; padding-top: 12px; }
          .note { color: #66766e; font-size: 10px; margin-top: 28px; }
          .signatures { display: flex; justify-content: space-around; margin-top: 70px; }
          .signature { border-top: 1px solid #9ba39e; font-size: 11px; padding-top: 8px; text-align: center; width: 150px; }
        </style>
      </head>
      <body>
        <div class="header">
          <div class="brand"><h1>گالری زرین</h1><p>فروش تخصصی طلا و جواهر</p></div>
          <div class="meta"><p>شناسه مالیاتی: ۱۴۰۵۸۲۰۴۱</p><p>تهران، بازار بزرگ، سرای زرگرها</p></div>
        </div>
        <div class="title"><h2>فاکتور فروش طلا و جواهر</h2><p>شماره ${INVOICE_NUMBER} · تاریخ ۱۴۰۵/۰۵/۱۸</p></div>
        <div class="customer">
          <div><span>خریدار</span><strong>${escapeHtml(customerName || 'مشتری نقدی')}</strong></div>
          <div><span>شماره تماس</span><strong>${escapeHtml(customerPhone || '—')}</strong></div>
        </div>
        <table>
          <thead><tr><th>ردیف</th><th>شرح کالا</th><th>وزن (گرم)</th><th>اجرت</th><th>مبلغ (ریال)</th></tr></thead>
          <tbody>${rows}</tbody>
        </table>
        <div class="totals">
          <div class="total-row"><span>اصل ارزش طلا</span><span>${formatInteger(totals.pureGoldValue)}</span></div>
          <div class="total-row"><span>اجرت ساخت</span><span>${formatInteger(totals.wage)}</span></div>
          <div class="total-row"><span>سود فروشنده</span><span>${formatInteger(totals.sellerProfit)}</span></div>
          <div class="total-row"><span>مالیات</span><span>${formatInteger(totals.vat)}</span></div>
          <div class="total-row grand"><span>مبلغ قابل پرداخت</span><span>${formatInteger(totals.total)} ریال</span></div>
        </div>
        <p class="note">اصل ارزش طلا مطابق مقررات از مالیات بر ارزش افزوده معاف است.</p>
        <div class="signatures"><div class="signature">امضا و مهر فروشنده</div><div class="signature">امضای خریدار</div></div>
      </body>
    </html>`;
}

function InvoicePreview({
  cart,
  customerName,
  customerPhone,
  exporting,
  onClose,
  onExport,
  totals,
  visible,
}: {
  cart: CartLine[];
  customerName: string;
  customerPhone: string;
  exporting: boolean;
  onClose: () => void;
  onExport: () => void;
  totals: InvoicePricing;
  visible: boolean;
}) {
  return (
    <Modal
      animationType="slide"
      onRequestClose={onClose}
      transparent
      visible={visible}
    >
      <View style={styles.modalBackdrop}>
        <SafeAreaView style={styles.modalShell}>
          <View style={styles.modalHeader}>
            <View>
              <AppText style={styles.modalTitle}>پیش‌نمایش فاکتور</AppText>
              <AppText style={styles.modalSubtitle}>
                آماده چاپ و اشتراک‌گذاری
              </AppText>
            </View>
            <Pressable
              accessibilityLabel="بستن پیش‌نمایش"
              onPress={onClose}
              style={({ pressed }) => [
                styles.modalClose,
                pressed && styles.rowPressed,
              ]}
            >
              <X color={palette.ink} size={21} />
            </Pressable>
          </View>
          <ScrollView
            contentContainerStyle={styles.previewScroll}
            showsVerticalScrollIndicator={false}
          >
            <View style={styles.invoicePaper}>
              <View style={styles.paperHeader}>
                <View style={styles.paperBrand}>
                  <LinearGradient
                    colors={[palette.gold, palette.goldDark]}
                    style={styles.paperLogo}
                  >
                    <Gem color={palette.surface} size={20} />
                  </LinearGradient>
                  <View>
                    <AppText style={styles.paperBrandName}>گالری زرین</AppText>
                    <AppText style={styles.paperBrandCaption}>
                      فروش تخصصی طلا و جواهر
                    </AppText>
                  </View>
                </View>
                <BadgeCheck color={palette.positive} size={23} />
              </View>
              <View style={styles.paperTitleBlock}>
                <AppText style={styles.paperTitle}>فاکتور فروش</AppText>
                <AppText style={styles.paperNumber}>
                  {INVOICE_NUMBER} · ۱۴۰۵/۰۵/۱۸
                </AppText>
              </View>
              <View style={styles.paperCustomer}>
                <View>
                  <AppText style={styles.paperLabel}>خریدار</AppText>
                  <AppText style={styles.paperCustomerValue}>
                    {customerName || 'مشتری نقدی'}
                  </AppText>
                </View>
                <View>
                  <AppText style={styles.paperLabel}>شماره تماس</AppText>
                  <AppText style={styles.paperCustomerValue}>
                    {customerPhone || '—'}
                  </AppText>
                </View>
              </View>
              <View style={styles.paperTableHeader}>
                <AppText style={[styles.paperTableHeading, styles.paperItemCol]}>
                  شرح
                </AppText>
                <AppText style={styles.paperTableHeading}>وزن</AppText>
                <AppText style={styles.paperTableHeading}>مبلغ</AppText>
              </View>
              {cart.map((line) => {
                const pricing = linePricing(line);
                return (
                  <View key={line.product.id} style={styles.paperTableRow}>
                    <View style={styles.paperItemCol}>
                      <AppText style={styles.paperItemName}>
                        {line.product.name}
                      </AppText>
                      <AppText style={styles.paperItemCode}>
                        {line.product.code}
                      </AppText>
                    </View>
                    <AppText style={styles.paperCell}>
                      {formatWeight(line.product.weightGrams * line.quantity)}
                    </AppText>
                    <AppText style={styles.paperCell}>
                      {formatInteger(pricing.total / 1_000_000)} م
                    </AppText>
                  </View>
                );
              })}
              <View style={styles.paperTotals}>
                <View style={styles.paperTotalRow}>
                  <AppText style={styles.paperTotalLabel}>اجرت و سود</AppText>
                  <AppText style={styles.paperTotalValue}>
                    {formatRials(totals.wage + totals.sellerProfit)}
                  </AppText>
                </View>
                <View style={styles.paperTotalRow}>
                  <AppText style={styles.paperTotalLabel}>مالیات</AppText>
                  <AppText style={styles.paperTotalValue}>
                    {formatRials(totals.vat)}
                  </AppText>
                </View>
                <View style={styles.paperGrandTotal}>
                  <AppText style={styles.paperGrandLabel}>
                    مبلغ قابل پرداخت
                  </AppText>
                  <View>
                    <AppText style={styles.paperGrandValue}>
                      {formatInteger(totals.total)}
                    </AppText>
                    <AppText style={styles.paperGrandUnit}>ریال</AppText>
                  </View>
                </View>
              </View>
              <View style={styles.paperFooter}>
                <BadgeCheck color={palette.positive} size={16} />
                <AppText style={styles.paperFooterText}>
                  اصالت و عیار کالا توسط گالری زرین تضمین می‌شود.
                </AppText>
              </View>
            </View>
          </ScrollView>
          <View style={styles.modalActions}>
            <Pressable
              accessibilityRole="button"
              disabled={exporting}
              onPress={onExport}
              style={({ pressed }) => [
                styles.exportButton,
                pressed && styles.primaryActionPressed,
              ]}
            >
              {exporting ? (
                <ActivityIndicator color={palette.surface} size="small" />
              ) : (
                <Share2 color={palette.surface} size={20} strokeWidth={1.9} />
              )}
              <AppText style={styles.exportButtonText}>
                {exporting ? 'در حال آماده‌سازی...' : 'چاپ یا اشتراک PDF'}
              </AppText>
            </Pressable>
          </View>
        </SafeAreaView>
      </View>
    </Modal>
  );
}

function ToastMessage({ message }: { message: string }) {
  return (
    <View pointerEvents="none" style={styles.toast}>
      <BadgeCheck color="#8CD0AE" size={19} />
      <AppText style={styles.toastText}>{message}</AppText>
    </View>
  );
}

export default function App() {
  const [fontsLoaded] = useFonts({
    Vazirmatn_400Regular,
    Vazirmatn_500Medium,
    Vazirmatn_600SemiBold,
    Vazirmatn_700Bold,
    Vazirmatn_800ExtraBold,
  });
  const [activeTab, setActiveTab] = useState<Tab>('home');
  const [cart, setCart] = useState<CartLine[]>([
    { product: products[0]!, quantity: 1 },
    { product: products[2]!, quantity: 1 },
  ]);
  const [customerName, setCustomerName] = useState('سارا احمدی');
  const [customerPhone, setCustomerPhone] = useState('۰۹۱۲ ۳۴۵ ۶۷۸۹');
  const [previewVisible, setPreviewVisible] = useState(false);
  const [exporting, setExporting] = useState(false);
  const [toast, setToast] = useState('');

  useEffect(() => {
    if (Platform.OS === 'web' && typeof document !== 'undefined') {
      document.documentElement.dir = 'rtl';
      document.documentElement.lang = 'fa';
      document.body.style.margin = '0';
      document.body.style.background = palette.forest;
      document.body.style.overflow = 'hidden';
    }
  }, []);

  useEffect(() => {
    if (!toast) {
      return;
    }
    const timeout = setTimeout(() => setToast(''), 2200);
    return () => clearTimeout(timeout);
  }, [toast]);

  const totals = useMemo(
    () => sumInvoicePricing(cart.map(linePricing)),
    [cart],
  );
  const cartCount = cart.reduce((count, line) => count + line.quantity, 0);

  const addToCart = (product: Product) => {
    setCart((current) => {
      const existing = current.find(
        (line) => line.product.id === product.id,
      );
      if (existing) {
        return current.map((line) =>
          line.product.id === product.id
            ? { ...line, quantity: line.quantity + 1 }
            : line,
        );
      }
      return [...current, { product, quantity: 1 }];
    });
    setToast(`${product.name} به فاکتور اضافه شد`);
  };

  const updateQuantity = (id: string, delta: number) => {
    setCart((current) =>
      current
        .map((line) =>
          line.product.id === id
            ? { ...line, quantity: Math.max(0, line.quantity + delta) }
            : line,
        )
        .filter((line) => line.quantity > 0),
    );
  };

  const removeLine = (id: string) => {
    setCart((current) => current.filter((line) => line.product.id !== id));
    setToast('محصول از فاکتور حذف شد');
  };

  const openInvoice = () => setActiveTab('invoice');

  const exportInvoice = async () => {
    const html = buildInvoiceHtml(cart, customerName, customerPhone, totals);
    setExporting(true);
    try {
      if (Platform.OS === 'web') {
        const printWindow = window.open('', '_blank', 'width=900,height=720');
        if (!printWindow) {
          throw new Error('Popup blocked');
        }
        printWindow.document.open();
        printWindow.document.write(html);
        printWindow.document.close();
        window.setTimeout(() => {
          printWindow.focus();
          printWindow.print();
        }, 250);
      } else {
        const { uri } = await Print.printToFileAsync({ html });
        if (await Sharing.isAvailableAsync()) {
          await Sharing.shareAsync(uri, {
            UTI: '.pdf',
            mimeType: 'application/pdf',
          });
        } else {
          await Print.printAsync({ html });
        }
      }
      setToast('فاکتور با موفقیت آماده شد');
    } catch {
      Alert.alert(
        'خطا در صدور فاکتور',
        'لطفاً دسترسی چاپ یا اشتراک‌گذاری را بررسی کنید.',
      );
    } finally {
      setExporting(false);
    }
  };

  if (!fontsLoaded) {
    return (
      <View style={styles.loadingScreen}>
        <LinearGradient
          colors={[palette.gold, palette.goldDark]}
          style={styles.loadingLogo}
        >
          <Gem color={palette.surface} size={30} />
        </LinearGradient>
        <ActivityIndicator color={palette.gold} size="small" />
      </View>
    );
  }

  return (
    <View style={styles.appBackground}>
      <StatusBar style="dark" />
      <SafeAreaView style={styles.phoneShell}>
        <View style={styles.screen}>
          {activeTab === 'home' ? (
            <HomeScreen
              onAdd={addToCart}
              onCreateInvoice={openInvoice}
              onShowProducts={() => setActiveTab('products')}
            />
          ) : null}
          {activeTab === 'products' ? (
            <ProductsScreen onAdd={addToCart} />
          ) : null}
          {activeTab === 'invoice' ? (
            <InvoiceScreen
              cart={cart}
              customerName={customerName}
              customerPhone={customerPhone}
              onCustomerNameChange={setCustomerName}
              onCustomerPhoneChange={setCustomerPhone}
              onDecrease={(id) => updateQuantity(id, -1)}
              onIncrease={(id) => updateQuantity(id, 1)}
              onOpenPreview={() => setPreviewVisible(true)}
              onRemove={removeLine}
              totals={totals}
            />
          ) : null}
          {activeTab === 'profile' ? <ProfileScreen /> : null}
          <BottomNavigation
            activeTab={activeTab}
            cartCount={cartCount}
            onChange={setActiveTab}
          />
          {toast ? <ToastMessage message={toast} /> : null}
        </View>
      </SafeAreaView>
      <InvoicePreview
        cart={cart}
        customerName={customerName}
        customerPhone={customerPhone}
        exporting={exporting}
        onClose={() => setPreviewVisible(false)}
        onExport={exportInvoice}
        totals={totals}
        visible={previewVisible}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  text: {
    color: palette.ink,
    fontFamily: 'Vazirmatn_400Regular',
    textAlign: 'right',
    writingDirection: 'rtl',
  },
  loadingScreen: {
    alignItems: 'center',
    backgroundColor: palette.forest,
    flex: 1,
    gap: 22,
    justifyContent: 'center',
  },
  loadingLogo: {
    alignItems: 'center',
    borderRadius: 26,
    height: 76,
    justifyContent: 'center',
    width: 76,
  },
  appBackground: {
    alignItems: 'center',
    backgroundColor: palette.forest,
    flex: 1,
  },
  phoneShell: {
    backgroundColor: palette.background,
    flex: 1,
    maxWidth: 480,
    overflow: 'hidden',
    width: '100%',
  },
  screen: {
    flex: 1,
    position: 'relative',
  },
  scrollContent: {
    paddingBottom: 116,
    paddingHorizontal: 18,
    paddingTop: Platform.OS === 'android' ? 30 : 18,
  },
  pressed: {
    opacity: 0.64,
  },
  rowPressed: {
    backgroundColor: '#F4F2EC',
  },
  header: {
    alignItems: 'center',
    flexDirection: 'row-reverse',
    justifyContent: 'space-between',
    marginBottom: 18,
  },
  brand: {
    alignItems: 'center',
    flexDirection: 'row-reverse',
    gap: 10,
  },
  logoMark: {
    alignItems: 'center',
    borderRadius: 14,
    height: 46,
    justifyContent: 'center',
    width: 46,
  },
  brandName: {
    fontFamily: 'Vazirmatn_700Bold',
    fontSize: 17,
    lineHeight: 25,
  },
  brandDate: {
    color: palette.muted,
    fontSize: 11,
    lineHeight: 18,
  },
  headerIcon: {
    alignItems: 'center',
    backgroundColor: palette.surface,
    borderColor: palette.border,
    borderRadius: 13,
    borderWidth: 1,
    height: 43,
    justifyContent: 'center',
    position: 'relative',
    width: 43,
  },
  notificationDot: {
    backgroundColor: palette.negative,
    borderColor: palette.surface,
    borderRadius: 5,
    borderWidth: 1.5,
    height: 9,
    position: 'absolute',
    right: 9,
    top: 8,
    width: 9,
  },
  goldCard: {
    borderRadius: 26,
    marginBottom: 14,
    overflow: 'hidden',
    padding: 20,
    position: 'relative',
  },
  goldOrbLarge: {
    backgroundColor: 'rgba(213,170,75,0.08)',
    borderColor: 'rgba(213,170,75,0.12)',
    borderRadius: 105,
    borderWidth: 1,
    height: 210,
    position: 'absolute',
    right: -95,
    top: -100,
    width: 210,
  },
  goldOrbSmall: {
    backgroundColor: 'rgba(255,255,255,0.025)',
    borderRadius: 65,
    bottom: 5,
    height: 130,
    left: -55,
    position: 'absolute',
    width: 130,
  },
  priceTopRow: {
    alignItems: 'flex-start',
    flexDirection: 'row-reverse',
    justifyContent: 'space-between',
  },
  priceEyebrow: {
    color: '#D9E0DC',
    fontFamily: 'Vazirmatn_500Medium',
    fontSize: 12,
  },
  liveRow: {
    alignItems: 'center',
    flexDirection: 'row-reverse',
    gap: 5,
    marginTop: 4,
  },
  liveDot: {
    backgroundColor: '#65BC93',
    borderRadius: 4,
    height: 7,
    width: 7,
  },
  liveText: {
    color: '#8AA397',
    fontSize: 9,
  },
  priceTrendPill: {
    alignItems: 'center',
    backgroundColor: 'rgba(95,181,141,0.12)',
    borderColor: 'rgba(95,181,141,0.18)',
    borderRadius: 10,
    borderWidth: 1,
    flexDirection: 'row-reverse',
    gap: 4,
    paddingHorizontal: 9,
    paddingVertical: 5,
  },
  priceTrendText: {
    color: '#7BC4A0',
    fontFamily: 'Vazirmatn_600SemiBold',
    fontSize: 11,
  },
  priceValueRow: {
    alignItems: 'baseline',
    flexDirection: 'row-reverse',
    gap: 8,
    marginTop: 13,
  },
  priceValue: {
    color: palette.surface,
    fontFamily: 'Vazirmatn_800ExtraBold',
    fontSize: 28,
    fontVariant: ['tabular-nums'],
    letterSpacing: -0.6,
    lineHeight: 38,
  },
  priceUnit: {
    color: '#A8B8B0',
    fontSize: 10,
  },
  chart: {
    height: 72,
    marginHorizontal: -5,
    marginTop: 3,
  },
  goldCardFooter: {
    alignItems: 'center',
    borderTopColor: 'rgba(255,255,255,0.08)',
    borderTopWidth: 1,
    flexDirection: 'row-reverse',
    justifyContent: 'space-around',
    marginTop: 2,
    paddingTop: 12,
  },
  marketItem: {
    alignItems: 'center',
    flex: 1,
  },
  marketLabel: {
    color: '#80978C',
    fontSize: 9,
  },
  marketValue: {
    color: '#DCE4E0',
    fontFamily: 'Vazirmatn_500Medium',
    fontSize: 11,
    marginTop: 3,
  },
  marketDivider: {
    backgroundColor: 'rgba(255,255,255,0.1)',
    height: 28,
    width: 1,
  },
  primaryAction: {
    alignItems: 'center',
    backgroundColor: palette.gold,
    borderRadius: 14,
    flexDirection: 'row-reverse',
    gap: 8,
    justifyContent: 'center',
    marginTop: 16,
    minHeight: 49,
  },
  primaryActionPressed: {
    opacity: 0.82,
    transform: [{ scale: 0.99 }],
  },
  primaryActionText: {
    color: palette.forest,
    fontFamily: 'Vazirmatn_700Bold',
    fontSize: 13,
  },
  summaryRow: {
    flexDirection: 'row-reverse',
    gap: 10,
    marginBottom: 24,
  },
  summaryCard: {
    backgroundColor: palette.surface,
    borderColor: palette.border,
    borderRadius: 18,
    borderWidth: 1,
    flex: 1,
    padding: 14,
  },
  summaryIcon: {
    alignItems: 'center',
    borderRadius: 11,
    height: 38,
    justifyContent: 'center',
    marginBottom: 10,
    width: 38,
  },
  summaryIconGold: {
    backgroundColor: palette.goldPale,
  },
  summaryIconGreen: {
    backgroundColor: '#E6F1EB',
  },
  summaryLabel: {
    color: palette.muted,
    fontSize: 10,
  },
  summaryValue: {
    fontFamily: 'Vazirmatn_700Bold',
    fontSize: 15,
    lineHeight: 26,
  },
  summaryDeltaRow: {
    alignItems: 'center',
    flexDirection: 'row-reverse',
    gap: 4,
    marginTop: 3,
  },
  summaryDeltaPositive: {
    color: palette.positive,
    fontSize: 9,
  },
  summaryCaption: {
    color: palette.muted,
    fontSize: 9,
    marginTop: 3,
  },
  sectionHeader: {
    alignItems: 'center',
    flexDirection: 'row-reverse',
    justifyContent: 'space-between',
    marginBottom: 12,
  },
  sectionTitle: {
    fontFamily: 'Vazirmatn_700Bold',
    fontSize: 16,
  },
  sectionAction: {
    alignItems: 'center',
    flexDirection: 'row-reverse',
    gap: 1,
  },
  sectionActionText: {
    color: palette.goldDark,
    fontFamily: 'Vazirmatn_500Medium',
    fontSize: 10,
  },
  horizontalProducts: {
    flexDirection: 'row-reverse',
    gap: 11,
    marginBottom: 24,
    paddingLeft: 3,
  },
  featuredCard: {
    backgroundColor: palette.surface,
    borderColor: palette.border,
    borderRadius: 18,
    borderWidth: 1,
    overflow: 'hidden',
    width: 158,
  },
  featuredVisual: {
    alignItems: 'center',
    height: 114,
    justifyContent: 'center',
    overflow: 'hidden',
    position: 'relative',
  },
  productHalo: {
    backgroundColor: 'rgba(255,255,255,0.22)',
    borderColor: 'rgba(255,255,255,0.32)',
    borderRadius: 54,
    borderWidth: 1,
    height: 108,
    position: 'absolute',
    width: 108,
  },
  karatPill: {
    backgroundColor: 'rgba(255,255,255,0.7)',
    borderRadius: 7,
    paddingHorizontal: 7,
    paddingVertical: 3,
    position: 'absolute',
    right: 8,
    top: 8,
  },
  karatPillText: {
    color: palette.forest,
    fontFamily: 'Vazirmatn_600SemiBold',
    fontSize: 8,
  },
  featuredBody: {
    padding: 11,
  },
  productName: {
    fontFamily: 'Vazirmatn_600SemiBold',
    fontSize: 12,
  },
  productMeta: {
    color: palette.muted,
    fontSize: 9,
    marginTop: 2,
  },
  productPriceRow: {
    alignItems: 'flex-end',
    flexDirection: 'row-reverse',
    justifyContent: 'space-between',
    marginTop: 9,
  },
  productPrice: {
    fontFamily: 'Vazirmatn_700Bold',
    fontSize: 13,
  },
  productPriceUnit: {
    color: palette.muted,
    fontSize: 7,
  },
  addButton: {
    alignItems: 'center',
    backgroundColor: palette.forest,
    borderRadius: 10,
    height: 32,
    justifyContent: 'center',
    width: 32,
  },
  invoiceList: {
    backgroundColor: palette.surface,
    borderColor: palette.border,
    borderRadius: 18,
    borderWidth: 1,
    overflow: 'hidden',
  },
  invoiceRow: {
    alignItems: 'center',
    flexDirection: 'row-reverse',
    justifyContent: 'space-between',
    minHeight: 72,
    paddingHorizontal: 12,
  },
  invoiceRowBorder: {
    borderBottomColor: palette.border,
    borderBottomWidth: 1,
  },
  invoiceIdentity: {
    alignItems: 'center',
    flexDirection: 'row-reverse',
    gap: 9,
  },
  invoiceIcon: {
    alignItems: 'center',
    backgroundColor: palette.goldPale,
    borderRadius: 11,
    height: 39,
    justifyContent: 'center',
    width: 39,
  },
  invoiceCustomer: {
    fontFamily: 'Vazirmatn_600SemiBold',
    fontSize: 11,
  },
  invoiceMetaRow: {
    alignItems: 'center',
    flexDirection: 'row-reverse',
    gap: 5,
    marginTop: 3,
  },
  invoiceMeta: {
    color: palette.muted,
    fontSize: 8,
  },
  metaDot: {
    backgroundColor: '#BBC1BD',
    borderRadius: 2,
    height: 3,
    width: 3,
  },
  invoiceAmount: {
    alignItems: 'flex-start',
  },
  invoiceAmountText: {
    fontFamily: 'Vazirmatn_600SemiBold',
    fontSize: 10,
  },
  invoiceAmountUnit: {
    color: palette.muted,
    fontSize: 7,
  },
  screenTitleRow: {
    alignItems: 'center',
    flexDirection: 'row-reverse',
    justifyContent: 'space-between',
    marginBottom: 18,
  },
  screenTitle: {
    fontFamily: 'Vazirmatn_800ExtraBold',
    fontSize: 24,
    letterSpacing: -0.4,
    lineHeight: 35,
  },
  screenSubtitle: {
    color: palette.muted,
    fontSize: 10,
    marginTop: 1,
  },
  inventoryPill: {
    alignItems: 'center',
    backgroundColor: '#E7F2EC',
    borderRadius: 10,
    flexDirection: 'row-reverse',
    gap: 5,
    paddingHorizontal: 9,
    paddingVertical: 6,
  },
  inventoryText: {
    color: palette.positive,
    fontFamily: 'Vazirmatn_500Medium',
    fontSize: 9,
  },
  searchRow: {
    flexDirection: 'row-reverse',
    gap: 9,
    marginBottom: 14,
  },
  searchBox: {
    alignItems: 'center',
    backgroundColor: palette.surface,
    borderColor: palette.border,
    borderRadius: 14,
    borderWidth: 1,
    flex: 1,
    flexDirection: 'row-reverse',
    gap: 8,
    minHeight: 48,
    paddingHorizontal: 13,
  },
  searchInput: {
    color: palette.ink,
    flex: 1,
    fontFamily: 'Vazirmatn_400Regular',
    fontSize: 11,
    height: 46,
    textAlign: 'right',
    writingDirection: 'rtl',
  },
  scanButton: {
    alignItems: 'center',
    backgroundColor: palette.forest,
    borderRadius: 14,
    height: 48,
    justifyContent: 'center',
    width: 48,
  },
  categories: {
    flexDirection: 'row-reverse',
    gap: 8,
    marginBottom: 17,
    paddingLeft: 2,
  },
  categoryPill: {
    backgroundColor: palette.surface,
    borderColor: palette.border,
    borderRadius: 11,
    borderWidth: 1,
    paddingHorizontal: 15,
    paddingVertical: 8,
  },
  categoryPillActive: {
    backgroundColor: palette.forest,
    borderColor: palette.forest,
  },
  categoryText: {
    color: palette.muted,
    fontFamily: 'Vazirmatn_500Medium',
    fontSize: 10,
  },
  categoryTextActive: {
    color: palette.surface,
  },
  catalogGrid: {
    flexDirection: 'row-reverse',
    flexWrap: 'wrap',
    gap: 10,
  },
  catalogCard: {
    backgroundColor: palette.surface,
    borderColor: palette.border,
    borderRadius: 18,
    borderWidth: 1,
    overflow: 'hidden',
    width: '48.5%',
  },
  catalogVisual: {
    alignItems: 'center',
    height: 126,
    justifyContent: 'center',
    overflow: 'hidden',
    position: 'relative',
  },
  productHaloSmall: {
    backgroundColor: 'rgba(255,255,255,0.22)',
    borderRadius: 48,
    height: 96,
    position: 'absolute',
    width: 96,
  },
  catalogCodePill: {
    backgroundColor: 'rgba(255,255,255,0.7)',
    borderRadius: 7,
    left: 8,
    paddingHorizontal: 6,
    paddingVertical: 3,
    position: 'absolute',
    top: 8,
  },
  catalogCode: {
    color: palette.forest,
    fontFamily: 'Vazirmatn_500Medium',
    fontSize: 7,
    writingDirection: 'ltr',
  },
  catalogBody: {
    padding: 11,
  },
  catalogName: {
    fontFamily: 'Vazirmatn_600SemiBold',
    fontSize: 12,
  },
  catalogMeta: {
    color: palette.muted,
    fontSize: 8,
    marginTop: 3,
  },
  catalogPrice: {
    fontFamily: 'Vazirmatn_700Bold',
    fontSize: 12,
    marginTop: 7,
  },
  catalogAddButton: {
    alignItems: 'center',
    backgroundColor: palette.goldPale,
    borderRadius: 10,
    flexDirection: 'row-reverse',
    gap: 5,
    justifyContent: 'center',
    marginTop: 9,
    minHeight: 34,
  },
  catalogAddText: {
    color: palette.forest,
    fontFamily: 'Vazirmatn_600SemiBold',
    fontSize: 8,
  },
  emptySearch: {
    alignItems: 'center',
    paddingVertical: 54,
  },
  emptyTitle: {
    fontFamily: 'Vazirmatn_600SemiBold',
    fontSize: 13,
    marginTop: 10,
  },
  emptySubtitle: {
    color: palette.muted,
    fontSize: 9,
    marginTop: 3,
    textAlign: 'center',
  },
  draftPill: {
    alignItems: 'center',
    backgroundColor: palette.goldPale,
    borderRadius: 10,
    flexDirection: 'row-reverse',
    gap: 5,
    paddingHorizontal: 10,
    paddingVertical: 7,
  },
  draftText: {
    color: palette.goldDark,
    fontFamily: 'Vazirmatn_600SemiBold',
    fontSize: 9,
  },
  invoiceSection: {
    backgroundColor: palette.surface,
    borderColor: palette.border,
    borderRadius: 19,
    borderWidth: 1,
    marginBottom: 12,
    padding: 14,
  },
  invoiceSectionTitleRow: {
    alignItems: 'center',
    flexDirection: 'row-reverse',
    gap: 8,
    marginBottom: 13,
  },
  sectionIconSmall: {
    alignItems: 'center',
    backgroundColor: palette.goldPale,
    borderRadius: 10,
    height: 34,
    justifyContent: 'center',
    width: 34,
  },
  invoiceSectionTitle: {
    fontFamily: 'Vazirmatn_700Bold',
    fontSize: 13,
  },
  itemsCount: {
    backgroundColor: '#F1F2EF',
    borderRadius: 8,
    marginRight: 'auto',
    paddingHorizontal: 8,
    paddingVertical: 4,
  },
  itemsCountText: {
    color: palette.muted,
    fontFamily: 'Vazirmatn_500Medium',
    fontSize: 8,
  },
  customerForm: {
    flexDirection: 'row-reverse',
    gap: 9,
  },
  fieldGroup: {
    flex: 1,
  },
  fieldLabel: {
    color: palette.muted,
    fontSize: 8,
    marginBottom: 5,
  },
  formInput: {
    backgroundColor: palette.cream,
    borderColor: palette.border,
    borderRadius: 11,
    borderWidth: 1,
    color: palette.ink,
    fontFamily: 'Vazirmatn_500Medium',
    fontSize: 10,
    height: 43,
    paddingHorizontal: 10,
    textAlign: 'right',
    writingDirection: 'rtl',
  },
  cartLines: {
    gap: 0,
  },
  cartLine: {
    alignItems: 'center',
    flexDirection: 'row-reverse',
    gap: 10,
    paddingVertical: 4,
  },
  cartProductVisual: {
    alignItems: 'center',
    borderRadius: 13,
    height: 69,
    justifyContent: 'center',
    width: 69,
  },
  cartLineBody: {
    flex: 1,
  },
  cartLineHeader: {
    alignItems: 'flex-start',
    flexDirection: 'row-reverse',
    justifyContent: 'space-between',
  },
  cartProductIdentity: {
    flex: 1,
  },
  cartProductName: {
    fontFamily: 'Vazirmatn_600SemiBold',
    fontSize: 11,
  },
  cartProductMeta: {
    color: palette.muted,
    fontSize: 8,
    marginTop: 2,
  },
  cartLineFooter: {
    alignItems: 'flex-end',
    flexDirection: 'row-reverse',
    justifyContent: 'space-between',
    marginTop: 8,
  },
  quantityControl: {
    alignItems: 'center',
    backgroundColor: palette.cream,
    borderColor: palette.border,
    borderRadius: 9,
    borderWidth: 1,
    flexDirection: 'row-reverse',
    gap: 8,
    padding: 3,
  },
  quantityButton: {
    alignItems: 'center',
    borderRadius: 6,
    height: 23,
    justifyContent: 'center',
    width: 23,
  },
  quantityValue: {
    fontFamily: 'Vazirmatn_600SemiBold',
    fontSize: 10,
    minWidth: 12,
    textAlign: 'center',
  },
  cartPrice: {
    alignItems: 'flex-start',
  },
  cartPriceValue: {
    fontFamily: 'Vazirmatn_600SemiBold',
    fontSize: 9,
  },
  cartPriceUnit: {
    color: palette.muted,
    fontSize: 7,
  },
  cartDivider: {
    backgroundColor: palette.border,
    height: 1,
    marginVertical: 11,
  },
  emptyCart: {
    alignItems: 'center',
    paddingVertical: 25,
  },
  totals: {
    gap: 2,
  },
  totalRow: {
    alignItems: 'center',
    flexDirection: 'row-reverse',
    justifyContent: 'space-between',
    paddingVertical: 6,
  },
  totalLabel: {
    color: palette.muted,
    fontSize: 10,
  },
  totalValueRow: {
    alignItems: 'baseline',
    flexDirection: 'row-reverse',
    gap: 4,
  },
  totalValue: {
    fontFamily: 'Vazirmatn_600SemiBold',
    fontSize: 10,
  },
  totalUnit: {
    color: palette.muted,
    fontSize: 7,
  },
  grandTotalRow: {
    borderTopColor: palette.border,
    borderTopWidth: 1,
    marginTop: 5,
    paddingTop: 13,
  },
  grandTotalLabel: {
    color: palette.ink,
    fontFamily: 'Vazirmatn_700Bold',
    fontSize: 13,
  },
  grandTotalValue: {
    color: palette.forest,
    fontFamily: 'Vazirmatn_800ExtraBold',
    fontSize: 15,
  },
  grandTotalUnit: {
    color: palette.forest,
    fontFamily: 'Vazirmatn_500Medium',
  },
  taxNote: {
    alignItems: 'center',
    backgroundColor: '#EAF3EE',
    borderRadius: 10,
    flexDirection: 'row-reverse',
    gap: 6,
    marginTop: 12,
    padding: 9,
  },
  taxNoteText: {
    color: palette.positive,
    flex: 1,
    fontSize: 8,
  },
  issueButton: {
    alignItems: 'center',
    backgroundColor: palette.forest,
    borderRadius: 15,
    flexDirection: 'row-reverse',
    gap: 9,
    justifyContent: 'center',
    minHeight: 53,
  },
  issueButtonDisabled: {
    opacity: 0.42,
  },
  issueButtonText: {
    color: palette.surface,
    fontFamily: 'Vazirmatn_700Bold',
    fontSize: 12,
  },
  profileCard: {
    alignItems: 'center',
    borderRadius: 24,
    marginBottom: 24,
    marginTop: 15,
    overflow: 'hidden',
    padding: 25,
  },
  profileAvatar: {
    alignItems: 'center',
    backgroundColor: palette.goldPale,
    borderRadius: 26,
    height: 72,
    justifyContent: 'center',
    width: 72,
  },
  profileName: {
    color: palette.surface,
    fontFamily: 'Vazirmatn_700Bold',
    fontSize: 17,
    marginTop: 12,
  },
  profileRole: {
    color: '#A9BCB3',
    fontSize: 10,
    marginTop: 2,
  },
  profileBadge: {
    alignItems: 'center',
    backgroundColor: 'rgba(121,196,159,0.1)',
    borderColor: 'rgba(121,196,159,0.2)',
    borderRadius: 10,
    borderWidth: 1,
    flexDirection: 'row-reverse',
    gap: 5,
    marginTop: 12,
    paddingHorizontal: 9,
    paddingVertical: 5,
  },
  profileBadgeText: {
    color: '#8FD2B1',
    fontFamily: 'Vazirmatn_500Medium',
    fontSize: 9,
  },
  settingsList: {
    backgroundColor: palette.surface,
    borderColor: palette.border,
    borderRadius: 18,
    borderWidth: 1,
    overflow: 'hidden',
  },
  settingsRow: {
    alignItems: 'center',
    flexDirection: 'row-reverse',
    justifyContent: 'space-between',
    minHeight: 72,
    paddingHorizontal: 12,
  },
  settingsIdentity: {
    alignItems: 'center',
    flexDirection: 'row-reverse',
    gap: 10,
  },
  settingsIcon: {
    alignItems: 'center',
    backgroundColor: palette.goldPale,
    borderRadius: 11,
    height: 40,
    justifyContent: 'center',
    width: 40,
  },
  settingsTitle: {
    fontFamily: 'Vazirmatn_600SemiBold',
    fontSize: 11,
  },
  settingsDescription: {
    color: palette.muted,
    fontSize: 8,
    marginTop: 2,
  },
  supportCard: {
    alignItems: 'center',
    backgroundColor: '#E9F1ED',
    borderRadius: 18,
    flexDirection: 'row-reverse',
    justifyContent: 'space-between',
    marginTop: 14,
    padding: 16,
  },
  supportTitle: {
    color: palette.forest,
    fontFamily: 'Vazirmatn_600SemiBold',
    fontSize: 11,
  },
  supportDescription: {
    color: palette.positive,
    fontSize: 8,
    marginTop: 2,
  },
  supportButton: {
    backgroundColor: palette.forest,
    borderRadius: 10,
    paddingHorizontal: 12,
    paddingVertical: 8,
  },
  supportButtonText: {
    color: palette.surface,
    fontFamily: 'Vazirmatn_600SemiBold',
    fontSize: 8,
  },
  bottomNav: {
    alignItems: 'center',
    backgroundColor: palette.surface,
    borderColor: palette.border,
    borderRadius: 22,
    borderWidth: 1,
    bottom: 12,
    flexDirection: 'row-reverse',
    justifyContent: 'space-around',
    left: 14,
    paddingHorizontal: 5,
    paddingVertical: 7,
    position: 'absolute',
    right: 14,
    shadowColor: '#1B342A',
    shadowOffset: { width: 0, height: 7 },
    shadowOpacity: 0.12,
    shadowRadius: 20,
  },
  navItem: {
    alignItems: 'center',
    flex: 1,
    gap: 2,
  },
  navIconWrap: {
    alignItems: 'center',
    borderRadius: 12,
    height: 34,
    justifyContent: 'center',
    position: 'relative',
    width: 45,
  },
  navIconActive: {
    backgroundColor: palette.forest,
  },
  navLabel: {
    color: palette.muted,
    fontSize: 8,
  },
  navLabelActive: {
    color: palette.forest,
    fontFamily: 'Vazirmatn_600SemiBold',
  },
  cartBadge: {
    alignItems: 'center',
    backgroundColor: palette.gold,
    borderColor: palette.surface,
    borderRadius: 8,
    borderWidth: 1.5,
    height: 16,
    justifyContent: 'center',
    minWidth: 16,
    paddingHorizontal: 3,
    position: 'absolute',
    right: 4,
    top: -4,
  },
  cartBadgeText: {
    color: palette.forest,
    fontFamily: 'Vazirmatn_700Bold',
    fontSize: 7,
    textAlign: 'center',
  },
  toast: {
    alignItems: 'center',
    alignSelf: 'center',
    backgroundColor: '#102C22',
    borderRadius: 13,
    bottom: 91,
    flexDirection: 'row-reverse',
    gap: 7,
    paddingHorizontal: 14,
    paddingVertical: 10,
    position: 'absolute',
  },
  toastText: {
    color: palette.surface,
    fontFamily: 'Vazirmatn_500Medium',
    fontSize: 10,
  },
  modalBackdrop: {
    alignItems: 'center',
    backgroundColor: 'rgba(8,24,18,0.65)',
    flex: 1,
    justifyContent: 'flex-end',
  },
  modalShell: {
    backgroundColor: palette.background,
    borderTopLeftRadius: 28,
    borderTopRightRadius: 28,
    flex: 1,
    marginTop: Platform.OS === 'web' ? 24 : 45,
    maxWidth: 480,
    overflow: 'hidden',
    width: '100%',
  },
  modalHeader: {
    alignItems: 'center',
    borderBottomColor: palette.border,
    borderBottomWidth: 1,
    flexDirection: 'row-reverse',
    justifyContent: 'space-between',
    paddingHorizontal: 18,
    paddingVertical: 14,
  },
  modalTitle: {
    fontFamily: 'Vazirmatn_700Bold',
    fontSize: 15,
  },
  modalSubtitle: {
    color: palette.muted,
    fontSize: 8,
    marginTop: 2,
  },
  modalClose: {
    alignItems: 'center',
    backgroundColor: palette.surface,
    borderColor: palette.border,
    borderRadius: 11,
    borderWidth: 1,
    height: 38,
    justifyContent: 'center',
    width: 38,
  },
  previewScroll: {
    padding: 15,
  },
  invoicePaper: {
    backgroundColor: palette.surface,
    borderColor: palette.border,
    borderRadius: 18,
    borderWidth: 1,
    padding: 18,
  },
  paperHeader: {
    alignItems: 'center',
    borderBottomColor: palette.gold,
    borderBottomWidth: 2,
    flexDirection: 'row-reverse',
    justifyContent: 'space-between',
    paddingBottom: 13,
  },
  paperBrand: {
    alignItems: 'center',
    flexDirection: 'row-reverse',
    gap: 9,
  },
  paperLogo: {
    alignItems: 'center',
    borderRadius: 10,
    height: 38,
    justifyContent: 'center',
    width: 38,
  },
  paperBrandName: {
    fontFamily: 'Vazirmatn_700Bold',
    fontSize: 13,
  },
  paperBrandCaption: {
    color: palette.muted,
    fontSize: 7,
  },
  paperTitleBlock: {
    alignItems: 'center',
    paddingVertical: 16,
  },
  paperTitle: {
    fontFamily: 'Vazirmatn_700Bold',
    fontSize: 15,
  },
  paperNumber: {
    color: palette.muted,
    fontSize: 8,
    marginTop: 3,
  },
  paperCustomer: {
    backgroundColor: palette.goldPale,
    borderRadius: 10,
    flexDirection: 'row-reverse',
    gap: 50,
    padding: 11,
  },
  paperLabel: {
    color: palette.muted,
    fontSize: 7,
  },
  paperCustomerValue: {
    fontFamily: 'Vazirmatn_600SemiBold',
    fontSize: 9,
    marginTop: 2,
  },
  paperTableHeader: {
    alignItems: 'center',
    backgroundColor: palette.forest,
    borderTopLeftRadius: 8,
    borderTopRightRadius: 8,
    flexDirection: 'row-reverse',
    marginTop: 15,
    paddingHorizontal: 9,
    paddingVertical: 8,
  },
  paperTableHeading: {
    color: palette.surface,
    flex: 1,
    fontFamily: 'Vazirmatn_500Medium',
    fontSize: 7,
    textAlign: 'center',
  },
  paperItemCol: {
    flex: 1.7,
    textAlign: 'right',
  },
  paperTableRow: {
    alignItems: 'center',
    borderBottomColor: palette.border,
    borderBottomWidth: 1,
    flexDirection: 'row-reverse',
    paddingHorizontal: 9,
    paddingVertical: 9,
  },
  paperItemName: {
    fontFamily: 'Vazirmatn_600SemiBold',
    fontSize: 8,
  },
  paperItemCode: {
    color: palette.muted,
    fontSize: 6,
    marginTop: 1,
    writingDirection: 'ltr',
  },
  paperCell: {
    flex: 1,
    fontSize: 7,
    textAlign: 'center',
  },
  paperTotals: {
    marginRight: 'auto',
    marginTop: 14,
    width: '72%',
  },
  paperTotalRow: {
    flexDirection: 'row-reverse',
    justifyContent: 'space-between',
    paddingVertical: 4,
  },
  paperTotalLabel: {
    color: palette.muted,
    fontSize: 7,
  },
  paperTotalValue: {
    fontFamily: 'Vazirmatn_500Medium',
    fontSize: 7,
  },
  paperGrandTotal: {
    alignItems: 'center',
    borderTopColor: palette.forest,
    borderTopWidth: 1,
    flexDirection: 'row-reverse',
    justifyContent: 'space-between',
    marginTop: 4,
    paddingTop: 8,
  },
  paperGrandLabel: {
    fontFamily: 'Vazirmatn_700Bold',
    fontSize: 9,
  },
  paperGrandValue: {
    color: palette.forest,
    fontFamily: 'Vazirmatn_800ExtraBold',
    fontSize: 11,
  },
  paperGrandUnit: {
    color: palette.muted,
    fontSize: 6,
  },
  paperFooter: {
    alignItems: 'center',
    backgroundColor: '#EAF3EE',
    borderRadius: 9,
    flexDirection: 'row-reverse',
    gap: 5,
    marginTop: 18,
    padding: 8,
  },
  paperFooterText: {
    color: palette.positive,
    flex: 1,
    fontSize: 6,
  },
  modalActions: {
    backgroundColor: palette.surface,
    borderTopColor: palette.border,
    borderTopWidth: 1,
    padding: 13,
  },
  exportButton: {
    alignItems: 'center',
    backgroundColor: palette.forest,
    borderRadius: 14,
    flexDirection: 'row-reverse',
    gap: 8,
    justifyContent: 'center',
    minHeight: 50,
  },
  exportButtonText: {
    color: palette.surface,
    fontFamily: 'Vazirmatn_700Bold',
    fontSize: 11,
  },
});
