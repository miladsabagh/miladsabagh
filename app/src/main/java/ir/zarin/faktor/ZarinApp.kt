package ir.zarin.faktor

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import ir.zarin.faktor.data.local.AppDatabase
import ir.zarin.faktor.data.repository.CustomerRepository
import ir.zarin.faktor.data.repository.InvoiceRepository
import ir.zarin.faktor.data.repository.ProductRepository
import ir.zarin.faktor.data.settings.SettingsRepository

/** وابستگی‌های سراسری برنامه؛ ساخت تنبل و بدون کتابخانه تزریق وابستگی. */
class AppContainer(context: Context) {

    private val applicationContext = context.applicationContext

    val database: AppDatabase by lazy { AppDatabase.build(applicationContext) }
    val productRepository: ProductRepository by lazy { ProductRepository(database) }
    val customerRepository: CustomerRepository by lazy { CustomerRepository(database) }
    val invoiceRepository: InvoiceRepository by lazy { InvoiceRepository(database) }
    val settingsRepository: SettingsRepository by lazy { SettingsRepository(applicationContext) }
}

class ZarinApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

fun CreationExtras.appContainer(): AppContainer =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ZarinApp).container
