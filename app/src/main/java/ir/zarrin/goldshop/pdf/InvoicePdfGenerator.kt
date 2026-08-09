package ir.zarrin.goldshop.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import ir.zarrin.goldshop.R
import ir.zarrin.goldshop.data.db.InvoiceItemEntity
import ir.zarrin.goldshop.data.db.InvoiceWithItems
import ir.zarrin.goldshop.data.settings.AppSettings
import ir.zarrin.goldshop.domain.InvoiceStatus
import ir.zarrin.goldshop.domain.PaymentMethod
import ir.zarrin.goldshop.domain.PricingMode
import ir.zarrin.goldshop.util.NumberToPersianWords
import ir.zarrin.goldshop.util.formatWeight
import ir.zarrin.goldshop.util.groupDigits
import ir.zarrin.goldshop.util.toJalaliDateTimeLabel
import ir.zarrin.goldshop.util.toPersianDigits
import java.io.File
import java.io.FileOutputStream

/**
 * ساخت فایل PDF فاکتور فروش در اندازه A4 با چیدمان راست‌به‌چپ.
 */
object InvoicePdfGenerator {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 32f
    private const val CONTENT_WIDTH = PAGE_WIDTH - 2 * MARGIN

    private val GOLD = Color.parseColor("#8A6A17")
    private val GOLD_SOFT = Color.parseColor("#FFF0C9")
    private val LINE = Color.parseColor("#C9BC9C")
    private val INK = Color.parseColor("#2E2712")
    private val MUTED = Color.parseColor("#6B6247")

    /** ستون‌های جدول از راست به چپ */
    private val COLUMNS = listOf(
        "ردیف" to 28f,
        "شرح کالا" to 148f,
        "عیار" to 34f,
        "وزن (گرم)" to 52f,
        "تعداد" to 34f,
        "نرخ هر گرم" to 68f,
        "اجرت و سود" to 68f,
        "مالیات" to 48f,
        "مبلغ کل" to 51f
    )

    fun generate(context: Context, data: InvoiceWithItems, settings: AppSettings): File {
        val regular = ResourcesCompat.getFont(context, R.font.vazirmatn_regular) ?: Typeface.DEFAULT
        val bold = ResourcesCompat.getFont(context, R.font.vazirmatn_bold) ?: Typeface.DEFAULT_BOLD

        val document = PdfDocument()
        val renderer = Renderer(document, regular, bold, settings)
        renderer.render(data)

        val dir = File(context.cacheDir, "invoices").apply { mkdirs() }
        val file = File(dir, "factor-${data.invoice.number}.pdf")
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        return file
    }

