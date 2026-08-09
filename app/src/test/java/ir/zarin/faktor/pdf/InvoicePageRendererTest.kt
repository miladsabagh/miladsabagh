package ir.zarin.faktor.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.test.core.app.ApplicationProvider
import ir.zarin.faktor.TestSamples
import ir.zarin.faktor.data.model.InvoiceItem
import java.io.File
import java.io.FileOutputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * تست چیدمان صفحه فاکتور. صفحه روی یک بوم A4 رسم و به‌صورت PNG ذخیره می‌شود
 * تا خروجی واقعی فاکتور قابل بررسی باشد.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class InvoicePageRendererTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val renderer by lazy { InvoicePageRenderer(context) }
    private val outputDirectory = File("build/outputs/invoice-preview").apply { mkdirs() }

    @Test
    fun `صفحه فاکتور با محتوای قابل مشاهده رسم می شود`() {
        val bitmap = renderPage(pageIndex = 0, data = TestSamples.invoice())

        writePng(bitmap, "invoice_page_a4.png")
        assertTrue("صفحه فاکتور تقریباً خالی رسم شد", inkRatio(bitmap) > 0.01)
    }

    @Test
    fun `فاکتور با مانده پرداخت نشده رسم می شود`() {
        val bitmap = renderPage(pageIndex = 0, data = TestSamples.invoice(paidRatio = 0.4))

        writePng(bitmap, "invoice_page_a4_partially_paid.png")
        assertTrue(inkRatio(bitmap) > 0.01)
    }

    @Test
    fun `تعداد صفحات بر اساس تعداد اقلام محاسبه می شود`() {
        assertEquals(1, renderer.pageCount(0))
        assertEquals(1, renderer.pageCount(1))
        assertEquals(1, renderer.pageCount(10))
        assertEquals(2, renderer.pageCount(11))
        assertEquals(3, renderer.pageCount(25))
    }

    @Test
    fun `فاکتور با اقلام زیاد در چند صفحه رسم می شود`() {
        val base = TestSamples.invoice()
        val manyItems = (1..14).map { index ->
            base.items[index % base.items.size].copy(id = index.toLong(), name = "قلم شماره $index")
        }
        val data = base.copy(items = manyItems)

        assertEquals(2, renderer.pageCount(data.items.size))

        val secondPage = renderPage(pageIndex = 1, data = data)
        writePng(secondPage, "invoice_page_a4_second_page.png")
        assertTrue(inkRatio(secondPage) > 0.005)
    }

    @Test
    fun `فاکتور بدون اطلاعات فروشگاه هم رسم می شود`() {
        val data = TestSamples.invoice().let {
            it.copy(
                invoice = it.invoice.copy(customerName = "", customerPhone = "", note = ""),
                items = listOf(InvoiceItem(id = 1, name = "دستبند طلا", weightGrams = 3.0, lineTotalRial = 1_000_000)),
            )
        }

        val bitmap = renderPage(0, data, settings = TestSamples.settings.copy(shopName = "", shopPhone = "", shopAddress = ""))

        assertTrue(inkRatio(bitmap) > 0.005)
    }

    private fun renderPage(
        pageIndex: Int,
        data: ir.zarin.faktor.data.model.InvoiceWithItems,
        settings: ir.zarin.faktor.data.settings.AppSettings = TestSamples.settings,
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(
            InvoicePageRenderer.PAGE_WIDTH,
            InvoicePageRenderer.PAGE_HEIGHT,
            Bitmap.Config.ARGB_8888,
        )
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        renderer.render(canvas, pageIndex, data, settings)
        return bitmap
    }

    private fun writePng(bitmap: Bitmap, fileName: String) {
        FileOutputStream(File(outputDirectory, fileName)).use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        }
    }

    /** نسبت پیکسل‌های غیرسفید؛ برای اطمینان از اینکه صفحه واقعاً رسم شده است. */
    private fun inkRatio(bitmap: Bitmap): Double {
        var painted = 0
        var sampled = 0
        var y = 0
        while (y < bitmap.height) {
            var x = 0
            while (x < bitmap.width) {
                sampled++
                if (bitmap.getPixel(x, y) != Color.WHITE) painted++
                x += 2
            }
            y += 2
        }
        return painted.toDouble() / sampled
    }
}
