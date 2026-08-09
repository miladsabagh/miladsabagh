package ir.zarrin.gold.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import ir.zarrin.gold.ui.screens.CustomersScreen
import ir.zarrin.gold.ui.screens.DashboardScreen
import ir.zarrin.gold.ui.screens.InvoiceDetailScreen
import ir.zarrin.gold.ui.screens.InvoicesScreen
import ir.zarrin.gold.ui.screens.NewInvoiceScreen
import ir.zarrin.gold.ui.screens.ProductsScreen
import ir.zarrin.gold.ui.screens.SettingsScreen

object Routes {
    const val DASHBOARD = "dashboard"
    const val PRODUCTS = "products"
    const val INVOICES = "invoices"
    const val CUSTOMERS = "customers"
    const val NEW_INVOICE = "new_invoice"
    const val SETTINGS = "settings"
    const val INVOICE_DETAIL = "invoice/{id}"
    fun invoiceDetail(id: Long) = "invoice/$id"
}

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab(Routes.DASHBOARD, "داشبورد", Icons.Filled.Home),
    Tab(Routes.PRODUCTS, "محصولات", Icons.Filled.Diamond),
    Tab(Routes.INVOICES, "فاکتورها", Icons.AutoMirrored.Filled.ReceiptLong),
    Tab(Routes.CUSTOMERS, "مشتریان", Icons.Filled.People),
)

@Composable
fun AppRoot(viewModel: AppViewModel = viewModel()) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in tabs.map { it.route }) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.DASHBOARD,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.DASHBOARD) {
                DashboardScreen(
                    viewModel = viewModel,
                    onNewInvoice = { navController.navigate(Routes.NEW_INVOICE) },
                    onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                    onOpenInvoice = { navController.navigate(Routes.invoiceDetail(it)) },
                )
            }
            composable(Routes.PRODUCTS) { ProductsScreen(viewModel) }
            composable(Routes.INVOICES) {
                InvoicesScreen(
                    viewModel = viewModel,
                    onNewInvoice = { navController.navigate(Routes.NEW_INVOICE) },
                    onOpenInvoice = { navController.navigate(Routes.invoiceDetail(it)) },
                )
            }
            composable(Routes.CUSTOMERS) { CustomersScreen(viewModel) }
            composable(Routes.SETTINGS) {
                SettingsScreen(viewModel, onBack = { navController.popBackStack() })
            }
            composable(Routes.NEW_INVOICE) {
                NewInvoiceScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onSaved = { id ->
                        navController.popBackStack()
                        navController.navigate(Routes.invoiceDetail(id))
                    },
                )
            }
            composable(
                Routes.INVOICE_DETAIL,
                arguments = listOf(navArgument("id") { type = NavType.LongType }),
            ) { entry ->
                val id = entry.arguments?.getLong("id") ?: 0L
                InvoiceDetailScreen(
                    viewModel = viewModel,
                    invoiceId = id,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
