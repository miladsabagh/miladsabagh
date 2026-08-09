package com.goldgallery.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goldgallery.app.logic.JalaliDate
import com.goldgallery.app.logic.PersianFormat
import com.goldgallery.app.ui.theme.BronzeDeep
import com.goldgallery.app.ui.theme.GoldPale
import com.goldgallery.app.viewmodel.ShopViewModel

@Composable
fun InvoicesScreen(viewModel: ShopViewModel, onOpenInvoice: (Long) -> Unit) {
    val invoices by viewModel.invoices.collectAsStateWithLifecycle()

    if (invoices.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                Icons.Outlined.Description,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.outline,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "هنوز فاکتوری صادر نشده است",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Text(
                "فاکتورهای صادرشده",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        }
        items(invoices, key = { it.invoice.id }) { data ->
            val total = data.items.sumOf { it.total }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenInvoice(data.invoice.id) },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        color = GoldPale,
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text(
                            "№ ${PersianFormat.toPersianDigits(data.invoice.number.toString())}",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.titleSmall,
                            color = BronzeDeep,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(Modifier.size(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            data.invoice.customerName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            "${JalaliDate.format(data.invoice.createdAt)} • ${PersianFormat.toPersianDigits(data.items.size.toString())} قلم",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            PersianFormat.money(total),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                        )
                        Icon(
                            Icons.Outlined.ChevronLeft,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                        )
                    }
                }
            }
        }
    }
}
