import React, { useEffect, useState } from 'react';
import {
  I18nManager,
  Modal,
  Platform,
  Pressable,
  SafeAreaView,
  StatusBar as RNStatusBar,
  StyleSheet,
  View,
} from 'react-native';
import { StatusBar } from 'expo-status-bar';
import { colors, spacing } from './src/theme';
import { AppProvider, useApp } from './src/context/AppContext';
import { FaText } from './src/components/ui';
import { DashboardScreen } from './src/screens/DashboardScreen';
import { ProductsScreen } from './src/screens/ProductsScreen';
import { NewInvoiceScreen } from './src/screens/NewInvoiceScreen';
import { InvoicesScreen } from './src/screens/InvoicesScreen';
import { SettingsScreen } from './src/screens/SettingsScreen';
import { InvoiceDetailScreen } from './src/screens/InvoiceDetailScreen';
import { Invoice } from './src/types';
import { toFa } from './src/utils/format';

type Tab = 'dashboard' | 'products' | 'invoice' | 'invoices' | 'settings';

const TABS: { key: Tab; label: string; icon: string }[] = [
  { key: 'dashboard', label: 'خانه', icon: '⌂' },
  { key: 'products', label: 'کالاها', icon: '💍' },
  { key: 'invoice', label: 'فاکتور', icon: '＋' },
  { key: 'invoices', label: 'فاکتورها', icon: '🧾' },
  { key: 'settings', label: 'تنظیمات', icon: '⚙' },
];

const TITLES: Record<Tab, string> = {
  dashboard: 'طلا و جواهر میلاد',
  products: 'مدیریت کالاها',
  invoice: 'صدور فاکتور',
  invoices: 'فاکتورها',
  settings: 'تنظیمات',
};

function Shell() {
  const { ready } = useApp();
  const [tab, setTab] = useState<Tab>('dashboard');
  const [viewing, setViewing] = useState<Invoice | null>(null);
  const [toast, setToast] = useState<string | null>(null);

  function openInvoice(inv: Invoice) {
    setViewing(inv);
  }

  function onCreated(inv: Invoice) {
    setToast(`فاکتور #${toFa(inv.number)} با موفقیت صادر شد`);
    setTimeout(() => setToast(null), 2500);
    setViewing(inv);
  }

  if (!ready) {
    return (
      <View style={styles.loading}>
        <FaText style={styles.loadingText}>در حال بارگذاری…</FaText>
      </View>
    );
  }

  return (
    <View style={styles.root}>
      <View style={styles.header}>
        <View style={styles.brand}>
          <View style={styles.logo}>
            <FaText style={styles.logoText}>۲۴</FaText>
          </View>
          <FaText style={styles.headerTitle}>{TITLES[tab]}</FaText>
        </View>
      </View>

      <View style={styles.body}>
        {tab === 'dashboard' && (
          <DashboardScreen
            onNewInvoice={() => setTab('invoice')}
            onOpenInvoice={openInvoice}
            onSeeAll={() => setTab('invoices')}
          />
        )}
        {tab === 'products' && <ProductsScreen />}
        {tab === 'invoice' && <NewInvoiceScreen onCreated={onCreated} />}
        {tab === 'invoices' && <InvoicesScreen onOpenInvoice={openInvoice} />}
        {tab === 'settings' && <SettingsScreen />}
      </View>

      {toast ? (
        <View style={styles.toast}>
          <FaText style={styles.toastText}>{toast}</FaText>
        </View>
      ) : null}

      <View style={styles.tabbar}>
        {TABS.map((t) => {
          const active = tab === t.key;
          const isPrimary = t.key === 'invoice';
          return (
            <Pressable
              key={t.key}
              style={styles.tabItem}
              onPress={() => setTab(t.key)}
            >
              <View
                style={[
                  styles.tabIconWrap,
                  isPrimary && styles.tabPrimary,
                  active && !isPrimary && styles.tabActiveWrap,
                ]}
              >
                <FaText
                  style={[
                    styles.tabIcon,
                    isPrimary && styles.tabPrimaryIcon,
                    active && !isPrimary && styles.tabIconActive,
                  ]}
                >
                  {t.icon}
                </FaText>
              </View>
              <FaText style={[styles.tabLabel, active && styles.tabLabelActive]}>
                {t.label}
              </FaText>
            </Pressable>
          );
        })}
      </View>

      <Modal
        visible={viewing !== null}
        animationType="slide"
        onRequestClose={() => setViewing(null)}
      >
        <SafeAreaView style={styles.modalSafe}>
          {viewing ? (
            <InvoiceDetailScreen invoice={viewing} onClose={() => setViewing(null)} />
          ) : null}
        </SafeAreaView>
      </Modal>
    </View>
  );
}

