package com.miladsabagh.goldshop.pdf

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import com.miladsabagh.goldshop.util.JalaliDate
import com.miladsabagh.goldshop.util.PersianFormat

/**
 * Draws a gold/jewelry sales invoice onto one or more PDF pages using [StaticLayout] so that
 * Persian (RTL) text is shaped and reordered correctly by the platform's text layout engine.
 *
 * Works with any [PdfDocument] subtype (including [android.print.pdf.PrintedPdfDocument]) so the
 * same drawing code powers both "share as PDF" and "print" flows.
 */
object InvoicePdfRenderer {

    private const val MARGIN = 36f
    private val ROW_HEIGHT = 26f

    private data class Column(val title: String, val width: Float)

    private val columns = listOf(
        Column("ردیف", 26f),
        Column("شرح کالا", 150f),
        Column("وزن(گرم)", 55f),
        Column("عيار", 32f),
        Column("تعداد", 32f),
        Column("اجرت٪", 38f),
        Column("سود٪", 38f),
        Column("مبلغ کل(تومان)", 120f)
    )

    fun renderToDocument(document: PdfDocument, pageWidth: Int, pageHeight: Int, content: InvoiceContent) {
        var itemIndex = 0
        var pageNumber = 1
        do {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            val page = document.startPage(pageInfo)
            itemIndex = drawPage(page.canvas, pageWidth.toFloat(), pageHeight.toFloat(), content, itemIndex, pageNumber)
            document.finishPage(page)
            pageNumber++
        } while (itemIndex < content.items.size)
    }

    private fun drawPage(
        canvas: Canvas,
        pageWidth: Float,
        pageHeight: Float,
        content: InvoiceContent,
        startItemIndex: Int,
        pageNumber: Int
    ): Int {
        val contentRight = pageWidth - MARGIN
        val contentLeft = MARGIN
        val contentWidth = contentRight - contentLeft
        var y = MARGIN

        val titlePaint = textPaint(20f, bold = true)
        val subPaint = textPaint(11f)
        val labelPaint = textPaint(12f)
        val boldLabelPaint = textPaint(12f, bold = true)
        val headerCellPaint = textPaint(10f, bold = true, color = Color.WHITE)
        val cellPaint = textPaint(10f)

        y += drawText(canvas, content.store.storeName, titlePaint, contentLeft, y, contentWidth, Layout.Alignment.ALIGN_NORMAL)
        y += 2f
        val storeInfo = listOfNotNull(
            content.store.storeAddress.ifBlank { null },
            content.store.storePhone.ifBlank { null }?.let { "تلفن: $it" }
        ).joinToString("   |   ")
        if (storeInfo.isNotBlank()) {
            y += drawText(canvas, storeInfo, subPaint, contentLeft, y, contentWidth, Layout.Alignment.ALIGN_NORMAL)
        }
        y += 8f
        canvas.drawLine(contentLeft, y, contentRight, y, borderPaint())
        y += 12f

        if (pageNumber == 1) {
            // Invoice meta + customer info
            val metaLeftText = "تاریخ: ${JalaliDate.formatFull(content.invoice.issuedAt)}"
            val metaRightText = "فاکتور فروش شماره: ${PersianFormat.toPersianDigits(content.invoice.invoiceNumber.toString())}"
            y += drawTwoColumnLine(canvas, metaRightText, metaLeftText, boldLabelPaint, contentLeft, y, contentWidth)
            y += 4f

            val custRightText = "خریدار: ${content.invoice.customerName}"
            val custLeftText = if (content.invoice.customerPhone.isNotBlank()) "تلفن: ${content.invoice.customerPhone}" else ""
            y += drawTwoColumnLine(canvas, custRightText, custLeftText, labelPaint, contentLeft, y, contentWidth)
            y += 4f

            val priceText = "نرخ پایه طلای ۱۸ عيار: ${PersianFormat.formatToman(content.invoice.goldPriceAtSale)}"
            y += drawText(canvas, priceText, labelPaint, contentLeft, y, contentWidth, Layout.Alignment.ALIGN_NORMAL)
            y += 10f
        }

        // Table header (right to left column order)
        val headerHeight = 24f
        canvas.drawRect(contentLeft, y, contentRight, y + headerHeight, headerBgPaint())
        var cursorX = contentRight
        for (col in columns) {
            val boxLeft = cursorX - col.width
            drawCell(canvas, col.title, headerCellPaint, boxLeft, y + 5f, col.width)
            cursorX = boxLeft
        }
        y += headerHeight

        var index = startItemIndex
        val hardLimitY = pageHeight - MARGIN
        val summaryReservedY = hardLimitY - 170f // space needed to fit the totals block below the table
        while (index < content.items.size) {
            val isLastItem = index == content.items.size - 1
            val limit = if (isLastItem) summaryReservedY else hardLimitY
            if (y + ROW_HEIGHT > limit) break
            val item = content.items[index]
            cursorX = contentRight
            val rowValues = listOf(
                PersianFormat.toPersianDigits((index + 1).toString()),
                item.itemName,
                PersianFormat.toPersianDigits(String.format(java.util.Locale.US, "%.3f", item.weightGrams)),
                PersianFormat.toPersianDigits(item.karat.toString()),
                PersianFormat.toPersianDigits(item.quantity.toString()),
                PersianFormat.toPersianDigits(String.format(java.util.Locale.US, "%.1f", item.laborFeePercent)),
                PersianFormat.toPersianDigits(String.format(java.util.Locale.US, "%.1f", item.profitPercent)),
                PersianFormat.formatNumber(item.lineTotal)
            )
            for ((i, col) in columns.withIndex()) {
                val boxLeft = cursorX - col.width
                drawCell(canvas, rowValues[i], cellPaint, boxLeft, y + 6f, col.width)
                cursorX = boxLeft
            }
            canvas.drawLine(contentLeft, y + ROW_HEIGHT, contentRight, y + ROW_HEIGHT, thinLinePaint())
            y += ROW_HEIGHT
            index++
        }

        if (index >= content.items.size) {
            y += 16f
            y += drawSummary(canvas, content, contentLeft, y, contentWidth)
        } else {
            y += 10f
            drawText(canvas, "ادامه در صفحه بعد...", subPaint, contentLeft, y, contentWidth, Layout.Alignment.ALIGN_NORMAL)
        }

        return index
    }

