import { useEffect, useMemo, useState } from 'react';
import {
  ActivityIndicator,
  Image,
  Modal,
  Platform,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';
import {
  BadgeCheck,
  Bell,
  Check,
  ChevronDown,
  ChevronLeft,
  Download,
  FileText,
  Gem,
  Grid2X2,
  Headphones,
  Heart,
  House,
  LogOut,
  MapPin,
  Minus,
  PackageCheck,
  Plus,
  Search,
  Share2,
  ShieldCheck,
  ShoppingBag,
  SlidersHorizontal,
  Trash2,
  Truck,
  UserRound,
  WalletCards,
  X,
} from 'lucide-react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { StatusBar } from 'expo-status-bar';
import {
  Vazirmatn_400Regular,
  Vazirmatn_500Medium,
  Vazirmatn_600SemiBold,
  Vazirmatn_700Bold,
  Vazirmatn_900Black,
  useFonts,
} from '@expo-google-fonts/vazirmatn';

import { calculateInvoice, cartLines, formatNumber, formatToman, productPrice } from './src/commerce';
import { ProductCard } from './src/components/ProductCard';
import { categories, products } from './src/data';
import { colors, fonts } from './src/theme';
import type { CartLine, CategoryId, Product } from './src/types';

type TabId = 'home' | 'catalog' | 'cart' | 'profile';

const navItems = [
  { id: 'home' as const, label: 'خانه', Icon: House },
  { id: 'catalog' as const, label: 'محصولات', Icon: Grid2X2 },
  { id: 'cart' as const, label: 'سبد خرید', Icon: ShoppingBag },
  { id: 'profile' as const, label: 'حساب من', Icon: UserRound },
];

export default function App() {
  const [fontsLoaded] = useFonts({
    Vazirmatn_400Regular,
    Vazirmatn_500Medium,
    Vazirmatn_600SemiBold,
    Vazirmatn_700Bold,
    Vazirmatn_900Black,
  });
  const [activeTab, setActiveTab] = useState<TabId>('home');
  const [selectedCategory, setSelectedCategory] = useState<CategoryId>('all');
  const [search, setSearch] = useState('');
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null);
  const [quantities, setQuantities] = useState<Record<string, number>>({});
  const [favourites, setFavourites] = useState<string[]>([]);
  const [invoiceVisible, setInvoiceVisible] = useState(false);
  const [toast, setToast] = useState('');

  const lines = useMemo(() => cartLines(quantities, products), [quantities]);
  const totals = useMemo(() => calculateInvoice(lines), [lines]);
  const itemCount = lines.reduce((sum, line) => sum + line.quantity, 0);
  const filteredProducts = useMemo(() => {
    const normalizedSearch = search.trim();
    return products.filter((product) => {
      const inCategory = selectedCategory === 'all' || product.category === selectedCategory;
      const matchesSearch =
        normalizedSearch.length === 0 ||
        product.name.includes(normalizedSearch) ||
        product.categoryLabel.includes(normalizedSearch);
      return inCategory && matchesSearch;
    });
  }, [search, selectedCategory]);

  useEffect(() => {
    if (!toast) return undefined;
    const timeout = setTimeout(() => setToast(''), 2400);
    return () => clearTimeout(timeout);
  }, [toast]);

  function addToCart(product: Product) {
    setQuantities((current) => ({
      ...current,
      [product.id]: (current[product.id] ?? 0) + 1,
    }));
    setToast(`${product.name} به سبد اضافه شد`);
  }

  function updateQuantity(productId: string, delta: number) {
    setQuantities((current) => {
      const next = Math.max(0, (current[productId] ?? 0) + delta);
      return { ...current, [productId]: next };
    });
  }

  function toggleFavourite(productId: string) {
    setFavourites((current) =>
      current.includes(productId)
        ? current.filter((id) => id !== productId)
        : [...current, productId],
    );
  }

  function openCategory(category: CategoryId) {
    setSelectedCategory(category);
    setActiveTab('catalog');
  }

  if (!fontsLoaded) {
    return (
      <View style={styles.loading}>
        <ActivityIndicator color={colors.gold} size="large" />
      </View>
    );
  }

  return (
    <View style={styles.viewport}>
      <StatusBar style="dark" />
      <View style={styles.appShell}>
        {activeTab === 'home' ? (
          <HomeScreen
            onAdd={addToCart}
            onCategory={openCategory}
            onView={setSelectedProduct}
            onViewAll={() => {
              setSelectedCategory('all');
              setActiveTab('catalog');
            }}
          />
        ) : null}
        {activeTab === 'catalog' ? (
          <CatalogScreen
            products={filteredProducts}
            search={search}
            selectedCategory={selectedCategory}
            onAdd={addToCart}
            onSearch={setSearch}
            onSelectCategory={setSelectedCategory}
            onView={setSelectedProduct}
          />
        ) : null}
        {activeTab === 'cart' ? (
          <CartScreen
            itemCount={itemCount}
            lines={lines}
            onBrowse={() => setActiveTab('catalog')}
            onCheckout={() => setInvoiceVisible(true)}
            onUpdateQuantity={updateQuantity}
          />
        ) : null}
        {activeTab === 'profile' ? <ProfileScreen /> : null}

        <BottomNavigation
          activeTab={activeTab}
          itemCount={itemCount}
          onChange={setActiveTab}
        />

        {toast ? (
          <View pointerEvents="none" style={styles.toast}>
            <View style={styles.toastIcon}>
              <Check color={colors.white} size={14} strokeWidth={3} />
            </View>
            <Text style={styles.toastText}>{toast}</Text>
          </View>
        ) : null}
      </View>

      <ProductModal
        favourite={selectedProduct ? favourites.includes(selectedProduct.id) : false}
        product={selectedProduct}
        onAdd={(product) => {
          addToCart(product);
          setSelectedProduct(null);
        }}
        onClose={() => setSelectedProduct(null)}
        onFavourite={toggleFavourite}
      />
      <InvoiceModal
        lines={lines}
        totals={totals}
        visible={invoiceVisible}
        onClose={() => setInvoiceVisible(false)}
        onSave={() => setToast('فاکتور با موفقیت ذخیره شد')}
      />
    </View>
  );
}

