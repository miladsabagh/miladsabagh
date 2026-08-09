import React, { useEffect, useState } from 'react';
import { ScrollView, StyleSheet, View } from 'react-native';
import { colors, radius, spacing } from '../theme';
import { useApp } from '../context/AppContext';
import { Button, Card, FaText, Field } from '../components/ui';
import { toFa, toman, toEn } from '../utils/format';
import { Settings } from '../types';

export function SettingsScreen() {
  const { settings, updateSettings } = useApp();
  const [draft, setDraft] = useState<Settings>(settings);
  const [saved, setSaved] = useState(false);

  useEffect(() => {
    setDraft(settings);
  }, [settings]);

  const num = (v: string) => Number(toEn(v).replace(/[^0-9.]/g, '')) || 0;

  async function save() {
    await updateSettings(draft);
    setSaved(true);
    setTimeout(() => setSaved(false), 2000);
  }

  return (
    <ScrollView contentContainerStyle={styles.content} showsVerticalScrollIndicator={false}>
      <Card>
        <FaText style={styles.title}>قیمت روز طلا</FaText>
        <FaText style={styles.subtitle}>
          قیمت هر گرم طلای ۱۸ عیار مبنای محاسبه همه فاکتورهاست
        </FaText>
        <Field
          label="قیمت هر گرم طلای ۱۸ عیار (تومان)"
          keyboardType="numeric"
          value={draft.pricePerGram18 ? toFa(draft.pricePerGram18) : ''}
          onChangeText={(t) => setDraft({ ...draft, pricePerGram18: num(t) })}
          placeholder="۳۴۵۰۰۰۰"
        />
        <View style={styles.pricePreview}>
          <FaText style={styles.pricePreviewVal}>{toman(draft.pricePerGram18)}</FaText>
        </View>
      </Card>

      <Card>
        <FaText style={styles.title}>سود و مالیات</FaText>
        <View style={styles.twoCol}>
          <View style={styles.col}>
            <Field
              label="درصد سود فروشنده"
              keyboardType="numeric"
              value={toFa(draft.profitPercent)}
              onChangeText={(t) => setDraft({ ...draft, profitPercent: num(t) })}
              placeholder="۷"
            />
          </View>
          <View style={styles.col}>
            <Field
              label="درصد مالیات (ارزش افزوده)"
              keyboardType="numeric"
              value={toFa(draft.taxPercent)}
              onChangeText={(t) => setDraft({ ...draft, taxPercent: num(t) })}
              placeholder="۹"
            />
          </View>
        </View>
        <FaText style={styles.note}>
          مالیات بر ارزش افزوده فقط روی اجرت و سود اعمال می‌شود (طبق قانون).
        </FaText>
      </Card>

      <Card>
        <FaText style={styles.title}>اطلاعات فروشگاه</FaText>
        <FaText style={styles.subtitle}>این اطلاعات در سربرگ فاکتور نمایش داده می‌شود</FaText>
        <Field
          label="نام فروشگاه"
          value={draft.shopName}
          onChangeText={(t) => setDraft({ ...draft, shopName: t })}
          placeholder="گالری طلا و جواهر"
        />
        <Field
          label="تلفن"
          value={draft.shopPhone}
          onChangeText={(t) => setDraft({ ...draft, shopPhone: t })}
          placeholder="۰۲۱-..."
        />
        <Field
          label="آدرس"
          value={draft.shopAddress}
          onChangeText={(t) => setDraft({ ...draft, shopAddress: t })}
          placeholder="تهران، ..."
          multiline
        />
      </Card>

      <Button
        title={saved ? 'ذخیره شد ✓' : 'ذخیره تنظیمات'}
        onPress={save}
        variant={saved ? 'soft' : 'primary'}
      />
      <View style={styles.footer}>
        <FaText style={styles.footerText}>طلا و جواهر میلاد · نسخه ۱٫۰</FaText>
      </View>
      <View style={{ height: spacing.xxl }} />
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  content: { padding: spacing.lg, gap: spacing.md },
  title: { color: colors.text, fontSize: 16, fontWeight: '800' },
  subtitle: { color: colors.textMuted, fontSize: 12, marginTop: 4, marginBottom: spacing.md },
  pricePreview: {
    backgroundColor: colors.surfaceAlt,
    borderRadius: radius.md,
    padding: spacing.md,
    alignItems: 'center',
  },
  pricePreviewVal: { color: colors.gold, fontSize: 18, fontWeight: '800' },
  twoCol: { flexDirection: 'row-reverse', gap: spacing.md, marginTop: spacing.sm },
  col: { flex: 1 },
  note: { color: colors.textDim, fontSize: 11, marginTop: spacing.sm },
  footer: { alignItems: 'center', marginTop: spacing.md },
  footerText: { color: colors.textDim, fontSize: 12 },
});
