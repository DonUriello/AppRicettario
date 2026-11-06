package com.example.recipes.ui.components

import com.example.recipes.domain.Aisle
import com.example.recipes.domain.AisleGuesser
import com.example.recipes.domain.Ingredient
import com.example.recipes.domain.MeasurementUnit
import com.example.recipes.domain.QuantityFormatter
import java.util.UUID

/** An ingredient as edited in a form: the quantity is kept as typed text. */
data class EditableIngredient(
    /** Stable key for the UI list; equals the database id for existing ingredients. */
    val key: String,
    val quantityText: String,
    val unit: MeasurementUnit,
    val name: String,
    val aisle: Aisle,
    /** Once the user picks an aisle, renaming the ingredient no longer re-guesses it. */
    val aisleWasEdited: Boolean = false,
    val reviewNote: String? = null,
) {
    /** Accepts both "1,5" and "1.5". */
    val quantity: Double? get() = quantityText.trim().replace(',', '.').toDoubleOrNull()

    val isNew: Boolean get() = key.startsWith(NEW_KEY_PREFIX)

    fun withName(newName: String) =
        copy(name = newName, aisle = if (aisleWasEdited) aisle else AisleGuesser.guess(newName))

    /** Typing a quantity on a "q.b." row turns it into pieces. */
    fun withQuantityText(newText: String): EditableIngredient {
        val filteredText = newText.filter { it.isDigit() || it == ',' || it == '.' }
        val newUnit = if (filteredText.isNotEmpty() && unit == MeasurementUnit.TO_TASTE) MeasurementUnit.PIECE else unit
        return copy(quantityText = filteredText, unit = newUnit)
    }

    fun withUnit(newUnit: MeasurementUnit) =
        copy(unit = newUnit, quantityText = if (newUnit == MeasurementUnit.TO_TASTE) "" else quantityText)

    fun withAisle(newAisle: Aisle) = copy(aisle = newAisle, aisleWasEdited = true)

    fun toIngredient(reviewNote: String? = this.reviewNote) = Ingredient(
        id = if (isNew) "" else key,
        quantity = if (unit == MeasurementUnit.TO_TASTE) null else quantity,
        unit = unit,
        name = name,
        aisle = aisle,
        reviewNote = reviewNote,
    )

    companion object {
        private const val NEW_KEY_PREFIX = "new-"

        fun newKey() = NEW_KEY_PREFIX + UUID.randomUUID()

        fun empty() = EditableIngredient(
            key = newKey(),
            quantityText = "",
            unit = MeasurementUnit.GRAM,
            name = "",
            aisle = Aisle.OTHER,
        )

        fun from(ingredient: Ingredient) = EditableIngredient(
            key = ingredient.id,
            quantityText = QuantityFormatter.formatForEditing(ingredient.quantity),
            unit = ingredient.unit,
            name = ingredient.name,
            aisle = ingredient.aisle,
            aisleWasEdited = true,
            reviewNote = ingredient.reviewNote,
        )
    }
}
