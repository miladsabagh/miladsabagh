import React, { useMemo } from 'react';
import { Pressable, ScrollView, StyleSheet, View } from 'react-native';
import { colors, radius, spacing } from '../theme';
import { useApp } from '../context/AppContext';
import { Card, FaText } from '../components/ui';
import { toman, toFa } from '../utils/format';
import { formatJalaliShort } from '../utils/jalali';
import { Invoice } from '../types';

function StatCard({
  label,
  value,
  sub,
  accent,
}: {
  label: string;
  value: string;
  sub?: string;
  accent: string;
}) {
  return (
    <View style={[styles.stat, { borderColor: accent + '55' }]}>
      <View style={[styles.statDot, { backgroundColor: accent }]} />
      <FaText style={styles.statValue}>{value}</FaText>
      <FaText style={styles.statLabel}>{label}</FaText>
      {sub ? <FaText style={styles.statSub}>{sub}</FaText> : null}
    </View>
  );
}

export function DashboardScreen({
  onNewInvoice,
  onOpenInvoice,
  onSeeAll,
}: {
  onNewInvoice: () => void;
  onOpenInvoice: (inv: Invoice) => void;
  onSeeAll: () => void;
}) {
  const { settings, products, invoices } = useApp();

  const totalSales = useMemo(
    () => invoices.reduce((a, b) => a + b.grandTotal, 0),
    [invoices],
  );
  const todayStr = formatJalaliShort(Date.now());
  const recent = invoices.slice(0, 4);

  return (
    <ScrollView
      contentContainerStyle={styles.content}
      showsVerticalScrollIndicator={false}
    >
      <View style={styles.priceCard}>
        <View style={styles.priceHeaderRow}>
          <View style={styles.livePill}>
            <View style={styles.liveDot} />
            <FaText style={styles.liveText}>قیمت امروز</FaText>
          </View>
          <FaText style={styles.priceDate}>{todayStr}</FaText>
        </View>
        <FaText style={styles.priceLabel}>هر گرم طلای ۱۸ عیار</FaText>
        <FaText style={styles.priceValue}>{toman(settings.pricePerGram18)}</FaText>
        <View style={styles.priceMetaRow}>
          <FaText style={styles.priceMeta}>سود فروش: ٪{toFa(settings.profitPercent)}</FaText>
          <FaText style={styles.priceMeta}>مالیات: ٪{toFa(settings.taxPercent)}</FaText>
        </View>
      </View>

      <Pressable onPress={onNewInvoice} style={styles.cta}>
        <FaText style={styles.ctaIcon}>＋</FaText>
        <View style={{ flex: 1 }}>
          <FaText style={styles.ctaTitle}>صدور فاکتور جدید</FaText>
          <FaText style={styles.ctaSub}>انتخاب کالا، محاسبه خودکار و صدور فاکتور</FaText>
        </View>
      </Pressable>

      <View style={styles.statsGrid}>
        <StatCard
          label="فروش کل"
          value={toman(totalSales)}
          accent={colors.gold}
        />
        <StatCard
          label="تعداد فاکتور"
          value={toFa(invoices.length)}
          sub="ثبت شده"
          accent={colors.green}
        />
        <StatCard
          label="کالاها"
          value={toFa(products.length)}
          sub="در انبار"
          accent={colors.blue}
        />
        <StatCard
          label="موجودی کل"
          value={toFa(products.reduce((a, b) => a + b.stock, 0))}
          sub="قطعه"
          accent={colors.goldSoft}
        />
      </View>

      <View style={styles.sectionHead}>
        <Pressable onPress={onSeeAll}>
          <FaText style={styles.sectionLink}>همه</FaText>
        </Pressable>
        <FaText style={styles.sectionTitle}>فاکتورهای اخیر</FaText>
      </View>

      {recent.length === 0 ? (
        <Card style={styles.empty}>
          <FaText style={styles.emptyIcon}>🧾</FaText>
          <FaText style={styles.emptyText}>هنوز فاکتوری صادر نشده است</FaText>
          <FaText style={styles.emptySub}>برای شروع، فاکتور جدید صادر کنید</FaText>
        </Card>
      ) : (
        recent.map((inv) => (
          <Pressable key={inv.id} onPress={() => onOpenInvoice(inv)}>
            <Card style={styles.invRow}>
              <View style={styles.invAmountBox}>
                <FaText style={styles.invAmount}>{toman(inv.grandTotal)}</FaText>
                <FaText style={styles.invDate}>{formatJalaliShort(inv.createdAt)}</FaText>
              </View>
              <View style={{ flex: 1 }}>
                <FaText style={styles.invCustomer}>
                  {inv.customerName || 'مشتری نقدی'}
                </FaText>
                <FaText style={styles.invMeta}>
                  فاکتور #{toFa(inv.number)} · {toFa(inv.items.length)} قلم کالا
                </FaText>
              </View>
            </Card>
          </Pressable>
        ))
      )}
      <View style={{ height: spacing.xxl }} />
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  content: { padding: spacing.lg, gap: spacing.md },
  priceCard: {
    backgroundColor: '#1a1400',
    borderRadius: radius.xl,
    padding: spacing.xl,
    borderWidth: 1,
    borderColor: colors.goldDeep,
  },
  priceHeaderRow: {
    flexDirection: 'row-reverse',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: spacing.md,
  },
  livePill: {
    flexDirection: 'row-reverse',
    alignItems: 'center',
    gap: 6,
    backgroundColor: 'rgba(233,185,73,0.15)',
    paddingVertical: 4,
    paddingHorizontal: 10,
    borderRadius: radius.pill,
  },
  liveDot: { width: 7, height: 7, borderRadius: 4, backgroundColor: colors.green },
  liveText: { color: colors.goldSoft, fontSize: 12, fontWeight: '600' },
  priceDate: { color: colors.textMuted, fontSize: 12 },
  priceLabel: { color: colors.goldSoft, fontSize: 14 },
  priceValue: { color: colors.gold, fontSize: 30, fontWeight: '800', marginTop: 4 },
  priceMetaRow: {
    flexDirection: 'row-reverse',
    gap: spacing.lg,
    marginTop: spacing.md,
  },
  priceMeta: { color: colors.textMuted, fontSize: 12 },
  cta: {
    flexDirection: 'row-reverse',
    alignItems: 'center',
    gap: spacing.md,
    backgroundColor: colors.gold,
    borderRadius: radius.lg,
    padding: spacing.lg,
  },
  ctaIcon: {
    fontSize: 26,
    fontWeight: '800',
    color: '#1a1400',
    width: 44,
    height: 44,
    lineHeight: 44,
    textAlign: 'center',
    backgroundColor: 'rgba(0,0,0,0.12)',
    borderRadius: radius.md,
  },
  ctaTitle: { color: '#1a1400', fontSize: 17, fontWeight: '800' },
  ctaSub: { color: '#3a3000', fontSize: 12, marginTop: 2 },
  statsGrid: {
    flexDirection: 'row-reverse',
    flexWrap: 'wrap',
    gap: spacing.md,
  },
  stat: {
    width: '47%',
    flexGrow: 1,
    backgroundColor: colors.card,
    borderRadius: radius.lg,
    borderWidth: 1,
    padding: spacing.lg,
  },
  statDot: { width: 10, height: 10, borderRadius: 5, marginBottom: spacing.sm },
  statValue: { color: colors.text, fontSize: 18, fontWeight: '800' },
  statLabel: { color: colors.textMuted, fontSize: 13, marginTop: 4 },
  statSub: { color: colors.textDim, fontSize: 11, marginTop: 2 },
  sectionHead: {
    flexDirection: 'row-reverse',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginTop: spacing.sm,
  },
  sectionTitle: { color: colors.text, fontSize: 17, fontWeight: '800' },
  sectionLink: { color: colors.gold, fontSize: 14, fontWeight: '600' },
  empty: { alignItems: 'center', paddingVertical: spacing.xl },
  emptyIcon: { fontSize: 34, marginBottom: spacing.sm },
  emptyText: { color: colors.text, fontSize: 15, fontWeight: '600' },
  emptySub: { color: colors.textMuted, fontSize: 12, marginTop: 4 },
  invRow: {
    flexDirection: 'row-reverse',
    alignItems: 'center',
    gap: spacing.md,
  },
  invAmountBox: { alignItems: 'flex-start' },
  invAmount: { color: colors.gold, fontSize: 15, fontWeight: '800' },
  invDate: { color: colors.textDim, fontSize: 11, marginTop: 2 },
  invCustomer: { color: colors.text, fontSize: 15, fontWeight: '700' },
  invMeta: { color: colors.textMuted, fontSize: 12, marginTop: 3 },
});
