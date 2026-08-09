package ir.zarrin.gold

import android.app.Application
import ir.zarrin.gold.data.AppDatabase
import ir.zarrin.gold.data.SettingsStore

class GoldApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.get(this) }
    val settingsStore: SettingsStore by lazy { SettingsStore(this) }
}
