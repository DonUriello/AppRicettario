package com.example.recipes.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IngredientTextParserTest {

    private fun assertParsed(line: String, quantity: Double?, unit: MeasurementUnit, name: String, aisle: Aisle) {
        val parsed = IngredientTextParser.parseLine(line)!!
        assertEquals("quantity of '$line'", quantity, parsed.quantity)
        assertEquals("unit of '$line'", unit, parsed.unit)
        assertEquals("name of '$line'", name, parsed.name)
        assertEquals("aisle of '$line'", aisle, parsed.aisle)
    }

    @Test fun gramsOfFlour() = assertParsed("200 g farina", 200.0, MeasurementUnit.GRAM, "farina", Aisle.BREAD_PASTA_AND_RICE)

    @Test fun numberWithoutUnitIsPiece() = assertParsed("2 uova", 2.0, MeasurementUnit.PIECE, "uova", Aisle.DAIRY_AND_EGGS)

    @Test fun clove() = assertParsed("1 spicchio aglio", 1.0, MeasurementUnit.CLOVE, "aglio", Aisle.PRODUCE)

    @Test fun toTaste() = assertParsed("sale q.b.", null, MeasurementUnit.TO_TASTE, "sale", Aisle.DRY_GOODS)

    @Test fun tablespoonsWithDi() = assertParsed("3 cucchiai di olio", 3.0, MeasurementUnit.TABLESPOON, "olio", Aisle.DRY_GOODS)

    @Test fun decimalCommaKilograms() = assertParsed("1,5 kg patate", 1.5, MeasurementUnit.KILOGRAM, "patate", Aisle.PRODUCE)

    @Test fun leadingBullet() = assertParsed("- 300 g zucchine", 300.0, MeasurementUnit.GRAM, "zucchine", Aisle.PRODUCE)

    @Test fun qbWithoutDots() = assertParsed("pepe qb", null, MeasurementUnit.TO_TASTE, "pepe", Aisle.DRY_GOODS)

    @Test fun otherBullets() {
        assertParsed("• 100 ml latte", 100.0, MeasurementUnit.MILLILITER, "latte", Aisle.DAIRY_AND_EGGS)
        assertParsed("* 1 l brodo vegetale", 1.0, MeasurementUnit.LITER, "brodo vegetale", Aisle.DRY_GOODS)
    }

    @Test fun unitAttachedToNumber() = assertParsed("200g farina", 200.0, MeasurementUnit.GRAM, "farina", Aisle.BREAD_PASTA_AND_RICE)

    @Test fun teaspoonsAndPieces() {
        assertParsed("1 cucchiaino di zucchero", 1.0, MeasurementUnit.TEASPOON, "zucchero", Aisle.DRY_GOODS)
        assertParsed("2 pz limoni", 2.0, MeasurementUnit.PIECE, "limoni", Aisle.PRODUCE)
    }

    @Test fun wordStartingWithUnitLetterIsNotAUnit() =
        assertParsed("1 limone", 1.0, MeasurementUnit.PIECE, "limone", Aisle.PRODUCE)

    @Test fun lineWithoutNumberIsToTasteAndFlagged() {
        val parsed = IngredientTextParser.parseLine("basilico fresco")!!
        assertNull(parsed.quantity)
        assertEquals(MeasurementUnit.TO_TASTE, parsed.unit)
        assertEquals("basilico fresco", parsed.name)
        assertTrue(parsed.quantityWasMissing)
        assertFalse(IngredientTextParser.parseLine("sale q.b.")!!.quantityWasMissing)
    }

    @Test fun blankLinesAreSkipped() {
        val parsed = IngredientTextParser.parseLines("200 g farina\n\n   \n2 uova\n")
        assertEquals(listOf("farina", "uova"), parsed.map { it.name })
    }
}
