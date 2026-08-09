package com.zarin.goldshop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.zarin.goldshop.ui.AppViewModel
import com.zarin.goldshop.ui.screens.CustomersScreen
import com.zarin.goldshop.ui.screens.DashboardScreen
import com.zarin.goldshop.ui.screens.InventoryScreen
import com.zarin.goldshop.ui.screens.InvoiceDetailScreen
import com.zarin.goldshop.ui.screens.InvoicesScreen
import com.zarin.goldshop.ui.screens.NewInvoiceScreen
import com.zarin.goldshop.ui.screens.SettingsScreen
import com.zarin.goldshop.ui.theme.GoldShopTheme

class MainActivity : ComponentActivity() {

    private val vm: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GoldShopTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    AppRoot(vm)
                }
            }
        }
    }
}

private data class TopDestination(val route: String, val label: String, val icon: ImageVector)

private val bottomDestinations = listOf(
    TopDestination("dashboard", "خانه", Icons.Filled.Home),
    TopDestination("inventory", "محصولات", Icons.Filled.Inventory2),
    TopDestination("new_invoice", "فاکتور جدید", Icons.Filled.Add),
    TopDestination("invoices", "فاکتورها", Icons.AutoMirrored.Filled.ReceiptLong),
    TopDestination("settings", "تنظیمات", Icons.Filled.Settings),
)

private fun titleFor(route: String?): String = when {
    route == null -> "زرین طلا"
    route.startsWith("invoice_detail") -> "جزئیات فاکتور"
    route == "customers" -> "مشتریان"
    else -> bottomDestinations.firstOrNull { it.route == route }?.label ?: "زرین طلا"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppRoot(vm: AppViewModel) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBackArrow = currentRoute?.startsWith("invoice_detail") == true || currentRoute == "customers"
    val showBottomBar = bottomDestinations.any { it.route == currentRoute }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titleFor(currentRoute)) },
                navigationIcon = {
                    if (showBackArrow) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(),
            )
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomDestinations.forEach { dest ->
                        NavigationBarItem(
                            selected = currentRoute == dest.route,
                            onClick = {
                                if (currentRoute != dest.route) {
                                    navController.navigate(dest.route) {
                                        popUpTo("dashboard") { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(dest.icon, contentDescription = dest.label) },
                            label = { Text(dest.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(padding),
        ) {
            composable("dashboard") {
                DashboardScreen(
                    vm = vm,
                    onNewInvoice = { navController.navigate("new_invoice") },
                    onInventory = { navController.navigate("inventory") },
                    onInvoices = { navController.navigate("invoices") },
                    onCustomers = { navController.navigate("customers") },
                )
            }
            composable("inventory") { InventoryScreen(vm) }
            composable("new_invoice") {
                NewInvoiceScreen(vm) { id ->
                    navController.navigate("invoice_detail/$id") {
                        popUpTo("new_invoice") { inclusive = true }
                    }
                }
            }
            composable("invoices") {
                InvoicesScreen(vm) { id -> navController.navigate("invoice_detail/$id") }
            }
            composable("settings") { SettingsScreen(vm) }
            composable("customers") { CustomersScreen(vm) }
            composable(
                route = "invoice_detail/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType }),
            ) { entry ->
                val id = entry.arguments?.getLong("id") ?: 0L
                InvoiceDetailScreen(vm, id, onDeleted = { navController.popBackStack() })
            }
        }
    }
}
