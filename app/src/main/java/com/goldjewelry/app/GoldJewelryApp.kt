package com.goldjewelry.app

import android.app.Application
import com.goldjewelry.app.data.database.AppDatabase
import com.goldjewelry.app.data.repository.CustomerRepository
import com.goldjewelry.app.data.repository.InvoiceRepository
import com.goldjewelry.app.data.repository.ProductRepository
import com.goldjewelry.app.data.repository.SettingsRepository

class GoldJewelryApp : Application() {

    val database by lazy { AppDatabase.getInstance(this) }

    val productRepository by lazy { ProductRepository(database.productDao()) }
    val customerRepository by lazy { CustomerRepository(database.customerDao()) }
    val invoiceRepository by lazy { InvoiceRepository(database.invoiceDao()) }
    val settingsRepository by lazy { SettingsRepository(database.settingsDao()) }
}
