package com.zarnegar.gold.ui.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.ui.graphics.vector.ImageVector

object Routes {
    const val DASHBOARD = "dashboard"
    const val PRODUCTS = "products"
    const val PRODUCT_EDIT = "product_edit"
    const val CUSTOMERS = "customers"
    const val SALE = "sale"
    const val INVOICES = "invoices"
    const val INVOICE_DETAIL = "invoice"
    const val SETTINGS = "settings"

    fun productEdit(id: Long) = "$PRODUCT_EDIT/$id"
    fun invoiceDetail(id: Long) = "$INVOICE_DETAIL/$id"
}

data class BottomTab(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

val bottomTabs = listOf(
    BottomTab(Routes.DASHBOARD, "خانه", Icons.Filled.Home),
    BottomTab(Routes.PRODUCTS, "کالاها", Icons.Filled.Diamond),
    BottomTab(Routes.SALE, "فروش", Icons.Filled.PointOfSale),
    BottomTab(Routes.INVOICES, "فاکتورها", Icons.AutoMirrored.Filled.ReceiptLong),
    BottomTab(Routes.CUSTOMERS, "مشتریان", Icons.Filled.Groups),
)
