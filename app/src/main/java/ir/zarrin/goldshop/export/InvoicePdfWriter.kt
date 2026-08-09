package ir.zarrin.goldshop.export

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
import android.text.TextUtils
import androidx.core.content.res.ResourcesCompat
import ir.zarrin.goldshop.R
import ir.zarrin.goldshop.core.PersianNumbers
import ir.zarrin.goldshop.domain.model.Karat
import java.io.File
import java.io.FileOutputStream

/**
 * Renders an invoice onto A4 pages using [PdfDocument].
 *
 * Every string goes through [StaticLayout] with an RTL direction heuristic so Persian text is
 * shaped and ordered correctly next to Latin digits and separators.
 */
object InvoicePdfWriter {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 32f
    private val CONTENT_WIDTH = PAGE_WIDTH - 2 * MARGIN

    private const val COLOR_INK = 0xFF1F1B13.toInt()
    private const val COLOR_MUTED = 0xFF6B6355.toInt()
    private const val COLOR_GOLD = 0xFF8A6410.toInt()
    private const val COLOR_GOLD_SOFT = 0xFFFFF1D0.toInt()
    private const val COLOR_LINE = 0xFFD9CDB8.toInt()
    private const val COLOR_ROW_ALT = 0xFFFBF6EC.toInt()

    private data class Column(val title: String, val width: Float, val alignEnd: Boolean = false)

    private val columns = listOf(
        Column("ردیف", 24f),
        Column("شرح کالا", 112f),
        Column("عیار", 40f),
        Column("وزن (گرم)", 44f),
        Column("تعداد", 30f),
        Column("نرخ هر گرم", 62f),
        Column("اجرت", 58f),
        Column("سود", 52f),
        Column("مالیات", 52f),
        Column("مبلغ کل", 57f)
    )

    fun write(context: Context, document: InvoiceDocument, outputFile: File): File {
        val regular = ResourcesCompat.getFont(context, R.font.vazirmatn_regular) ?: Typeface.DEFAULT
        val bold = ResourcesCompat.getFont(context, R.font.vazirmatn_bold) ?: Typeface.DEFAULT_BOLD

        val pdf = PdfDocument()
        var pageNumber = 1
        var page = pdf.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
        var canvas = page.canvas

        var y = drawHeader(canvas, document, regular, bold)
        y = drawParties(canvas, document, y, regular, bold)
        y = drawTableHeader(canvas, y, bold)

        val bottomLimit = PAGE_HEIGHT - MARGIN - 150f
        document.items.forEachIndexed { index, item ->
            val totals = document.lineTotals.getOrNull(index)
            val rowHeight = 20f
            if (y + rowHeight > bottomLimit) {
                drawPageFooter(canvas, pageNumber, regular)
                pdf.finishPage(page)
                pageNumber += 1
                page = pdf.startPage(
                    PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                )
                canvas = page.canvas
                y = MARGIN
                y = drawTableHeader(canvas, y, bold)
            }
            val cells = listOf(
                PersianNumbers.toPersianDigits((index + 1).toString()),
                item.title.ifBlank { item.kind.label },
                if (item.weightGrams > 0.0) Karat.label(item.karat).substringBefore(" (") else "—",
                if (item.weightGrams > 0.0) PersianNumbers.formatWeight(item.weightGrams) else "—",
                PersianNumbers.toPersianDigits(item.quantity.toString()),
                if (item.unitPriceOverride != null) "—" else PersianNumbers.formatAmount(item.goldRatePerGram),
                PersianNumbers.formatAmount(totals?.wage ?: 0L),
                PersianNumbers.formatAmount(totals?.profit ?: 0L),
                PersianNumbers.formatAmount(totals?.tax ?: 0L),
                PersianNumbers.formatAmount(totals?.total ?: 0L)
            )
            y = drawRow(canvas, cells, y, rowHeight, index % 2 == 1, regular)
        }

        y += 12f
        y = drawTotals(canvas, document, y, regular, bold)
        drawSignatures(canvas, document, y, regular)
        drawPageFooter(canvas, pageNumber, regular)
        pdf.finishPage(page)

        outputFile.parentFile?.mkdirs()
        FileOutputStream(outputFile).use { stream -> pdf.writeTo(stream) }
        pdf.close()
        return outputFile
    }

