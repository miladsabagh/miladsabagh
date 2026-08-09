import {
  Vazirmatn_400Regular,
  Vazirmatn_500Medium,
  Vazirmatn_600SemiBold,
  Vazirmatn_700Bold,
  Vazirmatn_800ExtraBold,
  useFonts,
} from '@expo-google-fonts/vazirmatn';
import { Feather, Ionicons } from '@expo/vector-icons';
import { LinearGradient } from 'expo-linear-gradient';
import * as Print from 'expo-print';
import * as Sharing from 'expo-sharing';
import { StatusBar } from 'expo-status-bar';
import React, { useMemo, useState } from 'react';
import {
  ActivityIndicator,
  Alert,
  Image,
  ImageBackground,
  KeyboardAvoidingView,
  Modal,
  Platform,
  Pressable,
  SafeAreaView,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  useWindowDimensions,
  View,
} from 'react-native';

import {
  Product,
  categories,
  formatNumber,
  formatToman,
  products,
} from './src/catalog';
import {
  CartLine,
  InvoiceTotals,
  buildInvoiceHtml,
  calculateInvoiceTotals,
  createInvoiceNumber,
} from './src/invoice';

type Tab = 'home' | 'shop' | 'cart' | 'profile';
type CartState = Record<string, number>;

const colors = {
  wine: '#421421',
  wineLight: '#6D253A',
  wineSoft: '#F0E4E6',
  gold: '#BD9142',
  goldLight: '#E6C982',
  cream: '#F8F5EE',
  paper: '#FFFDF9',
  ink: '#291D1F',
  inkSoft: '#756A6C',
  line: '#E7DFD2',
  success: '#2F7A5B',
  white: '#FFFFFF',
};

const shadow = {
  shadowColor: '#27151A',
  shadowOffset: { width: 0, height: 7 },
  shadowOpacity: 0.08,
  shadowRadius: 16,
  elevation: 4,
};

const SectionTitle = ({
  title,
  action,
  onAction,
}: {
  title: string;
  action?: string;
  onAction?: () => void;
}) => (
  <View style={styles.sectionTitleRow}>
    {action ? (
      <Pressable hitSlop={8} onPress={onAction}>
        <Text style={styles.sectionAction}>{action}</Text>
      </Pressable>
    ) : (
      <View />
    )}
    <View style={styles.sectionHeadingWrap}>
      <Text style={styles.sectionHeading}>{title}</Text>
      <View style={styles.sectionDash} />
    </View>
  </View>
);

const Header = ({
  cartCount,
  onCart,
}: {
  cartCount: number;
  onCart: () => void;
}) => (
  <View style={styles.header}>
    <Pressable style={styles.headerAction} onPress={onCart}>
      <Feather name="shopping-bag" size={20} color={colors.ink} />
      {cartCount > 0 && (
        <View style={styles.headerBadge}>
          <Text style={styles.headerBadgeText}>{formatNumber(cartCount)}</Text>
        </View>
      )}
    </Pressable>

    <View style={styles.brand}>
      <View>
        <Text style={styles.brandName}>آتلیه زرین</Text>
        <Text style={styles.brandCaption}>طلا و جواهرات اصیل</Text>
      </View>
      <View style={styles.brandMark}>
        <Ionicons name="diamond-outline" size={23} color={colors.goldLight} />
      </View>
    </View>
  </View>
);

const ProductCard = ({
  product,
  favorite,
  onFavorite,
  onOpen,
  onAdd,
}: {
  product: Product;
  favorite: boolean;
  onFavorite: () => void;
  onOpen: () => void;
  onAdd: () => void;
}) => (
  <Pressable style={styles.productCard} onPress={onOpen}>
    <View style={styles.productImageWrap}>
      <Image source={product.image} style={styles.productImage} />
      <Pressable
        hitSlop={6}
        style={styles.favoriteButton}
        onPress={(event) => {
          event.stopPropagation();
          onFavorite();
        }}
      >
        <Ionicons
          name={favorite ? 'heart' : 'heart-outline'}
          size={17}
          color={favorite ? colors.wineLight : colors.inkSoft}
        />
      </Pressable>
      {product.badge && (
        <View style={styles.productBadge}>
          <Text style={styles.productBadgeText}>{product.badge}</Text>
        </View>
      )}
    </View>
    <View style={styles.productBody}>
      <Text numberOfLines={1} style={styles.productName}>
        {product.name}
      </Text>
      <View style={styles.productMetaRow}>
        <Text style={styles.productMeta}>{formatNumber(product.weight)} گرم</Text>
        <View style={styles.metaDot} />
        <Text style={styles.productMeta}>{product.purity}</Text>
      </View>
      <View style={styles.productFooter}>
        <Pressable
          hitSlop={5}
          style={styles.addButton}
          onPress={(event) => {
            event.stopPropagation();
            onAdd();
          }}
        >
          <Feather name="plus" size={17} color={colors.white} />
        </Pressable>
        <View>
          {product.previousPrice && (
            <Text style={styles.previousPrice}>
              {formatNumber(product.previousPrice)}
            </Text>
          )}
          <Text style={styles.productPrice}>
            {formatNumber(product.price)}
            <Text style={styles.currency}> ت</Text>
          </Text>
        </View>
      </View>
    </View>
  </Pressable>
);

const HomeScreen = ({
  cartCount,
  favorites,
  onCart,
  onShop,
  onOpenProduct,
  onFavorite,
  onAdd,
}: {
  cartCount: number;
  favorites: string[];
  onCart: () => void;
  onShop: () => void;
  onOpenProduct: (product: Product) => void;
  onFavorite: (id: string) => void;
  onAdd: (id: string) => void;
}) => (
  <ScrollView
    showsVerticalScrollIndicator={false}
    contentContainerStyle={styles.screenContent}
  >
    <Header cartCount={cartCount} onCart={onCart} />

    <View style={styles.marketCard}>
      <View style={styles.marketTrend}>
        <Feather name="trending-up" size={14} color={colors.success} />
        <Text style={styles.marketTrendText}>۰٫۸٪</Text>
      </View>
      <View style={styles.marketMain}>
        <View>
          <Text style={styles.marketPrice}>
            {formatNumber(7_486_000)}
            <Text style={styles.marketCurrency}> تومان</Text>
          </Text>
          <Text style={styles.marketUpdate}>به‌روزرسانی ۱ دقیقه پیش</Text>
        </View>
        <View style={styles.marketLabelWrap}>
          <Text style={styles.marketLabel}>طلای ۱۸ عیار</Text>
          <Text style={styles.marketUnit}>قیمت هر گرم</Text>
        </View>
      </View>
    </View>

    <ImageBackground
      source={require('./assets/hero-jewelry.jpg')}
      style={styles.hero}
      imageStyle={styles.heroImage}
    >
      <LinearGradient
        colors={['rgba(49,13,25,0.06)', 'rgba(49,13,25,0.94)']}
        start={{ x: 0.1, y: 0 }}
        end={{ x: 0.95, y: 0.8 }}
        style={styles.heroOverlay}
      >
        <View style={styles.heroContent}>
          <View style={styles.heroEyebrow}>
            <View style={styles.heroEyebrowLine} />
            <Text style={styles.heroEyebrowText}>کالکشن تازه</Text>
          </View>
          <Text style={styles.heroTitle}>درخشش،{'\n'}امضای شماست</Text>
          <Text style={styles.heroDescription}>
            جواهراتی که برای لحظه‌های ماندگار ساخته شده‌اند
          </Text>
          <Pressable style={styles.heroButton} onPress={onShop}>
            <Feather name="arrow-left" size={16} color={colors.wine} />
            <Text style={styles.heroButtonText}>مشاهده کالکشن</Text>
          </Pressable>
        </View>
      </LinearGradient>
    </ImageBackground>

    <SectionTitle title="دسته‌بندی‌ها" action="مشاهده همه" onAction={onShop} />
    <ScrollView
      horizontal
      showsHorizontalScrollIndicator={false}
      contentContainerStyle={styles.categoryList}
    >
      {categories.slice(1).map((category, index) => (
        <Pressable key={category.id} style={styles.categoryItem} onPress={onShop}>
          <LinearGradient
            colors={
              index === 0
                ? [colors.wine, colors.wineLight]
                : [colors.paper, '#F2ECE0']
            }
            style={styles.categoryIcon}
          >
            <Text
              style={[
                styles.categorySymbol,
                index === 0 && styles.categorySymbolActive,
              ]}
            >
              {category.icon}
            </Text>
          </LinearGradient>
          <Text style={styles.categoryTitle}>{category.title}</Text>
        </Pressable>
      ))}
    </ScrollView>

    <SectionTitle title="محبوب‌ترین‌ها" action="همه محصولات" onAction={onShop} />
    <View style={styles.productGrid}>
      {products.map((product) => (
        <ProductCard
          key={product.id}
          product={product}
          favorite={favorites.includes(product.id)}
          onFavorite={() => onFavorite(product.id)}
          onOpen={() => onOpenProduct(product)}
          onAdd={() => onAdd(product.id)}
        />
      ))}
    </View>

    <View style={styles.promiseCard}>
      <View style={styles.promiseItem}>
        <Feather name="refresh-cw" size={20} color={colors.gold} />
        <Text style={styles.promiseTitle}>۷ روز</Text>
        <Text style={styles.promiseCaption}>ضمانت بازگشت</Text>
      </View>
      <View style={styles.promiseDivider} />
      <View style={styles.promiseItem}>
        <Feather name="shield" size={20} color={colors.gold} />
        <Text style={styles.promiseTitle}>اصالت</Text>
        <Text style={styles.promiseCaption}>تضمین عیار طلا</Text>
      </View>
      <View style={styles.promiseDivider} />
      <View style={styles.promiseItem}>
        <Feather name="truck" size={20} color={colors.gold} />
        <Text style={styles.promiseTitle}>رایگان</Text>
        <Text style={styles.promiseCaption}>ارسال بیمه‌شده</Text>
      </View>
    </View>
  </ScrollView>
);

