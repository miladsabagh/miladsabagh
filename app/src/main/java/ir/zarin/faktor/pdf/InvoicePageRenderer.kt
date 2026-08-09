package ir.zarin.faktor.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
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
import kotlin.math.ceil

/**
 * رسم صفحه فاکتور فروش در قطع A4 با متن راست‌چین فارسی.
 *
 * این کلاس مستقل از خروجی PDF است و روی هر [Canvas] با ابعاد [PAGE_WIDTH] × [PAGE_HEIGHT]
 * قابل استفاده است؛ همین موضوع امکان تست تصویری چیدمان فاکتور را فراهم می‌کند.
 */
class InvoicePageRenderer(private val context: Context) {

    private val regular: Typeface =
        ResourcesCompat.getFont(context, R.font.vazirmatn_regular) ?: Typeface.SANS_SERIF
    private val bold: Typeface =
        ResourcesCompat.getFont(context, R.font.vazirmatn_bold) ?: Typeface.DEFAULT_BOLD

    /** قالب‌بندی مبالغ فاکتور بر اساس واحد پول و نوع ارقام. */
    private inner class Formatter(settings: AppSettings) {
        private val unit: CurrencyUnit = settings.currencyUnit
        private val persian: Boolean = settings.persianDigits

        val unitLabel: String = context.getString(
            if (unit == CurrencyUnit.RIAL) R.string.rial else R.string.toman,
        )

        fun money(amountRial: Long): String = Money.format(amountRial, unit, persian)

        fun moneyWithUnit(amountRial: Long): String = "${money(amountRial)} $unitLabel"

        fun digits(text: String): String = if (persian) PersianNumbers.toPersianDigits(text) else text

        /** شناسه‌هایی مانند شماره فاکتور و تلفن که باید چپ‌به‌راست بمانند. */
        fun code(text: String): String = PersianNumbers.isolateLtr(digits(text))

        fun weight(grams: Double): String = Money.formatWeight(grams, persian)

        fun percent(value: Double): String = Money.formatPercent(value, persian)

        fun words(amountRial: Long): String = Money.inWords(amountRial, unit, unitLabel)
    }

    fun pageCount(itemCount: Int): Int =
        ceil(itemCount.coerceAtLeast(1).toDouble() / ROWS_PER_PAGE).toInt().coerceAtLeast(1)

    /** یک صفحه از فاکتور را روی بوم داده‌شده رسم می‌کند. */
    fun render(canvas: Canvas, pageIndex: Int, data: InvoiceWithItems, settings: AppSettings) {
        val format = Formatter(settings)
        val pages = pageCount(data.items.size)
        val isLastPage = pageIndex == pages - 1

        var y = drawHeader(canvas, data, settings, format)
        y = drawTableHeader(canvas, y, format.unitLabel)

        val firstItem = pageIndex * ROWS_PER_PAGE
        val lastItem = minOf(firstItem + ROWS_PER_PAGE, data.items.size)
        for (index in firstItem until lastItem) {
            y = drawItemRow(canvas, y, index + 1, data.items[index], format)
        }

        if (isLastPage) {
            drawTotals(canvas, y + 16f, data, format)
        }
        drawFooter(canvas, data, settings, format, pageIndex + 1, pages)
    }

