import { useMemo, useState } from "react";
import {
  Alert,
  I18nManager,
  Platform,
  Pressable,
  SafeAreaView,
  ScrollView,
  Share,
  StatusBar,
  StyleSheet,
  Text,
  TextInput,
  View
} from "react-native";

import { products } from "./src/data/products";
import {
  DEFAULT_GOLD_GRAM_PRICE,
  calculateInvoice,
  createInvoiceText,
  formatCurrencyFa
} from "./src/utils/pricing";

I18nManager.allowRTL(true);

const persianDigits = "۰۱۲۳۴۵۶۷۸۹";
const arabicDigits = "٠١٢٣٤٥٦٧٨٩";

function toEnglishDigits(value) {
  return value
    .replace(/[۰-۹]/g, (digit) => String(persianDigits.indexOf(digit)))
    .replace(/[٠-٩]/g, (digit) => String(arabicDigits.indexOf(digit)));
}

function parsePriceInput(value) {
  const numeric = toEnglishDigits(value).replace(/[^\d]/g, "");
  return Number(numeric) || DEFAULT_GOLD_GRAM_PRICE;
}

export default function App() {
  const [cart, setCart] = useState({});
  const [goldPriceInput, setGoldPriceInput] = useState(String(DEFAULT_GOLD_GRAM_PRICE));
  const [issuedInvoiceText, setIssuedInvoiceText] = useState("");
  const gramPrice = parsePriceInput(goldPriceInput);

  const invoice = useMemo(() => calculateInvoice(cart, products, gramPrice), [cart, gramPrice]);
  const hasCartItems = invoice.items.length > 0;

  const updateQuantity = (productId, delta) => {
    setCart((currentCart) => {
      const nextQuantity = Math.max((currentCart[productId] ?? 0) + delta, 0);
      return {
        ...currentCart,
        [productId]: nextQuantity
      };
    });
    setIssuedInvoiceText("");
  };

  const issueInvoice = async () => {
    if (!hasCartItems) {
      Alert.alert("سبد خرید خالی است", "برای صدور فاکتور حداقل یک کالا انتخاب کنید.");
      return;
    }

    const invoiceText = createInvoiceText(invoice, gramPrice);
    setIssuedInvoiceText(invoiceText);

    if (Platform.OS !== "web") {
      await Share.share({
        title: "فاکتور فروش طلا و جواهر",
        message: invoiceText
      });
    }
  };

  return (
    <SafeAreaView style={styles.safeArea}>
      <StatusBar barStyle="light-content" backgroundColor="#111827" />
      <ScrollView contentContainerStyle={styles.container}>
        <View style={styles.hero}>
          <Text style={styles.eyebrow}>فروشگاه آنلاین طلا و جواهر</Text>
          <Text style={styles.title}>گالری میلاد صباغ</Text>
          <Text style={styles.subtitle}>
            انتخاب محصول، محاسبه قیمت روز و صدور فاکتور فروش در یک اپ اندرویدی ساده و سریع.
          </Text>
        </View>

        <View style={styles.card}>
          <Text style={styles.sectionTitle}>قیمت روز طلا</Text>
          <Text style={styles.helperText}>قیمت هر گرم طلای ۱۸ عیار را به ریال وارد کنید.</Text>
          <TextInput
            keyboardType="number-pad"
            onChangeText={setGoldPriceInput}
            style={styles.input}
            value={goldPriceInput}
            placeholder="مثال: 42000000"
            placeholderTextColor="#9CA3AF"
          />
          <Text style={styles.pricePreview}>مبنای محاسبه: {formatCurrencyFa(gramPrice)}</Text>
        </View>

        <Text style={styles.sectionTitle}>ویترین محصولات</Text>
        {products.map((product) => {
          const quantity = cart[product.id] ?? 0;
          const line = invoice.items.find((item) => item.id === product.id);

          return (
            <View key={product.id} style={styles.productCard}>
              <View style={styles.productHeader}>
                <Text style={styles.productEmoji}>{product.imageEmoji}</Text>
                <View style={styles.productInfo}>
                  <Text style={styles.productTitle}>{product.title}</Text>
                  <Text style={styles.productMeta}>
                    {product.category} · طلای {product.karat} عیار · {product.weightGram} گرم
                  </Text>
                </View>
              </View>
              <Text style={styles.productDescription}>{product.description}</Text>
              <View style={styles.productFooter}>
                <View>
                  <Text style={styles.mutedLabel}>قیمت واحد با اجرت و مالیات</Text>
                  <Text style={styles.productPrice}>
                    {formatCurrencyFa(line?.unitTotal ?? calculateInvoice({ [product.id]: 1 }, [product], gramPrice).totals.payable)}
                  </Text>
                </View>
                <View style={styles.quantityControl}>
                  <Pressable style={styles.quantityButton} onPress={() => updateQuantity(product.id, -1)}>
                    <Text style={styles.quantityButtonText}>-</Text>
                  </Pressable>
                  <Text style={styles.quantityText}>{quantity}</Text>
                  <Pressable style={styles.quantityButton} onPress={() => updateQuantity(product.id, 1)}>
                    <Text style={styles.quantityButtonText}>+</Text>
                  </Pressable>
                </View>
              </View>
            </View>
          );
        })}

        <View style={styles.summaryCard}>
          <Text style={styles.sectionTitle}>خلاصه فاکتور</Text>
          <SummaryRow label="جمع طلای خام" value={invoice.totals.baseGoldPrice} />
          <SummaryRow label="جمع اجرت ساخت" value={invoice.totals.wage} />
          <SummaryRow label="سود فروشنده" value={invoice.totals.profit} />
          <SummaryRow label="مالیات" value={invoice.totals.tax} />
          <View style={styles.divider} />
          <View style={styles.totalRow}>
            <Text style={styles.totalLabel}>مبلغ قابل پرداخت</Text>
            <Text style={styles.totalValue}>{formatCurrencyFa(invoice.totals.payable)}</Text>
          </View>
          <Pressable
            onPress={issueInvoice}
            style={({ pressed }) => [
              styles.invoiceButton,
              !hasCartItems && styles.invoiceButtonDisabled,
              pressed && hasCartItems && styles.invoiceButtonPressed
            ]}
          >
            <Text style={styles.invoiceButtonText}>صدور و اشتراک گذاری فاکتور</Text>
          </Pressable>
        </View>

        {issuedInvoiceText ? (
          <View style={styles.invoicePreview}>
            <Text style={styles.sectionTitle}>پیش نمایش فاکتور صادر شده</Text>
            <Text style={styles.invoiceText}>{issuedInvoiceText}</Text>
          </View>
        ) : null}
      </ScrollView>
    </SafeAreaView>
  );
}

