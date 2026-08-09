package ir.goldshop.app.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import ir.goldshop.app.R
import ir.goldshop.app.data.entity.Invoice
import ir.goldshop.app.data.entity.InvoiceItem
import ir.goldshop.app.data.entity.ShopSettings
import ir.goldshop.app.util.PersianDate
import ir.goldshop.app.util.formatToman
import ir.goldshop.app.util.formatWeight
import ir.goldshop.app.util.toPersianDigits
import java.io.File
import java.io.FileOutputStream

/**
 * تولید فایل PDF فاکتور فروش طلا و جواهر، برای اشتراک‌گذاری، ذخیره یا چاپ.
 */
object InvoicePdfGenerator {

    private const val PAGE_WIDTH = 1240
    private const val PAGE_HEIGHT = 1754
    private const val MARGIN = 60f

    fun generate(context: Context, invoice: Invoice, items: List<InvoiceItem>, settings: ShopSettings): File {
        val regularTypeface = ResourcesCompat.getFont(context, R.font.vazirmatn_regular) ?: Typeface.DEFAULT
        val boldTypeface = ResourcesCompat.getFont(context, R.font.vazirmatn_bold) ?: Typeface.DEFAULT_BOLD

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        canvas.drawColor(Color.WHITE)

        val rightEdge = PAGE_WIDTH - MARGIN
        val leftEdge = MARGIN
        var y = MARGIN + 20f

        val titlePaint = textPaint(boldTypeface, 34f, Color.rgb(0x5B, 0x23, 0x33))
        canvas.drawTextRtl(settings.shopName, rightEdge, y, titlePaint)
        y += 46f

        val subPaint = textPaint(regularTypeface, 20f, Color.DKGRAY)
        if (settings.shopAddress.isNotBlank()) {
            y = canvas.drawWrappedRtl(settings.shopAddress, leftEdge, rightEdge, y, subPaint)
        }
        val contactLine = buildString {
            if (settings.shopPhone.isNotBlank()) append("تلفن: ${settings.shopPhone.toPersianDigits()}")
            if (settings.economicCode.isNotBlank()) {
                if (isNotEmpty()) append("   |   ")
                append("کد اقتصادی: ${settings.economicCode.toPersianDigits()}")
            }
        }
        if (contactLine.isNotBlank()) {
            canvas.drawTextRtl(contactLine, rightEdge, y, subPaint)
            y += 30f
        }

        y += 10f
        val dividerPaint = linePaint(Color.rgb(0xB8, 0x86, 0x0B))
        canvas.drawLine(leftEdge, y, rightEdge, y, dividerPaint)
        y += 40f

        val headerLabelPaint = textPaint(boldTypeface, 26f, Color.rgb(0x2B, 0x1D, 0x0E))
        canvas.drawTextRtl("فاکتور فروش طلا و جواهر", rightEdge, y, headerLabelPaint)
        y += 44f

        val metaPaint = textPaint(regularTypeface, 20f, Color.rgb(0x2B, 0x1D, 0x0E))
        val persianDate = PersianDate.fromTimestamp(invoice.issuedAt)
        canvas.drawTextRtl("شماره فاکتور: ${invoice.invoiceNumber.toPersianDigits()}", rightEdge, y, metaPaint)
        canvas.drawTextLtrAlignedLeft("تاریخ: ${persianDate.toDisplayString()}", leftEdge, y, metaPaint)
        y += 32f

        canvas.drawTextRtl("مشتری: ${invoice.customerName}", rightEdge, y, metaPaint)
        if (invoice.customerPhone.isNotBlank()) {
            canvas.drawTextLtrAlignedLeft("تماس: ${invoice.customerPhone.toPersianDigits()}", leftEdge, y, metaPaint)
        }
        y += 32f

        if (invoice.customerAddress.isNotBlank()) {
            y = canvas.drawWrappedRtl("آدرس: ${invoice.customerAddress}", leftEdge, rightEdge, y, metaPaint)
        }

        y += 20f

        // ---- Table ----
        val columns = listOf(
            Column("ردیف", 60f),
            Column("شرح کالا", 260f),
            Column("عیار", 90f),
            Column("وزن (گرم)", 130f),
            Column("تعداد", 80f),
            Column("نرخ گرم", 160f),
            Column("اجرت+سود+مالیات", 210f),
            Column("جمع (تومان)", 190f)
        )
        val tableWidth = columns.sumOf { it.width.toDouble() }.toFloat()
        val tableLeft = rightEdge - tableWidth

        val headerHeight = 56f
        val rowHeight = 50f
        val headerBgPaint = Paint().apply { color = Color.rgb(0xFF, 0xE9, 0xB0); style = Paint.Style.FILL }
        val borderPaint = linePaint(Color.rgb(0xD8, 0xC0, 0x8E))
        val cellTextPaint = textPaint(regularTypeface, 18f, Color.rgb(0x2B, 0x1D, 0x0E))
        val cellHeaderTextPaint = textPaint(boldTypeface, 18f, Color.rgb(0x2B, 0x1D, 0x0E))

        canvas.drawRect(tableLeft, y, rightEdge, y + headerHeight, headerBgPaint)
        drawTableRowBorders(canvas, columns, tableLeft, rightEdge, y, headerHeight, borderPaint)
        drawTableRowTexts(canvas, columns, rightEdge, y + headerHeight / 2 + 6f, columns.map { it.label }, cellHeaderTextPaint)
        y += headerHeight

        items.forEachIndexed { index, item ->
            val feesCombined = item.laborAmount + item.profitAmount + item.taxAmount
            val values = listOf(
                (index + 1).toString(),
                item.itemName,
                item.karat.toString(),
                item.weightGrams.formatWeight(),
                item.quantity.toString(),
                item.pricePerGram.formatToman(),
                feesCombined.formatToman(),
                item.lineTotal.formatToman()
            )
            drawTableRowBorders(canvas, columns, tableLeft, rightEdge, y, rowHeight, borderPaint)
            drawTableRowTexts(canvas, columns, rightEdge, y + rowHeight / 2 + 6f, values, cellTextPaint)
            y += rowHeight

            if (y > PAGE_HEIGHT - 260f) {
                // نسخه ساده: در صورت طولانی بودن اقلام، از رسم بیشتر صرف‌نظر می‌شود.
                return@forEachIndexed
            }
        }

        y += 40f

        // ---- Totals ----
        val totalsLabelPaint = textPaint(regularTypeface, 21f, Color.rgb(0x2B, 0x1D, 0x0E))
        val totalsBoldPaint = textPaint(boldTypeface, 24f, Color.rgb(0x5B, 0x23, 0x33))

        y = drawTotalRow(canvas, rightEdge, y, "مجموع وزن", "${invoice.totalWeightGrams.formatWeight()} گرم", totalsLabelPaint)
        y = drawTotalRow(canvas, rightEdge, y, "جمع اقلام", "${invoice.subtotalAmount.formatToman()} تومان", totalsLabelPaint)
        y = drawTotalRow(canvas, rightEdge, y, "تخفیف", "${invoice.discountAmount.formatToman()} تومان", totalsLabelPaint)
        canvas.drawLine(rightEdge - 460f, y, rightEdge, y, dividerPaint)
        y += 34f
        y = drawTotalRow(canvas, rightEdge, y, "مبلغ نهایی قابل پرداخت", "${invoice.totalAmount.formatToman()} تومان", totalsBoldPaint)
        y = drawTotalRow(canvas, rightEdge, y, "پرداخت‌شده", "${invoice.paidAmount.formatToman()} تومان", totalsLabelPaint)
        if (!invoice.isFullyPaid) {
            y = drawTotalRow(canvas, rightEdge, y, "باقیمانده", "${invoice.remainingAmount.formatToman()} تومان", totalsLabelPaint)
        }
        y = drawTotalRow(canvas, rightEdge, y, "روش پرداخت", invoice.paymentMethod, totalsLabelPaint)

        if (invoice.notes.isNotBlank()) {
            y += 20f
            y = canvas.drawWrappedRtl("توضیحات: ${invoice.notes}", leftEdge, rightEdge, y, totalsLabelPaint)
        }

        val footerPaint = textPaint(regularTypeface, 18f, Color.GRAY)
        canvas.drawTextRtl(settings.invoiceFooterNote, rightEdge, PAGE_HEIGHT - MARGIN, footerPaint)

        document.finishPage(page)

        val invoicesDir = File(context.cacheDir, "invoices").apply { mkdirs() }
        val file = File(invoicesDir, "invoice-${invoice.invoiceNumber}.pdf")
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        return file
    }