    private fun textPaint(typeface: Typeface, size: Float, color: Int): TextPaint =
        TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            this.typeface = typeface
            textSize = size
            this.color = color
        }

    /** Lays text out inside the box that ends at [right] and returns the height it used. */
    private fun drawText(
        canvas: Canvas,
        text: String,
        right: Float,
        width: Float,
        top: Float,
        paint: TextPaint,
        alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL,
        maxLines: Int = 1
    ): Float {
        val safeWidth = width.toInt().coerceAtLeast(1)
        val layout = StaticLayout.Builder
            .obtain(text, 0, text.length, paint, safeWidth)
            .setAlignment(alignment)
            .setTextDirection(TextDirectionHeuristics.RTL)
            .setIncludePad(false)
            .setMaxLines(maxLines)
            .setEllipsize(TextUtils.TruncateAt.END)
            .build()
        canvas.save()
        canvas.translate(right - width, top)
        layout.draw(canvas)
        canvas.restore()
        return layout.height.toFloat()
    }

    private fun drawHeader(
        canvas: Canvas,
        document: InvoiceDocument,
        regular: Typeface,
        bold: Typeface
    ): Float {
        val right = PAGE_WIDTH - MARGIN
        val background = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = COLOR_GOLD_SOFT }
        canvas.drawRoundRect(RectF(MARGIN, MARGIN, right, MARGIN + 74f), 10f, 10f, background)

        val accent = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = COLOR_GOLD }
        canvas.drawRoundRect(RectF(MARGIN, MARGIN, MARGIN + 5f, MARGIN + 74f), 3f, 3f, accent)

        var y = MARGIN + 10f
        y += drawText(canvas, document.settings.shopName, right - 12f, 300f, y, textPaint(bold, 15f, COLOR_GOLD))
        val contact = buildList {
            if (document.settings.ownerName.isNotBlank()) add(document.settings.ownerName)
            if (document.settings.phone.isNotBlank()) {
                add("تلفن: ${PersianNumbers.toPersianDigits(document.settings.phone)}")
            }
        }.joinToString(" • ")
        if (contact.isNotBlank()) {
            y += drawText(canvas, contact, right - 12f, 300f, y + 2f, textPaint(regular, 9f, COLOR_MUTED)) + 2f
        }
        if (document.settings.address.isNotBlank()) {
            y += drawText(
                canvas,
                document.settings.address,
                right - 12f,
                300f,
                y + 2f,
                textPaint(regular, 9f, COLOR_MUTED)
            )
        }

        // Invoice identity sits on the opposite side of the header band.
        val identityRight = MARGIN + 200f
        var identityY = MARGIN + 12f
        identityY += drawText(
            canvas, document.title, identityRight, 190f, identityY,
            textPaint(bold, 12f, COLOR_INK), Layout.Alignment.ALIGN_OPPOSITE
        )
        identityY += drawText(
            canvas, "شماره: ${document.numberLabel}", identityRight, 190f, identityY + 3f,
            textPaint(regular, 9.5f, COLOR_INK), Layout.Alignment.ALIGN_OPPOSITE
        )
        drawText(
            canvas, "تاریخ: ${document.dateLabel}   ساعت: ${document.timeLabel}", identityRight, 190f,
            identityY + 3f, textPaint(regular, 9.5f, COLOR_INK), Layout.Alignment.ALIGN_OPPOSITE
        )
        return MARGIN + 74f + 14f
    }

    private fun drawParties(
        canvas: Canvas,
        document: InvoiceDocument,
        top: Float,
        regular: Typeface,
        bold: Typeface
    ): Float {
        val right = PAGE_WIDTH - MARGIN
        val boxHeight = 52f
        val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_LINE
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
        }
        canvas.drawRoundRect(RectF(MARGIN, top, right, top + boxHeight), 8f, 8f, border)

        val invoice = document.invoice
        var y = top + 8f
        y += drawText(
            canvas, "مشخصات خریدار", right - 10f, 120f, y, textPaint(bold, 9.5f, COLOR_GOLD)
        )
        val identity = buildList {
            add("نام: ${invoice.customerName.ifBlank { "مشتری متفرقه" }}")
            if (invoice.customerPhone.isNotBlank()) {
                add("تلفن: ${PersianNumbers.toPersianDigits(invoice.customerPhone)}")
            }
            if (invoice.customerNationalId.isNotBlank()) {
                add("کد ملی: ${PersianNumbers.toPersianDigits(invoice.customerNationalId)}")
            }
        }.joinToString("   •   ")
        y += drawText(canvas, identity, right - 10f, CONTENT_WIDTH - 20f, y + 2f, textPaint(regular, 9f, COLOR_INK))
        val extra = buildList {
            if (invoice.customerAddress.isNotBlank()) add("نشانی: ${invoice.customerAddress}")
            add("نرخ هر گرم طلای ۱۸ عیار: ${document.money(invoice.baseGoldRate)}")
            add("نحوه پرداخت: ${invoice.paymentMethod.label}")
        }.joinToString("   •   ")
        drawText(canvas, extra, right - 10f, CONTENT_WIDTH - 20f, y + 2f, textPaint(regular, 9f, COLOR_MUTED))
        return top + boxHeight + 12f
    }

    private fun drawTableHeader(canvas: Canvas, top: Float, bold: Typeface): Float {
        val right = PAGE_WIDTH - MARGIN
        val height = 22f
        val background = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = COLOR_GOLD }
        canvas.drawRoundRect(RectF(MARGIN, top, right, top + height), 5f, 5f, background)

        val paint = textPaint(bold, 8f, Color.WHITE)
        var cellRight = right
        for (column in columns) {
            drawText(
                canvas, column.title, cellRight - 3f, column.width - 6f, top + 6f, paint,
                Layout.Alignment.ALIGN_CENTER
            )
            cellRight -= column.width
        }
        return top + height
    }

    private fun drawRow(
        canvas: Canvas,
        cells: List<String>,
        top: Float,
        height: Float,
        alternate: Boolean,
        regular: Typeface
    ): Float {
        val right = PAGE_WIDTH - MARGIN
        if (alternate) {
            val background = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = COLOR_ROW_ALT }
            canvas.drawRect(RectF(MARGIN, top, right, top + height), background)
        }
        val separator = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_LINE
            strokeWidth = 0.5f
        }
        canvas.drawLine(MARGIN, top + height, right, top + height, separator)

        val paint = textPaint(regular, 7.5f, COLOR_INK)
        var cellRight = right
        for ((index, column) in columns.withIndex()) {
            val text = cells.getOrElse(index) { "" }
            drawText(
                canvas, text, cellRight - 3f, column.width - 6f, top + 5f, paint,
                if (index == 1) Layout.Alignment.ALIGN_NORMAL else Layout.Alignment.ALIGN_CENTER
            )
            cellRight -= column.width
        }
        return top + height
    }

    private fun drawTotals(
        canvas: Canvas,
        document: InvoiceDocument,
        top: Float,
        regular: Typeface,
        bold: Typeface
    ): Float {
        val right = PAGE_WIDTH - MARGIN
        val totals = document.totals
        val boxWidth = 250f
        val boxRight = MARGIN + boxWidth

        val summaryRows = listOf(
            "جمع ارزش طلا" to document.money(totals.goldValue),
            "جمع اجرت ساخت" to document.money(totals.wage),
            "جمع سود فروشنده" to document.money(totals.profit),
            "مالیات بر ارزش افزوده" to document.money(totals.tax)
        ).toMutableList()
        if (totals.stone > 0L) summaryRows.add(2, "قیمت نگین و سنگ" to document.money(totals.stone))
        if (totals.discount > 0L) summaryRows.add("تخفیف" to "− ${document.money(totals.discount)}")

        var y = top
        val labelPaint = textPaint(regular, 9f, COLOR_MUTED)
        val valuePaint = textPaint(regular, 9f, COLOR_INK)
        for ((label, value) in summaryRows) {
            drawText(canvas, label, boxRight, 120f, y, labelPaint)
            drawText(canvas, value, boxRight - 120f, boxWidth - 120f, y, valuePaint, Layout.Alignment.ALIGN_OPPOSITE)
            y += 14f
        }

        val payableTop = y + 4f
        val payableBackground = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = COLOR_GOLD_SOFT }
        canvas.drawRoundRect(RectF(MARGIN, payableTop, boxRight, payableTop + 24f), 6f, 6f, payableBackground)
        drawText(canvas, "مبلغ قابل پرداخت", boxRight - 8f, 110f, payableTop + 6f, textPaint(bold, 10f, COLOR_GOLD))
        drawText(
            canvas, document.money(totals.payable), boxRight - 118f, boxWidth - 126f, payableTop + 6f,
            textPaint(bold, 11f, COLOR_INK), Layout.Alignment.ALIGN_OPPOSITE
        )
        var leftY = payableTop + 30f
        drawText(canvas, "پرداخت‌شده: ${document.money(totals.paid)}", boxRight, boxWidth, leftY, valuePaint)
        leftY += 14f
        drawText(
            canvas,
            if (totals.remaining > 0L) "مانده بدهی: ${document.money(totals.remaining)}"
            else "تسویه کامل",
            boxRight,
            boxWidth,
            leftY,
            textPaint(bold, 9f, if (totals.remaining > 0L) 0xFFB3261E.toInt() else 0xFF2E7D4F.toInt())
        )

        // Weight recap and the amount spelled out live on the right hand column.
        var rightY = top
        val recap = listOf(
            "تعداد اقلام" to PersianNumbers.toPersianDigits(totals.pieceCount.toString()),
            "وزن کل" to "${PersianNumbers.formatWeight(totals.totalWeightGrams)} گرم",
            "جمع کل فاکتور" to document.money(totals.gross)
        )
        for ((label, value) in recap) {
            drawText(canvas, label, right, 110f, rightY, labelPaint)
            drawText(canvas, value, right - 110f, 150f, rightY, valuePaint, Layout.Alignment.ALIGN_OPPOSITE)
            rightY += 14f
        }
        rightY += 4f
        rightY += drawText(
            canvas, "مبلغ به حروف: ${document.amountInWords()}", right, 260f, rightY,
            textPaint(regular, 8.5f, COLOR_INK), maxLines = 3
        )

        return maxOf(leftY + 20f, rightY + 10f)
    }

    private fun drawSignatures(canvas: Canvas, document: InvoiceDocument, top: Float, regular: Typeface) {
        val right = PAGE_WIDTH - MARGIN
        val y = minOf(top, PAGE_HEIGHT - MARGIN - 60f)
        val paint = textPaint(regular, 9f, COLOR_MUTED)
        drawText(canvas, "مهر و امضای فروشنده", right, 200f, y, paint)
        drawText(canvas, "امضای خریدار", MARGIN + 200f, 200f, y, paint, Layout.Alignment.ALIGN_OPPOSITE)
        if (document.settings.invoiceFooter.isNotBlank()) {
            drawText(
                canvas, document.settings.invoiceFooter, right, CONTENT_WIDTH, y + 24f,
                textPaint(regular, 8f, COLOR_MUTED), Layout.Alignment.ALIGN_CENTER, maxLines = 2
            )
        }
    }

    private fun drawPageFooter(canvas: Canvas, pageNumber: Int, regular: Typeface) {
        val right = PAGE_WIDTH - MARGIN
        drawText(
            canvas,
            "صفحه ${PersianNumbers.toPersianDigits(pageNumber.toString())} — تولید شده با اپلیکیشن زرّین",
            right,
            CONTENT_WIDTH,
            PAGE_HEIGHT - MARGIN - 12f,
            textPaint(regular, 7.5f, COLOR_MUTED),
            Layout.Alignment.ALIGN_CENTER
        )
    }
}
