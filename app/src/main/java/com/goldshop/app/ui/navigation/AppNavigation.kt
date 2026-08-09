package com.goldshop.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.goldshop.app.ui.ShopViewModel
import com.goldshop.app.ui.screens.customers.CustomersScreen
import com.goldshop.app.ui.screens.home.HomeScreen
import com.goldshop.app.ui.screens.invoice.InvoiceHubScreen
import com.goldshop.app.ui.screens.products.ProductsScreen
import com.goldshop.app.ui.screens.settings.SettingsScreen
import com.goldshop.app.ui.theme.Gold
import com.goldshop.app.ui.theme.Ink

private enum class Dest(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    Home("home", "خانه", Icons.Default.Home),
    Products("products", "محصولات", Icons.Default.Storefront),
    Invoice("invoice", "فاکتور", Icons.AutoMirrored.Filled.ReceiptLong),
    Customers("customers", "مشتریان", Icons.Default.People),
    Settings("settings", "تنظیمات", Icons.Default.Settings)
}

@Composable
fun GoldShopNavHost(viewModel: ShopViewModel) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    val products by viewModel.products.collectAsStateWithLifecycle()
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val invoices by viewModel.invoices.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val dashboard by viewModel.dashboard.collectAsStateWithLifecycle()
    val draft by viewModel.draft.collectAsStateWithLifecycle()
    val selectedInvoice by viewModel.selectedInvoice.collectAsStateWithLifecycle()
    val totals by viewModel.cartTotals.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                Dest.entries.forEach { dest ->
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
                        label = { Text(dest.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Ink,
                            selectedTextColor = MaterialTheme.colorScheme.onBackground,
                            indicatorColor = Gold,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Dest.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Dest.Home.route) {
                HomeScreen(
                    settings = settings,
                    stats = dashboard,
                    recentInvoices = invoices,
                    onNewInvoice = {
                        navController.navigate(Dest.Invoice.route)
                    },
                    onOpenInvoice = { id ->
                        viewModel.loadInvoice(id)
                        navController.navigate(Dest.Invoice.route)
                    },
                    onOpenProducts = {
                        navController.navigate(Dest.Products.route)
                    }
                )
            }
            composable(Dest.Products.route) {
                ProductsScreen(
                    products = products,
                    goldPricePerGram18 = settings.goldPricePerGram18,
                    onSave = viewModel::saveProduct,
                    onDelete = viewModel::deleteProduct,
                    onAddToInvoice = { product ->
                        viewModel.addToCart(product)
                        navController.navigate(Dest.Invoice.route)
                    }
                )
            }
            composable(Dest.Invoice.route) {
                InvoiceHubScreen(
                    products = products,
                    customers = customers,
                    invoices = invoices,
                    draft = draft,
                    goldPrice = settings.goldPricePerGram18,
                    subtotal = totals.first,
                    tax = totals.second,
                    total = totals.third,
                    selectedInvoice = selectedInvoice,
                    onAddProduct = viewModel::addToCart,
                    onRemoveProduct = viewModel::removeFromCart,
                    onChangeQty = viewModel::changeCartQty,
                    onSelectCustomer = viewModel::selectCustomer,
                    onUpdateDraft = { name, phone, discount, notes, paid ->
                        viewModel.updateDraft(name, phone, discount, notes, paid)
                    },
                    onIssue = { done -> viewModel.issueInvoice(done) },
                    onOpenInvoice = viewModel::loadInvoice,
                    onClearSelected = viewModel::clearSelectedInvoice,
                    onExportPdf = viewModel::exportInvoicePdf,
                    onClearDraft = viewModel::clearDraft
                )
            }
            composable(Dest.Customers.route) {
                CustomersScreen(
                    customers = customers,
                    onSave = viewModel::saveCustomer,
                    onDelete = viewModel::deleteCustomer,
                    onSelectForInvoice = { customer ->
                        viewModel.selectCustomer(customer)
                        navController.navigate(Dest.Invoice.route)
                    }
                )
            }
            composable(Dest.Settings.route) {
                SettingsScreen(
                    settings = settings,
                    onSave = viewModel::updateSettings
                )
            }
        }
    }
}
