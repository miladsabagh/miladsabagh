package com.zarnegar.gold

import android.app.Application
import android.content.Context
import com.zarnegar.gold.data.db.ZarnegarDatabase
import com.zarnegar.gold.data.repo.CustomerRepository
import com.zarnegar.gold.data.repo.InvoiceRepository
import com.zarnegar.gold.data.repo.ProductRepository
import com.zarnegar.gold.data.repo.SettingsRepository
import com.zarnegar.gold.data.seed.SeedData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** ظرف وابستگی‌های برنامه (تزریق وابستگی سبک و دستی). */
class AppContainer(context: Context) {
    private val database = ZarnegarDatabase.get(context)

    val productRepository = ProductRepository(database.productDao())
    val customerRepository = CustomerRepository(database.customerDao())
    val invoiceRepository = InvoiceRepository(database.invoiceDao(), database.productDao())
    val settingsRepository = SettingsRepository(context)

    suspend fun seed() = SeedData.populateIfEmpty(database.productDao(), database.customerDao())
}

class ZarnegarApp : Application() {

    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        appScope.launch { container.seed() }
    }
}
