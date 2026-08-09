package com.goldgallery.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.goldgallery.app.ui.AppNavHost
import com.goldgallery.app.ui.theme.GoldGalleryTheme
import com.goldgallery.app.viewmodel.ShopViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = (application as GoldGalleryApp).repository
        setContent {
            GoldGalleryTheme {
                val viewModel: ShopViewModel = viewModel(factory = ShopViewModel.factory(repository))
                AppNavHost(viewModel)
            }
        }
    }
}
