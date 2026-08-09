package com.goldjewelry.app.data.repository

import com.goldjewelry.app.data.database.SettingsDao
import com.goldjewelry.app.data.model.ShopSettings
import kotlinx.coroutines.flow.Flow

class SettingsRepository(private val settingsDao: SettingsDao) {

    fun getSettings(): Flow<ShopSettings?> = settingsDao.getSettings()

    suspend fun getSettingsOnce(): ShopSettings =
        settingsDao.getSettingsOnce() ?: ShopSettings()

    suspend fun update(settings: ShopSettings) = settingsDao.update(settings)

    suspend fun incrementInvoiceNumber(): ShopSettings {
        val current = getSettingsOnce()
        val updated = current.copy(lastInvoiceNumber = current.lastInvoiceNumber + 1)
        settingsDao.update(updated)
        return updated
    }
}