function SummaryRow({ label, value }) {
  return (
    <View style={styles.summaryRow}>
      <Text style={styles.summaryLabel}>{label}</Text>
      <Text style={styles.summaryValue}>{formatCurrencyFa(value)}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: "#111827"
  },
  container: {
    backgroundColor: "#F8F3E8",
    padding: 20,
    paddingBottom: 40
  },
  hero: {
    backgroundColor: "#111827",
    borderRadius: 28,
    marginBottom: 18,
    padding: 24
  },
  eyebrow: {
    color: "#FACC15",
    fontSize: 14,
    marginBottom: 8,
    textAlign: "right",
    writingDirection: "rtl"
  },
  title: {
    color: "#FFFFFF",
    fontSize: 32,
    fontWeight: "800",
    marginBottom: 12,
    textAlign: "right",
    writingDirection: "rtl"
  },
  subtitle: {
    color: "#D1D5DB",
    fontSize: 16,
    lineHeight: 28,
    textAlign: "right",
    writingDirection: "rtl"
  },
  card: {
    backgroundColor: "#FFFFFF",
    borderRadius: 22,
    marginBottom: 18,
    padding: 18,
    shadowColor: "#111827",
    shadowOpacity: 0.08,
    shadowRadius: 14
  },
  sectionTitle: {
    color: "#1F2937",
    fontSize: 20,
    fontWeight: "800",
    marginBottom: 10,
    textAlign: "right",
    writingDirection: "rtl"
  },
  helperText: {
    color: "#6B7280",
    fontSize: 14,
    marginBottom: 12,
    textAlign: "right",
    writingDirection: "rtl"
  },
  input: {
    backgroundColor: "#F9FAFB",
    borderColor: "#E5E7EB",
    borderRadius: 16,
    borderWidth: 1,
    color: "#111827",
    fontSize: 18,
    padding: 14,
    textAlign: "right"
  },
  pricePreview: {
    color: "#92400E",
    fontSize: 14,
    fontWeight: "700",
    marginTop: 10,
    textAlign: "right",
    writingDirection: "rtl"
  },
  productCard: {
    backgroundColor: "#FFFFFF",
    borderRadius: 24,
    marginBottom: 14,
    padding: 18,
    shadowColor: "#111827",
    shadowOpacity: 0.08,
    shadowRadius: 14
  },
  productHeader: {
    alignItems: "center",
    flexDirection: "row-reverse",
    gap: 12
  },
  productEmoji: {
    backgroundColor: "#FEF3C7",
    borderRadius: 22,
    fontSize: 30,
    overflow: "hidden",
    padding: 12
  },
  productInfo: {
    flex: 1
  },
  productTitle: {
    color: "#111827",
    fontSize: 18,
    fontWeight: "800",
    textAlign: "right",
    writingDirection: "rtl"
  },
  productMeta: {
    color: "#6B7280",
    fontSize: 13,
    marginTop: 4,
    textAlign: "right",
    writingDirection: "rtl"
  },
  productDescription: {
    color: "#4B5563",
    fontSize: 14,
    lineHeight: 24,
    marginTop: 12,
    textAlign: "right",
    writingDirection: "rtl"
  },
  productFooter: {
    alignItems: "center",
    flexDirection: "row-reverse",
    justifyContent: "space-between",
    marginTop: 16
  },
  mutedLabel: {
    color: "#9CA3AF",
    fontSize: 12,
    marginBottom: 4,
    textAlign: "right",
    writingDirection: "rtl"
  },
  productPrice: {
    color: "#B45309",
    fontSize: 15,
    fontWeight: "800",
    textAlign: "right",
    writingDirection: "rtl"
  },
  quantityControl: {
    alignItems: "center",
    backgroundColor: "#111827",
    borderRadius: 18,
    flexDirection: "row",
    padding: 4
  },
  quantityButton: {
    alignItems: "center",
    backgroundColor: "#FACC15",
    borderRadius: 14,
    height: 34,
    justifyContent: "center",
    width: 34
  },
  quantityButtonText: {
    color: "#111827",
    fontSize: 20,
    fontWeight: "800"
  },
  quantityText: {
    color: "#FFFFFF",
    fontSize: 16,
    fontWeight: "800",
    minWidth: 32,
    textAlign: "center"
  },
  summaryCard: {
    backgroundColor: "#111827",
    borderRadius: 26,
    marginTop: 8,
    padding: 20
  },
  summaryRow: {
    alignItems: "center",
    flexDirection: "row-reverse",
    justifyContent: "space-between",
    marginBottom: 10
  },
  summaryLabel: {
    color: "#D1D5DB",
    fontSize: 15,
    textAlign: "right",
    writingDirection: "rtl"
  },
  summaryValue: {
    color: "#FFFFFF",
    fontSize: 15,
    fontWeight: "700",
    textAlign: "left",
    writingDirection: "rtl"
  },
  divider: {
    backgroundColor: "#374151",
    height: 1,
    marginVertical: 12
  },
  totalRow: {
    alignItems: "center",
    flexDirection: "row-reverse",
    justifyContent: "space-between",
    marginBottom: 18
  },
  totalLabel: {
    color: "#FACC15",
    fontSize: 18,
    fontWeight: "800",
    textAlign: "right",
    writingDirection: "rtl"
  },
  totalValue: {
    color: "#FACC15",
    fontSize: 18,
    fontWeight: "900",
    textAlign: "left",
    writingDirection: "rtl"
  },
  invoiceButton: {
    alignItems: "center",
    backgroundColor: "#FACC15",
    borderRadius: 18,
    padding: 16
  },
  invoiceButtonDisabled: {
    opacity: 0.45
  },
  invoiceButtonPressed: {
    transform: [{ scale: 0.98 }]
  },
  invoiceButtonText: {
    color: "#111827",
    fontSize: 16,
    fontWeight: "900",
    textAlign: "center",
    writingDirection: "rtl"
  },
  invoicePreview: {
    backgroundColor: "#FFFFFF",
    borderRadius: 22,
    marginTop: 18,
    padding: 18
  },
  invoiceText: {
    color: "#1F2937",
    fontFamily: Platform.OS === "android" ? "monospace" : undefined,
    fontSize: 14,
    lineHeight: 24,
    textAlign: "right",
    writingDirection: "rtl"
  }
});