function HomeScreen({
  onAdd,
  onCategory,
  onView,
  onViewAll,
}: {
  onAdd: (product: Product) => void;
  onCategory: (category: CategoryId) => void;
  onView: (product: Product) => void;
  onViewAll: () => void;
}) {
  return (
    <ScrollView
      contentContainerStyle={styles.screenContent}
      showsVerticalScrollIndicator={false}
      style={styles.screen}
    >
      <View style={styles.topHeader}>
        <Pressable accessibilityLabel="اعلان‌ها" style={styles.iconButton}>
          <Bell color={colors.ink} size={20} strokeWidth={1.8} />
          <View style={styles.notificationDot} />
        </Pressable>
        <View style={styles.brand}>
          <View style={styles.brandMark}>
            <Gem color={colors.gold} size={20} strokeWidth={1.8} />
          </View>
          <View>
            <Text style={styles.brandName}>زرین</Text>
            <Text style={styles.brandCaption}>طلا و جواهر اصیل</Text>
          </View>
        </View>
      </View>

      <View style={styles.greetingRow}>
        <View style={styles.locationPill}>
          <ChevronDown color={colors.inkSoft} size={14} />
          <Text style={styles.locationText}>تهران، سعادت‌آباد</Text>
          <MapPin color={colors.gold} size={15} />
        </View>
        <View>
          <Text style={styles.eyebrow}>سلام، نگار جان</Text>
          <Text style={styles.greeting}>امروز چی می‌درخشه؟</Text>
        </View>
      </View>

      <LinearGradient
        colors={[colors.forestDark, '#1C594C']}
        end={{ x: 0, y: 1 }}
        start={{ x: 1, y: 0 }}
        style={styles.hero}
      >
        <View style={styles.heroOrbLarge} />
        <View style={styles.heroOrbSmall} />
        <View style={styles.heroSparkOne}>
          <Text style={styles.spark}>✦</Text>
        </View>
        <View style={styles.heroSparkTwo}>
          <Text style={styles.sparkSmall}>✧</Text>
        </View>
        <View style={styles.heroContent}>
          <View style={styles.heroKicker}>
            <View style={styles.liveDot} />
            <Text style={styles.heroKickerText}>قیمت لحظه‌ای طلای ۱۸ عیار</Text>
          </View>
          <Text style={styles.heroPrice}>۱۲٬۴۸۰٬۰۰۰</Text>
          <Text style={styles.heroPriceUnit}>تومان / گرم</Text>
          <View style={styles.heroChangeRow}>
            <Text style={styles.heroUpdate}>بروزرسانی ۵ دقیقه پیش</Text>
            <View style={styles.heroChange}>
              <Text style={styles.heroChangeText}>٪ ۰٫۸۶ +</Text>
            </View>
          </View>
        </View>
        <Pressable accessibilityLabel="مشاهده کالکشن جدید" onPress={onViewAll} style={styles.heroButton}>
          <ChevronLeft color={colors.forest} size={16} />
          <Text style={styles.heroButtonText}>مشاهده کالکشن</Text>
        </Pressable>
      </LinearGradient>

      <SectionHeader action="مشاهده همه" title="دسته‌بندی‌ها" onAction={onViewAll} />
      <View style={styles.categoryRow}>
        {categories.slice(1).map((category) => (
          <Pressable
            accessibilityLabel={`دسته ${category.label}`}
            key={category.id}
            onPress={() => onCategory(category.id)}
            style={({ pressed }) => [styles.categoryItem, pressed && styles.categoryPressed]}
          >
            <View style={styles.categoryIcon}>
              <Text style={styles.categorySymbol}>{category.icon}</Text>
            </View>
            <Text style={styles.categoryLabel}>{category.label}</Text>
          </Pressable>
        ))}
      </View>

      <View style={styles.assuranceStrip}>
        <AssuranceItem Icon={ShieldCheck} label="ضمانت اصالت" />
        <View style={styles.assuranceDivider} />
        <AssuranceItem Icon={Truck} label="ارسال امن" />
        <View style={styles.assuranceDivider} />
        <AssuranceItem Icon={WalletCards} label="پرداخت مطمئن" />
      </View>

      <SectionHeader action="همه محصولات" title="محبوب‌ترین‌ها" onAction={onViewAll} />
      <View style={styles.productGrid}>
        {products.slice(0, 4).map((product) => (
          <ProductCard key={product.id} product={product} onAdd={onAdd} onView={onView} />
        ))}
      </View>

      <LinearGradient
        colors={['#EFE2C1', '#F9F3E4']}
        end={{ x: 0, y: 0 }}
        start={{ x: 1, y: 1 }}
        style={styles.giftBanner}
      >
        <View style={styles.giftIcon}>
          <Gem color={colors.forest} size={25} strokeWidth={1.5} />
        </View>
        <View style={styles.giftCopy}>
          <Text style={styles.giftTitle}>هدیه‌ای که ماندگار می‌شود</Text>
          <Text style={styles.giftText}>بسته‌بندی ویژه و کارت تبریک رایگان</Text>
        </View>
        <ChevronLeft color={colors.forest} size={20} />
      </LinearGradient>

      <Text style={styles.disclaimer}>قیمت‌ها در این نسخه نمایشی هستند.</Text>
    </ScrollView>
  );
}

function CatalogScreen({
  products: visibleProducts,
  search,
  selectedCategory,
  onAdd,
  onSearch,
  onSelectCategory,
  onView,
}: {
  products: Product[];
  search: string;
  selectedCategory: CategoryId;
  onAdd: (product: Product) => void;
  onSearch: (value: string) => void;
  onSelectCategory: (category: CategoryId) => void;
  onView: (product: Product) => void;
}) {
  return (
    <ScrollView
      contentContainerStyle={styles.screenContent}
      keyboardShouldPersistTaps="handled"
      showsVerticalScrollIndicator={false}
      style={styles.screen}
    >
      <View style={styles.pageHeader}>
        <Pressable accessibilityLabel="فیلتر محصولات" style={styles.iconButton}>
          <SlidersHorizontal color={colors.ink} size={20} />
        </Pressable>
        <View>
          <Text style={styles.pageTitle}>محصولات زرین</Text>
          <Text style={styles.pageSubtitle}>انتخابی برای هر سلیقه</Text>
        </View>
      </View>

      <View style={styles.searchBox}>
        <Search color={colors.inkSoft} size={19} />
        <TextInput
          accessibilityLabel="جستجوی محصولات"
          onChangeText={onSearch}
          placeholder="جستجو بین محصولات..."
          placeholderTextColor="#9AA19E"
          style={styles.searchInput}
          value={search}
        />
      </View>

      <ScrollView
        contentContainerStyle={styles.filterChips}
        horizontal
        showsHorizontalScrollIndicator={false}
      >
        {categories.map((category) => {
          const selected = category.id === selectedCategory;
          return (
            <Pressable
              accessibilityLabel={`فیلتر ${category.label}`}
              key={category.id}
              onPress={() => onSelectCategory(category.id)}
              style={[styles.filterChip, selected && styles.filterChipSelected]}
            >
              <Text style={[styles.filterChipText, selected && styles.filterChipTextSelected]}>
                {category.label}
              </Text>
            </Pressable>
          );
        })}
      </ScrollView>

      <View style={styles.resultHeader}>
        <Text style={styles.resultCount}>{formatNumber(visibleProducts.length)} محصول</Text>
        <Text style={styles.resultTitle}>
          {categories.find((category) => category.id === selectedCategory)?.label}
        </Text>
      </View>

      {visibleProducts.length ? (
        <View style={styles.productGrid}>
          {visibleProducts.map((product) => (
            <ProductCard key={product.id} product={product} onAdd={onAdd} onView={onView} />
          ))}
        </View>
      ) : (
        <View style={styles.noResults}>
          <Search color={colors.gold} size={30} strokeWidth={1.5} />
          <Text style={styles.noResultsTitle}>محصولی پیدا نشد</Text>
          <Text style={styles.noResultsText}>عبارت دیگری را امتحان کنید.</Text>
        </View>
      )}
    </ScrollView>
  );
}

