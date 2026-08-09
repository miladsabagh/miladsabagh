package com.zarnegar.gold.pdf

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
import com.zarnegar.gold.domain.model.Invoice
import com.zarnegar.gold.domain.model.ShopSettings
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/** ساخت PDF فاکتور و اشتراک‌گذاری آن با سایر برنامه‌ها (واتساپ، ایمیل، …) */
object InvoiceSharing {

    fun share(context: Context, invoice: Invoice, settings: ShopSettings) {
        val file = InvoicePdfGenerator(context).generateFile(invoice, settings)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(
                Intent.EXTRA_SUBJECT,
                "فاکتور ${invoice.number} - ${settings.shopName}",
            )
            putExtra(
                Intent.EXTRA_TEXT,
                "فاکتور خرید شما از ${settings.shopName} پیوست شده است.",
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            Intent.createChooser(intent, "ارسال فاکتور").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            },
        )
    }

    fun print(context: Context, invoice: Invoice, settings: ShopSettings) {
        val file = InvoicePdfGenerator(context).generateFile(invoice, settings)
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        printManager.print(
            "فاکتور ${invoice.number}",
            PdfFileAdapter(file, "invoice-${invoice.number}.pdf"),
            PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                .build(),
        )
    }
}

/** آداپتور چاپ که یک فایل PDF آمادهٔ روی دیسک را به سامانهٔ چاپ تحویل می‌دهد. */
private class PdfFileAdapter(
    private val file: File,
    private val documentName: String,
) : PrintDocumentAdapter() {

    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes?,
        cancellationSignal: CancellationSignal?,
        callback: LayoutResultCallback,
        extras: Bundle?,
    ) {
        if (cancellationSignal?.isCanceled == true) {
            callback.onLayoutCancelled()
            return
        }
        val info = PrintDocumentInfo.Builder(documentName)
            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
            .build()
        callback.onLayoutFinished(info, true)
    }

    override fun onWrite(
        pages: Array<out PageRange>?,
        destination: ParcelFileDescriptor,
        cancellationSignal: CancellationSignal?,
        callback: WriteResultCallback,
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
