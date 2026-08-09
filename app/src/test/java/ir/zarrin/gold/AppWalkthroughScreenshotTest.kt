package ir.zarrin.gold

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.core.app.ApplicationProvider
import ir.zarrin.gold.data.Customer
import ir.zarrin.gold.data.Product
import ir.zarrin.gold.data.StoreSettings
import ir.zarrin.gold.pdf.InvoicePdfGenerator
import ir.zarrin.gold.ui.AppRoot
import ir.zarrin.gold.ui.theme.ZarrinGoldTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.io.FileOutputStream

/**
 * واک‌ثروی کامل اپ روی JVM با Robolectric (رندر گرافیکی واقعی):
 * داشبورد → محصولات → مشتریان → فاکتور جدید → افزودن قلم → صدور فاکتور →
 * جزئیات فاکتور → تولید PDF. از هر مرحله اسکرین‌شات ذخیره می‌شود.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(application = GoldApp::class, sdk = [34], qualifiers = "w411dp-h891dp-420dpi")
class AppWalkthroughScreenshotTest {

    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private val outDir = File(
        System.getProperty("screenshot.dir") ?: "build/reports/screenshots"
    ).apply { mkdirs() }

    private fun clickTab(label: String) {
        compose.onNode(
            hasText(label) and
                SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)
        ).performClick()
        compose.waitForIdle()
    }

    private fun capture(name: String) {
        compose.waitForIdle()
        val view = compose.activity.window.decorView
        val bitmap = Bitmap.createBitmap(
            view.width.coerceAtLeast(1),
            view.height.coerceAtLeast(1),
            Bitmap.Config.ARGB_8888,
        )
        view.draw(Canvas(bitmap))
        FileOutputStream(File(outDir, "$name.png")).use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun fullInvoiceWalkthrough() {
        val app = ApplicationProvider.getApplicationContext<GoldApp>()

        runBlocking {
            app.settingsStore.update(
                StoreSettings(
                    storeName = "طلا و جواهر زرین",
                    storePhone = "021-55667788",
                    storeAddress = "تهران، بازار بزرگ، راسته طلافروش‌ها",
                    goldPricePerGram18k = 5_200_000,
                    taxPercent = 10.0,
                    profitPercent = 7.0,
                )
            )
            app.database.productDao().insert(
                Product(name = "انگشتر سولیتر", category = "انگشتر", karat = 18,
                    weightGrams = 4.5, wagePercent = 12.0, stock = 3)
            )
            app.database.productDao().insert(
                Product(name = "گردنبند فلامینگو", category = "گردنبند", karat = 18,
                    weightGrams = 8.2, wagePercent = 15.0, stock = 2)
            )
            app.database.productDao().insert(
                Product(name = "سکه تمام بهار آزادی", category = "سکه", karat = 22,
                    weightGrams = 8.133, wagePercent = 2.0, stock = 10)
            )
            app.database.customerDao().insert(
                Customer(name = "سارا محمدی", phone = "09123456789", address = "تهران، سعادت‌آباد")
            )
        }

        compose.setContent {
            ZarrinGoldTheme(darkTheme = false) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    AppRoot()
                }
            }
        }

        // ۱) داشبورد با قیمت روز طلا و آمار
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText("قیمت روز طلای ۱۸ عیار (هر گرم)")
                .fetchSemanticsNodes().isNotEmpty()
        }
        capture("01_dashboard")

        // ۲) فهرست محصولات با قیمت محاسبه‌شده روز
        clickTab("محصولات")
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText("انگشتر سولیتر").fetchSemanticsNodes().isNotEmpty()
        }
        capture("02_products")

        // ۳) مشتریان
        clickTab("مشتریان")
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText("سارا محمدی").fetchSemanticsNodes().isNotEmpty()
        }
        capture("03_customers")

        // ۴) صفحه فاکتور جدید
        clickTab("فاکتورها")
        compose.onNodeWithText("فاکتور جدید", useUnmergedTree = true).performClick()
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText("اقلام فاکتور").fetchSemanticsNodes().isNotEmpty()
        }

        // انتخاب خریدار از دفترچه مشتریان
        compose.onNodeWithText("مشتری متفرقه (برای انتخاب بزنید)").performClick()
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText("انتخاب").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onAllNodesWithText("انتخاب").onLast().performClick()
        compose.waitForIdle()

        // افزودن قلم از محصولات
        compose.onNodeWithText("از محصولات", substring = true).performClick()
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText("افزودن").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onAllNodesWithText("افزودن").onLast().performClick() // انتخاب محصول از شیت
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText("تعداد: ۱").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("+").performClick() // افزایش تعداد به ۲
        compose.onNodeWithText("افزودن").performClick() // تأیید
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText("جمع بهای طلا").fetchSemanticsNodes().isNotEmpty()
        }
        capture("04_new_invoice_with_item")

        // ۵) صدور فاکتور
        compose.onNodeWithText("صدور فاکتور", substring = true).performClick()
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText("مبنای محاسبه (گرم ۱۸ عیار)")
                .fetchSemanticsNodes().isNotEmpty()
        }
        capture("05_invoice_detail")

        // ۶) رندر سند فاکتور (همان کدی که صفحه PDF را ترسیم می‌کند) روی Bitmap.
        //    خود PdfDocument فقط روی دستگاه واقعی در دسترس است و در Robolectric
        //    پشتیبانی نمی‌شود؛ منطق کامل ترسیم اینجا تست می‌شود.
        val data = runBlocking { app.database.invoiceDao().observeAll().first().first() }
        val settings = runBlocking { app.settingsStore.settings.first() }
        assertTrue("فاکتور باید حداقل یک قلم داشته باشد", data.items.isNotEmpty())
        assertTrue("مبلغ فاکتور باید بزرگ‌تر از صفر باشد", data.invoice.total > 0)
        // مجموع موجودی اولیه ۳+۲+۱۰=۱۵ بود؛ با فروش ۲ عدد باید ۱۳ شود.
        val totalStock = runBlocking {
            app.database.productDao().observeAll().first().sumOf { it.stock }
        }
        assertTrue("موجودی باید ۲ واحد کم شده باشد (فعلی: $totalStock)", totalStock == 13)

        val scale = 2
        val docBitmap = Bitmap.createBitmap(
            InvoicePdfGenerator.PAGE_WIDTH * scale,
            InvoicePdfGenerator.PAGE_HEIGHT * scale,
            Bitmap.Config.ARGB_8888,
        )
        Canvas(docBitmap).apply {
            drawColor(android.graphics.Color.WHITE)
            scale(scale.toFloat(), scale.toFloat())
            InvoicePdfGenerator.drawInvoice(app, this, data, settings)
        }
        FileOutputStream(File(outDir, "06_invoice_pdf_document.png")).use {
            docBitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }
}
