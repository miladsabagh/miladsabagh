package ir.zarrin.goldshop.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ir.zarrin.goldshop.ui.screens.CustomerEditorScreen
import ir.zarrin.goldshop.ui.screens.CustomerListScreen
import ir.zarrin.goldshop.ui.screens.DashboardScreen
import ir.zarrin.goldshop.ui.screens.InvoiceDetailScreen
import ir.zarrin.goldshop.ui.screens.InvoiceEditorScreen
import ir.zarrin.goldshop.ui.screens.InvoiceListScreen
import ir.zarrin.goldshop.ui.screens.ProductEditorScreen
import ir.zarrin.goldshop.ui.screens.ProductListScreen
import ir.zarrin.goldshop.ui.screens.ReportsScreen
import ir.zarrin.goldshop.ui.screens.SettingsScreen

object Routes {
    const val DASHBOARD = "dashboard"
    const val INVOICES = "invoices"
    const val PRODUCTS = "products"
    const val CUSTOMERS = "customers"
    const val SETTINGS = "settings"
    const val REPORTS = "reports"

    const val PRODUCT_EDITOR = "product_editor/{productId}"
    const val CUSTOMER_EDITOR = "customer_editor/{customerId}"
    const val INVOICE_EDITOR = "invoice_editor/{invoiceId}"
    const val INVOICE_DETAIL = "invoice_detail/{invoiceId}"

    fun productEditor(id: Long = 0L) = "product_editor/$id"
    fun customerEditor(id: Long = 0L) = "customer_editor/$id"
    fun invoiceEditor(id: Long = 0L) = "invoice_editor/$id"
    fun invoiceDetail(id: Long) = "invoice_detail/$id"
}

private data class BottomDestination(val route: String, val label: String, val icon: ImageVector)

private val bottomDestinations = listOf(
    BottomDestination(Routes.DASHBOARD, "خانه", Icons.Filled.Home),
    BottomDestination(Routes.INVOICES, "فاکتورها", Icons.AutoMirrored.Filled.ReceiptLong),
    BottomDestination(Routes.PRODUCTS, "کالاها", Icons.Filled.Diamond),
    BottomDestination(Routes.CUSTOMERS, "مشتریان", Icons.Filled.Groups),
    BottomDestination(Routes.SETTINGS, "تنظیمات", Icons.Filled.Settings)
)

@Composable
private fun ZarrinBottomBar(navController: NavHostController, currentRoute: String?) {
    NavigationBar {
        bottomDestinations.forEach { destination ->
            NavigationBarItem(
                selected = currentRoute == destination.route,
                onClick = {
                    if (currentRoute != destination.route) {
                        navController.navigate(destination.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(destination.icon, contentDescription = destination.label) },
                label = { Text(destination.label) },
                colors = NavigationBarItemDefaults.colors()
            )
        }
    }
}

@Composable
fun ZarrinApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val bottomBar: @Composable () -> Unit = { ZarrinBottomBar(navController, currentRoute) }

    NavHost(navController = navController, startDestination = Routes.DASHBOARD) {
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                bottomBar = bottomBar,
                onNewInvoice = { navController.navigate(Routes.invoiceEditor()) },
                onOpenInvoice = { id -> navController.navigate(Routes.invoiceDetail(id)) },
                onOpenInvoices = { navController.navigate(Routes.INVOICES) },
                onOpenProducts = { navController.navigate(Routes.PRODUCTS) },
                onOpenCustomers = { navController.navigate(Routes.CUSTOMERS) },
                onOpenReports = { navController.navigate(Routes.REPORTS) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(Routes.INVOICES) {
            InvoiceListScreen(
                bottomBar = bottomBar,
                onNewInvoice = { navController.navigate(Routes.invoiceEditor()) },
                onOpenInvoice = { id -> navController.navigate(Routes.invoiceDetail(id)) }
            )
        }
        composable(Routes.PRODUCTS) {
            ProductListScreen(
                bottomBar = bottomBar,
                onAddProduct = { navController.navigate(Routes.productEditor()) },
                onEditProduct = { id -> navController.navigate(Routes.productEditor(id)) }
            )
        }
        composable(Routes.CUSTOMERS) {
            CustomerListScreen(
                bottomBar = bottomBar,
                onAddCustomer = { navController.navigate(Routes.customerEditor()) },
                onEditCustomer = { id -> navController.navigate(Routes.customerEditor(id)) }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                bottomBar = bottomBar,
                onOpenReports = { navController.navigate(Routes.REPORTS) }
            )
        }
        composable(Routes.REPORTS) {
            ReportsScreen(
                onBack = { navController.popBackStack() },
                onOpenInvoice = { id -> navController.navigate(Routes.invoiceDetail(id)) }
            )
        }
        composable(Routes.PRODUCT_EDITOR) {
            ProductEditorScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.CUSTOMER_EDITOR) {
            CustomerEditorScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.INVOICE_EDITOR) {
            InvoiceEditorScreen(
                onBack = { navController.popBackStack() },
                onSaved = { id ->
                    navController.navigate(Routes.invoiceDetail(id)) {
                        popUpTo(Routes.INVOICE_EDITOR) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.INVOICE_DETAIL) {
            InvoiceDetailScreen(
                onBack = { navController.popBackStack() },
                onEdit = { id -> navController.navigate(Routes.invoiceEditor(id)) },
                onDeleted = {
                    navController.popBackStack(Routes.DASHBOARD, inclusive = false)
                }
            )
        }
    }
}
