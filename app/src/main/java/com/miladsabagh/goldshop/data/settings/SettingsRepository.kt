package com.miladsabagh.goldshop.data.settings

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "goldshop_settings")

data class StoreSettings(
    val storeName: String = "جواهری من",
    val storePhone: String = "",
    val storeAddress: String = "",
    val defaultLaborFeePercent: Double = 7.0,
    val defaultProfitPercent: Double = 7.0,
    val defaultTaxPercent: Double = 9.0,
    val currentGoldPrice: Double = 0.0
)

class SettingsRepository(context: Context) {

    private val dataStore = context.dataStore

    private object Keys {
        val STORE_NAME = stringPreferencesKey("store_name")
        val STORE_PHONE = stringPreferencesKey("store_phone")
        val STORE_ADDRESS = stringPreferencesKey("store_address")
        val LABOR_FEE = doublePreferencesKey("default_labor_fee_percent")
        val PROFIT = doublePreferencesKey("default_profit_percent")
        val TAX = doublePreferencesKey("default_tax_percent")
        val GOLD_PRICE = doublePreferencesKey("current_gold_price")
    }

    val settingsFlow: Flow<StoreSettings> = dataStore.data.map { prefs ->
        StoreSettings(
            storeName = prefs[Keys.STORE_NAME] ?: "جواهری من",
            storePhone = prefs[Keys.STORE_PHONE] ?: "",
            storeAddress = prefs[Keys.STORE_ADDRESS] ?: "",
            defaultLaborFeePercent = prefs[Keys.LABOR_FEE] ?: 7.0,
            defaultProfitPercent = prefs[Keys.PROFIT] ?: 7.0,
            defaultTaxPercent = prefs[Keys.TAX] ?: 9.0,
            currentGoldPrice = prefs[Keys.GOLD_PRICE] ?: 0.0
        )
    }

    suspend fun updateStoreInfo(name: String, phone: String, address: String) {
        dataStore.edit { prefs ->
            prefs[Keys.STORE_NAME] = name
            prefs[Keys.STORE_PHONE] = phone
            prefs[Keys.STORE_ADDRESS] = address
        }
    }

    suspend fun updateDefaults(laborFeePercent: Double, profitPercent: Double, taxPercent: Double) {
        dataStore.edit { prefs ->
            prefs[Keys.LABOR_FEE] = laborFeePercent
            prefs[Keys.PROFIT] = profitPercent
            prefs[Keys.TAX] = taxPercent
        }
    }

    suspend fun updateGoldPrice(pricePerGram: Double) {
        dataStore.edit { prefs ->
            prefs[Keys.GOLD_PRICE] = pricePerGram
        }
    }
}