function CartScreen({
  itemCount,
  lines,
  onBrowse,
  onCheckout,
  onUpdateQuantity,
}: {
  itemCount: number;
  lines: CartLine[];
  onBrowse: () => void;
  onCheckout: () => void;
  onUpdateQuantity: (productId: string, delta: number) => void;
}) {
  const totals = calculateInvoice(lines);

  return (
    <ScrollView
      contentContainerStyle={styles.screenContent}
      showsVerticalScrollIndicator={false}
      style={styles.screen}
    >
      <View style={styles.pageHeader}>
        <View style={styles.itemCountPill}>
          <Text style={styles.itemCountText}>{formatNumber(itemCount)} کالا</Text>
        </View>
        <View>
          <Text style={styles.pageTitle}>سبد خرید</Text>
          <Text style={styles.pageSubtitle}>خرید امن و شفاف</Text>
        </View>
      </View>

      {lines.length === 0 ? (
        <View style={styles.emptyCart}>
          <View style={styles.emptyCartIcon}>
            <ShoppingBag color={colors.gold} size={42} strokeWidth={1.4} />
          </View>
          <Text style={styles.emptyCartTitle}>سبد خریدت هنوز خالیه</Text>
          <Text style={styles.emptyCartText}>
            از بین انتخاب‌های خاص زرین، قطعه محبوبت را پیدا کن.
          </Text>
          <Pressable accessibilityLabel="مشاهده محصولات" onPress={onBrowse} style={styles.primaryButton}>
            <Text style={styles.primaryButtonText}>مشاهده محصولات</Text>
          </Pressable>
        </View>
      ) : (
        <>
          <View style={styles.cartList}>
            {lines.map((line) => (
              <View key={line.product.id} style={styles.cartItem}>
                <Image source={{ uri: line.product.image }} style={styles.cartImage} />
                <View style={styles.cartItemCopy}>
                  <Text numberOfLines={1} style={styles.cartItemName}>
                    {line.product.name}
                  </Text>
                  <Text style={styles.cartItemMeta}>
                    {formatNumber(line.product.weight)} گرم • طلای {formatNumber(line.product.karat)} عیار
                  </Text>
                  <Text style={styles.cartItemPrice}>
                    {formatToman(productPrice(line.product) * line.quantity)}
                  </Text>
                  <View style={styles.quantityRow}>
                    <Pressable
                      accessibilityLabel={`افزایش تعداد ${line.product.name}`}
                      onPress={() => onUpdateQuantity(line.product.id, 1)}
                      style={styles.quantityButton}
                    >
                      <Plus color={colors.forest} size={15} />
                    </Pressable>
                    <Text style={styles.quantityText}>{formatNumber(line.quantity)}</Text>
                    <Pressable
                      accessibilityLabel={
                        line.quantity === 1
                          ? `حذف ${line.product.name}`
                          : `کاهش تعداد ${line.product.name}`
                      }
                      onPress={() => onUpdateQuantity(line.product.id, -1)}
                      style={styles.quantityButton}
                    >
                      {line.quantity === 1 ? (
                        <Trash2 color={colors.danger} size={14} />
                      ) : (
                        <Minus color={colors.forest} size={15} />
                      )}
                    </Pressable>
                  </View>
                </View>
              </View>
            ))}
          </View>

          <View style={styles.deliveryCard}>
            <View style={styles.deliveryIcon}>
              <Truck color={colors.forest} size={20} />
            </View>
            <View style={styles.deliveryCopy}>
              <Text style={styles.deliveryTitle}>ارسال ویژه و بیمه‌شده</Text>
              <Text style={styles.deliveryText}>تحویل حدودی: ۲ تا ۴ روز کاری</Text>
            </View>
            <BadgeCheck color={colors.gold} size={20} />
          </View>

          <View style={styles.summaryCard}>
            <Text style={styles.summaryTitle}>خلاصه سفارش</Text>
            <SummaryRow label="ارزش طلای خام" value={formatToman(totals.goldValue)} />
            <SummaryRow label="اجرت و سود فروشنده" value={formatToman(totals.serviceFee)} />
            <SummaryRow label="مالیات خدمات" value={formatToman(totals.tax)} />
            <SummaryRow
              highlight
              label="هزینه ارسال"
              value={totals.shipping ? formatToman(totals.shipping) : 'رایگان'}
            />
            {totals.discount ? (
              <SummaryRow
                highlight
                label="تخفیف خرید چندمحصولی"
                value={`− ${formatToman(totals.discount)}`}
              />
            ) : null}
            <View style={styles.summaryDivider} />
            <View style={styles.totalRow}>
              <Text style={styles.totalValue}>{formatToman(totals.grandTotal)}</Text>
              <Text style={styles.totalLabel}>مبلغ قابل پرداخت</Text>
            </View>
          </View>

          <Pressable
            accessibilityLabel="صدور فاکتور و ادامه خرید"
            onPress={onCheckout}
            style={({ pressed }) => [styles.checkoutButton, pressed && styles.checkoutPressed]}
          >
            <FileText color={colors.white} size={20} />
            <Text style={styles.checkoutButtonText}>صدور فاکتور و ادامه</Text>
          </Pressable>
          <View style={styles.secureNote}>
            <ShieldCheck color={colors.forest} size={16} />
            <Text style={styles.secureNoteText}>تمام جزئیات وزن و اجرت در فاکتور ثبت می‌شود.</Text>
          </View>
        </>
      )}
    </ScrollView>
  );
}

function ProfileScreen() {
  const menuItems = [
    { label: 'سفارش‌های من', caption: 'پیگیری و مشاهده جزئیات', Icon: PackageCheck },
    { label: 'فاکتورهای من', caption: 'دانلود فاکتورهای رسمی', Icon: FileText },
    { label: 'آدرس‌های من', caption: 'مدیریت آدرس‌های تحویل', Icon: MapPin },
    { label: 'پشتیبانی زرین', caption: 'هر روز از ۹ تا ۲۱', Icon: Headphones },
  ];

  return (
    <ScrollView
      contentContainerStyle={styles.screenContent}
      showsVerticalScrollIndicator={false}
      style={styles.screen}
    >
      <View style={styles.pageHeader}>
        <Pressable accessibilityLabel="اعلان‌ها" style={styles.iconButton}>
          <Bell color={colors.ink} size={20} />
        </Pressable>
        <View>
          <Text style={styles.pageTitle}>حساب کاربری</Text>
          <Text style={styles.pageSubtitle}>مدیریت پروفایل و سفارش‌ها</Text>
        </View>
      </View>

      <LinearGradient
        colors={[colors.forestDark, '#1C594C']}
        style={styles.profileCard}
      >
        <View style={styles.profilePattern}>✦</View>
        <View style={styles.avatar}>
          <Text style={styles.avatarText}>ن</Text>
        </View>
        <View style={styles.profileCopy}>
          <Text style={styles.profileName}>نگار احمدی</Text>
          <Text style={styles.profilePhone}>۰۹۱۲ ••• •• ۶۴</Text>
          <View style={styles.clubBadge}>
            <Gem color={colors.gold} size={13} />
            <Text style={styles.clubBadgeText}>عضو باشگاه زرین</Text>
          </View>
        </View>
      </LinearGradient>

      <View style={styles.statsRow}>
        <View style={styles.statItem}>
          <Text style={styles.statValue}>۳</Text>
          <Text style={styles.statLabel}>سفارش</Text>
        </View>
        <View style={styles.statsDivider} />
        <View style={styles.statItem}>
          <Text style={styles.statValue}>۲</Text>
          <Text style={styles.statLabel}>علاقه‌مندی</Text>
        </View>
        <View style={styles.statsDivider} />
        <View style={styles.statItem}>
          <Text style={styles.statValue}>۱٬۲۵۰</Text>
          <Text style={styles.statLabel}>امتیاز</Text>
        </View>
      </View>

      <View style={styles.profileMenu}>
        {menuItems.map(({ label, caption, Icon }) => (
          <Pressable accessibilityLabel={label} key={label} style={styles.profileMenuItem}>
            <ChevronLeft color={colors.inkSoft} size={18} />
            <View style={styles.profileMenuCopy}>
              <Text style={styles.profileMenuTitle}>{label}</Text>
              <Text style={styles.profileMenuCaption}>{caption}</Text>
            </View>
            <View style={styles.profileMenuIcon}>
              <Icon color={colors.forest} size={20} />
            </View>
          </Pressable>
        ))}
      </View>

      <Pressable accessibilityLabel="خروج از حساب" style={styles.logoutButton}>
        <LogOut color={colors.danger} size={18} />
        <Text style={styles.logoutText}>خروج از حساب کاربری</Text>
      </Pressable>
    </ScrollView>
  );
}

