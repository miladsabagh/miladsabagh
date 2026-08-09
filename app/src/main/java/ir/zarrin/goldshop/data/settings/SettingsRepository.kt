package ir.zarrin.goldshop.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import ir.zarrin.goldshop.domain.model.Currency
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Shop profile plus the pricing defaults applied to every new invoice. */
data class ShopSettings(
    val shopName: String = "گالری طلا و جواهر زرّین",
    val ownerName: String = "",
    val phone: String = "",
    val address: String = "",
    val goldRate18: Long = 3_500_000L,
    val taxPercent: Double = 10.0,
    val defaultWagePercent: Double = 7.0,
    val defaultProfitPercent: Double = 7.0,
    val currency: Currency = Currency.TOMAN,
    val invoiceFooter: String = "کالای فروخته‌شده با ارائه‌ی همین فاکتور و طبق ضوابط اتحادیه قابل بازگشت است."
)

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "shop_settings")

class SettingsRepository(private val context: Context) {

    private object Keys {
        val shopName = stringPreferencesKey("shop_name")
        val ownerName = stringPreferencesKey("owner_name")
        val phone = stringPreferencesKey("phone")
        val address = stringPreferencesKey("address")
        val goldRate18 = longPreferencesKey("gold_rate_18")
        val taxPercent = doublePreferencesKey("tax_percent")
        val wagePercent = doublePreferencesKey("wage_percent")
        val profitPercent = doublePreferencesKey("profit_percent")
        val currency = stringPreferencesKey("currency")
        val invoiceFooter = stringPreferencesKey("invoice_footer")
    }

    val settings: Flow<ShopSettings> = context.settingsDataStore.data.map { preferences ->
        val defaults = ShopSettings()
        ShopSettings(
            shopName = preferences[Keys.shopName] ?: defaults.shopName,
            ownerName = preferences[Keys.ownerName] ?: defaults.ownerName,
            phone = preferences[Keys.phone] ?: defaults.phone,
            address = preferences[Keys.address] ?: defaults.address,
            goldRate18 = preferences[Keys.goldRate18] ?: defaults.goldRate18,
            taxPercent = preferences[Keys.taxPercent] ?: defaults.taxPercent,
            defaultWagePercent = preferences[Keys.wagePercent] ?: defaults.defaultWagePercent,
            defaultProfitPercent = preferences[Keys.profitPercent] ?: defaults.defaultProfitPercent,
            currency = Currency.fromName(preferences[Keys.currency]),
            invoiceFooter = preferences[Keys.invoiceFooter] ?: defaults.invoiceFooter
        )
    }

    suspend fun update(settings: ShopSettings) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.shopName] = settings.shopName
            preferences[Keys.ownerName] = settings.ownerName
            preferences[Keys.phone] = settings.phone
            preferences[Keys.address] = settings.address
            preferences[Keys.goldRate18] = settings.goldRate18
            preferences[Keys.taxPercent] = settings.taxPercent
            preferences[Keys.wagePercent] = settings.defaultWagePercent
            preferences[Keys.profitPercent] = settings.defaultProfitPercent
            preferences[Keys.currency] = settings.currency.name
            preferences[Keys.invoiceFooter] = settings.invoiceFooter
        }
    }

    suspend fun updateGoldRate(rate: Long) {
        context.settingsDataStore.edit { preferences -> preferences[Keys.goldRate18] = rate }
    }
}
