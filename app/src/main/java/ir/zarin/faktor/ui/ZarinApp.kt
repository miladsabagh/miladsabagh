package ir.zarin.faktor.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ir.zarin.faktor.R
import ir.zarin.faktor.ui.common.DisplayFormat
import ir.zarin.faktor.ui.common.LocalDisplayFormat
import ir.zarin.faktor.ui.common.ZarinTopBar
import ir.zarin.faktor.ui.customers.CustomerEditScreen
import ir.zarin.faktor.ui.customers.CustomersScreen
import ir.zarin.faktor.ui.dashboard.DashboardScreen
import ir.zarin.faktor.ui.invoices.InvoiceDetailScreen
import ir.zarin.faktor.ui.invoices.InvoicesScreen
import ir.zarin.faktor.ui.navigation.MainScaffold
import ir.zarin.faktor.ui.navigation.Routes
import ir.zarin.faktor.ui.products.ProductEditScreen
import ir.zarin.faktor.ui.products.ProductsScreen
import ir.zarin.faktor.ui.sale.NewSaleScreen
import ir.zarin.faktor.ui.settings.SettingsScreen

@Composable
fun ZarinApp(
    rootViewModel: AppRootViewModel = viewModel(factory = AppRootViewModel.Factory),
) {
    val settings by rootViewModel.settings.collectAsStateWithLifecycle()
    val format = remember(settings.currencyUnit, settings.persianDigits) {
        DisplayFormat(unit = settings.currencyUnit, persianDigits = settings.persianDigits)
    }

    CompositionLocalProvider(LocalDisplayFormat provides format) {
        ZarinNavHost(rememberNavController())
    }
}

@Composable
private fun ZarinNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.DASHBOARD) {
        composable(Routes.DASHBOARD) {
            MainScaffold(
                title = stringResource(R.string.app_name),
                selectedRoute = Routes.DASHBOARD,
                onTabSelected = navController::navigateToTab,
                floatingActionButton = { NewInvoiceFab { navController.navigate(Routes.NEW_SALE) } },
            ) { modifier ->
                DashboardScreen(
                    onNewSale = { navController.navigate(Routes.NEW_SALE) },
                    onOpenProducts = { navController.navigateToTab(Routes.PRODUCTS) },
                    onOpenCustomers = { navController.navigate(Routes.CUSTOMERS) },
                    onOpenInvoices = { navController.navigateToTab(Routes.INVOICES) },
                    onOpenSettings = { navController.navigateToTab(Routes.SETTINGS) },
                    onOpenInvoice = { id -> navController.navigate(Routes.invoiceDetail(id)) },
                    modifier = modifier,
                )
            }
        }

        composable(Routes.PRODUCTS) {
            MainScaffold(
                title = stringResource(R.string.products_title),
                selectedRoute = Routes.PRODUCTS,
                onTabSelected = navController::navigateToTab,
                floatingActionButton = {
                    FloatingActionButton(onClick = { navController.navigate(Routes.productEdit(0)) }) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_product))
                    }
                },
            ) { modifier ->
                ProductsScreen(
                    onEditProduct = { id -> navController.navigate(Routes.productEdit(id)) },
                    modifier = modifier,
                )
            }
        }

        composable(Routes.INVOICES) {
            MainScaffold(
                title = stringResource(R.string.invoices_title),
                selectedRoute = Routes.INVOICES,
                onTabSelected = navController::navigateToTab,
                floatingActionButton = { NewInvoiceFab { navController.navigate(Routes.NEW_SALE) } },
            ) { modifier ->
                InvoicesScreen(
                    onOpenInvoice = { id -> navController.navigate(Routes.invoiceDetail(id)) },
                    modifier = modifier,
                )
            }
        }

        composable(Routes.SETTINGS) {
            MainScaffold(
                title = stringResource(R.string.settings_title),
                selectedRoute = Routes.SETTINGS,
                onTabSelected = navController::navigateToTab,
            ) { modifier ->
                SettingsScreen(modifier = modifier)
            }
        }

        composable(Routes.CUSTOMERS) {
            Scaffold(
                topBar = {
                    ZarinTopBar(
                        title = stringResource(R.string.customers_title),
                        onBack = { navController.popBackStack() },
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(onClick = { navController.navigate(Routes.customerEdit(0)) }) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_customer))
                    }
                },
            ) { padding ->
                CustomersScreen(
                    onEditCustomer = { id -> navController.navigate(Routes.customerEdit(id)) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                )
            }
        }

        composable(
            route = Routes.PRODUCT_EDIT,
            arguments = listOf(navArgument(Routes.PRODUCT_ID_ARG) { type = NavType.StringType }),
        ) {
            ProductEditScreen(onDone = { navController.popBackStack() })
        }

        composable(
            route = Routes.CUSTOMER_EDIT,
            arguments = listOf(navArgument(Routes.CUSTOMER_ID_ARG) { type = NavType.StringType }),
        ) {
            CustomerEditScreen(onDone = { navController.popBackStack() })
        }

        composable(
            route = Routes.INVOICE_DETAIL,
            arguments = listOf(navArgument(Routes.INVOICE_ID_ARG) { type = NavType.StringType }),
        ) {
            InvoiceDetailScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.NEW_SALE) {
            NewSaleScreen(
                onBack = { navController.popBackStack() },
                onAddCustomer = { navController.navigate(Routes.customerEdit(0)) },
                onInvoiceCreated = { id ->
                    navController.navigate(Routes.invoiceDetail(id)) {
                        popUpTo(Routes.NEW_SALE) { inclusive = true }
                    }
                },
            )
        }
    }
}

@Composable
private fun NewInvoiceFab(onClick: () -> Unit) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        icon = { Icon(Icons.Default.PointOfSale, contentDescription = null) },
        text = { Text(stringResource(R.string.new_invoice)) },
    )
}

private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
