package com.zarin.goldshop.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.zarin.goldshop.data.AppSettings
import com.zarin.goldshop.data.InvoiceWithItems
import java.io.File
import java.io.FileOutputStream

/** Renders an invoice to an A4 PDF with proper right-to-left Persian text. */
object InvoicePdf {

    private const val PAGE_W = 595
    private const val PAGE_H = 842
    private const val MARGIN = 36f

    fun generate(context: Context, data: InvoiceWithItems, settings: AppSettings): File {
        val doc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, 1).create()
        val page = doc.startPage(pageInfo)
        val canvas = page.canvas

        val title = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#8A6D00")
            textSize = 22f
            isFakeBoldText = true
        }
        val normal = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#111111")
            textSize = 12f
        }
        val muted = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#555555")
            textSize = 11f
        }
        val bold = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#111111")
            textSize = 12f
            isFakeBoldText = true
        }
        val line = Paint().apply { color = Color.parseColor("#DDDDDD"); strokeWidth = 1f }
        val headerBg = Paint().apply { color = Color.parseColor("#F5EAC6") }

        val right = PAGE_W - MARGIN
        var y = MARGIN + 20f

        drawRtl(canvas, settings.shopName, title, right, y)
        y += 22f
        if (settings.shopPhone.isNotBlank()) {
            drawRtl(canvas, "تلفن: " + PersianUtils.toPersianDigits(settings.shopPhone), muted, right, y); y += 15f
        }
        if (settings.shopAddress.isNotBlank()) {
            drawRtl(canvas, "آدرس: " + settings.shopAddress, muted, right, y); y += 15f
        }

        y += 8f
        canvas.drawLine(MARGIN, y, right, y, line)
        y += 24f

        drawRtl(canvas, "فاکتور فروش", bold, right, y)
        drawLtr(canvas, "شماره: " + PersianUtils.toPersianDigits(data.invoice.invoiceNumber), normal, MARGIN, y)
        y += 18f
        drawRtl(canvas, "تاریخ: " + PersianUtils.formatDateLong(data.invoice.dateMillis), normal, right, y)
        drawLtr(canvas, "نرخ روز طلا: " + PersianUtils.formatToman(data.invoice.goldPricePerGram), muted, MARGIN, y)
        y += 18f
        drawRtl(canvas, "خریدار: " + data.invoice.customerName, normal, right, y)
        if (data.invoice.customerPhone.isNotBlank()) {
            drawLtr(canvas, "تلفن: " + PersianUtils.toPersianDigits(data.invoice.customerPhone), muted, MARGIN, y)
        }
        y += 22f

        // Table header
        val rowH = 22f
        canvas.drawRect(MARGIN, y, right, y + rowH, headerBg)
        val cols = floatArrayOf(right - 4f, right - 190f, right - 250f, right - 315f, right - 400f, MARGIN + 4f)
        // columns (right to left): شرح | وزن | عیار | اجرت | تعداد | مبلغ کل
        val headerY = y + 15f
        drawRtl(canvas, "شرح کالا", bold, cols[0], headerY)
        drawRtl(canvas, "وزن (گرم)", bold, cols[1], headerY)
        drawRtl(canvas, "عیار", bold, cols[2], headerY)
        drawRtl(canvas, "اجرت", bold, cols[3], headerY)
        drawRtl(canvas, "تعداد", bold, cols[4], headerY)
        drawLtr(canvas, "مبلغ (تومان)", bold, cols[5], headerY)
        y += rowH

        data.items.forEach { item ->
            val ry = y + 15f
            drawRtl(canvas, item.name, normal, cols[0], ry)
            drawRtl(canvas, PersianUtils.formatWeight(item.weight), normal, cols[1], ry)
            drawRtl(canvas, PersianUtils.toPersianDigits(item.karat.toString()), normal, cols[2], ry)
            drawRtl(canvas, PersianUtils.toPersianDigits(item.wagePercent.toInt().toString()) + "٪", normal, cols[3], ry)
            drawRtl(canvas, PersianUtils.toPersianDigits(item.quantity.toString()), normal, cols[4], ry)
            drawLtr(canvas, PersianUtils.formatAmount(item.lineTotal), normal, cols[5], ry)
            y += rowH
            canvas.drawLine(MARGIN, y, right, y, line)
        }

        y += 20f
        val totalsRight = right
        fun totalRow(label: String, value: Long, strong: Boolean = false) {
            val p = if (strong) bold else normal
            drawRtl(canvas, label, p, totalsRight, y)
            drawLtr(canvas, PersianUtils.formatToman(value), p, MARGIN + 260f, y)
            y += 18f
        }
        totalRow("جمع ارزش طلا:", data.invoice.goldValueTotal)
        totalRow("جمع اجرت:", data.invoice.wageTotal)
        if (data.invoice.stoneTotal > 0) totalRow("جمع نگین/سنگ:", data.invoice.stoneTotal)
        totalRow("سود فروشنده:", data.invoice.profitTotal)
        totalRow("مالیات بر ارزش افزوده:", data.invoice.taxTotal)
        if (data.invoice.discount > 0) totalRow("تخفیف:", -data.invoice.discount)
        y += 4f
        canvas.drawLine(MARGIN + 240f, y, right, y, line)
        y += 20f
        totalRow("مبلغ قابل پرداخت:", data.invoice.grandTotal, strong = true)

        y += 30f
        drawRtl(canvas, "با تشکر از خرید شما", muted, right, y)

        doc.finishPage(page)

        val dir = File(context.cacheDir, "invoices").apply { mkdirs() }
        val file = File(dir, "invoice_${data.invoice.invoiceNumber}.pdf")
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
        return file
    }

    fun uriFor(context: Context, file: File) =
        FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)

    private fun drawRtl(canvas: Canvas, text: String, paint: TextPaint, rightX: Float, baselineY: Float) {
        val width = PAGE_W
        val layout = StaticLayout.Builder
            .obtain(text, 0, text.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setIncludePad(false)
            .build()
        val textWidth = paint.measureText(text)
        canvas.save()
        canvas.translate(rightX - textWidth, baselineY - paint.textSize)
        layout.draw(canvas)
        canvas.restore()
    }

    private fun drawLtr(canvas: Canvas, text: String, paint: TextPaint, leftX: Float, baselineY: Float) {
        val layout = StaticLayout.Builder
            .obtain(text, 0, text.length, paint, PAGE_W)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setIncludePad(false)
            .build()
        canvas.save()
        canvas.translate(leftX, baselineY - paint.textSize)
        layout.draw(canvas)
        canvas.restore()
    }
}
