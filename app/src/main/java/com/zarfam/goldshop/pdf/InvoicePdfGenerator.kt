package com.zarfam.goldshop.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.res.ResourcesCompat
import com.zarfam.goldshop.R
import com.zarfam.goldshop.data.db.InvoiceWithItems
import com.zarfam.goldshop.data.db.ShopSettings
import com.zarfam.goldshop.domain.JalaliDate
import com.zarfam.goldshop.domain.formatWeight
import com.zarfam.goldshop.domain.toMoney
import com.zarfam.goldshop.domain.toPersianDigits
import java.io.File

/**
 * Renders a sales invoice as an A4 PDF using the platform PdfDocument API.
 * All text is drawn right-to-left with the Vazirmatn font.
 */
class InvoicePdfGenerator(private val context: Context) {

    private companion object {
        const val PAGE_W = 595
        const val PAGE_H = 842
        const val MARGIN = 36f
        const val GOLD = 0xFF8D6E1F.toInt()
        const val GOLD_LIGHT = 0xFFF7EBCE.toInt()
        const val INK = 0xFF201B10.toInt()
        const val GRAY = 0xFF6F6753.toInt()
        const val ROW_H = 26f
    }

    private val regular: Typeface =
        ResourcesCompat.getFont(context, R.font.vazirmatn_regular) ?: Typeface.DEFAULT
    private val bold: Typeface =
        ResourcesCompat.getFont(context, R.font.vazirmatn_bold) ?: Typeface.DEFAULT_BOLD

