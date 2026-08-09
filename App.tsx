import * as Clipboard from "expo-clipboard";
import { StatusBar } from "expo-status-bar";
import { useMemo, useState } from "react";
import {
  I18nManager,
  Pressable,
  SafeAreaView,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View
} from "react-native";

import { catalog } from "./src/data/catalog";
import {
  buildInvoiceText,
  calculateInvoice,
  formatGram,
  formatRial,
  type CartItem,
  type JewelryProduct
} from "./src/lib/invoice";

I18nManager.allowRTL(true);
I18nManager.forceRTL(true);

type QuantityMap = Record<string, number>;

const colors = {
  background: "#fff9ef",
  card: "#ffffff",
  primary: "#9c6b12",
  primaryDark: "#4d3000",
  accent: "#f4d27c",
  border: "#ead9b7",
  muted: "#765f39",
  success: "#167047",
  danger: "#b23b3b"
};

function parseNumericInput(value: string): number {
  const normalized = value
    .replace(/[۰-۹]/g, (digit) => String("۰۱۲۳۴۵۶۷۸۹".indexOf(digit)))
    .replace(/[٠-٩]/g, (digit) => String("٠١٢٣٤٥٦٧٨٩".indexOf(digit)))
    .replace(/[^\d.]/g, "");

  return Number(normalized || 0);
}

