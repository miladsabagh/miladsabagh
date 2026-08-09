package com.goldgallery.app.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.goldgallery.app.data.db.InvoiceWithItems
import com.goldgallery.app.logic.JalaliDate
import com.goldgallery.app.logic.PersianFormat
import java.io.File
import java.io.FileOutputStream

object InvoicePdfGenerator {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 36f
    private const val BOTTOM_LIMIT = PAGE_HEIGHT - 70f

    private const val COLOR_GOLD = 0xFFC9A227.toInt()
    private const val COLOR_DARK = 0xFF2E2303.toInt()
    private const val COLOR_LINE = 0xFFD8C68E.toInt()
    private const val COLOR_ZEBRA = 0xFFFAF3DE.toInt()

    fun generate(context: Context, data: InvoiceWithItems): File {
        val font = Typeface.createFromAsset(context.assets, "fonts/vazirmatn.ttf")
        val bold = Typeface.create(font, Typeface.BOLD)

        val pdf = PdfDocument()
        var pageNumber = 1
        var page = pdf.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
        var canvas = page.canvas
        var y = drawHeader(canvas, font, bold, data)

        y = drawTableHeader(canvas, font, bold, y)

        data.items.forEachIndexed { index, item ->
            val rowHeight = 30f
            if (y + rowHeight > BOTTOM_LIMIT) {
                pdf.finishPage(page)
                pageNumber++
                page = pdf.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
                canvas = page.canvas
                y = drawTableHeader(canvas, font, bold, MARGIN + 10f)
            }
            drawItemRow(canvas, font, index, item, y, rowHeight, zebra = index % 2 == 1)
            y += rowHeight
        }

        y = drawTotals(canvas, font, bold, data, y + 14f)
        drawFooter(canvas, font, data, y + 24f)

        pdf.finishPage(page)

        val dir = File(context.cacheDir, "invoices").apply { mkdirs() }
        val file = File(dir, "invoice-${data.invoice.number}.pdf")
        FileOutputStream(file).use { pdf.writeTo(it) }
        pdf.close()
        return file
    }

