import { useMemo, useState } from 'react';
import {
  I18nManager,
  SafeAreaView,
  ScrollView,
  StatusBar,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View,
} from 'react-native';

import {
  DEFAULT_GOLD_RATE,
  calculateInvoice,
  catalog,
  formatRial,
  makeInvoiceNumber,
} from './src/invoice';

I18nManager.allowRTL(true);
I18nManager.forceRTL(true);

const initialCart = [
  { ...catalog[0], quantity: 1 },
  { ...catalog[1], quantity: 1 },
];

const digitsFa = ['۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹'];

function toPersianNumber(value) {
  return String(value).replace(/\d/g, (digit) => digitsFa[Number(digit)]);
}

function normalizeNumber(value) {
  const fa = '۰۱۲۳۴۵۶۷۸۹';
  const ar = '٠١٢٣٤٥٦٧٨٩';
  return String(value)
    .replace(/[۰-۹]/g, (digit) => fa.indexOf(digit))
    .replace(/[٠-٩]/g, (digit) => ar.indexOf(digit))
    .replace(/[^\d]/g, '');
}

export default function App() {
  const [customerName, setCustomerName] = useState('مشتری حضوری');
  const [customerPhone, setCustomerPhone] = useState('09120000000');
  const [goldRate, setGoldRate] = useState(String(DEFAULT_GOLD_RATE));
  const [cart, setCart] = useState(initialCart);
  const [invoiceReady, setInvoiceReady] = useState(true);

  const parsedGoldRate = Number(normalizeNumber(goldRate)) || DEFAULT_GOLD_RATE;
  const invoice = useMemo(() => calculateInvoice(cart, parsedGoldRate), [cart, parsedGoldRate]);
  const invoiceNumber = useMemo(() => makeInvoiceNumber(), []);

  function addToCart(product) {
    setCart((items) => {
      const found = items.find((item) => item.id === product.id);
      if (found) {
        return items.map((item) =>
          item.id === product.id ? { ...item, quantity: item.quantity + 1 } : item
        );
      }
      return [...items, { ...product, quantity: 1 }];
    });
    setInvoiceReady(false);
  }

  function changeQuantity(productId, amount) {
    setCart((items) =>
      items
        .map((item) =>
          item.id === productId ? { ...item, quantity: Math.max(1, item.quantity + amount) } : item
        )
        .filter(Boolean)
    );
    setInvoiceReady(false);
  }

  function removeFromCart(productId) {
    setCart((items) => items.filter((item) => item.id !== productId));
    setInvoiceReady(false);
  }

  return (
    <SafeAreaView style={styles.safeArea}>
      <StatusBar barStyle="light-content" />
      <ScrollView contentContainerStyle={styles.container} showsVerticalScrollIndicator={false}>
        <View style={styles.hero}>
          <View style={styles.heroBadge}>
            <Text style={styles.heroBadgeText}>فروشگاه طلا و جواهر</Text>
          </View>
          <Text style={styles.heroTitle}>طلای میلاد</Text>
          <Text style={styles.heroSubtitle}>
            مدیریت فروش، محاسبه اجرت و مالیات، و صدور فاکتور رسمی برای مشتری
          </Text>
          <View style={styles.heroStats}>
            <Stat label="اقلام فاکتور" value={toPersianNumber(cart.length)} />
            <Stat label="جمع فاکتور" value={formatRial(invoice.grandTotal)} />
          </View>
        </View>

        <View style={styles.section}>
          <SectionHeader title="اطلاعات فروش" eyebrow="ثبت سریع سفارش" />
          <View style={styles.inputGrid}>
            <LabeledInput
              label="نام مشتری"
              value={customerName}
              onChangeText={setCustomerName}
              placeholder="نام و نام خانوادگی"
            />
            <LabeledInput
              label="شماره تماس"
              value={customerPhone}
              onChangeText={setCustomerPhone}
              keyboardType="phone-pad"
              placeholder="09..."
            />
            <LabeledInput
              label="نرخ هر گرم طلای ۱۸ عیار"
              value={goldRate}
              onChangeText={setGoldRate}
              keyboardType="numeric"
              placeholder="مثلا 45200000"
            />
          </View>
        </View>

        <View style={styles.section}>
          <SectionHeader title="کاتالوگ جواهرات" eyebrow="افزودن به سبد" />
          {catalog.map((product) => (
            <View key={product.id} style={styles.productCard}>
              <View style={[styles.productGem, { backgroundColor: product.imageTone }]}>
                <Text style={styles.productGemText}>طلا</Text>
              </View>
              <View style={styles.productContent}>
                <Text style={styles.productCategory}>{product.category}</Text>
                <Text style={styles.productTitle}>{product.title}</Text>
                <Text style={styles.productMeta}>
                  وزن {toPersianNumber(product.weightGram)} گرم | اجرت{' '}
                  {toPersianNumber(product.makingFeePercent)}٪ | سود{' '}
                  {toPersianNumber(product.profitPercent)}٪
                </Text>
              </View>
              <TouchableOpacity style={styles.addButton} onPress={() => addToCart(product)}>
                <Text style={styles.addButtonText}>افزودن</Text>
              </TouchableOpacity>
            </View>
          ))}
        </View>

        <View style={styles.section}>
          <SectionHeader title="سبد خرید" eyebrow="قبل از صدور فاکتور" />
          {cart.length === 0 ? (
            <Text style={styles.emptyText}>هنوز محصولی انتخاب نشده است.</Text>
          ) : (
            invoice.lines.map((item) => (
              <View key={item.id} style={styles.cartRow}>
                <View style={styles.cartInfo}>
                  <Text style={styles.cartTitle}>{item.title}</Text>
                  <Text style={styles.cartMeta}>
                    {toPersianNumber(item.quantity)} عدد | {toPersianNumber(item.weightGram)} گرم
                  </Text>
                  <Text style={styles.cartPrice}>{formatRial(item.total)}</Text>
                </View>
                <View style={styles.quantityControls}>
                  <TouchableOpacity
                    style={styles.quantityButton}
                    onPress={() => changeQuantity(item.id, 1)}
                  >
                    <Text style={styles.quantityText}>+</Text>
                  </TouchableOpacity>
                  <TouchableOpacity
                    style={styles.quantityButton}
                    onPress={() => changeQuantity(item.id, -1)}
                  >
                    <Text style={styles.quantityText}>-</Text>
                  </TouchableOpacity>
                  <TouchableOpacity style={styles.removeButton} onPress={() => removeFromCart(item.id)}>
                    <Text style={styles.removeButtonText}>حذف</Text>
                  </TouchableOpacity>
                </View>
              </View>
            ))
          )}
          <TouchableOpacity
            disabled={cart.length === 0}
            style={[styles.primaryButton, cart.length === 0 && styles.disabledButton]}
            onPress={() => setInvoiceReady(true)}
          >
            <Text style={styles.primaryButtonText}>صدور فاکتور</Text>
          </TouchableOpacity>
        </View>

        {invoiceReady && cart.length > 0 && (
          <View style={styles.invoiceCard}>
            <View style={styles.invoiceHeader}>
              <View>
                <Text style={styles.invoiceLabel}>شماره فاکتور</Text>
                <Text style={styles.invoiceNumber}>{invoiceNumber}</Text>
              </View>
              <View style={styles.invoiceStatus}>
                <Text style={styles.invoiceStatusText}>آماده چاپ</Text>
              </View>
            </View>

            <View style={styles.customerBox}>
              <Text style={styles.customerLine}>خریدار: {customerName}</Text>
              <Text style={styles.customerLine}>تماس: {customerPhone}</Text>
              <Text style={styles.customerLine}>نرخ روز: {formatRial(parsedGoldRate)}</Text>
            </View>

            {invoice.lines.map((item, index) => (
              <View key={item.id} style={styles.invoiceLine}>
                <Text style={styles.invoiceLineTitle}>
                  {toPersianNumber(index + 1)}. {item.title}
                </Text>
                <Text style={styles.invoiceLineMeta}>
                  ارزش طلا {formatRial(item.goldValue)} + اجرت {formatRial(item.makingFee)} + سود{' '}
                  {formatRial(item.profit)} + مالیات {formatRial(item.tax)}
                </Text>
                <Text style={styles.invoiceLineTotal}>{formatRial(item.total)}</Text>
              </View>
            ))}

            <View style={styles.totals}>
              <TotalRow label="ارزش طلای خام" value={invoice.subtotalGold} />
              <TotalRow label="جمع اجرت ساخت" value={invoice.makingFee} />
              <TotalRow label="جمع سود فروشگاه" value={invoice.profit} />
              <TotalRow label="مالیات بر اجرت و سود" value={invoice.tax} />
              <View style={styles.grandTotalRow}>
                <Text style={styles.grandTotalLabel}>مبلغ قابل پرداخت</Text>
                <Text style={styles.grandTotalValue}>{formatRial(invoice.grandTotal)}</Text>
              </View>
            </View>

            <View style={styles.invoiceActions}>
              <TouchableOpacity style={styles.secondaryButton}>
                <Text style={styles.secondaryButtonText}>اشتراک فاکتور</Text>
              </TouchableOpacity>
              <TouchableOpacity style={styles.secondaryButton}>
                <Text style={styles.secondaryButtonText}>چاپ/ذخیره PDF</Text>
              </TouchableOpacity>
            </View>
          </View>
        )}
      </ScrollView>
    </SafeAreaView>
  );
}

