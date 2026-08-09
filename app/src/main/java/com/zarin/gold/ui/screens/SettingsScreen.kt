package com.zarin.gold.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zarin.gold.ui.components.AtmosphereBackground
import com.zarin.gold.ui.components.GoldButton
import com.zarin.gold.ui.components.SectionTitle
import com.zarin.gold.ui.components.ZarinField
import com.zarin.gold.ui.theme.ZarinColors
import com.zarin.gold.util.toPersianCurrency

@Composable
fun SettingsScreen(
    goldPrice18: Long,
    onSavePrice: (String) -> Unit
) {
    var priceText by remember(goldPrice18) { mutableStateOf(goldPrice18.toString()) }

    AtmosphereBackground {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                SectionTitle(
                    title = "تنظیمات نرخ",
                    subtitle = "قیمت پایه هر گرم طلای ۱۸ عیار را به‌روز کنید"
                )
            }

            item {
                Text(
                    text = "نرخ فعلی: ${goldPrice18.toPersianCurrency()}",
                    color = ZarinColors.AmberSoft,
                    fontSize = 14.sp
                )
            }

            item {
                ZarinField(
                    value = priceText,
                    onValueChange = { priceText = it.filter(Char::isDigit) },
                    label = "قیمت هر گرم ۱۸ عیار (ریال)"
                )
            }

            item {
                GoldButton(
                    text = "ذخیره نرخ",
                    onClick = { onSavePrice(priceText) },
                    enabled = priceText.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "قیمت کالاها و فاکتورهای جدید بر اساس این نرخ محاسبه می‌شود. فاکتورهای قبلی با نرخ زمان صدور حفظ می‌مانند.",
                    color = ZarinColors.IvoryMuted,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
