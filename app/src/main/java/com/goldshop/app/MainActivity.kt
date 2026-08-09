package com.goldshop.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.goldshop.app.ui.ShopViewModel
import com.goldshop.app.ui.navigation.GoldShopNavHost
import com.goldshop.app.ui.theme.GoldShopTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        forcePersianLocale()
        enableEdgeToEdge()
        setContent {
            GoldShopTheme(darkTheme = false) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val app = application as GoldShopApplication
                    val viewModel: ShopViewModel = viewModel(factory = ShopViewModel.factory(app))
                    GoldShopNavHost(viewModel = viewModel)
                }
            }
        }
    }

    private fun forcePersianLocale() {
        val locale = Locale.forLanguageTag("fa-IR")
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}
