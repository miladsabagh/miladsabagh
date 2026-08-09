package ir.zarrin.goldshop.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.zarrin.goldshop.core.JalaliDate
import ir.zarrin.goldshop.core.PersianCalendar
import ir.zarrin.goldshop.core.PersianNumbers

@Composable
fun JalaliDatePickerDialog(
    initial: JalaliDate,
    onDismiss: () -> Unit,
    onConfirm: (JalaliDate) -> Unit
) {
    val currentYear = remember { PersianCalendar.today().year }
    var year by remember { mutableIntStateOf(initial.year) }
    var month by remember { mutableIntStateOf(initial.month) }
    var day by remember { mutableIntStateOf(initial.day) }

    val monthLength = PersianCalendar.monthLength(year, month)
    if (day > monthLength) day = monthLength

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("انتخاب تاریخ فاکتور") },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DropdownSelector(
                        label = "روز",
                        options = (1..monthLength).toList(),
                        selected = day,
                        optionLabel = { PersianNumbers.toPersianDigits(it.toString()) },
                        onSelect = { day = it },
                        modifier = Modifier.weight(1f)
                    )
                    DropdownSelector(
                        label = "ماه",
                        options = (1..12).toList(),
                        selected = month,
                        optionLabel = { PersianCalendar.MONTH_NAMES[it - 1] },
                        onSelect = { month = it },
                        modifier = Modifier.weight(1.4f)
                    )
                    DropdownSelector(
                        label = "سال",
                        options = ((currentYear - 5)..(currentYear + 1)).toList(),
                        selected = year,
                        optionLabel = { PersianNumbers.toPersianDigits(it.toString()) },
                        onSelect = { year = it },
                        modifier = Modifier.weight(1.2f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(JalaliDate(year, month, day)) }) { Text("تأیید") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } }
    )
}