    private fun drawSummary(canvas: Canvas, content: InvoiceContent, left: Float, top: Float, width: Float): Float {
        val labelPaint = textPaint(12f)
        val boldPaint = textPaint(13f, bold = true)
        var y = top
        val inv = content.invoice

        y += drawTwoColumnLine(canvas, "جمع وزن: ${PersianFormat.formatWeight(inv.totalWeightGrams)}", "", labelPaint, left, y, width)
        y += 4f
        y += drawTwoColumnLine(canvas, "جمع اجناس (بدون مالیات): ${PersianFormat.formatToman(inv.subtotalAmount)}", "", labelPaint, left, y, width)
        y += 4f
        y += drawTwoColumnLine(canvas, "مالیات ارزش افزوده: ${PersianFormat.formatToman(inv.taxAmount)}", "", labelPaint, left, y, width)
        y += 4f
        y += drawTwoColumnLine(canvas, "تخفیف: ${PersianFormat.formatToman(inv.discountAmount)}", "", labelPaint, left, y, width)
        y += 6f
        canvas.drawLine(left, y, left + width, y, borderPaint())
        y += 8f
        y += drawTwoColumnLine(canvas, "مبلغ نهایی قابل پرداخت: ${PersianFormat.formatToman(inv.totalAmount)}", "", boldPaint, left, y, width)
        y += 4f
        y += drawTwoColumnLine(canvas, "مبلغ پرداختی: ${PersianFormat.formatToman(inv.paidAmount)}", "", labelPaint, left, y, width)
        y += 4f
        y += drawTwoColumnLine(canvas, "مانده حساب: ${PersianFormat.formatToman(inv.remainingAmount)}", "", labelPaint, left, y, width)
        if (inv.notes.isNotBlank()) {
            y += 8f
            y += drawText(canvas, "توضیحات: ${inv.notes}", labelPaint, left, y, width, Layout.Alignment.ALIGN_NORMAL)
        }
        y += 16f
        y += drawText(
            canvas,
            "این فاکتور توسط اپلیکیشن جواهریار صادر شده است.",
            textPaint(9f, italic = true),
            left,
            y,
            width,
            Layout.Alignment.ALIGN_NORMAL
        )
        return y - top
    }

    private fun drawCell(canvas: Canvas, text: String, paint: TextPaint, boxLeft: Float, top: Float, width: Float) {
        val layout = StaticLayout.Builder
            .obtain(text, 0, text.length, paint, width.toInt().coerceAtLeast(1))
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setTextDirection(TextDirectionHeuristics.RTL)
            .setIncludePad(false)
            .setMaxLines(2)
            .build()
        canvas.save()
        canvas.translate(boxLeft, top)
        layout.draw(canvas)
        canvas.restore()
    }

    /** Draws [rightText] right-aligned and [leftText] left-aligned on the same visual line. */
    private fun drawTwoColumnLine(
        canvas: Canvas,
        rightText: String,
        leftText: String,
        paint: TextPaint,
        left: Float,
        top: Float,
        width: Float
    ): Float {
        val halfWidth = width / 2f
        var maxHeight = 0f
        if (rightText.isNotBlank()) {
            maxHeight = maxOf(maxHeight, drawText(canvas, rightText, paint, left + halfWidth, top, halfWidth, Layout.Alignment.ALIGN_NORMAL))
        }
        if (leftText.isNotBlank()) {
            maxHeight = maxOf(maxHeight, drawText(canvas, leftText, paint, left, top, halfWidth, Layout.Alignment.ALIGN_OPPOSITE))
        }
        return maxHeight
    }

    /** Draws RTL-aware text within a box and returns the consumed height. */
    private fun drawText(
        canvas: Canvas,
        text: String,
        paint: TextPaint,
        boxLeft: Float,
        top: Float,
        width: Float,
        alignment: Layout.Alignment
    ): Float {
        if (text.isBlank()) return 0f
        val layout = StaticLayout.Builder
            .obtain(text, 0, text.length, paint, width.toInt().coerceAtLeast(1))
            .setAlignment(alignment)
            .setTextDirection(TextDirectionHeuristics.RTL)
            .setIncludePad(false)
            .build()
        canvas.save()
        canvas.translate(boxLeft, top)
        layout.draw(canvas)
        canvas.restore()
        return layout.height.toFloat()
    }

    private fun textPaint(size: Float, bold: Boolean = false, italic: Boolean = false, color: Int = Color.BLACK): TextPaint {
        return TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = size
            this.color = color
            isFakeBoldText = bold
            textSkewX = if (italic) -0.2f else 0f
        }
    }

    private fun borderPaint(): Paint = Paint().apply {
        color = Color.rgb(0xC9, 0xA2, 0x27)
        strokeWidth = 1.5f
    }

    private fun thinLinePaint(): Paint = Paint().apply {
        color = Color.LTGRAY
        strokeWidth = 0.5f
    }

    private fun headerBgPaint(): Paint = Paint().apply {
        color = Color.rgb(0xC9, 0xA2, 0x27)
    }
}
