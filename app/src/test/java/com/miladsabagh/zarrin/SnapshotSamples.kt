package com.miladsabagh.zarrin

import com.miladsabagh.zarrin.data.ShopSettings
import com.miladsabagh.zarrin.data.db.CustomerEntity
import com.miladsabagh.zarrin.data.db.InvoiceEntity
import com.miladsabagh.zarrin.data.db.InvoiceItemEntity
import com.miladsabagh.zarrin.data.db.InvoiceWithItems
import com.miladsabagh.zarrin.data.db.ProductCategory
import com.miladsabagh.zarrin.data.db.ProductEntity
import com.miladsabagh.zarrin.ui.CartLine

object SnapshotSamples {

    val settings = ShopSettings(
        goldPricePerGram18k = 3_850_000,
        profitPercent = 7.0,
        taxPercent = 9.0,
        storeName = "گالری طلا و جواهر زرین",
        storePhone = "021-12345678",
        storeAddress = "تهران، بازار بزرگ، پاساژ طلای زرین، پلاک ۱۲"
    )

    val products = listOf(
        ProductEntity(1, "انگشتر طلای زنانه مدل رز", ProductCategory.RING, 18, 2.35, 350_000),
        ProductEntity(2, "گردنبند زنجیری کارتیه", ProductCategory.NECKLACE, 18, 6.80, 420_000),
        ProductEntity(3, "النگوی تک‌پوش صوفیا", ProductCategory.BANGLE, 18, 8.60, 330_000),
        ProductEntity(4, "سرویس کامل عروس لیلا", ProductCategory.SET, 18, 42.0, 460_000),
        ProductEntity(5, "سکه تمام بهار آزادی", ProductCategory.COIN, 22, 8.133, 0)
    )

    val customers = listOf(
        CustomerEntity(1, "مریم احمدی", "09121234567", "تهران، سعادت‌آباد"),
        CustomerEntity(2, "رضا کریمی", "09359876543"),
        CustomerEntity(3, "مشتری حضوری", "-")
    )

    val cartLines = listOf(
        CartLine(1, "انگشتر طلای زنانه مدل رز", 18, 2.35, 350_000),
        CartLine(3, "النگوی تک‌پوش صوفیا", 18, 8.60, 330_000)
    )

    val invoice = InvoiceWithItems(
        invoice = InvoiceEntity(
            id = 1,
            invoiceNumber = 1001,
            customerId = 1,
            customerName = "مریم احمدی",
            customerPhone = "09121234567",
            createdAt = 1754762400000L,
            goldPricePerGram18k = 3_850_000,
            profitPercent = 7.0,
            taxPercent = 9.0,
            goldValue = 42_122_500,
            wageTotal = 3_660_500,
            profitAmount = 3_204_810,
            taxAmount = 617_878,
            grandTotal = 49_605_688
        ),
        items = listOf(
            InvoiceItemEntity(
                id = 1, invoiceId = 1, productId = 1,
                title = "انگشتر طلای زنانه مدل رز",
                karat = 18, weightGrams = 2.35, wagePerGram = 350_000,
                goldPricePerGram = 3_850_000, goldValue = 9_047_500,
                wageAmount = 822_500, profitAmount = 690_900, taxAmount = 136_206,
                lineTotal = 10_697_106
            ),
            InvoiceItemEntity(
                id = 2, invoiceId = 1, productId = 3,
                title = "النگوی تک‌پوش صوفیا",
                karat = 18, weightGrams = 8.60, wagePerGram = 330_000,
                goldPricePerGram = 3_850_000, goldValue = 33_110_000,
                wageAmount = 2_838_000, profitAmount = 2_516_360, taxAmount = 481_892,
                lineTotal = 38_946_252
            )
        )
    )

    val invoices = listOf(
        invoice,
        InvoiceWithItems(
            invoice = invoice.invoice.copy(
                id = 2, invoiceNumber = 1002,
                customerName = "رضا کریمی", customerPhone = "09359876543",
                createdAt = 1754848800000L, grandTotal = 12_340_000
            ),
            items = listOf(invoice.items.first().copy(id = 3, invoiceId = 2))
        )
    )
}
