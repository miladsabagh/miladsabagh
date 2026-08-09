package ir.zarin.faktor.ui.sale

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.zarin.faktor.R
import ir.zarin.faktor.data.model.Customer
import ir.zarin.faktor.data.model.Product
import ir.zarin.faktor.domain.GoldPricing
import ir.zarin.faktor.domain.PricingContext
import ir.zarin.faktor.ui.common.EmptyState
import ir.zarin.faktor.ui.common.LocalDisplayFormat
import ir.zarin.faktor.ui.common.currencyLabel
import ir.zarin.faktor.ui.common.icon
import ir.zarin.faktor.ui.common.label
import ir.zarin.faktor.ui.products.toPriceLineInput

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductPickerSheet(
    products: List<Product>,
    pricingContext: PricingContext,
    onSelect: (Product) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val format = LocalDisplayFormat.current
    var query by remember { mutableStateOf("") }
    val filtered = remember(products, query) {
        if (query.isBlank()) {
            products
        } else {
            products.filter { it.name.contains(query, true) || it.code.contains(query, true) }
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = stringResource(R.string.from_inventory),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.search_products)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
            )
            if (filtered.isEmpty()) {
                EmptyState(icon = Icons.Default.Diamond, text = stringResource(R.string.empty_products))
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                items(filtered, key = { it.id }) { product ->
                    val price = GoldPricing.calculate(product.toPriceLineInput(), pricingContext).totalRial
                    ListItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(product) },
                        headlineContent = { Text(product.name) },
                        supportingContent = {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(product.category.label(), style = MaterialTheme.typography.labelSmall)
                                Text(
                                    text = "${format.money(price)} ${currencyLabel()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        },
                        leadingContent = { Icon(product.category.icon, contentDescription = null) },
                    )
                    HorizontalDivider()
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerPickerSheet(
    customers: List<Customer>,
    onSelect: (Customer?) -> Unit,
    onAddCustomer: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val format = LocalDisplayFormat.current
    var query by remember { mutableStateOf("") }
    val filtered = remember(customers, query) {
        if (query.isBlank()) {
            customers
        } else {
            customers.filter { it.name.contains(query, true) || it.phone.contains(query) }
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.select_customer),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                androidx.compose.material3.TextButton(onClick = onAddCustomer) {
                    Text(stringResource(R.string.add_customer))
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.search_customers)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                item {
                    ListItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(null) },
                        headlineContent = { Text(stringResource(R.string.walk_in_customer)) },
                        leadingContent = { Icon(Icons.Default.PersonOff, contentDescription = null) },
                    )
                    HorizontalDivider()
                }
                if (filtered.isEmpty()) {
                    item {
                        EmptyState(
                            icon = Icons.Default.Groups,
                            text = stringResource(R.string.empty_customers),
                        )
                    }
                }
                items(filtered, key = { it.id }) { customer ->
                    ListItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(customer) },
                        headlineContent = { Text(customer.name) },
                        supportingContent = { Text(format.code(customer.phone)) },
                    )
                    HorizontalDivider()
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