function App(): JSX.Element {
  const [goldPrice, setGoldPrice] = useState("35700000");
  const [vatPercent, setVatPercent] = useState("10");
  const [discount, setDiscount] = useState("0");
  const [customerName, setCustomerName] = useState("");
  const [customerPhone, setCustomerPhone] = useState("");
  const [quantities, setQuantities] = useState<QuantityMap>({ [catalog[0].id]: 1 });
  const [invoiceIssued, setInvoiceIssued] = useState(false);

  const cart = useMemo<CartItem[]>(() => {
    return catalog
      .map((product) => ({ product, quantity: quantities[product.id] ?? 0 }))
      .filter((item) => item.quantity > 0);
  }, [quantities]);

  const invoice = useMemo(() => {
    return calculateInvoice(cart, {
      goldPricePerGram: parseNumericInput(goldPrice),
      vatPercent: parseNumericInput(vatPercent),
      discount: parseNumericInput(discount),
      customer: {
        name: customerName.trim(),
        phone: customerPhone.trim()
      }
    });
  }, [cart, customerName, customerPhone, discount, goldPrice, vatPercent]);

  const invoiceText = useMemo(() => buildInvoiceText(invoice), [invoice]);

  function updateQuantity(product: JewelryProduct, delta: number): void {
    setInvoiceIssued(false);
    setQuantities((current) => {
      const nextQuantity = Math.max(0, (current[product.id] ?? 0) + delta);
      return {
        ...current,
        [product.id]: nextQuantity
      };
    });
  }

  async function copyInvoice(): Promise<void> {
    setInvoiceIssued(true);
    await Clipboard.setStringAsync(invoiceText);
  }

  return (
    <SafeAreaView style={styles.safeArea}>
      <StatusBar style="dark" />
      <ScrollView contentContainerStyle={styles.container}>
        <View style={styles.hero}>
          <Text style={styles.badge}>نسخه اندروید فروشگاهی</Text>
          <Text style={styles.title}>فروش طلا و جواهر</Text>
          <Text style={styles.subtitle}>
            ثبت سفارش، محاسبه اجرت و مالیات، مدیریت سبد خرید و صدور فاکتور آماده ارسال برای مشتری.
          </Text>
        </View>

        <View style={styles.panel}>
          <Text style={styles.sectionTitle}>تنظیمات قیمت روز</Text>
          <View style={styles.inputGrid}>
            <LabeledInput label="قیمت هر گرم طلا (ریال)" value={goldPrice} onChangeText={setGoldPrice} />
            <LabeledInput label="مالیات بر اجرت و سود (%)" value={vatPercent} onChangeText={setVatPercent} />
            <LabeledInput label="تخفیف فاکتور (ریال)" value={discount} onChangeText={setDiscount} />
          </View>
        </View>

        <View style={styles.panel}>
          <Text style={styles.sectionTitle}>کاتالوگ محصولات</Text>
          {catalog.map((product) => (
            <ProductCard
              key={product.id}
              product={product}
              quantity={quantities[product.id] ?? 0}
              onDecrease={() => updateQuantity(product, -1)}
              onIncrease={() => updateQuantity(product, 1)}
            />
          ))}
        </View>

        <View style={styles.panel}>
          <Text style={styles.sectionTitle}>اطلاعات مشتری</Text>
          <LabeledInput label="نام مشتری" value={customerName} onChangeText={setCustomerName} keyboardType="default" />
          <LabeledInput label="شماره تماس" value={customerPhone} onChangeText={setCustomerPhone} />
        </View>

        <View style={styles.invoiceCard}>
          <View style={styles.invoiceHeader}>
            <View>
              <Text style={styles.invoiceLabel}>فاکتور فروش</Text>
              <Text style={styles.invoiceNumber}>{invoice.invoiceNumber}</Text>
            </View>
            <Text style={styles.payable}>{formatRial(invoice.payable)}</Text>
          </View>

          {invoice.lines.length === 0 ? (
            <Text style={styles.emptyCart}>برای صدور فاکتور، حداقل یک محصول به سبد اضافه کنید.</Text>
          ) : (
            invoice.lines.map((line) => (
              <View key={line.id} style={styles.invoiceLine}>
                <View style={styles.lineHeading}>
                  <Text style={styles.lineName}>{line.name}</Text>
                  <Text style={styles.lineQty}>x{line.quantity}</Text>
                </View>
                <Text style={styles.lineMeta}>
                  وزن {formatGram(line.weightGram)} | طلا {formatRial(line.goldValue)} | اجرت {formatRial(line.makingFee)}
                </Text>
                <Text style={styles.lineMeta}>
                  سود {formatRial(line.sellerProfit)} | سنگ {formatRial(line.stoneFee)} | مالیات {formatRial(line.vat)}
                </Text>
                <Text style={styles.lineTotal}>{formatRial(line.total)}</Text>
              </View>
            ))
          )}

          <SummaryRow label="جمع کل" value={formatRial(invoice.subtotal)} />
          <SummaryRow label="تخفیف" value={formatRial(invoice.discount)} />
          <SummaryRow label="وزن کل" value={formatGram(invoice.totalWeightGram)} />
          <SummaryRow label="قابل پرداخت" value={formatRial(invoice.payable)} emphasized />

          <Pressable
            accessibilityRole="button"
            disabled={invoice.lines.length === 0}
            onPress={copyInvoice}
            style={({ pressed }) => [
              styles.primaryButton,
              invoice.lines.length === 0 && styles.disabledButton,
              pressed && styles.pressedButton
            ]}
          >
            <Text style={styles.primaryButtonText}>
              {invoiceIssued ? "فاکتور کپی شد" : "صدور و کپی فاکتور"}
            </Text>
          </Pressable>
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

interface LabeledInputProps {
  label: string;
  value: string;
  onChangeText: (value: string) => void;
  keyboardType?: "default" | "numeric";
}

function LabeledInput({ label, value, onChangeText, keyboardType = "numeric" }: LabeledInputProps): JSX.Element {
  return (
    <View style={styles.inputGroup}>
      <Text style={styles.inputLabel}>{label}</Text>
      <TextInput
        keyboardType={keyboardType}
        onChangeText={onChangeText}
        placeholder={label}
        placeholderTextColor="#a18b63"
        style={styles.input}
        value={value}
      />
    </View>
  );
}

interface ProductCardProps {
  product: JewelryProduct;
  quantity: number;
  onDecrease: () => void;
  onIncrease: () => void;
}

function ProductCard({ product, quantity, onDecrease, onIncrease }: ProductCardProps): JSX.Element {
  return (
    <View style={styles.productCard}>
      <View style={styles.productMark}>
        <Text style={styles.productMarkText}>{product.purityKarat}K</Text>
      </View>
      <View style={styles.productBody}>
        <Text style={styles.productTitle}>{product.name}</Text>
        <Text style={styles.productDescription}>{product.description}</Text>
        <Text style={styles.productMeta}>
          وزن {formatGram(product.weightGram)} | اجرت {product.makingFeePercent}% + {formatRial(product.makingFeePerGram)}
        </Text>
      </View>
      <View style={styles.stepper}>
        <Pressable accessibilityRole="button" onPress={onDecrease} style={styles.stepperButton}>
          <Text style={styles.stepperText}>-</Text>
        </Pressable>
        <Text style={styles.quantity}>{quantity}</Text>
        <Pressable accessibilityRole="button" onPress={onIncrease} style={styles.stepperButton}>
          <Text style={styles.stepperText}>+</Text>
        </Pressable>
      </View>
    </View>
  );
}

function SummaryRow({ label, value, emphasized = false }: { label: string; value: string; emphasized?: boolean }): JSX.Element {
  return (
    <View style={[styles.summaryRow, emphasized && styles.summaryRowEmphasized]}>
      <Text style={[styles.summaryLabel, emphasized && styles.summaryLabelEmphasized]}>{label}</Text>
      <Text style={[styles.summaryValue, emphasized && styles.summaryValueEmphasized]}>{value}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  safeArea: {
    backgroundColor: colors.background,
    flex: 1
  },
  container: {
    gap: 18,
    padding: 18,
    paddingBottom: 36
  },
  hero: {
    backgroundColor: colors.primaryDark,
    borderRadius: 28,
    gap: 10,
    padding: 24
  },
  badge: {
    alignSelf: "flex-start",
    backgroundColor: colors.accent,
    borderRadius: 999,
    color: colors.primaryDark,
    fontSize: 13,
    fontWeight: "700",
    paddingHorizontal: 14,
    paddingVertical: 7
  },
  title: {
    color: "#fff7e3",
    fontSize: 30,
    fontWeight: "900",
    textAlign: "right"
  },
  subtitle: {
    color: "#f2dfb8",
    fontSize: 15,
    lineHeight: 25,
    textAlign: "right"
  },
  panel: {
    backgroundColor: colors.card,
    borderColor: colors.border,
    borderRadius: 24,
    borderWidth: 1,
    gap: 14,
    padding: 18
  },
  sectionTitle: {
    color: colors.primaryDark,
    fontSize: 20,
    fontWeight: "900",
    textAlign: "right"
  },
  inputGrid: {
    gap: 12
  },
  inputGroup: {
    gap: 8
  },
  inputLabel: {
    color: colors.muted,
    fontSize: 13,
    fontWeight: "700",
    textAlign: "right"
  },
  input: {
    backgroundColor: "#fffaf0",
    borderColor: colors.border,
    borderRadius: 16,
    borderWidth: 1,
    color: colors.primaryDark,
    fontSize: 16,
    paddingHorizontal: 14,
    paddingVertical: 12,
    textAlign: "right"
  },
  productCard: {
    alignItems: "center",
    backgroundColor: "#fffaf0",
    borderColor: colors.border,
    borderRadius: 20,
    borderWidth: 1,
    flexDirection: "row-reverse",
    gap: 12,
    padding: 12
  },
  productMark: {
    alignItems: "center",
    backgroundColor: colors.primary,
    borderRadius: 18,
    height: 58,
    justifyContent: "center",
    width: 58
  },
  productMarkText: {
    color: "#fff8df",
    fontSize: 16,
    fontWeight: "900"
  },
  productBody: {
    flex: 1,
    gap: 5
  },
  productTitle: {
    color: colors.primaryDark,
    fontSize: 16,
    fontWeight: "900",
    textAlign: "right"
  },
  productDescription: {
    color: colors.muted,
    fontSize: 13,
    lineHeight: 20,
    textAlign: "right"
  },
  productMeta: {
    color: colors.primary,
    fontSize: 12,
    fontWeight: "700",
    textAlign: "right"
  },
  stepper: {
    alignItems: "center",
    gap: 8
  },
  stepperButton: {
    alignItems: "center",
    backgroundColor: colors.accent,
    borderRadius: 12,
    height: 32,
    justifyContent: "center",
    width: 32
  },
  stepperText: {
    color: colors.primaryDark,
    fontSize: 20,
    fontWeight: "900"
  },
  quantity: {
    color: colors.primaryDark,
    fontSize: 16,
    fontWeight: "900"
  },
  invoiceCard: {
    backgroundColor: colors.primaryDark,
    borderRadius: 28,
    gap: 14,
    padding: 18
  },
  invoiceHeader: {
    alignItems: "flex-start",
    flexDirection: "row-reverse",
    justifyContent: "space-between",
    gap: 12
  },
  invoiceLabel: {
    color: colors.accent,
    fontSize: 14,
    fontWeight: "800",
    textAlign: "right"
  },
  invoiceNumber: {
    color: "#fff7e3",
    fontSize: 13,
    marginTop: 4,
    textAlign: "right"
  },
  payable: {
    color: "#fff7e3",
    flexShrink: 1,
    fontSize: 20,
    fontWeight: "900",
    textAlign: "left"
  },
  emptyCart: {
    backgroundColor: "rgba(255,255,255,0.08)",
    borderRadius: 16,
    color: "#fff7e3",
    padding: 14,
    textAlign: "right"
  },
  invoiceLine: {
    backgroundColor: "rgba(255,255,255,0.08)",
    borderRadius: 18,
    gap: 6,
    padding: 14
  },
  lineHeading: {
    alignItems: "center",
    flexDirection: "row-reverse",
    justifyContent: "space-between"
  },
  lineName: {
    color: "#fff7e3",
    fontSize: 15,
    fontWeight: "900",
    textAlign: "right"
  },
  lineQty: {
    color: colors.accent,
    fontSize: 14,
    fontWeight: "900"
  },
  lineMeta: {
    color: "#e9d4a8",
    fontSize: 12,
    lineHeight: 19,
    textAlign: "right"
  },
  lineTotal: {
    color: colors.accent,
    fontSize: 15,
    fontWeight: "900",
    textAlign: "left"
  },
  summaryRow: {
    alignItems: "center",
    borderTopColor: "rgba(255,255,255,0.14)",
    borderTopWidth: 1,
    flexDirection: "row-reverse",
    justifyContent: "space-between",
    paddingTop: 12
  },
  summaryRowEmphasized: {
    backgroundColor: "rgba(244,210,124,0.12)",
    borderRadius: 16,
    borderTopWidth: 0,
    padding: 12
  },
  summaryLabel: {
    color: "#e9d4a8",
    fontSize: 14,
    fontWeight: "700"
  },
  summaryLabelEmphasized: {
    color: "#fff7e3",
    fontSize: 16,
    fontWeight: "900"
  },
  summaryValue: {
    color: "#fff7e3",
    fontSize: 14,
    fontWeight: "800"
  },
  summaryValueEmphasized: {
    color: colors.accent,
    fontSize: 18,
    fontWeight: "900"
  },
  primaryButton: {
    alignItems: "center",
    backgroundColor: colors.accent,
    borderRadius: 18,
    paddingVertical: 15
  },
  disabledButton: {
    opacity: 0.55
  },
  pressedButton: {
    transform: [{ scale: 0.99 }]
  },
  primaryButtonText: {
    color: colors.primaryDark,
    fontSize: 16,
    fontWeight: "900"
  }
});

export default App;
