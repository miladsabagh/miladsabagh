package com.miladsabagh.zarrin.data

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "zarrin_settings")

data class ShopSettings(
    val goldPricePerGram18k: Long = 3_850_000,
    val profitPercent: Double = 7.0,
    val taxPercent: Double = 9.0,
    val storeName: String = "گالری طلا و جواهر زرین",
    val storePhone: String = "021-12345678",
    val storeAddress: String = "تهران، بازار بزرگ، پاساژ طلای زرین، پلاک ۱۲"
)

class SettingsStore(private val context: Context) {

    private object Keys {
        val GOLD_PRICE = longPreferencesKey("gold_price_per_gram_18k")
        val PROFIT_PERCENT = doublePreferencesKey("profit_percent")
        val TAX_PERCENT = doublePreferencesKey("tax_percent")
        val STORE_NAME = stringPreferencesKey("store_name")
        val STORE_PHONE = stringPreferencesKey("store_phone")
        val STORE_ADDRESS = stringPreferencesKey("store_address")
    }

    val settings: Flow<ShopSettings> = context.dataStore.data.map { prefs ->
        val defaults = ShopSettings()
        ShopSettings(
            goldPricePerGram18k = prefs[Keys.GOLD_PRICE] ?: defaults.goldPricePerGram18k,
            profitPercent = prefs[Keys.PROFIT_PERCENT] ?: defaults.profitPercent,
            taxPercent = prefs[Keys.TAX_PERCENT] ?: defaults.taxPercent,
            storeName = prefs[Keys.STORE_NAME] ?: defaults.storeName,
            storePhone = prefs[Keys.STORE_PHONE] ?: defaults.storePhone,
            storeAddress = prefs[Keys.STORE_ADDRESS] ?: defaults.storeAddress
        )
    }

    suspend fun setGoldPrice(value: Long) {
        context.dataStore.edit { it[Keys.GOLD_PRICE] = value }
    }

    suspend fun setProfitPercent(value: Double) {
        context.dataStore.edit { it[Keys.PROFIT_PERCENT] = value }
    }

    suspend fun setTaxPercent(value: Double) {
        context.dataStore.edit { it[Keys.TAX_PERCENT] = value }
    }

    suspend fun setStoreInfo(name: String, phone: String, address: String) {
        context.dataStore.edit {
            it[Keys.STORE_NAME] = name
            it[Keys.STORE_PHONE] = phone
            it[Keys.STORE_ADDRESS] = address
        }
    }
}
