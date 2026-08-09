package com.miladsabagh.goldshop

import android.app.Application
import com.miladsabagh.goldshop.data.local.AppDatabase
import com.miladsabagh.goldshop.data.repository.GoldShopRepository
import com.miladsabagh.goldshop.data.settings.SettingsRepository

/**
 * Application-wide singletons: database, repository and settings.
 * Kept simple (no DI framework) so the project builds without extra setup.
 */
class GoldShopApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    val settingsRepository: SettingsRepository by lazy { SettingsRepository(this) }

    val repository: GoldShopRepository by lazy {
        GoldShopRepository(
            productDao = database.productDao(),
            customerDao = database.customerDao(),
            invoiceDao = database.invoiceDao(),
            goldPriceDao = database.goldPriceDao()
        )
    }
}
