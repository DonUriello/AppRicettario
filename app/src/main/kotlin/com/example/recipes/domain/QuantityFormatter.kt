package com.example.recipes.domain

import kotlin.math.ceil
import kotlin.math.roundToLong

/**
 * Turns a quantity and its unit into Italian display text ("1,5 kg", "1 spicchio", "q.b.").
 * The returned strings are unit symbols and grammatical forms rather than translatable UI text.
 */
object QuantityFormatter {

    const val TO_TASTE_LABEL = "q.b."

    fun format(quantity: Double?, unit: MeasurementUnit): String {
        if (unit == MeasurementUnit.TO_TASTE || quantity == null) return TO_TASTE_LABEL
        return when (unit) {
            MeasurementUnit.GRAM -> formatMetric(quantity, smallSymbol = "g", largeSymbol = "kg")
            MeasurementUnit.KILOGRAM -> formatMetric(quantity * 1000, smallSymbol = "g", largeSymbol = "kg")
            MeasurementUnit.MILLILITER -> formatMetric(quantity, smallSymbol = "ml", largeSymbol = "l")
            MeasurementUnit.LITER -> formatMetric(quantity * 1000, smallSymbol = "ml", largeSymbol = "l")
            MeasurementUnit.PIECE -> formatDecimal(roundUpToHalf(quantity))
            MeasurementUnit.CLOVE -> {
                val cloves = ceil(quantity - ROUNDING_TOLERANCE).toLong().coerceAtLeast(1)
                "$cloves " + if (cloves == 1L) "spicchio" else "spicchi"
            }
            MeasurementUnit.TABLESPOON -> withNoun(quantity, singular = "cucchiaio", plural = "cucchiai")
            MeasurementUnit.TEASPOON -> withNoun(quantity, singular = "cucchiaino", plural = "cucchiaini")
            MeasurementUnit.TO_TASTE -> TO_TASTE_LABEL
        }
    }

    /** Formats a number with a comma decimal separator, at most one decimal, no trailing ",0". */
    fun formatDecimal(value: Double): String {
        val tenths = (value * 10).roundToLong()
        val wholePart = tenths / 10
        val decimalPart = tenths % 10
        return if (decimalPart == 0L) wholePart.toString() else "$wholePart,$decimalPart"
    }

    /** Text for an editable quantity field: up to two decimals, comma separator. */
    fun formatForEditing(quantity: Double?): String {
        if (quantity == null) return ""
        val hundredths = (quantity * 100).roundToLong()
        val wholePart = hundredths / 100
        val decimalPart = hundredths % 100
        return when {
            decimalPart == 0L -> wholePart.toString()
            decimalPart % 10 == 0L -> "$wholePart,${decimalPart / 10}"
            else -> "$wholePart,${decimalPart.toString().padStart(2, '0')}"
        }
    }

    private fun formatMetric(baseQuantity: Double, smallSymbol: String, largeSymbol: String): String =
        if (baseQuantity >= 1000 - ROUNDING_TOLERANCE) {
            "${formatDecimal(baseQuantity / 1000)} $largeSymbol"
        } else {
            "${formatDecimal(baseQuantity)} $smallSymbol"
        }

    private fun withNoun(quantity: Double, singular: String, plural: String): String {
        val text = formatDecimal(quantity)
        return "$text " + if (text == "1") singular else plural
    }

    private fun roundUpToHalf(value: Double): Double = ceil(value * 2 - ROUNDING_TOLERANCE) / 2

    /** Guards against floating point noise such as 2.0000000004 rounding up to 2.5. */
    private const val ROUNDING_TOLERANCE = 1e-6
}
