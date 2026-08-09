package com.zarnegar.gold

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.View
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * برنامه را واقعاً اجرا می‌کند، یک فروش کامل را از انتخاب کالا تا صدور فاکتور طی می‌کند
 * و از هر مرحله تصویر می‌گیرد. خروجی‌ها در `app/build/screenshots/` ذخیره می‌شوند.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-xhdpi")
class AppScreenshotTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private val outputDir = File("build/screenshots").apply { mkdirs() }

    private fun capture(name: String) {
        composeRule.waitForIdle()
        val view = composeRule.activity.window.decorView
        if (view.width == 0 || view.height == 0) {
            view.measure(
                View.MeasureSpec.makeMeasureSpec(822, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(1782, View.MeasureSpec.EXACTLY),
            )
            view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        }
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        view.draw(canvas)

        File(outputDir, "$name.png").outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }

        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        assertTrue("تصویر «$name» خالی است", pixels.toSet().size > 8)
    }

    private fun waitForText(text: String, substring: Boolean = false) {
        composeRule.waitUntil(timeoutMillis = 30_000) {
            composeRule.onAllNodesWithText(text, substring = substring)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitUntilTextGone(text: String) {
        composeRule.waitUntil(timeoutMillis = 30_000) {
            composeRule.onAllNodesWithText(text, substring = true)
                .fetchSemanticsNodes().isEmpty()
        }
    }

    private fun back() {
        composeRule.onAllNodesWithContentDescription("بازگشت").onFirst().performClick()
        composeRule.waitForIdle()
    }

    /** فهرست تنبل فقط بخش دیده‌شده را می‌سازد، پس باید تا رسیدن به متن پیمایش کرد. */
    private fun scrollTo(text: String) {
        composeRule.onAllNodes(hasScrollAction()).onFirst()
            .performScrollToNode(hasText(text, substring = true))
        composeRule.waitForIdle()
    }

    @Test
    fun `walks through a full sale and captures every screen`() {
        // ۱) داشبورد پس از بارگذاری دادهٔ نمونه
        waitForText("دسترسی سریع")
        waitForText("نرخ هر گرم طلای ۱۸ عیار")
        capture("01_dashboard")

        // ۲) فهرست کالاها با قیمت لحظه‌ای
        composeRule.onNodeWithText("کالاها").performClick()
        waitForText("انگشتر طرح ونکلیف")
        capture("02_products")

        // ۳) ریز محاسبهٔ قیمت یک کالا
        composeRule.onNodeWithText("انگشتر طرح ونکلیف").performClick()
        waitForText("مشخصات کالا")
        scrollTo("پیش‌نمایش قیمت با نرخ روز")
        capture("03_product_pricing")
        back()
        waitForText("انگشتر طرح ونکلیف")

        // ۴) افزودن دو کالا به فاکتور
        composeRule.onAllNodesWithText("افزودن", substring = true).onFirst().performClick()
        composeRule.waitForIdle()
        composeRule.onAllNodesWithText("افزودن", substring = true)[1].performClick()
        composeRule.waitForIdle()

        // ۵) صفحهٔ صدور فاکتور با سبد پر (پس از محو شدن پیام «افزوده شد»)
        composeRule.onNodeWithText("فروش").performClick()
        waitForText("اقلام فاکتور")
        waitUntilTextGone("به فاکتور اضافه شد")
        capture("04_new_sale")
        scrollTo("جمع‌بندی فاکتور")
        capture("05_sale_totals")

        // ۶) ثبت فاکتور و مشاهدهٔ جزئیات
        composeRule.onNodeWithText("ثبت و صدور فاکتور").performClick()
        waitForText("اشتراک PDF", substring = true)
        waitForText("شمارهٔ فاکتور")
        capture("06_invoice_detail")
        scrollTo("مبلغ قابل پرداخت")
        capture("07_invoice_totals")

        // ۷) فهرست فاکتورها
        back()
        composeRule.onNodeWithText("فاکتورها").performClick()
        waitForText("جمع فروش")
        capture("08_invoices")

        // ۸) تنظیمات فروشگاه
        composeRule.onNodeWithText("خانه").performClick()
        waitForText("دسترسی سریع")
        composeRule.onAllNodesWithContentDescription("تنظیمات").onFirst().performClick()
        waitForText("اطلاعات فروشگاه (سربرگ فاکتور)")
        capture("09_settings")

        // ۹) مشتریان
        back()
        composeRule.onNodeWithText("مشتریان").performClick()
        waitForText("مریم احمدی")
        capture("10_customers")

        val saved = outputDir.listFiles()?.count { it.name.endsWith(".png") } ?: 0
        assertTrue("انتظار حداقل ۱۰ تصویر می‌رفت، اما $saved تصویر ذخیره شد", saved >= 10)
    }
}
