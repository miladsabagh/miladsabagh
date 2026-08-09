package com.miladsabagh.zarrin.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.miladsabagh.zarrin.data.ShopSettings
import com.miladsabagh.zarrin.data.db.InvoiceWithItems
import java.io.File
import java.io.FileOutputStream

/**
 * Renders an A4 invoice PDF. Android's Canvas shapes Persian/Arabic text correctly,
 * so drawText is safe for RTL strings. Coordinates are measured from the right edge
 * so the layout reads right-to-left.
 */
object InvoicePdf {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 40

    private val gold = Color.rgb(140, 109, 18)
    private val ink = Color.rgb(27, 23, 16)
    private val muted = Color.rgb(110, 100, 80)
    private val rule = Color.rgb(222, 210, 180)
    private val band = Color.rgb(248, 240, 222)

    fun generate(context: Context, data: InvoiceWithItems, settings: ShopSettings): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        drawInvoice(canvas, data, settings)

        document.finishPage(page)

        val dir = File(context.cacheDir, "invoices").apply { mkdirs() }
        val file = File(dir, "invoice_${data.invoice.invoiceNumber}.pdf")
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        return file
    }

    fun share(context: Context, file: File, invoiceNumber: Long) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "فاکتور ${invoiceNumber.formatInvoiceNumber()}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "اشتراک‌گذاری فاکتور").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    private fun drawInvoice(canvas: Canvas, data: InvoiceWithItems, settings: ShopSettings) {
        val invoice = data.invoice
        val right = (PAGE_WIDTH - MARGIN).toFloat()
        val left = MARGIN.toFloat()

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ink
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = gold
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ink
            textSize = 11f
            textAlign = Paint.Align.RIGHT
        }
        val leftTextPaint = Paint(textPaint).apply { textAlign = Paint.Align.LEFT }
        val smallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = muted
            textSize = 9f
            textAlign = Paint.Align.RIGHT
        }
        val linePaint = Paint().apply {
            color = rule
            strokeWidth = 1f
        }
        val bandPaint = Paint().apply { color = band }

        var y = MARGIN + 20f

        // Store header
        canvas.drawText(settings.storeName, right, y, titlePaint)
        y += 18f
        canvas.drawText("تلفن: ${settings.storePhone.toPersianDigits()}", right, y, smallPaint)
        canvas.drawText(settings.storeAddress.toPersianDigits(), left, y, smallPaint)
        y += 14f
        canvas.drawLine(left, y, right, y, linePaint)
        y += 22f

        // Invoice meta
        canvas.drawText("فاکتور فروش طلا و جواهر", right, y, headerPaint)
        canvas.drawText("شماره: ${invoice.invoiceNumber.formatInvoiceNumber()}", left, y, textPaint.apply { textAlign = Paint.Align.LEFT })
        textPaint.textAlign = Paint.Align.RIGHT
        y += 18f
        canvas.drawText("خریدار: ${invoice.customerName}", right, y, textPaint)
        canvas.drawText("تاریخ: ${invoice.createdAt.formatDateTime()}", left, y, leftTextPaint)
        y += 16f
        canvas.drawText("تلفن خریدار: ${invoice.customerPhone.toPersianDigits()}", right, y, textPaint)
        canvas.drawText(
            "نرخ روز طلای ۱۸ عیار: ${invoice.goldPricePerGram18k.formatToman()}",
            left, y, leftTextPaint
        )
        y += 14f
        canvas.drawLine(left, y, right, y, linePaint)
        y += 20f

        // Table header
        val colTitle = right
        val colKarat = right - 165
        val colWeight = right - 225
        val colWage = right - 290
        val colGold = right - 380
        val colTotal = left

        canvas.drawRect(RectF(left, y - 14f, right, y + 6f), bandPaint)
        canvas.drawText("شرح کالا", colTitle - 6, y, headerPaint)
        canvas.drawText("عیار", colKarat, y, headerPaint)
        canvas.drawText("وزن", colWeight, y, headerPaint)
        canvas.drawText("اجرت/گرم", colWage, y, headerPaint)
        canvas.drawText("طلای خام", colGold, y, headerPaint)
        canvas.drawText("مبلغ نهایی", colTotal + 62, y, headerPaint)
        y += 22f

        data.items.forEach { item ->
            val title = if (item.title.length > 34) item.title.take(33) + "…" else item.title
            canvas.drawText(title, colTitle - 6, y, textPaint)
            canvas.drawText(item.karat.toString().toPersianDigits(), colKarat, y, textPaint)
            canvas.drawText(item.weightGrams.formatGram(), colWeight, y, textPaint)
            canvas.drawText(item.wagePerGram.formatToman(false), colWage, y, textPaint)
            canvas.drawText(item.goldValue.formatToman(false), colGold, y, textPaint)
            canvas.drawText(item.lineTotal.formatToman(false), colTotal + 62, y, textPaint)
            y += 18f
            canvas.drawLine(left, y - 12f, right, y - 12f, linePaint)
        }

        y += 16f

        // Totals block (right-aligned label/value pairs)
        fun totalRow(label: String, value: String, bold: Boolean = false) {
            val labelPaint = if (bold) headerPaint else textPaint
            val valuePaint = Paint(textPaint).apply {
                textAlign = Paint.Align.LEFT
                if (bold) typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText(label, right, y, labelPaint)
            canvas.drawText(value, left + 150, y, valuePaint)
            y += 17f
        }

        totalRow("جمع طلای خام:", invoice.goldValue.formatToman())
        totalRow("جمع اجرت ساخت:", invoice.wageTotal.formatToman())
        totalRow(
            "سود فروشنده (${invoice.profitPercent.toString().toPersianDigits()}٪):",
            invoice.profitAmount.formatToman()
        )
        totalRow(
            "مالیات ارزش افزوده (${invoice.taxPercent.toString().toPersianDigits()}٪):",
            invoice.taxAmount.formatToman()
        )
        canvas.drawLine(left, y - 8f, right, y - 8f, linePaint)
        y += 6f
        totalRow("مبلغ قابل پرداخت:", invoice.grandTotal.formatToman(), bold = true)

        // Footer
        val footerY = PAGE_HEIGHT - MARGIN - 24f
        canvas.drawLine(left, footerY, right, footerY, linePaint)
        canvas.drawText(
            "کالای فروخته‌شده طبق نرخ روز محاسبه شده است. از خرید شما سپاسگزاریم.",
            right, footerY + 16f, smallPaint
        )
    }
}
