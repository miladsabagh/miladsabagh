package com.goldgallery.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Diamond
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.FormatListNumberedRtl
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.goldgallery.app.logic.PersianFormat
import com.goldgallery.app.ui.screens.CartScreen
import com.goldgallery.app.ui.screens.HomeScreen
import com.goldgallery.app.ui.screens.InvoiceDetailScreen
import com.goldgallery.app.ui.screens.InvoicesScreen
import com.goldgallery.app.ui.screens.ProductsScreen
import com.goldgallery.app.viewmodel.ShopViewModel

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Home : Screen("home", "خانه", Icons.Outlined.Home)
    data object Products : Screen("products", "محصولات", Icons.Outlined.Diamond)
    data object Cart : Screen("cart", "فاکتور جدید", Icons.AutoMirrored.Outlined.ReceiptLong)
    data object Invoices : Screen("invoices", "فاکتورها", Icons.Outlined.FormatListNumberedRtl)
}

private val bottomScreens = listOf(Screen.Home, Screen.Products, Screen.Cart, Screen.Invoices)

@Composable
fun AppNavHost(viewModel: ShopViewModel) {
    val navController = rememberNavController()
    val cart by viewModel.cart.collectAsStateWithLifecycle()
    val cartCount = cart.sumOf { it.quantity }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomScreens.map { it.route }) {
                NavigationBar {
                    bottomScreens.forEach { screen ->
                        NavigationBarItem(
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                if (screen is Screen.Cart) {
                                    BadgedBox(
                                        badge = {
                                            if (cartCount > 0) {
                                                Badge { Text(PersianFormat.toPersianDigits(cartCount.toString())) }
                                            }
                                        }
                                    ) {
                                        Icon(screen.icon, contentDescription = screen.label)
                                    }
                                } else {
                                    Icon(screen.icon, contentDescription = screen.label)
                                }
                            },
                            label = { Text(screen.label) },
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigate = { route -> navController.navigate(route) },
                )
            }
            composable(Screen.Products.route) {
                ProductsScreen(viewModel = viewModel)
            }
            composable(Screen.Cart.route) {
                CartScreen(
                    viewModel = viewModel,
                    onInvoiceIssued = { id ->
                        navController.navigate("invoice/$id") {
                            popUpTo(Screen.Home.route)
                        }
                    },
                )
            }
            composable(Screen.Invoices.route) {
                InvoicesScreen(
                    viewModel = viewModel,
                    onOpenInvoice = { id -> navController.navigate("invoice/$id") },
                )
            }
            composable(
                route = "invoice/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType }),
            ) { entry ->
                InvoiceDetailScreen(
                    viewModel = viewModel,
                    invoiceId = entry.arguments?.getLong("id") ?: 0L,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
