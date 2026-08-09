package com.zarfam.goldshop

import android.app.Application
import com.zarfam.goldshop.data.db.AppDatabase

class GoldShopApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.get(this) }
}
