package com.zarnegar.gold.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zarnegar.gold.core.PersianText
import com.zarnegar.gold.ui.components.AmountField
import com.zarnegar.gold.ui.components.DecimalField
import com.zarnegar.gold.ui.components.SectionCard
import com.zarnegar.gold.ui.components.TextInputField
import com.zarnegar.gold.ui.vm.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onBack: () -> Unit) {
    val stored by viewModel.settings.collectAsStateWithLifecycle()
    val saved by viewModel.saved.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var draft by remember(stored) { mutableStateOf(stored) }

    LaunchedEffect(saved) {
        if (saved) {
            snackbarHostState.showSnackbar("تنظیمات ذخیره شد")
            viewModel.consumeSaved()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("تنظیمات فروشگاه") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "بازگشت",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                SectionCard(title = "اطلاعات فروشگاه (سربرگ فاکتور)") {
                    TextInputField(
                        label = "نام فروشگاه",
                        value = draft.shopName,
                        onValueChange = { draft = draft.copy(shopName = it) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    TextInputField(
                        label = "نام مالک",
                        value = draft.ownerName,
                        onValueChange = { draft = draft.copy(ownerName = it) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    TextInputField(
                        label = "تلفن",
                        value = draft.phone,
                        onValueChange = { draft = draft.copy(phone = it) },
                        keyboardType = KeyboardType.Phone,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    TextInputField(
                        label = "نشانی",
                        value = draft.address,
                        onValueChange = { draft = draft.copy(address = it) },
                        singleLine = false,
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    TextInputField(
                        label = "کد اقتصادی",
                        value = draft.economicCode,
                        onValueChange = { draft = draft.copy(economicCode = it) },
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    TextInputField(
                        label = "شمارهٔ کارت / شبا",
                        value = draft.cardNumber,
                        onValueChange = { draft = draft.copy(cardNumber = it) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            item {
                SectionCard(title = "نرخ‌گذاری") {
                    AmountField(
                        label = "نرخ هر گرم طلای ۱۸ عیار",
                        value = draft.goldRatePerGram18k,
                        onValueChange = { draft = draft.copy(goldRatePerGram18k = it) },
                        suffix = draft.currencyLabel,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    DecimalField(
                        label = "درصد اجرت پیش‌فرض",
                        value = draft.defaultWagePercent,
                        onValueChange = { draft = draft.copy(defaultWagePercent = it) },
                        suffix = "٪",
                        modifier = Modifier.fillMaxWidth(),
                    )
                    DecimalField(
                        label = "درصد سود پیش‌فرض",
                        value = draft.defaultProfitPercent,
                        onValueChange = { draft = draft.copy(defaultProfitPercent = it) },
                        suffix = "٪",
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text("واحد پول", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("تومان", "ریال").forEach { unit ->
                            FilterChip(
                                selected = draft.currencyLabel == unit,
                                onClick = { draft = draft.copy(currencyLabel = unit) },
                                label = { Text(unit) },
                            )
                        }
                    }
                }
            }

            item {
                SectionCard(title = "مالیات و گرد کردن") {
                    DecimalField(
                        label = "نرخ مالیات بر ارزش افزوده",
                        value = draft.vatPercent,
                        onValueChange = { draft = draft.copy(vatPercent = it) },
                        suffix = "٪",
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = "طبق قانون، مالیات بر ارزش افزودهٔ مصنوعات طلا فقط به اجرت ساخت و " +
                            "سود فروشنده تعلق می‌گیرد و ارزش طلای خام معاف است.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("ارزش سنگ مشمول مالیات باشد")
                            Text(
                                text = "برای جواهرات نگین‌دار",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = draft.vatOnStone,
                            onCheckedChange = { draft = draft.copy(vatOnStone = it) },
                        )
                    }
                    Text("گرد کردن مبلغ نهایی", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(0L, 1_000L, 10_000L, 100_000L).forEach { unit ->
                            FilterChip(
                                selected = draft.roundTo == unit,
                                onClick = { draft = draft.copy(roundTo = unit) },
                                label = {
                                    Text(
                                        if (unit == 0L) {
                                            "بدون گرد کردن"
                                        } else {
                                            PersianText.formatNumber(unit)
                                        },
                                    )
                                },
                            )
                        }
                    }
                }
            }

            item {
                SectionCard(title = "پانویس فاکتور") {
                    TextInputField(
                        label = "متن پانویس",
                        value = draft.invoiceFooterNote,
                        onValueChange = { draft = draft.copy(invoiceFooterNote = it) },
                        singleLine = false,
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            item {
                Button(
                    onClick = { viewModel.save(draft) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("ذخیرهٔ تنظیمات") }
            }

            item {
                Text(
                    text = "زرنگار • نسخهٔ ۱٫۰٫۰",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
