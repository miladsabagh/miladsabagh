package com.zarnegar.gold.data.seed

import com.zarnegar.gold.data.db.CustomerDao
import com.zarnegar.gold.data.db.ProductDao
import com.zarnegar.gold.data.db.toEntity
import com.zarnegar.gold.domain.model.Customer
import com.zarnegar.gold.domain.model.PricingMode
import com.zarnegar.gold.domain.model.Product
import com.zarnegar.gold.domain.model.ProductCategory
import com.zarnegar.gold.domain.model.WageMode

/** دادهٔ نمونه برای اولین اجرای برنامه تا کاربر با یک انبار آماده شروع کند. */
object SeedData {

    val products: List<Product> = listOf(
        Product(
            code = "R-1001",
            name = "انگشتر طرح ونکلیف",
            category = ProductCategory.RING,
            karat = 750,
            weightGrams = 3.450,
            wageMode = WageMode.PERCENT,
            wagePercent = 9.0,
            profitPercent = 7.0,
            stock = 4,
        ),
        Product(
            code = "N-2043",
            name = "گردنبند زنجیر کارتیه",
            category = ProductCategory.NECKLACE,
            karat = 750,
            weightGrams = 7.820,
            wageMode = WageMode.PERCENT,
            wagePercent = 12.0,
            profitPercent = 7.0,
            stock = 2,
        ),
        Product(
            code = "B-3110",
            name = "دستبند النگویی حصیری",
            category = ProductCategory.BRACELET,
            karat = 750,
            weightGrams = 12.140,
            wageMode = WageMode.PER_GRAM,
            wagePerGram = 320_000,
            profitPercent = 7.0,
            stock = 3,
        ),
        Product(
            code = "E-4077",
            name = "گوشواره حلقه‌ای نگین‌دار",
            category = ProductCategory.EARRING,
            karat = 750,
            weightGrams = 2.980,
            stoneWeightGrams = 0.180,
            stoneValue = 1_800_000,
            wageMode = WageMode.PERCENT,
            wagePercent = 14.0,
            profitPercent = 7.0,
            stock = 5,
        ),
        Product(
            code = "S-5002",
            name = "سرویس کامل عروس طرح ایتالیایی",
            category = ProductCategory.SET,
            karat = 750,
            weightGrams = 41.600,
            wageMode = WageMode.PERCENT,
            wagePercent = 15.0,
            profitPercent = 7.0,
            stock = 1,
        ),
        Product(
            code = "C-9001",
            name = "سکه تمام بهار آزادی (طرح جدید)",
            category = ProductCategory.COIN,
            pricingMode = PricingMode.FIXED,
            fixedPrice = 92_500_000,
            vatExempt = true,
            stock = 8,
        ),
        Product(
            code = "C-9002",
            name = "نیم‌سکه بهار آزادی",
            category = ProductCategory.COIN,
            pricingMode = PricingMode.FIXED,
            fixedPrice = 51_200_000,
            vatExempt = true,
            stock = 6,
        ),
        Product(
            code = "G-7001",
            name = "انگشتر جواهر با الماس تراش برلیان",
            category = ProductCategory.GEM,
            karat = 750,
            weightGrams = 5.240,
            stoneWeightGrams = 0.640,
            stoneValue = 48_000_000,
            wageMode = WageMode.PERCENT,
            wagePercent = 18.0,
            profitPercent = 10.0,
            stock = 1,
        ),
    )

    val customers: List<Customer> = listOf(
        Customer(
            fullName = "مریم احمدی",
            phone = "09121234567",
            nationalCode = "0064512378",
            address = "تهران، خیابان ولیعصر، پلاک ۱۲",
        ),
        Customer(
            fullName = "علی رضایی",
            phone = "09351112233",
            nationalCode = "1270054893",
            address = "اصفهان، خیابان چهارباغ بالا",
        ),
        Customer(
            fullName = "سارا موسوی",
            phone = "09197778899",
            address = "شیراز، بلوار زند",
        ),
    )

    suspend fun populateIfEmpty(productDao: ProductDao, customerDao: CustomerDao) {
        if (productDao.count() == 0) {
            productDao.insertAll(products.map { it.toEntity() })
        }
        if (customerDao.count() == 0) {
            customerDao.insertAll(customers.map { it.toEntity() })
        }
    }
}
