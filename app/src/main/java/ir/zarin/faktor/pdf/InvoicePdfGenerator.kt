package ir.zarin.faktor.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import ir.zarin.faktor.R
import ir.zarin.faktor.core.CurrencyUnit
import ir.zarin.faktor.core.JalaliCalendar
import ir.zarin.faktor.core.Money
import ir.zarin.faktor.core.PersianNumbers
import ir.zarin.faktor.data.model.InvoiceItem
import ir.zarin.faktor.data.model.InvoiceWithItems
import ir.zarin.faktor.data.model.PaymentMethod
import ir.zarin.faktor.data.model.PricingMode
import ir.zarin.faktor.data.settings.AppSettings
import java.io.File
import java.io.FileOutputStream

/**
 * ساخت فایل PDF فاکتور فروش در قطع A4 با متن راست‌چین فارسی.
 */
class InvoicePdfGenerator(private val context: Context) {

    private val regular: Typeface =
        ResourcesCompat.getFont(context, R.font.vazirmatn_regular) ?: Typeface.SANS_SERIF
    private val bold: Typeface =
        ResourcesCompat.getFont(context, R.font.vazirmatn_bold) ?: Typeface.DEFAULT_BOLD

    /** قالب‌بندی مبالغ فاکتور بر اساس واحد پول و نوع ارقام. */
    private inner class Formatter(settings: AppSettings) {
        val unit: CurrencyUnit = settings.currencyUnit
        val persian: Boolean = settings.persianDigits
        val unitLabel: String = context.getString(
            if (unit == CurrencyUnit.RIAL) R.string.rial else R.string.toman,
        )

        fun money(amountRial: Long): String = Money.format(amountRial, unit, persian)

        fun moneyWithUnit(amountRial: Long): String = "${money(amountRial)} $unitLabel"

        fun digits(text: String): String = if (persian) PersianNumbers.toPersianDigits(text) else text

        fun weight(grams: Double): String = Money.formatWeight(grams, persian)

        fun percent(value: Double): String = Money.formatPercent(value, persian)

        fun words(amountRial: Long): String = Money.inWords(amountRial, unit, unitLabel)
    }

    fun generate(data: InvoiceWithItems, settings: AppSettings, outputDirectory: File): File {
        val format = Formatter(settings)
        val document = PdfDocument()

        var pageNumber = 1
        var page = document.startPage(pageInfo(pageNumber))
        var canvas = page.canvas
        var cursorY = drawHeader(canvas, data, settings, format)
        cursorY = drawTableHeader(canvas, cursorY, format.unitLabel)

        data.items.forEachIndexed { index, item ->
            if (cursorY > PAGE_HEIGHT - MARGIN - ROW_HEIGHT * 6) {
                document.finishPage(page)
                pageNumber += 1
                page = document.startPage(pageInfo(pageNumber))
                canvas = page.canvas
                cursorY = drawHeader(canvas, data, settings, format)
                cursorY = drawTableHeader(canvas, cursorY, format.unitLabel)
            }
            cursorY = drawItemRow(canvas, cursorY, index + 1, item, format)
        }

        drawTotals(canvas, cursorY + 14f, data, format)
        drawFooter(canvas, data, settings, format)
        document.finishPage(page)

        if (!outputDirectory.exists()) outputDirectory.mkdirs()
        val file = File(outputDirectory, "faktor-${data.invoice.number}.pdf")
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        return file
    }

    private fun pageInfo(pageNumber: Int) =
        PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()

