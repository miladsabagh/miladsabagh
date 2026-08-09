package com.miladsabagh.zarrin.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miladsabagh.zarrin.data.Repository
import com.miladsabagh.zarrin.data.SettingsStore
import com.miladsabagh.zarrin.ui.screens.CustomersScreen
import com.miladsabagh.zarrin.ui.screens.DashboardScreen
import com.miladsabagh.zarrin.ui.screens.InvoiceDetailScreen
import com.miladsabagh.zarrin.ui.screens.InvoicesScreen
import com.miladsabagh.zarrin.ui.screens.NewInvoiceScreen
import com.miladsabagh.zarrin.ui.screens.ProductsScreen
import com.miladsabagh.zarrin.ui.screens.SettingsScreen

object Routes {
    const val DASHBOARD = "dashboard"
    const val PRODUCTS = "products"
    const val CUSTOMERS = "customers"
    const val INVOICES = "invoices"
    const val SETTINGS = "settings"
    const val NEW_INVOICE = "new_invoice"
    const val INVOICE_DETAIL = "invoice_detail"
}

data class BottomDest(
    val route: String,
    val label: String,
    val icon: ImageVector
)

val bottomDestinations = listOf(
    BottomDest(Routes.DASHBOARD, "داشبورد", Icons.Filled.Home),
    BottomDest(Routes.PRODUCTS, "محصولات", Icons.Filled.Diamond),
    BottomDest(Routes.NEW_INVOICE, "فاکتور جدید", Icons.Filled.Add),
    BottomDest(Routes.INVOICES, "فاکتورها", Icons.Filled.Receipt),
    BottomDest(Routes.CUSTOMERS, "مشتریان", Icons.Filled.People)
)

@Composable
fun ZarrinApp(repository: Repository, settingsStore: SettingsStore) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val showBottomBar = currentRoute in bottomDestinations.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomDestinations.forEach { dest ->
                        NavigationBarItem(
                            selected = currentRoute == dest.route,
                            onClick = {
                                navController.navigate(dest.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(dest.icon, contentDescription = dest.label) },
                            label = { Text(dest.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            repository = repository,
            settingsStore = settingsStore,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    repository: Repository,
    settingsStore: SettingsStore,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.DASHBOARD,
        modifier = modifier
    ) {
        composable(Routes.DASHBOARD) {
            val vm: DashboardViewModel = viewModel(
                key = "dashboard",
                initializer = { DashboardViewModel(repository, settingsStore) }
            )
            DashboardScreen(
                viewModel = vm,
                onNewInvoice = { navController.navigate(Routes.NEW_INVOICE) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onOpenInvoice = { id -> navController.navigate("${Routes.INVOICE_DETAIL}/$id") }
            )
        }

        composable(Routes.PRODUCTS) {
            val vm: ProductsViewModel = viewModel(
                key = "products",
                initializer = { ProductsViewModel(repository) }
            )
            ProductsScreen(viewModel = vm)
        }

        composable(Routes.CUSTOMERS) {
            val vm: CustomersViewModel = viewModel(
                key = "customers",
                initializer = { CustomersViewModel(repository) }
            )
            CustomersScreen(viewModel = vm)
        }

        composable(Routes.INVOICES) {
            val vm: InvoicesViewModel = viewModel(
                key = "invoices",
                initializer = { InvoicesViewModel(repository) }
            )
            InvoicesScreen(
                viewModel = vm,
                onOpenInvoice = { id -> navController.navigate("${Routes.INVOICE_DETAIL}/$id") }
            )
        }

        composable(Routes.NEW_INVOICE) {
            val vm: NewInvoiceViewModel = viewModel(
                key = "new_invoice",
                initializer = { NewInvoiceViewModel(repository, settingsStore) }
            )
            NewInvoiceScreen(
                viewModel = vm,
                onSaved = { id ->
                    navController.navigate("${Routes.INVOICE_DETAIL}/$id") {
                        popUpTo(Routes.INVOICES)
                    }
                }
            )
        }

        composable(Routes.SETTINGS) {
            val vm: SettingsViewModel = viewModel(
                key = "settings",
                initializer = { SettingsViewModel(settingsStore) }
            )
            SettingsScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }

        composable(
            route = "${Routes.INVOICE_DETAIL}/{invoiceId}",
            arguments = listOf(navArgument("invoiceId") { type = NavType.LongType })
        ) { entry ->
            val invoiceId = entry.arguments?.getLong("invoiceId") ?: return@composable
            val vm: InvoiceDetailViewModel = viewModel(
                key = "invoice_detail_$invoiceId",
                initializer = { InvoiceDetailViewModel(repository, settingsStore, invoiceId) }
            )
            InvoiceDetailScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