    private class Renderer(
        private val document: PdfDocument,
        private val regular: Typeface,
        private val bold: Typeface,
        private val settings: AppSettings
    ) {
        private var page: PdfDocument.Page? = null
        private var canvas: Canvas = Canvas()
        private var y = MARGIN
        private var pageNumber = 0

        private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = regular
            color = INK
            textSize = 9f
        }
        private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = LINE
            strokeWidth = 0.7f
        }
        private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

        fun render(data: InvoiceWithItems) {
            startPage()
            drawHeader(data)
            drawPartiesBox(data)
            var index = 1
            drawTableHeader()
            data.items.forEach { item ->
                if (y > PAGE_HEIGHT - 220f) {
                    finishPage()
                    startPage()
                    drawTableHeader()
                }
                drawItemRow(index++, item)
            }
            drawTotals(data)
            drawFooter(data)
            finishPage()
        }

        private fun startPage() {
            pageNumber += 1
            val info = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            page = document.startPage(info)
            canvas = page!!.canvas
            y = MARGIN
        }

        private fun finishPage() {
            page?.let { document.finishPage(it) }
            page = null
        }

        private fun drawHeader(data: InvoiceWithItems) {
            fillPaint.color = GOLD_SOFT
            canvas.drawRoundRect(RectF(MARGIN, y, MARGIN + CONTENT_WIDTH, y + 74f), 8f, 8f, fillPaint)
            strokePaint.color = GOLD
            canvas.drawRoundRect(RectF(MARGIN, y, MARGIN + CONTENT_WIDTH, y + 74f), 8f, 8f, strokePaint)
            strokePaint.color = LINE

            text(
                settings.shopName,
                MARGIN + CONTENT_WIDTH - 250f,
                y + 10f,
                250f,
                size = 15f,
                typeface = bold,
                color = GOLD,
                align = Layout.Alignment.ALIGN_NORMAL
            )
            val shopLines = buildList {
                if (settings.ownerName.isNotBlank()) add(settings.ownerName)
                if (settings.phone.isNotBlank()) add("تلفن: ${settings.phone.toPersianDigits()}")
                if (settings.address.isNotBlank()) add(settings.address)
            }.joinToString("  |  ")
            if (shopLines.isNotBlank()) {
                text(
                    shopLines,
                    MARGIN + CONTENT_WIDTH - 300f,
                    y + 32f,
                    300f,
                    size = 8.5f,
                    color = MUTED,
                    align = Layout.Alignment.ALIGN_NORMAL
                )
            }

            text(
                "فاکتور فروش طلا و جواهر",
                MARGIN + 8f,
                y + 10f,
                200f,
                size = 13f,
                typeface = bold,
                color = INK,
                align = Layout.Alignment.ALIGN_OPPOSITE
            )
            text(
                "شماره فاکتور: ${data.invoice.number.toPersianDigits()}",
                MARGIN + 8f,
                y + 32f,
                200f,
                size = 9f,
                align = Layout.Alignment.ALIGN_OPPOSITE
            )
            text(
                "تاریخ: ${data.invoice.dateMillis.toJalaliDateTimeLabel()}",
                MARGIN + 8f,
                y + 48f,
                200f,
                size = 9f,
                align = Layout.Alignment.ALIGN_OPPOSITE
            )
            y += 86f
        }

        private fun drawPartiesBox(data: InvoiceWithItems) {
            val boxHeight = 52f
            canvas.drawRoundRect(RectF(MARGIN, y, MARGIN + CONTENT_WIDTH, y + boxHeight), 6f, 6f, strokePaint)

            val half = CONTENT_WIDTH / 2f
            text(
                "خریدار: ${data.invoice.customerName}",
                MARGIN + half + 8f,
                y + 8f,
                half - 16f,
                size = 9.5f,
                typeface = bold,
                align = Layout.Alignment.ALIGN_NORMAL
            )
            val phone = data.invoice.customerPhone.ifBlank { "—" }
            text(
                "تلفن: ${phone.toPersianDigits()}",
                MARGIN + half + 8f,
                y + 27f,
                half - 16f,
                size = 9f,
                color = MUTED,
                align = Layout.Alignment.ALIGN_NORMAL
            )

            text(
                "نرخ روز طلای ۱۸ عیار: ${money(data.invoice.goldRate18)}",
                MARGIN + 8f,
                y + 8f,
                half - 16f,
                size = 9f,
                align = Layout.Alignment.ALIGN_OPPOSITE
            )
            text(
                "شیوه پرداخت: ${PaymentMethod.fromName(data.invoice.paymentMethod).label}" +
                    "   -   وضعیت: ${InvoiceStatus.fromName(data.invoice.status).label}",
                MARGIN + 8f,
                y + 27f,
                half - 16f,
                size = 9f,
                color = MUTED,
                align = Layout.Alignment.ALIGN_OPPOSITE
            )
            y += boxHeight + 12f
        }

        private fun drawTableHeader() {
            val rowHeight = 22f
            fillPaint.color = GOLD_SOFT
            canvas.drawRect(MARGIN, y, MARGIN + CONTENT_WIDTH, y + rowHeight, fillPaint)
            canvas.drawRect(MARGIN, y, MARGIN + CONTENT_WIDTH, y + rowHeight, strokePaint)

            var right = MARGIN + CONTENT_WIDTH
            COLUMNS.forEach { (title, width) ->
                val left = right - width
                text(
                    title,
                    left + 2f,
                    y + 6f,
                    width - 4f,
                    size = 8.5f,
                    typeface = bold,
                    align = Layout.Alignment.ALIGN_CENTER
                )
                if (left > MARGIN) canvas.drawLine(left, y, left, y + rowHeight, strokePaint)
                right = left
            }
            y += rowHeight
        }

        private fun drawItemRow(index: Int, item: InvoiceItemEntity) {
            val byWeight = PricingMode.fromName(item.pricingMode) == PricingMode.BY_WEIGHT
            val values = listOf(
                index.toPersianDigits(),
                item.name,
                if (byWeight) item.karat.toPersianDigits() else "—",
                if (byWeight) item.weightGrams.formatWeight() else "—",
                item.quantity.toPersianDigits(),
                if (byWeight) money(item.ratePerGram) else "مقطوع",
                if (byWeight) money(item.wage + item.profit) else "—",
                money(item.tax),
                money(item.lineTotal)
            )

            val nameWidth = COLUMNS[1].second - 6f
            val nameHeight = measure(item.name, nameWidth, 8.5f)
            val rowHeight = maxOf(22f, nameHeight + 10f)

            canvas.drawRect(MARGIN, y, MARGIN + CONTENT_WIDTH, y + rowHeight, strokePaint)
            var right = MARGIN + CONTENT_WIDTH
            COLUMNS.forEachIndexed { columnIndex, (_, width) ->
                val left = right - width
                text(
                    values[columnIndex],
                    left + 3f,
                    y + (rowHeight - if (columnIndex == 1) nameHeight else 11f) / 2f,
                    width - 6f,
                    size = 8.5f,
                    align = if (columnIndex == 1) Layout.Alignment.ALIGN_NORMAL else Layout.Alignment.ALIGN_CENTER
                )
                if (left > MARGIN) canvas.drawLine(left, y, left, y + rowHeight, strokePaint)
                right = left
            }
            y += rowHeight
        }

        private fun drawTotals(data: InvoiceWithItems) {
            y += 12f
            val invoice = data.invoice
            val boxWidth = 250f
            val left = MARGIN
            val rows = buildList {
                add("جمع بهای طلا" to money(invoice.totalGoldValue))
                add("جمع اجرت ساخت" to money(invoice.totalWage))
                add("جمع سود فروشنده" to money(invoice.totalProfit))
                if (invoice.totalStone > 0) add("بهای نگین و سنگ" to money(invoice.totalStone))
                add("مالیات بر ارزش افزوده (${invoice.taxPercent.toInt().toPersianDigits()}٪)" to money(invoice.totalTax))
                if (invoice.itemsDiscount + invoice.invoiceDiscount > 0) {
                    add("تخفیف" to money(invoice.itemsDiscount + invoice.invoiceDiscount))
                }
            }

            var rowY = y
            rows.forEach { (title, value) ->
                text(title, left + boxWidth - 130f, rowY, 130f, size = 9f, color = MUTED, align = Layout.Alignment.ALIGN_NORMAL)
                text(value, left, rowY, boxWidth - 134f, size = 9f, align = Layout.Alignment.ALIGN_OPPOSITE)
                rowY += 15f
            }

            val summaryTop = y
            val summaryLeft = MARGIN + CONTENT_WIDTH - 250f
            fillPaint.color = GOLD_SOFT
            canvas.drawRoundRect(RectF(summaryLeft, summaryTop, summaryLeft + 250f, summaryTop + 74f), 6f, 6f, fillPaint)
            strokePaint.color = GOLD
            canvas.drawRoundRect(RectF(summaryLeft, summaryTop, summaryLeft + 250f, summaryTop + 74f), 6f, 6f, strokePaint)
            strokePaint.color = LINE

            text("مبلغ قابل پرداخت", summaryLeft + 120f, summaryTop + 10f, 122f, size = 10f, typeface = bold, align = Layout.Alignment.ALIGN_NORMAL)
            text(money(invoice.grandTotal), summaryLeft + 8f, summaryTop + 8f, 115f, size = 12f, typeface = bold, color = GOLD, align = Layout.Alignment.ALIGN_OPPOSITE)
            text("پرداخت شده", summaryLeft + 120f, summaryTop + 32f, 122f, size = 9f, color = MUTED, align = Layout.Alignment.ALIGN_NORMAL)
            text(money(invoice.paidAmount), summaryLeft + 8f, summaryTop + 32f, 115f, size = 9f, align = Layout.Alignment.ALIGN_OPPOSITE)
            val remaining = (invoice.grandTotal - invoice.paidAmount).coerceAtLeast(0L)
            text("مانده", summaryLeft + 120f, summaryTop + 52f, 122f, size = 9f, color = MUTED, align = Layout.Alignment.ALIGN_NORMAL)
            text(money(remaining), summaryLeft + 8f, summaryTop + 52f, 115f, size = 9f, align = Layout.Alignment.ALIGN_OPPOSITE)

            y = maxOf(rowY, summaryTop + 74f) + 10f

            val words = NumberToPersianWords.amountToWords(
                settings.display(invoice.grandTotal),
                settings.currencyLabel
            )
            text("مبلغ به حروف: $words", MARGIN + 4f, y, CONTENT_WIDTH - 8f, size = 9f, typeface = bold)
            y += measure("مبلغ به حروف: $words", CONTENT_WIDTH - 8f, 9f) + 8f

            if (invoice.totalWeight > 0) {
                text(
                    "جمع وزن کالاهای وزنی: ${invoice.totalWeight.formatWeight()} گرم" +
                        "   -   تعداد اقلام: ${data.items.sumOf { it.quantity }.toPersianDigits()}",
                    MARGIN + 4f,
                    y,
                    CONTENT_WIDTH - 8f,
                    size = 9f,
                    color = MUTED
                )
                y += 16f
            }

            if (invoice.note.isNotBlank()) {
                text("توضیحات: ${invoice.note}", MARGIN + 4f, y, CONTENT_WIDTH - 8f, size = 8.5f, color = MUTED)
                y += measure("توضیحات: ${invoice.note}", CONTENT_WIDTH - 8f, 8.5f) + 6f
            }
        }

        private fun drawFooter(data: InvoiceWithItems) {
            val footerY = PAGE_HEIGHT - MARGIN - 54f
            canvas.drawLine(MARGIN, footerY, MARGIN + CONTENT_WIDTH, footerY, strokePaint)
            text("مهر و امضای فروشنده", MARGIN + CONTENT_WIDTH - 180f, footerY + 10f, 180f, size = 9f, color = MUTED, align = Layout.Alignment.ALIGN_NORMAL)
            text("امضای خریدار", MARGIN, footerY + 10f, 180f, size = 9f, color = MUTED, align = Layout.Alignment.ALIGN_OPPOSITE)
            text(
                "این فاکتور توسط نرم‌افزار زرین صادر شده است.",
                MARGIN,
                footerY + 34f,
                CONTENT_WIDTH,
                size = 8f,
                color = MUTED,
                align = Layout.Alignment.ALIGN_CENTER
            )
        }

        private fun money(amount: Long): String =
            "${settings.display(amount).groupDigits()} ${settings.currencyLabel}"

        private fun paintFor(size: Float, typeface: Typeface, color: Int): TextPaint =
            TextPaint(textPaint).apply {
                this.textSize = size
                this.typeface = typeface
                this.color = color
            }

        private fun measure(value: String, width: Float, size: Float): Float =
            layoutOf(value, paintFor(size, regular, INK), width, Layout.Alignment.ALIGN_NORMAL).height.toFloat()

        private fun text(
            value: String,
            x: Float,
            top: Float,
            width: Float,
            size: Float = 9f,
            typeface: Typeface = regular,
            color: Int = INK,
            align: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL
        ) {
            val layout = layoutOf(value, paintFor(size, typeface, color), width, align)
            canvas.save()
            canvas.translate(x, top)
            layout.draw(canvas)
            canvas.restore()
        }

        private fun layoutOf(
            value: String,
            paint: TextPaint,
            width: Float,
            align: Layout.Alignment
        ): StaticLayout = StaticLayout.Builder
            .obtain(value, 0, value.length, paint, width.toInt().coerceAtLeast(1))
            .setAlignment(align)
            .setTextDirection(TextDirectionHeuristics.RTL)
            .setLineSpacing(1f, 1f)
            .setIncludePad(false)
            .build()
    }
}
