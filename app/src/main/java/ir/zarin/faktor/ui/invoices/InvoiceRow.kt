package ir.zarin.faktor.ui.invoices

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.zarin.faktor.R
import ir.zarin.faktor.data.model.Invoice
import ir.zarin.faktor.ui.common.LocalDisplayFormat
import ir.zarin.faktor.ui.common.StatusChip
import ir.zarin.faktor.ui.common.color
import ir.zarin.faktor.ui.common.currencyLabel
import ir.zarin.faktor.ui.common.label

@Composable
fun InvoiceRow(
    invoice: Invoice,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val format = LocalDisplayFormat.current
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
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = invoice.customerName.ifBlank { stringResource(R.string.walk_in_customer) },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${stringResource(R.string.invoice_number)} ${format.digits(invoice.number)}" +
                        " • ${format.date(invoice.dateMillis)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${format.money(invoice.grandTotalRial)} ${currencyLabel()}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(6.dp))
                StatusChip(text = invoice.status.label(), color = invoice.status.color)
            }
        }
    }
}
