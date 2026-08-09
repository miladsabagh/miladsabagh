package ir.goldshop.app

import android.app.Application
import ir.goldshop.app.data.AppDatabase
import ir.goldshop.app.data.repository.GoldShopRepository

class GoldShopApplication : Application() {

    lateinit var repository: GoldShopRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.getInstance(this)
        repository = GoldShopRepository(database)
    }
}
