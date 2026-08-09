package com.zarnegar.gold.data.repo

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.zarnegar.gold.domain.model.ShopSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore("shop_settings")

class SettingsRepository(private val context: Context) {

    private object Keys {
        val shopName = stringPreferencesKey("shop_name")
        val ownerName = stringPreferencesKey("owner_name")
        val phone = stringPreferencesKey("phone")
        val address = stringPreferencesKey("address")
        val economicCode = stringPreferencesKey("economic_code")
        val cardNumber = stringPreferencesKey("card_number")
        val currencyLabel = stringPreferencesKey("currency_label")
        val goldRate = longPreferencesKey("gold_rate_18k")
        val vatPercent = doublePreferencesKey("vat_percent")
        val vatOnStone = booleanPreferencesKey("vat_on_stone")
        val roundTo = longPreferencesKey("round_to")
        val defaultWage = doublePreferencesKey("default_wage_percent")
        val defaultProfit = doublePreferencesKey("default_profit_percent")
        val footerNote = stringPreferencesKey("invoice_footer_note")
    }

    val settings: Flow<ShopSettings> = context.settingsDataStore.data.map { prefs ->
        val defaults = ShopSettings()
        ShopSettings(
            shopName = prefs[Keys.shopName] ?: defaults.shopName,
            ownerName = prefs[Keys.ownerName] ?: defaults.ownerName,
            phone = prefs[Keys.phone] ?: defaults.phone,
            address = prefs[Keys.address] ?: defaults.address,
            economicCode = prefs[Keys.economicCode] ?: defaults.economicCode,
            cardNumber = prefs[Keys.cardNumber] ?: defaults.cardNumber,
            currencyLabel = prefs[Keys.currencyLabel] ?: defaults.currencyLabel,
            goldRatePerGram18k = prefs[Keys.goldRate] ?: defaults.goldRatePerGram18k,
            vatPercent = prefs[Keys.vatPercent] ?: defaults.vatPercent,
            vatOnStone = prefs[Keys.vatOnStone] ?: defaults.vatOnStone,
            roundTo = prefs[Keys.roundTo] ?: defaults.roundTo,
            defaultWagePercent = prefs[Keys.defaultWage] ?: defaults.defaultWagePercent,
            defaultProfitPercent = prefs[Keys.defaultProfit] ?: defaults.defaultProfitPercent,
            invoiceFooterNote = prefs[Keys.footerNote] ?: defaults.invoiceFooterNote,
        )
    }

    suspend fun save(settings: ShopSettings) {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.shopName] = settings.shopName
            prefs[Keys.ownerName] = settings.ownerName
            prefs[Keys.phone] = settings.phone
            prefs[Keys.address] = settings.address
            prefs[Keys.economicCode] = settings.economicCode
            prefs[Keys.cardNumber] = settings.cardNumber
            prefs[Keys.currencyLabel] = settings.currencyLabel
            prefs[Keys.goldRate] = settings.goldRatePerGram18k
            prefs[Keys.vatPercent] = settings.vatPercent
            prefs[Keys.vatOnStone] = settings.vatOnStone
            prefs[Keys.roundTo] = settings.roundTo
            prefs[Keys.defaultWage] = settings.defaultWagePercent
            prefs[Keys.defaultProfit] = settings.defaultProfitPercent
            prefs[Keys.footerNote] = settings.invoiceFooterNote
        }
    }

    suspend fun updateGoldRate(rate: Long) {
        context.settingsDataStore.edit { it[Keys.goldRate] = rate }
    }
}
