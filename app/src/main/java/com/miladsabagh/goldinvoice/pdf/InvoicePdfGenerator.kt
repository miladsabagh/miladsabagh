package com.miladsabagh.goldinvoice.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import androidx.core.content.res.ResourcesCompat
import com.miladsabagh.goldinvoice.R
import com.miladsabagh.goldinvoice.data.dao.InvoiceWithItems
import com.miladsabagh.goldinvoice.data.repository.ShopSettings
import com.miladsabagh.goldinvoice.util.formatCurrency
import com.miladsabagh.goldinvoice.util.formatInvoiceDate
import com.miladsabagh.goldinvoice.util.formatWeight
import com.miladsabagh.goldinvoice.util.toPersianDigits
import java.io.File
import java.io.FileOutputStream

/**
 * Renders a printable A4 invoice (فاکتور فروش) as a PDF using the Vazirmatn typeface
 * so that Persian text and digits render correctly regardless of device locale.
 */
object InvoicePdfGenerator {

    private const val PAGE_WIDTH = 595 // A4 at 72dpi
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 36f

    fun generate(context: Context, data: InvoiceWithItems, shop: ShopSettings): File {
        val typeface = ResourcesCompat.getFont(context, R.font.vazirmatn_regular)
        val boldTypeface = ResourcesCompat.getFont(context, R.font.vazirmatn_bold)

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val rightEdge = PAGE_WIDTH - MARGIN
        val leftEdge = MARGIN
        var y = MARGIN + 20f

        val titlePaint = textPaint(boldTypeface, 20f, Color.rgb(0x2B, 0x26, 0x20))
        val subPaint = textPaint(typeface, 11f, Color.rgb(0x5A, 0x50, 0x40))
        val labelPaint = textPaint(typeface, 10.5f, Color.rgb(0x5A, 0x50, 0x40))
        val valuePaint = textPaint(boldTypeface, 11.5f, Color.rgb(0x2B, 0x26, 0x20))
        val headerBg = Paint().apply { color = Color.rgb(0xC9, 0xA2, 0x27) }
        val tableHeaderPaint = textPaint(boldTypeface, 10f, Color.WHITE)
        val cellPaint = textPaint(typeface, 10f, Color.rgb(0x2B, 0x26, 0x20))
        val linePaint = Paint().apply {
            color = Color.rgb(0xE0, 0xD8, 0xC0)
            strokeWidth = 1f
        }

        canvas.drawText(shop.shopName.ifBlank { "طلا و جواهر" }, rightEdge, y, titlePaint)
        y += 18f
        if (shop.shopAddress.isNotBlank()) {
            canvas.drawText(shop.shopAddress, rightEdge, y, subPaint)
            y += 15f
        }
        val contactLine = buildString {
            if (shop.shopPhone.isNotBlank()) append("تلفن: ${shop.shopPhone.toPersianDigits()}")
            if (shop.shopLicenseNumber.isNotBlank()) {
                if (isNotEmpty()) append("   |   ")
                append("پروانه کسب: ${shop.shopLicenseNumber.toPersianDigits()}")
            }
        }
        if (contactLine.isNotBlank()) {
            canvas.drawText(contactLine, rightEdge, y, subPaint)
            y += 15f
        }

        y += 6f
        canvas.drawLine(leftEdge, y, rightEdge, y, linePaint)
        y += 22f

        // Invoice meta info (invoice number, date) and customer info side by side.
        canvas.drawText("شماره فاکتور:", rightEdge, y, labelPaint)
        canvas.drawText(data.invoice.invoiceNumber.toPersianDigits(), rightEdge - 78f, y, valuePaint)

        canvas.drawText("تاریخ:", rightEdge - 220f, y, labelPaint)
        canvas.drawText(formatInvoiceDate(data.invoice.createdAt), rightEdge - 260f, y, valuePaint)
        y += 20f

        val customerName = data.invoice.customerNameSnapshot.ifBlank { "مشتری متفرقه" }
        canvas.drawText("مشتری:", rightEdge, y, labelPaint)
        canvas.drawText(customerName, rightEdge - 78f, y, valuePaint)
        if (data.invoice.customerPhoneSnapshot.isNotBlank()) {
            canvas.drawText("تلفن مشتری:", rightEdge - 220f, y, labelPaint)
            canvas.drawText(data.invoice.customerPhoneSnapshot.toPersianDigits(), rightEdge - 300f, y, valuePaint)
        }
        y += 26f

        // Table header
        val tableTop = y
        val tableHeight = 26f
        canvas.drawRect(RectF(leftEdge, tableTop, rightEdge, tableTop + tableHeight), headerBg)

        // Column boundaries from right to left: name | weight | karat | qty | unit price | line total
        val colRight = rightEdge
        val colName = colRight - 8f
        val colWeightX = leftEdge + 400f
        val colKaratX = leftEdge + 330f
        val colQtyX = leftEdge + 280f
        val colUnitX = leftEdge + 200f
        val colTotalX = leftEdge + 90f

        val headerBaseline = tableTop + 17f
        canvas.drawText("شرح کالا", colName, headerBaseline, tableHeaderPaint)
        canvas.drawText("وزن (گرم)", colWeightX, headerBaseline, tableHeaderPaint)
        canvas.drawText("عیار", colKaratX, headerBaseline, tableHeaderPaint)
        canvas.drawText("تعداد", colQtyX, headerBaseline, tableHeaderPaint)
        canvas.drawText("قیمت واحد", colUnitX, headerBaseline, tableHeaderPaint)
        canvas.drawText("جمع", colTotalX, headerBaseline, tableHeaderPaint)

        y = tableTop + tableHeight + 18f

        data.items.forEach { item ->
            canvas.drawText(item.itemName, colName, y, cellPaint)
            canvas.drawText(formatWeight(item.weightGrams), colWeightX, y, cellPaint)
            canvas.drawText(item.karat.toString().toPersianDigits(), colKaratX, y, cellPaint)
            canvas.drawText(item.quantity.toString().toPersianDigits(), colQtyX, y, cellPaint)
            canvas.drawText(formatCurrency(item.unitPrice), colUnitX, y, cellPaint)
            canvas.drawText(formatCurrency(item.lineTotal), colTotalX, y, cellPaint)
            y += 16f
            canvas.drawLine(leftEdge, y - 10f, rightEdge, y - 10f, linePaint)
        }

        y += 14f
        canvas.drawLine(leftEdge, y, rightEdge, y, linePaint)
        y += 24f

        drawTotalRow(canvas, "جمع کل", data.invoice.subtotal, rightEdge, y, labelPaint, valuePaint)
        y += 18f
        drawTotalRow(canvas, "تخفیف", data.invoice.discountAmount, rightEdge, y, labelPaint, valuePaint)
        y += 18f
        drawTotalRow(canvas, "مبلغ قابل پرداخت", data.invoice.grandTotal, rightEdge, y, labelPaint, textPaint(boldTypeface, 13f, Color.rgb(0x9C, 0x7A, 0x0F)))
        y += 18f
        drawTotalRow(canvas, "مبلغ پرداخت‌شده", data.invoice.paidAmount, rightEdge, y, labelPaint, valuePaint)
        y += 18f
        val remaining = (data.invoice.grandTotal - data.invoice.paidAmount).coerceAtLeast(0.0)
        drawTotalRow(canvas, "مانده حساب", remaining, rightEdge, y, labelPaint, valuePaint)

        if (data.invoice.notes.isNotBlank()) {
            y += 30f
            canvas.drawText("توضیحات: ${data.invoice.notes}", rightEdge, y, subPaint)
        }

        y = PAGE_HEIGHT - MARGIN - 10f
        canvas.drawText("این فاکتور به‌صورت الکترونیکی صادر شده است.", PAGE_WIDTH / 2f, y, textPaint(typeface, 9f, Color.GRAY).apply { textAlign = Paint.Align.CENTER })

        document.finishPage(page)

        val outputDir = File(context.cacheDir, "invoices").apply { mkdirs() }
        val outputFile = File(outputDir, "invoice_${data.invoice.invoiceNumber}.pdf")
        FileOutputStream(outputFile).use { document.writeTo(it) }
        document.close()
        return outputFile
    }

    private fun drawTotalRow(
        canvas: Canvas,
        label: String,
        amount: Double,
        rightEdge: Float,
        y: Float,
        labelPaint: Paint,
        valuePaint: Paint
    ) {
        canvas.drawText(label, rightEdge, y, labelPaint)
        val valueText = "${formatCurrency(amount)} تومان"
        canvas.drawText(valueText, rightEdge - 140f, y, valuePaint)
    }

    private fun textPaint(typeface: android.graphics.Typeface?, size: Float, color: Int): Paint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.typeface = typeface
            textSize = size
            this.color = color
            textAlign = Paint.Align.RIGHT
        }
}
