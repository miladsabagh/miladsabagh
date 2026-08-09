package com.zarin.gold.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zarin.gold.data.Product
import com.zarin.gold.data.ProductCategory
import com.zarin.gold.ui.CatalogUiState
import com.zarin.gold.ui.components.AtmosphereBackground
import com.zarin.gold.ui.components.SectionTitle
import com.zarin.gold.ui.components.ZarinField
import com.zarin.gold.ui.theme.ZarinColors
import com.zarin.gold.util.toPersian
import com.zarin.gold.util.toPersianCurrency
import com.zarin.gold.util.toPersianWeight
import kotlin.math.roundToLong

@Composable
fun CatalogScreen(
    state: CatalogUiState,
    goldPrice18: Long,
    onQueryChange: (String) -> Unit,
    onCategoryChange: (ProductCategory?) -> Unit,
    onAddToInvoice: (Product) -> Unit
) {
    AtmosphereBackground {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                SectionTitle(
                    title = "کاتالوگ زرین",
                    subtitle = "انتخاب کالا برای افزودن به فاکتور"
                )
            }

            item {
                ZarinField(
                    value = state.query,
                    onValueChange = onQueryChange,
                    label = "جستجوی نام کالا"
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryChip(
                        label = "همه",
                        selected = state.selectedCategory == null,
                        onClick = { onCategoryChange(null) }
                    )
                    ProductCategory.entries.forEach { category ->
                        CategoryChip(
                            label = category.labelFa,
                            selected = state.selectedCategory == category,
                            onClick = { onCategoryChange(category) }
                        )
                    }
                }
            }

            items(state.filtered, key = { it.id }) { product ->
                ProductRow(
                    product = product,
                    goldPrice18 = goldPrice18,
                    onAdd = { onAddToInvoice(product) }
                )
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) ZarinColors.Amber else Color.Transparent
    val fg = if (selected) ZarinColors.Night else ZarinColors.IvoryMuted
    Text(
        text = label,
        color = fg,
        fontSize = 13.sp,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(1.dp, ZarinColors.Amber.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    )
}

@Composable
private fun ProductRow(product: Product, goldPrice18: Long, onAdd: () -> Unit) {
    val unit = ((goldPrice18.toDouble() * product.carat) / 18.0).roundToLong()
    val goldValue = (product.weightGram * unit).roundToLong()
    val making = (goldValue * product.makingFeePercent / 100.0).roundToLong()
    val estimate = goldValue + making + product.stonePrice

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(ZarinColors.NightElevated.copy(alpha = 0.88f))
            .border(1.dp, ZarinColors.Line, RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF3A2E1A), Color(0xFF1A1612))
                    ),
                    RoundedCornerShape(14.dp)
                )
                .border(1.dp, ZarinColors.Amber.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = product.category.labelFa.take(1),
                color = ZarinColors.AmberSoft,
                fontFamily = FontFamily.Serif,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(product.name, color = ZarinColors.Ivory, fontWeight = FontWeight.Medium, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${product.category.labelFa} · ${product.carat.toPersian()} عیار · ${product.weightGram.toPersianWeight()}",
                color = ZarinColors.IvoryMuted,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "تخمین: ${estimate.toPersianCurrency()}",
                color = ZarinColors.AmberSoft,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(ZarinColors.Amber)
                .clickable(onClick = onAdd),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Add, contentDescription = "افزودن", tint = ZarinColors.Night)
        }
    }
}
