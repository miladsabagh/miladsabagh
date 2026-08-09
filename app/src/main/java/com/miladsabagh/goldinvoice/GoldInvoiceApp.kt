package com.miladsabagh.goldinvoice

import android.app.Application
import com.miladsabagh.goldinvoice.data.AppDatabase
import com.miladsabagh.goldinvoice.data.repository.CustomerRepository
import com.miladsabagh.goldinvoice.data.repository.InvoiceRepository
import com.miladsabagh.goldinvoice.data.repository.ProductRepository
import com.miladsabagh.goldinvoice.data.repository.SettingsRepository

class GoldInvoiceApp : Application() {

    private val database by lazy { AppDatabase.getInstance(this) }

    val customerRepository by lazy { CustomerRepository(database.customerDao()) }
    val productRepository by lazy { ProductRepository(database.productDao()) }
    val invoiceRepository by lazy { InvoiceRepository(database.invoiceDao()) }
    val settingsRepository by lazy { SettingsRepository(this) }
}
