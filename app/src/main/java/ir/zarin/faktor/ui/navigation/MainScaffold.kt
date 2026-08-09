package ir.zarin.faktor.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import ir.zarin.faktor.R
import ir.zarin.faktor.ui.common.ZarinTopBar

data class MainTab(val route: String, val icon: ImageVector, val labelRes: Int)

val MAIN_TABS = listOf(
    MainTab(Routes.DASHBOARD, Icons.Default.Home, R.string.nav_dashboard),
    MainTab(Routes.PRODUCTS, Icons.Default.Diamond, R.string.nav_products),
    MainTab(Routes.INVOICES, Icons.AutoMirrored.Filled.ReceiptLong, R.string.nav_invoices),
    MainTab(Routes.SETTINGS, Icons.Default.Settings, R.string.nav_settings),
)

/** قاب اصلی برنامه: نوار بالا، نوار ناوبری پایین و دکمه شناور. */
@Composable
fun MainScaffold(
    title: String,
    selectedRoute: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (Modifier) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = { ZarinTopBar(title = title) },
        bottomBar = {
            NavigationBar {
                MAIN_TABS.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedRoute == tab.route,
                        onClick = { onTabSelected(tab.route) },
                        icon = { Icon(tab.icon, contentDescription = stringResource(tab.labelRes)) },
                        label = { Text(stringResource(tab.labelRes)) },
                    )
                }
            }
        },
        floatingActionButton = floatingActionButton,
    ) { padding ->
        content(
            Modifier
                .fillMaxSize()
                .padding(padding),
        )
    }
}
