package ir.goldshop.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * تنظیمات فروشگاه به صورت یک رکورد ثابت (تک‌سطری) نگهداری می‌شود.
 */
@Entity(tableName = "shop_settings")
data class ShopSettings(
    @PrimaryKey
    val id: Int = SINGLE_ROW_ID,
    val shopName: String = "فروشگاه طلا و جواهر",
    val shopAddress: String = "",
    val shopPhone: String = "",
    val economicCode: String = "",
    val nationalId: String = "",
    val goldPricePerGram: Double = 0.0,
    val defaultLaborPercent: Double = 7.0,
    val defaultProfitPercent: Double = 7.0,
    val defaultTaxPercent: Double = 9.0,
    val applyTaxOnLaborAndProfitOnly: Boolean = true,
    val nextInvoiceNumber: Long = 1001L,
    val invoiceFooterNote: String = "با تشکر از خرید شما"
) {
    companion object {
        const val SINGLE_ROW_ID = 1
    }
}
