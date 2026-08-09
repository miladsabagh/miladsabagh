package com.goldgallery.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AllInclusive
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.Diamond
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material.icons.outlined.PanoramaFishEye
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goldgallery.app.data.db.ProductEntity
import com.goldgallery.app.data.model.Category
import com.goldgallery.app.logic.GoldCalculator
import com.goldgallery.app.logic.PersianFormat
import com.goldgallery.app.ui.theme.BronzeDeep
import com.goldgallery.app.ui.theme.Gold
import com.goldgallery.app.ui.theme.GoldPale
import com.goldgallery.app.viewmodel.ShopViewModel

fun categoryIcon(category: Category): ImageVector = when (category) {
    Category.RING -> Icons.Outlined.Circle
    Category.NECKLACE -> Icons.Outlined.AllInclusive
    Category.BRACELET -> Icons.Outlined.PanoramaFishEye
    Category.EARRING -> Icons.Outlined.WaterDrop
    Category.SET -> Icons.Outlined.Diamond
    Category.COIN -> Icons.Outlined.MonetizationOn
}

@Composable
fun ProductsScreen(viewModel: ShopViewModel) {
    val products by viewModel.products.collectAsStateWithLifecycle()
    val goldPrice by viewModel.goldPrice18.collectAsStateWithLifecycle()
    val cart by viewModel.cart.collectAsStateWithLifecycle()

    var query by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf("") }

    val filtered = products.filter { product ->
        (selectedCategory.isEmpty() || product.category == selectedCategory) &&
            (query.isBlank() || product.name.contains(query.trim()))
    }

    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            placeholder = { Text("جستجوی محصول…") },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                FilterChip(
                    selected = selectedCategory.isEmpty(),
                    onClick = { selectedCategory = "" },
                    label = { Text("همه") },
                )
            }
            items(Category.entries.toList()) { category ->
                FilterChip(
                    selected = selectedCategory == category.name,
                    onClick = { selectedCategory = category.name },
                    label = { Text(category.persianName) },
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(filtered, key = { it.id }) { product ->
                ProductCard(
                    product = product,
                    goldPrice18 = goldPrice,
                    inCartQty = cart.firstOrNull { it.product.id == product.id }?.quantity ?: 0,
                    onAdd = { viewModel.addToCart(product) },
                )
            }
        }
    }
}

@Composable
private fun ProductCard(
    product: ProductEntity,
    goldPrice18: Long,
    inCartQty: Int,
    onAdd: () -> Unit,
) {
    val category = Category.of(product.category)
    val price = GoldCalculator.itemPrice(
        weightGrams = product.weightGrams,
        karat = product.karat,
        wagePerGram = product.wagePerGram,
        base18PerGram = goldPrice18,
    )
    val available = product.stock - inCartQty

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(92.dp)
                    .background(Brush.linearGradient(listOf(GoldPale, Gold.copy(alpha = 0.55f)))),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    categoryIcon(category),
                    contentDescription = category.persianName,
                    tint = BronzeDeep,
                    modifier = Modifier.size(44.dp),
                )
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    color = BronzeDeep,
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(
                        category.persianName,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldPale,
                    )
                }
            }
            Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    product.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
                Text(
                    "${PersianFormat.weight(product.weightGrams)} • ${PersianFormat.karat(product.karat)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "اجرت: ${PersianFormat.money(product.wagePerGram)}/گرم",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    PersianFormat.money(price.total),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                if (available <= 0) {
                    Text(
                        "ناموجود",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                } else {
                    Button(
                        onClick = onAdd,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(vertical = 4.dp),
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.size(4.dp))
                        Text(
                            if (inCartQty > 0) "افزودن (${PersianFormat.toPersianDigits(inCartQty.toString())})"
                            else "افزودن به فاکتور",
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
        }
    }
}
