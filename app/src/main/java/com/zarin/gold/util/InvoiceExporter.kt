package com.zarin.gold.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.zarin.gold.data.Invoice
import java.io.File
import java.io.FileOutputStream

object InvoiceExporter {

    fun sharePdf(context: Context, invoice: Invoice) {
        val file = createPdf(context, invoice)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "فاکتور ${invoice.invoiceNumber}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "اشتراک‌گذاری فاکتور"))
    }

    fun createPdf(context: Context, invoice: Invoice): File {
        val dir = File(context.cacheDir, "invoices").apply { mkdirs() }
        val file = File(dir, "${invoice.invoiceNumber}.pdf")

        val doc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = doc.startPage(pageInfo)
        drawInvoice(page.canvas, invoice)
        doc.finishPage(page)

        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
        return file
    }

    private fun drawInvoice(canvas: Canvas, invoice: Invoice) {
        val title = Paint().apply {
            textSize = 22f
            isFakeBoldText = true
            color = 0xFF1A1A1A.toInt()
            textAlign = Paint.Align.RIGHT
        }
        val body = Paint().apply {
            textSize = 12f
            color = 0xFF222222.toInt()
            textAlign = Paint.Align.RIGHT
        }
        val muted = Paint().apply {
            textSize = 11f
            color = 0xFF666666.toInt()
            textAlign = Paint.Align.RIGHT
        }
        val linePaint = Paint().apply {
            color = 0xFFD4AF37.toInt()
            strokeWidth = 1.5f
        }

        var y = 56f
        val right = 545f

        canvas.drawText("گالری زرین — فاکتور فروش", right, y, title)
        y += 28f
        canvas.drawLine(50f, y, right, y, linePaint)
        y += 28f

        canvas.drawText("شماره: ${invoice.invoiceNumber}", right, y, body)
        y += 18f
        canvas.drawText("تاریخ: ${invoice.createdAt.toPersianDateTime()}", right, y, body)
        y += 18f
        canvas.drawText("مشتری: ${invoice.customerName}", right, y, body)
        y += 18f
        if (invoice.customerPhone.isNotBlank()) {
            canvas.drawText("تلفن: ${invoice.customerPhone.toPersianDigits()}", right, y, body)
            y += 18f
        }
        canvas.drawText(
            "قیمت پایه طلای ۱۸: ${invoice.goldPricePerGram18.toPersianCurrency()}",
            right,
            y,
            muted
        )
        y += 30f

        canvas.drawText("شرح کالا", right, y, body)
        canvas.drawText("مبلغ", 120f, y, body.apply { textAlign = Paint.Align.LEFT })
        body.textAlign = Paint.Align.RIGHT
        y += 10f
        canvas.drawLine(50f, y, right, y, linePaint)
        y += 20f

        invoice.lines.forEach { line ->
            val desc =
                "${line.productName} | ${line.carat} عیار | ${line.weightGram.toPersianWeight()} × ${line.quantity.toPersian()}"
            canvas.drawText(desc, right, y, body)
            canvas.drawText(
                line.lineTotal.toPersianCurrency(),
                50f,
                y,
                Paint(body).apply { textAlign = Paint.Align.LEFT }
            )
            y += 18f
            if (y > 760f) return@forEach
        }

        y += 16f
        canvas.drawLine(50f, y, right, y, linePaint)
        y += 24f
        canvas.drawText("جمع جزء: ${invoice.subtotal.toPersianCurrency()}", right, y, body)
        y += 18f
        canvas.drawText("تخفیف: ${invoice.discount.toPersianCurrency()}", right, y, body)
        y += 18f
        canvas.drawText("مالیات ۹٪: ${invoice.tax.toPersianCurrency()}", right, y, body)
        y += 22f
        canvas.drawText("مبلغ قابل پرداخت: ${invoice.total.toPersianCurrency()}", right, y, title)
        y += 28f
        if (invoice.note.isNotBlank()) {
            canvas.drawText("یادداشت: ${invoice.note}", right, y, muted)
        }
        y += 40f
        canvas.drawText("با سپاس از اعتماد شما — زرین", right, y, muted)
    }
}