function ProductModal({
  favourite,
  product,
  onAdd,
  onClose,
  onFavourite,
}: {
  favourite: boolean;
  product: Product | null;
  onAdd: (product: Product) => void;
  onClose: () => void;
  onFavourite: (productId: string) => void;
}) {
  if (!product) return null;

  return (
    <Modal animationType="slide" onRequestClose={onClose} transparent visible>
      <View style={styles.modalBackdrop}>
        <View style={styles.productSheet}>
          <ScrollView showsVerticalScrollIndicator={false}>
            <View style={styles.productHero}>
              <Image source={{ uri: product.image }} style={styles.productHeroImage} />
              <Pressable accessibilityLabel="بستن جزئیات محصول" onPress={onClose} style={styles.closeButton}>
                <X color={colors.ink} size={20} />
              </Pressable>
              <View style={styles.productActions}>
                <Pressable accessibilityLabel="اشتراک محصول" style={styles.floatingButton}>
                  <Share2 color={colors.ink} size={18} />
                </Pressable>
                <Pressable
                  accessibilityLabel="افزودن به علاقه‌مندی"
                  onPress={() => onFavourite(product.id)}
                  style={styles.floatingButton}
                >
                  <Heart
                    color={favourite ? colors.danger : colors.ink}
                    fill={favourite ? colors.danger : 'transparent'}
                    size={19}
                  />
                </Pressable>
              </View>
            </View>
            <View style={styles.productDetails}>
              <View style={styles.productDetailKicker}>
                <Text style={styles.productDetailKickerText}>{product.categoryLabel}</Text>
                <View style={styles.productDetailDot} />
                <Text style={styles.productDetailKickerText}>کد {product.id.toUpperCase()}</Text>
              </View>
              <Text style={styles.productDetailName}>{product.name}</Text>
              <Text style={styles.productDescription}>
                طراحی ظریف و ماندگار، ساخته‌شده از طلای استاندارد همراه با شناسنامه اصالت زرین.
              </Text>

              <View style={styles.specRow}>
                <Spec label="وزن" value={`${formatNumber(product.weight)} گرم`} />
                <View style={styles.specDivider} />
                <Spec label="عیار" value={`${formatNumber(product.karat)}`} />
                <View style={styles.specDivider} />
                <Spec label="اصالت" value="تضمینی" />
              </View>

              <View style={styles.priceBreakdown}>
                <Text style={styles.priceBreakdownTitle}>جزئیات قیمت</Text>
                <SummaryRow label="ارزش طلای خام" value={formatToman(product.goldValue)} />
                <SummaryRow label="اجرت، سود و مالیات" value={formatToman(productPrice(product) - product.goldValue)} />
                <View style={styles.summaryDivider} />
                <View style={styles.totalRow}>
                  <Text style={styles.totalValue}>{formatToman(productPrice(product))}</Text>
                  <Text style={styles.totalLabel}>قیمت نهایی</Text>
                </View>
              </View>
            </View>
          </ScrollView>
          <View style={styles.productFooter}>
            <Pressable
              accessibilityLabel={`افزودن ${product.name} به سبد خرید`}
              onPress={() => onAdd(product)}
              style={styles.checkoutButton}
            >
              <ShoppingBag color={colors.white} size={19} />
              <Text style={styles.checkoutButtonText}>افزودن به سبد خرید</Text>
            </Pressable>
          </View>
        </View>
      </View>
    </Modal>
  );
}

function InvoiceModal({
  lines,
  totals,
  visible,
  onClose,
  onSave,
}: {
  lines: CartLine[];
  totals: ReturnType<typeof calculateInvoice>;
  visible: boolean;
  onClose: () => void;
  onSave: () => void;
}) {
  return (
    <Modal animationType="slide" onRequestClose={onClose} visible={visible}>
      <View style={styles.invoiceViewport}>
        <View style={styles.invoiceShell}>
          <View style={styles.invoiceHeader}>
            <Pressable accessibilityLabel="بستن فاکتور" onPress={onClose} style={styles.invoiceClose}>
              <X color={colors.ink} size={21} />
            </Pressable>
            <View>
              <Text style={styles.invoiceHeaderTitle}>فاکتور خرید</Text>
              <Text style={styles.invoiceHeaderCaption}>پیش‌نمایش نهایی</Text>
            </View>
            <View style={styles.invoiceHeaderMark}>
              <Gem color={colors.gold} size={22} />
            </View>
          </View>

          <ScrollView
            contentContainerStyle={styles.invoiceScrollContent}
            showsVerticalScrollIndicator={false}
          >
            <View style={styles.invoiceSuccess}>
              <View style={styles.invoiceSuccessIcon}>
                <Check color={colors.white} size={25} strokeWidth={3} />
              </View>
              <Text style={styles.invoiceSuccessTitle}>فاکتور شما آماده است</Text>
              <Text style={styles.invoiceSuccessText}>
                جزئیات وزن، اجرت و مالیات به‌صورت شفاف ثبت شد.
              </Text>
            </View>

            <View style={styles.receipt}>
              <View style={styles.receiptBrand}>
                <View style={styles.receiptBrandMark}>
                  <Gem color={colors.gold} size={22} />
                </View>
                <View>
                  <Text style={styles.receiptBrandName}>گالری زرین</Text>
                  <Text style={styles.receiptBrandMeta}>فروش تخصصی طلا و جواهر</Text>
                </View>
              </View>

              <View style={styles.receiptRule} />
              <View style={styles.receiptMetaGrid}>
                <ReceiptMeta label="شماره فاکتور" value="ZR-۱۴۰۵-۲۴۸۱" />
                <ReceiptMeta label="تاریخ صدور" value="۱۸ مرداد ۱۴۰۵" />
                <ReceiptMeta label="خریدار" value="نگار احمدی" />
                <ReceiptMeta label="نوع پرداخت" value="درگاه آنلاین" />
              </View>
              <View style={styles.receiptRule} />

              <Text style={styles.receiptSectionTitle}>اقلام سفارش</Text>
              {lines.map((line, index) => (
                <View key={line.product.id} style={styles.receiptLine}>
                  <Text style={styles.receiptLinePrice}>
                    {formatToman(productPrice(line.product) * line.quantity)}
                  </Text>
                  <View style={styles.receiptLineCopy}>
                    <Text style={styles.receiptLineName}>
                      {formatNumber(index + 1)}. {line.product.name}
                    </Text>
                    <Text style={styles.receiptLineMeta}>
                      {formatNumber(line.product.weight)} گرم × {formatNumber(line.quantity)}
                    </Text>
                  </View>
                </View>
              ))}

              <View style={styles.receiptRule} />
              <SummaryRow label="ارزش طلای خام" value={formatToman(totals.goldValue)} />
              <SummaryRow label="اجرت و سود فروشنده" value={formatToman(totals.serviceFee)} />
              <SummaryRow label="مالیات خدمات" value={formatToman(totals.tax)} />
              <SummaryRow
                label="هزینه ارسال"
                value={totals.shipping ? formatToman(totals.shipping) : 'رایگان'}
              />
              {totals.discount ? (
                <SummaryRow label="تخفیف" value={`− ${formatToman(totals.discount)}`} />
              ) : null}
              <View style={styles.receiptTotal}>
                <Text style={styles.receiptTotalValue}>{formatToman(totals.grandTotal)}</Text>
                <Text style={styles.receiptTotalLabel}>مبلغ نهایی فاکتور</Text>
              </View>

              <View style={styles.invoiceStamp}>
                <ShieldCheck color={colors.forest} size={20} />
                <View>
                  <Text style={styles.invoiceStampTitle}>تضمین اصالت زرین</Text>
                  <Text style={styles.invoiceStampText}>این فاکتور دارای اعتبار فروشگاهی است.</Text>
                </View>
              </View>
            </View>

            <Pressable
              accessibilityLabel="ذخیره فاکتور"
              onPress={() => {
                onSave();
                onClose();
              }}
              style={styles.checkoutButton}
            >
              <Download color={colors.white} size={20} />
              <Text style={styles.checkoutButtonText}>ذخیره فاکتور</Text>
            </Pressable>
            <Pressable accessibilityLabel="بازگشت به سبد خرید" onPress={onClose} style={styles.secondaryButton}>
              <Text style={styles.secondaryButtonText}>بازگشت به سبد خرید</Text>
            </Pressable>
          </ScrollView>
        </View>
      </View>
    </Modal>
  );
}

function BottomNavigation({
  activeTab,
  itemCount,
  onChange,
}: {
  activeTab: TabId;
  itemCount: number;
  onChange: (tab: TabId) => void;
}) {
  return (
    <View style={styles.bottomNav}>
      {navItems.map(({ id, label, Icon }) => {
        const active = activeTab === id;
        return (
          <Pressable
            accessibilityLabel={label}
            key={id}
            onPress={() => onChange(id)}
            style={styles.navItem}
          >
            <View style={[styles.navIconWrap, active && styles.navIconWrapActive]}>
              <Icon
                color={active ? colors.forest : '#8A9490'}
                fill={active && id === 'home' ? colors.forest : 'transparent'}
                size={21}
                strokeWidth={active ? 2.2 : 1.8}
              />
              {id === 'cart' && itemCount > 0 ? (
                <View style={styles.cartBadge}>
                  <Text style={styles.cartBadgeText}>{formatNumber(itemCount)}</Text>
                </View>
              ) : null}
            </View>
            <Text style={[styles.navLabel, active && styles.navLabelActive]}>{label}</Text>
          </Pressable>
        );
      })}
    </View>
  );
}

