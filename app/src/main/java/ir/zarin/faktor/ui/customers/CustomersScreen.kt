package ir.zarin.faktor.ui.customers

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
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
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
import ir.zarin.faktor.data.model.Customer
import ir.zarin.faktor.ui.common.ConfirmDialog
import ir.zarin.faktor.ui.common.EmptyState
import ir.zarin.faktor.ui.common.LocalDisplayFormat

@Composable
fun CustomersScreen(
    onEditCustomer: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CustomersViewModel = viewModel(factory = CustomersViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CustomersContent(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onEditCustomer = onEditCustomer,
        onDeleteCustomer = viewModel::delete,
        modifier = modifier,
    )
}

@Composable
fun CustomersContent(
    state: CustomersUiState,
    onQueryChange: (String) -> Unit,
    onEditCustomer: (Long) -> Unit,
    onDeleteCustomer: (Customer) -> Unit,
    modifier: Modifier = Modifier,
) {
    var pendingDelete by remember { mutableStateOf<Customer?>(null) }
    val format = LocalDisplayFormat.current

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.search_customers)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
            )
        }

        if (state.customers.isEmpty()) {
            item {
                EmptyState(icon = Icons.Default.Groups, text = stringResource(R.string.empty_customers))
            }
        }

        items(state.customers, key = { it.id }) { customer ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onEditCustomer(customer.id) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier
                                .padding(10.dp)
                                .size(22.dp),
                        )
                    }
                    Spacer(Modifier.size(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = customer.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        if (customer.phone.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = format.code(customer.phone),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    IconButton(onClick = { pendingDelete = customer }) {
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

    pendingDelete?.let { customer ->
        ConfirmDialog(
            text = stringResource(R.string.delete_customer_confirm),
            onConfirm = {
                onDeleteCustomer(customer)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null },
            confirmLabel = stringResource(R.string.delete),
        )
    }
}
