package com.zarnegar.gold.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.zarnegar.gold.ui.nav.Routes
import com.zarnegar.gold.ui.screens.CustomersScreen
import com.zarnegar.gold.ui.screens.DashboardScreen
import com.zarnegar.gold.ui.screens.InvoiceDetailScreen
import com.zarnegar.gold.ui.screens.InvoicesScreen
import com.zarnegar.gold.ui.screens.ProductEditScreen
import com.zarnegar.gold.ui.screens.ProductsScreen
import com.zarnegar.gold.ui.screens.SaleScreen
import com.zarnegar.gold.ui.screens.SettingsScreen
import com.zarnegar.gold.ui.vm.CustomersViewModel
import com.zarnegar.gold.ui.vm.DashboardViewModel
import com.zarnegar.gold.ui.vm.InvoicesViewModel
import com.zarnegar.gold.ui.vm.ProductsViewModel
import com.zarnegar.gold.ui.vm.SaleViewModel
import com.zarnegar.gold.ui.vm.SettingsViewModel
import com.zarnegar.gold.ui.vm.ZarnegarViewModels

@Composable
fun ZarnegarNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    // سبد فروش بین صفحهٔ کالاها و صفحهٔ فروش مشترک است، پس در دامنهٔ اکتیویتی نگه داشته می‌شود.
    val saleViewModel: SaleViewModel = viewModel(factory = ZarnegarViewModels.sale)

    NavHost(
        navController = navController,
        startDestination = Routes.DASHBOARD,
        modifier = modifier,
    ) {
        composable(Routes.DASHBOARD) {
            val viewModel: DashboardViewModel = viewModel(factory = ZarnegarViewModels.dashboard)
            DashboardScreen(
                viewModel = viewModel,
                onNewSale = { navController.navigate(Routes.SALE) },
                onOpenProducts = { navController.navigate(Routes.PRODUCTS) },
                onOpenInvoices = { navController.navigate(Routes.INVOICES) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onOpenInvoice = { navController.navigate(Routes.invoiceDetail(it)) },
            )
        }

        composable(Routes.PRODUCTS) {
            val productsViewModel: ProductsViewModel =
                viewModel(factory = ZarnegarViewModels.products)
            ProductsScreen(
                viewModel = productsViewModel,
                saleViewModel = saleViewModel,
                onAddProduct = { navController.navigate(Routes.productEdit(0)) },
                onEditProduct = { id -> navController.navigate(Routes.productEdit(id)) },
                onOpenSale = { navController.navigate(Routes.SALE) },
            )
        }

        composable(
            route = "${Routes.PRODUCT_EDIT}/{id}",
            arguments = listOf(navArgument("id") { type = NavType.LongType }),
        ) { entry ->
            val id = entry.arguments?.getLong("id") ?: 0L
            val viewModel: ProductsViewModel = viewModel(factory = ZarnegarViewModels.products)
            ProductEditScreen(
                productId = id,
                viewModel = viewModel,
                onDone = { navController.popBackStack() },
            )
        }

        composable(Routes.CUSTOMERS) {
            val viewModel: CustomersViewModel = viewModel(factory = ZarnegarViewModels.customers)
            CustomersScreen(viewModel = viewModel)
        }

        composable(Routes.SALE) {
            SaleScreen(
                viewModel = saleViewModel,
                onInvoiceCreated = { id ->
                    navController.navigate(Routes.invoiceDetail(id)) {
                        popUpTo(Routes.SALE) { inclusive = false }
                    }
                },
            )
        }

        composable(Routes.INVOICES) {
            val viewModel: InvoicesViewModel = viewModel(factory = ZarnegarViewModels.invoices)
            InvoicesScreen(
                viewModel = viewModel,
                onOpenInvoice = { navController.navigate(Routes.invoiceDetail(it)) },
                onNewSale = { navController.navigate(Routes.SALE) },
            )
        }

        composable(
            route = "${Routes.INVOICE_DETAIL}/{id}",
            arguments = listOf(navArgument("id") { type = NavType.LongType }),
        ) { entry ->
            val id = entry.arguments?.getLong("id") ?: 0L
            val invoicesViewModel: InvoicesViewModel =
                viewModel(factory = ZarnegarViewModels.invoices)
            val settingsViewModel: SettingsViewModel =
                viewModel(factory = ZarnegarViewModels.settings)
            InvoiceDetailScreen(
                invoiceId = id,
                viewModel = invoicesViewModel,
                settingsViewModel = settingsViewModel,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.SETTINGS) {
            val viewModel: SettingsViewModel = viewModel(factory = ZarnegarViewModels.settings)
            SettingsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
    }
}
