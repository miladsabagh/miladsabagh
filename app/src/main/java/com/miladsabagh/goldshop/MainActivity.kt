package com.miladsabagh.goldshop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.miladsabagh.goldshop.ui.AppViewModelFactory
import com.miladsabagh.goldshop.ui.LocalViewModelFactory
import com.miladsabagh.goldshop.ui.navigation.GoldShopNavGraph
import com.miladsabagh.goldshop.ui.theme.GoldShopTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        val app = application as GoldShopApp
        val viewModelFactory = AppViewModelFactory(app)

        setContent {
            GoldShopTheme {
                CompositionLocalProvider(
                    LocalViewModelFactory provides viewModelFactory,
                    LocalLayoutDirection provides LayoutDirection.Rtl
                ) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        GoldShopNavGraph()
                    }
                }
            }
        }
    }
}
