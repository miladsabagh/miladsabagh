package com.zarrin.goldshop

import android.app.Application
import com.zarrin.goldshop.data.AppDatabase
import com.zarrin.goldshop.data.ShopRepository

class GoldShopApp : Application() {
    lateinit var repository: ShopRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = ShopRepository(AppDatabase.get(this))
    }
}
