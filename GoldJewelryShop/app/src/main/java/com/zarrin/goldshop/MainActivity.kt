package com.zarrin.goldshop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zarrin.goldshop.ui.ShopViewModel
import com.zarrin.goldshop.ui.ShopViewModelFactory
import com.zarrin.goldshop.ui.navigation.AppNav
import com.zarrin.goldshop.ui.theme.GoldShopTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as GoldShopApp
        setContent {
            GoldShopTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    val viewModel: ShopViewModel = viewModel(
                        factory = ShopViewModelFactory(app, app.repository)
                    )
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            AppNav(viewModel)
                        }
                    }
                }
            }
        }
    }
}
