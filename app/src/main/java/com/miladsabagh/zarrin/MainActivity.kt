package com.miladsabagh.zarrin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.miladsabagh.zarrin.data.Repository
import com.miladsabagh.zarrin.data.SettingsStore
import com.miladsabagh.zarrin.ui.ZarrinApp
import com.miladsabagh.zarrin.ui.theme.ZarrinTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = Repository(applicationContext)
        val settingsStore = SettingsStore(applicationContext)

        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                ZarrinTheme {
                    ZarrinApp(repository = repository, settingsStore = settingsStore)
                }
            }
        }
    }
}
