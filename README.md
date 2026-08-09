# زرین گالری — اپلیکیشن اندروید فروش طلا و جواهر

اپلیکیشن موبایل اندروید برای مدیریت فروشگاه طلا و جواهر با قابلیت **صدور فاکتور**، محاسبه قیمت بر اساس وزن/عیار/اجرت، و اشتراک‌گذاری PDF.

## قابلیت‌ها

- داشبورد فروش روزانه و ماهانه
- مدیریت کالاها (انگشتر، گردنبند، دستبند، سکه، شمش و …)
- دفترچه مشتریان
- صدور فاکتور با محاسبه:
  - تبدیل وزن به معادل عیار ۱۸
  - اجرت ساخت
  - سود فروشنده
  - مالیات بر ارزش افزوده
- مشاهده تاریخچه فاکتورها
- خروجی و اشتراک‌گذاری PDF فاکتور
- تنظیمات فروشگاه و قیمت روز طلا
- رابط کاربری فارسی و راست‌چین (RTL)
- داده‌های نمونه برای شروع سریع

## ساختار پروژه

```
GoldJewelryShop/
├── app/src/main/java/com/zarrin/goldshop/
│   ├── data/          # Room + Repository
│   ├── domain/        # محاسبه قیمت و فرمت‌ها
│   ├── invoice/       # تولید PDF
│   └── ui/            # Compose screens + navigation
└── app/src/test/      # تست محاسبه قیمت
```

## اجرا

1. Android Studio Ladybug یا جدیدتر را باز کنید
2. پوشه `GoldJewelryShop` را به‌عنوان پروژه باز کنید
3. Gradle Sync را اجرا کنید
4. روی یک امولاتور یا گوشی واقعی Run بزنید

یا از خط فرمان:

```bash
cd GoldJewelryShop
./gradlew assembleDebug
```

APK خروجی:

`app/build/outputs/apk/debug/app-debug.apk`

## تست

```bash
cd GoldJewelryShop
./gradlew test
```

## فناوری‌ها

- Kotlin
- Jetpack Compose + Material 3
- Room Database
- Navigation Compose
- PDF generation با `PdfDocument`
