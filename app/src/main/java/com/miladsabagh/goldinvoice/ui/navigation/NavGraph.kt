package com.miladsabagh.goldinvoice.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Style
import androidx.compose.ui.graphics.vector.ImageVector
import com.miladsabagh.goldinvoice.R

object Routes {
    const val DASHBOARD = "dashboard"
    const val INVOICES = "invoices"
    const val INVOICE_NEW = "invoices/new"
    const val INVOICE_DETAIL = "invoices/{invoiceId}"
    const val PRODUCTS = "products"
    const val PRODUCT_NEW = "products/new"
    const val PRODUCT_EDIT = "products/{productId}/edit"
    const val CUSTOMERS = "customers"
    const val CUSTOMER_NEW = "customers/new"
    const val CUSTOMER_EDIT = "customers/{customerId}/edit"
    const val SETTINGS = "settings"

    fun invoiceDetail(id: Long) = "invoices/$id"
    fun productEdit(id: Long) = "products/$id/edit"
    fun customerEdit(id: Long) = "customers/$id/edit"
}

data class BottomTab(val route: String, val labelRes: Int, val icon: ImageVector)

val bottomTabs = listOf(
    BottomTab(Routes.DASHBOARD, R.string.nav_dashboard, Icons.Filled.Dashboard),
    BottomTab(Routes.INVOICES, R.string.nav_invoices, Icons.AutoMirrored.Filled.ReceiptLong),
    BottomTab(Routes.PRODUCTS, R.string.nav_products, Icons.Filled.Style),
    BottomTab(Routes.CUSTOMERS, R.string.nav_customers, Icons.Filled.Group),
    BottomTab(Routes.SETTINGS, R.string.nav_settings, Icons.Filled.Settings)
)
