package com.goldjewelry.app.util

import android.content.Context
import android.graphics.Color
import android.net.Uri
import androidx.core.content.FileProvider
import com.goldjewelry.app.data.model.InvoiceWithItems
import com.goldjewelry.app.data.model.ShopSettings
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object InvoicePdfGenerator {

    private val goldColor = DeviceRgb(212, 175, 55)
    private val darkColor = DeviceRgb(26, 26, 46)

    fun generate(
        context: Context,
        invoiceWithItems: InvoiceWithItems,
        settings: ShopSettings
    ): Uri {
        val invoice = invoiceWithItems.invoice
        val dir = File(context.getExternalFilesDir(null), "invoices").apply { mkdirs() }
        val file = File(dir, "invoice_${invoice.invoiceNumber}.pdf")

        val writer = PdfWriter(file)
        val pdfDoc = PdfDocument(writer)
        val document = Document(pdfDoc)

        val font = PdfFontFactory.createFont()

        // Header
        document.add(
            Paragraph(settings.shopName)
                .setFont(font)
                .setFontSize(20f)
                .setFontColor(goldColor)
                .setTextAlignment(TextAlignment.CENTER)
                .setBold()
        )

        if (settings.shopAddress.isNotBlank()) {
            document.add(
                Paragraph(settings.shopAddress)
                    .setFont(font)
                    .setFontSize(10f)
                    .setTextAlignment(TextAlignment.CENTER)
            )
        }

        if (settings.shopPhone.isNotBlank()) {
            document.add(
                Paragraph("تلفن: ${settings.shopPhone}")
                    .setFont(font)
                    .setFontSize(10f)
                    .setTextAlignment(TextAlignment.CENTER)
            )
        }

        document.add(Paragraph("\n"))

        // Invoice info
        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale("fa", "IR"))
        val dateStr = dateFormat.format(Date(invoice.createdAt))

        document.add(
            Paragraph("شماره فاکتور: ${invoice.invoiceNumber}")
                .setFont(font)
                .setFontSize(12f)
                .setBold()
        )
        document.add(
            Paragraph("تاریخ: $dateStr")
                .setFont(font)
                .setFontSize(11f)
        )
        document.add(
            Paragraph("مشتری: ${invoice.customerName}")
                .setFont(font)
                .setFontSize(11f)
        )
        if (invoice.customerPhone.isNotBlank()) {
            document.add(
                Paragraph("تلفن: ${invoice.customerPhone}")
                    .setFont(font)
                    .setFontSize(11f)
            )
        }

        document.add(
            Paragraph("نرخ طلا: ${PersianFormatter.formatCurrency(invoice.goldPricePerGram)} به ازای هر گرم")
                .setFont(font)
                .setFontSize(10f)
        )

        document.add(Paragraph("\n"))

        // Items table
        val table = Table(UnitValue.createPercentArray(floatArrayOf(3f, 1.5f, 1f, 1.5f, 2f)))
            .useAllAvailableWidth()

        listOf("شرح کالا", "وزن", "تعداد", "قیمت واحد", "جمع").forEach { header ->
            table.addHeaderCell(
                Cell().add(
                    Paragraph(header)
                        .setFont(font)
                        .setFontSize(10f)
                        .setBold()
                        .setFontColor(DeviceRgb(Color.WHITE))
                ).setBackgroundColor(darkColor)
            )
        }

        invoiceWithItems.items.forEach { item ->
            table.addCell(cell(item.productName, font))
            table.addCell(cell(PersianFormatter.formatWeight(item.weightGrams), font))
            table.addCell(cell(PersianFormatter.toPersianDigits(item.quantity.toString()), font))
            table.addCell(cell(PersianFormatter.formatCurrency(item.unitPrice), font))
            table.addCell(cell(PersianFormatter.formatCurrency(item.lineTotal), font))
        }

        document.add(table)
        document.add(Paragraph("\n"))

        // Totals
        document.add(totalRow("جمع کل:", PersianFormatter.formatCurrency(invoice.subtotal), font))
        if (invoice.discount > 0) {
            document.add(totalRow("تخفیف:", PersianFormatter.formatCurrency(invoice.discount), font))
        }
        if (invoice.tax > 0) {
            document.add(totalRow("مالیات:", PersianFormatter.formatCurrency(invoice.tax), font))
        }
        document.add(
            Paragraph("مبلغ قابل پرداخت: ${PersianFormatter.formatCurrency(invoice.total)}")
                .setFont(font)
                .setFontSize(14f)
                .setBold()
                .setFontColor(goldColor)
                .setTextAlignment(TextAlignment.LEFT)
        )

        if (invoice.notes.isNotBlank()) {
            document.add(Paragraph("\n"))
            document.add(
                Paragraph("توضیحات: ${invoice.notes}")
                    .setFont(font)
                    .setFontSize(10f)
            )
        }

        document.add(Paragraph("\n\n"))
        document.add(
            Paragraph("با تشکر از خرید شما")
                .setFont(font)
                .setFontSize(11f)
                .setTextAlignment(TextAlignment.CENTER)
                .setItalic()
        )

        document.close()

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    private fun cell(text: String, font: com.itextpdf.kernel.font.PdfFont): Cell {
        return Cell().add(
            Paragraph(text)
                .setFont(font)
                .setFontSize(9f)
        )
    }

    private fun totalRow(label: String, value: String, font: com.itextpdf.kernel.font.PdfFont): Paragraph {
        return Paragraph("$label $value")
            .setFont(font)
            .setFontSize(11f)
            .setTextAlignment(TextAlignment.LEFT)
    }
}
