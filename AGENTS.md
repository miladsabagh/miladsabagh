# راهنمای کار روی این مخزن

پروژه یک اپلیکیشن اندروید (Kotlin + Jetpack Compose + Room) برای فروش طلا و جواهر است.
جزئیات قابلیت‌ها و منطق قیمت‌گذاری در `README.md` آمده است.

## قواعد کد

- زبان رابط کاربری و متن‌های نمایشی فارسی است و کل برنامه راست‌به‌چپ اجرا می‌شود
  (`LocalLayoutDirection` در `MainActivity` روی `Rtl` تنظیم شده است).
- مبالغ به‌صورت `Long` و بر حسب تومان ذخیره می‌شوند. نمایش ریالی فقط یک ضریب نمایشی
  در `AppSettings.display` است؛ هنگام خواندن ورودی کاربر مقدار را بر `currency.multiplier` تقسیم کنید.
- هر منطق محاسباتی جدید (قیمت، تاریخ، قالب‌بندی) باید در `domain/` یا `util/` قرار بگیرد و تست واحد داشته باشد.
- تاریخ‌ها با `JalaliDate` و اعداد با کمک `PersianText.kt` نمایش داده می‌شوند؛ از `java.text` برای این کار استفاده نکنید.

## ساخت و تست

```bash
./gradlew :app:testDebugUnitTest   # تست‌های واحد
./gradlew :app:assembleDebug       # ساخت APK دیباگ
./gradlew :app:assembleRelease     # اعتبارسنجی قواعد R8 و lint
```

مسیر SDK باید در `local.properties` تنظیم شود (`sdk.dir=...`).

## Cursor Cloud specific instructions

محیط Cloud Agent به‌صورت پیش‌فرض Android SDK ندارد و مهم‌تر اینکه **شتاب‌دهی KVM کار نمی‌کند**:
`/dev/kvm` قابل باز کردن است و `KVM_CREATE_VM` موفق می‌شود، اما فراخوانی `KVM_CREATE_VCPU`
پروسه را با SIGSEGV از بین می‌برد. به همین دلیل شبیه‌ساز با تنظیمات پیش‌فرض بدون هیچ پیام خطایی
درست بعد از `Activated packet streamer for bluetooth emulation` معلق می‌ماند.

مراحل راه‌اندازی شبیه‌ساز که آزمایش شده و کار می‌کند:

```bash
# ۱) نصب SDK
mkdir -p ~/android-sdk/cmdline-tools && cd ~/android-sdk
curl -sL -o cmdtools.zip https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
unzip -q cmdtools.zip && mkdir -p cmdline-tools/latest \
  && mv cmdline-tools/bin cmdline-tools/lib cmdline-tools/NOTICE.txt cmdline-tools/source.properties cmdline-tools/latest/
export ANDROID_HOME=$HOME/android-sdk
export PATH=$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH
yes | sdkmanager --licenses > /dev/null
sdkmanager "platform-tools" "platforms;android-35" "build-tools;35.0.0" "emulator" \
           "system-images;android-30;default;x86_64"

# ۲) ساخت AVD سبک (ایمیج AOSP بدون Google Play و صفحه کوچک‌تر، برای سرعت بیشتر)
echo no | avdmanager create avd -n zarrin -k "system-images;android-30;default;x86_64" -d "Nexus 4"
# در ~/.android/avd/zarrin.avd/config.ini مقادیر زیر را تنظیم کنید (کلید تکراری نگذارید):
# hw.lcd.width=720  hw.lcd.height=1280  hw.lcd.density=320  hw.cpu.ncore=4  hw.ramSize=3072

# ۳) اجرای شبیه‌ساز در حالت نرم‌افزاری (بدون KVM) — بوت حدود سه دقیقه طول می‌کشد
emulator -avd zarrin -gpu swiftshader_indirect -no-audio -no-snapshot -accel off -no-boot-anim -cores 4
```

نکته‌های عملی پس از بالا آمدن شبیه‌ساز:

- برای جلوگیری از خاموش شدن صفحه (که باعث سیاه شدن اسکرین‌شات‌ها و بی‌اثر شدن ضربه‌ها می‌شود):
  `adb shell dumpsys battery set ac 1`، `adb shell settings put global stay_on_while_plugged_in 7`
  و `adb shell settings put system screen_off_timeout 86400000`.
- انیمیشن‌ها را کم کنید و دیالوگ‌های خطای سیستمی را خاموش کنید:
  `settings put global window_animation_scale 0.3` (و مشابه آن برای transition و animator) و
  `settings put global hide_error_dialogs 1`.
- اولین اجرای برنامه به دلیل TCG حدود یک دقیقه طول می‌کشد؛ در انتظارها زمان کافی بگذارید.
- برای هدایت رابط کاربری، `adb shell uiautomator dump` به‌همراه `adb shell input tap` قابل اتکاتر از
  کلیک تصویری است. دکمه شناور «فاکتور جدید» در درخت دسترس‌پذیری ظاهر نمی‌شود و باید با مختصات لمس شود.
- خروجی PDF ساخته‌شده در `cache/invoices/` برنامه ذخیره می‌شود و با
  `adb exec-out run-as ir.zarrin.goldshop.debug cat cache/invoices/<name>.pdf` قابل استخراج است.
