package com.zarrin.goldshop.invoice

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.zarrin.goldshop.data.InvoiceWithItems
import com.zarrin.goldshop.data.ShopSettingsEntity
import com.zarrin.goldshop.domain.formatDateFa
import com.zarrin.goldshop.domain.formatPercentFa
import com.zarrin.goldshop.domain.formatTomanFa
import com.zarrin.goldshop.domain.formatWeightFa
import com.zarrin.goldshop.domain.toPersianDigits
import java.io.File
import java.io.FileOutputStream

object InvoicePdfWriter {

    fun write(
        context: Context,
        invoiceWithItems: InvoiceWithItems,
        settings: ShopSettingsEntity
    ): android.net.Uri {
        val invoice = invoiceWithItems.invoice
        val dir = File(context.cacheDir, "invoices").apply { mkdirs() }
        val file = File(dir, "${invoice.invoiceNumber}.pdf")

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 20f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
            color = 0xFF1A1410.toInt()
        }
        val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 11f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            textAlign = Paint.Align.RIGHT
            color = 0xFF2C241B.toInt()
        }
        val mutedPaint = TextPaint(bodyPaint).apply {
            color = 0xFF6B5E4E.toInt()
            textSize = 10f
        }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFC9A227.toInt()
            strokeWidth = 1.5f
        }

        var y = 48f
        val right = 555f
        val left = 40f

        canvas.drawText(settings.shopName, right, y, titlePaint)
        y += 22f
        canvas.drawText("فاکتور فروش طلا و جواهر", right, y, bodyPaint)
        y += 18f
        canvas.drawText("شماره: ${toPersianDigits(invoice.invoiceNumber)}", right, y, mutedPaint)
        y += 16f
        canvas.drawText("تاریخ: ${formatDateFa(invoice.createdAt)}", right, y, mutedPaint)
        y += 12f
        canvas.drawLine(left, y, right, y, linePaint)
        y += 24f

        canvas.drawText("خریدار: ${invoice.customerName}", right, y, bodyPaint)
        y += 16f
        if (invoice.customerPhone.isNotBlank()) {
            canvas.drawText("تلفن: ${toPersianDigits(invoice.customerPhone)}", right, y, mutedPaint)
            y += 16f
        }
        canvas.drawText(
            "قیمت روز طلای ۱۸ عیار: ${formatTomanFa(invoice.goldPrice18PerGram)}",
            right,
            y,
            mutedPaint
        )
        y += 24f

        canvas.drawText("اقلام فاکتور", right, y, titlePaint.apply { textSize = 14f })
        y += 18f

        invoiceWithItems.items.forEachIndexed { index, item ->
            val header = "${toPersianDigits((index + 1).toString())}. ${item.productName}"
            canvas.drawText(header, right, y, bodyPaint)
            y += 14f
            val detail = "کد ${toPersianDigits(item.productCode)} | " +
                "${formatWeightFa(item.weightGrams)} | " +
                "عیار ${toPersianDigits(item.purityKarat.toString())} | " +
                "اجرت ${formatPercentFa(item.makingFeePercent)} | " +
                formatTomanFa(item.lineTotal)
            drawWrapped(canvas, detail, left, right, y, mutedPaint) { y = it }
            y += 10f
        }

        y += 8f
        canvas.drawLine(left, y, right, y, linePaint)
        y += 20f

        fun row(label: String, value: String) {
            canvas.drawText("$label: $value", right, y, bodyPaint)
            y += 16f
        }

        row("جمع اقلام", formatTomanFa(invoice.subtotal))
        row("سود (${formatPercentFa(invoice.profitPercent)})", formatTomanFa(invoice.profitAmount))
        row("مالیات (${formatPercentFa(invoice.vatPercent)})", formatTomanFa(invoice.vatAmount))
        titlePaint.textSize = 15f
        canvas.drawText("مبلغ قابل پرداخت: ${formatTomanFa(invoice.totalAmount)}", right, y, titlePaint)
        y += 18f
        canvas.drawText("پرداخت‌شده: ${formatTomanFa(invoice.paidAmount)}", right, y, bodyPaint)
        y += 16f
        val remain = (invoice.totalAmount - invoice.paidAmount).coerceAtLeast(0)
        canvas.drawText("مانده: ${formatTomanFa(remain)}", right, y, bodyPaint)

        if (invoice.notes.isNotBlank()) {
            y += 24f
            canvas.drawText("توضیحات: ${invoice.notes}", right, y, mutedPaint)
        }

        y = 780f
        if (settings.shopAddress.isNotBlank()) {
            canvas.drawText(settings.shopAddress, right, y, mutedPaint)
            y += 14f
        }
        if (settings.shopPhone.isNotBlank()) {
            canvas.drawText("تلفن فروشگاه: ${toPersianDigits(settings.shopPhone)}", right, y, mutedPaint)
        }

        document.finishPage(page)
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    private fun drawWrapped(
        canvas: android.graphics.Canvas,
        text: String,
        left: Float,
        right: Float,
        startY: Float,
        paint: TextPaint,
        onY: (Float) -> Unit
    ) {
        val width = (right - left).toInt()
        val layout = StaticLayout.Builder
            .obtain(text, 0, text.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_OPPOSITE)
            .setIncludePad(false)
            .build()
        canvas.save()
        canvas.translate(left, startY - paint.textSize)
        layout.draw(canvas)
        canvas.restore()
        onY(startY + layout.height)
    }
}
