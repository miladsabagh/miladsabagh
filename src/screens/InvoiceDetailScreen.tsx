import React from 'react';
import { Alert, Platform, Pressable, StyleSheet, View } from 'react-native';
import { colors, radius, spacing } from '../theme';
import { useApp } from '../context/AppContext';
import { Button, FaText } from '../components/ui';
import { InvoiceReceipt } from '../components/InvoiceReceipt';
import { Invoice } from '../types';
import { toFa } from '../utils/format';

export function InvoiceDetailScreen({
  invoice,
  onClose,
}: {
  invoice: Invoice;
  onClose: () => void;
}) {
  const { settings, deleteInvoice } = useApp();

  function confirmDelete() {
    if (Platform.OS === 'web') {
      // eslint-disable-next-line no-alert
      const ok = typeof window !== 'undefined' ? window.confirm('این فاکتور حذف شود؟') : true;
      if (ok) {
        void deleteInvoice(invoice.id);
        onClose();
      }
      return;
    }
    Alert.alert('حذف فاکتور', 'این فاکتور حذف شود؟', [
      { text: 'انصراف', style: 'cancel' },
      {
        text: 'حذف',
        style: 'destructive',
        onPress: () => {
          void deleteInvoice(invoice.id);
          onClose();
        },
      },
    ]);
  }

  return (
    <View style={styles.container}>
      <View style={styles.topbar}>
        <Pressable onPress={confirmDelete} hitSlop={10}>
          <FaText style={styles.deleteLink}>حذف</FaText>
        </Pressable>
        <FaText style={styles.title}>فاکتور #{toFa(invoice.number)}</FaText>
        <Pressable onPress={onClose} hitSlop={10} style={styles.closeBtn}>
          <FaText style={styles.closeText}>✕</FaText>
        </Pressable>
      </View>

      <InvoiceReceipt invoice={invoice} settings={settings} />

      <View style={styles.actions}>
        <Button title="بازگشت" variant="soft" onPress={onClose} style={{ flex: 1 }} />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.bg },
  topbar: {
    flexDirection: 'row-reverse',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: spacing.lg,
    paddingVertical: spacing.md,
    borderBottomWidth: 1,
    borderBottomColor: colors.border,
  },
  title: { color: colors.text, fontSize: 17, fontWeight: '800' },
  deleteLink: { color: colors.red, fontSize: 14, fontWeight: '600' },
  closeBtn: {
    width: 32,
    height: 32,
    borderRadius: 16,
    backgroundColor: colors.surfaceAlt,
    alignItems: 'center',
    justifyContent: 'center',
  },
  closeText: { color: colors.text, fontSize: 15, fontWeight: '700' },
  actions: {
    flexDirection: 'row-reverse',
    padding: spacing.lg,
    borderTopWidth: 1,
    borderTopColor: colors.border,
  },
});