    private fun drawHeader(
        canvas: Canvas,
        data: InvoiceWithItems,
        settings: AppSettings,
        format: Formatter,
    ): Float {
        val invoice = data.invoice
        var rightY = MARGIN

        val shopName = settings.shopName.ifBlank { context.getString(R.string.app_name) }
        rightY += drawRtl(canvas, shopName, MARGIN, rightY, CONTENT_WIDTH * 0.55f, paint(16f, bold, GOLD_DARK))

        val contactParts = buildList {
            if (settings.shopPhone.isNotBlank()) {
                add("${context.getString(R.string.phone)}: ${format.digits(settings.shopPhone)}")
            }
            if (settings.shopAddress.isNotBlank()) add(settings.shopAddress)
        }
        if (contactParts.isNotEmpty()) {
            rightY += drawRtl(
                canvas,
                contactParts.joinToString(" — "),
                MARGIN,
                rightY + 2f,
                CONTENT_WIDTH * 0.55f,
                paint(9f, regular, Color.DKGRAY),
            )
        }

        var leftY = MARGIN
        val leftWidth = CONTENT_WIDTH * 0.4f
        leftY += drawRtl(
            canvas,
            context.getString(R.string.invoice_detail),
            MARGIN,
            leftY,
            leftWidth,
            paint(15f, bold),
            Layout.Alignment.ALIGN_OPPOSITE,
        )
        leftY += drawRtl(
            canvas,
            "${context.getString(R.string.invoice_number)}: ${format.digits(invoice.number)}",
            MARGIN,
            leftY + 2f,
            leftWidth,
            paint(10f, regular),
            Layout.Alignment.ALIGN_OPPOSITE,
        )
        val date = JalaliCalendar.fromEpochMillis(invoice.dateMillis)
        leftY += drawRtl(
            canvas,
            "${context.getString(R.string.invoice_date)}: ${format.digits(date.format())}" +
                " - ${format.digits(JalaliCalendar.formatTime(invoice.dateMillis))}",
            MARGIN,
            leftY + 2f,
            leftWidth,
            paint(10f, regular),
            Layout.Alignment.ALIGN_OPPOSITE,
        )

        var y = maxOf(rightY, leftY) + 10f
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint(GOLD, 1.4f))
        y += 10f

