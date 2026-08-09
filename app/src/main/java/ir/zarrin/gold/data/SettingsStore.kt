package ir.zarrin.gold.data

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** تنظیمات فروشگاه و پارامترهای قیمت‌گذاری. */
data class StoreSettings(
    val storeName: String = "طلا و جواهر زرین",
    val storePhone: String = "",
    val storeAddress: String = "",
    /** قیمت روز هر گرم طلای ۱۸ عیار (تومان) */
    val goldPricePerGram18k: Long = 0,
    /** نرخ مالیات بر ارزش افزوده (درصد) */
    val taxPercent: Double = 10.0,
    /** درصد سود پیش‌فرض فروشنده */
    val profitPercent: Double = 7.0,
)

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsStore(private val context: Context) {

    private object Keys {
        val storeName = stringPreferencesKey("store_name")
        val storePhone = stringPreferencesKey("store_phone")
        val storeAddress = stringPreferencesKey("store_address")
        val goldPrice = longPreferencesKey("gold_price_18k")
        val taxPercent = doublePreferencesKey("tax_percent")
        val profitPercent = doublePreferencesKey("profit_percent")
    }

    val settings: Flow<StoreSettings> = context.dataStore.data.map { prefs ->
        val defaults = StoreSettings()
        StoreSettings(
            storeName = prefs[Keys.storeName] ?: defaults.storeName,
            storePhone = prefs[Keys.storePhone] ?: defaults.storePhone,
            storeAddress = prefs[Keys.storeAddress] ?: defaults.storeAddress,
            goldPricePerGram18k = prefs[Keys.goldPrice] ?: defaults.goldPricePerGram18k,
            taxPercent = prefs[Keys.taxPercent] ?: defaults.taxPercent,
            profitPercent = prefs[Keys.profitPercent] ?: defaults.profitPercent,
        )
    }

    suspend fun update(settings: StoreSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.storeName] = settings.storeName
            prefs[Keys.storePhone] = settings.storePhone
            prefs[Keys.storeAddress] = settings.storeAddress
            prefs[Keys.goldPrice] = settings.goldPricePerGram18k
            prefs[Keys.taxPercent] = settings.taxPercent
            prefs[Keys.profitPercent] = settings.profitPercent
        }
    }

    suspend fun setGoldPrice(price: Long) {
        context.dataStore.edit { prefs -> prefs[Keys.goldPrice] = price }
    }
}
