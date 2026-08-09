package com.goldshop.app

import android.app.Application
import com.goldshop.app.data.local.AppDatabase
import com.goldshop.app.data.repository.ShopRepository

class GoldShopApplication : Application() {
    lateinit var repository: ShopRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.getInstance(this)
        repository = ShopRepository(db)
    }
}
