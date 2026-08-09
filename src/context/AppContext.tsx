import React, {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from 'react';
import { Invoice, Product, Settings } from '../types';
import * as db from '../storage/db';
import { defaultSettings } from '../storage/seed';

interface AppState {
  ready: boolean;
  products: Product[];
  settings: Settings;
  invoices: Invoice[];
  addProduct: (p: Product) => Promise<void>;
  updateProduct: (p: Product) => Promise<void>;
  deleteProduct: (id: string) => Promise<void>;
  updateSettings: (s: Settings) => Promise<void>;
  addInvoice: (i: Omit<Invoice, 'id' | 'number' | 'createdAt'>) => Promise<Invoice>;
  deleteInvoice: (id: string) => Promise<void>;
}

const AppContext = createContext<AppState | undefined>(undefined);

export function AppProvider({ children }: { children: React.ReactNode }) {
  const [ready, setReady] = useState(false);
  const [products, setProducts] = useState<Product[]>([]);
  const [settings, setSettings] = useState<Settings>(defaultSettings);
  const [invoices, setInvoices] = useState<Invoice[]>([]);

  useEffect(() => {
    (async () => {
      const [p, s, i] = await Promise.all([
        db.loadProducts(),
        db.loadSettings(),
        db.loadInvoices(),
      ]);
      setProducts(p);
      setSettings(s);
      setInvoices(i);
      setReady(true);
    })();
  }, []);

  const addProduct = useCallback(async (p: Product) => {
    setProducts((prev) => {
      const next = [p, ...prev];
      void db.saveProducts(next);
      return next;
    });
  }, []);

  const updateProduct = useCallback(async (p: Product) => {
    setProducts((prev) => {
      const next = prev.map((x) => (x.id === p.id ? p : x));
      void db.saveProducts(next);
      return next;
    });
  }, []);

  const deleteProduct = useCallback(async (id: string) => {
    setProducts((prev) => {
      const next = prev.filter((x) => x.id !== id);
      void db.saveProducts(next);
      return next;
    });
  }, []);

  const updateSettings = useCallback(async (s: Settings) => {
    setSettings(s);
    await db.saveSettings(s);
  }, []);

  const addInvoice = useCallback(
    async (input: Omit<Invoice, 'id' | 'number' | 'createdAt'>) => {
      const number = await db.nextInvoiceNumber();
      const invoice: Invoice = {
        ...input,
        id: `inv_${Date.now()}`,
        number,
        createdAt: Date.now(),
      };
      setInvoices((prev) => {
        const next = [invoice, ...prev];
        void db.saveInvoices(next);
        return next;
      });
      return invoice;
    },
    [],
  );

  const deleteInvoice = useCallback(async (id: string) => {
    setInvoices((prev) => {
      const next = prev.filter((x) => x.id !== id);
      void db.saveInvoices(next);
      return next;
    });
  }, []);

  const value = useMemo<AppState>(
    () => ({
      ready,
      products,
      settings,
      invoices,
      addProduct,
      updateProduct,
      deleteProduct,
      updateSettings,
      addInvoice,
      deleteInvoice,
    }),
    [
      ready,
      products,
      settings,
      invoices,
      addProduct,
      updateProduct,
      deleteProduct,
      updateSettings,
      addInvoice,
      deleteInvoice,
    ],
  );

  return <AppContext.Provider value={value}>{children}</AppContext.Provider>;
}

export function useApp(): AppState {
  const ctx = useContext(AppContext);
  if (!ctx) throw new Error('useApp must be used within AppProvider');
  return ctx;
}