function SectionHeader({
  title,
  action,
  onAction,
}: {
  title: string;
  action: string;
  onAction: () => void;
}) {
  return (
    <View style={styles.sectionHeader}>
      <Pressable accessibilityLabel={action} onPress={onAction} style={styles.sectionAction}>
        <ChevronLeft color={colors.gold} size={14} />
        <Text style={styles.sectionActionText}>{action}</Text>
      </Pressable>
      <Text style={styles.sectionTitle}>{title}</Text>
    </View>
  );
}

function AssuranceItem({
  Icon,
  label,
}: {
  Icon: typeof ShieldCheck;
  label: string;
}) {
  return (
    <View style={styles.assuranceItem}>
      <Icon color={colors.gold} size={18} strokeWidth={1.8} />
      <Text style={styles.assuranceLabel}>{label}</Text>
    </View>
  );
}

function SummaryRow({
  label,
  value,
  highlight = false,
}: {
  label: string;
  value: string;
  highlight?: boolean;
}) {
  return (
    <View style={styles.summaryRow}>
      <Text style={[styles.summaryValue, highlight && styles.summaryHighlight]}>{value}</Text>
      <Text style={styles.summaryLabel}>{label}</Text>
    </View>
  );
}

function Spec({ label, value }: { label: string; value: string }) {
  return (
    <View style={styles.spec}>
      <Text style={styles.specValue}>{value}</Text>
      <Text style={styles.specLabel}>{label}</Text>
    </View>
  );
}