    private fun drawHeader(
        canvas: Canvas,
        data: InvoiceWithItems,
        settings: AppSettings,
        format: Formatter,
    ): Float {
        val invoice = data.invoice
        val shopBlockWidth = CONTENT_WIDTH * 0.55f
        val invoiceBlockWidth = CONTENT_WIDTH * 0.40f
        var rightY = MARGIN

        val shopName = settings.shopName.ifBlank { context.getString(R.string.app_name) }
        rightY += drawRight(canvas, shopName, rightY, shopBlockWidth, paint(16f, bold, GOLD_DARK))

        val contactParts = buildList {
            if (settings.shopPhone.isNotBlank()) {
                add("${context.getString(R.string.phone)}: ${format.code(settings.shopPhone)}")
            }
            if (settings.shopAddress.isNotBlank()) add(settings.shopAddress)
        }
        if (contactParts.isNotEmpty()) {
            rightY += drawRight(
                canvas,
                contactParts.joinToString(" — "),
                rightY + 3f,
                shopBlockWidth,
                paint(9f, regular, Color.DKGRAY),
            )
        }

        var leftY = MARGIN
        leftY += drawLeft(
            canvas,
            context.getString(R.string.invoice_detail),
            leftY,
            invoiceBlockWidth,
            paint(15f, bold),
        )
        leftY += drawLeft(
            canvas,
            "${context.getString(R.string.invoice_number)}: ${format.code(invoice.number)}",
            leftY + 3f,
            invoiceBlockWidth,
            paint(10f, regular),
        )
        val date = JalaliCalendar.fromEpochMillis(invoice.dateMillis)
        leftY += drawLeft(
            canvas,
            "${context.getString(R.string.invoice_date)}: ${format.digits(date.format())}" +
                " - ${format.digits(JalaliCalendar.formatTime(invoice.dateMillis))}",
            leftY + 3f,
            invoiceBlockWidth,
            paint(10f, regular),
        )

        var y = maxOf(rightY, leftY) + 12f
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint(GOLD, 1.4f))
        y += 10f

        val customerName = invoice.customerName.ifBlank { context.getString(R.string.walk_in_customer) }
        val customerLine = buildString {
            append("${context.getString(R.string.customer)}: $customerName")
            if (invoice.customerPhone.isNotBlank()) {
                append("   ${context.getString(R.string.phone)}: ${format.code(invoice.customerPhone)}")
            }
        }
        val customerHeight = drawRight(canvas, customerLine, y, CONTENT_WIDTH * 0.55f, paint(10.5f, regular))
        drawLeft(
            canvas,
            "${context.getString(R.string.gold_rate_title)}: ${format.moneyWithUnit(invoice.goldRatePerGramRial)}",
            y,
            CONTENT_WIDTH * 0.42f,
            paint(9.5f, regular, Color.DKGRAY),
        )
        return y + customerHeight + 12f
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

    private fun drawTotals(canvas: Canvas, top: Float, data: InvoiceWithItems, format: Formatter) {
        val invoice = data.invoice
        val rows = buildList {
            add(context.getString(R.string.gold_value) to invoice.goldTotalRial)
            if (invoice.wageTotalRial > 0) add(context.getString(R.string.wage) to invoice.wageTotalRial)
            if (invoice.profitTotalRial > 0) add(context.getString(R.string.profit) to invoice.profitTotalRial)
            if (invoice.stoneTotalRial > 0) add(context.getString(R.string.stone) to invoice.stoneTotalRial)
            if (invoice.vatTotalRial > 0) add(context.getString(R.string.vat) to invoice.vatTotalRial)
            val discount = invoice.itemsDiscountRial + invoice.invoiceDiscountRial
            if (discount > 0) add(context.getString(R.string.discount) to discount)
        }

        val boxWidth = CONTENT_WIDTH * 0.52f
        val boxLeft = MARGIN
        var y = top

        rows.forEach { (label, amount) ->
            drawBox(canvas, label, boxLeft, y, boxWidth, paint(10f, regular, Color.DKGRAY))
            drawBox(
                canvas,
                format.moneyWithUnit(amount),
                boxLeft,
                y,
                boxWidth,
                paint(10f, regular),
                Layout.Alignment.ALIGN_OPPOSITE,
            )
            y += 17f
        }

        y += 4f
        canvas.drawLine(boxLeft, y, boxLeft + boxWidth, y, linePaint(Color.LTGRAY, 1f))
        y += 8f

        drawBox(canvas, context.getString(R.string.payable), boxLeft, y, boxWidth, paint(12f, bold))
        y += drawBox(
            canvas,
            format.moneyWithUnit(invoice.grandTotalRial),
            boxLeft,
            y,
            boxWidth,
            paint(12f, bold, GOLD_DARK),
            Layout.Alignment.ALIGN_OPPOSITE,
        )
        y += 10f

        drawBox(canvas, context.getString(R.string.paid_amount), boxLeft, y, boxWidth, paint(9.5f, regular, Color.DKGRAY))
        y += drawBox(
            canvas,
            format.moneyWithUnit(invoice.paidAmountRial),
            boxLeft,
            y,
            boxWidth,
            paint(9.5f, regular, Color.DKGRAY),
            Layout.Alignment.ALIGN_OPPOSITE,
        )
        y += 3f
        if (invoice.remainingRial > 0) {
            drawBox(canvas, context.getString(R.string.remaining), boxLeft, y, boxWidth, paint(9.5f, bold, RED))
            y += drawBox(
                canvas,
                format.moneyWithUnit(invoice.remainingRial),
                boxLeft,
                y,
                boxWidth,
                paint(9.5f, bold, RED),
                Layout.Alignment.ALIGN_OPPOSITE,
            )
            y += 3f
        }

        y += 12f
        y += drawRight(
            canvas,
            context.getString(R.string.amount_in_words, format.words(invoice.grandTotalRial)),
            y,
            CONTENT_WIDTH,
            paint(10f, bold),
        )

        if (invoice.note.isNotBlank()) {
            y += 8f
            drawRight(
                canvas,
                "${context.getString(R.string.note)}: ${invoice.note}",
                y,
                CONTENT_WIDTH,
                paint(9.5f, regular, Color.DKGRAY),
            )
        }
    }

