package com.zarnegar.gold.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.zarnegar.gold.core.PersianText

private const val SEPARATOR = '\u066C'

/** ارقام لاتین را با ارقام فارسی و جداکنندهٔ هزارگان نمایش می‌دهد. */
object PersianAmountTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        if (digits.isEmpty()) return TransformedText(AnnotatedString(""), OffsetMapping.Identity)

        val out = StringBuilder()
        val originalToTransformed = IntArray(digits.length + 1)
        for (index in digits.indices) {
            if (index > 0 && (digits.length - index) % 3 == 0) out.append(SEPARATOR)
            originalToTransformed[index] = out.length
            val ch = digits[index]
            out.append(if (ch in '0'..'9') '\u06F0' + (ch - '0') else ch)
        }
        originalToTransformed[digits.length] = out.length

        val transformedToOriginal = IntArray(out.length + 1)
        var original = 0
        for (index in out.indices) {
            transformedToOriginal[index] = original
            if (out[index] != SEPARATOR) original++
        }
        transformedToOriginal[out.length] = original

        return TransformedText(
            AnnotatedString(out.toString()),
            object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int =
                    originalToTransformed[offset.coerceIn(0, digits.length)]

                override fun transformedToOriginal(offset: Int): Int =
                    transformedToOriginal[offset.coerceIn(0, out.length)]
            },
        )
    }
}

/** ارقام لاتین را با ارقام فارسی نمایش می‌دهد (بدون جداکننده). */
object PersianDigitsTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText = TransformedText(
        AnnotatedString(PersianText.toPersianDigits(text.text)),
        OffsetMapping.Identity,
    )
}

/** فیلد مبلغ (عدد صحیح) با جداکنندهٔ هزارگان */
@Composable
fun AmountField(
    label: String,
    value: Long,
    onValueChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
    suffix: String? = null,
    enabled: Boolean = true,
    supportingText: String? = null,
) {
    var text by remember { mutableStateOf(if (value == 0L) "" else value.toString()) }
    LaunchedEffect(value) {
        val current = text.toLongOrNull() ?: 0L
        if (current != value) text = if (value == 0L) "" else value.toString()
    }

    OutlinedTextField(
        value = text,
        onValueChange = { raw ->
            val digits = PersianText.toEnglishDigits(raw).filter { it.isDigit() }.take(15)
            text = digits.trimStart('0').ifEmpty { if (digits.isEmpty()) "" else "0" }
            onValueChange(text.toLongOrNull() ?: 0L)
        },
        label = { Text(label) },
        singleLine = true,
        enabled = enabled,
        visualTransformation = PersianAmountTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        suffix = suffix?.let { { Text(it) } },
        supportingText = supportingText?.let { { Text(it) } },
        modifier = modifier,
    )
}

/** فیلد اعشاری (وزن، درصد و …) */
@Composable
fun DecimalField(
    label: String,
    value: Double,
    onValueChange: (Double) -> Unit,
    modifier: Modifier = Modifier,
    suffix: String? = null,
    enabled: Boolean = true,
) {
    var text by remember { mutableStateOf(formatDecimal(value)) }
    LaunchedEffect(value) {
        val current = text.toDoubleOrNull() ?: 0.0
        if (kotlin.math.abs(current - value) > 1e-9) text = formatDecimal(value)
    }

    OutlinedTextField(
        value = text,
        onValueChange = { raw ->
            val normalized = PersianText.toEnglishDigits(raw)
                .replace('\u066B', '.')
                .replace(',', '.')
                .filter { it.isDigit() || it == '.' }
            val cleaned = normalized.split('.').let { parts ->
                if (parts.size <= 1) normalized else parts[0] + "." + parts.drop(1).joinToString("")
            }.take(12)
            text = cleaned
            onValueChange(cleaned.toDoubleOrNull() ?: 0.0)
        },
        label = { Text(label) },
        singleLine = true,
        enabled = enabled,
        visualTransformation = PersianDigitsTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        suffix = suffix?.let { { Text(it) } },
        modifier = modifier,
    )
}

/** فیلد متنی ساده */
@Composable
fun TextInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = singleLine,
        minLines = minLines,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier,
    )
}

private fun formatDecimal(value: Double): String = when {
    value == 0.0 -> ""
    value == value.toLong().toDouble() -> value.toLong().toString()
    else -> value.toString()
}
