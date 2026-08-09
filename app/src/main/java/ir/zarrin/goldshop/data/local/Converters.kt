package ir.zarrin.goldshop.data.local

import androidx.room.TypeConverter
import ir.zarrin.goldshop.domain.model.InvoiceType
import ir.zarrin.goldshop.domain.model.ItemKind
import ir.zarrin.goldshop.domain.model.PaymentMethod
import ir.zarrin.goldshop.domain.model.TaxBasis
import ir.zarrin.goldshop.domain.model.WageMode

class Converters {

    @TypeConverter fun itemKindToString(value: ItemKind): String = value.name

    @TypeConverter fun stringToItemKind(value: String?): ItemKind = ItemKind.fromName(value)

    @TypeConverter fun wageModeToString(value: WageMode): String = value.name

    @TypeConverter fun stringToWageMode(value: String?): WageMode = WageMode.fromName(value)

    @TypeConverter fun taxBasisToString(value: TaxBasis): String = value.name

    @TypeConverter fun stringToTaxBasis(value: String?): TaxBasis = TaxBasis.fromName(value)

    @TypeConverter fun invoiceTypeToString(value: InvoiceType): String = value.name

    @TypeConverter fun stringToInvoiceType(value: String?): InvoiceType = InvoiceType.fromName(value)

    @TypeConverter fun paymentMethodToString(value: PaymentMethod): String = value.name

    @TypeConverter fun stringToPaymentMethod(value: String?): PaymentMethod = PaymentMethod.fromName(value)
}
