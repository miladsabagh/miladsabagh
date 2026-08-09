package com.miladsabagh.goldinvoice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.miladsabagh.goldinvoice.ui.ViewModelFactory
import com.miladsabagh.goldinvoice.ui.customers.CustomerEditScreen
import com.miladsabagh.goldinvoice.ui.customers.CustomerEditViewModel
import com.miladsabagh.goldinvoice.ui.customers.CustomerListScreen
import com.miladsabagh.goldinvoice.ui.customers.CustomerListViewModel
import com.miladsabagh.goldinvoice.ui.dashboard.DashboardScreen
import com.miladsabagh.goldinvoice.ui.dashboard.DashboardViewModel
import com.miladsabagh.goldinvoice.ui.invoice.NewInvoiceScreen
import com.miladsabagh.goldinvoice.ui.invoice.NewInvoiceViewModel
import com.miladsabagh.goldinvoice.ui.invoices.InvoiceDetailScreen
import com.miladsabagh.goldinvoice.ui.invoices.InvoiceDetailViewModel
import com.miladsabagh.goldinvoice.ui.invoices.InvoiceListScreen
import com.miladsabagh.goldinvoice.ui.invoices.InvoiceListViewModel
import com.miladsabagh.goldinvoice.ui.navigation.Routes
import com.miladsabagh.goldinvoice.ui.navigation.bottomTabs
import com.miladsabagh.goldinvoice.ui.products.ProductEditScreen
import com.miladsabagh.goldinvoice.ui.products.ProductEditViewModel
import com.miladsabagh.goldinvoice.ui.products.ProductListScreen
import com.miladsabagh.goldinvoice.ui.products.ProductListViewModel
import com.miladsabagh.goldinvoice.ui.settings.SettingsScreen
import com.miladsabagh.goldinvoice.ui.settings.SettingsViewModel
import com.miladsabagh.goldinvoice.ui.theme.GoldInvoiceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as GoldInvoiceApp
        setContent {
            GoldInvoiceTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    AppRoot(app)
                }
            }
        }
    }
}

@Composable
private fun AppRoot(app: GoldInvoiceApp) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination

    Scaffold(
        bottomBar = {
            val showBottomBar = bottomTabs.any { tab -> currentRoute?.hierarchy?.any { it.route == tab.route } == true }
            if (showBottomBar) {
                NavigationBar {
                    bottomTabs.forEach { tab ->
                        val selected = currentRoute?.hierarchy?.any { it.route == tab.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = stringResource(tab.labelRes)) },
                            label = { Text(stringResource(tab.labelRes)) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.DASHBOARD,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.DASHBOARD) {
                val vm: DashboardViewModel = viewModel(factory = ViewModelFactory {
                    DashboardViewModel(app.settingsRepository, app.invoiceRepository, app.customerRepository, app.productRepository)
                })
                DashboardScreen(
                    viewModel = vm,
                    onNewInvoice = { navController.navigate(Routes.INVOICE_NEW) },
                    onNewProduct = { navController.navigate(Routes.PRODUCT_NEW) },
                    onNewCustomer = { navController.navigate(Routes.CUSTOMER_NEW) },
                    onInvoiceClick = { id -> navController.navigate(Routes.invoiceDetail(id)) }
                )
            }

            composable(Routes.INVOICES) {
                val vm: InvoiceListViewModel = viewModel(factory = ViewModelFactory {
                    InvoiceListViewModel(app.invoiceRepository)
                })
                InvoiceListScreen(
                    viewModel = vm,
                    onNewInvoice = { navController.navigate(Routes.INVOICE_NEW) },
                    onInvoiceClick = { id -> navController.navigate(Routes.invoiceDetail(id)) }
                )
            }

            composable(Routes.INVOICE_NEW) {
                val vm: NewInvoiceViewModel = viewModel(factory = ViewModelFactory {
                    NewInvoiceViewModel(app.invoiceRepository, app.productRepository, app.customerRepository, app.settingsRepository)
                })
                NewInvoiceScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() },
                    onSaved = { id ->
                        navController.navigate(Routes.invoiceDetail(id)) {
                            popUpTo(Routes.INVOICES) { inclusive = false }
                        }
                    }
                )
            }

            composable(
                route = Routes.INVOICE_DETAIL,
                arguments = listOf(navArgument("invoiceId") { type = NavType.LongType })
            ) { backStack ->
                val invoiceId = backStack.arguments?.getLong("invoiceId") ?: 0L
                val vm: InvoiceDetailViewModel = viewModel(factory = ViewModelFactory {
                    InvoiceDetailViewModel(app.invoiceRepository, app.settingsRepository, invoiceId)
                })
                InvoiceDetailScreen(viewModel = vm, onBack = { navController.popBackStack() })
            }

            composable(Routes.PRODUCTS) {
                val vm: ProductListViewModel = viewModel(factory = ViewModelFactory {
                    ProductListViewModel(app.productRepository)
                })
                ProductListScreen(
                    viewModel = vm,
                    onAddProduct = { navController.navigate(Routes.PRODUCT_NEW) },
                    onEditProduct = { id -> navController.navigate(Routes.productEdit(id)) }
                )
            }

            composable(Routes.PRODUCT_NEW) {
                val vm: ProductEditViewModel = viewModel(factory = ViewModelFactory {
                    ProductEditViewModel(app.productRepository, app.settingsRepository, null)
                })
                ProductEditScreen(viewModel = vm, onBack = { navController.popBackStack() })
            }

            composable(
                route = Routes.PRODUCT_EDIT,
                arguments = listOf(navArgument("productId") { type = NavType.LongType })
            ) { backStack ->
                val productId = backStack.arguments?.getLong("productId") ?: 0L
                val vm: ProductEditViewModel = viewModel(factory = ViewModelFactory {
                    ProductEditViewModel(app.productRepository, app.settingsRepository, productId)
                })
                ProductEditScreen(viewModel = vm, onBack = { navController.popBackStack() })
            }

            composable(Routes.CUSTOMERS) {
                val vm: CustomerListViewModel = viewModel(factory = ViewModelFactory {
                    CustomerListViewModel(app.customerRepository)
                })
                CustomerListScreen(
                    viewModel = vm,
                    onAddCustomer = { navController.navigate(Routes.CUSTOMER_NEW) },
                    onEditCustomer = { id -> navController.navigate(Routes.customerEdit(id)) }
                )
            }

            composable(Routes.CUSTOMER_NEW) {
                val vm: CustomerEditViewModel = viewModel(factory = ViewModelFactory {
                    CustomerEditViewModel(app.customerRepository, null)
                })
                CustomerEditScreen(viewModel = vm, onBack = { navController.popBackStack() })
            }

            composable(
                route = Routes.CUSTOMER_EDIT,
                arguments = listOf(navArgument("customerId") { type = NavType.LongType })
            ) { backStack ->
                val customerId = backStack.arguments?.getLong("customerId") ?: 0L
                val vm: CustomerEditViewModel = viewModel(factory = ViewModelFactory {
                    CustomerEditViewModel(app.customerRepository, customerId)
                })
                CustomerEditScreen(viewModel = vm, onBack = { navController.popBackStack() })
            }

            composable(Routes.SETTINGS) {
                val vm: SettingsViewModel = viewModel(factory = ViewModelFactory {
                    SettingsViewModel(app.settingsRepository)
                })
                SettingsScreen(viewModel = vm)
            }
        }
    }
}
