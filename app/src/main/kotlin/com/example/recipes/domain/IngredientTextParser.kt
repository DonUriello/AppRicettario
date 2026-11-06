package com.example.recipes.domain

data class ParsedIngredient(
    val quantity: Double?,
    val unit: MeasurementUnit,
    val name: String,
    val aisle: Aisle,
    /** True when the line had no leading number and was therefore mapped to [MeasurementUnit.TO_TASTE]. */
    val quantityWasMissing: Boolean = false,
)

/**
 * Parses free-text ingredient lines such as "200 g farina" or "sale q.b.".
 * Used by "Incolla una lista" and by the importers.
 */
object IngredientTextParser {

    private val leadingBullet = Regex("^[\\-•*]+\\s*")
    private val toTasteMarker = Regex("(?i)(?<![\\p{L}])q\\.?\\s?b\\.?(?![\\p{L}])")
    private val quantityPattern = Regex("^(\\d+(?:[.,]\\d+)?)\\s*(.*)$")
    private val unitWords: Map<String, MeasurementUnit> = mapOf(
        "g" to MeasurementUnit.GRAM,
        "gr" to MeasurementUnit.GRAM,
        "kg" to MeasurementUnit.KILOGRAM,
        "ml" to MeasurementUnit.MILLILITER,
        "l" to MeasurementUnit.LITER,
        "pz" to MeasurementUnit.PIECE,
        "spicchio" to MeasurementUnit.CLOVE,
        "spicchi" to MeasurementUnit.CLOVE,
        "cucchiaio" to MeasurementUnit.TABLESPOON,
        "cucchiai" to MeasurementUnit.TABLESPOON,
        "cucchiaino" to MeasurementUnit.TEASPOON,
        "cucchiaini" to MeasurementUnit.TEASPOON,
    )

    /** Parses every non-blank line of [text]. */
    fun parseLines(text: String): List<ParsedIngredient> =
        text.lines().mapNotNull { line -> parseLine(line) }

    /** Returns null for a blank line. */
    fun parseLine(line: String): ParsedIngredient? {
        val cleanedLine = line.trim().replace(leadingBullet, "").trim()
        if (cleanedLine.isEmpty()) return null

        if (toTasteMarker.containsMatchIn(cleanedLine)) {
            val name = cleanName(cleanedLine.replace(toTasteMarker, " "))
            return toTaste(name, quantityWasMissing = false)
        }

        val quantityMatch = quantityPattern.find(cleanedLine)
            ?: return toTaste(cleanName(cleanedLine), quantityWasMissing = true)

        val quantity = quantityMatch.groupValues[1].replace(',', '.').toDouble()
        var remainder = quantityMatch.groupValues[2].trim()
        var unit = MeasurementUnit.PIECE

        val firstWord = remainder.substringBefore(' ').trimEnd('.')
        val recognisedUnit = unitWords[firstWord.lowercase()]
        if (recognisedUnit != null) {
            unit = recognisedUnit
            remainder = remainder.substringAfter(' ', missingDelimiterValue = "")
        }
        remainder = remainder.trim().removePrefixIgnoringCase("di ").removePrefixIgnoringCase("d'")

        val name = cleanName(remainder)
        return ParsedIngredient(quantity = quantity, unit = unit, name = name, aisle = AisleGuesser.guess(name))
    }

    private fun toTaste(name: String, quantityWasMissing: Boolean) = ParsedIngredient(
        quantity = null,
        unit = MeasurementUnit.TO_TASTE,
        name = name,
        aisle = AisleGuesser.guess(name),
        quantityWasMissing = quantityWasMissing,
    )

    private fun cleanName(rawName: String): String =
        rawName.replace(Regex("\\s+"), " ").trim().trim(',', ';', ':', '-').trim()

    private fun String.removePrefixIgnoringCase(prefix: String): String =
        if (startsWith(prefix, ignoreCase = true)) substring(prefix.length).trimStart() else this
}
