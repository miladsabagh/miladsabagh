package ir.zarin.faktor.pdf

import android.content.Context
import android.graphics.pdf.PdfDocument
import ir.zarin.faktor.data.model.InvoiceWithItems
import ir.zarin.faktor.data.settings.AppSettings
import java.io.File
import java.io.FileOutputStream

/** تبدیل فاکتور به یک فایل PDF چندصفحه‌ای در قطع A4. */
class InvoicePdfGenerator(context: Context) {

    private val renderer = InvoicePageRenderer(context)

    fun generate(data: InvoiceWithItems, settings: AppSettings, outputDirectory: File): File {
        val document = PdfDocument()
        try {
            repeat(renderer.pageCount(data.items.size)) { pageIndex ->
                val pageInfo = PdfDocument.PageInfo
                    .Builder(InvoicePageRenderer.PAGE_WIDTH, InvoicePageRenderer.PAGE_HEIGHT, pageIndex + 1)
                    .create()
                val page = document.startPage(pageInfo)
                renderer.render(page.canvas, pageIndex, data, settings)
                document.finishPage(page)
            }

            if (!outputDirectory.exists()) outputDirectory.mkdirs()
            val file = File(outputDirectory, "faktor-${data.invoice.number}.pdf")
            FileOutputStream(file).use { document.writeTo(it) }
            return file
        } finally {
            document.close()
        }
    }
}