    fun getShareUri(context: Context, file: File) =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    private data class Column(val label: String, val width: Float)

    private fun textPaint(typeface: Typeface, size: Float, color: Int): TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        this.typeface = typeface
        this.textSize = size
        this.color = color
        this.textAlign = Paint.Align.RIGHT
    }

    private fun linePaint(color: Int): Paint = Paint().apply {
        this.color = color
        this.strokeWidth = 2f
        this.style = Paint.Style.STROKE
    }

    private fun Canvas.drawTextRtl(text: String, rightX: Float, y: Float, paint: TextPaint) {
        val p = TextPaint(paint).apply { textAlign = Paint.Align.RIGHT }
        this.drawText(text, rightX, y, p)
    }

    private fun Canvas.drawTextLtrAlignedLeft(text: String, leftX: Float, y: Float, paint: TextPaint) {
        val p = TextPaint(paint).apply { textAlign = Paint.Align.LEFT }
        this.drawText(text, leftX, y, p)
    }

    private fun Canvas.drawWrappedRtl(text: String, leftX: Float, rightX: Float, startY: Float, paint: TextPaint): Float {
        val width = (rightX - leftX).toInt().coerceAtLeast(50)
        val layout = StaticLayout.Builder
            .obtain(text, 0, text.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_OPPOSITE)
            .setTextDirection(android.text.TextDirectionHeuristics.RTL)
            .setLineSpacing(0f, 1.15f)
            .build()
        this.save()
        this.translate(leftX, startY - paint.textSize)
        layout.draw(this)
        this.restore()
        return startY + layout.height + 12f
    }

    private fun drawTableRowBorders(
        canvas: Canvas,
        columns: List<Column>,
        tableLeft: Float,
        tableRight: Float,
        top: Float,
        height: Float,
        paint: Paint
    ) {
        canvas.drawRect(RectF(tableLeft, top, tableRight, top + height), paint)
        var x = tableRight
        for (column in columns) {
            canvas.drawLine(x, top, x, top + height, paint)
            x -= column.width
        }
        canvas.drawLine(x, top, x, top + height, paint)
    }

    private fun drawTableRowTexts(
        canvas: Canvas,
        columns: List<Column>,
        tableRight: Float,
        baselineY: Float,
        values: List<String>,
        paint: TextPaint
    ) {
        var x = tableRight
        for ((index, column) in columns.withIndex()) {
            val cellRight = x - 10f
            val cellWidthAvailable = column.width - 20f
            val value = values.getOrNull(index).orEmpty()
            val ellipsized = TextUtils.ellipsize(value, paint, cellWidthAvailable, TextUtils.TruncateAt.END).toString()
            canvas.drawText(ellipsized, cellRight, baselineY, paint.apply { textAlign = Paint.Align.RIGHT })
            x -= column.width
        }
    }

    private fun drawTotalRow(canvas: Canvas, rightEdge: Float, y: Float, label: String, value: String, paint: TextPaint): Float {
        val labelWidth = 260f
        canvas.drawTextRtl(label, rightEdge, y, paint)
        canvas.drawText(value, rightEdge - labelWidth, y, TextPaint(paint).apply { textAlign = Paint.Align.RIGHT })
        return y + paint.textSize + 18f
    }
}
