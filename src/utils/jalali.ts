import { toFa } from './format';

const MONTHS = [
  'فروردین',
  'اردیبهشت',
  'خرداد',
  'تیر',
  'مرداد',
  'شهریور',
  'مهر',
  'آبان',
  'آذر',
  'دی',
  'بهمن',
  'اسفند',
];

function div(a: number, b: number): number {
  return Math.floor(a / b);
}

// Gregorian -> Jalali conversion (algorithm by Kazimierz M. Borkowski)
export function toJalali(gy: number, gm: number, gd: number): [number, number, number] {
  const gDaysInMonth = [31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];
  const jDaysInMonth = [31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29];

  let gy2 = gm > 2 ? gy + 1 : gy;
  let days =
    355666 +
    365 * gy +
    div(gy2 + 3, 4) -
    div(gy2 + 99, 100) +
    div(gy2 + 399, 400) +
    gd +
    gDaysInMonth.slice(0, gm - 1).reduce((a, b) => a + b, 0);

  let jy = -1595 + 33 * div(days, 12053);
  days %= 12053;
  jy += 4 * div(days, 1461);
  days %= 1461;
  if (days > 365) {
    jy += div(days - 1, 365);
    days = (days - 1) % 365;
  }

  let jm: number;
  let jd: number;
  if (days < 186) {
    jm = 1 + div(days, 31);
    jd = 1 + (days % 31);
  } else {
    jm = 7 + div(days - 186, 30);
    jd = 1 + ((days - 186) % 30);
  }
  // reference jDaysInMonth to satisfy leap logic consumers; keep for clarity
  void jDaysInMonth;
  return [jy, jm, jd];
}

export function formatJalaliDate(ts: number): string {
  const d = new Date(ts);
  const [jy, jm, jd] = toJalali(d.getFullYear(), d.getMonth() + 1, d.getDate());
  return `${toFa(jd)} ${MONTHS[jm - 1]} ${toFa(jy)}`;
}

export function formatJalaliShort(ts: number): string {
  const d = new Date(ts);
  const [jy, jm, jd] = toJalali(d.getFullYear(), d.getMonth() + 1, d.getDate());
  const pad = (n: number) => (n < 10 ? `0${n}` : `${n}`);
  return toFa(`${jy}/${pad(jm)}/${pad(jd)}`);
}

export function formatTime(ts: number): string {
  const d = new Date(ts);
  const pad = (n: number) => (n < 10 ? `0${n}` : `${n}`);
  return toFa(`${pad(d.getHours())}:${pad(d.getMinutes())}`);
}
