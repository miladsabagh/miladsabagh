package ir.zarin.faktor.ui.invoices

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.zarin.faktor.R
import ir.zarin.faktor.ui.common.EmptyState

@Composable
fun InvoicesScreen(
    onOpenInvoice: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InvoicesViewModel = viewModel(factory = InvoicesViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    InvoicesContent(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onOpenInvoice = onOpenInvoice,
        modifier = modifier,
    )
}

@Composable
fun InvoicesContent(
    state: InvoicesUiState,
    onQueryChange: (String) -> Unit,
    onOpenInvoice: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
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
                placeholder = { Text(stringResource(R.string.search_invoices)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
            )
        }

        if (state.invoices.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    text = stringResource(R.string.empty_invoices),
                )
            }
        }

        items(state.invoices, key = { it.id }) { invoice ->
            InvoiceRow(invoice = invoice, onClick = { onOpenInvoice(invoice.id) })
        }
    }
}
