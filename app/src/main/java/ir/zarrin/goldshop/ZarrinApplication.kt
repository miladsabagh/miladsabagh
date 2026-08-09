package ir.zarrin.goldshop

import android.app.Application
import android.content.Context
import ir.zarrin.goldshop.data.ShopRepository
import ir.zarrin.goldshop.data.local.ZarrinDatabase
import ir.zarrin.goldshop.data.settings.SettingsRepository

/** Hand rolled dependency container; the app is small enough not to need a DI framework. */
class AppContainer(context: Context) {

    private val database = ZarrinDatabase.build(context)

    val repository = ShopRepository(
        productDao = database.productDao(),
        customerDao = database.customerDao(),
        invoiceDao = database.invoiceDao()
    )

    val settingsRepository = SettingsRepository(context.applicationContext)
}

class ZarrinApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
