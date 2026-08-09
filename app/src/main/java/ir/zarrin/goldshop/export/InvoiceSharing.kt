package ir.zarrin.goldshop.export

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/** Builds the PDF once and then hands it to the share sheet or the system print dialog. */
object InvoiceSharing {

    private fun pdfFile(context: Context, document: InvoiceDocument): File {
        val directory = File(context.cacheDir, "invoices")
        directory.mkdirs()
        val file = File(directory, InvoiceDocument.fileName(document.invoice))
        return InvoicePdfWriter.write(context, document, file)
    }

    fun sharePdf(context: Context, document: InvoiceDocument) {
        val file = pdfFile(context, document)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "${document.title} ${document.numberLabel}")
            putExtra(Intent.EXTRA_TEXT, "${document.title} شماره ${document.numberLabel} — ${document.money(document.totals.payable)}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "ارسال فاکتور").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    fun shareText(context: Context, document: InvoiceDocument) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, document.toPlainText())
        }
        context.startActivity(Intent.createChooser(intent, "ارسال متن فاکتور").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    fun print(context: Context, document: InvoiceDocument) {
        val file = pdfFile(context, document)
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val jobName = "${document.title} ${document.numberLabel}"
        printManager.print(jobName, PdfFilePrintAdapter(file, jobName), null)
    }

    /** Streams an already rendered PDF to the printing framework. */
    private class PdfFilePrintAdapter(
        private val file: File,
        private val jobName: String
    ) : PrintDocumentAdapter() {

        override fun onLayout(
            oldAttributes: PrintAttributes?,
            newAttributes: PrintAttributes?,
            cancellationSignal: CancellationSignal?,
            callback: LayoutResultCallback,
            extras: Bundle?
        ) {
            if (cancellationSignal?.isCanceled == true) {
                callback.onLayoutCancelled()
                return
            }
            val info = PrintDocumentInfo.Builder(jobName)
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                .build()
            callback.onLayoutFinished(info, true)
        }

        override fun onWrite(
            pages: Array<out PageRange>?,
            destination: ParcelFileDescriptor,
            cancellationSignal: CancellationSignal?,
            callback: WriteResultCallback
        ) {
            try {
                FileInputStream(file).use { input ->
                    FileOutputStream(destination.fileDescriptor).use { output ->
                        input.copyTo(output)
                    }
                }
                callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
            } catch (error: Exception) {
                callback.onWriteFailed(error.message)
            }
        }
    }
}