    private fun textPaint(font: Typeface, size: Float, color: Int = COLOR_DARK): Paint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = font
            textSize = size
            this.color = color
            textAlign = Paint.Align.RIGHT
        }

    private fun fillPaint(color: Int): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        this.color = color
    }

    private fun strokePaint(color: Int, width: Float = 1f): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = width
        this.color = color
    }

    private fun rtl(canvas: Canvas, paint: Paint, text: String, x: Float, y: Float) {
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(text, x, y, paint)
    }

    private fun ltr(canvas: Canvas, paint: Paint, text: String, x: Float, y: Float) {
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText(text, x, y, paint)
    }

    private fun center(canvas: Canvas, paint: Paint, text: String, x: Float, y: Float) {
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(text, x, y, paint)
    }

    private fun drawHeader(canvas: Canvas, font: Typeface, bold: Typeface, data: InvoiceWithItems): Float {
        canvas.drawRect(RectF(0f, 0f, PAGE_WIDTH.toFloat(), 92f), fillPaint(COLOR_DARK))
        canvas.drawRect(RectF(0f, 92f, PAGE_WIDTH.toFloat(), 97f), fillPaint(COLOR_GOLD))

        rtl(canvas, textPaint(bold, 20f, Color.WHITE), "گالری طلا و جواهر پارسه", PAGE_WIDTH - MARGIN, 44f)
        rtl(canvas, textPaint(font, 11f, COLOR_GOLD), "فاکتور فروش طلا، جواهر و سکه", PAGE_WIDTH - MARGIN, 68f)
        ltr(canvas, textPaint(bold, 13f, COLOR_GOLD), PersianFormat.toPersianDigits("№ ${data.invoice.number}"), MARGIN, 44f)
        ltr(canvas, textPaint(font, 11f, Color.WHITE), JalaliDate.formatLong(data.invoice.createdAt), MARGIN, 66f)

        var y = 122f
        val label = textPaint(font, 10f, 0xFF8A7A4A.toInt())
        val value = textPaint(bold, 11.5f)

        rtl(canvas, label, "نام مشتری:", PAGE_WIDTH - MARGIN, y)
        rtl(canvas, value, data.invoice.customerName, PAGE_WIDTH - MARGIN - 68f, y)
        y += 20f
        rtl(canvas, label, "شماره تماس:", PAGE_WIDTH - MARGIN, y)
        rtl(canvas, value, PersianFormat.toPersianDigits(data.invoice.customerPhone.ifBlank { "-" }), PAGE_WIDTH - MARGIN - 68f, y)
        y += 20f
        rtl(canvas, label, "قیمت هر گرم طلای ۱۸ عیار:", PAGE_WIDTH - MARGIN, y)
        rtl(canvas, value, PersianFormat.money(data.invoice.goldPrice18PerGram), PAGE_WIDTH - MARGIN - 128f, y)
        y += 16f
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, strokePaint(COLOR_LINE))
        return y + 18f
    }

    private fun drawTableHeader(canvas: Canvas, font: Typeface, bold: Typeface, y: Float): Float {
        val rowHeight = 26f
        canvas.drawRect(RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + rowHeight), fillPaint(COLOR_GOLD))
        val paint = textPaint(bold, 10.5f, COLOR_DARK)
        val ty = y + 17f
        center(canvas, paint, "ردیف", colX(0), ty)
        rtl(canvas, paint, "شرح کالا", colX(1), ty)
        center(canvas, paint, "تعداد", colX(2), ty)
        center(canvas, paint, "وزن", colX(3), ty)
        center(canvas, paint, "عیار", colX(4), ty)
        center(canvas, paint, "اجرت", colX(5), ty)
        center(canvas, paint, "مبلغ (تومان)", colX(6), ty)
        return y + rowHeight
    }

    // ستون‌ها از راست به چپ چیده می‌شوند تا جدول کاملاً راست‌به‌چپ باشد
    private fun colX(col: Int): Float {
        val right = PAGE_WIDTH - MARGIN
        return when (col) {
            0 -> right - 18f            // ردیف (مرکز)
            1 -> right - 42f            // شرح (راست‌چین)
            2 -> right - 252f           // تعداد (مرکز)
            3 -> right - 292f           // وزن (مرکز)
            4 -> right - 344f           // عیار (مرکز)
            5 -> right - 400f           // اجرت (مرکز)
            else -> MARGIN + 78f        // مبلغ (مرکز)
        }
    }

    private fun drawItemRow(
        canvas: Canvas,
        font: Typeface,
        index: Int,
        item: com.goldgallery.app.data.db.InvoiceItemEntity,
        y: Float,
        rowHeight: Float,
        zebra: Boolean,
    ) {
        if (zebra) {
            canvas.drawRect(RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + rowHeight), fillPaint(COLOR_ZEBRA))
        }
        val paint = textPaint(font, 10f)
        val ty = y + 19f
        center(canvas, paint, PersianFormat.toPersianDigits((index + 1).toString()), colX(0), ty)
        rtl(canvas, paint, item.productName, colX(1), ty)
        center(canvas, paint, PersianFormat.toPersianDigits(item.quantity.toString()), colX(2), ty)
        center(canvas, paint, PersianFormat.weightRaw(item.weightGrams), colX(3), ty)
        center(canvas, paint, PersianFormat.toPersianDigits(item.karat.toString()), colX(4), ty)
        center(canvas, paint, PersianFormat.number(item.wage), colX(5), ty)
        center(canvas, paint, PersianFormat.number(item.total), colX(6), ty)
        canvas.drawLine(MARGIN, y + rowHeight, PAGE_WIDTH - MARGIN, y + rowHeight, strokePaint(COLOR_LINE))
    }

    private fun drawTotals(canvas: Canvas, font: Typeface, bold: Typeface, data: InvoiceWithItems, yStart: Float): Float {
        val boxRight = PAGE_WIDTH - MARGIN
        val boxLeft = PAGE_WIDTH - MARGIN - 250f
        var y = yStart

        fun row(label: String, amount: Long, emphasize: Boolean = false) {
            if (emphasize) {
                canvas.drawRoundRect(RectF(boxLeft - 8f, y - 13f, boxRight + 8f, y + 7f), 6f, 6f, fillPaint(COLOR_DARK))
                rtl(canvas, textPaint(bold, 12f, Color.WHITE), label, boxRight, y)
                ltr(canvas, textPaint(bold, 12f, Color.WHITE), PersianFormat.number(amount), boxLeft, y)
            } else {
                rtl(canvas, textPaint(font, 10.5f), label, boxRight, y)
                ltr(canvas, textPaint(font, 10.5f), PersianFormat.number(amount), boxLeft, y)
            }
            y += 22f
        }

        row("جمع طلای خام", data.items.sumOf { it.goldRaw })
        row("جمع اجرت ساخت", data.items.sumOf { it.wage })
        row("سود فروشنده (٪۷)", data.items.sumOf { it.profit })
        row("مالیات بر ارزش افزوده (٪۹)", data.items.sumOf { it.tax })
        y += 4f
        row("مبلغ قابل پرداخت", data.items.sumOf { it.total }, emphasize = true)
        return y
    }

    private fun drawFooter(canvas: Canvas, font: Typeface, data: InvoiceWithItems, y: Float) {
        val paint = textPaint(font, 9.5f, 0xFF8A7A4A.toInt())
        val fy = maxOf(y + 10f, PAGE_HEIGHT - 52f)
        canvas.drawLine(MARGIN, fy - 14f, PAGE_WIDTH - MARGIN, fy - 14f, strokePaint(COLOR_LINE))
        center(canvas, paint, "با سپاس از اعتماد شما • کلیه اقلام دارای فاکتور معتبر و ضمانت اصالت کالا هستند", PAGE_WIDTH / 2f, fy)
        center(canvas, paint, "تهران، بازار بزرگ، پاساژ طلای پارسه، پلاک ۱۲ • تلفن: ۰۲۱-۳۳۹۰۰۰۰۰", PAGE_WIDTH / 2f, fy + 16f)
    }
}
