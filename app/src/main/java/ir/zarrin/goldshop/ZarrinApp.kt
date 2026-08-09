package ir.zarrin.goldshop

import android.app.Application
import android.content.Context
import ir.zarrin.goldshop.data.db.ZarrinDatabase
import ir.zarrin.goldshop.data.repo.CustomerRepository
import ir.zarrin.goldshop.data.repo.InvoiceRepository
import ir.zarrin.goldshop.data.repo.ProductRepository
import ir.zarrin.goldshop.data.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** نگهدارنده وابستگی‌های سراسری برنامه */
class AppContainer(context: Context) {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database: ZarrinDatabase = ZarrinDatabase.build(context, applicationScope)

    val settingsRepository = SettingsRepository(context)

    val productRepository = ProductRepository(database.productDao())

    val customerRepository = CustomerRepository(database.customerDao())

    val invoiceRepository = InvoiceRepository(
        database = database,
        invoiceDao = database.invoiceDao(),
        productDao = database.productDao(),
        settingsRepository = settingsRepository
    )
}

class ZarrinApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
