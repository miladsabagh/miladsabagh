package com.goldgallery.app

import android.app.Application
import com.goldgallery.app.data.ShopRepository
import com.goldgallery.app.data.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class GoldGalleryApp : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    lateinit var repository: ShopRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = ShopRepository(AppDatabase.get(this))
        appScope.launch { repository.seedIfEmpty() }
    }
}
