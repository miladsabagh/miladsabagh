package com.zarin.gold.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Diamond
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.zarin.gold.data.Invoice
import com.zarin.gold.data.ZarinRepository
import com.zarin.gold.ui.ZarinViewModel
import com.zarin.gold.ui.screens.CatalogScreen
import com.zarin.gold.ui.screens.HistoryScreen
import com.zarin.gold.ui.screens.HomeScreen
import com.zarin.gold.ui.screens.InvoiceCreateScreen
import com.zarin.gold.ui.screens.InvoiceDetailScreen
import com.zarin.gold.ui.screens.SettingsScreen
import com.zarin.gold.ui.theme.ZarinColors
import kotlinx.coroutines.launch

private enum class Tab(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    Home("home", "خانه", Icons.Outlined.Home),
    Catalog("catalog", "کالاها", Icons.Outlined.Diamond),
    Invoice("invoice", "فاکتور", Icons.Outlined.ReceiptLong),
    History("history", "سوابق", Icons.Outlined.History),
    Settings("settings", "نرخ", Icons.Outlined.Settings)
}

@Composable
fun ZarinAppRoot(repository: ZarinRepository) {
    val vm: ZarinViewModel = viewModel(factory = ZarinViewModel.factory(repository))
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBottomBar = Tab.entries.any { it.route == currentRoute }

    Scaffold(
        containerColor = ZarinColors.Night,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = ZarinColors.NightElevated.copy(alpha = 0.96f),
                    tonalElevation = 0.dp
                ) {
                    Tab.entries.forEach { tab ->
                        val selected = currentRoute == tab.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ZarinColors.Night,
                                selectedTextColor = ZarinColors.AmberSoft,
                                indicatorColor = ZarinColors.Amber,
                                unselectedIconColor = ZarinColors.IvoryMuted,
                                unselectedTextColor = ZarinColors.IvoryMuted
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Tab.Home.route,
            modifier = Modifier.padding(padding),
            enterTransition = {
                fadeIn(tween(280)) + slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    tween(280)
                )
            },
            exitTransition = { fadeOut(tween(180)) },
            popEnterTransition = {
                fadeIn(tween(280)) + slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.End,
                    tween(280)
                )
            },
            popExitTransition = { fadeOut(tween(180)) }
        ) {
            composable(Tab.Home.route) {
                val home by vm.homeState.collectAsState()
                HomeScreen(
                    state = home,
                    onOpenCatalog = {
                        navController.navigate(Tab.Catalog.route) {
                            launchSingleTop = true
                        }
                    },
                    onCreateInvoice = {
                        navController.navigate(Tab.Invoice.route) {
                            launchSingleTop = true
                        }
                    },
                    onOpenHistory = {
                        navController.navigate(Tab.History.route) {
                            launchSingleTop = true
                        }
                    },
                    onOpenInvoice = { id ->
                        navController.navigate("invoice/$id")
                    }
                )
            }

            composable(Tab.Catalog.route) {
                val catalog by vm.catalog.collectAsState()
                val price by vm.goldPrice18.collectAsState()
                CatalogScreen(
                    state = catalog,
                    goldPrice18 = price,
                    onQueryChange = vm::setQuery,
                    onCategoryChange = vm::setCategory,
                    onAddToInvoice = { product ->
                        vm.addProductToDraft(product)
                        navController.navigate(Tab.Invoice.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Tab.Invoice.route) {
                val draft by vm.draft.collectAsState()
                val customers by vm.customers.collectAsState()
                LaunchedEffect(draft.lastCreatedId) {
                    draft.lastCreatedId?.let { id ->
                        vm.clearDraftCreatedFlag()
                        navController.navigate("invoice/$id")
                    }
                }
                InvoiceCreateScreen(
                    draft = draft,
                    customers = customers,
                    onCustomerChange = vm::updateDraftCustomer,
                    onSelectCustomer = vm::selectCustomer,
                    onDiscountChange = vm::updateDiscount,
                    onNoteChange = vm::updateNote,
                    onChangeQuantity = vm::changeQuantity,
                    onRemoveLine = vm::removeLine,
                    onOpenCatalog = {
                        navController.navigate(Tab.Catalog.route) {
                            launchSingleTop = true
                        }
                    },
                    onSubmit = { vm.submitInvoice { } }
                )
            }

            composable(Tab.History.route) {
                val invoices by vm.invoices.collectAsState()
                HistoryScreen(
                    invoices = invoices,
                    onOpenInvoice = { id -> navController.navigate("invoice/$id") }
                )
            }

            composable(Tab.Settings.route) {
                val price by vm.goldPrice18.collectAsState()
                SettingsScreen(
                    goldPrice18 = price,
                    onSavePrice = vm::updateGoldPrice
                )
            }

            composable(
                route = "invoice/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { entry ->
                val id = entry.arguments?.getLong("id") ?: return@composable
                var invoice by remember { mutableStateOf<Invoice?>(null) }
                val scope = rememberCoroutineScope()
                LaunchedEffect(id) {
                    scope.launch { invoice = vm.getInvoice(id) }
                }
                InvoiceDetailScreen(
                    invoice = invoice,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
