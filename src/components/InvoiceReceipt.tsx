import React from 'react';
import { ScrollView, StyleSheet, View } from 'react-native';
import { colors, radius, spacing } from '../theme';
import { Invoice, Settings, CATEGORY_LABELS } from '../types';
import { FaText } from './ui';
import { toman, toFa, weightFa, faNumber } from '../utils/format';
import { formatJalaliDate, formatTime } from '../utils/jalali';

function Row({ label, value, strong }: { label: string; value: string; strong?: boolean }) {
  return (
    <View style={styles.row}>
      <FaText style={[styles.rowVal, strong && styles.strong]}>{value}</FaText>
      <FaText style={[styles.rowLabel, strong && styles.strong]}>{label}</FaText>
    </View>
  );
}

export function InvoiceReceipt({
  invoice,
  settings,
}: {
  invoice: Invoice;
  settings: Settings;
}) {
  return (
    <ScrollView
      style={styles.scroll}
      contentContainerStyle={styles.content}
      showsVerticalScrollIndicator={false}
    >
      <View style={styles.paper}>
        <View style={styles.header}>
          <View style={styles.logo}>
            <FaText style={styles.logoText}>۲۴</FaText>
          </View>
          <FaText style={styles.shopName}>{settings.shopName}</FaText>
          <FaText style={styles.shopInfo}>{settings.shopAddress}</FaText>
          <FaText style={styles.shopInfo}>تلفن: {toFa(settings.shopPhone)}</FaText>
        </View>

        <View style={styles.metaBar}>
          <View>
            <FaText style={styles.metaLabel}>شماره فاکتور</FaText>
            <FaText style={styles.metaValue}>{toFa(invoice.number)}</FaText>
          </View>
          <View style={styles.metaCol}>
            <FaText style={styles.metaLabel}>تاریخ</FaText>
            <FaText style={styles.metaValue}>{formatJalaliDate(invoice.createdAt)}</FaText>
            <FaText style={styles.metaTime}>ساعت {formatTime(invoice.createdAt)}</FaText>
          </View>
        </View>

        <View style={styles.customerBox}>
          <FaText style={styles.customerName}>
            خریدار: {invoice.customerName || 'مشتری نقدی'}
          </FaText>
          {invoice.customerPhone ? (
            <FaText style={styles.customerPhone}>تلفن: {toFa(invoice.customerPhone)}</FaText>
          ) : null}
        </View>

        <View style={styles.priceNote}>
          <FaText style={styles.priceNoteText}>
            قیمت هر گرم طلای ۱۸ عیار: {toman(invoice.goldPricePerGram18)}
          </FaText>
        </View>

        <View style={styles.tableHead}>
          <FaText style={[styles.th, styles.thTotal]}>مبلغ</FaText>
          <FaText style={[styles.th, styles.thWage]}>اجرت</FaText>
          <FaText style={[styles.th, styles.thWeight]}>وزن</FaText>
          <FaText style={[styles.th, styles.thName]}>شرح کالا</FaText>
        </View>

        {invoice.items.map((it, idx) => (
          <View key={idx} style={styles.tableRow}>
            <FaText style={[styles.td, styles.thTotal]}>{faNumber(it.lineTotal)}</FaText>
            <FaText style={[styles.td, styles.thWage]}>٪{toFa(it.wagePercent)}</FaText>
            <FaText style={[styles.td, styles.thWeight]}>
              {toFa((it.weight * it.quantity).toFixed(3).replace(/0+$/, '').replace(/\.$/, ''))}
            </FaText>
            <View style={styles.thName}>
              <FaText style={styles.tdName}>{it.name}</FaText>
              <FaText style={styles.tdSub}>
                {CATEGORY_LABELS[it.category]} · عیار {toFa(it.karat)} · تعداد {toFa(it.quantity)}
              </FaText>
            </View>
          </View>
        ))}

        <View style={styles.totals}>
          <Row label="جمع ارزش طلا" value={toman(invoice.subtotalGold)} />
          <Row label="جمع اجرت" value={toman(invoice.totalWage)} />
          <Row label="سود فروشنده" value={toman(invoice.totalProfit)} />
          <Row
            label={`مالیات بر ارزش افزوده (${toFa(settings.taxPercent)}٪)`}
            value={toman(invoice.totalTax)}
          />
          {invoice.discount > 0 ? (
            <Row label="تخفیف" value={'-' + toman(invoice.discount)} />
          ) : null}
          <View style={styles.grandRow}>
            <FaText style={styles.grandVal}>{toman(invoice.grandTotal)}</FaText>
            <FaText style={styles.grandLabel}>مبلغ قابل پرداخت</FaText>
          </View>
        </View>

        <View style={styles.footer}>
          <FaText style={styles.footerText}>
            از خرید شما سپاسگزاریم — این فاکتور به منزله رسید رسمی فروش است.
          </FaText>
        </View>
      </View>
      <View style={{ height: spacing.xxl }} />
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  scroll: { flex: 1 },
  content: { padding: spacing.lg },
  paper: {
    backgroundColor: '#fbfaf6',
    borderRadius: radius.lg,
    padding: spacing.lg,
    borderWidth: 1,
    borderColor: '#e6e1d5',
  },
  header: { alignItems: 'center', marginBottom: spacing.md },
  logo: {
    width: 52,
    height: 52,
    borderRadius: 26,
    backgroundColor: colors.gold,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: spacing.sm,
  },
  logoText: { color: '#1a1400', fontWeight: '800', fontSize: 20 },
  shopName: { color: '#1a1400', fontSize: 19, fontWeight: '800', textAlign: 'center' },
  shopInfo: { color: '#6b6350', fontSize: 12, textAlign: 'center', marginTop: 2 },
  metaBar: {
    flexDirection: 'row-reverse',
    justifyContent: 'space-between',
    backgroundColor: '#f1ede1',
    borderRadius: radius.md,
    padding: spacing.md,
    marginVertical: spacing.md,
  },
  metaCol: { alignItems: 'flex-start' },
  metaLabel: { color: '#8a806a', fontSize: 11, textAlign: 'right' },
  metaValue: { color: '#1a1400', fontSize: 15, fontWeight: '700', marginTop: 2, textAlign: 'right' },
  metaTime: { color: '#8a806a', fontSize: 11, textAlign: 'right', marginTop: 2 },
  customerBox: { marginBottom: spacing.sm },
  customerName: { color: '#1a1400', fontSize: 15, fontWeight: '700' },
  customerPhone: { color: '#6b6350', fontSize: 12, marginTop: 2 },
  priceNote: {
    backgroundColor: '#fff6e0',
    borderRadius: radius.sm,
    paddingVertical: spacing.sm,
    paddingHorizontal: spacing.md,
    marginBottom: spacing.md,
    borderWidth: 1,
    borderColor: '#f0e2b8',
  },
  priceNoteText: { color: '#8a6d00', fontSize: 12, fontWeight: '600' },
  tableHead: {
    flexDirection: 'row-reverse',
    backgroundColor: '#1a1400',
    borderTopLeftRadius: radius.sm,
    borderTopRightRadius: radius.sm,
    paddingVertical: spacing.sm,
    paddingHorizontal: spacing.sm,
  },
  th: { color: colors.goldSoft, fontSize: 12, fontWeight: '700' },
  td: { color: '#1a1400', fontSize: 12.5 },
  thName: { flex: 1, paddingHorizontal: 4 },
  thWeight: { width: 58, textAlign: 'center' },
  thWage: { width: 46, textAlign: 'center' },
  thTotal: { width: 84, textAlign: 'left' },
  tableRow: {
    flexDirection: 'row-reverse',
    alignItems: 'center',
    paddingVertical: spacing.sm,
    paddingHorizontal: spacing.sm,
    borderBottomWidth: 1,
    borderBottomColor: '#eae5d8',
  },
  tdName: { color: '#1a1400', fontSize: 13.5, fontWeight: '700' },
  tdSub: { color: '#8a806a', fontSize: 10.5, marginTop: 2 },
  totals: { marginTop: spacing.md },
  row: {
    flexDirection: 'row-reverse',
    justifyContent: 'space-between',
    paddingVertical: 5,
  },
  rowLabel: { color: '#6b6350', fontSize: 13 },
  rowVal: { color: '#1a1400', fontSize: 13, fontWeight: '600' },
  strong: { fontWeight: '800', color: '#1a1400' },
  grandRow: {
    flexDirection: 'row-reverse',
    justifyContent: 'space-between',
    alignItems: 'center',
    backgroundColor: colors.gold,
    borderRadius: radius.md,
    paddingVertical: spacing.md,
    paddingHorizontal: spacing.md,
    marginTop: spacing.sm,
  },
  grandLabel: { color: '#1a1400', fontSize: 14, fontWeight: '800' },
  grandVal: { color: '#1a1400', fontSize: 17, fontWeight: '800' },
  footer: {
    marginTop: spacing.lg,
    borderTopWidth: 1,
    borderTopColor: '#e6e1d5',
    paddingTop: spacing.md,
  },
  footerText: { color: '#8a806a', fontSize: 11, textAlign: 'center' },
});
