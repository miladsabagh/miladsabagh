package com.zarin.gold.data

object SeedData {
    val products = listOf(
        Product(
            name = "انگشتر کلاسیک یارا",
            category = ProductCategory.RING,
            carat = 18,
            weightGram = 4.2,
            makingFeePercent = 12.0,
            stonePrice = 2_500_000,
            imageHint = "ring"
        ),
        Product(
            name = "گردنبند ستاره نقره‌ای‌طلا",
            category = ProductCategory.NECKLACE,
            carat = 18,
            weightGram = 8.6,
            makingFeePercent = 15.0,
            stonePrice = 4_800_000,
            imageHint = "necklace"
        ),
        Product(
            name = "دستبند بافت آریا",
            category = ProductCategory.BRACELET,
            carat = 18,
            weightGram = 11.3,
            makingFeePercent = 10.0,
            imageHint = "bracelet"
        ),
        Product(
            name = "گوشواره قطره نور",
            category = ProductCategory.EARRING,
            carat = 18,
            weightGram = 3.1,
            makingFeePercent = 14.0,
            stonePrice = 1_200_000,
            imageHint = "earring"
        ),
        Product(
            name = "سکه بهار آزادی طرح جدید",
            category = ProductCategory.COIN,
            carat = 22,
            weightGram = 8.13,
            makingFeePercent = 2.0,
            imageHint = "coin"
        ),
        Product(
            name = "شمش یک گرمی زرین",
            category = ProductCategory.BULLION,
            carat = 24,
            weightGram = 1.0,
            makingFeePercent = 3.5,
            imageHint = "bullion"
        ),
        Product(
            name = "سرویس عروس لیان",
            category = ProductCategory.SET,
            carat = 18,
            weightGram = 28.4,
            makingFeePercent = 18.0,
            stonePrice = 12_000_000,
            imageHint = "set"
        ),
        Product(
            name = "انگشتر مردانه کوهستان",
            category = ProductCategory.RING,
            carat = 18,
            weightGram = 6.8,
            makingFeePercent = 9.0,
            imageHint = "ring_m"
        )
    )

    val customers = listOf(
        Customer(name = "سارا محمدی", phone = "09121234567"),
        Customer(name = "علی رضایی", phone = "09351234567"),
        Customer(name = "مریم کریمی", phone = "09199887766")
    )

    /** قیمت پایه هر گرم طلای ۱۸ عیار (ریال) — نمونه */
    const val DEFAULT_GOLD_PRICE_18: Long = 42_500_000L
}
