package com.goldshop.app.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.TextPaint
import com.goldshop.app.data.model.InvoiceWithItems
import com.goldshop.app.data.model.ShopSettings
import java.io.File
import java.io.FileOutputStream

object InvoicePdfGenerator {

    fun generate(
        context: Context,
        invoiceWithItems: InvoiceWithItems,
        settings: ShopSettings
    ): File {
        val invoice = invoiceWithItems.invoice
        val items = invoiceWithItems.items

        val pageWidth = 595 // A4 points
        val pageHeight = 842
        val doc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = doc.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            color = 0xFF1A1410.toInt()
        }
        val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 12f
            color = 0xFF2C241C.toInt()
            textAlign = Paint.Align.RIGHT
        }
        val boldPaint = TextPaint(bodyPaint).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val linePaint = Paint().apply {
            color = 0xFFC9A227.toInt()
            strokeWidth = 2f
        }
        val thinLine = Paint().apply {
            color = 0xFFD0C4A8.toInt()
            strokeWidth = 1f
        }

        var y = 48f
        val right = pageWidth - 40f
        val left = 40f
        val center = pageWidth / 2f

        canvas.drawText(settings.shopName, center, y, titlePaint)
        y += 24f
        bodyPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("فاکتور فروش طلا و جواهر", center, y, bodyPaint)
        y += 16f
        if (settings.shopPhone.isNotBlank()) {
            canvas.drawText("تلفن: ${settings.shopPhone.toPersianDigits()}", center, y, bodyPaint)
            y += 16f
        }
        if (settings.shopAddress.isNotBlank()) {
            canvas.drawText(settings.shopAddress, center, y, bodyPaint)
            y += 16f
        }
        y += 8f
        canvas.drawLine(left, y, right, y, linePaint)
        y += 28f

        bodyPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("شماره فاکتور: ${invoice.invoiceNumber.toPersianDigits()}", right, y, boldPaint)
        y += 18f
        canvas.drawText("تاریخ: ${invoice.createdAt.formatDateFa()}", right, y, bodyPaint)
        y += 18f
        canvas.drawText("مشتری: ${invoice.customerName}", right, y, bodyPaint)
        y += 18f
        if (invoice.customerPhone.isNotBlank()) {
            canvas.drawText("موبایل: ${invoice.customerPhone.toPersianDigits()}", right, y, bodyPaint)
            y += 18f
        }
        canvas.drawText(
            "نرخ طلای ۱۸ عیار: ${invoice.goldPricePerGram18.formatToman()}",
            right,
            y,
            bodyPaint
        )
        y += 28f

        // Table header
        canvas.drawLine(left, y, right, y, thinLine)
        y += 18f
        canvas.drawText("ردیف | کالا | وزن | عیار | تعداد | مبلغ", right, y, boldPaint)
        y += 10f
        canvas.drawLine(left, y, right, y, thinLine)
        y += 20f

        items.forEachIndexed { index, item ->
            val line = buildString {
                append((index + 1).toPersianDigits())
                append(" | ")
                append(item.productName)
                append(" | ")
                append(item.weightGrams.toPersianDigits())
                append("گ | ")
                append(purityLabel(item.purity))
                append(" | ")
                append(item.quantity.toPersianDigits())
                append(" | ")
                append(item.lineTotal.formatToman())
            }
            canvas.drawText(line, right, y, bodyPaint)
            y += 18f
            if (y > pageHeight - 160) {
                // keep simple single-page layout for v1
                return@forEachIndexed
            }
        }

        y += 8f
        canvas.drawLine(left, y, right, y, thinLine)
        y += 24f
        canvas.drawText("جمع جزء: ${invoice.subtotal.formatToman()}", right, y, bodyPaint)
        y += 18f
        if (invoice.discount > 0) {
            canvas.drawText("تخفیف: ${invoice.discount.formatToman()}", right, y, bodyPaint)
            y += 18f
        }
        if (invoice.taxAmount > 0) {
            canvas.drawText("مالیات: ${invoice.taxAmount.formatToman()}", right, y, bodyPaint)
            y += 18f
        }
        canvas.drawText("مبلغ قابل پرداخت: ${invoice.total.formatToman()}", right, y, boldPaint)
        y += 18f
        canvas.drawText("پرداخت‌شده: ${invoice.paidAmount.formatToman()}", right, y, bodyPaint)

        if (invoice.notes.isNotBlank()) {
            y += 28f
            canvas.drawText("توضیحات: ${invoice.notes}", right, y, bodyPaint)
        }

        y = pageHeight - 60f
        canvas.drawLine(left, y, right, y, linePaint)
        y += 20f
        bodyPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("با تشکر از خرید شما", center, y, bodyPaint)

        doc.finishPage(page)

        val dir = File(context.cacheDir, "invoices").apply { mkdirs() }
        val file = File(dir, "${invoice.invoiceNumber}.pdf")
        FileOutputStream(file).use { out -> doc.writeTo(out) }
        doc.close()
        return file
    }
}
