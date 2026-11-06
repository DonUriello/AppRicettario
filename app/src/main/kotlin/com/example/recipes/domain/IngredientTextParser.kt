package com.example.recipes.domain

data class ParsedIngredient(
    val quantity: Double?,
    val unit: MeasurementUnit,
    val name: String,
    val aisle: Aisle,
    /** True when the line had no quantity at all (and no "q.b.") and was therefore mapped to [MeasurementUnit.TO_TASTE]. */
    val quantityWasMissing: Boolean = false,
)

/**
 * Parses free-text ingredient lines such as "200 g farina" or "sale q.b.".
 * Used by "Incolla una lista" and by the importers.
 */
object IngredientTextParser {

    private val leadingBullet = Regex("^[\\-•*]+\\s*")
    private val toTasteMarker = Regex("(?i)(?<![\\p{L}])q\\.?\\s?b\\.?(?![\\p{L}])")
    private const val NUMBER = "\\d+(?:[.,]\\d+)?(?:\\s+\\d+/\\d+)?(?:/\\d+)?"
    private val quantityPattern = Regex("^($NUMBER)\\s*(.*)$")
    private val trailingQuantityPattern = Regex("^(.*?\\S)\\s+($NUMBER)\\s*([\\p{L}.]*)$")
    private val unicodeFractions = mapOf("½" to "1/2", "¼" to "1/4", "¾" to "3/4", "⅓" to "1/3", "⅔" to "2/3")
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
        val cleanedLine = replaceFractions(line.trim().replace(leadingBullet, "").trim())
        if (cleanedLine.isEmpty()) return null

        if (toTasteMarker.containsMatchIn(cleanedLine)) {
            val name = cleanName(cleanedLine.replace(toTasteMarker, " "))
            return toTaste(name, quantityWasMissing = false)
        }

        return parseLeadingQuantity(cleanedLine)
            ?: parseTrailingQuantity(cleanedLine)
            ?: toTaste(cleanName(cleanedLine), quantityWasMissing = true)
    }

    /** "<number> [unit] [di] <name>", e.g. "200 g farina". */
    private fun parseLeadingQuantity(line: String): ParsedIngredient? {
        val quantityMatch = quantityPattern.find(line) ?: return null
        val quantity = parseNumber(quantityMatch.groupValues[1]) ?: return null
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
        if (name.isEmpty()) return null
        return ParsedIngredient(quantity = quantity, unit = unit, name = name, aisle = AisleGuesser.guess(name))
    }

    /**
     * "<name> <number> [unit]", e.g. "Spaghetti 320 g": the order many Italian recipe sites use.
     * Lines without any number still fall back to [MeasurementUnit.TO_TASTE].
     */
    private fun parseTrailingQuantity(line: String): ParsedIngredient? {
        val match = trailingQuantityPattern.find(line) ?: return null
        val quantity = parseNumber(match.groupValues[2]) ?: return null
        val unitWord = match.groupValues[3].trimEnd('.').lowercase()
        val unit = if (unitWord.isEmpty()) MeasurementUnit.PIECE else unitWords[unitWord] ?: return null
        val name = cleanName(match.groupValues[1])
        if (name.isEmpty()) return null
        return ParsedIngredient(quantity = quantity, unit = unit, name = name, aisle = AisleGuesser.guess(name))
    }

    /** Accepts "1,5", "1.5" and simple fractions such as "1/2" or "1 1/2". */
    private fun parseNumber(text: String): Double? {
        val parts = text.trim().split(Regex("\\s+"))
        return parts.sumOf { part ->
            if (part.contains('/')) {
                val (numerator, denominator) = part.split('/').map { it.toDoubleOrNull() ?: return null }
                if (denominator == 0.0) return null
                numerator / denominator
            } else {
                part.replace(',', '.').toDoubleOrNull() ?: return null
            }
        }
    }

    private fun replaceFractions(text: String): String {
        var result = text
        unicodeFractions.forEach { (symbol, replacement) ->
            // "1½" → "1 1/2", "½" → "1/2"
            result = result.replace(Regex("(\\d)$symbol"), "$1 $replacement").replace(symbol, replacement)
        }
        return result
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
