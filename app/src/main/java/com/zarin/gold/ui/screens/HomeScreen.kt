package com.zarin.gold.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Diamond
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zarin.gold.ui.HomeUiState
import com.zarin.gold.ui.components.AtmosphereBackground
import com.zarin.gold.ui.components.BrandMark
import com.zarin.gold.ui.components.PriceRibbon
import com.zarin.gold.ui.components.QuickAction
import com.zarin.gold.ui.theme.ZarinColors
import com.zarin.gold.util.toPersian
import com.zarin.gold.util.toPersianCurrency
import com.zarin.gold.util.toPersianDateTime
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    state: HomeUiState,
    onOpenCatalog: () -> Unit,
    onCreateInvoice: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenInvoice: (Long) -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(80)
        visible = true
    }

    AtmosphereBackground {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn() + slideInVertically { it / 3 }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                    listOf(
                                        androidx.compose.ui.graphics.Color(0xFF2C2418),
                                        androidx.compose.ui.graphics.Color(0xFF121014)
                                    )
                                )
                            )
                            .border(1.dp, ZarinColors.Amber.copy(alpha = 0.35f), RoundedCornerShape(28.dp))
                            .padding(24.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        BrandMark()
                        Column {
                            Text(
                                text = "درخشش در هر فاکتور",
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Medium,
                                fontSize = 26.sp,
                                color = ZarinColors.Ivory
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "فروش طلا و جواهر با صدور فاکتور رسمی، دقیق و سریع.",
                                color = ZarinColors.IvoryMuted,
                                fontSize = 14.sp,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }
            }

            item {
                PriceRibbon(
                    label = "نرخ لحظه‌ای طلای ۱۸ عیار",
                    value = state.goldPrice18.toPersianCurrency()
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickAction(
                        title = "کاتالوگ",
                        icon = Icons.Outlined.Diamond,
                        onClick = onOpenCatalog,
                        modifier = Modifier.weight(1f)
                    )
                    QuickAction(
                        title = "فاکتور جدید",
                        icon = Icons.Outlined.ReceiptLong,
                        onClick = onCreateInvoice,
                        modifier = Modifier.weight(1f)
                    )
                    QuickAction(
                        title = "سوابق",
                        icon = Icons.Outlined.History,
                        onClick = onOpenHistory,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(ZarinColors.NightElevated.copy(alpha = 0.8f))
                        .border(1.dp, ZarinColors.Line, RoundedCornerShape(18.dp))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatChip("کالا", state.productCount.toPersian())
                    StatChip("فاکتور", state.invoiceCount.toPersian())
                    Box(contentAlignment = Alignment.Center) {
                        androidx.compose.material3.Icon(
                            Icons.Outlined.Storefront,
                            contentDescription = null,
                            tint = ZarinColors.Amber,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            item {
                Text(
                    text = "آخرین فاکتورها",
                    fontFamily = FontFamily.Serif,
                    fontSize = 20.sp,
                    color = ZarinColors.Ivory,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (state.recentInvoices.isEmpty()) {
                item {
                    Text(
                        text = "هنوز فاکتوری صادر نشده. از «فاکتور جدید» شروع کنید.",
                        color = ZarinColors.IvoryMuted,
                        fontSize = 13.sp
                    )
                }
            } else {
                items(state.recentInvoices, key = { it.id }) { invoice ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(ZarinColors.NightSoft.copy(alpha = 0.75f))
                            .border(1.dp, ZarinColors.Line, RoundedCornerShape(16.dp))
                            .clickable { onOpenInvoice(invoice.id) }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(invoice.customerName, color = ZarinColors.Ivory, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "${invoice.invoiceNumber}  ·  ${invoice.createdAt.toPersianDateTime()}",
                                color = ZarinColors.IvoryMuted,
                                fontSize = 12.sp
                            )
                        }
                        Text(
                            invoice.total.toPersianCurrency(),
                            color = ZarinColors.AmberSoft,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }
}

@Composable
private fun StatChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = ZarinColors.AmberSoft, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(label, color = ZarinColors.IvoryMuted, fontSize = 12.sp)
    }
}