    private fun paint(size: Float, tf: Typeface = regular, color: Int = INK, align: Paint.Align = Paint.Align.RIGHT) =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = size
            typeface = tf
            this.color = color
            textAlign = align
        }

    fun generate(data: InvoiceWithItems, settings: ShopSettings): File {
        val doc = PdfDocument()
        val invoice = data.invoice

        var pageNumber = 1
        var page = doc.startPage(PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, pageNumber).create())
        var canvas = page.canvas
        val right = PAGE_W - MARGIN
        val left = MARGIN

        // ---------- Header ----------
        val headerPaint = Paint().apply { color = GOLD }
        canvas.drawRect(0f, 0f, PAGE_W.toFloat(), 92f, headerPaint)
        canvas.drawText(settings.shopName, PAGE_W / 2f, 42f, paint(20f, bold, Color.WHITE, Paint.Align.CENTER))
        val contact = listOf(settings.shopPhone, settings.shopAddress).filter { it.isNotBlank() }
            .joinToString("  |  ").toPersianDigits()
        if (contact.isNotBlank()) {
            canvas.drawText(contact, PAGE_W / 2f, 68f, paint(11f, regular, 0xFFF4E9CD.toInt(), Paint.Align.CENTER))
        }

        var y = 126f
        canvas.drawText("فاکتور فروش طلا و جواهر", right, y, paint(16f, bold, GOLD))
        canvas.drawText("شماره فاکتور: ${invoice.invoiceNumber.toString().toPersianDigits()}", left + 150f, y - 8f, paint(11f, regular, GRAY, Paint.Align.LEFT))
        canvas.drawText("تاریخ: ${JalaliDate.format(invoice.dateMillis)}", left + 150f, y + 10f, paint(11f, regular, GRAY, Paint.Align.LEFT))

        // ---------- Customer box ----------
        y += 26f
        val boxPaint = Paint().apply { color = GOLD_LIGHT }
        canvas.drawRoundRect(left, y, right, y + 46f, 8f, 8f, boxPaint)
        val customerLine = buildString {
            append("خریدار: ")
            append(invoice.customerName.ifBlank { "مشتری" })
            if (invoice.customerPhone.isNotBlank()) append("    تلفن: ${invoice.customerPhone.toPersianDigits()}")
        }
        canvas.drawText(customerLine, right - 12f, y + 20f, paint(11f, regular))
        canvas.drawText(
            "قیمت هر گرم طلای ۱۸ عیار: ${invoice.goldPricePerGram18.toMoney()} تومان",
            right - 12f, y + 38f, paint(10f, regular, GRAY),
        )
        y += 66f

        // ---------- Items table ----------
        // Column anchors, laid out right-to-left.
        val colRow = right - 24f          // ردیف (center)
        val colName = right - 60f         // شرح (right-aligned, grows left)
        val colKarat = left + 292f        // عیار (center)
        val colWeight = left + 237f       // وزن (center)
        val colQty = left + 177f          // تعداد (center)
        val colTotal = left + 85f         // مبلغ (center)

        fun drawTableHeader() {
            canvas.drawRoundRect(left, y, right, y + ROW_H, 6f, 6f, Paint().apply { color = GOLD })
            val hp = paint(10f, bold, Color.WHITE, Paint.Align.CENTER)
            val yText = y + ROW_H / 2 + 4f
            canvas.drawText("ردیف", colRow, yText, hp)
            canvas.drawText("شرح کالا", colName - 60f, yText, hp)
            canvas.drawText("عیار", colKarat, yText, hp)
            canvas.drawText("وزن (گرم)", colWeight, yText, hp)
            canvas.drawText("تعداد", colQty, yText, hp)
            canvas.drawText("مبلغ (تومان)", colTotal, yText, hp)
            y += ROW_H + 4f
        }

        fun newPageIfNeeded() {
            if (y > PAGE_H - 210f) {
                doc.finishPage(page)
                pageNumber++
                page = doc.startPage(PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, pageNumber).create())
                canvas = page.canvas
                y = MARGIN + 10f
                drawTableHeader()
            }
        }

        drawTableHeader()

        val rowPaint = paint(10f)
        val rowCenter = paint(10f, regular, INK, Paint.Align.CENTER)
        data.items.forEachIndexed { index, item ->
            newPageIfNeeded()
            if (index % 2 == 1) {
                canvas.drawRect(left, y - 4f, right, y + ROW_H - 4f, Paint().apply { color = 0xFFFBF5E6.toInt() })
            }
            val yText = y + ROW_H / 2
            canvas.drawText((index + 1).toString().toPersianDigits(), colRow, yText, rowCenter)
            val name = if (item.name.length > 24) item.name.take(23) + "…" else item.name
            canvas.drawText(name, colName, yText, rowPaint)
            canvas.drawText(item.karat.toString().toPersianDigits(), colKarat, yText, rowCenter)
            canvas.drawText(item.weightGrams.formatWeight(), colWeight, yText, rowCenter)
            canvas.drawText(item.quantity.toString().toPersianDigits(), colQty, yText, rowCenter)
            canvas.drawText(item.lineTotal.toMoney(), colTotal, yText, rowCenter)
            y += ROW_H
        }

        // ---------- Totals ----------
        y += 14f
        if (y > PAGE_H - 190f) {
            doc.finishPage(page)
            pageNumber++
            page = doc.startPage(PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, pageNumber).create())
            canvas = page.canvas
            y = MARGIN + 20f
        }

        val rawTotal = data.items.sumOf { it.rawGoldValue }
        val wageTotal = data.items.sumOf { it.wageAmount }
        val profitTotal = data.items.sumOf { it.profitAmount }
        val taxTotal = data.items.sumOf { it.taxAmount }

        val labelP = paint(10f, regular, GRAY)
        val valueP = paint(10f, regular, INK, Paint.Align.LEFT)
        val boxTop = y
        canvas.drawRoundRect(left, boxTop, right, boxTop + 150f, 8f, 8f, Paint().apply {
            color = 0xFFFDF8EA.toInt()
        })
        canvas.drawRoundRect(left, boxTop, right, boxTop + 150f, 8f, 8f, Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 1f
            color = GOLD
        })

        y = boxTop + 24f
        fun totalRow(label: String, value: String, boldRow: Boolean = false) {
            canvas.drawText(label, right - 14f, y, if (boldRow) paint(12f, bold, GOLD) else labelP)
            canvas.drawText(value, left + 14f, y, if (boldRow) paint(12f, bold, GOLD, Paint.Align.LEFT) else valueP)
            y += 21f
        }
        totalRow("ارزش طلای خام:", "${rawTotal.toMoney()} تومان")
        totalRow("اجرت ساخت:", "${wageTotal.toMoney()} تومان")
        totalRow("سود فروشنده:", "${profitTotal.toMoney()} تومان")
        totalRow("مالیات بر ارزش افزوده (${invoice.taxPercent.formatWeight()}٪):", "${taxTotal.toMoney()} تومان")
        totalRow("تخفیف:", "${invoice.discount.toMoney()} تومان")
        y += 4f
        totalRow("مبلغ قابل پرداخت:", "${invoice.grandTotal.toMoney()} تومان", boldRow = true)

        if (invoice.note.isNotBlank()) {
            y += 16f
            canvas.drawText("توضیحات: ${invoice.note}", right, y, paint(10f, regular, GRAY))
        }

        // ---------- Footer ----------
        canvas.drawText(
            "این فاکتور توسط اپلیکیشن زرفام صادر شده است",
            PAGE_W / 2f, PAGE_H - 24f, paint(9f, regular, GRAY, Paint.Align.CENTER),
        )

        doc.finishPage(page)

        val dir = File(context.cacheDir, "invoices").apply { mkdirs() }
        val file = File(dir, "invoice_${invoice.invoiceNumber}.pdf")
        file.outputStream().use { doc.writeTo(it) }
        doc.close()
        return file
    }
}
