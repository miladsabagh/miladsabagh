package com.zarnegar.gold

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.zarnegar.gold.domain.model.Invoice
import com.zarnegar.gold.domain.model.InvoiceLine
import com.zarnegar.gold.domain.model.InvoiceStatus
import com.zarnegar.gold.domain.model.PaymentMethod
import com.zarnegar.gold.domain.model.PricingMode
import com.zarnegar.gold.domain.model.SaleItem
import com.zarnegar.gold.domain.model.ShopSettings
import com.zarnegar.gold.domain.pricing.GoldPricing
import com.zarnegar.gold.pdf.InvoicePdfGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * تولید واقعی فایل PDF فاکتور.
 *
 * `android.graphics.pdf.PdfDocument` به گرافیک بومی اندروید نیاز دارد و روی JVM
 * (Robolectric) اجرا نمی‌شود؛ به همین دلیل این آزمون روی دستگاه یا شبیه‌ساز اجرا می‌شود:
 *
 * ```
 * ./gradlew connectedDebugAndroidTest
 * ```
 */
@RunWith(AndroidJUnit4::class)
class InvoicePdfInstrumentedTest {

    private val settings = ShopSettings(
        shopName = "گالری طلا و جواهر زرنگار",
        phone = "02188776655",
        address = "تهران، بازار بزرگ طلافروشان",
        cardNumber = "6037-9912-3456-7890",
        goldRatePerGram18k = 3_500_000,
    )

    private fun sampleInvoice(): Invoice {
        val items = listOf(
            SaleItem(
                title = "انگشتر طرح ونکلیف",
                karat = 750,
                weightGrams = 3.450,
                wagePercent = 9.0,
                profitPercent = 7.0,
            ),
            SaleItem(
                title = "سکه تمام بهار آزادی",
                pricingMode = PricingMode.FIXED,
                fixedPrice = 92_500_000,
                vatExempt = true,
            ),
        )
        val totals = GoldPricing.priceInvoice(items, settings.pricing())
        return Invoice(
            id = 1,
            number = "1403-0001",
            createdAt = System.currentTimeMillis(),
            customerName = "مریم احمدی",
            customerPhone = "09121234567",
            goldRateSnapshot = settings.goldRatePerGram18k,
            vatPercentSnapshot = settings.vatPercent,
            grossBeforeTax = totals.grossBeforeTax,
            vatTotal = totals.vatTotal,
            roundingAdjustment = totals.roundingAdjustment,
            payable = totals.payable,
            paidAmount = totals.payable,
            paymentMethod = PaymentMethod.CASH,
            status = InvoiceStatus.PAID,
            lines = totals.lines.map { line ->
                InvoiceLine(
                    invoiceId = 1,
                    title = line.item.title,
                    karat = line.item.karat,
                    weightGrams = line.item.weightGrams,
                    quantity = line.quantity,
                    ratePerGram = line.ratePerGram,
                    goldValue = line.unitGoldValue * line.quantity,
                    wage = line.unitWage * line.quantity,
                    profit = line.unitProfit * line.quantity,
                    stoneValue = line.unitStoneValue * line.quantity,
                    vat = line.vat,
                    total = line.total,
                )
            },
        )
    }

    @Test
    fun generatesAValidPdfFile() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext as Context

        val file = InvoicePdfGenerator(context).generateFile(sampleInvoice(), settings)

        assertTrue("فایل PDF ساخته نشد", file.exists())
        assertTrue("فایل PDF بیش از حد کوچک است", file.length() > 1_000)
        assertEquals(
            "%PDF",
            file.inputStream().use { stream ->
                ByteArray(4).also { stream.read(it) }
            }.toString(Charsets.US_ASCII),
        )
    }
}
