package com.goldjewelry.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Dashboard : Screen("dashboard", "داشبورد", Icons.Default.Dashboard)
    data object Products : Screen("products", "محصولات", Icons.Default.Diamond)
    data object Sales : Screen("sales", "فروش", Icons.Default.ShoppingCart)
    data object Invoices : Screen("invoices", "فاکتورها", Icons.Default.Receipt)
    data object Customers : Screen("customers", "مشتریان", Icons.Default.People)
    data object Settings : Screen("settings", "تنظیمات", Icons.Default.Settings)

    data object InvoiceDetail : Screen("invoice/{invoiceId}", "جزئیات فاکتور", Icons.Default.Receipt) {
        fun createRoute(invoiceId: Long) = "invoice/$invoiceId"
    }

    data object NewProduct : Screen("product/new", "محصول جدید", Icons.Default.Diamond)
    data object NewCustomer : Screen("customer/new", "مشتری جدید", Icons.Default.People)
}

val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Products,
    Screen.Sales,
    Screen.Invoices,
    Screen.Customers
)
