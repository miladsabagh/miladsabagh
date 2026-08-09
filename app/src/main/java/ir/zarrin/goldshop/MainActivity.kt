package ir.zarrin.goldshop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import ir.zarrin.goldshop.data.settings.AppSettings
import ir.zarrin.goldshop.ui.ZarrinAppRoot
import ir.zarrin.goldshop.ui.LocalAppContainer
import ir.zarrin.goldshop.ui.theme.ZarrinTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val container = (application as ZarrinApp).container

        setContent {
            val settings by container.settingsRepository.settings
                .collectAsState(initial = AppSettings())

            CompositionLocalProvider(
                LocalAppContainer provides container,
                LocalLayoutDirection provides LayoutDirection.Rtl
            ) {
                ZarrinTheme(darkTheme = settings.darkTheme) {
                    ZarrinAppRoot(settings = settings)
                }
            }
        }
    }
}
