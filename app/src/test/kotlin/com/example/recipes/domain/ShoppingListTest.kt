package com.example.recipes.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class ShoppingListTest {

    private fun item(name: String, aisle: Aisle, isStaple: Boolean = false, isChecked: Boolean = false) =
        ShoppingListItem(
            name = name,
            baseUnit = MeasurementUnit.GRAM,
            totalQuantity = 100.0,
            aisle = aisle,
            recipeTitles = listOf("Ricetta"),
            isStaple = isStaple,
            isChecked = isChecked,
        )

    @Test fun groupsByAisleInWalkingOrderAndSeparatesStaples() {
        val shoppingList = ShoppingList.from(
            listOf(
                item("spaghetti", Aisle.BREAD_PASTA_AND_RICE),
                item("zucca", Aisle.PRODUCE),
                item("sale", Aisle.DRY_GOODS, isStaple = true),
                item("aglio", Aisle.PRODUCE, isChecked = true),
            ),
        )
        assertEquals(listOf(Aisle.PRODUCE, Aisle.BREAD_PASTA_AND_RICE), shoppingList.sections.map { it.aisle })
        assertEquals(listOf("aglio", "zucca"), shoppingList.sections.first().items.map { it.name })
        assertEquals(listOf("sale"), shoppingList.staples.map { it.name })
        assertEquals(1, shoppingList.checkedCount)
        assertEquals(3, shoppingList.totalCount)
    }

    @Test fun exportListsUncheckedItemsOnly() {
        val shoppingList = ShoppingList.from(
            listOf(
                item("zucca", Aisle.PRODUCE),
                item("aglio", Aisle.PRODUCE, isChecked = true),
                item("burro", Aisle.DAIRY_AND_EGGS, isChecked = true),
                item("spaghetti", Aisle.BREAD_PASTA_AND_RICE),
            ),
        )
        val text = ShoppingListTextExporter.export(shoppingList) { aisle -> aisle.name }
        assertEquals("PRODUCE\n- zucca, 100 g\n\nBREAD_PASTA_AND_RICE\n- spaghetti, 100 g", text)
    }

    @Test fun instructionStepsDropNumbering() {
        assertEquals(
            listOf("Infarina le fettine.", "Rosolale nel burro."),
            InstructionSteps.split("1. Infarina le fettine.\n\n2) Rosolale nel burro."),
        )
    }
}
