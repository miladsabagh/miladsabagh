package com.goldjewelry.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.goldjewelry.app.GoldJewelryApp
import com.goldjewelry.app.ui.screens.customers.CustomersScreen
import com.goldjewelry.app.ui.screens.dashboard.DashboardScreen
import com.goldjewelry.app.ui.screens.invoices.InvoiceDetailScreen
import com.goldjewelry.app.ui.screens.invoices.InvoicesListScreen
import com.goldjewelry.app.ui.screens.products.ProductsScreen
import com.goldjewelry.app.ui.screens.sales.SalesScreen
import com.goldjewelry.app.ui.screens.settings.SettingsScreen
import com.goldjewelry.app.ui.viewmodel.CustomersViewModel
import com.goldjewelry.app.ui.viewmodel.DashboardViewModel
import com.goldjewelry.app.ui.viewmodel.InvoicesViewModel
import com.goldjewelry.app.ui.viewmodel.ProductsViewModel
import com.goldjewelry.app.ui.viewmodel.SalesViewModel
import com.goldjewelry.app.ui.viewmodel.SettingsViewModel

@Composable
fun GoldJewelryNavHost() {
    val context = LocalContext.current
    val app = context.applicationContext as GoldJewelryApp
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                val vm: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory(app))
                DashboardScreen(
                    viewModel = vm,
                    onInvoiceClick = { id ->
                        navController.navigate(Screen.InvoiceDetail.createRoute(id))
                    },
                    onSettingsClick = {
                        navController.navigate(Screen.Settings.route)
                    }
                )
            }

            composable(Screen.Products.route) {
                val vm: ProductsViewModel = viewModel(factory = ProductsViewModel.Factory(app))
                ProductsScreen(viewModel = vm)
            }

            composable(Screen.Sales.route) {
                val vm: SalesViewModel = viewModel(factory = SalesViewModel.Factory(app))
                SalesScreen(
                    viewModel = vm,
                    onInvoiceCreated = { id ->
                        navController.navigate(Screen.InvoiceDetail.createRoute(id)) {
                            popUpTo(Screen.Sales.route)
                        }
                    }
                )
            }

            composable(Screen.Invoices.route) {
                val vm: InvoicesViewModel = viewModel(factory = InvoicesViewModel.Factory(app))
                InvoicesListScreen(
                    viewModel = vm,
                    onInvoiceClick = { id ->
                        navController.navigate(Screen.InvoiceDetail.createRoute(id))
                    }
                )
            }

            composable(Screen.Customers.route) {
                val vm: CustomersViewModel = viewModel(factory = CustomersViewModel.Factory(app))
                CustomersScreen(viewModel = vm)
            }

            composable(Screen.Settings.route) {
                val vm: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory(app))
                SettingsScreen(viewModel = vm)
            }

            composable(
                route = Screen.InvoiceDetail.route,
                arguments = listOf(navArgument("invoiceId") { type = NavType.LongType })
            ) { backStackEntry ->
                val invoiceId = backStackEntry.arguments?.getLong("invoiceId") ?: return@composable
                val vm: InvoicesViewModel = viewModel(factory = InvoicesViewModel.Factory(app))
                InvoiceDetailScreen(
                    invoiceId = invoiceId,
                    viewModel = vm,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
