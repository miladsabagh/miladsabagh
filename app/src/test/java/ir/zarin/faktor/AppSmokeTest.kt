package ir.zarin.faktor

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * برنامه واقعی را با اکتیویتی، ناوبری، پایگاه داده Room و تنظیمات DataStore بالا
 * می‌آورد تا از سالم بودن اتصال همه بخش‌ها مطمئن شویم.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-xhdpi")
class AppSmokeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `داشبورد با نرخ روز و دسترسی سریع بالا می آید`() {
        composeRule.onNodeWithText("نرخ هر گرم طلای ۱۸ عیار").assertIsDisplayed()
        composeRule.onNodeWithText("دسترسی سریع").assertIsDisplayed()
        composeRule.onNodeWithText("فروش امروز").assertIsDisplayed()
        composeRule.onNodeWithText("آخرین فاکتورها").assertIsDisplayed()
    }

    @Test
    fun `زبانه کالاها فهرست خالی انبار را باز می کند`() {
        clickTab("کالاها")

        composeRule.onNodeWithText("هنوز کالایی ثبت نشده است").assertIsDisplayed()
        composeRule.onNodeWithText("جستجوی نام یا کد کالا…").assertIsDisplayed()
    }

    @Test
    fun `زبانه فاکتورها فهرست خالی فاکتورها را باز می کند`() {
        clickTab("فاکتورها")

        composeRule.onNodeWithText("هنوز فاکتوری صادر نشده است").assertIsDisplayed()
    }

    @Test
    fun `زبانه تنظیمات فرم فروشگاه را باز می کند`() {
        clickTab("تنظیمات")

        composeRule.onNodeWithText("اطلاعات فروشگاه").assertIsDisplayed()
        composeRule.onNodeWithText("نام فروشگاه").assertIsDisplayed()
        composeRule.onNodeWithText("تنظیمات قیمت‌گذاری").assertIsDisplayed()
    }

    @Test
    fun `دکمه فاکتور جدید صفحه صدور فاکتور را باز می کند`() {
        composeRule.onNodeWithContentDescription("فاکتور جدید").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("فاکتور فروش جدید").assertIsDisplayed()
        composeRule.onNodeWithText("هنوز قلمی به فاکتور اضافه نشده است").assertIsDisplayed()
        composeRule.onNodeWithText("مشتری متفرقه").assertIsDisplayed()
        composeRule.onNodeWithText("ثبت و صدور فاکتور").assertIsDisplayed()

        composeRule.onRoot().captureRoboImage("build/outputs/screenshots/09_running_app_new_sale.png")
    }

    @Test
    fun `اطلاعات فروشگاه و نرخ روز ذخیره و در داشبورد نمایش داده می شود`() {
        clickTab("تنظیمات")

        composeRule.onNode(hasText("نام فروشگاه") and hasSetTextAction())
            .performTextInput("گالری طلا و جواهر زرین")
        composeRule.onNode(hasText("نرخ هر گرم طلای ۱۸ عیار") and hasSetTextAction())
            .performTextInput("9200000")
        composeRule.onNodeWithText("ذخیره").performScrollTo().performClick()

        clickTab("داشبورد")
        awaitText("۹,۲۰۰,۰۰۰")

        composeRule.onNodeWithText("گالری طلا و جواهر زرین").assertIsDisplayed()
        composeRule.onNodeWithText("۹,۲۰۰,۰۰۰").assertIsDisplayed()
        composeRule.onRoot().captureRoboImage("build/outputs/screenshots/10_running_app_dashboard.png")
    }

    /** روی زبانه نوار پایین (و نه برچسب‌های هم‌نام دیگر) کلیک می‌کند. */
    private fun clickTab(label: String) {
        composeRule.onAllNodes(hasText(label) and hasClickAction()).onFirst().performClick()
        composeRule.waitForIdle()
    }

    /**
     * DataStore روی نخ واقعی می‌نویسد، بنابراین انتظار باید با زمان واقعی انجام شود
     * نه ساعت مجازی تست.
     */
    private fun awaitText(text: String) {
        val deadline = System.currentTimeMillis() + TIMEOUT_MILLIS
        while (System.currentTimeMillis() < deadline) {
            composeRule.waitForIdle()
            if (composeRule.onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty()) return
            Thread.sleep(POLL_INTERVAL_MILLIS)
        }
        throw AssertionError("متن «$text» در زمان تعیین‌شده نمایش داده نشد")
    }

    private companion object {
        const val TIMEOUT_MILLIS = 10_000L
        const val POLL_INTERVAL_MILLIS = 50L
    }
}
