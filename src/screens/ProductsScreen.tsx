import React, { useMemo, useState } from 'react';
import {
  FlatList,
  Modal,
  Pressable,
  ScrollView,
  StyleSheet,
  View,
} from 'react-native';
import { colors, radius, spacing } from '../theme';
import { useApp } from '../context/AppContext';
import { Button, Card, Chip, FaText, Field } from '../components/ui';
import { toFa, toman, weightFa, toEn } from '../utils/format';
import { Category, CATEGORY_LABELS, Product } from '../types';
import { pricePerGram } from '../utils/calc';

const CATEGORIES: Category[] = ['gold', 'coin', 'jewelry'];
const ACCENTS = ['#e9b949', '#f5d98b', '#d4af37', '#c9a227', '#b8860b', '#f0c75e'];

function emptyDraft(): Product {
  return {
    id: '',
    code: '',
    name: '',
    category: 'jewelry',
    karat: 18,
    weight: 0,
    wagePercent: 12,
    stock: 1,
    accent: ACCENTS[Math.floor(Math.random() * ACCENTS.length)],
  };
}

export function ProductsScreen() {
  const { products, settings, addProduct, updateProduct, deleteProduct } = useApp();
  const [filter, setFilter] = useState<Category | 'all'>('all');
  const [modalOpen, setModalOpen] = useState(false);
  const [draft, setDraft] = useState<Product>(emptyDraft());
  const [isEdit, setIsEdit] = useState(false);

  const filtered = useMemo(
    () => (filter === 'all' ? products : products.filter((p) => p.category === filter)),
    [products, filter],
  );

  function openNew() {
    setDraft(emptyDraft());
    setIsEdit(false);
    setModalOpen(true);
  }

  function openEdit(p: Product) {
    setDraft({ ...p });
    setIsEdit(true);
    setModalOpen(true);
  }

  async function save() {
    const clean: Product = {
      ...draft,
      name: draft.name.trim() || 'کالای بدون نام',
      code: draft.code.trim() || `G-${Math.floor(1000 + Math.random() * 9000)}`,
    };
    if (isEdit) {
      await updateProduct(clean);
    } else {
      await addProduct({ ...clean, id: `p_${Date.now()}` });
    }
    setModalOpen(false);
  }

  const num = (v: string) => Number(toEn(v).replace(/[^0-9.]/g, '')) || 0;

  return (
    <View style={styles.container}>
      <View style={styles.filterBar}>
        <Chip label="همه" active={filter === 'all'} onPress={() => setFilter('all')} />
        {CATEGORIES.map((c) => (
          <Chip
            key={c}
            label={CATEGORY_LABELS[c]}
            active={filter === c}
            onPress={() => setFilter(c)}
          />
        ))}
      </View>

      <FlatList
        data={filtered}
        keyExtractor={(p) => p.id}
        contentContainerStyle={styles.list}
        showsVerticalScrollIndicator={false}
        renderItem={({ item }) => {
          const unit = item.weight * pricePerGram(settings.pricePerGram18, item.karat);
          return (
            <Pressable onPress={() => openEdit(item)}>
              <Card style={styles.prodCard}>
                <View style={[styles.thumb, { backgroundColor: item.accent + '22', borderColor: item.accent }]}>
                  <FaText style={[styles.thumbText, { color: item.accent }]}>
                    {toFa(item.karat)}
                  </FaText>
                  <FaText style={styles.thumbKarat}>عیار</FaText>
                </View>
                <View style={{ flex: 1 }}>
                  <View style={styles.prodTop}>
                    <View style={styles.stockPill}>
                      <FaText style={styles.stockText}>موجودی {toFa(item.stock)}</FaText>
                    </View>
                    <FaText style={styles.prodName} numberOfLines={1}>
                      {item.name}
                    </FaText>
                  </View>
                  <FaText style={styles.prodMeta}>
                    {CATEGORY_LABELS[item.category]} · {weightFa(item.weight)} · اجرت ٪{toFa(item.wagePercent)}
                  </FaText>
                  <View style={styles.prodBottom}>
                    <FaText style={styles.prodPrice}>{toman(unit)}</FaText>
                    <FaText style={styles.prodCode}>{item.code}</FaText>
                  </View>
                </View>
              </Card>
            </Pressable>
          );
        }}
        ListEmptyComponent={
          <Card style={styles.empty}>
            <FaText style={styles.emptyText}>کالایی در این دسته وجود ندارد</FaText>
          </Card>
        }
      />

      <Pressable style={styles.fab} onPress={openNew}>
        <FaText style={styles.fabText}>＋</FaText>
      </Pressable>

      <Modal visible={modalOpen} animationType="slide" transparent onRequestClose={() => setModalOpen(false)}>
        <View style={styles.modalWrap}>
          <View style={styles.sheet}>
            <View style={styles.sheetHandle} />
            <View style={styles.sheetHead}>
              {isEdit ? (
                <Pressable
                  onPress={async () => {
                    await deleteProduct(draft.id);
                    setModalOpen(false);
                  }}
                >
                  <FaText style={styles.deleteLink}>حذف</FaText>
                </Pressable>
              ) : (
                <View style={{ width: 40 }} />
              )}
              <FaText style={styles.sheetTitle}>
                {isEdit ? 'ویرایش کالا' : 'افزودن کالا'}
              </FaText>
              <Pressable onPress={() => setModalOpen(false)}>
                <FaText style={styles.closeLink}>بستن</FaText>
              </Pressable>
            </View>

            <ScrollView showsVerticalScrollIndicator={false}>
              <Field
                label="نام کالا"
                value={draft.name}
                onChangeText={(t) => setDraft({ ...draft, name: t })}
                placeholder="مثلاً انگشتر طلای زنانه"
              />
              <Field
                label="کد کالا"
                value={draft.code}
                onChangeText={(t) => setDraft({ ...draft, code: t })}
                placeholder="G-1001"
              />

              <FaText style={styles.groupLabel}>دسته‌بندی</FaText>
              <View style={styles.catRow}>
                {CATEGORIES.map((c) => (
                  <Chip
                    key={c}
                    label={CATEGORY_LABELS[c]}
                    active={draft.category === c}
                    onPress={() => setDraft({ ...draft, category: c })}
                  />
                ))}
              </View>

              <View style={styles.twoCol}>
                <View style={styles.col}>
                  <Field
                    label="وزن (گرم)"
                    keyboardType="numeric"
                    value={draft.weight ? toFa(draft.weight) : ''}
                    onChangeText={(t) => setDraft({ ...draft, weight: num(t) })}
                    placeholder="۰"
                  />
                </View>
                <View style={styles.col}>
                  <Field
                    label="عیار"
                    keyboardType="numeric"
                    value={draft.karat ? toFa(draft.karat) : ''}
                    onChangeText={(t) => setDraft({ ...draft, karat: num(t) })}
                    placeholder="۱۸"
                  />
                </View>
              </View>

              <View style={styles.twoCol}>
                <View style={styles.col}>
                  <Field
                    label="درصد اجرت"
                    keyboardType="numeric"
                    value={draft.wagePercent ? toFa(draft.wagePercent) : ''}
                    onChangeText={(t) => setDraft({ ...draft, wagePercent: num(t) })}
                    placeholder="۱۲"
                  />
                </View>
                <View style={styles.col}>
                  <Field
                    label="موجودی"
                    keyboardType="numeric"
                    value={draft.stock ? toFa(draft.stock) : ''}
                    onChangeText={(t) => setDraft({ ...draft, stock: num(t) })}
                    placeholder="۱"
                  />
                </View>
              </View>

              <View style={styles.pricePreview}>
                <FaText style={styles.pricePreviewVal}>
                  {toman(draft.weight * pricePerGram(settings.pricePerGram18, draft.karat || 18))}
                </FaText>
                <FaText style={styles.pricePreviewLabel}>ارزش طلای خام</FaText>
              </View>

              <Button title={isEdit ? 'ذخیره تغییرات' : 'افزودن کالا'} onPress={save} icon="✓" />
              <View style={{ height: spacing.xl }} />
            </ScrollView>
          </View>
        </View>
      </Modal>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  filterBar: {
    flexDirection: 'row-reverse',
    gap: spacing.sm,
    paddingHorizontal: spacing.lg,
    paddingBottom: spacing.md,
  },
  list: { paddingHorizontal: spacing.lg, gap: spacing.md, paddingBottom: 100 },
  prodCard: { flexDirection: 'row-reverse', alignItems: 'center', gap: spacing.md },
  thumb: {
    width: 58,
    height: 58,
    borderRadius: radius.md,
    borderWidth: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  thumbText: { fontSize: 20, fontWeight: '800' },
  thumbKarat: { color: colors.textMuted, fontSize: 10 },
  prodTop: {
    flexDirection: 'row-reverse',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: spacing.sm,
  },
  prodName: { color: colors.text, fontSize: 15, fontWeight: '700', flex: 1 },
  stockPill: {
    backgroundColor: colors.surfaceAlt,
    paddingVertical: 3,
    paddingHorizontal: 8,
    borderRadius: radius.pill,
  },
  stockText: { color: colors.textMuted, fontSize: 11 },
  prodMeta: { color: colors.textMuted, fontSize: 12, marginTop: 4 },
  prodBottom: {
    flexDirection: 'row-reverse',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginTop: spacing.sm,
  },
  prodPrice: { color: colors.gold, fontSize: 14, fontWeight: '800' },
  prodCode: { color: colors.textDim, fontSize: 11 },
  empty: { alignItems: 'center', paddingVertical: spacing.xl },
  emptyText: { color: colors.textMuted, fontSize: 14 },
  fab: {
    position: 'absolute',
    bottom: spacing.xl,
    left: spacing.xl,
    width: 58,
    height: 58,
    borderRadius: 29,
    backgroundColor: colors.gold,
    alignItems: 'center',
    justifyContent: 'center',
    elevation: 6,
    shadowColor: '#000',
    shadowOpacity: 0.35,
    shadowRadius: 8,
    shadowOffset: { width: 0, height: 4 },
  },
  fabText: { color: '#1a1400', fontSize: 30, fontWeight: '800', lineHeight: 34 },
  modalWrap: { flex: 1, backgroundColor: colors.overlay, justifyContent: 'flex-end' },
  sheet: {
    backgroundColor: colors.surface,
    borderTopLeftRadius: radius.xl,
    borderTopRightRadius: radius.xl,
    padding: spacing.lg,
    maxHeight: '92%',
  },
  sheetHandle: {
    width: 44,
    height: 5,
    borderRadius: 3,
    backgroundColor: colors.border,
    alignSelf: 'center',
    marginBottom: spacing.md,
  },
  sheetHead: {
    flexDirection: 'row-reverse',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: spacing.lg,
  },
  sheetTitle: { color: colors.text, fontSize: 17, fontWeight: '800' },
  closeLink: { color: colors.textMuted, fontSize: 14 },
  deleteLink: { color: colors.red, fontSize: 14, fontWeight: '600' },
  groupLabel: { color: colors.textMuted, fontSize: 13, marginBottom: spacing.sm, textAlign: 'right' },
  catRow: { flexDirection: 'row-reverse', gap: spacing.sm, marginBottom: spacing.md },
  twoCol: { flexDirection: 'row-reverse', gap: spacing.md },
  col: { flex: 1 },
  pricePreview: {
    backgroundColor: colors.surfaceAlt,
    borderRadius: radius.md,
    padding: spacing.md,
    marginBottom: spacing.md,
    alignItems: 'center',
  },
  pricePreviewVal: { color: colors.gold, fontSize: 18, fontWeight: '800' },
  pricePreviewLabel: { color: colors.textMuted, fontSize: 12, marginTop: 2 },
});
