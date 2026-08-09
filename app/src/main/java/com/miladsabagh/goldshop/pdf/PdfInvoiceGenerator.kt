package com.miladsabagh.goldshop.pdf

import android.content.Context
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

/** Generates a shareable PDF file for an invoice, sized like ISO A4 (in PDF points, 72dpi). */
object PdfInvoiceGenerator {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842

    fun generateFile(context: Context, content: InvoiceContent): File {
        val document = PdfDocument()
        InvoicePdfRenderer.renderToDocument(document, PAGE_WIDTH, PAGE_HEIGHT, content)

        val dir = File(context.cacheDir, "invoices").apply { mkdirs() }
        val file = File(dir, "invoice_${content.invoice.invoiceNumber}.pdf")
        FileOutputStream(file).use { out -> document.writeTo(out) }
        document.close()
        return file
    }

    fun getShareUri(context: Context, file: File) =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}
