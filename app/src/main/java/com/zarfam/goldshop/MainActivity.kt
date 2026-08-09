package com.zarfam.goldshop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.zarfam.goldshop.ui.screens.CustomersScreen
import com.zarfam.goldshop.ui.screens.DashboardScreen
import com.zarfam.goldshop.ui.screens.InvoiceDetailScreen
import com.zarfam.goldshop.ui.screens.InvoicesScreen
import com.zarfam.goldshop.ui.screens.NewInvoiceScreen
import com.zarfam.goldshop.ui.screens.ProductsScreen
import com.zarfam.goldshop.ui.screens.SettingsScreen
import com.zarfam.goldshop.ui.theme.ZarFamTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZarFamTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    ZarFamApp()
                }
            }
        }
    }
}

private data class TopDestination(
    val route: String,
    val title: String,
    val icon: ImageVector,
)

private val topDestinations = listOf(
    TopDestination("dashboard", "خانه", Icons.Default.Home),
    TopDestination("invoices", "فاکتورها", Icons.AutoMirrored.Filled.ReceiptLong),
    TopDestination("products", "محصولات", Icons.Default.Diamond),
    TopDestination("customers", "مشتریان", Icons.Default.People),
    TopDestination("settings", "تنظیمات", Icons.Default.Settings),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZarFamApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val isTopLevel = topDestinations.any { it.route == currentRoute }

    Scaffold(
        topBar = {
            if (isTopLevel) {
                TopAppBar(
                    title = {
                        Text(
                            topDestinations.first { it.route == currentRoute }.let {
                                if (it.route == "dashboard") "زرفام — طلا و جواهر" else it.title
                            },
                            fontWeight = FontWeight.Bold,
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
                )
            }
        },
        bottomBar = {
            if (isTopLevel) {
                NavigationBar {
                    topDestinations.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(destination.icon, contentDescription = destination.title) },
                            label = { Text(destination.title) },
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
                    onNewInvoice = { navController.navigate("invoice/new") },
                    onOpenInvoice = { navController.navigate("invoice/$it") },
                    onOpenProducts = {
                        navController.navigate("products") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
            composable("invoices") {
                InvoicesScreen(
                    onNewInvoice = { navController.navigate("invoice/new") },
                    onOpenInvoice = { navController.navigate("invoice/$it") },
                )
            }
            composable("products") { ProductsScreen() }
            composable("customers") { CustomersScreen() }
            composable("settings") { SettingsScreen() }
            composable("invoice/new") {
                NewInvoiceScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = { id ->
                        navController.navigate("invoice/$id") {
                            popUpTo("invoice/new") { inclusive = true }
                        }
                    },
                )
            }
            composable(
                "invoice/{invoiceId}",
                arguments = listOf(navArgument("invoiceId") { type = NavType.LongType }),
            ) { entry ->
                val invoiceId = entry.arguments?.getLong("invoiceId") ?: 0L
                InvoiceDetailScreen(
                    invoiceId = invoiceId,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
