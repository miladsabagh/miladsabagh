package ir.goldshop.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
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
import ir.goldshop.app.data.repository.GoldShopRepository
import ir.goldshop.app.ui.GoldShopViewModelFactory
import ir.goldshop.app.ui.customers.CustomerEditScreen
import ir.goldshop.app.ui.customers.CustomerListScreen
import ir.goldshop.app.ui.customers.CustomerViewModel
import ir.goldshop.app.ui.dashboard.DashboardScreen
import ir.goldshop.app.ui.dashboard.DashboardViewModel
import ir.goldshop.app.ui.invoice.InvoiceDetailScreen
import ir.goldshop.app.ui.invoice.InvoiceListScreen
import ir.goldshop.app.ui.invoice.InvoiceViewModel
import ir.goldshop.app.ui.invoice.NewInvoiceScreen
import ir.goldshop.app.ui.products.ProductEditScreen
import ir.goldshop.app.ui.products.ProductListScreen
import ir.goldshop.app.ui.products.ProductViewModel
import ir.goldshop.app.ui.settings.SettingsScreen
import ir.goldshop.app.ui.settings.SettingsViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

private val bottomBarIcons = mapOf(
    Screen.Dashboard.route to Icons.Filled.Dashboard,
    Screen.Invoices.route to Icons.AutoMirrored.Filled.ReceiptLong,
    Screen.Products.route to Icons.Filled.Diamond,
    Screen.Customers.route to Icons.Filled.People,
    Screen.Settings.route to Icons.Filled.Settings
)

@Composable
fun GoldShopApp(repository: GoldShopRepository) {
    val navController = rememberNavController()
    val factory = GoldShopViewModelFactory(repository)

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = bottomNavItems.any { it.first.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { (screen, label) ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (!selected) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(bottomBarIcons[screen.route]!!, contentDescription = label) },
                            label = { Text(label) }
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
                val vm: DashboardViewModel = viewModel(factory = factory)
                DashboardScreen(
                    viewModel = vm,
                    onNewInvoice = { navController.navigate(Screen.NewInvoice.route) },
                    onOpenProducts = { navController.navigate(Screen.Products.route) },
                    onOpenCustomers = { navController.navigate(Screen.Customers.route) },
                    onOpenSettings = { navController.navigate(Screen.Settings.route) },
                    onOpenInvoices = { navController.navigate(Screen.Invoices.route) }
                )
            }

            composable(Screen.Invoices.route) {
                val vm: InvoiceViewModel = viewModel(factory = factory)
                InvoiceListScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() },
                    onNewInvoice = { navController.navigate(Screen.NewInvoice.route) },
                    onOpenInvoice = { id -> navController.navigate(Screen.InvoiceDetail.createRoute(id)) }
                )
            }

            composable(Screen.Products.route) {
                val vm: ProductViewModel = viewModel(factory = factory)
                ProductListScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() },
                    onAddProduct = { navController.navigate(Screen.ProductEdit.createRoute()) },
                    onEditProduct = { id -> navController.navigate(Screen.ProductEdit.createRoute(id)) }
                )
            }

            composable(Screen.Customers.route) {
                val vm: CustomerViewModel = viewModel(factory = factory)
                CustomerListScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() },
                    onAddCustomer = { navController.navigate(Screen.CustomerEdit.createRoute()) },
                    onEditCustomer = { id -> navController.navigate(Screen.CustomerEdit.createRoute(id)) }
                )
            }

            composable(Screen.Settings.route) {
                val vm: SettingsViewModel = viewModel(factory = factory)
                SettingsScreen(viewModel = vm, onBack = { navController.popBackStack() })
            }

            composable(Screen.NewInvoice.route) {
                val vm: InvoiceViewModel = viewModel(factory = factory)
                NewInvoiceScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() },
                    onInvoiceCreated = { id ->
                        navController.navigate(Screen.InvoiceDetail.createRoute(id)) {
                            popUpTo(Screen.Dashboard.route)
                        }
                    }
                )
            }

            composable(
                route = Screen.InvoiceDetail.route,
                arguments = listOf(navArgument("invoiceId") { type = NavType.LongType })
            ) { backStackEntry ->
                val vm: InvoiceViewModel = viewModel(factory = factory)
                val invoiceId = backStackEntry.arguments?.getLong("invoiceId") ?: 0L
                InvoiceDetailScreen(
                    viewModel = vm,
                    repository = repository,
                    invoiceId = invoiceId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.ProductEdit.route,
                arguments = listOf(navArgument("productId") { type = NavType.LongType; defaultValue = -1L })
            ) { backStackEntry ->
                val vm: ProductViewModel = viewModel(factory = factory)
                val productId = backStackEntry.arguments?.getLong("productId")?.takeIf { it >= 0 }
                ProductEditScreen(
                    viewModel = vm,
                    productId = productId,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.CustomerEdit.route,
                arguments = listOf(navArgument("customerId") { type = NavType.LongType; defaultValue = -1L })
            ) { backStackEntry ->
                val vm: CustomerViewModel = viewModel(factory = factory)
                val customerId = backStackEntry.arguments?.getLong("customerId")?.takeIf { it >= 0 }
                CustomerEditScreen(
                    viewModel = vm,
                    customerId = customerId,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }
        }
    }
}
