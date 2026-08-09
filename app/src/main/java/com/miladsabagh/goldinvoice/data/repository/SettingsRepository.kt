package com.miladsabagh.goldinvoice.data.repository

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "shop_settings")

data class ShopSettings(
    val shopName: String = "طلا و جواهر",
    val shopPhone: String = "",
    val shopAddress: String = "",
    val shopLicenseNumber: String = "",
    val goldPricePerGram18k: Double = 0.0,
    val defaultLaborFeePercent: Double = 7.0,
    val defaultProfitPercent: Double = 7.0,
    val defaultTaxPercent: Double = 9.0
)

class SettingsRepository(private val context: Context) {

    private object Keys {
        val SHOP_NAME = stringPreferencesKey("shop_name")
        val SHOP_PHONE = stringPreferencesKey("shop_phone")
        val SHOP_ADDRESS = stringPreferencesKey("shop_address")
        val SHOP_LICENSE = stringPreferencesKey("shop_license")
        val GOLD_PRICE = doublePreferencesKey("gold_price_per_gram_18k")
        val LABOR_FEE = doublePreferencesKey("default_labor_fee_percent")
        val PROFIT = doublePreferencesKey("default_profit_percent")
        val TAX = doublePreferencesKey("default_tax_percent")
    }

    val settingsFlow: Flow<ShopSettings> = context.dataStore.data.map { prefs ->
        val defaults = ShopSettings()
        ShopSettings(
            shopName = prefs[Keys.SHOP_NAME] ?: defaults.shopName,
            shopPhone = prefs[Keys.SHOP_PHONE] ?: defaults.shopPhone,
            shopAddress = prefs[Keys.SHOP_ADDRESS] ?: defaults.shopAddress,
            shopLicenseNumber = prefs[Keys.SHOP_LICENSE] ?: defaults.shopLicenseNumber,
            goldPricePerGram18k = prefs[Keys.GOLD_PRICE] ?: defaults.goldPricePerGram18k,
            defaultLaborFeePercent = prefs[Keys.LABOR_FEE] ?: defaults.defaultLaborFeePercent,
            defaultProfitPercent = prefs[Keys.PROFIT] ?: defaults.defaultProfitPercent,
            defaultTaxPercent = prefs[Keys.TAX] ?: defaults.defaultTaxPercent
        )
    }

    suspend fun updateShopInfo(
        shopName: String,
        shopPhone: String,
        shopAddress: String,
        shopLicenseNumber: String
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SHOP_NAME] = shopName
            prefs[Keys.SHOP_PHONE] = shopPhone
            prefs[Keys.SHOP_ADDRESS] = shopAddress
            prefs[Keys.SHOP_LICENSE] = shopLicenseNumber
        }
    }

    suspend fun updateGoldPrice(pricePerGram18k: Double) {
        context.dataStore.edit { prefs -> prefs[Keys.GOLD_PRICE] = pricePerGram18k }
    }

    suspend fun updatePricingDefaults(
        laborFeePercent: Double,
        profitPercent: Double,
        taxPercent: Double
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LABOR_FEE] = laborFeePercent
            prefs[Keys.PROFIT] = profitPercent
            prefs[Keys.TAX] = taxPercent
        }
    }
}
