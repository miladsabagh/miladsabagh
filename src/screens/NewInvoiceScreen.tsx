import React, { useMemo, useState } from 'react';
import { Pressable, ScrollView, StyleSheet, View } from 'react-native';
import { colors, radius, spacing } from '../theme';
import { useApp } from '../context/AppContext';
import { Button, Card, FaText, Field } from '../components/ui';
import { toFa, toman, weightFa, toEn } from '../utils/format';
import { CATEGORY_LABELS, Invoice } from '../types';
import { computeInvoice, DraftLine } from '../utils/calc';

export function NewInvoiceScreen({
  onCreated,
}: {
  onCreated: (inv: Invoice) => void;
}) {
  const { products, settings, addInvoice } = useApp();
  const [customerName, setCustomerName] = useState('');
  const [customerPhone, setCustomerPhone] = useState('');
  const [discountText, setDiscountText] = useState('');
  const [qty, setQty] = useState<Record<string, number>>({});
  const [saving, setSaving] = useState(false);

  const discount = Number(toEn(discountText).replace(/[^0-9]/g, '')) || 0;

  const lines: DraftLine[] = useMemo(
    () =>
      products
        .map((p) => ({ product: p, quantity: qty[p.id] || 0 }))
        .filter((l) => l.quantity > 0),
    [products, qty],
  );

  const totals = useMemo(
    () => computeInvoice(lines, settings, discount),
    [lines, settings, discount],
  );

  const itemCount = lines.reduce((a, b) => a + b.quantity, 0);

  function setQ(id: string, delta: number) {
    setQty((prev) => {
      const next = Math.max(0, (prev[id] || 0) + delta);
      return { ...prev, [id]: next };
    });
  }

  async function submit() {
    if (lines.length === 0) return;
    setSaving(true);
    try {
      const inv = await addInvoice({
        customerName: customerName.trim(),
        customerPhone: customerPhone.trim(),
        goldPricePerGram18: settings.pricePerGram18,
        items: totals.items,
        discount,
        subtotalGold: totals.subtotalGold,
        totalWage: totals.totalWage,
        totalProfit: totals.totalProfit,
        totalTax: totals.totalTax,
        grandTotal: totals.grandTotal,
      });
      setCustomerName('');
      setCustomerPhone('');
      setDiscountText('');
      setQty({});
      onCreated(inv);
    } finally {
      setSaving(false);
    }
  }

  return (
    <View style={styles.container}>
      <ScrollView
        contentContainerStyle={styles.content}
        showsVerticalScrollIndicator={false}
        keyboardShouldPersistTaps="handled"
      >
        <Card>
          <FaText style={styles.sectionTitle}>مشخصات مشتری</FaText>
          <Field
            label="نام و نام خانوادگی"
            value={customerName}
            onChangeText={setCustomerName}
            placeholder="مشتری نقدی"
          />
          <Field
            label="شماره تماس"
            keyboardType="phone-pad"
            value={customerPhone}
            onChangeText={setCustomerPhone}
            placeholder="۰۹۱۲۰۰۰۰۰۰۰"
          />
        </Card>

        <View style={styles.sectionHead}>
          <FaText style={styles.countBadge}>
            {toFa(itemCount)} قلم انتخاب شده
          </FaText>
          <FaText style={styles.sectionTitle}>انتخاب کالا</FaText>
        </View>

        {products.map((p) => {
          const q = qty[p.id] || 0;
          const active = q > 0;
          return (
            <Card key={p.id} style={[styles.itemCard, active && styles.itemCardActive]}>
              <View style={styles.stepper}>
                <Pressable
                  style={[styles.stepBtn, styles.stepPlus]}
                  onPress={() => setQ(p.id, 1)}
                >
                  <FaText style={styles.stepPlusText}>＋</FaText>
                </Pressable>
                <FaText style={styles.stepQty}>{toFa(q)}</FaText>
                <Pressable
                  style={[styles.stepBtn, q === 0 && styles.stepBtnDisabled]}
                  onPress={() => setQ(p.id, -1)}
                >
                  <FaText style={styles.stepMinusText}>−</FaText>
                </Pressable>
              </View>
              <View style={{ flex: 1 }}>
                <FaText style={styles.itemName} numberOfLines={1}>{p.name}</FaText>
                <FaText style={styles.itemMeta}>
                  {CATEGORY_LABELS[p.category]} · {weightFa(p.weight)} · عیار {toFa(p.karat)}
                </FaText>
                <FaText style={styles.itemWage}>اجرت ٪{toFa(p.wagePercent)}</FaText>
              </View>
            </Card>
          );
        })}

        <Card style={{ marginTop: spacing.sm }}>
          <Field
            label="تخفیف (تومان)"
            keyboardType="numeric"
            value={discountText ? toFa(discountText.replace(/[^0-9]/g, '')) : ''}
            onChangeText={setDiscountText}
            placeholder="۰"
          />
        </Card>
        <View style={{ height: 220 }} />
      </ScrollView>

      <View style={styles.summaryBar}>
        <View style={styles.sumRow}>
          <FaText style={styles.sumVal}>{toman(totals.subtotalGold)}</FaText>
          <FaText style={styles.sumLabel}>ارزش طلا</FaText>
        </View>
        <View style={styles.sumRow}>
          <FaText style={styles.sumVal}>{toman(totals.totalWage)}</FaText>
          <FaText style={styles.sumLabel}>اجرت</FaText>
        </View>
        <View style={styles.sumRow}>
          <FaText style={styles.sumVal}>{toman(totals.totalTax)}</FaText>
          <FaText style={styles.sumLabel}>مالیات</FaText>
        </View>
        <View style={styles.grandRow}>
          <FaText style={styles.grandVal}>{toman(totals.grandTotal)}</FaText>
          <FaText style={styles.grandLabel}>مبلغ نهایی</FaText>
        </View>
        <Button
          title="صدور فاکتور"
          icon="🧾"
          onPress={submit}
          loading={saving}
          disabled={lines.length === 0}
        />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  content: { padding: spacing.lg, gap: spacing.md },
  sectionTitle: { color: colors.text, fontSize: 16, fontWeight: '800', marginBottom: spacing.md },
  sectionHead: {
    flexDirection: 'row-reverse',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginTop: spacing.sm,
  },
  countBadge: {
    color: colors.gold,
    fontSize: 12,
    fontWeight: '700',
    backgroundColor: 'rgba(233,185,73,0.12)',
    paddingVertical: 4,
    paddingHorizontal: 10,
    borderRadius: radius.pill,
  },
  itemCard: { flexDirection: 'row-reverse', alignItems: 'center', gap: spacing.md, paddingVertical: spacing.md },
  itemCardActive: { borderColor: colors.gold },
  itemName: { color: colors.text, fontSize: 15, fontWeight: '700' },
  itemMeta: { color: colors.textMuted, fontSize: 12, marginTop: 3 },
  itemWage: { color: colors.textDim, fontSize: 11, marginTop: 2 },
  stepper: {
    flexDirection: 'row-reverse',
    alignItems: 'center',
    gap: spacing.sm,
    backgroundColor: colors.surfaceAlt,
    borderRadius: radius.pill,
    padding: 4,
  },
  stepBtn: {
    width: 34,
    height: 34,
    borderRadius: 17,
    backgroundColor: colors.surface,
    alignItems: 'center',
    justifyContent: 'center',
  },
  stepBtnDisabled: { opacity: 0.4 },
  stepPlus: { backgroundColor: colors.gold },
  stepPlusText: { color: '#1a1400', fontSize: 18, fontWeight: '800', lineHeight: 20 },
  stepMinusText: { color: colors.text, fontSize: 20, fontWeight: '800', lineHeight: 22 },
  stepQty: { color: colors.text, fontSize: 16, fontWeight: '800', minWidth: 22, textAlign: 'center' },
  summaryBar: {
    position: 'absolute',
    bottom: 0,
    left: 0,
    right: 0,
    backgroundColor: colors.surface,
    borderTopLeftRadius: radius.xl,
    borderTopRightRadius: radius.xl,
    borderTopWidth: 1,
    borderColor: colors.border,
    padding: spacing.lg,
    gap: 6,
  },
  sumRow: { flexDirection: 'row-reverse', justifyContent: 'space-between' },
  sumLabel: { color: colors.textMuted, fontSize: 12 },
  sumVal: { color: colors.text, fontSize: 12, fontWeight: '600' },
  grandRow: {
    flexDirection: 'row-reverse',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginTop: spacing.sm,
    marginBottom: spacing.sm,
  },
  grandLabel: { color: colors.gold, fontSize: 15, fontWeight: '800' },
  grandVal: { color: colors.gold, fontSize: 20, fontWeight: '800' },
});