        val customerName = invoice.customerName.ifBlank { context.getString(R.string.walk_in_customer) }
        val customerLine = buildString {
            append("${context.getString(R.string.customer)}: $customerName")
            if (invoice.customerPhone.isNotBlank()) {
                append("   ${context.getString(R.string.phone)}: ${format.digits(invoice.customerPhone)}")
            }
        }
        val customerHeight = drawRtl(canvas, customerLine, MARGIN, y, CONTENT_WIDTH * 0.6f, paint(10.5f, regular))
        drawRtl(
            canvas,
            "${context.getString(R.string.gold_rate_title)}: ${format.moneyWithUnit(invoice.goldRatePerGramRial)}",
            MARGIN,
            y,
            CONTENT_WIDTH * 0.4f,
            paint(9.5f, regular, Color.DKGRAY),
            Layout.Alignment.ALIGN_OPPOSITE,
        )
        return y + customerHeight + 10f
    }

    private fun drawTableHeader(canvas: Canvas, top: Float, unitLabel: String): Float {
        val headers = listOf(
            context.getString(R.string.row),
            context.getString(R.string.description),
            context.getString(R.string.karat),
            context.getString(R.string.weight_gram),
            context.getString(R.string.quantity),
            "${context.getString(R.string.wage)} (٪)",
            "${context.getString(R.string.line_total)} ($unitLabel)",
        )
        canvas.drawRect(MARGIN, top, PAGE_WIDTH - MARGIN, top + ROW_HEIGHT, fillPaint(HEADER_BG))
        drawRowCells(canvas, top, headers, paint(9f, bold))
        canvas.drawRect(MARGIN, top, PAGE_WIDTH - MARGIN, top + ROW_HEIGHT, strokePaint())
        return top + ROW_HEIGHT
    }

    private fun drawItemRow(
        canvas: Canvas,
        top: Float,
        index: Int,
        item: InvoiceItem,
        format: Formatter,
    ): Float {
        val byWeight = item.pricingMode == PricingMode.BY_WEIGHT
        val cells = listOf(
            format.digits(index.toString()),
            item.name,
            if (byWeight) format.digits(item.karat.toString()) else "—",
            if (byWeight) format.weight(item.weightGrams) else "—",
            format.digits(item.quantity.toString()),
            if (byWeight) format.percent(item.wagePercent) else "—",
            format.money(item.lineTotalRial),
        )
        canvas.drawRect(MARGIN, top, PAGE_WIDTH - MARGIN, top + ROW_HEIGHT, strokePaint())
        drawRowCells(canvas, top, cells, paint(9.5f, regular))
        return top + ROW_HEIGHT
    }

    private fun drawRowCells(canvas: Canvas, top: Float, cells: List<String>, textPaint: TextPaint) {
        var right = PAGE_WIDTH - MARGIN
        cells.forEachIndexed { index, text ->
            val width = CONTENT_WIDTH * COLUMN_WEIGHTS[index]
            val left = right - width
            val layout = buildLayout(text, textPaint, (width - 8f).toInt(), Layout.Alignment.ALIGN_CENTER)
            canvas.save()
            canvas.translate(left + 4f, top + (ROW_HEIGHT - layout.height) / 2f)
            layout.draw(canvas)
            canvas.restore()
            if (index < cells.lastIndex) {
                canvas.drawLine(left, top, left, top + ROW_HEIGHT, strokePaint())
            }
            right = left
        }
    }

    private fun drawTotals(canvas: Canvas, top: Float, data: InvoiceWithItems, format: Formatter): Float {
        val invoice = data.invoice
        val rows = buildList {
            add(context.getString(R.string.gold_value) to invoice.goldTotalRial)
            if (invoice.wageTotalRial > 0) add(context.getString(R.string.wage) to invoice.wageTotalRial)
            if (invoice.profitTotalRial > 0) add(context.getString(R.string.profit) to invoice.profitTotalRial)
            if (invoice.stoneTotalRial > 0) add(context.getString(R.string.stone) to invoice.stoneTotalRial)
            if (invoice.vatTotalRial > 0) add(context.getString(R.string.vat) to invoice.vatTotalRial)
            val discount = invoice.itemsDiscountRial + invoice.invoiceDiscountRial
            if (discount > 0) add(context.getString(R.string.discount) to -discount)
        }

        val boxWidth = CONTENT_WIDTH * 0.46f
        var y = top

        rows.forEach { (label, amount) ->
            drawRtl(canvas, label, MARGIN, y, boxWidth * 0.5f, paint(10f, regular, Color.DKGRAY))
            drawRtl(
                canvas,
                format.moneyWithUnit(amount),
                MARGIN + boxWidth * 0.5f,
                y,
                boxWidth * 0.5f,
                paint(10f, regular),
                Layout.Alignment.ALIGN_OPPOSITE,
            )
            y += 16f
        }

        y += 4f
        canvas.drawLine(MARGIN, y, MARGIN + boxWidth, y, linePaint(Color.LTGRAY, 1f))
        y += 8f

        drawRtl(canvas, context.getString(R.string.payable), MARGIN, y, boxWidth * 0.5f, paint(12f, bold))
        y += drawRtl(
            canvas,
            format.moneyWithUnit(invoice.grandTotalRial),
            MARGIN + boxWidth * 0.5f,
            y,
            boxWidth * 0.5f,
            paint(12f, bold, GOLD_DARK),
            Layout.Alignment.ALIGN_OPPOSITE,
        )
        y += 8f

        drawRtl(
            canvas,
            "${context.getString(R.string.paid_amount)}: ${format.moneyWithUnit(invoice.paidAmountRial)}",
            MARGIN,
            y,
            boxWidth,
            paint(9.5f, regular, Color.DKGRAY),
        )
        y += 15f
        if (invoice.remainingRial > 0) {
            drawRtl(
                canvas,
                "${context.getString(R.string.remaining)}: ${format.moneyWithUnit(invoice.remainingRial)}",
                MARGIN,
                y,
                boxWidth,
                paint(9.5f, regular, RED),
            )
            y += 15f
        }

        y += 8f
        y += drawRtl(
            canvas,
            context.getString(R.string.amount_in_words, format.words(invoice.grandTotalRial)),
            MARGIN,
            y,
            CONTENT_WIDTH,
            paint(10f, bold),
        )

        if (invoice.note.isNotBlank()) {
            y += 6f
            y += drawRtl(
                canvas,
                "${context.getString(R.string.note)}: ${invoice.note}",
                MARGIN,
                y,
                CONTENT_WIDTH,
                paint(9.5f, regular, Color.DKGRAY),
            )
        }
        return y
    }

    private fun drawFooter(
        canvas: Canvas,
        data: InvoiceWithItems,
        settings: AppSettings,
        format: Formatter,
    ) {
        val y = PAGE_HEIGHT - MARGIN - 44f
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint(Color.LTGRAY, 1f))

        drawRtl(
            canvas,
            "امضای فروشنده",
            PAGE_WIDTH - MARGIN - CONTENT_WIDTH * 0.3f,
            y + 10f,
            CONTENT_WIDTH * 0.3f,
            paint(10f, regular, Color.DKGRAY),
            Layout.Alignment.ALIGN_CENTER,
        )
        drawRtl(
            canvas,
            "امضای خریدار",
            MARGIN,
            y + 10f,
            CONTENT_WIDTH * 0.3f,
            paint(10f, regular, Color.DKGRAY),
            Layout.Alignment.ALIGN_CENTER,
        )

        val method = context.getString(
            when (data.invoice.paymentMethod) {
                PaymentMethod.CASH -> R.string.payment_cash
                PaymentMethod.CARD -> R.string.payment_card
                PaymentMethod.TRANSFER -> R.string.payment_transfer
                PaymentMethod.CREDIT -> R.string.payment_credit
            },
        )
        drawRtl(
            canvas,
            "${context.getString(R.string.payment_method)}: $method   |   " +
                "${context.getString(R.string.vat_percent)}: ${format.percent(settings.vatPercent)}",
            MARGIN,
            y + 30f,
            CONTENT_WIDTH,
            paint(8.5f, regular, Color.GRAY),
            Layout.Alignment.ALIGN_CENTER,
        )
    }

    /** متن راست‌چین فارسی را رسم می‌کند و ارتفاع اشغال‌شده را برمی‌گرداند. */
    private fun drawRtl(
        canvas: Canvas,
        text: String,
        left: Float,
        top: Float,
        width: Float,
        textPaint: TextPaint,
        alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL,
    ): Float {
        val layout = buildLayout(text, textPaint, width.toInt(), alignment)
        canvas.save()
        canvas.translate(left, top)
        layout.draw(canvas)
        canvas.restore()
        return layout.height.toFloat()
    }

    private fun buildLayout(
        text: String,
        textPaint: TextPaint,
        width: Int,
        alignment: Layout.Alignment,
    ): StaticLayout = StaticLayout.Builder
        .obtain(text, 0, text.length, textPaint, width.coerceAtLeast(1))
        .setAlignment(alignment)
        .setTextDirection(TextDirectionHeuristics.RTL)
        .setIncludePad(false)
        .setLineSpacing(2f, 1f)
        .build()

    private fun paint(size: Float, typeface: Typeface, color: Int = Color.BLACK): TextPaint =
        TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            this.textSize = size
            this.typeface = typeface
            this.color = color
        }

    private fun linePaint(color: Int, strokeWidth: Float) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        this.strokeWidth = strokeWidth
        style = Paint.Style.STROKE
    }

    private fun strokePaint() = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = BORDER
        strokeWidth = 0.7f
        style = Paint.Style.STROKE
    }

    private fun fillPaint(color: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = Paint.Style.FILL
    }

    companion object {
        private const val PAGE_WIDTH = 595
        private const val PAGE_HEIGHT = 842
        private const val MARGIN = 32f
        private const val CONTENT_WIDTH = PAGE_WIDTH - MARGIN * 2
        private const val ROW_HEIGHT = 26f

        /** ردیف، شرح کالا، عیار، وزن، تعداد، اجرت، مبلغ */
        private val COLUMN_WEIGHTS = listOf(0.06f, 0.34f, 0.08f, 0.11f, 0.08f, 0.11f, 0.22f)

        private const val GOLD = 0xFFB8860B.toInt()
        private const val GOLD_DARK = 0xFF7A5B08.toInt()
        private const val HEADER_BG = 0xFFF5EBD9.toInt()
        private const val RED = 0xFFB3261E.toInt()
        private const val BORDER = 0xFFCCCCCC.toInt()
    }
}
