package ir.zarrin.goldshop.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import ir.zarrin.goldshop.domain.Currency
import ir.zarrin.goldshop.util.groupDigits
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/** تنظیمات فروشگاه و مقادیر پیش‌فرض محاسبات */
data class AppSettings(
    val shopName: String = "گالری طلا و جواهر زرین",
    val ownerName: String = "",
    val phone: String = "",
    val address: String = "",
    val goldRate18: Long = 6_850_000L,
    val rateUpdatedAt: Long = 0L,
    val taxPercent: Double = 10.0,
    val defaultWagePercent: Double = 9.0,
    val defaultProfitPercent: Double = 7.0,
    val currency: Currency = Currency.TOMAN,
    val invoiceSequence: Int = 0,
    val darkTheme: Boolean = false
) {
    /** تبدیل مبلغ پایه (تومان) به واحد نمایش */
    fun display(amount: Long): Long = amount * currency.multiplier

    /** مبلغ با جداکننده سه‌رقمی و واحد پول، مثال: ۱۲٬۵۰۰٬۰۰۰ تومان */
    fun money(amount: Long): String = "${display(amount).groupDigits()} ${currency.label}"

    /** مبلغ بدون واحد پول */
    fun moneyPlain(amount: Long): String = display(amount).groupDigits()

    val currencyLabel: String get() = currency.label
}

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "zarrin_settings")

class SettingsRepository(private val context: Context) {

    private object Keys {
        val SHOP_NAME = stringPreferencesKey("shop_name")
        val OWNER_NAME = stringPreferencesKey("owner_name")
        val PHONE = stringPreferencesKey("phone")
        val ADDRESS = stringPreferencesKey("address")
        val GOLD_RATE_18 = longPreferencesKey("gold_rate_18")
        val RATE_UPDATED_AT = longPreferencesKey("rate_updated_at")
        val TAX_PERCENT = doublePreferencesKey("tax_percent")
        val WAGE_PERCENT = doublePreferencesKey("wage_percent")
        val PROFIT_PERCENT = doublePreferencesKey("profit_percent")
        val CURRENCY = stringPreferencesKey("currency")
        val INVOICE_SEQUENCE = intPreferencesKey("invoice_sequence")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        val defaults = AppSettings()
        AppSettings(
            shopName = prefs[Keys.SHOP_NAME] ?: defaults.shopName,
            ownerName = prefs[Keys.OWNER_NAME] ?: defaults.ownerName,
            phone = prefs[Keys.PHONE] ?: defaults.phone,
            address = prefs[Keys.ADDRESS] ?: defaults.address,
            goldRate18 = prefs[Keys.GOLD_RATE_18] ?: defaults.goldRate18,
            rateUpdatedAt = prefs[Keys.RATE_UPDATED_AT] ?: defaults.rateUpdatedAt,
            taxPercent = prefs[Keys.TAX_PERCENT] ?: defaults.taxPercent,
            defaultWagePercent = prefs[Keys.WAGE_PERCENT] ?: defaults.defaultWagePercent,
            defaultProfitPercent = prefs[Keys.PROFIT_PERCENT] ?: defaults.defaultProfitPercent,
            currency = Currency.fromName(prefs[Keys.CURRENCY]),
            invoiceSequence = prefs[Keys.INVOICE_SEQUENCE] ?: defaults.invoiceSequence,
            darkTheme = prefs[Keys.DARK_THEME] ?: defaults.darkTheme
        )
    }

    suspend fun current(): AppSettings = settings.first()

    suspend fun updateShopInfo(name: String, owner: String, phone: String, address: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SHOP_NAME] = name
            prefs[Keys.OWNER_NAME] = owner
            prefs[Keys.PHONE] = phone
            prefs[Keys.ADDRESS] = address
        }
    }

    suspend fun updateGoldRate(rate: Long) {
        context.dataStore.edit { prefs ->
            prefs[Keys.GOLD_RATE_18] = rate
            prefs[Keys.RATE_UPDATED_AT] = System.currentTimeMillis()
        }
    }

    suspend fun updateDefaults(tax: Double, wage: Double, profit: Double) {
        context.dataStore.edit { prefs ->
            prefs[Keys.TAX_PERCENT] = tax
            prefs[Keys.WAGE_PERCENT] = wage
            prefs[Keys.PROFIT_PERCENT] = profit
        }
    }

    suspend fun updateCurrency(currency: Currency) {
        context.dataStore.edit { prefs -> prefs[Keys.CURRENCY] = currency.name }
    }

    suspend fun updateDarkTheme(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.DARK_THEME] = enabled }
    }

    suspend fun nextInvoiceSequence(): Int {
        var next = 1
        context.dataStore.edit { prefs ->
            next = (prefs[Keys.INVOICE_SEQUENCE] ?: 0) + 1
            prefs[Keys.INVOICE_SEQUENCE] = next
        }
        return next
    }
}