function SectionHeader({ title, eyebrow }) {
  return (
    <View style={styles.sectionHeader}>
      <Text style={styles.eyebrow}>{eyebrow}</Text>
      <Text style={styles.sectionTitle}>{title}</Text>
    </View>
  );
}

function LabeledInput({ label, ...props }) {
  return (
    <View style={styles.inputWrapper}>
      <Text style={styles.inputLabel}>{label}</Text>
      <TextInput
        {...props}
        placeholderTextColor="#9C8E7D"
        style={styles.input}
        textAlign="right"
      />
    </View>
  );
}

function Stat({ label, value }) {
  return (
    <View style={styles.statCard}>
      <Text style={styles.statValue}>{value}</Text>
      <Text style={styles.statLabel}>{label}</Text>
    </View>
  );
}

function TotalRow({ label, value }) {
  return (
    <View style={styles.totalRow}>
      <Text style={styles.totalLabel}>{label}</Text>
      <Text style={styles.totalValue}>{formatRial(value)}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: '#120C06',
  },
  container: {
    padding: 18,
    paddingBottom: 36,
    backgroundColor: '#F7EFE4',
  },
  hero: {
    borderRadius: 30,
    padding: 24,
    backgroundColor: '#1F1308',
    marginBottom: 18,
  },
  heroBadge: {
    alignSelf: 'flex-start',
    paddingHorizontal: 14,
    paddingVertical: 7,
    borderRadius: 999,
    backgroundColor: '#D9A441',
  },
  heroBadgeText: {
    color: '#1F1308',
    fontWeight: '800',
    writingDirection: 'rtl',
  },
  heroTitle: {
    marginTop: 28,
    fontSize: 34,
    fontWeight: '900',
    color: '#FFF8E8',
    textAlign: 'right',
    writingDirection: 'rtl',
  },
  heroSubtitle: {
    marginTop: 10,
    color: '#E6D1AA',
    fontSize: 16,
    lineHeight: 28,
    textAlign: 'right',
    writingDirection: 'rtl',
  },
  heroStats: {
    flexDirection: 'row-reverse',
    gap: 12,
    marginTop: 24,
  },
  statCard: {
    flex: 1,
    padding: 14,
    borderRadius: 20,
    backgroundColor: '#352314',
  },
  statValue: {
    color: '#FFE0A3',
    fontSize: 16,
    fontWeight: '900',
    textAlign: 'right',
  },
  statLabel: {
    color: '#CDB891',
    marginTop: 6,
    textAlign: 'right',
  },
  section: {
    marginBottom: 18,
    padding: 18,
    borderRadius: 26,
    backgroundColor: '#FFFDF8',
    shadowColor: '#61401C',
    shadowOffset: { width: 0, height: 12 },
    shadowOpacity: 0.08,
    shadowRadius: 24,
    elevation: 4,
  },
  sectionHeader: {
    marginBottom: 14,
  },
  eyebrow: {
    color: '#A97424',
    fontSize: 12,
    fontWeight: '800',
    textAlign: 'right',
    writingDirection: 'rtl',
  },
  sectionTitle: {
    color: '#21170F',
    fontSize: 22,
    fontWeight: '900',
    textAlign: 'right',
    writingDirection: 'rtl',
  },
  inputGrid: {
    gap: 12,
  },
  inputWrapper: {
    gap: 7,
  },
  inputLabel: {
    color: '#4E3724',
    fontWeight: '800',
    textAlign: 'right',
    writingDirection: 'rtl',
  },
  input: {
    minHeight: 52,
    borderRadius: 16,
    borderWidth: 1,
    borderColor: '#E7D8BE',
    backgroundColor: '#FFF8EA',
    color: '#20160E',
    paddingHorizontal: 14,
    fontSize: 16,
    writingDirection: 'rtl',
  },
  productCard: {
    flexDirection: 'row-reverse',
    alignItems: 'center',
    gap: 12,
    borderWidth: 1,
    borderColor: '#EFE1C9',
    borderRadius: 22,
    padding: 12,
    marginBottom: 12,
    backgroundColor: '#FFFCF4',
  },
  productGem: {
    width: 62,
    height: 62,
    borderRadius: 20,
    justifyContent: 'center',
    alignItems: 'center',
  },
  productGemText: {
    color: '#5F3910',
    fontWeight: '900',
  },
  productContent: {
    flex: 1,
  },
  productCategory: {
    color: '#B28335',
    fontSize: 12,
    fontWeight: '800',
    textAlign: 'right',
  },
  productTitle: {
    color: '#1E160E',
    fontSize: 17,
    fontWeight: '900',
    textAlign: 'right',
    marginTop: 3,
    writingDirection: 'rtl',
  },
  productMeta: {
    color: '#7A6A58',
    marginTop: 5,
    textAlign: 'right',
    writingDirection: 'rtl',
  },
  addButton: {
    borderRadius: 16,
    paddingHorizontal: 14,
    paddingVertical: 11,
    backgroundColor: '#1F1308',
  },
  addButtonText: {
    color: '#FFE4B0',
    fontWeight: '900',
  },
  emptyText: {
    color: '#8A7660',
    textAlign: 'center',
    writingDirection: 'rtl',
  },
  cartRow: {
    flexDirection: 'row-reverse',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: 12,
    borderBottomWidth: 1,
    borderBottomColor: '#EFE4D3',
    paddingVertical: 12,
  },
  cartInfo: {
    flex: 1,
  },
  cartTitle: {
    color: '#25190F',
    fontSize: 16,
    fontWeight: '900',
    textAlign: 'right',
    writingDirection: 'rtl',
  },
  cartMeta: {
    color: '#7D6A57',
    marginTop: 5,
    textAlign: 'right',
    writingDirection: 'rtl',
  },
  cartPrice: {
    color: '#A06112',
    marginTop: 5,
    fontWeight: '900',
    textAlign: 'right',
  },
  quantityControls: {
    flexDirection: 'row-reverse',
    alignItems: 'center',
    gap: 8,
  },
  quantityButton: {
    width: 34,
    height: 34,
    borderRadius: 12,
    backgroundColor: '#F0DFC2',
    alignItems: 'center',
    justifyContent: 'center',
  },
  quantityText: {
    color: '#40280F',
    fontWeight: '900',
    fontSize: 18,
  },
  removeButton: {
    paddingHorizontal: 12,
    paddingVertical: 9,
    borderRadius: 12,
    backgroundColor: '#FFF1EB',
  },
  removeButtonText: {
    color: '#B54521',
    fontWeight: '800',
  },
  primaryButton: {
    marginTop: 16,
    minHeight: 54,
    borderRadius: 18,
    backgroundColor: '#C88A28',
    alignItems: 'center',
    justifyContent: 'center',
  },
  disabledButton: {
    opacity: 0.45,
  },
  primaryButtonText: {
    color: '#221506',
    fontWeight: '900',
    fontSize: 17,
  },
  invoiceCard: {
    borderRadius: 30,
    padding: 20,
    backgroundColor: '#21170F',
  },
  invoiceHeader: {
    flexDirection: 'row-reverse',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 16,
  },
  invoiceLabel: {
    color: '#BAA17C',
    textAlign: 'right',
  },
  invoiceNumber: {
    color: '#FFE7AE',
    fontSize: 20,
    fontWeight: '900',
    marginTop: 4,
  },
  invoiceStatus: {
    borderRadius: 999,
    paddingHorizontal: 12,
    paddingVertical: 8,
    backgroundColor: '#3D2B18',
  },
  invoiceStatusText: {
    color: '#F7CA72',
    fontWeight: '900',
  },
  customerBox: {
    borderRadius: 18,
    padding: 14,
    backgroundColor: '#322417',
    marginBottom: 12,
  },
  customerLine: {
    color: '#EFE0C5',
    lineHeight: 26,
    textAlign: 'right',
    writingDirection: 'rtl',
  },
  invoiceLine: {
    paddingVertical: 12,
    borderBottomWidth: 1,
    borderBottomColor: '#3E2C1C',
  },
  invoiceLineTitle: {
    color: '#FFFFFF',
    fontWeight: '900',
    textAlign: 'right',
    writingDirection: 'rtl',
  },
  invoiceLineMeta: {
    color: '#CDBDA5',
    marginTop: 6,
    lineHeight: 23,
    textAlign: 'right',
    writingDirection: 'rtl',
  },
  invoiceLineTotal: {
    color: '#FFD783',
    fontWeight: '900',
    marginTop: 6,
    textAlign: 'right',
  },
  totals: {
    marginTop: 14,
    gap: 10,
  },
  totalRow: {
    flexDirection: 'row-reverse',
    justifyContent: 'space-between',
    gap: 12,
  },
  totalLabel: {
    color: '#C9B89F',
    writingDirection: 'rtl',
  },
  totalValue: {
    color: '#FFF4DD',
    fontWeight: '800',
  },
  grandTotalRow: {
    marginTop: 8,
    borderRadius: 18,
    padding: 14,
    flexDirection: 'row-reverse',
    justifyContent: 'space-between',
    backgroundColor: '#F0B84A',
  },
  grandTotalLabel: {
    color: '#20150A',
    fontWeight: '900',
    writingDirection: 'rtl',
  },
  grandTotalValue: {
    color: '#20150A',
    fontWeight: '900',
  },
  invoiceActions: {
    flexDirection: 'row-reverse',
    gap: 10,
    marginTop: 16,
  },
  secondaryButton: {
    flex: 1,
    borderRadius: 16,
    paddingVertical: 13,
    alignItems: 'center',
    backgroundColor: '#342516',
  },
  secondaryButtonText: {
    color: '#F8D89B',
    fontWeight: '900',
  },
});
