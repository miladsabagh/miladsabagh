package com.miladsabagh.goldshop.ui

import androidx.compose.runtime.compositionLocalOf
import androidx.lifecycle.ViewModelProvider

val LocalViewModelFactory = compositionLocalOf<ViewModelProvider.Factory> {
    error("ViewModelFactory not provided. Wrap the composable tree with CompositionLocalProvider.")
}