function ReceiptMeta({ label, value }: { label: string; value: string }) {
  return (
    <View style={styles.receiptMeta}>
      <Text style={styles.receiptMetaLabel}>{label}</Text>
      <Text style={styles.receiptMetaValue}>{value}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  viewport: {
    flex: 1,
    backgroundColor: Platform.OS === 'web' ? '#E6E0D6' : colors.cream,
  },
  appShell: {
    width: '100%',
    maxWidth: 460,
    flex: 1,
    alignSelf: 'center',
    overflow: 'hidden',
    backgroundColor: colors.cream,
    shadowColor: colors.black,
    shadowOffset: { width: 0, height: 0 },
    shadowOpacity: 0.12,
    shadowRadius: 28,
  },
  loading: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: colors.cream,
  },
  screen: {
    flex: 1,
  },
  screenContent: {
    paddingTop: Platform.OS === 'android' ? 50 : 28,
    paddingHorizontal: 18,
    paddingBottom: 120,
  },
  topHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  brand: {
    flexDirection: 'row-reverse',
    alignItems: 'center',
    gap: 10,
  },
  brandMark: {
    width: 42,
    height: 42,
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: 14,
    backgroundColor: colors.forest,
  },
  brandName: {
    color: colors.ink,
    fontFamily: fonts.black,
    fontSize: 20,
    lineHeight: 22,
    textAlign: 'right',
  },
  brandCaption: {
    marginTop: 2,
    color: colors.inkSoft,
    fontFamily: fonts.regular,
    fontSize: 9.5,
    textAlign: 'right',
  },
  iconButton: {
    width: 42,
    height: 42,
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: 14,
    borderWidth: 1,
    borderColor: colors.line,
    backgroundColor: colors.paper,
  },
  notificationDot: {
    position: 'absolute',
    top: 9,
    right: 9,
    width: 7,
    height: 7,
    borderRadius: 4,
    borderWidth: 1.5,
    borderColor: colors.paper,
    backgroundColor: colors.gold,
  },
  greetingRow: {
    marginTop: 24,
    flexDirection: 'row',
    alignItems: 'flex-end',
    justifyContent: 'space-between',
  },
  eyebrow: {
    color: colors.gold,
    fontFamily: fonts.semibold,
    fontSize: 11,
    textAlign: 'right',
  },
  greeting: {
    marginTop: 3,
    color: colors.ink,
    fontFamily: fonts.bold,
    fontSize: 18,
    textAlign: 'right',
  },
  locationPill: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
    borderRadius: 14,
    backgroundColor: colors.paper,
    borderWidth: 1,
    borderColor: colors.line,
    paddingHorizontal: 9,
    paddingVertical: 7,
  },
  locationText: {
    color: colors.inkSoft,
    fontFamily: fonts.regular,
    fontSize: 9,
    writingDirection: 'rtl',
  },
  hero: {
    minHeight: 228,
    marginTop: 20,
    overflow: 'hidden',
    borderRadius: 28,
    padding: 21,
  },
  heroOrbLarge: {
    position: 'absolute',
    width: 220,
    height: 220,
    left: -76,
    top: -48,
    borderRadius: 120,
    borderWidth: 1,
    borderColor: 'rgba(231,194,109,0.22)',
    backgroundColor: 'rgba(255,255,255,0.025)',
  },
  heroOrbSmall: {
    position: 'absolute',
    width: 130,
    height: 130,
    left: -25,
    bottom: -64,
    borderRadius: 70,
    borderWidth: 1,
    borderColor: 'rgba(231,194,109,0.16)',
  },
  heroSparkOne: {
    position: 'absolute',
    left: 54,
    top: 62,
  },
  heroSparkTwo: {
    position: 'absolute',
    left: 110,
    top: 26,
  },
  spark: {
    color: colors.gold,
    fontSize: 28,
  },
  sparkSmall: {
    color: '#F2D89C',
    fontSize: 15,
  },
  heroContent: {
    alignItems: 'flex-end',
  },
  heroKicker: {
    flexDirection: 'row-reverse',
    alignItems: 'center',
    gap: 7,
  },
  liveDot: {
    width: 7,
    height: 7,
    borderRadius: 4,
    backgroundColor: '#62D59C',
  },
  heroKickerText: {
    color: '#C9DCD6',
    fontFamily: fonts.medium,
    fontSize: 10.5,
  },
  heroPrice: {
    marginTop: 10,
    color: colors.white,
    fontFamily: fonts.black,
    fontSize: 29,
    letterSpacing: 0.4,
  },
  heroPriceUnit: {
    marginTop: -3,
    color: '#C9DCD6',
    fontFamily: fonts.regular,
    fontSize: 10,
  },
  heroChangeRow: {
    marginTop: 10,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  heroUpdate: {
    color: '#91ADA5',
    fontFamily: fonts.regular,
    fontSize: 8.5,
  },
  heroChange: {
    borderRadius: 8,
    backgroundColor: 'rgba(98,213,156,0.15)',
    paddingHorizontal: 7,
    paddingVertical: 3,
  },
  heroChangeText: {
    color: '#79DFAE',
    fontFamily: fonts.semibold,
    fontSize: 9,
  },
  heroButton: {
    position: 'absolute',
    right: 20,
    bottom: 18,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
    borderRadius: 13,
    backgroundColor: colors.goldLight,
    paddingHorizontal: 12,
    paddingVertical: 8,
  },
  heroButtonText: {
    color: colors.forest,
    fontFamily: fonts.bold,
    fontSize: 10,
  },
  sectionHeader: {
    marginTop: 28,
    marginBottom: 15,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  sectionTitle: {
    color: colors.ink,
    fontFamily: fonts.bold,
    fontSize: 17,
    textAlign: 'right',
  },
  sectionAction: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 1,
    paddingVertical: 4,
  },
  sectionActionText: {
    color: colors.gold,
    fontFamily: fonts.medium,
    fontSize: 10,
  },
  categoryRow: {
    flexDirection: 'row-reverse',
    justifyContent: 'space-between',
  },
  categoryItem: {
    width: '23%',
    alignItems: 'center',
    gap: 8,
  },
  categoryPressed: {
    opacity: 0.65,
  },
  categoryIcon: {
    width: 62,
    height: 62,
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: 20,
    borderWidth: 1,
    borderColor: colors.line,
    backgroundColor: colors.paper,
  },
  categorySymbol: {
    color: colors.gold,
    fontFamily: fonts.regular,
    fontSize: 27,
  },
  categoryLabel: {
    color: colors.ink,
    fontFamily: fonts.medium,
    fontSize: 10.5,
  },
  assuranceStrip: {
    marginTop: 25,
    flexDirection: 'row-reverse',
    alignItems: 'center',
    justifyContent: 'space-around',
    borderRadius: 18,
    borderWidth: 1,
    borderColor: colors.line,
    backgroundColor: colors.paper,
    paddingVertical: 13,
  },
  assuranceItem: {
    flexDirection: 'row-reverse',
    alignItems: 'center',
    gap: 5,
  },
  assuranceLabel: {
    color: colors.inkSoft,
    fontFamily: fonts.medium,
    fontSize: 8.5,
  },
  assuranceDivider: {
    width: 1,
    height: 22,
    backgroundColor: colors.line,
  },
  productGrid: {
    flexDirection: 'row-reverse',
    flexWrap: 'wrap',
    gap: 13,
  },
  giftBanner: {
    marginTop: 25,
    flexDirection: 'row',
    alignItems: 'center',
    borderRadius: 22,
    padding: 15,
  },
  giftIcon: {
    width: 48,
    height: 48,
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: 16,
    backgroundColor: 'rgba(255,255,255,0.65)',
  },
  giftCopy: {
    flex: 1,
    alignItems: 'flex-end',
    paddingHorizontal: 11,
  },
  giftTitle: {
    color: colors.forest,
    fontFamily: fonts.bold,
    fontSize: 12.5,
  },
  giftText: {
    marginTop: 3,
    color: colors.inkSoft,
    fontFamily: fonts.regular,
    fontSize: 9,
  },
  disclaimer: {
    marginTop: 20,
    color: '#949A96',
    fontFamily: fonts.regular,
    fontSize: 8.5,
    textAlign: 'center',
  },
  pageHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 22,
  },
  pageTitle: {
    color: colors.ink,
    fontFamily: fonts.bold,
    fontSize: 21,
    textAlign: 'right',
  },
  pageSubtitle: {
    marginTop: 2,
    color: colors.inkSoft,
    fontFamily: fonts.regular,
    fontSize: 10,
    textAlign: 'right',
  },
  searchBox: {
    height: 48,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 9,
    borderRadius: 16,
    borderWidth: 1,
    borderColor: colors.line,
    backgroundColor: colors.paper,
    paddingHorizontal: 14,
  },
  searchInput: {
    flex: 1,
    height: '100%',
    color: colors.ink,
    fontFamily: fonts.regular,
    fontSize: 11.5,
    textAlign: 'right',
    writingDirection: 'rtl',
    outlineStyle: 'none',
  },
  filterChips: {
    paddingVertical: 17,
    flexDirection: 'row-reverse',
    gap: 8,
  },
  filterChip: {
    borderRadius: 13,
    borderWidth: 1,
    borderColor: colors.line,
    backgroundColor: colors.paper,
    paddingHorizontal: 16,
    paddingVertical: 9,
  },
  filterChipSelected: {
    borderColor: colors.forest,
    backgroundColor: colors.forest,
  },
  filterChipText: {
    color: colors.inkSoft,
    fontFamily: fonts.medium,
    fontSize: 10.5,
  },
  filterChipTextSelected: {
    color: colors.white,
    fontFamily: fonts.semibold,
  },
  resultHeader: {
    marginTop: 3,
    marginBottom: 14,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  resultTitle: {
    color: colors.ink,
    fontFamily: fonts.bold,
    fontSize: 16,
  },
  resultCount: {
    color: colors.inkSoft,
    fontFamily: fonts.regular,
    fontSize: 10,
  },
  noResults: {
    marginTop: 40,
    alignItems: 'center',
    borderRadius: 22,
    borderWidth: 1,
    borderColor: colors.line,
    backgroundColor: colors.paper,
    paddingVertical: 40,
  },
  noResultsTitle: {
    marginTop: 12,
    color: colors.ink,
    fontFamily: fonts.bold,
    fontSize: 15,
  },
  noResultsText: {
    marginTop: 4,
    color: colors.inkSoft,
    fontFamily: fonts.regular,
    fontSize: 10,
  },
  itemCountPill: {
    borderRadius: 12,
    backgroundColor: colors.goldLight,
    paddingHorizontal: 11,
    paddingVertical: 7,
  },
  itemCountText: {
    color: colors.forest,
    fontFamily: fonts.semibold,
    fontSize: 10,
  },
  emptyCart: {
    marginTop: 36,
    alignItems: 'center',
    borderRadius: 28,
    borderWidth: 1,
    borderColor: colors.line,
    backgroundColor: colors.paper,
    paddingHorizontal: 28,
    paddingVertical: 42,
  },
  emptyCartIcon: {
    width: 88,
    height: 88,
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: 30,
    backgroundColor: colors.goldLight,
  },
  emptyCartTitle: {
    marginTop: 22,
    color: colors.ink,
    fontFamily: fonts.bold,
    fontSize: 18,
  },
  emptyCartText: {
    marginTop: 7,
    color: colors.inkSoft,
    fontFamily: fonts.regular,
    fontSize: 11,
    lineHeight: 20,
    textAlign: 'center',
    writingDirection: 'rtl',
  },
  primaryButton: {
    marginTop: 22,
    borderRadius: 14,
    backgroundColor: colors.forest,
    paddingHorizontal: 28,
    paddingVertical: 12,
  },
  primaryButtonText: {
    color: colors.white,
    fontFamily: fonts.semibold,
    fontSize: 11,
  },
  cartList: {
    gap: 11,
  },
  cartItem: {
    flexDirection: 'row-reverse',
    borderRadius: 20,
    borderWidth: 1,
    borderColor: colors.line,
    backgroundColor: colors.paper,
    padding: 10,
  },
  cartImage: {
    width: 104,
    height: 112,
    borderRadius: 15,
    backgroundColor: '#E8E1D6',
  },
  cartItemCopy: {
    flex: 1,
    alignItems: 'flex-end',
    paddingRight: 12,
    paddingVertical: 3,
  },
  cartItemName: {
    maxWidth: '100%',
    color: colors.ink,
    fontFamily: fonts.bold,
    fontSize: 13,
    textAlign: 'right',
  },
  cartItemMeta: {
    marginTop: 5,
    color: colors.inkSoft,
    fontFamily: fonts.regular,
    fontSize: 9.5,
    writingDirection: 'rtl',
  },
  cartItemPrice: {
    marginTop: 10,
    color: colors.forest,
    fontFamily: fonts.bold,
    fontSize: 11,
    writingDirection: 'rtl',
  },
  quantityRow: {
    marginTop: 'auto',
    flexDirection: 'row-reverse',
    alignItems: 'center',
    gap: 10,
  },
  quantityButton: {
    width: 28,
    height: 28,
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: 9,
    borderWidth: 1,
    borderColor: colors.line,
    backgroundColor: colors.cream,
  },
  quantityText: {
    minWidth: 16,
    color: colors.ink,
    fontFamily: fonts.bold,
    fontSize: 11,
    textAlign: 'center',
  },
  deliveryCard: {
    marginTop: 16,
    flexDirection: 'row-reverse',
    alignItems: 'center',
    borderRadius: 18,
    backgroundColor: colors.forestMuted,
    padding: 12,
  },
  deliveryIcon: {
    width: 40,
    height: 40,
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: 13,
    backgroundColor: 'rgba(255,255,255,0.72)',
  },
  deliveryCopy: {
    flex: 1,
    alignItems: 'flex-end',
    paddingHorizontal: 10,
  },
  deliveryTitle: {
    color: colors.forest,
    fontFamily: fonts.semibold,
    fontSize: 11,
  },
  deliveryText: {
    marginTop: 3,
    color: colors.inkSoft,
    fontFamily: fonts.regular,
    fontSize: 8.5,
  },
  summaryCard: {
    marginTop: 16,
    borderRadius: 22,
    borderWidth: 1,
    borderColor: colors.line,
    backgroundColor: colors.paper,
    padding: 17,
  },
  summaryTitle: {
    marginBottom: 13,
    color: colors.ink,
    fontFamily: fonts.bold,
    fontSize: 15,
    textAlign: 'right',
  },
  summaryRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingVertical: 6,
  },
  summaryLabel: {
    color: colors.inkSoft,
    fontFamily: fonts.regular,
    fontSize: 10.5,
    textAlign: 'right',
  },
  summaryValue: {
    color: colors.ink,
    fontFamily: fonts.medium,
    fontSize: 10.5,
    writingDirection: 'rtl',
  },
  summaryHighlight: {
    color: colors.forest,
    fontFamily: fonts.semibold,
  },
  summaryDivider: {
    height: 1,
    marginVertical: 9,
    backgroundColor: colors.line,
  },
  totalRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  totalLabel: {
    color: colors.ink,
    fontFamily: fonts.bold,
    fontSize: 11.5,
  },
  totalValue: {
    color: colors.forest,
    fontFamily: fonts.black,
    fontSize: 14,
    writingDirection: 'rtl',
  },
  checkoutButton: {
    minHeight: 52,
    marginTop: 16,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 8,
    borderRadius: 16,
    backgroundColor: colors.forest,
    paddingHorizontal: 18,
  },
  checkoutPressed: {
    backgroundColor: colors.forestDark,
    transform: [{ scale: 0.99 }],
  },
  checkoutButtonText: {
    color: colors.white,
    fontFamily: fonts.bold,
    fontSize: 12.5,
  },
  secureNote: {
    marginTop: 11,
    flexDirection: 'row-reverse',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 6,
  },
  secureNoteText: {
    color: colors.inkSoft,
    fontFamily: fonts.regular,
    fontSize: 8.5,
  },
  profileCard: {
    minHeight: 154,
    overflow: 'hidden',
    flexDirection: 'row-reverse',
    alignItems: 'center',
    borderRadius: 26,
    padding: 20,
  },
  profilePattern: {
    position: 'absolute',
    left: 24,
    top: 13,
    color: 'rgba(232,194,107,0.34)',
    fontSize: 78,
  },
  avatar: {
    width: 72,
    height: 72,
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: 25,
    borderWidth: 2,
    borderColor: colors.gold,
    backgroundColor: '#F0E4C6',
  },
  avatarText: {
    color: colors.forest,
    fontFamily: fonts.black,
    fontSize: 29,
  },
  profileCopy: {
    alignItems: 'flex-end',
    paddingRight: 14,
  },
  profileName: {
    color: colors.white,
    fontFamily: fonts.bold,
    fontSize: 18,
  },
  profilePhone: {
    marginTop: 4,
    color: '#C6D8D2',
    fontFamily: fonts.regular,
    fontSize: 10,
  },
  clubBadge: {
    marginTop: 11,
    flexDirection: 'row-reverse',
    alignItems: 'center',
    gap: 5,
    borderRadius: 10,
    backgroundColor: 'rgba(255,255,255,0.1)',
    paddingHorizontal: 9,
    paddingVertical: 5,
  },
  clubBadgeText: {
    color: '#F2DEAD',
    fontFamily: fonts.medium,
    fontSize: 9,
  },
  statsRow: {
    marginTop: 14,
    flexDirection: 'row-reverse',
    alignItems: 'center',
    justifyContent: 'space-around',
    borderRadius: 20,
    borderWidth: 1,
    borderColor: colors.line,
    backgroundColor: colors.paper,
    paddingVertical: 15,
  },
  statItem: {
    alignItems: 'center',
    minWidth: 70,
  },
  statValue: {
    color: colors.forest,
    fontFamily: fonts.bold,
    fontSize: 16,
  },
  statLabel: {
    marginTop: 3,
    color: colors.inkSoft,
    fontFamily: fonts.regular,
    fontSize: 9,
  },
  statsDivider: {
    width: 1,
    height: 30,
    backgroundColor: colors.line,
  },
  profileMenu: {
    marginTop: 16,
    overflow: 'hidden',
    borderRadius: 22,
    borderWidth: 1,
    borderColor: colors.line,
    backgroundColor: colors.paper,
  },
  profileMenuItem: {
    minHeight: 68,
    flexDirection: 'row',
    alignItems: 'center',
    borderBottomWidth: 1,
    borderBottomColor: colors.line,
    paddingHorizontal: 14,
  },
  profileMenuCopy: {
    flex: 1,
    alignItems: 'flex-end',
    paddingHorizontal: 11,
  },
  profileMenuTitle: {
    color: colors.ink,
    fontFamily: fonts.semibold,
    fontSize: 11.5,
  },
  profileMenuCaption: {
    marginTop: 3,
    color: colors.inkSoft,
    fontFamily: fonts.regular,
    fontSize: 8.5,
  },
  profileMenuIcon: {
    width: 40,
    height: 40,
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: 13,
    backgroundColor: colors.forestMuted,
  },
  logoutButton: {
    marginTop: 15,
    minHeight: 48,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 7,
    borderRadius: 16,
    borderWidth: 1,
    borderColor: '#ECD8D5',
    backgroundColor: '#FFF8F7',
  },
  logoutText: {
    color: colors.danger,
    fontFamily: fonts.semibold,
    fontSize: 10.5,
  },
  bottomNav: {
    position: 'absolute',
    right: 0,
    bottom: 0,
    left: 0,
    minHeight: 78,
    flexDirection: 'row-reverse',
    alignItems: 'center',
    justifyContent: 'space-around',
    borderTopWidth: 1,
    borderTopColor: colors.line,
    backgroundColor: 'rgba(255,254,251,0.98)',
    paddingBottom: Platform.OS === 'android' ? 8 : 6,
    paddingTop: 8,
    shadowColor: colors.black,
    shadowOffset: { width: 0, height: -5 },
    shadowOpacity: 0.06,
    shadowRadius: 15,
    elevation: 12,
  },
  navItem: {
    minWidth: 67,
    alignItems: 'center',
    gap: 3,
  },
  navIconWrap: {
    width: 40,
    height: 31,
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: 12,
  },
  navIconWrapActive: {
    backgroundColor: colors.forestMuted,
  },
  navLabel: {
    color: '#8A9490',
    fontFamily: fonts.regular,
    fontSize: 8.5,
  },
  navLabelActive: {
    color: colors.forest,
    fontFamily: fonts.semibold,
  },
  cartBadge: {
    position: 'absolute',
    top: -5,
    right: 0,
    minWidth: 16,
    height: 16,
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: 8,
    borderWidth: 1.5,
    borderColor: colors.paper,
    backgroundColor: colors.gold,
    paddingHorizontal: 3,
  },
  cartBadgeText: {
    color: colors.white,
    fontFamily: fonts.bold,
    fontSize: 7,
  },
  toast: {
    position: 'absolute',
    top: Platform.OS === 'android' ? 50 : 25,
    right: 18,
    left: 18,
    minHeight: 50,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 9,
    borderRadius: 16,
    backgroundColor: colors.ink,
    paddingHorizontal: 14,
    shadowColor: colors.black,
    shadowOffset: { width: 0, height: 5 },
    shadowOpacity: 0.2,
    shadowRadius: 12,
    elevation: 12,
  },
  toastIcon: {
    width: 24,
    height: 24,
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: 12,
    backgroundColor: colors.gold,
  },
  toastText: {
    color: colors.white,
    fontFamily: fonts.medium,
    fontSize: 10.5,
    writingDirection: 'rtl',
  },
  modalBackdrop: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'flex-end',
    backgroundColor: 'rgba(8,18,15,0.5)',
  },
  productSheet: {
    width: '100%',
    maxWidth: 460,
    maxHeight: '94%',
    overflow: 'hidden',
    borderTopLeftRadius: 30,
    borderTopRightRadius: 30,
    backgroundColor: colors.cream,
  },
  productHero: {
    height: 340,
    backgroundColor: '#E7DFD3',
  },
  productHeroImage: {
    width: '100%',
    height: '100%',
  },
  closeButton: {
    position: 'absolute',
    top: 17,
    right: 17,
    width: 40,
    height: 40,
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: 14,
    backgroundColor: 'rgba(255,255,255,0.92)',
  },
  productActions: {
    position: 'absolute',
    top: 17,
    left: 17,
    flexDirection: 'row',
    gap: 8,
  },
  floatingButton: {
    width: 40,
    height: 40,
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: 14,
    backgroundColor: 'rgba(255,255,255,0.92)',
  },
  productDetails: {
    padding: 19,
  },
  productDetailKicker: {
    flexDirection: 'row-reverse',
    alignItems: 'center',
    gap: 7,
  },
  productDetailKickerText: {
    color: colors.gold,
    fontFamily: fonts.semibold,
    fontSize: 9.5,
  },
  productDetailDot: {
    width: 3,
    height: 3,
    borderRadius: 2,
    backgroundColor: colors.gold,
  },
  productDetailName: {
    marginTop: 7,
    color: colors.ink,
    fontFamily: fonts.black,
    fontSize: 22,
    textAlign: 'right',
  },
  productDescription: {
    marginTop: 8,
    color: colors.inkSoft,
    fontFamily: fonts.regular,
    fontSize: 10.5,
    lineHeight: 20,
    textAlign: 'right',
    writingDirection: 'rtl',
  },
  specRow: {
    marginTop: 18,
    flexDirection: 'row-reverse',
    alignItems: 'center',
    justifyContent: 'space-around',
    borderRadius: 17,
    borderWidth: 1,
    borderColor: colors.line,
    backgroundColor: colors.paper,
    paddingVertical: 12,
  },
  spec: {
    minWidth: 70,
    alignItems: 'center',
  },
  specValue: {
    color: colors.ink,
    fontFamily: fonts.bold,
    fontSize: 11,
  },
  specLabel: {
    marginTop: 2,
    color: colors.inkSoft,
    fontFamily: fonts.regular,
    fontSize: 8,
  },
  specDivider: {
    width: 1,
    height: 26,
    backgroundColor: colors.line,
  },
  priceBreakdown: {
    marginTop: 13,
    borderRadius: 18,
    borderWidth: 1,
    borderColor: colors.line,
    backgroundColor: colors.paper,
    padding: 14,
  },
  priceBreakdownTitle: {
    marginBottom: 8,
    color: colors.ink,
    fontFamily: fonts.bold,
    fontSize: 12,
    textAlign: 'right',
  },
  productFooter: {
    borderTopWidth: 1,
    borderTopColor: colors.line,
    backgroundColor: colors.paper,
    paddingHorizontal: 18,
    paddingBottom: Platform.OS === 'android' ? 18 : 15,
  },
  invoiceViewport: {
    flex: 1,
    alignItems: 'center',
    backgroundColor: Platform.OS === 'web' ? '#E6E0D6' : colors.cream,
  },
  invoiceShell: {
    width: '100%',
    maxWidth: 460,
    flex: 1,
    backgroundColor: colors.cream,
  },
  invoiceHeader: {
    minHeight: 72,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    borderBottomWidth: 1,
    borderBottomColor: colors.line,
    backgroundColor: colors.paper,
    paddingTop: Platform.OS === 'android' ? 20 : 5,
    paddingHorizontal: 18,
  },
  invoiceClose: {
    width: 38,
    height: 38,
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: 13,
    backgroundColor: colors.cream,
  },
  invoiceHeaderTitle: {
    color: colors.ink,
    fontFamily: fonts.bold,
    fontSize: 16,
    textAlign: 'center',
  },
  invoiceHeaderCaption: {
    marginTop: 2,
    color: colors.inkSoft,
    fontFamily: fonts.regular,
    fontSize: 8,
    textAlign: 'center',
  },
  invoiceHeaderMark: {
    width: 38,
    height: 38,
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: 13,
    backgroundColor: colors.forest,
  },
  invoiceScrollContent: {
    padding: 18,
    paddingBottom: 40,
  },
  invoiceSuccess: {
    alignItems: 'center',
    marginBottom: 18,
  },
  invoiceSuccessIcon: {
    width: 52,
    height: 52,
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: 19,
    backgroundColor: colors.forest,
  },
  invoiceSuccessTitle: {
    marginTop: 10,
    color: colors.ink,
    fontFamily: fonts.bold,
    fontSize: 16,
  },
  invoiceSuccessText: {
    marginTop: 3,
    color: colors.inkSoft,
    fontFamily: fonts.regular,
    fontSize: 9,
    textAlign: 'center',
  },
  receipt: {
    borderRadius: 22,
    borderWidth: 1,
    borderColor: colors.line,
    backgroundColor: colors.paper,
    padding: 18,
    shadowColor: colors.black,
    shadowOffset: { width: 0, height: 7 },
    shadowOpacity: 0.06,
    shadowRadius: 14,
    elevation: 2,
  },
  receiptBrand: {
    flexDirection: 'row-reverse',
    alignItems: 'center',
    gap: 10,
  },
  receiptBrandMark: {
    width: 45,
    height: 45,
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: 15,
    backgroundColor: colors.forest,
  },
  receiptBrandName: {
    color: colors.ink,
    fontFamily: fonts.black,
    fontSize: 16,
    textAlign: 'right',
  },
  receiptBrandMeta: {
    marginTop: 2,
    color: colors.inkSoft,
    fontFamily: fonts.regular,
    fontSize: 8.5,
    textAlign: 'right',
  },
  receiptRule: {
    height: 1,
    marginVertical: 16,
    backgroundColor: colors.line,
  },
  receiptMetaGrid: {
    flexDirection: 'row-reverse',
    flexWrap: 'wrap',
    rowGap: 12,
  },
  receiptMeta: {
    width: '50%',
    alignItems: 'flex-end',
  },
  receiptMetaLabel: {
    color: colors.inkSoft,
    fontFamily: fonts.regular,
    fontSize: 8,
  },
  receiptMetaValue: {
    marginTop: 3,
    color: colors.ink,
    fontFamily: fonts.semibold,
    fontSize: 9.5,
    writingDirection: 'rtl',
  },
  receiptSectionTitle: {
    marginBottom: 8,
    color: colors.ink,
    fontFamily: fonts.bold,
    fontSize: 11.5,
    textAlign: 'right',
  },
  receiptLine: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingVertical: 8,
  },
  receiptLineCopy: {
    flex: 1,
    alignItems: 'flex-end',
    paddingLeft: 10,
  },
  receiptLineName: {
    color: colors.ink,
    fontFamily: fonts.semibold,
    fontSize: 9.5,
    textAlign: 'right',
    writingDirection: 'rtl',
  },
  receiptLineMeta: {
    marginTop: 2,
    color: colors.inkSoft,
    fontFamily: fonts.regular,
    fontSize: 8,
    writingDirection: 'rtl',
  },
  receiptLinePrice: {
    color: colors.ink,
    fontFamily: fonts.medium,
    fontSize: 8.5,
    writingDirection: 'rtl',
  },
  receiptTotal: {
    marginTop: 13,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    borderRadius: 14,
    backgroundColor: colors.forest,
    paddingHorizontal: 13,
    paddingVertical: 12,
  },
  receiptTotalLabel: {
    color: '#D8E7E2',
    fontFamily: fonts.semibold,
    fontSize: 9.5,
  },
  receiptTotalValue: {
    color: colors.white,
    fontFamily: fonts.black,
    fontSize: 12.5,
    writingDirection: 'rtl',
  },
  invoiceStamp: {
    marginTop: 15,
    flexDirection: 'row-reverse',
    alignItems: 'center',
    gap: 8,
    borderRadius: 14,
    borderWidth: 1,
    borderColor: '#BFD6CE',
    backgroundColor: colors.forestMuted,
    padding: 10,
  },
  invoiceStampTitle: {
    color: colors.forest,
    fontFamily: fonts.bold,
    fontSize: 9,
    textAlign: 'right',
  },
  invoiceStampText: {
    marginTop: 2,
    color: colors.inkSoft,
    fontFamily: fonts.regular,
    fontSize: 7.5,
    textAlign: 'right',
  },
  secondaryButton: {
    minHeight: 48,
    marginTop: 10,
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: 15,
    borderWidth: 1,
    borderColor: colors.line,
    backgroundColor: colors.paper,
  },
  secondaryButtonText: {
    color: colors.forest,
    fontFamily: fonts.semibold,
    fontSize: 11,
  },
});
