package com.zarnegar.gold

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.test.core.app.ApplicationProvider
import com.zarnegar.gold.domain.model.Invoice
import com.zarnegar.gold.domain.model.InvoiceLine
import com.zarnegar.gold.domain.model.InvoiceStatus
import com.zarnegar.gold.domain.model.PaymentMethod
import com.zarnegar.gold.domain.model.SaleItem
import com.zarnegar.gold.domain.model.ShopSettings
import com.zarnegar.gold.domain.pricing.GoldPricing
import com.zarnegar.gold.pdf.InvoicePdfGenerator
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * صفحهٔ فاکتور را با همان کدی که PDF را رسم می‌کند روی یک Bitmap می‌کشد تا چیدمان
 * راست‌چین و محتوای فاکتور به‌صورت تصویری قابل بررسی باشد.
 *
 * تولید خودِ فایل PDF در `InvoicePdfInstrumentedTest` (روی دستگاه) آزموده می‌شود،
 * چون `PdfDocument` به گرافیک بومی اندروید نیاز دارد.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class InvoicePdfRenderTest {

    private val settings = ShopSettings(
        shopName = "گالری طلا و جواهر زرنگار",
        ownerName = "میلاد صباغ",
        phone = "02188776655",
        address = "تهران، بازار بزرگ طلافروشان، پاساژ زرین، پلاک ۱۴",
        economicCode = "411356789021",
        cardNumber = "6037-9912-3456-7890",
        goldRatePerGram18k = 3_500_000,
        vatPercent = 10.0,
        roundTo = 1_000,
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
                title = "گردنبند زنجیر کارتیه",
                karat = 750,
                weightGrams = 7.820,
                wagePercent = 12.0,
                profitPercent = 7.0,
            ),
            SaleItem(
                title = "گوشواره حلقه‌ای نگین‌دار",
                karat = 750,
                weightGrams = 2.980,
                stoneWeightGrams = 0.180,
                stoneValue = 1_800_000,
                wagePercent = 14.0,
                profitPercent = 7.0,
                quantity = 2,
            ),
            SaleItem(
                title = "سکه تمام بهار آزادی",
                pricingMode = com.zarnegar.gold.domain.model.PricingMode.FIXED,
                fixedPrice = 92_500_000,
                vatExempt = true,
            ),
        )
        val totals = GoldPricing.priceInvoice(items, settings.pricing(), invoiceDiscount = 2_000_000)

        return Invoice(
            id = 1,
            number = "1403-0007",
            createdAt = 1_723_400_000_000, // ۲۱ مرداد ۱۴۰۳
            customerName = "مریم احمدی",
            customerPhone = "09121234567",
            customerNationalCode = "0064512378",
            goldRateSnapshot = settings.goldRatePerGram18k,
            vatPercentSnapshot = settings.vatPercent,
            grossBeforeTax = totals.grossBeforeTax,
            itemDiscountTotal = totals.itemDiscountTotal,
            invoiceDiscount = totals.invoiceDiscount,
            vatTotal = totals.vatTotal,
            roundingAdjustment = totals.roundingAdjustment,
            payable = totals.payable,
            paidAmount = totals.payable,
            paymentMethod = PaymentMethod.CARD,
            status = InvoiceStatus.PAID,
            note = "سرویس عروس، تحویل حضوری",
            lines = totals.lines.mapIndexed { index, line ->
                InvoiceLine(
                    id = index.toLong() + 1,
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
                    discount = line.discount,
                    vat = line.vat,
                    total = line.total,
                )
            },
        )
    }

    @Test
    fun `renders the invoice page to a bitmap`() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val invoice = sampleInvoice()

        val bitmap = Bitmap.createBitmap(595, 842, Bitmap.Config.ARGB_8888)
        InvoicePdfGenerator(context).renderFirstPage(Canvas(bitmap), invoice, settings)

        val outputDir = File("build/screenshots").apply { mkdirs() }
        File(outputDir, "invoice_page.png").outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }

        // صفحه نباید سفیدِ خالی باشد
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        val nonWhite = pixels.count { it != android.graphics.Color.WHITE }
        assertTrue("صفحهٔ فاکتور خالی رسم شده است ($nonWhite پیکسل)", nonWhite > 5_000)
    }
}