const ShopScreen = ({
  favorites,
  onOpenProduct,
  onFavorite,
  onAdd,
}: {
  favorites: string[];
  onOpenProduct: (product: Product) => void;
  onFavorite: (id: string) => void;
  onAdd: (id: string) => void;
}) => {
  const [query, setQuery] = useState('');
  const [activeCategory, setActiveCategory] = useState<string>('all');

  const visibleProducts = products.filter((product) => {
    const matchesCategory =
      activeCategory === 'all' || product.category === activeCategory;
    const matchesQuery =
      !query.trim() ||
      product.name.includes(query.trim()) ||
      product.category.includes(query.trim());
    return matchesCategory && matchesQuery;
  });

  return (
    <ScrollView
      showsVerticalScrollIndicator={false}
      keyboardShouldPersistTaps="handled"
      contentContainerStyle={styles.screenContent}
    >
      <View style={styles.pageHeader}>
        <View style={styles.pageHeaderIcon}>
          <Feather name="sliders" size={19} color={colors.ink} />
        </View>
        <View>
          <Text style={styles.pageTitle}>فروشگاه</Text>
          <Text style={styles.pageSubtitle}>انتخابی برای ماندن</Text>
        </View>
      </View>

      <View style={styles.searchBox}>
        <Feather name="search" size={19} color={colors.inkSoft} />
        <TextInput
          value={query}
          onChangeText={setQuery}
          placeholder="جست‌وجوی طلا و جواهر..."
          placeholderTextColor="#A69A9B"
          style={styles.searchInput}
          textAlign="right"
        />
      </View>

      <ScrollView
        horizontal
        showsHorizontalScrollIndicator={false}
        contentContainerStyle={styles.filterRow}
      >
        {categories.map((category) => {
          const selected = activeCategory === category.id;
          return (
            <Pressable
              key={category.id}
              onPress={() => setActiveCategory(category.id)}
              style={[styles.filterChip, selected && styles.filterChipActive]}
            >
              <Text
                style={[
                  styles.filterChipText,
                  selected && styles.filterChipTextActive,
                ]}
              >
                {category.title}
              </Text>
            </Pressable>
          );
        })}
      </ScrollView>

      <View style={styles.resultRow}>
        <Text style={styles.resultSort}>پیشنهاد زرین</Text>
        <Text style={styles.resultCount}>
          {formatNumber(visibleProducts.length)} محصول
        </Text>
      </View>

      {visibleProducts.length ? (
        <View style={styles.productGrid}>
          {visibleProducts.map((product) => (
            <ProductCard
              key={product.id}
              product={product}
              favorite={favorites.includes(product.id)}
              onFavorite={() => onFavorite(product.id)}
              onOpen={() => onOpenProduct(product)}
              onAdd={() => onAdd(product.id)}
            />
          ))}
        </View>
      ) : (
        <View style={styles.emptySearch}>
          <Feather name="search" size={26} color={colors.gold} />
          <Text style={styles.emptyTitle}>محصولی پیدا نشد</Text>
          <Text style={styles.emptyCaption}>عبارت دیگری را امتحان کنید</Text>
        </View>
      )}
    </ScrollView>
  );
};

const QuantityControl = ({
  quantity,
  onChange,
}: {
  quantity: number;
  onChange: (quantity: number) => void;
}) => (
  <View style={styles.quantityControl}>
    <Pressable style={styles.quantityButton} onPress={() => onChange(quantity + 1)}>
      <Feather name="plus" size={14} color={colors.wine} />
    </Pressable>
    <Text style={styles.quantityText}>{formatNumber(quantity)}</Text>
    <Pressable
      style={styles.quantityButton}
      onPress={() => onChange(quantity - 1)}
    >
      <Feather
        name={quantity === 1 ? 'trash-2' : 'minus'}
        size={14}
        color={quantity === 1 ? '#A14B5A' : colors.wine}
      />
    </Pressable>
  </View>
);

const CartScreen = ({
  lines,
  totals,
  customerName,
  customerPhone,
  onNameChange,
  onPhoneChange,
  onQuantityChange,
  onInvoice,
  onShop,
}: {
  lines: CartLine[];
  totals: InvoiceTotals;
  customerName: string;
  customerPhone: string;
  onNameChange: (value: string) => void;
  onPhoneChange: (value: string) => void;
  onQuantityChange: (id: string, quantity: number) => void;
  onInvoice: () => void;
  onShop: () => void;
}) => (
  <KeyboardAvoidingView
    style={styles.flex}
    behavior={Platform.OS === 'ios' ? 'padding' : undefined}
  >
    <ScrollView
      showsVerticalScrollIndicator={false}
      keyboardShouldPersistTaps="handled"
      contentContainerStyle={styles.screenContent}
    >
      <View style={styles.pageHeader}>
        <View style={[styles.pageHeaderIcon, styles.pageHeaderIconDark]}>
          <Feather name="shopping-bag" size={19} color={colors.white} />
        </View>
        <View>
          <Text style={styles.pageTitle}>سبد خرید</Text>
          <Text style={styles.pageSubtitle}>
            {formatNumber(lines.length)} انتخاب درخشان
          </Text>
        </View>
      </View>

      {lines.length ? (
        <>
          <View style={styles.cartList}>
            {lines.map(({ product, quantity }) => (
              <View key={product.id} style={styles.cartItem}>
                <Image source={product.image} style={styles.cartImage} />
                <View style={styles.cartDetails}>
                  <View>
                    <Text style={styles.cartProductName}>{product.name}</Text>
                    <Text style={styles.cartProductMeta}>
                      {product.purity} • {formatNumber(product.weight)} گرم
                    </Text>
                  </View>
                  <View style={styles.cartItemFooter}>
                    <QuantityControl
                      quantity={quantity}
                      onChange={(value) => onQuantityChange(product.id, value)}
                    />
                    <Text style={styles.cartPrice}>
                      {formatNumber(product.price * quantity)}
                      <Text style={styles.currency}> ت</Text>
                    </Text>
                  </View>
                </View>
              </View>
            ))}
          </View>

          <SectionTitle title="اطلاعات خریدار" />
          <View style={styles.formCard}>
            <Text style={styles.inputLabel}>نام و نام خانوادگی</Text>
            <View style={styles.inputWrap}>
              <Feather name="user" size={17} color={colors.gold} />
              <TextInput
                value={customerName}
                onChangeText={onNameChange}
                placeholder="مثلاً نگار احمدی"
                placeholderTextColor="#AAA0A1"
                style={styles.formInput}
                textAlign="right"
              />
            </View>
            <Text style={styles.inputLabel}>شماره تماس</Text>
            <View style={styles.inputWrap}>
              <Feather name="phone" size={17} color={colors.gold} />
              <TextInput
                value={customerPhone}
                onChangeText={onPhoneChange}
                placeholder="۰۹۱۲ ۱۲۳ ۴۵۶۷"
                placeholderTextColor="#AAA0A1"
                keyboardType="phone-pad"
                style={styles.formInput}
                textAlign="right"
              />
            </View>
          </View>

          <SectionTitle title="خلاصه سفارش" />
          <View style={styles.summaryCard}>
            <View style={styles.summaryRow}>
              <Text style={styles.summaryValue}>
                {formatToman(totals.subtotal)}
              </Text>
              <Text style={styles.summaryLabel}>جمع کالاها</Text>
            </View>
            <View style={styles.summaryRow}>
              <Text style={styles.summaryValue}>
                {formatToman(totals.makingFee)}
              </Text>
              <Text style={styles.summaryLabel}>اجرت ساخت (۷٪)</Text>
            </View>
            <View style={styles.summaryRow}>
              <Text style={styles.summaryValue}>{formatToman(totals.tax)}</Text>
              <Text style={styles.summaryLabel}>مالیات اجرت (۱۰٪)</Text>
            </View>
            <View style={styles.summaryDivider} />
            <View style={styles.summaryRow}>
              <Text style={styles.summaryTotalValue}>
                {formatToman(totals.total)}
              </Text>
              <Text style={styles.summaryTotalLabel}>مبلغ نهایی</Text>
            </View>
          </View>

          <Pressable style={styles.invoiceCta} onPress={onInvoice}>
            <Feather name="arrow-left" size={19} color={colors.wine} />
            <View style={styles.invoiceCtaTextWrap}>
              <Text style={styles.invoiceCtaTitle}>مشاهده و صدور فاکتور</Text>
              <Text style={styles.invoiceCtaCaption}>فاکتور رسمی قابل چاپ و اشتراک</Text>
            </View>
            <View style={styles.invoiceCtaIcon}>
              <Feather name="file-text" size={19} color={colors.goldLight} />
            </View>
          </Pressable>
        </>
      ) : (
        <View style={styles.emptyCart}>
          <LinearGradient
            colors={['#F0E4E6', '#F9F3E8']}
            style={styles.emptyCartIcon}
          >
            <Feather name="shopping-bag" size={33} color={colors.wine} />
          </LinearGradient>
          <Text style={styles.emptyTitle}>سبد شما هنوز خالی‌ست</Text>
          <Text style={styles.emptyCaption}>
            از میان کالکشن‌های زرین، انتخاب خود را پیدا کنید
          </Text>
          <Pressable style={styles.primaryButton} onPress={onShop}>
            <Text style={styles.primaryButtonText}>مشاهده محصولات</Text>
          </Pressable>
        </View>
      )}
    </ScrollView>
  </KeyboardAvoidingView>
);

