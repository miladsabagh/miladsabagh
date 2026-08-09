import React, { useMemo, useState } from 'react';
import { FlatList, Pressable, StyleSheet, View } from 'react-native';
import { colors, radius, spacing } from '../theme';
import { useApp } from '../context/AppContext';
import { Card, FaText, Field } from '../components/ui';
import { toFa, toman, toEn } from '../utils/format';
import { formatJalaliShort, formatTime } from '../utils/jalali';
import { Invoice } from '../types';

export function InvoicesScreen({
  onOpenInvoice,
}: {
  onOpenInvoice: (inv: Invoice) => void;
}) {
  const { invoices } = useApp();
  const [query, setQuery] = useState('');

  const filtered = useMemo(() => {
    const q = toEn(query.trim());
    if (!q) return invoices;
    return invoices.filter(
      (inv) =>
        inv.customerName.includes(query.trim()) ||
        String(inv.number).includes(q) ||
        toEn(inv.customerPhone).includes(q),
    );
  }, [invoices, query]);

  const total = filtered.reduce((a, b) => a + b.grandTotal, 0);

  return (
    <View style={styles.container}>
      <View style={styles.searchWrap}>
        <Field
          label="جستجو"
          value={query}
          onChangeText={setQuery}
          placeholder="نام مشتری یا شماره فاکتور"
        />
        <View style={styles.summary}>
          <FaText style={styles.summaryVal}>{toman(total)}</FaText>
          <FaText style={styles.summaryLabel}>
            جمع {toFa(filtered.length)} فاکتور
          </FaText>
        </View>
      </View>

      <FlatList
        data={filtered}
        keyExtractor={(i) => i.id}
        contentContainerStyle={styles.list}
        showsVerticalScrollIndicator={false}
        renderItem={({ item }) => (
          <Pressable onPress={() => onOpenInvoice(item)}>
            <Card style={styles.row}>
              <View style={styles.numBox}>
                <FaText style={styles.numLabel}>#</FaText>
                <FaText style={styles.numText}>{toFa(item.number)}</FaText>
              </View>
              <View style={{ flex: 1 }}>
                <FaText style={styles.customer}>{item.customerName || 'مشتری نقدی'}</FaText>
                <FaText style={styles.meta}>
                  {formatJalaliShort(item.createdAt)} · ساعت {formatTime(item.createdAt)}
                </FaText>
                <FaText style={styles.metaItems}>
                  {toFa(item.items.length)} قلم · {toFa(item.items.reduce((a, b) => a + b.quantity, 0))} عدد
                </FaText>
              </View>
              <View style={styles.amountBox}>
                <FaText style={styles.amount}>{toman(item.grandTotal)}</FaText>
                <FaText style={styles.chevron}>مشاهده ›</FaText>
              </View>
            </Card>
          </Pressable>
        )}
        ListEmptyComponent={
          <Card style={styles.empty}>
            <FaText style={styles.emptyIcon}>🧾</FaText>
            <FaText style={styles.emptyText}>فاکتوری یافت نشد</FaText>
          </Card>
        }
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  searchWrap: { paddingHorizontal: spacing.lg },
  summary: {
    flexDirection: 'row-reverse',
    justifyContent: 'space-between',
    alignItems: 'center',
    backgroundColor: colors.card,
    borderRadius: radius.md,
    borderWidth: 1,
    borderColor: colors.border,
    padding: spacing.md,
    marginBottom: spacing.md,
  },
  summaryVal: { color: colors.gold, fontSize: 16, fontWeight: '800' },
  summaryLabel: { color: colors.textMuted, fontSize: 13 },
  list: { paddingHorizontal: spacing.lg, gap: spacing.md, paddingBottom: spacing.xxl },
  row: { flexDirection: 'row-reverse', alignItems: 'center', gap: spacing.md },
  numBox: {
    width: 54,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: colors.surfaceAlt,
    borderRadius: radius.md,
    paddingVertical: spacing.sm,
  },
  numLabel: { color: colors.textDim, fontSize: 12 },
  numText: { color: colors.goldSoft, fontSize: 15, fontWeight: '800' },
  customer: { color: colors.text, fontSize: 15, fontWeight: '700' },
  meta: { color: colors.textMuted, fontSize: 12, marginTop: 3 },
  metaItems: { color: colors.textDim, fontSize: 11, marginTop: 2 },
  amountBox: { alignItems: 'flex-start' },
  amount: { color: colors.gold, fontSize: 15, fontWeight: '800' },
  chevron: { color: colors.textMuted, fontSize: 12, marginTop: 4 },
  empty: { alignItems: 'center', paddingVertical: spacing.xxl },
  emptyIcon: { fontSize: 34, marginBottom: spacing.sm },
  emptyText: { color: colors.textMuted, fontSize: 14 },
});
