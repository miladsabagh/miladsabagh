package ir.zarin.faktor.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import ir.zarin.faktor.R
import ir.zarin.faktor.data.model.PaymentMethod
import ir.zarin.faktor.data.model.PaymentStatus
import ir.zarin.faktor.data.model.PricingMode
import ir.zarin.faktor.data.model.ProductCategory
import ir.zarin.faktor.ui.theme.PaidGreen
import ir.zarin.faktor.ui.theme.PartialAmber
import ir.zarin.faktor.ui.theme.UnpaidRed

@Composable
fun ProductCategory.label(): String = stringResource(
    when (this) {
        ProductCategory.GOLD -> R.string.category_gold
        ProductCategory.JEWELRY -> R.string.category_jewelry
        ProductCategory.COIN -> R.string.category_coin
        ProductCategory.SILVER -> R.string.category_silver
        ProductCategory.OTHER -> R.string.category_other
    },
)

val ProductCategory.icon: ImageVector
    get() = when (this) {
        ProductCategory.GOLD -> Icons.Default.WorkspacePremium
        ProductCategory.JEWELRY -> Icons.Default.Diamond
        ProductCategory.COIN -> Icons.Default.Savings
        ProductCategory.SILVER -> Icons.Default.Paid
        ProductCategory.OTHER -> Icons.Default.Category
    }

@Composable
fun PricingMode.label(): String = stringResource(
    when (this) {
        PricingMode.BY_WEIGHT -> R.string.pricing_by_weight
        PricingMode.FIXED_PRICE -> R.string.pricing_fixed
    },
)

@Composable
fun PaymentMethod.label(): String = stringResource(
    when (this) {
        PaymentMethod.CASH -> R.string.payment_cash
        PaymentMethod.CARD -> R.string.payment_card
        PaymentMethod.TRANSFER -> R.string.payment_transfer
        PaymentMethod.CREDIT -> R.string.payment_credit
    },
)

@Composable
fun PaymentStatus.label(): String = stringResource(
    when (this) {
        PaymentStatus.PAID -> R.string.status_paid
        PaymentStatus.PARTIAL -> R.string.status_partial
        PaymentStatus.UNPAID -> R.string.status_unpaid
    },
)

val PaymentStatus.color: Color
    get() = when (this) {
        PaymentStatus.PAID -> PaidGreen
        PaymentStatus.PARTIAL -> PartialAmber
        PaymentStatus.UNPAID -> UnpaidRed
    }