const ProfileScreen = ({ invoiceCount }: { invoiceCount: number }) => (
  <ScrollView
    showsVerticalScrollIndicator={false}
    contentContainerStyle={styles.screenContent}
  >
    <View style={styles.pageHeader}>
      <View style={styles.pageHeaderIcon}>
        <Feather name="settings" size={19} color={colors.ink} />
      </View>
      <View>
        <Text style={styles.pageTitle}>حساب من</Text>
        <Text style={styles.pageSubtitle}>همراه زرین شما</Text>
      </View>
    </View>

    <LinearGradient
      colors={[colors.wine, '#6A2940']}
      start={{ x: 0, y: 0 }}
      end={{ x: 1, y: 1 }}
      style={styles.profileCard}
    >
      <View style={styles.profileAvatar}>
        <Text style={styles.profileAvatarText}>ن</Text>
      </View>
      <View style={styles.profileInfo}>
        <Text style={styles.profileName}>نگار احمدی</Text>
        <Text style={styles.profilePhone}>۰۹۱۲ ۱۲۳ ۴۵۶۷</Text>
        <View style={styles.profileLevel}>
          <Ionicons name="diamond" size={11} color={colors.goldLight} />
          <Text style={styles.profileLevelText}>عضو طلایی</Text>
        </View>
      </View>
      <Feather name="edit-2" size={17} color={colors.goldLight} />
    </LinearGradient>

    <View style={styles.profileStats}>
      <View style={styles.profileStat}>
        <Text style={styles.profileStatValue}>{formatNumber(2)}</Text>
        <Text style={styles.profileStatLabel}>سفارش‌ها</Text>
      </View>
      <View style={styles.profileStatDivider} />
      <View style={styles.profileStat}>
        <Text style={styles.profileStatValue}>{formatNumber(invoiceCount)}</Text>
        <Text style={styles.profileStatLabel}>فاکتورها</Text>
      </View>
      <View style={styles.profileStatDivider} />
      <View style={styles.profileStat}>
        <Text style={styles.profileStatValue}>{formatNumber(4)}</Text>
        <Text style={styles.profileStatLabel}>علاقه‌مندی‌ها</Text>
      </View>
    </View>

    <SectionTitle title="سفارش اخیر" action="مشاهده همه" />
    <View style={styles.orderCard}>
      <View style={styles.orderTopRow}>
        <View style={styles.orderStatus}>
          <Text style={styles.orderStatusText}>تحویل شده</Text>
        </View>
        <View>
          <Text style={styles.orderNumber}>سفارش ZR-۲۴۸۱</Text>
          <Text style={styles.orderDate}>۱۴ مرداد ۱۴۰۵</Text>
        </View>
      </View>
      <View style={styles.orderProductRow}>
        <View style={styles.orderThumbStack}>
          <Image source={products[0].image} style={styles.orderThumb} />
          <Image
            source={products[2].image}
            style={[styles.orderThumb, styles.orderThumbOverlap]}
          />
        </View>
        <Text style={styles.orderPrice}>{formatToman(108_170_000)}</Text>
      </View>
    </View>

    <SectionTitle title="خدمات زرین" />
    <View style={styles.menuCard}>
      {[
        ['file-text', 'فاکتورهای من', 'دریافت نسخه PDF'],
        ['shield', 'ضمانت و اصالت', 'استعلام و رهگیری محصول'],
        ['map-pin', 'نشانی‌های من', 'مدیریت نشانی تحویل'],
        ['headphones', 'پشتیبانی اختصاصی', 'هر روز از ۹ تا ۲۱'],
      ].map(([icon, title, caption], index) => (
        <View
          key={title}
          style={[styles.menuItem, index === 3 && styles.menuItemLast]}
        >
          <Feather name="chevron-left" size={17} color="#A3999A" />
          <View style={styles.menuItemMain}>
            <View>
              <Text style={styles.menuItemTitle}>{title}</Text>
              <Text style={styles.menuItemCaption}>{caption}</Text>
            </View>
            <View style={styles.menuIcon}>
              <Feather
                name={icon as keyof typeof Feather.glyphMap}
                size={18}
                color={colors.gold}
              />
            </View>
          </View>
        </View>
      ))}
    </View>
  </ScrollView>
);

const BottomNav = ({
  activeTab,
  cartCount,
  onChange,
}: {
  activeTab: Tab;
  cartCount: number;
  onChange: (tab: Tab) => void;
}) => {
  const tabs: Array<{
    id: Tab;
    label: string;
    icon: keyof typeof Feather.glyphMap;
  }> = [
    { id: 'home', label: 'خانه', icon: 'home' },
    { id: 'shop', label: 'فروشگاه', icon: 'grid' },
    { id: 'cart', label: 'سبد خرید', icon: 'shopping-bag' },
    { id: 'profile', label: 'حساب من', icon: 'user' },
  ];

  return (
    <View style={styles.bottomNav}>
      {tabs.map((tab) => {
        const active = tab.id === activeTab;
        return (
          <Pressable
            key={tab.id}
            style={styles.navItem}
            onPress={() => onChange(tab.id)}
          >
            <View style={[styles.navIconWrap, active && styles.navIconWrapActive]}>
              <Feather
                name={tab.icon}
                size={19}
                color={active ? colors.white : '#8C8183'}
              />
              {tab.id === 'cart' && cartCount > 0 && (
                <View style={styles.navBadge}>
                  <Text style={styles.navBadgeText}>{formatNumber(cartCount)}</Text>
                </View>
              )}
            </View>
            <Text style={[styles.navLabel, active && styles.navLabelActive]}>
              {tab.label}
            </Text>
          </Pressable>
        );
      })}
    </View>
  );
};

