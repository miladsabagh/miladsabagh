package ir.zarrin.gold.pdf

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import ir.zarrin.gold.R
import ir.zarrin.gold.data.InvoiceWithItems
import ir.zarrin.gold.data.StoreSettings
import ir.zarrin.gold.util.JalaliDate
import ir.zarrin.gold.util.PersianFormat
import java.io.File

/**
 * تولید فاکتور PDF (اندازه A4) با چیدمان راست‌به‌چپ و فونت فارسی.
 */
object InvoicePdfGenerator {

    const val PAGE_WIDTH = 595
    const val PAGE_HEIGHT = 842
    private const val MARGIN = 36f
    private val RIGHT = PAGE_WIDTH - MARGIN
    private const val LEFT = MARGIN

    fun generate(context: Context, data: InvoiceWithItems, settings: StoreSettings): File {
        val doc = PdfDocument()
        val page = doc.startPage(
            PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        )
        drawInvoice(context, page.canvas, data, settings)
        doc.finishPage(page)

        val dir = File(context.cacheDir, "invoices").apply { mkdirs() }
        val file = File(dir, "invoice_${data.invoice.number}.pdf")
        file.outputStream().use { doc.writeTo(it) }
        doc.close()
        return file
    }

    /** ترسیم کامل فاکتور روی هر Canvas (صفحه PDF یا Bitmap). */
    fun drawInvoice(
        context: Context,
        c: Canvas,
        data: InvoiceWithItems,
        settings: StoreSettings,
    ) {
        val vazir = ResourcesCompat.getFont(context, R.font.vazirmatn_regular)
            ?: Typeface.DEFAULT
        val vazirBold = ResourcesCompat.getFont(context, R.font.vazirmatn_bold)
            ?: Typeface.DEFAULT_BOLD

        val title = Paint().apply {
            typeface = vazirBold; textSize = 18f; color = Color.rgb(61, 47, 0)
            textAlign = Paint.Align.CENTER; isAntiAlias = true
        }
        val bold = Paint().apply {
            typeface = vazirBold; textSize = 11f; color = Color.BLACK
            textAlign = Paint.Align.RIGHT; isAntiAlias = true
        }
        val normal = Paint().apply {
            typeface = vazir; textSize = 10f; color = Color.BLACK
            textAlign = Paint.Align.RIGHT; isAntiAlias = true
        }
        val small = Paint().apply {
            typeface = vazir; textSize = 8.5f; color = Color.DKGRAY
            textAlign = Paint.Align.CENTER; isAntiAlias = true
        }
        val line = Paint().apply {
            color = Color.rgb(154, 123, 10); strokeWidth = 1.2f
        }
        val thinLine = Paint().apply { color = Color.LTGRAY; strokeWidth = 0.6f }
        val headerBg = Paint().apply { color = Color.rgb(246, 232, 184) }

        var y = MARGIN + 12f

        // سربرگ
        c.drawText(settings.storeName, PAGE_WIDTH / 2f, y, title)
        y += 16f
        val contact = listOfNotNull(
            settings.storePhone.ifBlank { null }?.let { "تلفن: ${PersianFormat.toPersianDigits(it)}" },
            settings.storeAddress.ifBlank { null },
        ).joinToString("  |  ")
        if (contact.isNotBlank()) {
            c.drawText(contact, PAGE_WIDTH / 2f, y, small)
            y += 12f
        }
        y += 4f
        c.drawLine(LEFT, y, RIGHT, y, line)
        y += 20f

        // مشخصات فاکتور
        val inv = data.invoice
        c.drawText("فاکتور فروش شماره ${PersianFormat.toPersianDigits(inv.number.toString())}", RIGHT, y, bold)
        drawLeft(c, "تاریخ: ${JalaliDate.format(inv.date)}", LEFT, y, normal)
        y += 16f
        c.drawText("خریدار: ${inv.customerName}", RIGHT, y, normal)
        if (inv.customerPhone.isNotBlank()) {
            drawLeft(c, "تلفن: ${PersianFormat.toPersianDigits(inv.customerPhone)}", LEFT, y, normal)
        }
        y += 14f
        c.drawText(
            "مبنای محاسبه: هر گرم طلای ۱۸ عیار ${PersianFormat.formatCurrency(inv.goldPricePerGram18k)}",
            RIGHT, y, normal
        )
        y += 18f

        // جدول اقلام — ستون‌ها از راست به چپ
        val tableTop = y
        val colEdges = floatArrayOf(RIGHT, RIGHT - 22f, RIGHT - 150f, RIGHT - 185f, RIGHT - 240f, RIGHT - 268f, RIGHT - 348f, RIGHT - 428f, LEFT)
        val headers = listOf("#", "شرح کالا", "عیار", "وزن (گرم)", "تعداد", "بهای طلا", "اجرت", "جمع (تومان)")

        c.drawRect(LEFT, tableTop, RIGHT, tableTop + 18f, headerBg)
        val headPaint = Paint(bold).apply { textSize = 9.5f; textAlign = Paint.Align.CENTER }
        for (i in headers.indices) {
            val cx = (colEdges[i] + colEdges[i + 1]) / 2f
            c.drawText(headers[i], cx, tableTop + 13f, headPaint)
        }
        y = tableTop + 18f

        val cell = Paint(normal).apply { textSize = 9.5f; textAlign = Paint.Align.CENTER }
        data.items.forEachIndexed { index, item ->
            val rowH = 17f
            for (i in headers.indices) {
                val cx = (colEdges[i] + colEdges[i + 1]) / 2f
                val text = when (i) {
                    0 -> PersianFormat.toPersianDigits((index + 1).toString())
                    1 -> item.name
                    2 -> PersianFormat.toPersianDigits(item.karat.toString())
                    3 -> PersianFormat.formatWeight(item.weightGrams).removeSuffix(" گرم")
                    4 -> PersianFormat.formatNumber(item.quantity.toLong())
                    5 -> PersianFormat.formatNumber(item.goldValue)
                    6 -> PersianFormat.formatNumber(item.wage)
                    else -> PersianFormat.formatNumber(item.lineTotal)
                }
                c.drawText(text, cx, y + 12f, cell)
            }
            y += rowH
            c.drawLine(LEFT, y, RIGHT, y, thinLine)
        }
        y += 20f

        // جمع‌بندی
        val rows = buildList {
            add("جمع بهای طلا" to inv.goldValue)
            add("جمع اجرت ساخت" to inv.wage)
            add("سود فروشنده" to inv.profit)
            add("مالیات بر ارزش افزوده" to inv.tax)
            if (inv.discount > 0) add("تخفیف" to -inv.discount)
        }
        rows.forEach { (label, value) ->
            c.drawText(label, RIGHT, y, normal)
            val sign = if (value < 0) "− " else ""
            drawLeft(c, sign + PersianFormat.formatCurrency(kotlin.math.abs(value)), LEFT + 160f, y, normal)
            y += 15f
        }
        y += 4f
        c.drawLine(LEFT, y, RIGHT, y, line)
        y += 18f
        val totalPaint = Paint(bold).apply { textSize = 13f }
        c.drawText("مبلغ قابل پرداخت", RIGHT, y, totalPaint)
        drawLeft(c, PersianFormat.formatCurrency(inv.total), LEFT + 160f, y, totalPaint)
        y += 26f

        c.drawText(
            "وضعیت پرداخت: " + if (inv.paid) "پرداخت شده" else "پرداخت نشده",
            RIGHT, y, normal
        )
        y += 30f
        c.drawText("امضای فروشنده", RIGHT - 30f, y, normal)
        drawLeft(c, "امضای خریدار", LEFT + 90f, y, normal)

        val footer = Paint(small).apply { textAlign = Paint.Align.CENTER }
        c.drawText(
            "این فاکتور توسط اپلیکیشن زرین صادر شده است.",
            PAGE_WIDTH / 2f, PAGE_HEIGHT - 24f, footer
        )
    }

    fun share(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "اشتراک‌گذاری فاکتور"))
    }

    private fun drawLeft(c: Canvas, text: String, x: Float, y: Float, base: Paint) {
        val p = Paint(base).apply { textAlign = Paint.Align.LEFT }
        c.drawText(text, x, y, p)
    }
}
