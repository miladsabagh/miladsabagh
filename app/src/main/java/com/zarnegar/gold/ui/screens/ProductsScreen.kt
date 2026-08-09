package com.zarnegar.gold.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zarnegar.gold.core.PersianText
import com.zarnegar.gold.domain.model.Karats
import com.zarnegar.gold.domain.model.PricingMode
import com.zarnegar.gold.domain.model.Product
import com.zarnegar.gold.domain.model.ProductCategory
import com.zarnegar.gold.domain.model.SaleItem
import com.zarnegar.gold.domain.model.ShopSettings
import com.zarnegar.gold.domain.pricing.GoldPricing
import com.zarnegar.gold.ui.components.EmptyState
import com.zarnegar.gold.ui.components.StatusChip
import com.zarnegar.gold.ui.vm.ProductsViewModel
import com.zarnegar.gold.ui.vm.SaleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    viewModel: ProductsViewModel,
    saleViewModel: SaleViewModel,
    onAddProduct: () -> Unit,
    onEditProduct: (Long) -> Unit,
    onOpenSale: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val saleState by saleViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingDelete by remember { mutableStateOf<Product?>(null) }
    val saleMessage by saleViewModel.message.collectAsStateWithLifecycle()

    LaunchedEffect(saleMessage) {
        saleMessage?.let {
            snackbarHostState.showSnackbar(it)
            saleViewModel.consumeMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("کالاها و موجودی") },
                actions = {
                    BadgedBox(
                        badge = {
                            if (saleState.cart.items.isNotEmpty()) {
                                Badge {
                                    Text(
                                        PersianText.formatNumber(
                                            saleState.cart.items.size.toLong(),
                                        ),
                                    )
                                }
                            }
                        },
                    ) {
                        IconButton(onClick = onOpenSale) {
                            Icon(Icons.Filled.ShoppingCart, contentDescription = "سبد فروش")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddProduct) {
                Icon(Icons.Filled.Add, contentDescription = "افزودن کالا")
            }
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                placeholder = { Text("جستجوی نام یا کد کالا") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = state.category == null,
                    onClick = { viewModel.onCategoryChange(null) },
                    label = { Text("همه") },
                )
                ProductCategory.entries.forEach { category ->
                    FilterChip(
                        selected = state.category == category,
                        onClick = { viewModel.onCategoryChange(category) },
                        label = { Text(category.label) },
                    )
                }
            }

            val products = state.filtered
            if (products.isEmpty()) {
                EmptyState(
                    icon = Icons.Filled.Diamond,
                    title = "کالایی یافت نشد",
                    message = "با دکمهٔ + کالای جدید اضافه کنید یا عبارت جستجو را تغییر دهید.",
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(products, key = { it.id }) { product ->
                        ProductCard(
                            product = product,
                            settings = state.settings,
                            onEdit = { onEditProduct(product.id) },
                            onDelete = { pendingDelete = product },
                            onAddToCart = { saleViewModel.addProduct(product) },
                        )
                    }
                }
            }
        }
    }

    pendingDelete?.let { product ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("حذف کالا") },
            text = { Text("«${product.name}» حذف شود؟") },
            confirmButton = {
                Button(onClick = {
                    viewModel.delete(product)
                    pendingDelete = null
                }) { Text("حذف") }
            },
            dismissButton = {
                androidx.compose.material3.OutlinedButton(onClick = { pendingDelete = null }) {
                    Text("انصراف")
                }
            },
        )
    }
}

@Composable
private fun ProductCard(
    product: Product,
    settings: ShopSettings,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddToCart: () -> Unit,
) {
    val breakdown = remember(product, settings) {
        GoldPricing.priceLine(SaleItem.fromProduct(product), settings.pricing())
    }
    val currency = settings.currencyLabel

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "کد ${PersianText.formatCode(product.code)} • " +
                            product.category.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                StatusChip(
                    text = "موجودی ${PersianText.formatNumber(product.stock.toLong())}",
                    container = if (product.stock > 1) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.errorContainer
                    },
                    content = if (product.stock > 1) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.onErrorContainer
                    },
                )
            }

            if (product.pricingMode == PricingMode.BY_WEIGHT) {
                Text(
                    text = "${PersianText.formatGrams(product.weightGrams)} • " +
                        Karats.label(product.karat),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Text(
                    text = "قیمت مقطوع",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "قیمت فروش با مالیات",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "${PersianText.formatNumber(breakdown.total)} $currency",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Row {
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "حذف",
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Filled.Edit, contentDescription = "ویرایش")
                    }
                    Button(onClick = onAddToCart, shape = RoundedCornerShape(12.dp)) {
                        Icon(Icons.Filled.AddShoppingCart, contentDescription = null)
                        Text("  افزودن")
                    }
                }
            }
        }
    }
}