const ProductModal = ({
  product,
  visible,
  favorite,
  onClose,
  onFavorite,
  onAdd,
}: {
  product: Product | null;
  visible: boolean;
  favorite: boolean;
  onClose: () => void;
  onFavorite: () => void;
  onAdd: () => void;
}) => {
  if (!product) return null;

  return (
    <Modal
      visible={visible}
      animationType="slide"
      presentationStyle="pageSheet"
      onRequestClose={onClose}
    >
      <SafeAreaView style={styles.modalPage}>
        <ScrollView
          showsVerticalScrollIndicator={false}
          contentContainerStyle={styles.productModalContent}
        >
          <View style={styles.modalImageWrap}>
            <Image source={product.image} style={styles.modalImage} />
            <LinearGradient
              colors={['rgba(0,0,0,.3)', 'transparent']}
              style={styles.modalImageGradient}
            />
            <Pressable style={styles.modalClose} onPress={onClose}>
              <Feather name="x" size={21} color={colors.ink} />
            </Pressable>
            <Pressable style={styles.modalFavorite} onPress={onFavorite}>
              <Ionicons
                name={favorite ? 'heart' : 'heart-outline'}
                size={21}
                color={favorite ? colors.wineLight : colors.ink}
              />
            </Pressable>
          </View>
          <View style={styles.productModalBody}>
            {product.badge && (
              <Text style={styles.modalBadgeText}>{product.badge}</Text>
            )}
            <Text style={styles.modalProductName}>{product.name}</Text>
            <Text style={styles.modalProductDescription}>{product.subtitle}</Text>

            <View style={styles.specRow}>
              <View style={styles.specItem}>
                <Text style={styles.specValue}>{product.purity}</Text>
                <Text style={styles.specLabel}>عیار طلا</Text>
              </View>
              <View style={styles.specDivider} />
              <View style={styles.specItem}>
                <Text style={styles.specValue}>
                  {formatNumber(product.weight)} گرم
                </Text>
                <Text style={styles.specLabel}>وزن تقریبی</Text>
              </View>
              <View style={styles.specDivider} />
              <View style={styles.specItem}>
                <Text style={styles.specValue}>زرین</Text>
                <Text style={styles.specLabel}>گارانتی اصالت</Text>
              </View>
            </View>

            <View style={styles.craftCard}>
              <View style={styles.craftIcon}>
                <Ionicons name="diamond-outline" size={21} color={colors.gold} />
              </View>
              <View>
                <Text style={styles.craftTitle}>ساخته‌شده با ظرافت</Text>
                <Text style={styles.craftCaption}>
                  همراه شناسنامه اصالت و بسته‌بندی ویژه زرین
                </Text>
              </View>
            </View>
          </View>
        </ScrollView>
        <View style={styles.productModalFooter}>
          <Pressable style={styles.modalAddButton} onPress={onAdd}>
            <Feather name="shopping-bag" size={19} color={colors.white} />
            <Text style={styles.modalAddButtonText}>افزودن به سبد</Text>
          </Pressable>
          <View style={styles.modalPriceWrap}>
            <Text style={styles.modalPrice}>{formatNumber(product.price)}</Text>
            <Text style={styles.modalCurrency}>تومان</Text>
          </View>
        </View>
      </SafeAreaView>
    </Modal>
  );
};

const InvoiceModal = ({
  visible,
  lines,
  totals,
  customerName,
  customerPhone,
  invoiceNumber,
  onClose,
  onExport,
  exporting,
}: {
  visible: boolean;
  lines: CartLine[];
  totals: InvoiceTotals;
  customerName: string;
  customerPhone: string;
  invoiceNumber: string;
  onClose: () => void;
  onExport: () => void;
  exporting: boolean;
}) => (
  <Modal
    visible={visible}
    animationType="slide"
    presentationStyle="pageSheet"
    onRequestClose={onClose}
  >
    <SafeAreaView style={styles.invoiceModalPage}>
      <View style={styles.invoiceModalHeader}>
        <Pressable style={styles.invoiceCloseButton} onPress={onClose}>
          <Feather name="x" size={21} color={colors.ink} />
        </Pressable>
        <View>
          <Text style={styles.invoiceModalTitle}>پیش‌نمایش فاکتور</Text>
          <Text style={styles.invoiceModalSubtitle}>آماده صدور و اشتراک</Text>
        </View>
      </View>
      <ScrollView
        showsVerticalScrollIndicator={false}
        contentContainerStyle={styles.invoiceScroll}
      >
        <View style={styles.invoicePaper}>
          <View style={styles.invoiceBrandBlock}>
            <View>
              <Text style={styles.invoiceBrand}>آتلیه زرین</Text>
              <Text style={styles.invoiceBrandCaption}>طلا و جواهرات اصیل</Text>
            </View>
            <View style={styles.invoiceBrandMark}>
              <Ionicons name="diamond-outline" size={22} color={colors.goldLight} />
            </View>
          </View>
          <View style={styles.invoiceHeadingRow}>
            <View>
              <Text style={styles.invoiceInfoLabel}>شماره فاکتور</Text>
              <Text style={styles.invoiceInfoValue}>{invoiceNumber}</Text>
            </View>
            <View>
              <Text style={styles.invoiceInfoLabel}>تاریخ صدور</Text>
              <Text style={styles.invoiceInfoValue}>۱۸ مرداد ۱۴۰۵</Text>
            </View>
          </View>
          <View style={styles.invoiceCustomer}>
            <View>
              <Text style={styles.invoiceInfoLabel}>شماره تماس</Text>
              <Text style={styles.invoiceInfoValue}>{customerPhone || '—'}</Text>
            </View>
            <View>
              <Text style={styles.invoiceInfoLabel}>خریدار</Text>
              <Text style={styles.invoiceInfoValue}>
                {customerName || 'مشتری گرامی'}
              </Text>
            </View>
          </View>
          <View style={styles.invoiceLines}>
            {lines.map(({ product, quantity }) => (
              <View key={product.id} style={styles.invoiceLine}>
                <Text style={styles.invoiceLinePrice}>
                  {formatNumber(product.price * quantity)}
                </Text>
                <View style={styles.invoiceLineMain}>
                  <Text style={styles.invoiceLineName}>{product.name}</Text>
                  <Text style={styles.invoiceLineMeta}>
                    {formatNumber(quantity)} عدد • {product.purity} •{' '}
                    {formatNumber(product.weight)} گرم
                  </Text>
                </View>
              </View>
            ))}
          </View>
          <View style={styles.invoiceTotals}>
            <View style={styles.invoiceTotalRow}>
              <Text style={styles.invoiceTotalValue}>
                {formatNumber(totals.subtotal)}
              </Text>
              <Text style={styles.invoiceTotalLabel}>جمع کالاها</Text>
            </View>
            <View style={styles.invoiceTotalRow}>
              <Text style={styles.invoiceTotalValue}>
                {formatNumber(totals.makingFee)}
              </Text>
              <Text style={styles.invoiceTotalLabel}>اجرت ساخت</Text>
            </View>
            <View style={styles.invoiceTotalRow}>
              <Text style={styles.invoiceTotalValue}>
                {formatNumber(totals.tax)}
              </Text>
              <Text style={styles.invoiceTotalLabel}>مالیات اجرت</Text>
            </View>
            <View style={styles.invoiceFinalRow}>
              <Text style={styles.invoiceFinalValue}>
                {formatNumber(totals.total)} تومان
              </Text>
              <Text style={styles.invoiceFinalLabel}>مبلغ قابل پرداخت</Text>
            </View>
          </View>
          <View style={styles.invoiceSeal}>
            <Ionicons name="shield-checkmark" size={17} color={colors.success} />
            <Text style={styles.invoiceSealText}>
              اصالت و عیار تمام کالاها تضمین شده است
            </Text>
          </View>
        </View>
      </ScrollView>
      <View style={styles.invoiceFooter}>
        <Pressable
          disabled={exporting}
          style={[styles.exportButton, exporting && styles.buttonDisabled]}
          onPress={onExport}
        >
          {exporting ? (
            <ActivityIndicator size="small" color={colors.white} />
          ) : (
            <Feather name="download" size={19} color={colors.white} />
          )}
          <Text style={styles.exportButtonText}>
            {Platform.OS === 'web' ? 'چاپ یا ذخیره PDF' : 'دریافت و اشتراک PDF'}
          </Text>
        </Pressable>
      </View>
    </SafeAreaView>
  </Modal>
);

