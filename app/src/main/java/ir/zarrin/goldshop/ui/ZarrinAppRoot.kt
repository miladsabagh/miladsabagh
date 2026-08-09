package ir.zarrin.goldshop.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ir.zarrin.goldshop.data.settings.AppSettings
import ir.zarrin.goldshop.ui.customers.CustomersScreen
import ir.zarrin.goldshop.ui.dashboard.DashboardScreen
import ir.zarrin.goldshop.ui.invoices.InvoiceDetailScreen
import ir.zarrin.goldshop.ui.invoices.InvoicesScreen
import ir.zarrin.goldshop.ui.newinvoice.NewInvoiceScreen
import ir.zarrin.goldshop.ui.products.ProductEditScreen
import ir.zarrin.goldshop.ui.products.ProductsScreen
import ir.zarrin.goldshop.ui.reports.ReportsScreen
import ir.zarrin.goldshop.ui.settings.SettingsScreen

object Routes {
    const val DASHBOARD = "dashboard"
    const val INVOICES = "invoices"
    const val PRODUCTS = "products"
    const val CUSTOMERS = "customers"
    const val SETTINGS = "settings"
    const val REPORTS = "reports"
    const val NEW_INVOICE = "new_invoice"
    const val INVOICE_DETAIL = "invoice/{id}"
    const val PRODUCT_EDIT = "product/{id}"

    fun invoiceDetail(id: Long) = "invoice/$id"
    fun productEdit(id: Long) = "product/$id"
}

private data class TabItem(val route: String, val label: String, val icon: ImageVector)

private val TABS = listOf(
    TabItem(Routes.DASHBOARD, "خانه", Icons.Default.Home),
    TabItem(Routes.INVOICES, "فاکتورها", Icons.Default.Description),
    TabItem(Routes.PRODUCTS, "کالاها", Icons.Default.Diamond),
    TabItem(Routes.CUSTOMERS, "مشتریان", Icons.Default.People),
    TabItem(Routes.SETTINGS, "تنظیمات", Icons.Default.Settings)
)

@Composable
fun ZarrinAppRoot(settings: AppSettings) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = TABS.any { it.route == currentRoute }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    TABS.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                if (currentRoute != tab.route) {
                                    navController.navigate(tab.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label, style = MaterialTheme.typography.labelSmall) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.DASHBOARD,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.DASHBOARD) {
                DashboardScreen(
                    onNewInvoice = { navController.navigate(Routes.NEW_INVOICE) },
                    onOpenInvoice = { navController.navigate(Routes.invoiceDetail(it)) },
                    onOpenInvoices = { navController.navigate(Routes.INVOICES) },
                    onOpenProducts = { navController.navigate(Routes.PRODUCTS) },
                    onOpenCustomers = { navController.navigate(Routes.CUSTOMERS) },
                    onOpenReports = { navController.navigate(Routes.REPORTS) }
                )
            }
            composable(Routes.INVOICES) {
                InvoicesScreen(
                    onNewInvoice = { navController.navigate(Routes.NEW_INVOICE) },
                    onOpenInvoice = { navController.navigate(Routes.invoiceDetail(it)) }
                )
            }
            composable(Routes.PRODUCTS) {
                ProductsScreen(
                    onAddProduct = { navController.navigate(Routes.productEdit(0L)) },
                    onEditProduct = { navController.navigate(Routes.productEdit(it)) }
                )
            }
            composable(Routes.CUSTOMERS) {
                CustomersScreen()
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(onOpenReports = { navController.navigate(Routes.REPORTS) })
            }
            composable(Routes.REPORTS) {
                ReportsScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.NEW_INVOICE) {
                NewInvoiceScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = { id ->
                        navController.popBackStack()
                        navController.navigate(Routes.invoiceDetail(id))
                    }
                )
            }
            composable(
                route = Routes.INVOICE_DETAIL,
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { entry ->
                InvoiceDetailScreen(
                    invoiceId = entry.arguments?.getLong("id") ?: 0L,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Routes.PRODUCT_EDIT,
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { entry ->
                ProductEditScreen(
                    productId = entry.arguments?.getLong("id") ?: 0L,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
