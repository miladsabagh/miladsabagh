import { Image, Pressable, StyleSheet, Text, View } from 'react-native';
import { Plus, Star } from 'lucide-react-native';

import { formatNumber, formatToman, productPrice } from '../commerce';
import { colors, fonts } from '../theme';
import type { Product } from '../types';

type ProductCardProps = {
  product: Product;
  onAdd: (product: Product) => void;
  onView: (product: Product) => void;
};

export function ProductCard({ product, onAdd, onView }: ProductCardProps) {
  return (
    <Pressable
      accessibilityLabel={`مشاهده ${product.name}`}
      onPress={() => onView(product)}
      style={({ pressed }) => [styles.card, pressed && styles.pressed]}
    >
      <View style={styles.imageWrap}>
        <Image source={{ uri: product.image }} style={styles.image} />
        {product.badge ? (
          <View style={styles.badge}>
            <Text style={styles.badgeText}>{product.badge}</Text>
          </View>
        ) : null}
        <View style={styles.rating}>
          <Star color={colors.gold} fill={colors.gold} size={11} strokeWidth={1.8} />
          <Text style={styles.ratingText}>{formatNumber(product.rating)}</Text>
        </View>
      </View>

      <View style={styles.content}>
        <Text numberOfLines={1} style={styles.name}>
          {product.name}
        </Text>
        <View style={styles.metaRow}>
          <Text style={styles.meta}>{formatNumber(product.weight)} گرم</Text>
          <View style={styles.dot} />
          <Text style={styles.meta}>طلای {formatNumber(product.karat)} عیار</Text>
        </View>
        <View style={styles.priceRow}>
          <Pressable
            accessibilityLabel={`افزودن ${product.name} به سبد`}
            hitSlop={8}
            onPress={(event) => {
              event.stopPropagation();
              onAdd(product);
            }}
            style={({ pressed }) => [styles.addButton, pressed && styles.addPressed]}
          >
            <Plus color={colors.white} size={17} strokeWidth={2.4} />
          </Pressable>
          <View>
            <Text style={styles.price}>{formatToman(productPrice(product))}</Text>
            <Text style={styles.priceHint}>با احتساب اجرت و مالیات</Text>
          </View>
        </View>
      </View>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  card: {
    width: '48.25%',
    minWidth: 0,
    overflow: 'hidden',
    borderRadius: 20,
    backgroundColor: colors.paper,
    borderWidth: 1,
    borderColor: colors.line,
    shadowColor: colors.black,
    shadowOffset: { width: 0, height: 5 },
    shadowOpacity: 0.06,
    shadowRadius: 12,
    elevation: 2,
  },
  pressed: {
    opacity: 0.92,
    transform: [{ scale: 0.985 }],
  },
  imageWrap: {
    height: 150,
    backgroundColor: '#E9E3D7',
  },
  image: {
    width: '100%',
    height: '100%',
  },
  badge: {
    position: 'absolute',
    top: 10,
    right: 10,
    borderRadius: 10,
    backgroundColor: colors.forest,
    paddingHorizontal: 8,
    paddingVertical: 4,
  },
  badgeText: {
    color: colors.white,
    fontFamily: fonts.semibold,
    fontSize: 9,
  },
  rating: {
    position: 'absolute',
    left: 9,
    bottom: 8,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 3,
    borderRadius: 10,
    backgroundColor: 'rgba(255,255,255,0.93)',
    paddingHorizontal: 7,
    paddingVertical: 4,
  },
  ratingText: {
    color: colors.ink,
    fontFamily: fonts.bold,
    fontSize: 10,
  },
  content: {
    paddingHorizontal: 11,
    paddingTop: 11,
    paddingBottom: 12,
  },
  name: {
    color: colors.ink,
    fontFamily: fonts.bold,
    fontSize: 12.5,
    textAlign: 'right',
    writingDirection: 'rtl',
  },
  metaRow: {
    marginTop: 5,
    flexDirection: 'row-reverse',
    alignItems: 'center',
    gap: 5,
  },
  meta: {
    color: colors.inkSoft,
    fontFamily: fonts.regular,
    fontSize: 9.5,
    writingDirection: 'rtl',
  },
  dot: {
    width: 3,
    height: 3,
    borderRadius: 3,
    backgroundColor: colors.gold,
  },
  priceRow: {
    marginTop: 13,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: 5,
  },
  addButton: {
    width: 30,
    height: 30,
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: 10,
    backgroundColor: colors.forest,
  },
  addPressed: {
    backgroundColor: colors.gold,
  },
  price: {
    color: colors.ink,
    fontFamily: fonts.bold,
    fontSize: 10.5,
    textAlign: 'right',
    writingDirection: 'rtl',
  },
  priceHint: {
    marginTop: 2,
    color: colors.inkSoft,
    fontFamily: fonts.regular,
    fontSize: 7.5,
    textAlign: 'right',
    writingDirection: 'rtl',
  },
});
