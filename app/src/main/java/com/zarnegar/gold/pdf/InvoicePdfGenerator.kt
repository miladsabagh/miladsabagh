package com.zarnegar.gold.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.res.ResourcesCompat
import com.zarnegar.gold.R
import com.zarnegar.gold.core.JalaliDate
import com.zarnegar.gold.core.PersianText
import com.zarnegar.gold.domain.model.Invoice
import com.zarnegar.gold.domain.model.InvoiceLine
import com.zarnegar.gold.domain.model.ShopSettings
import java.io.File
import java.io.OutputStream
import kotlin.math.abs

/**
 * تولید فاکتور PDF در قطع A4 با چیدمان راست‌چین.
 *
 * اندازه‌ها بر حسب «پوینت» (۱/۷۲ اینچ) است؛ برگ A4 برابر ۵۹۵×۸۴۲ پوینت.
 */
class InvoicePdfGenerator(private val context: Context) {

    private companion object {
        const val PAGE_WIDTH = 595
        const val PAGE_HEIGHT = 842
        const val MARGIN = 32f
        const val CONTENT_WIDTH = PAGE_WIDTH - 2 * MARGIN

        const val GOLD = 0xFF8A6300.toInt()
        const val GOLD_LIGHT = 0xFFFFF3D0.toInt()
        const val INK = 0xFF1F1B13.toInt()
        const val MUTED = 0xFF6B6355.toInt()
        const val LINE = 0xFFD9CDB4.toInt()

        /** عرض ستون‌ها از راست به چپ */
        val COLUMNS = listOf(
            "ردیف" to 24f,
            "شرح کالا" to 128f,
            "عیار" to 34f,
            "وزن (گرم)" to 44f,
            "تعداد" to 28f,
            "ارزش طلا" to 62f,
            "اجرت" to 52f,
            "سود" to 45f,
            "مالیات" to 50f,
            "مبلغ کل" to 64f,
        )
    }

    private val regular: Typeface =
        ResourcesCompat.getFont(context, R.font.vazirmatn_regular) ?: Typeface.DEFAULT
    private val bold: Typeface =
        ResourcesCompat.getFont(context, R.font.vazirmatn_bold) ?: Typeface.DEFAULT_BOLD
    private val semiBold: Typeface =
        ResourcesCompat.getFont(context, R.font.vazirmatn_semibold) ?: bold