    private fun drawFooter(
        canvas: Canvas,
        data: InvoiceWithItems,
        settings: AppSettings,
        format: Formatter,
        pageNumber: Int,
        pageCount: Int,
    ) {
        val y = PAGE_HEIGHT - MARGIN - 46f
        val signatureWidth = CONTENT_WIDTH * 0.3f
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint(Color.LTGRAY, 1f))

        drawBox(
            canvas,
            context.getString(R.string.seller_signature),
            PAGE_WIDTH - MARGIN - signatureWidth,
            y + 10f,
            signatureWidth,
            paint(10f, regular, Color.DKGRAY),
            Layout.Alignment.ALIGN_CENTER,
        )
        drawBox(
            canvas,
            context.getString(R.string.buyer_signature),
            MARGIN,
            y + 10f,
            signatureWidth,
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
        drawBox(
            canvas,
            "${context.getString(R.string.payment_method)}: $method   |   " +
                "${context.getString(R.string.vat_percent)}: ${format.percent(settings.vatPercent)}   |   " +
                context.getString(
                    R.string.page_of,
                    format.digits(pageNumber.toString()),
                    format.digits(pageCount.toString()),
                ),
            MARGIN,
            y + 32f,
            CONTENT_WIDTH,
            paint(8.5f, regular, Color.GRAY),
            Layout.Alignment.ALIGN_CENTER,
        )
    }

    /** متن را در کادری که به لبه راست صفحه چسبیده رسم می‌کند. */
    private fun drawRight(
        canvas: Canvas,
        text: String,
        top: Float,
        width: Float,
        textPaint: TextPaint,
    ): Float = drawBox(canvas, text, PAGE_WIDTH - MARGIN - width, top, width, textPaint)

    /** متن را در کادری که به لبه چپ صفحه چسبیده رسم می‌کند. */
    private fun drawLeft(
        canvas: Canvas,
        text: String,
        top: Float,
        width: Float,
        textPaint: TextPaint,
    ): Float = drawBox(canvas, text, MARGIN, top, width, textPaint, Layout.Alignment.ALIGN_OPPOSITE)

    /** متن راست‌چین فارسی را رسم می‌کند و ارتفاع اشغال‌شده را برمی‌گرداند. */
    private fun drawBox(
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
        .setLineSpacing(3f, 1f)
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
        const val PAGE_WIDTH = 595
        const val PAGE_HEIGHT = 842

        private const val MARGIN = 32f
        private const val CONTENT_WIDTH = PAGE_WIDTH - MARGIN * 2
        private const val ROW_HEIGHT = 26f
        private const val ROWS_PER_PAGE = 10

        /** ردیف، شرح کالا، عیار، وزن، تعداد، اجرت، مبلغ */
        private val COLUMN_WEIGHTS = listOf(0.06f, 0.34f, 0.08f, 0.11f, 0.08f, 0.11f, 0.22f)

        private const val GOLD = 0xFFB8860B.toInt()
        private const val GOLD_DARK = 0xFF7A5B08.toInt()
        private const val HEADER_BG = 0xFFF5EBD9.toInt()
        private const val RED = 0xFFB3261E.toInt()
        private const val BORDER = 0xFFCCCCCC.toInt()
    }
}
