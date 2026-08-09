package ir.zarrin.goldshop.ui

import androidx.compose.runtime.compositionLocalOf
import ir.zarrin.goldshop.AppContainer

val LocalAppContainer = compositionLocalOf<AppContainer> {
    error("AppContainer در دسترس نیست")
}
