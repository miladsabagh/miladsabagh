package ir.zarin.faktor.ui.products

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.zarin.faktor.R
import ir.zarin.faktor.data.model.PricingMode
import ir.zarin.faktor.data.model.Product
import ir.zarin.faktor.domain.GoldPricing
import ir.zarin.faktor.domain.PriceLineInput
import ir.zarin.faktor.domain.PricingContext
import ir.zarin.faktor.ui.common.ConfirmDialog
import ir.zarin.faktor.ui.common.EmptyState
import ir.zarin.faktor.ui.common.LocalDisplayFormat
import ir.zarin.faktor.ui.common.StatusChip
import ir.zarin.faktor.ui.common.currencyLabel
import ir.zarin.faktor.ui.common.icon
import ir.zarin.faktor.ui.common.label
import ir.zarin.faktor.ui.theme.PartialAmber

@Composable
fun ProductsScreen(
    onEditProduct: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProductsViewModel = viewModel(factory = ProductsViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ProductsContent(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onEditProduct = onEditProduct,
        onDeleteProduct = viewModel::delete,
        modifier = modifier,
    )
}

@Composable
fun ProductsContent(
    state: ProductsUiState,
    onQueryChange: (String) -> Unit,
    onEditProduct: (Long) -> Unit,
    onDeleteProduct: (Product) -> Unit,
    modifier: Modifier = Modifier,
) {
    var pendingDelete by remember { mutableStateOf<Product?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.search_products)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
            )
        }

        if (state.products.isEmpty()) {
            item {
                EmptyState(icon = Icons.Default.Diamond, text = stringResource(R.string.empty_products))
            }
        }

        items(state.products, key = { it.id }) { product ->
            ProductCard(
                product = product,
                pricingContext = state.pricingContext,
                onClick = { onEditProduct(product.id) },
                onDelete = { pendingDelete = product },
            )
        }
    }

    pendingDelete?.let { product ->
        ConfirmDialog(
            text = stringResource(R.string.delete_product_confirm),
            onConfirm = {
                onDeleteProduct(product)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null },
            confirmLabel = stringResource(R.string.delete),
        )
    }
}

@Composable
private fun ProductCard(
    product: Product,
    pricingContext: PricingContext,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val format = LocalDisplayFormat.current
    val unitPrice = remember(product, pricingContext) {
        GoldPricing.calculate(product.toPriceLineInput(quantity = 1), pricingContext).totalRial
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Icon(
                    imageVector = product.category.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(22.dp),
                )
            }
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = productSubtitle(product),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "${format.money(unitPrice)} ${currencyLabel()}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                if (product.stockQty <= 0) {
                    StatusChip(text = stringResource(R.string.out_of_stock), color = PartialAmber)
                } else {
                    StatusChip(
                        text = stringResource(R.string.in_stock, format.count(product.stockQty)),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Composable
private fun productSubtitle(product: Product): String {
    val format = LocalDisplayFormat.current
    val parts = buildList {
        add(product.category.label())
        if (product.code.isNotBlank()) add(format.code(product.code))
        if (product.pricingMode == PricingMode.BY_WEIGHT) {
            add("${format.weight(product.weightGrams)} ${stringResource(R.string.gram)}")
            add("${stringResource(R.string.karat)} ${format.count(product.karat)}")
            add("${stringResource(R.string.wage)} ${format.percent(product.wagePercent)}٪")
        } else {
            add(stringResource(R.string.pricing_fixed))
        }
    }
    return parts.joinToString(" • ")
}

/** ورودی محاسبه قیمت برای یک کالای انبار. */
fun Product.toPriceLineInput(quantity: Int = 1, discountRial: Long = 0): PriceLineInput = PriceLineInput(
    pricingMode = pricingMode,
    quantity = quantity,
    weightGrams = weightGrams,
    karat = karat,
    wagePercent = wagePercent,
    stonePriceRial = stonePriceRial,
    unitFixedPriceRial = fixedPriceRial,
    applyVat = applyVat,
    discountRial = discountRial,
)
