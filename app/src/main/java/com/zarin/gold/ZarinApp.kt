package com.zarin.gold

import android.app.Application
import com.zarin.gold.data.ZarinDatabase
import com.zarin.gold.data.ZarinRepository

class ZarinApp : Application() {
    lateinit var repository: ZarinRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val db = ZarinDatabase.get(this)
        repository = ZarinRepository(db)
    }
}