export default function App() {
  const [fontsLoaded] = useFonts({
    Vazirmatn_400Regular,
    Vazirmatn_500Medium,
    Vazirmatn_600SemiBold,
    Vazirmatn_700Bold,
    Vazirmatn_800ExtraBold,
  });
  const { width } = useWindowDimensions();
  const [activeTab, setActiveTab] = useState<Tab>('home');
  const [cart, setCart] = useState<CartState>({});
  const [favorites, setFavorites] = useState<string[]>(['luna-necklace']);
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null);
  const [invoiceVisible, setInvoiceVisible] = useState(false);
  const [invoiceNumber, setInvoiceNumber] = useState(createInvoiceNumber());
  const [customerName, setCustomerName] = useState('');
  const [customerPhone, setCustomerPhone] = useState('');
  const [exporting, setExporting] = useState(false);
  const [invoiceCount, setInvoiceCount] = useState(2);
  const [toast, setToast] = useState('');

  const lines = useMemo(
    () =>
      products
        .filter((product) => Boolean(cart[product.id]))
        .map((product) => ({ product, quantity: cart[product.id] })),
    [cart],
  );
  const totals = useMemo(() => calculateInvoiceTotals(lines), [lines]);
  const cartCount = lines.reduce((sum, line) => sum + line.quantity, 0);

  const showToast = (message: string) => {
    setToast(message);
    setTimeout(() => setToast(''), 2200);
  };

  const addToCart = (id: string) => {
    setCart((current) => ({ ...current, [id]: (current[id] || 0) + 1 }));
    showToast('به سبد خرید اضافه شد');
  };

  const toggleFavorite = (id: string) => {
    setFavorites((current) =>
      current.includes(id)
        ? current.filter((favoriteId) => favoriteId !== id)
        : [...current, id],
    );
  };

  const changeQuantity = (id: string, quantity: number) => {
    setCart((current) => {
      if (quantity <= 0) {
        const next = { ...current };
        delete next[id];
        return next;
      }
      return { ...current, [id]: quantity };
    });
  };

  const openInvoice = () => {
    setInvoiceNumber(createInvoiceNumber());
    setInvoiceVisible(true);
  };

  const exportInvoice = async () => {
    setExporting(true);
    const html = buildInvoiceHtml({
      lines,
      totals,
      invoiceNumber,
      customerName,
      customerPhone,
      dateLabel: '۱۸ مرداد ۱۴۰۵',
    });

    try {
      if (Platform.OS === 'web') {
        await Print.printAsync({ html });
      } else {
        const { uri } = await Print.printToFileAsync({ html });
        const canShare = await Sharing.isAvailableAsync();
        if (canShare) {
          await Sharing.shareAsync(uri, {
            mimeType: 'application/pdf',
            dialogTitle: 'اشتراک فاکتور زرین',
            UTI: '.pdf',
          });
        } else {
          await Print.printAsync({ uri });
        }
      }
      setInvoiceCount((count) => count + 1);
    } catch (error) {
      Alert.alert(
        'صدور فاکتور انجام نشد',
        'لطفاً دوباره تلاش کنید یا دسترسی چاپ دستگاه را بررسی کنید.',
      );
    } finally {
      setExporting(false);
    }
  };

  if (!fontsLoaded) {
    return (
      <View style={styles.loading}>
        <StatusBar style="dark" />
        <View style={styles.loadingMark}>
          <Ionicons name="diamond-outline" size={28} color={colors.goldLight} />
        </View>
        <ActivityIndicator size="small" color={colors.wine} />
      </View>
    );
  }

  const content = (() => {
    if (activeTab === 'shop') {
      return (
        <ShopScreen
          favorites={favorites}
          onOpenProduct={setSelectedProduct}
          onFavorite={toggleFavorite}
          onAdd={addToCart}
        />
      );
    }
    if (activeTab === 'cart') {
      return (
        <CartScreen
          lines={lines}
          totals={totals}
          customerName={customerName}
          customerPhone={customerPhone}
          onNameChange={setCustomerName}
          onPhoneChange={setCustomerPhone}
          onQuantityChange={changeQuantity}
          onInvoice={openInvoice}
          onShop={() => setActiveTab('shop')}
        />
      );
    }
    if (activeTab === 'profile') {
      return <ProfileScreen invoiceCount={invoiceCount} />;
    }
    return (
      <HomeScreen
        cartCount={cartCount}
        favorites={favorites}
        onCart={() => setActiveTab('cart')}
        onShop={() => setActiveTab('shop')}
        onOpenProduct={setSelectedProduct}
        onFavorite={toggleFavorite}
        onAdd={addToCart}
      />
    );
  })();

  return (
    <View style={styles.desktopBackdrop}>
      <SafeAreaView
        style={[styles.appShell, width > 520 && styles.appShellDesktop]}
      >
        <StatusBar style="dark" />
        <View style={styles.flex}>{content}</View>
        <BottomNav
          activeTab={activeTab}
          cartCount={cartCount}
          onChange={setActiveTab}
        />
        {toast ? (
          <View style={styles.toast}>
            <Feather name="check-circle" size={17} color={colors.white} />
            <Text style={styles.toastText}>{toast}</Text>
          </View>
        ) : null}
      </SafeAreaView>

      <ProductModal
        product={selectedProduct}
        visible={Boolean(selectedProduct)}
        favorite={
          selectedProduct ? favorites.includes(selectedProduct.id) : false
        }
        onClose={() => setSelectedProduct(null)}
        onFavorite={() =>
          selectedProduct && toggleFavorite(selectedProduct.id)
        }
        onAdd={() => {
          if (selectedProduct) {
            addToCart(selectedProduct.id);
            setSelectedProduct(null);
          }
        }}
      />

      <InvoiceModal
        visible={invoiceVisible}
        lines={lines}
        totals={totals}
        customerName={customerName}
        customerPhone={customerPhone}
        invoiceNumber={invoiceNumber}
        onClose={() => setInvoiceVisible(false)}
        onExport={exportInvoice}
        exporting={exporting}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  flex: { flex: 1 },
  desktopBackdrop: {
    flex: 1,
    backgroundColor: '#D8D0C2',
    alignItems: 'center',
  },
  appShell: {
    flex: 1,
    width: '100%',
    backgroundColor: colors.cream,
  },
  appShellDesktop: {
    maxWidth: 480,
    shadowColor: '#1E1115',
    shadowOffset: { width: 0, height: 0 },
    shadowOpacity: 0.25,
    shadowRadius: 25,
  },
  loading: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    gap: 20,
    backgroundColor: colors.cream,
  },
  loadingMark: {
    width: 58,
    height: 58,
    borderRadius: 20,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: colors.wine,
  },
  screenContent: {
    paddingHorizontal: 20,
    paddingBottom: 124,
  },
  header: {
    minHeight: 72,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingTop: 8,
    paddingBottom: 10,
  },
  headerAction: {
    width: 42,
    height: 42,
    borderRadius: 14,
    borderWidth: 1,
    borderColor: colors.line,
    backgroundColor: colors.paper,
    alignItems: 'center',
    justifyContent: 'center',
  },
  headerBadge: {
    position: 'absolute',
    top: -5,
    right: -5,
    minWidth: 18,
    height: 18,
    borderRadius: 9,
    paddingHorizontal: 4,
    backgroundColor: colors.wineLight,
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 2,
    borderColor: colors.cream,
  },
  headerBadgeText: {
    color: colors.white,
    fontFamily: 'Vazirmatn_700Bold',
    fontSize: 9,
  },
  brand: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
  },
  brandName: {
    color: colors.ink,
    fontFamily: 'Vazirmatn_800ExtraBold',
    fontSize: 17,
    textAlign: 'right',
  },
  brandCaption: {
    color: colors.inkSoft,
    fontFamily: 'Vazirmatn_400Regular',
    fontSize: 9,
    textAlign: 'right',
    marginTop: -2,
  },
  brandMark: {
    width: 43,
    height: 43,
    borderRadius: 15,
    backgroundColor: colors.wine,
    alignItems: 'center',
    justifyContent: 'center',
  },
  marketCard: {
    borderRadius: 16,
    borderWidth: 1,
    borderColor: colors.line,
    backgroundColor: colors.paper,
    padding: 15,
    marginBottom: 15,
  },
  marketTrend: {
    position: 'absolute',
    left: 14,
    top: 14,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 10,
    backgroundColor: '#E7F3ED',
  },
  marketTrendText: {
    color: colors.success,
    fontFamily: 'Vazirmatn_600SemiBold',
    fontSize: 10,
  },
  marketMain: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  marketLabelWrap: { alignItems: 'flex-end' },
  marketLabel: {
    color: colors.ink,
    fontFamily: 'Vazirmatn_700Bold',
    fontSize: 13,
  },
  marketUnit: {
    color: colors.inkSoft,
    fontFamily: 'Vazirmatn_400Regular',
    fontSize: 10,
    marginTop: 2,
  },
  marketPrice: {
    color: colors.wine,
    fontFamily: 'Vazirmatn_800ExtraBold',
    fontSize: 16,
  },
  marketCurrency: {
    fontFamily: 'Vazirmatn_500Medium',
    fontSize: 10,
  },
  marketUpdate: {
    color: '#9C9192',
    fontFamily: 'Vazirmatn_400Regular',
    fontSize: 8,
    marginTop: 2,
  },
  hero: {
    height: 255,
    borderRadius: 24,
    overflow: 'hidden',
    marginBottom: 26,
    ...shadow,
  },
  heroImage: { borderRadius: 24 },
  heroOverlay: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'flex-end',
    padding: 23,
  },
  heroContent: { width: '72%', alignItems: 'flex-end' },
  heroEyebrow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 7,
    marginBottom: 7,
  },
  heroEyebrowLine: {
    width: 24,
    height: 1,
    backgroundColor: colors.goldLight,
  },
  heroEyebrowText: {
    color: colors.goldLight,
    fontFamily: 'Vazirmatn_500Medium',
    fontSize: 10,
  },
  heroTitle: {
    color: colors.white,
    fontFamily: 'Vazirmatn_800ExtraBold',
    fontSize: 27,
    lineHeight: 38,
    textAlign: 'right',
  },
  heroDescription: {
    color: '#F3E9E6',
    fontFamily: 'Vazirmatn_400Regular',
    fontSize: 10,
    lineHeight: 18,
    textAlign: 'right',
    marginTop: 5,
    marginBottom: 15,
  },
  heroButton: {
    height: 38,
    borderRadius: 12,
    paddingHorizontal: 14,
    backgroundColor: colors.goldLight,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  heroButtonText: {
    color: colors.wine,
    fontFamily: 'Vazirmatn_700Bold',
    fontSize: 11,
  },
  sectionTitleRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 14,
    marginTop: 3,
  },
  sectionHeadingWrap: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  sectionHeading: {
    color: colors.ink,
    fontFamily: 'Vazirmatn_800ExtraBold',
    fontSize: 16,
  },
  sectionDash: {
    width: 4,
    height: 19,
    borderRadius: 3,
    backgroundColor: colors.gold,
  },
  sectionAction: {
    color: colors.wineLight,
    fontFamily: 'Vazirmatn_500Medium',
    fontSize: 10,
  },
  categoryList: {
    flexDirection: 'row-reverse',
    gap: 15,
    paddingBottom: 25,
  },
  categoryItem: { alignItems: 'center', gap: 7 },
  categoryIcon: {
    width: 62,
    height: 62,
    borderRadius: 22,
    borderWidth: 1,
    borderColor: '#E8DED0',
    alignItems: 'center',
    justifyContent: 'center',
  },
  categorySymbol: {
    color: colors.gold,
    fontSize: 24,
  },
  categorySymbolActive: { color: colors.goldLight },
  categoryTitle: {
    color: colors.ink,
    fontFamily: 'Vazirmatn_500Medium',
    fontSize: 10,
  },
  productGrid: {
    flexDirection: 'row-reverse',
    flexWrap: 'wrap',
    gap: 12,
    marginBottom: 24,
  },
  productCard: {
    width: '48.3%',
    borderRadius: 19,
    backgroundColor: colors.paper,
    overflow: 'hidden',
    borderWidth: 1,
    borderColor: '#EEE6DA',
    ...shadow,
  },
  productImageWrap: { height: 154, backgroundColor: '#EAE1D5' },
  productImage: { width: '100%', height: '100%', resizeMode: 'cover' },
  favoriteButton: {
    position: 'absolute',
    top: 9,
    right: 9,
    width: 31,
    height: 31,
    borderRadius: 11,
    backgroundColor: 'rgba(255,255,255,.9)',
    alignItems: 'center',
    justifyContent: 'center',
  },
  productBadge: {
    position: 'absolute',
    left: 8,
    bottom: 8,
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 8,
    backgroundColor: 'rgba(64,20,33,.92)',
  },
  productBadgeText: {
    color: colors.white,
    fontFamily: 'Vazirmatn_600SemiBold',
    fontSize: 8,
  },
  productBody: { padding: 11 },
  productName: {
    color: colors.ink,
    fontFamily: 'Vazirmatn_700Bold',
    fontSize: 12,
    textAlign: 'right',
  },
  productMetaRow: {
    flexDirection: 'row-reverse',
    alignItems: 'center',
    gap: 5,
    marginTop: 3,
  },
  productMeta: {
    color: colors.inkSoft,
    fontFamily: 'Vazirmatn_400Regular',
    fontSize: 8,
  },
  metaDot: {
    width: 3,
    height: 3,
    borderRadius: 2,
    backgroundColor: colors.gold,
  },
  productFooter: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-end',
    marginTop: 10,
  },
  addButton: {
    width: 30,
    height: 30,
    borderRadius: 10,
    backgroundColor: colors.wine,
    alignItems: 'center',
    justifyContent: 'center',
  },
  previousPrice: {
    color: '#AAA0A1',
    fontFamily: 'Vazirmatn_400Regular',
    fontSize: 8,
    textDecorationLine: 'line-through',
    textAlign: 'right',
  },
  productPrice: {
    color: colors.wine,
    fontFamily: 'Vazirmatn_800ExtraBold',
    fontSize: 12,
    textAlign: 'right',
  },
  currency: {
    color: colors.inkSoft,
    fontFamily: 'Vazirmatn_500Medium',
    fontSize: 8,
  },
  promiseCard: {
    minHeight: 99,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-around',
    borderRadius: 20,
    backgroundColor: colors.wine,
    marginBottom: 8,
    paddingHorizontal: 10,
  },
  promiseItem: {
    flex: 1,
    alignItems: 'center',
    gap: 2,
  },
  promiseTitle: {
    color: colors.white,
    fontFamily: 'Vazirmatn_700Bold',
    fontSize: 10,
    marginTop: 2,
  },
  promiseCaption: {
    color: '#D8C9CB',
    fontFamily: 'Vazirmatn_400Regular',
    fontSize: 7,
  },
  promiseDivider: {
    width: 1,
    height: 42,
    backgroundColor: 'rgba(255,255,255,.14)',
  },
  pageHeader: {
    minHeight: 78,
    paddingTop: 10,
    paddingBottom: 12,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  pageHeaderIcon: {
    width: 42,
    height: 42,
    borderRadius: 14,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: colors.paper,
    borderWidth: 1,
    borderColor: colors.line,
  },
  pageHeaderIconDark: {
    backgroundColor: colors.wine,
    borderColor: colors.wine,
  },
  pageTitle: {
    color: colors.ink,
    fontFamily: 'Vazirmatn_800ExtraBold',
    fontSize: 22,
    textAlign: 'right',
  },
  pageSubtitle: {
    color: colors.inkSoft,
    fontFamily: 'Vazirmatn_400Regular',
    fontSize: 10,
    textAlign: 'right',
  },
  searchBox: {
    height: 51,
    borderRadius: 16,
    borderWidth: 1,
    borderColor: colors.line,
    backgroundColor: colors.paper,
    paddingHorizontal: 15,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 9,
  },
  searchInput: {
    flex: 1,
    height: '100%',
    color: colors.ink,
    fontFamily: 'Vazirmatn_400Regular',
    fontSize: 11,
    outlineStyle: 'none',
  } as never,
  filterRow: {
    flexDirection: 'row-reverse',
    gap: 8,
    paddingVertical: 16,
  },
  filterChip: {
    borderRadius: 12,
    borderWidth: 1,
    borderColor: colors.line,
    backgroundColor: colors.paper,
    paddingVertical: 8,
    paddingHorizontal: 13,
  },
  filterChipActive: {
    borderColor: colors.wine,
    backgroundColor: colors.wine,
  },
  filterChipText: {
    color: colors.inkSoft,
    fontFamily: 'Vazirmatn_500Medium',
    fontSize: 10,
  },
  filterChipTextActive: { color: colors.white },
  resultRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 14,
  },
  resultSort: {
    color: colors.gold,
    fontFamily: 'Vazirmatn_600SemiBold',
    fontSize: 10,
  },
  resultCount: {
    color: colors.ink,
    fontFamily: 'Vazirmatn_700Bold',
    fontSize: 11,
  },
  emptySearch: {
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 70,
  },
  emptyTitle: {
    color: colors.ink,
    fontFamily: 'Vazirmatn_700Bold',
    fontSize: 16,
    marginTop: 13,
  },
  emptyCaption: {
    color: colors.inkSoft,
    fontFamily: 'Vazirmatn_400Regular',
    fontSize: 11,
    lineHeight: 20,
    textAlign: 'center',
    maxWidth: 270,
    marginTop: 4,
  },
  cartList: { gap: 11, marginBottom: 24 },
  cartItem: {
    minHeight: 118,
    borderRadius: 18,
    backgroundColor: colors.paper,
    borderWidth: 1,
    borderColor: colors.line,
    padding: 10,
    flexDirection: 'row-reverse',
    gap: 12,
  },
  cartImage: {
    width: 93,
    minHeight: 96,
    borderRadius: 13,
    resizeMode: 'cover',
  },
  cartDetails: {
    flex: 1,
    justifyContent: 'space-between',
    paddingVertical: 3,
  },
  cartProductName: {
    color: colors.ink,
    fontFamily: 'Vazirmatn_700Bold',
    fontSize: 13,
    textAlign: 'right',
  },
  cartProductMeta: {
    color: colors.inkSoft,
    fontFamily: 'Vazirmatn_400Regular',
    fontSize: 9,
    textAlign: 'right',
    marginTop: 3,
  },
  cartItemFooter: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  cartPrice: {
    color: colors.wine,
    fontFamily: 'Vazirmatn_800ExtraBold',
    fontSize: 12,
  },
  quantityControl: {
    height: 30,
    flexDirection: 'row',
    alignItems: 'center',
    borderRadius: 10,
    backgroundColor: colors.cream,
    borderWidth: 1,
    borderColor: colors.line,
  },
  quantityButton: {
    width: 29,
    height: 28,
    alignItems: 'center',
    justifyContent: 'center',
  },
  quantityText: {
    minWidth: 22,
    textAlign: 'center',
    color: colors.ink,
    fontFamily: 'Vazirmatn_700Bold',
    fontSize: 10,
  },
  formCard: {
    borderRadius: 18,
    backgroundColor: colors.paper,
    borderWidth: 1,
    borderColor: colors.line,
    padding: 15,
    marginBottom: 23,
  },
  inputLabel: {
    color: colors.inkSoft,
    fontFamily: 'Vazirmatn_500Medium',
    fontSize: 9,
    textAlign: 'right',
    marginBottom: 6,
    marginTop: 4,
  },
  inputWrap: {
    height: 47,
    borderRadius: 13,
    borderWidth: 1,
    borderColor: colors.line,
    backgroundColor: '#FCFAF5',
    flexDirection: 'row',
    alignItems: 'center',
    gap: 9,
    paddingHorizontal: 12,
    marginBottom: 8,
  },
  formInput: {
    flex: 1,
    height: '100%',
    color: colors.ink,
    fontFamily: 'Vazirmatn_400Regular',
    fontSize: 11,
    outlineStyle: 'none',
  } as never,
  summaryCard: {
    borderRadius: 18,
    backgroundColor: colors.paper,
    borderWidth: 1,
    borderColor: colors.line,
    padding: 17,
    gap: 10,
    marginBottom: 14,
  },
  summaryRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  summaryLabel: {
    color: colors.inkSoft,
    fontFamily: 'Vazirmatn_400Regular',
    fontSize: 10,
  },
  summaryValue: {
    color: colors.ink,
    fontFamily: 'Vazirmatn_600SemiBold',
    fontSize: 10,
  },
  summaryDivider: { height: 1, backgroundColor: colors.line, marginVertical: 2 },
  summaryTotalLabel: {
    color: colors.ink,
    fontFamily: 'Vazirmatn_700Bold',
    fontSize: 12,
  },
  summaryTotalValue: {
    color: colors.wine,
    fontFamily: 'Vazirmatn_800ExtraBold',
    fontSize: 14,
  },
  invoiceCta: {
    minHeight: 69,
    borderRadius: 18,
    paddingHorizontal: 14,
    backgroundColor: colors.goldLight,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 8,
  },
  invoiceCtaTextWrap: { flex: 1, alignItems: 'flex-end', marginHorizontal: 12 },
  invoiceCtaTitle: {
    color: colors.wine,
    fontFamily: 'Vazirmatn_800ExtraBold',
    fontSize: 13,
  },
  invoiceCtaCaption: {
    color: '#765B2B',
    fontFamily: 'Vazirmatn_400Regular',
    fontSize: 8,
    marginTop: 2,
  },
  invoiceCtaIcon: {
    width: 39,
    height: 39,
    borderRadius: 13,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: colors.wine,
  },
  emptyCart: {
    alignItems: 'center',
    paddingTop: 80,
  },
  emptyCartIcon: {
    width: 80,
    height: 80,
    borderRadius: 28,
    alignItems: 'center',
    justifyContent: 'center',
  },
  primaryButton: {
    height: 45,
    borderRadius: 14,
    backgroundColor: colors.wine,
    paddingHorizontal: 24,
    alignItems: 'center',
    justifyContent: 'center',
    marginTop: 20,
  },
  primaryButtonText: {
    color: colors.white,
    fontFamily: 'Vazirmatn_700Bold',
    fontSize: 11,
  },
  profileCard: {
    minHeight: 124,
    borderRadius: 22,
    padding: 19,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 14,
    ...shadow,
  },
  profileAvatar: {
    width: 62,
    height: 62,
    borderRadius: 23,
    backgroundColor: colors.goldLight,
    alignItems: 'center',
    justifyContent: 'center',
  },
  profileAvatarText: {
    color: colors.wine,
    fontFamily: 'Vazirmatn_800ExtraBold',
    fontSize: 24,
  },
  profileInfo: { flex: 1, alignItems: 'flex-end' },
  profileName: {
    color: colors.white,
    fontFamily: 'Vazirmatn_800ExtraBold',
    fontSize: 16,
  },
  profilePhone: {
    color: '#DCCFD1',
    fontFamily: 'Vazirmatn_400Regular',
    fontSize: 9,
    marginTop: 1,
  },
  profileLevel: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 8,
    backgroundColor: 'rgba(255,255,255,.1)',
    marginTop: 7,
  },
  profileLevelText: {
    color: colors.goldLight,
    fontFamily: 'Vazirmatn_600SemiBold',
    fontSize: 8,
  },
  profileStats: {
    minHeight: 82,
    borderRadius: 18,
    backgroundColor: colors.paper,
    borderWidth: 1,
    borderColor: colors.line,
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: 14,
    marginBottom: 25,
  },
  profileStat: { flex: 1, alignItems: 'center' },
  profileStatValue: {
    color: colors.wine,
    fontFamily: 'Vazirmatn_800ExtraBold',
    fontSize: 15,
  },
  profileStatLabel: {
    color: colors.inkSoft,
    fontFamily: 'Vazirmatn_400Regular',
    fontSize: 9,
  },
  profileStatDivider: { width: 1, height: 31, backgroundColor: colors.line },
  orderCard: {
    borderRadius: 18,
    backgroundColor: colors.paper,
    borderWidth: 1,
    borderColor: colors.line,
    padding: 15,
    marginBottom: 24,
  },
  orderTopRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingBottom: 12,
    borderBottomWidth: 1,
    borderBottomColor: colors.line,
  },
  orderStatus: {
    paddingHorizontal: 9,
    paddingVertical: 5,
    borderRadius: 9,
    backgroundColor: '#E7F3ED',
  },
  orderStatusText: {
    color: colors.success,
    fontFamily: 'Vazirmatn_600SemiBold',
    fontSize: 8,
  },
  orderNumber: {
    color: colors.ink,
    fontFamily: 'Vazirmatn_700Bold',
    fontSize: 11,
    textAlign: 'right',
  },
  orderDate: {
    color: colors.inkSoft,
    fontFamily: 'Vazirmatn_400Regular',
    fontSize: 8,
    textAlign: 'right',
    marginTop: 2,
  },
  orderProductRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingTop: 12,
  },
  orderThumbStack: { flexDirection: 'row' },
  orderThumb: {
    width: 42,
    height: 42,
    borderRadius: 13,
    borderWidth: 2,
    borderColor: colors.paper,
  },
  orderThumbOverlap: { marginLeft: -10 },
  orderPrice: {
    color: colors.wine,
    fontFamily: 'Vazirmatn_800ExtraBold',
    fontSize: 11,
  },
  menuCard: {
    borderRadius: 18,
    backgroundColor: colors.paper,
    borderWidth: 1,
    borderColor: colors.line,
    paddingHorizontal: 15,
  },
  menuItem: {
    minHeight: 69,
    borderBottomWidth: 1,
    borderBottomColor: colors.line,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  menuItemLast: { borderBottomWidth: 0 },
  menuItemMain: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
  },
  menuItemTitle: {
    color: colors.ink,
    fontFamily: 'Vazirmatn_600SemiBold',
    fontSize: 11,
    textAlign: 'right',
  },
  menuItemCaption: {
    color: colors.inkSoft,
    fontFamily: 'Vazirmatn_400Regular',
    fontSize: 8,
    textAlign: 'right',
    marginTop: 2,
  },
  menuIcon: {
    width: 38,
    height: 38,
    borderRadius: 13,
    backgroundColor: '#F7F0E4',
    alignItems: 'center',
    justifyContent: 'center',
  },
  bottomNav: {
    position: 'absolute',
    bottom: 0,
    left: 0,
    right: 0,
    height: Platform.OS === 'ios' ? 88 : 76,
    paddingBottom: Platform.OS === 'ios' ? 17 : 6,
    paddingHorizontal: 10,
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: 'rgba(255,253,249,.98)',
    borderTopWidth: 1,
    borderTopColor: colors.line,
    shadowColor: '#29171D',
    shadowOffset: { width: 0, height: -7 },
    shadowOpacity: 0.07,
    shadowRadius: 16,
    elevation: 12,
  },
  navItem: {
    flex: 1,
    alignItems: 'center',
    gap: 3,
  },
  navIconWrap: {
    width: 39,
    height: 31,
    borderRadius: 12,
    alignItems: 'center',
    justifyContent: 'center',
  },
  navIconWrapActive: { backgroundColor: colors.wine },
  navLabel: {
    color: '#8C8183',
    fontFamily: 'Vazirmatn_500Medium',
    fontSize: 8,
  },
  navLabelActive: {
    color: colors.wine,
    fontFamily: 'Vazirmatn_700Bold',
  },
  navBadge: {
    position: 'absolute',
    top: -5,
    right: -6,
    minWidth: 16,
    height: 16,
    borderRadius: 8,
    paddingHorizontal: 3,
    backgroundColor: colors.gold,
    borderWidth: 2,
    borderColor: colors.paper,
    alignItems: 'center',
    justifyContent: 'center',
  },
  navBadgeText: {
    color: colors.white,
    fontFamily: 'Vazirmatn_700Bold',
    fontSize: 7,
  },
  toast: {
    position: 'absolute',
    bottom: Platform.OS === 'ios' ? 99 : 86,
    alignSelf: 'center',
    borderRadius: 13,
    backgroundColor: '#243D34',
    paddingHorizontal: 15,
    paddingVertical: 10,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
    ...shadow,
  },
  toastText: {
    color: colors.white,
    fontFamily: 'Vazirmatn_600SemiBold',
    fontSize: 10,
  },
  modalPage: { flex: 1, backgroundColor: colors.cream },
  productModalContent: { paddingBottom: 108 },
  modalImageWrap: { height: 390, backgroundColor: '#E9E0D4' },
  modalImage: { width: '100%', height: '100%', resizeMode: 'cover' },
  modalImageGradient: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    height: 110,
  },
  modalClose: {
    position: 'absolute',
    top: 18,
    left: 18,
    width: 40,
    height: 40,
    borderRadius: 14,
    backgroundColor: 'rgba(255,255,255,.93)',
    alignItems: 'center',
    justifyContent: 'center',
  },
  modalFavorite: {
    position: 'absolute',
    top: 18,
    right: 18,
    width: 40,
    height: 40,
    borderRadius: 14,
    backgroundColor: 'rgba(255,255,255,.93)',
    alignItems: 'center',
    justifyContent: 'center',
  },
  productModalBody: {
    padding: 21,
    alignItems: 'flex-end',
  },
  modalBadgeText: {
    color: colors.gold,
    fontFamily: 'Vazirmatn_700Bold',
    fontSize: 10,
    marginBottom: 5,
  },
  modalProductName: {
    color: colors.ink,
    fontFamily: 'Vazirmatn_800ExtraBold',
    fontSize: 24,
  },
  modalProductDescription: {
    color: colors.inkSoft,
    fontFamily: 'Vazirmatn_400Regular',
    fontSize: 11,
    marginTop: 3,
  },
  specRow: {
    width: '100%',
    minHeight: 78,
    flexDirection: 'row',
    alignItems: 'center',
    borderRadius: 17,
    backgroundColor: colors.paper,
    borderWidth: 1,
    borderColor: colors.line,
    marginTop: 21,
  },
  specItem: { flex: 1, alignItems: 'center' },
  specValue: {
    color: colors.wine,
    fontFamily: 'Vazirmatn_700Bold',
    fontSize: 11,
  },
  specLabel: {
    color: colors.inkSoft,
    fontFamily: 'Vazirmatn_400Regular',
    fontSize: 7,
    marginTop: 2,
  },
  specDivider: { width: 1, height: 32, backgroundColor: colors.line },
  craftCard: {
    width: '100%',
    minHeight: 70,
    flexDirection: 'row-reverse',
    alignItems: 'center',
    gap: 12,
    borderRadius: 17,
    backgroundColor: '#F2E9DA',
    padding: 13,
    marginTop: 12,
  },
  craftIcon: {
    width: 42,
    height: 42,
    borderRadius: 14,
    backgroundColor: colors.wine,
    alignItems: 'center',
    justifyContent: 'center',
  },
  craftTitle: {
    color: colors.ink,
    fontFamily: 'Vazirmatn_700Bold',
    fontSize: 11,
    textAlign: 'right',
  },
  craftCaption: {
    color: colors.inkSoft,
    fontFamily: 'Vazirmatn_400Regular',
    fontSize: 8,
    textAlign: 'right',
    marginTop: 2,
  },
  productModalFooter: {
    position: 'absolute',
    bottom: 0,
    left: 0,
    right: 0,
    minHeight: 88,
    paddingHorizontal: 20,
    paddingVertical: 14,
    backgroundColor: colors.paper,
    borderTopWidth: 1,
    borderTopColor: colors.line,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 17,
  },
  modalAddButton: {
    flex: 1,
    height: 53,
    borderRadius: 16,
    backgroundColor: colors.wine,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 9,
  },
  modalAddButtonText: {
    color: colors.white,
    fontFamily: 'Vazirmatn_700Bold',
    fontSize: 12,
  },
  modalPriceWrap: { alignItems: 'flex-end' },
  modalPrice: {
    color: colors.wine,
    fontFamily: 'Vazirmatn_800ExtraBold',
    fontSize: 17,
  },
  modalCurrency: {
    color: colors.inkSoft,
    fontFamily: 'Vazirmatn_400Regular',
    fontSize: 9,
  },
  invoiceModalPage: { flex: 1, backgroundColor: '#EDE8DF' },
  invoiceModalHeader: {
    minHeight: 76,
    paddingHorizontal: 20,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  invoiceCloseButton: {
    width: 40,
    height: 40,
    borderRadius: 14,
    backgroundColor: colors.paper,
    alignItems: 'center',
    justifyContent: 'center',
  },
  invoiceModalTitle: {
    color: colors.ink,
    fontFamily: 'Vazirmatn_800ExtraBold',
    fontSize: 17,
    textAlign: 'right',
  },
  invoiceModalSubtitle: {
    color: colors.inkSoft,
    fontFamily: 'Vazirmatn_400Regular',
    fontSize: 9,
    textAlign: 'right',
  },
  invoiceScroll: { paddingHorizontal: 17, paddingBottom: 110 },
  invoicePaper: {
    backgroundColor: colors.paper,
    borderRadius: 5,
    overflow: 'hidden',
    ...shadow,
  },
  invoiceBrandBlock: {
    minHeight: 90,
    padding: 19,
    backgroundColor: colors.wine,
    flexDirection: 'row-reverse',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  invoiceBrand: {
    color: colors.white,
    fontFamily: 'Vazirmatn_800ExtraBold',
    fontSize: 20,
    textAlign: 'right',
  },
  invoiceBrandCaption: {
    color: '#D8C9CB',
    fontFamily: 'Vazirmatn_400Regular',
    fontSize: 8,
    textAlign: 'right',
  },
  invoiceBrandMark: {
    width: 43,
    height: 43,
    borderRadius: 15,
    backgroundColor: 'rgba(255,255,255,.1)',
    alignItems: 'center',
    justifyContent: 'center',
  },
  invoiceHeadingRow: {
    padding: 18,
    flexDirection: 'row',
    justifyContent: 'space-between',
    borderBottomWidth: 1,
    borderBottomColor: colors.line,
  },
  invoiceInfoLabel: {
    color: colors.inkSoft,
    fontFamily: 'Vazirmatn_400Regular',
    fontSize: 8,
    textAlign: 'right',
  },
  invoiceInfoValue: {
    color: colors.ink,
    fontFamily: 'Vazirmatn_700Bold',
    fontSize: 10,
    textAlign: 'right',
    marginTop: 3,
  },
  invoiceCustomer: {
    margin: 15,
    padding: 13,
    borderRadius: 11,
    backgroundColor: colors.cream,
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  invoiceLines: { paddingHorizontal: 15 },
  invoiceLine: {
    minHeight: 61,
    borderBottomWidth: 1,
    borderBottomColor: colors.line,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  invoiceLineMain: { alignItems: 'flex-end' },
  invoiceLineName: {
    color: colors.ink,
    fontFamily: 'Vazirmatn_700Bold',
    fontSize: 10,
  },
  invoiceLineMeta: {
    color: colors.inkSoft,
    fontFamily: 'Vazirmatn_400Regular',
    fontSize: 7,
    marginTop: 2,
  },
  invoiceLinePrice: {
    color: colors.wine,
    fontFamily: 'Vazirmatn_700Bold',
    fontSize: 9,
  },
  invoiceTotals: { padding: 15, gap: 8 },
  invoiceTotalRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  invoiceTotalLabel: {
    color: colors.inkSoft,
    fontFamily: 'Vazirmatn_400Regular',
    fontSize: 8,
  },
  invoiceTotalValue: {
    color: colors.ink,
    fontFamily: 'Vazirmatn_600SemiBold',
    fontSize: 8,
  },
  invoiceFinalRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    borderTopWidth: 1,
    borderTopColor: colors.goldLight,
    paddingTop: 12,
    marginTop: 3,
  },
  invoiceFinalLabel: {
    color: colors.ink,
    fontFamily: 'Vazirmatn_700Bold',
    fontSize: 10,
  },
  invoiceFinalValue: {
    color: colors.wine,
    fontFamily: 'Vazirmatn_800ExtraBold',
    fontSize: 13,
  },
  invoiceSeal: {
    minHeight: 47,
    backgroundColor: '#EDF4F0',
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 7,
  },
  invoiceSealText: {
    color: colors.success,
    fontFamily: 'Vazirmatn_500Medium',
    fontSize: 8,
  },
  invoiceFooter: {
    position: 'absolute',
    left: 0,
    right: 0,
    bottom: 0,
    minHeight: 86,
    padding: 15,
    backgroundColor: colors.paper,
    borderTopWidth: 1,
    borderTopColor: colors.line,
  },
  exportButton: {
    flex: 1,
    maxHeight: 56,
    borderRadius: 17,
    backgroundColor: colors.wine,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 9,
  },
  exportButtonText: {
    color: colors.white,
    fontFamily: 'Vazirmatn_700Bold',
    fontSize: 12,
  },
  buttonDisabled: { opacity: 0.65 },
});
