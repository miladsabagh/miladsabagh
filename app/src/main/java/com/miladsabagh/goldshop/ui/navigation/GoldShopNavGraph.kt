package com.miladsabagh.goldshop.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.miladsabagh.goldshop.ui.customers.CustomerEditScreen
import com.miladsabagh.goldshop.ui.customers.CustomerListScreen
import com.miladsabagh.goldshop.ui.home.HomeScreen
import com.miladsabagh.goldshop.ui.invoice.InvoiceDetailScreen
import com.miladsabagh.goldshop.ui.invoice.InvoiceListScreen
import com.miladsabagh.goldshop.ui.invoice.NewInvoiceScreen
import com.miladsabagh.goldshop.ui.products.ProductEditScreen
import com.miladsabagh.goldshop.ui.products.ProductListScreen
import com.miladsabagh.goldshop.ui.reports.ReportsScreen
import com.miladsabagh.goldshop.ui.settings.SettingsScreen

private data class BottomNavItem(val screen: Screen, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, "خانه", Icons.Filled.Home),
    BottomNavItem(Screen.Invoices, "فاکتورها", Icons.AutoMirrored.Filled.ReceiptLong),
    BottomNavItem(Screen.Products, "کالاها", Icons.Filled.Diamond),
    BottomNavItem(Screen.Customers, "مشتریان", Icons.Filled.People),
    BottomNavItem(Screen.Settings, "تنظیمات", Icons.Filled.Settings)
)

@Composable
fun GoldShopNavGraph(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = bottomNavItems.any { it.screen.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.screen.route,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNewInvoice = { navController.navigate(Screen.NewInvoice.route) },
                    onOpenInvoices = { navController.navigate(Screen.Invoices.route) },
                    onOpenProducts = { navController.navigate(Screen.Products.route) },
                    onOpenCustomers = { navController.navigate(Screen.Customers.route) },
                    onOpenReports = { navController.navigate(Screen.Reports.route) },
                    onAddProduct = { navController.navigate(Screen.ProductEdit.createRoute()) },
                    onAddCustomer = { navController.navigate(Screen.CustomerEdit.createRoute()) }
                )
            }

            composable(Screen.Invoices.route) {
                InvoiceListScreen(
                    onNewInvoice = { navController.navigate(Screen.NewInvoice.route) },
                    onOpenInvoice = { id -> navController.navigate(Screen.InvoiceDetail.createRoute(id)) }
                )
            }

            composable(Screen.NewInvoice.route) {
                NewInvoiceScreen(
                    onBack = { navController.popBackStack() },
                    onInvoiceSaved = { id ->
                        navController.navigate(Screen.InvoiceDetail.createRoute(id)) {
                            popUpTo(Screen.Invoices.route) { inclusive = false }
                        }
                    }
                )
            }

            composable(
                route = Screen.InvoiceDetail.route,
                arguments = listOf(navArgument("invoiceId") { type = NavType.LongType })
            ) { backStack ->
                val invoiceId = backStack.arguments?.getLong("invoiceId") ?: -1L
                InvoiceDetailScreen(invoiceId = invoiceId, onBack = { navController.popBackStack() })
            }

            composable(Screen.Products.route) {
                ProductListScreen(
                    onAddProduct = { navController.navigate(Screen.ProductEdit.createRoute()) },
                    onEditProduct = { id -> navController.navigate(Screen.ProductEdit.createRoute(id)) }
                )
            }

            composable(
                route = Screen.ProductEdit.route,
                arguments = listOf(navArgument("productId") { type = NavType.LongType; defaultValue = -1L })
            ) { backStack ->
                val productId = backStack.arguments?.getLong("productId") ?: -1L
                ProductEditScreen(
                    productId = productId.takeIf { it > 0 },
                    onDone = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Customers.route) {
                CustomerListScreen(
                    onAddCustomer = { navController.navigate(Screen.CustomerEdit.createRoute()) },
                    onEditCustomer = { id -> navController.navigate(Screen.CustomerEdit.createRoute(id)) }
                )
            }

            composable(
                route = Screen.CustomerEdit.route,
                arguments = listOf(navArgument("customerId") { type = NavType.LongType; defaultValue = -1L })
            ) { backStack ->
                val customerId = backStack.arguments?.getLong("customerId") ?: -1L
                CustomerEditScreen(
                    customerId = customerId.takeIf { it > 0 },
                    onDone = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Reports.route) {
                ReportsScreen(onBack = { navController.popBackStack() })
            }

            composable(Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}
