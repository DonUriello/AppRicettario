package com.example.recipes.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class QuantityFormatterTest {

    private fun format(quantity: Double?, unit: MeasurementUnit) = QuantityFormatter.format(quantity, unit)

    @Test fun grams() {
        assertEquals("320 g", format(320.0, MeasurementUnit.GRAM))
        assertEquals("1,5 kg", format(1500.0, MeasurementUnit.GRAM))
        assertEquals("1 kg", format(1000.0, MeasurementUnit.GRAM))
        assertEquals("1,5 kg", format(1.5, MeasurementUnit.KILOGRAM))
        assertEquals("12,5 g", format(12.5, MeasurementUnit.GRAM))
    }

    @Test fun milliliters() {
        assertEquals("2 l", format(2000.0, MeasurementUnit.MILLILITER))
        assertEquals("250 ml", format(250.0, MeasurementUnit.MILLILITER))
        assertEquals("1 l", format(1.0, MeasurementUnit.LITER))
    }

    @Test fun piecesRoundUpToHalf() {
        assertEquals("1,5", format(1.2, MeasurementUnit.PIECE))
        assertEquals("2", format(2.0, MeasurementUnit.PIECE))
        assertEquals("2", format(1.6, MeasurementUnit.PIECE))
        assertEquals("0,5", format(0.25, MeasurementUnit.PIECE))
        // Floating point noise must not push an exact value to the next half.
        assertEquals("2", format(2.0000000001, MeasurementUnit.PIECE))
    }

    @Test fun cloves() {
        assertEquals("1 spicchio", format(1.0, MeasurementUnit.CLOVE))
        assertEquals("2 spicchi", format(1.2, MeasurementUnit.CLOVE))
        assertEquals("1 spicchio", format(0.5, MeasurementUnit.CLOVE))
    }

    @Test fun spoons() {
        assertEquals("1 cucchiaio", format(1.0, MeasurementUnit.TABLESPOON))
        assertEquals("1,5 cucchiai", format(1.5, MeasurementUnit.TABLESPOON))
        assertEquals("3 cucchiaini", format(3.0, MeasurementUnit.TEASPOON))
        assertEquals("1 cucchiaino", format(1.0, MeasurementUnit.TEASPOON))
    }

    @Test fun toTaste() {
        assertEquals("q.b.", format(null, MeasurementUnit.TO_TASTE))
        assertEquals("q.b.", format(null, MeasurementUnit.GRAM))
    }

    @Test fun editingText() {
        assertEquals("1,5", QuantityFormatter.formatForEditing(1.5))
        assertEquals("200", QuantityFormatter.formatForEditing(200.0))
        assertEquals("0,25", QuantityFormatter.formatForEditing(0.25))
        assertEquals("", QuantityFormatter.formatForEditing(null))
    }
}
