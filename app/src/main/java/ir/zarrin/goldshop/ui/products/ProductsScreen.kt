package ir.zarrin.goldshop.ui.products

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.zarrin.goldshop.data.db.ProductEntity
import ir.zarrin.goldshop.data.settings.AppSettings
import ir.zarrin.goldshop.domain.PriceInput
import ir.zarrin.goldshop.domain.PricingEngine
import ir.zarrin.goldshop.domain.PricingMode
import ir.zarrin.goldshop.domain.ProductCategory
import ir.zarrin.goldshop.ui.LocalAppContainer
import ir.zarrin.goldshop.ui.components.ChipRow
import ir.zarrin.goldshop.ui.components.EmptyState
import ir.zarrin.goldshop.ui.components.SearchField
import ir.zarrin.goldshop.ui.components.StatusPill
import ir.zarrin.goldshop.ui.theme.SuccessGreen
import ir.zarrin.goldshop.ui.theme.WarningAmber
import ir.zarrin.goldshop.util.formatPercent
import ir.zarrin.goldshop.util.formatWeight
import ir.zarrin.goldshop.util.toPersianDigits

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    onAddProduct: () -> Unit,
    onEditProduct: (Long) -> Unit
) {
    val container = LocalAppContainer.current
    val viewModel: ProductsViewModel = viewModel {
        ProductsViewModel(container.productRepository, container.settingsRepository)
    }
    val state by viewModel.state.collectAsState()
    var pendingDelete by remember { mutableStateOf<ProductEntity?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("کالاها و موجودی") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddProduct,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "کالای جدید")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                SearchField(
                    value = state.query,
                    onValueChange = viewModel::onQueryChange,
                    placeholder = "جستجوی نام یا کد کالا"
                )
                Spacer(Modifier.height(10.dp))
                ChipRow(
                    items = ProductCategory.entries.toList(),
                    selected = state.category,
                    labelOf = { it.label },
                    onSelect = viewModel::onCategoryChange
                )
            }

            if (state.products.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Diamond,
                    title = "کالایی یافت نشد",
                    message = "برای افزودن کالای جدید روی دکمه + بزنید."
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.products, key = { it.id }) { product ->
                        ProductRow(
                            product = product,
                            settings = state.settings,
                            onClick = { onEditProduct(product.id) },
                            onDelete = { pendingDelete = product }
                        )
                    }
                    item { Spacer(Modifier.height(72.dp)) }
                }
            }
        }
    }

    pendingDelete?.let { product ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("حذف کالا") },
            text = { Text("آیا «${product.name}» حذف شود؟ این عمل قابل بازگشت نیست.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(product)
                    pendingDelete = null
                }) { Text("حذف", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("انصراف") }
            }
        )
    }
}

@Composable
private fun ProductRow(
    product: ProductEntity,
    settings: AppSettings,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val mode = PricingMode.fromName(product.pricingMode)
    val price = PricingEngine.calculate(
        PriceInput(
            pricingMode = mode,
            weightGrams = product.weightGrams,
            karat = product.karat,
            baseRatePerGram = settings.goldRate18,
            wagePercent = product.wagePercent,
            profitPercent = product.profitPercent,
            stonePrice = product.stonePrice,
            fixedPrice = product.fixedPrice,
            taxPercent = settings.taxPercent,
            taxable = product.taxable,
            quantity = 1
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Diamond,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(4.dp))
                Text(
                    buildString {
                        append(ProductCategory.fromName(product.category).label)
                        append(" • کد ")
                        append(product.code.toPersianDigits())
                        if (mode == PricingMode.BY_WEIGHT) {
                            append(" • ")
                            append(product.karat.toPersianDigits())
                            append(" عیار • ")
                            append(product.weightGrams.formatWeight())
                            append(" گرم")
                        } else {
                            append(" • قیمت مقطوع")
                        }
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (mode == PricingMode.BY_WEIGHT) {
                    Spacer(Modifier.height(3.dp))
                    Text(
                        "اجرت ${product.wagePercent.formatPercent()} • سود ${product.profitPercent.formatPercent()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    settings.money(price.unitPrice),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                StatusPill(
                    text = "موجودی ${product.stockQty.toPersianDigits()}",
                    color = if (product.stockQty > 0) SuccessGreen else WarningAmber
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "حذف",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