    private fun paint(
        size: Float,
        typeface: Typeface = regular,
        color: Int = INK,
        align: Paint.Align = Paint.Align.RIGHT,
    ) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.textSize = size
        this.typeface = typeface
        this.color = color
        this.textAlign = align
    }

    /** فایل PDF فاکتور را در حافظهٔ موقت می‌سازد و برمی‌گرداند. */
    fun generateFile(invoice: Invoice, settings: ShopSettings): File {
        val dir = File(context.cacheDir, "invoices").apply { mkdirs() }
        val file = File(dir, "invoice-${invoice.number}.pdf")
        file.outputStream().use { write(invoice, settings, it) }
        return file
    }

    fun write(invoice: Invoice, settings: ShopSettings, output: OutputStream) {
        val document = PdfDocument()
        val chunks = paginate(invoice.lines)
        chunks.forEachIndexed { index, lines ->
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, index + 1).create()
            val page = document.startPage(pageInfo)
            drawPage(
                canvas = page.canvas,
                invoice = invoice,
                settings = settings,
                lines = lines,
                startIndex = chunks.take(index).sumOf { it.size },
                pageNumber = index + 1,
                pageCount = chunks.size,
                isLastPage = index == chunks.lastIndex,
            )
            document.finishPage(page)
        }
        document.writeTo(output)
        document.close()
    }

    /**
     * صفحهٔ اول فاکتور را روی یک بوم دلخواه (به اندازهٔ ۵۹۵×۸۴۲) رسم می‌کند؛
     * برای پیش‌نمایش و آزمون‌های تصویری.
     */
    fun renderFirstPage(canvas: Canvas, invoice: Invoice, settings: ShopSettings) {
        canvas.drawColor(Color.WHITE)
        val pages = paginate(invoice.lines)
        drawPage(
            canvas = canvas,
            invoice = invoice,
            settings = settings,
            lines = pages.first(),
            startIndex = 0,
            pageNumber = 1,
            pageCount = pages.size,
            isLastPage = pages.size == 1,
        )
    }

    private fun paginate(lines: List<InvoiceLine>): List<List<InvoiceLine>> {
        if (lines.isEmpty()) return listOf(emptyList())
        val firstPage = 12
        val nextPages = 22
        val result = mutableListOf<List<InvoiceLine>>()
        var index = 0
        while (index < lines.size) {
            val size = if (result.isEmpty()) firstPage else nextPages
            result.add(lines.subList(index, minOf(index + size, lines.size)))
            index += size
        }
        return result
    }

    private fun drawPage(
        canvas: Canvas,
        invoice: Invoice,
        settings: ShopSettings,
        lines: List<InvoiceLine>,
        startIndex: Int,
        pageNumber: Int,
        pageCount: Int,
        isLastPage: Boolean,
    ) {
        val right = PAGE_WIDTH - MARGIN
        val left = MARGIN
        var y = drawHeader(canvas, invoice, settings, right, left)
        y = drawCustomerBox(canvas, invoice, settings, right, left, y)
        y = drawTable(canvas, invoice, settings, lines, startIndex, right, left, y)
        if (isLastPage) {
            y = drawTotals(canvas, invoice, settings, right, left, y)
            drawFooter(canvas, settings, right, left, y)
        }
        drawPageNumber(canvas, pageNumber, pageCount)
    }

    private fun drawHeader(
        canvas: Canvas,
        invoice: Invoice,
        settings: ShopSettings,
        right: Float,
        left: Float,
    ): Float {
        canvas.drawRect(
            0f,
            0f,
            PAGE_WIDTH.toFloat(),
            8f,
            Paint().apply { color = GOLD },
        )

        var y = 52f
        canvas.drawText(settings.shopName, right, y, paint(17f, bold, GOLD))

        y += 16f
        val subtitle = buildList {
            if (settings.ownerName.isNotBlank()) add(settings.ownerName)
            if (settings.phone.isNotBlank()) add("تلفن: ${PersianText.formatCode(settings.phone)}")
        }.joinToString(" • ")
        if (subtitle.isNotBlank()) {
            canvas.drawText(subtitle, right, y, paint(9f, regular, MUTED))
            y += 13f
        }
        if (settings.address.isNotBlank()) {
            canvas.drawText(settings.address, right, y, paint(9f, regular, MUTED))
            y += 13f
        }
        if (settings.economicCode.isNotBlank()) {
            canvas.drawText(
                "کد اقتصادی: ${PersianText.formatCode(settings.economicCode)}",
                right,
                y,
                paint(9f, regular, MUTED),
            )
            y += 13f
        }

        // کادر عنوان و شمارهٔ فاکتور در سمت چپ
        val boxWidth = 190f
        val box = RectF(left, 30f, left + boxWidth, 104f)
        canvas.drawRoundRect(box, 10f, 10f, Paint().apply { color = GOLD_LIGHT })
        canvas.drawRoundRect(
            box,
            10f,
            10f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 0.8f
                color = GOLD
            },
        )
        val boxRight = box.right - 12f
        canvas.drawText("فاکتور فروش کالا و خدمات", boxRight, box.top + 22f, paint(11f, bold, GOLD))
        canvas.drawText(
            "شمارهٔ فاکتور: ${PersianText.formatCode(invoice.number)}",
            boxRight,
            box.top + 40f,
            paint(9f, regular, INK),
        )
        canvas.drawText(
            "تاریخ: ${JalaliDate.fromEpochMillis(invoice.createdAt).formatNumeric()}" +
                "  ساعت: ${JalaliDate.formatTime(invoice.createdAt)}",
            boxRight,
            box.top + 56f,
            paint(9f, regular, INK),
        )

        return maxOf(y, box.bottom + 16f)
    }

    private fun drawCustomerBox(
        canvas: Canvas,
        invoice: Invoice,
        settings: ShopSettings,
        right: Float,
        left: Float,
        top: Float,
    ): Float {
        val box = RectF(left, top, right, top + 56f)
        canvas.drawRoundRect(
            box,
            8f,
            8f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 0.8f
                color = LINE
            },
        )
        canvas.drawText("مشخصات خریدار", right - 10f, top + 17f, paint(9.5f, semiBold, GOLD))

        val row1 = top + 33f
        canvas.drawText(
            "نام: ${invoice.customerName}",
            right - 10f,
            row1,
            paint(9f, regular, INK),
        )
        if (invoice.customerPhone.isNotBlank()) {
            canvas.drawText(
                "تلفن: ${PersianText.formatCode(invoice.customerPhone)}",
                right - 200f,
                row1,
                paint(9f, regular, INK),
            )
        }
        if (invoice.customerNationalCode.isNotBlank()) {
            canvas.drawText(
                "کد ملی: ${PersianText.formatCode(invoice.customerNationalCode)}",
                right - 340f,
                row1,
                paint(9f, regular, INK),
            )
        }
        canvas.drawText(
            "نرخ پایهٔ هر گرم طلای ۱۸ عیار در زمان صدور: " +
                "${PersianText.formatNumber(invoice.goldRateSnapshot)} ${settings.currencyLabel}",
            right - 10f,
            top + 48f,
            paint(8.5f, regular, MUTED),
        )
        return box.bottom + 14f
    }

    private fun drawTable(
        canvas: Canvas,
        invoice: Invoice,
        settings: ShopSettings,
        lines: List<InvoiceLine>,
        startIndex: Int,
        right: Float,
        left: Float,
        top: Float,
    ): Float {
        val headerHeight = 22f
        val rowHeight = 20f

        val headerRect = RectF(left, top, right, top + headerHeight)
        canvas.drawRect(headerRect, Paint().apply { color = GOLD })

        var x = right
        val boundaries = mutableListOf<Pair<Float, Float>>() // (rightX, width)
        COLUMNS.forEach { (title, width) ->
            boundaries.add(x to width)
            drawFitted(
                canvas,
                title,
                x - 4f,
                width - 8f,
                top + 15f,
                paint(8f, semiBold, Color.WHITE),
            )
            x -= width
        }

        val gridPaint = Paint().apply {
            color = LINE
            style = Paint.Style.STROKE
            strokeWidth = 0.6f
        }

        var y = top + headerHeight
        lines.forEachIndexed { index, line ->
            val rowRect = RectF(left, y, right, y + rowHeight)
            if (index % 2 == 1) {
                canvas.drawRect(rowRect, Paint().apply { color = 0xFFFBF6EA.toInt() })
            }
            canvas.drawRect(rowRect, gridPaint)

            val baseline = y + 13.5f
            val values = listOf(
                PersianText.formatNumber((startIndex + index + 1).toLong()),
                line.title,
                if (line.karat > 0 && line.goldValue > 0) {
                    PersianText.formatNumber(line.karat.toLong())
                } else {
                    "—"
                },
                if (line.weightGrams > 0) {
                    PersianText.formatGrams(line.weightGrams, withUnit = false)
                } else {
                    "—"
                },
                PersianText.formatNumber(line.quantity.toLong()),
                if (line.goldValue > 0) PersianText.formatNumber(line.goldValue) else "—",
                if (line.wage > 0) PersianText.formatNumber(line.wage) else "—",
                if (line.profit > 0) PersianText.formatNumber(line.profit) else "—",
                PersianText.formatNumber(line.vat),
                PersianText.formatNumber(line.total),
            )
            values.forEachIndexed { columnIndex, value ->
                val (columnRight, width) = boundaries[columnIndex]
                val typeface = if (columnIndex == COLUMNS.lastIndex) semiBold else regular
                drawFitted(
                    canvas,
                    value,
                    columnRight - 4f,
                    width - 8f,
                    baseline,
                    paint(8f, typeface, INK),
                )
            }
            y += rowHeight
        }

        // خطوط عمودی ستون‌ها
        var lineX = right
        COLUMNS.forEach { (_, width) ->
            canvas.drawLine(lineX, top, lineX, y, gridPaint)
            lineX -= width
        }
        canvas.drawLine(lineX, top, lineX, y, gridPaint)

        return y + 14f
    }

    private fun drawTotals(
        canvas: Canvas,
        invoice: Invoice,
        settings: ShopSettings,
        right: Float,
        left: Float,
        top: Float,
    ): Float {
        val currency = settings.currencyLabel
        val boxWidth = 250f
        val boxLeft = right - boxWidth

        val rows = buildList {
            add("جمع کل پیش از مالیات" to invoice.grossBeforeTax)
            val discount = invoice.itemDiscountTotal + invoice.invoiceDiscount
            if (discount > 0) add("تخفیف" to -discount)
            add(
                "مالیات بر ارزش افزوده " +
                    "(${PersianText.formatPercent(invoice.vatPercentSnapshot)})" to invoice.vatTotal,
            )
            if (invoice.roundingAdjustment != 0L) {
                val label =
                    if (invoice.roundingAdjustment > 0) "گرد کردن (اضافه)" else "گرد کردن (کسر)"
                add(label to abs(invoice.roundingAdjustment))
            }
        }

        var y = top
        rows.forEach { (label, value) ->
            canvas.drawText(label, right, y + 11f, paint(9f, regular, MUTED))
            canvas.drawText(
                "${PersianText.formatNumber(value)} $currency",
                boxLeft,
                y + 11f,
                paint(9f, regular, INK, Paint.Align.LEFT),
            )
            y += 17f
        }

        val payableRect = RectF(boxLeft - 6f, y + 2f, right + 6f, y + 32f)
        canvas.drawRoundRect(payableRect, 8f, 8f, Paint().apply { color = GOLD_LIGHT })
        canvas.drawText("مبلغ قابل پرداخت", right, y + 22f, paint(11f, bold, GOLD))
        canvas.drawText(
            "${PersianText.formatNumber(invoice.payable)} $currency",
            boxLeft,
            y + 22f,
            paint(11f, bold, GOLD, Paint.Align.LEFT),
        )
        y += 40f

        canvas.drawText(
            "به حروف: ${PersianText.numberToWords(invoice.payable)} $currency",
            right,
            y + 10f,
            paint(9f, semiBold, INK),
        )
        y += 20f

        val payment = buildString {
            append("روش پرداخت: ${invoice.paymentMethod.label}")
            append("  |  پرداخت‌شده: ${PersianText.formatNumber(invoice.paidAmount)} $currency")
            if (invoice.remaining > 0) {
                append("  |  مانده: ${PersianText.formatNumber(invoice.remaining)} $currency")
            }
            append("  |  وضعیت: ${invoice.status.label}")
        }
        canvas.drawText(payment, right, y + 10f, paint(9f, regular, INK))
        y += 18f

        // خلاصهٔ وزنی در سمت چپ
        val totalWeight = invoice.lines.sumOf { it.weightGrams * it.quantity }
        if (totalWeight > 0) {
            canvas.drawText(
                "وزن کل: ${PersianText.formatGrams(totalWeight)}",
                left,
                top + 11f,
                paint(9f, regular, MUTED, Paint.Align.LEFT),
            )
        }
        if (invoice.note.isNotBlank()) {
            canvas.drawText("توضیحات: ${invoice.note}", right, y + 10f, paint(8.5f, regular, MUTED))
            y += 16f
        }
        return y + 10f
    }

    private fun drawFooter(
        canvas: Canvas,
        settings: ShopSettings,
        right: Float,
        left: Float,
        top: Float,
    ) {
        var y = maxOf(top, PAGE_HEIGHT - 120f)
        if (settings.cardNumber.isNotBlank()) {
            canvas.drawText(
                "شمارهٔ کارت: " +
                    PersianText.formatCode(settings.cardNumber),
                right,
                y,
                paint(9f, regular, INK),
            )
            y += 16f
        }
        if (settings.invoiceFooterNote.isNotBlank()) {
            canvas.drawText(settings.invoiceFooterNote, right, y, paint(8.5f, regular, MUTED))
            y += 16f
        }

        val signatureY = PAGE_HEIGHT - 56f
        val dash = Paint().apply {
            color = LINE
            strokeWidth = 0.8f
        }
        canvas.drawLine(right - 150f, signatureY, right, signatureY, dash)
        canvas.drawText("مهر و امضای فروشنده", right, signatureY + 14f, paint(8.5f, regular, MUTED))
        canvas.drawLine(left, signatureY, left + 150f, signatureY, dash)
        canvas.drawText(
            "امضای خریدار",
            left,
            signatureY + 14f,
            paint(8.5f, regular, MUTED, Paint.Align.LEFT),
        )
    }

    private fun drawPageNumber(canvas: Canvas, pageNumber: Int, pageCount: Int) {
        canvas.drawText(
            "صفحهٔ ${PersianText.toPersianDigits(pageNumber.toString())} از " +
                PersianText.toPersianDigits(pageCount.toString()),
            PAGE_WIDTH / 2f,
            PAGE_HEIGHT - 20f,
            paint(8f, regular, MUTED, Paint.Align.CENTER),
        )
    }

    /** متن را طوری می‌نویسد که در عرض ستون جا شود؛ در صورت نیاز اندازهٔ قلم کوچک می‌شود. */
    private fun drawFitted(
        canvas: Canvas,
        text: String,
        rightX: Float,
        maxWidth: Float,
        baselineY: Float,
        paint: Paint,
    ) {
        var value = text
        while (paint.measureText(value) > maxWidth && paint.textSize > 5f) {
            paint.textSize = paint.textSize - 0.5f
        }
        if (paint.measureText(value) > maxWidth) {
            while (value.length > 1 && paint.measureText("$value…") > maxWidth) {
                value = value.dropLast(1)
            }
            value = "$value…"
        }
        canvas.drawText(value, rightX, baselineY, paint)
    }
}