export default function App() {
  useEffect(() => {
    if (Platform.OS === 'web' && typeof document !== 'undefined') {
      document.documentElement.dir = 'rtl';
      document.documentElement.lang = 'fa';
      document.body.dir = 'rtl';
    } else {
      try {
        I18nManager.allowRTL(true);
        I18nManager.forceRTL(true);
      } catch {
        // no-op
      }
    }
  }, []);

  return (
    <SafeAreaView style={styles.safe}>
      <StatusBar style="light" />
      <AppProvider>
        <Shell />
      </AppProvider>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safe: {
    flex: 1,
    backgroundColor: colors.bg,
    paddingTop: Platform.OS === 'android' ? RNStatusBar.currentHeight : 0,
  },
  root: { flex: 1, backgroundColor: colors.bg },
  loading: { flex: 1, alignItems: 'center', justifyContent: 'center', backgroundColor: colors.bg },
  loadingText: { color: colors.textMuted, fontSize: 15 },
  header: {
    paddingHorizontal: spacing.lg,
    paddingVertical: spacing.md,
    borderBottomWidth: 1,
    borderBottomColor: colors.border,
    backgroundColor: colors.surface,
  },
  brand: { flexDirection: 'row-reverse', alignItems: 'center', gap: spacing.md },
  logo: {
    width: 40,
    height: 40,
    borderRadius: 12,
    backgroundColor: colors.gold,
    alignItems: 'center',
    justifyContent: 'center',
  },
  logoText: { color: '#1a1400', fontWeight: '800', fontSize: 17 },
  headerTitle: { color: colors.text, fontSize: 18, fontWeight: '800' },
  body: { flex: 1 },
  tabbar: {
    flexDirection: 'row-reverse',
    backgroundColor: colors.surface,
    borderTopWidth: 1,
    borderTopColor: colors.border,
    paddingBottom: Platform.OS === 'ios' ? spacing.lg : spacing.sm,
    paddingTop: spacing.sm,
  },
  tabItem: { flex: 1, alignItems: 'center', gap: 3 },
  tabIconWrap: {
    width: 42,
    height: 34,
    borderRadius: 10,
    alignItems: 'center',
    justifyContent: 'center',
  },
  tabActiveWrap: { backgroundColor: 'rgba(233,185,73,0.14)' },
  tabPrimary: {
    backgroundColor: colors.gold,
    width: 48,
    height: 40,
    borderRadius: 14,
    marginTop: -18,
    borderWidth: 4,
    borderColor: colors.surface,
  },
  tabIcon: { fontSize: 19, color: colors.textMuted },
  tabIconActive: { color: colors.gold },
  tabPrimaryIcon: { color: '#1a1400', fontSize: 24, fontWeight: '800' },
  tabLabel: { fontSize: 11, color: colors.textMuted },
  tabLabelActive: { color: colors.gold, fontWeight: '700' },
  toast: {
    position: 'absolute',
    bottom: 90,
    alignSelf: 'center',
    backgroundColor: colors.green,
    paddingVertical: spacing.md,
    paddingHorizontal: spacing.xl,
    borderRadius: 999,
    elevation: 6,
  },
  toastText: { color: '#04150d', fontWeight: '800', fontSize: 13 },
  modalSafe: {
    flex: 1,
    backgroundColor: colors.bg,
    paddingTop: Platform.OS === 'android' ? RNStatusBar.currentHeight : 0,
  },
});
