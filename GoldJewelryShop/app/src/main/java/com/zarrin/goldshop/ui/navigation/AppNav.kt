package com.zarrin.goldshop.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.zarrin.goldshop.ui.ShopViewModel
import com.zarrin.goldshop.ui.screens.CreateInvoiceScreen
import com.zarrin.goldshop.ui.screens.CustomersScreen
import com.zarrin.goldshop.ui.screens.DashboardScreen
import com.zarrin.goldshop.ui.screens.InvoiceDetailScreen
import com.zarrin.goldshop.ui.screens.InvoicesScreen
import com.zarrin.goldshop.ui.screens.ProductsScreen
import com.zarrin.goldshop.ui.screens.SettingsScreen

object Routes {
    const val DASHBOARD = "dashboard"
    const val PRODUCTS = "products"
    const val CUSTOMERS = "customers"
    const val INVOICES = "invoices"
    const val CREATE_INVOICE = "create_invoice"
    const val SETTINGS = "settings"
    const val INVOICE_DETAIL = "invoice/{id}"
    fun invoiceDetail(id: Long) = "invoice/$id"
}

@Composable
fun AppNav(viewModel: ShopViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.DASHBOARD) {
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                viewModel = viewModel,
                onNewInvoice = { navController.navigate(Routes.CREATE_INVOICE) },
                onProducts = { navController.navigate(Routes.PRODUCTS) },
                onCustomers = { navController.navigate(Routes.CUSTOMERS) },
                onInvoices = { navController.navigate(Routes.INVOICES) },
                onSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(Routes.PRODUCTS) {
            ProductsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable(Routes.CUSTOMERS) {
            CustomersScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable(Routes.INVOICES) {
            InvoicesScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onOpenInvoice = { id -> navController.navigate(Routes.invoiceDetail(id)) }
            )
        }
        composable(Routes.CREATE_INVOICE) {
            CreateInvoiceScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onInvoiceCreated = { id ->
                    navController.navigate(Routes.invoiceDetail(id)) {
                        popUpTo(Routes.DASHBOARD)
                    }
                }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable(
            route = Routes.INVOICE_DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { entry ->
            val id = entry.arguments?.getLong("id") ?: return@composable
            InvoiceDetailScreen(
                invoiceId = id,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
