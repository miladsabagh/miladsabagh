package ir.zarin.faktor.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import ir.zarin.faktor.core.CurrencyUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    private object Keys {
        val SHOP_NAME = stringPreferencesKey("shop_name")
        val SHOP_PHONE = stringPreferencesKey("shop_phone")
        val SHOP_ADDRESS = stringPreferencesKey("shop_address")
        val GOLD_RATE = longPreferencesKey("gold_rate_rial")
        val PROFIT_PERCENT = doublePreferencesKey("profit_percent")
        val VAT_PERCENT = doublePreferencesKey("vat_percent")
        val CURRENCY_UNIT = stringPreferencesKey("currency_unit")
        val INVOICE_PREFIX = stringPreferencesKey("invoice_prefix")
        val PERSIAN_DIGITS = booleanPreferencesKey("persian_digits")
    }

    val settings: Flow<AppSettings> = context.settingsDataStore.data.map { preferences ->
        AppSettings(
            shopName = preferences[Keys.SHOP_NAME].orEmpty(),
            shopPhone = preferences[Keys.SHOP_PHONE].orEmpty(),
            shopAddress = preferences[Keys.SHOP_ADDRESS].orEmpty(),
            goldRatePerGramRial = preferences[Keys.GOLD_RATE] ?: 0L,
            profitPercent = preferences[Keys.PROFIT_PERCENT] ?: AppSettings.DEFAULT_PROFIT_PERCENT,
            vatPercent = preferences[Keys.VAT_PERCENT] ?: AppSettings.DEFAULT_VAT_PERCENT,
            currencyUnit = CurrencyUnit.fromNameOrDefault(preferences[Keys.CURRENCY_UNIT]),
            invoicePrefix = preferences[Keys.INVOICE_PREFIX].orEmpty(),
            persianDigits = preferences[Keys.PERSIAN_DIGITS] ?: true,
        )
    }

    suspend fun update(settings: AppSettings) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.SHOP_NAME] = settings.shopName
            preferences[Keys.SHOP_PHONE] = settings.shopPhone
            preferences[Keys.SHOP_ADDRESS] = settings.shopAddress
            preferences[Keys.GOLD_RATE] = settings.goldRatePerGramRial
            preferences[Keys.PROFIT_PERCENT] = settings.profitPercent
            preferences[Keys.VAT_PERCENT] = settings.vatPercent
            preferences[Keys.CURRENCY_UNIT] = settings.currencyUnit.name
            preferences[Keys.INVOICE_PREFIX] = settings.invoicePrefix
            preferences[Keys.PERSIAN_DIGITS] = settings.persianDigits
        }
    }

    suspend fun updateGoldRate(rateRial: Long) {
        context.settingsDataStore.edit { preferences -> preferences[Keys.GOLD_RATE] = rateRial }
    }
}
