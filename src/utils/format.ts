const FA_DIGITS = ['۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹'];

export function toFa(input: string | number): string {
  return String(input).replace(/[0-9]/g, (d) => FA_DIGITS[Number(d)]);
}

export function toEn(input: string): string {
  return input
    .replace(/[۰-۹]/g, (d) => String(FA_DIGITS.indexOf(d)))
    .replace(/[٠-٩]/g, (d) => String('٠١٢٣٤٥٦٧٨٩'.indexOf(d)));
}

export function groupDigits(n: number): string {
  const rounded = Math.round(n);
  const sign = rounded < 0 ? '-' : '';
  const s = Math.abs(rounded).toString();
  return sign + s.replace(/\B(?=(\d{3})+(?!\d))/g, '٬');
}

export function toman(n: number): string {
  return toFa(groupDigits(n)) + ' تومان';
}

export function faNumber(n: number, fractionDigits = 0): string {
  const fixed = fractionDigits > 0 ? n.toFixed(fractionDigits) : String(Math.round(n));
  const [intPart, decPart] = fixed.split('.');
  const grouped = intPart.replace(/\B(?=(\d{3})+(?!\d))/g, '٬');
  return toFa(decPart ? `${grouped}.${decPart}` : grouped);
}

export function weightFa(grams: number): string {
  const s = Number.isInteger(grams) ? String(grams) : grams.toFixed(3).replace(/0+$/, '').replace(/\.$/, '');
  return toFa(s) + ' گرم';
}
