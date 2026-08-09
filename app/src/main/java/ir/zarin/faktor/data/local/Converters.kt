package ir.zarin.faktor.data.local

import androidx.room.TypeConverter
import ir.zarin.faktor.core.CurrencyUnit
import ir.zarin.faktor.data.model.PaymentMethod
import ir.zarin.faktor.data.model.PricingMode
import ir.zarin.faktor.data.model.ProductCategory

class Converters {

    @TypeConverter
    fun categoryToString(value: ProductCategory): String = value.name

    @TypeConverter
    fun stringToCategory(value: String?): ProductCategory = ProductCategory.fromNameOrDefault(value)

    @TypeConverter
    fun pricingModeToString(value: PricingMode): String = value.name

    @TypeConverter
    fun stringToPricingMode(value: String?): PricingMode = PricingMode.fromNameOrDefault(value)

    @TypeConverter
    fun paymentMethodToString(value: PaymentMethod): String = value.name

    @TypeConverter
    fun stringToPaymentMethod(value: String?): PaymentMethod = PaymentMethod.fromNameOrDefault(value)

    @TypeConverter
    fun currencyUnitToString(value: CurrencyUnit): String = value.name

    @TypeConverter
    fun stringToCurrencyUnit(value: String?): CurrencyUnit = CurrencyUnit.fromNameOrDefault(value)
}
