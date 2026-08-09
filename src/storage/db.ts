import AsyncStorage from '@react-native-async-storage/async-storage';
import { Invoice, Product, Settings } from '../types';
import { defaultSettings, seedProducts } from './seed';

const KEYS = {
  products: '@gold/products',
  settings: '@gold/settings',
  invoices: '@gold/invoices',
  counter: '@gold/invoiceCounter',
};

async function readJSON<T>(key: string, fallback: T): Promise<T> {
  try {
    const raw = await AsyncStorage.getItem(key);
    if (raw == null) return fallback;
    return JSON.parse(raw) as T;
  } catch {
    return fallback;
  }
}

async function writeJSON(key: string, value: unknown): Promise<void> {
  await AsyncStorage.setItem(key, JSON.stringify(value));
}

export async function loadProducts(): Promise<Product[]> {
  const existing = await AsyncStorage.getItem(KEYS.products);
  if (existing == null) {
    await writeJSON(KEYS.products, seedProducts);
    return seedProducts;
  }
  return JSON.parse(existing) as Product[];
}

export async function saveProducts(products: Product[]): Promise<void> {
  await writeJSON(KEYS.products, products);
}

export async function loadSettings(): Promise<Settings> {
  const existing = await AsyncStorage.getItem(KEYS.settings);
  if (existing == null) {
    await writeJSON(KEYS.settings, defaultSettings);
    return defaultSettings;
  }
  return { ...defaultSettings, ...(JSON.parse(existing) as Settings) };
}

export async function saveSettings(settings: Settings): Promise<void> {
  await writeJSON(KEYS.settings, settings);
}

export async function loadInvoices(): Promise<Invoice[]> {
  return readJSON<Invoice[]>(KEYS.invoices, []);
}

export async function saveInvoices(invoices: Invoice[]): Promise<void> {
  await writeJSON(KEYS.invoices, invoices);
}

export async function nextInvoiceNumber(): Promise<number> {
  const current = await readJSON<number>(KEYS.counter, 1000);
  const next = current + 1;
  await writeJSON(KEYS.counter, next);
  return next;
}
