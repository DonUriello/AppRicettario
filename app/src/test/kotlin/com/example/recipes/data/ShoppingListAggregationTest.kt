package com.example.recipes.data

import com.example.recipes.domain.Ingredient
import com.example.recipes.domain.IngredientTextParser
import com.example.recipes.domain.MeasurementUnit
import com.example.recipes.domain.Recipe
import com.example.recipes.domain.RecipeStatus
import com.example.recipes.domain.ShoppingList
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ShoppingListAggregationTest {

    @get:Rule val databaseRule = DatabaseTestRule()

    private val mealPlan get() = databaseRule.mealPlanRepository
    private val shoppingList get() = databaseRule.shoppingListRepository

    private fun recipeFromLines(title: String, vararg lines: String) = Recipe(
        id = "",
        title = title,
        servings = 2,
        sourceUrl = null,
        instructions = "",
        status = RecipeStatus.VERIFIED,
        ingredients = lines.mapNotNull(IngredientTextParser::parseLine).map { parsed ->
            Ingredient(id = "", quantity = parsed.quantity, unit = parsed.unit, name = parsed.name, aisle = parsed.aisle)
        },
        tags = emptyList(),
        plannedServings = null,
    )

    @Test
    fun scalingAndUnits() = runTest {
        val spaghettiId = databaseRule.saveSampleRecipe("Spaghetti al pomodoro")
        val risottoId = databaseRule.saveSampleRecipe("Risotto alla zucca")
        mealPlan.addToPlan(spaghettiId, plannedServings = 2)
        mealPlan.addToPlan(risottoId, plannedServings = 4)

        val items = shoppingList.observeShoppingListItems().first()
        val itemsToBuy = items.filterNot { it.isStaple }.map { Triple(it.name, it.totalQuantity, it.baseUnit) }
        val expected = listOf(
            Triple("spaghetti", 180.0, MeasurementUnit.GRAM),
            Triple("passata di pomodoro", 400.0, MeasurementUnit.GRAM),
            Triple("aglio", 1.0, MeasurementUnit.CLOVE),
            Triple("basilico", null, MeasurementUnit.TO_TASTE),
            Triple("riso carnaroli", 320.0, MeasurementUnit.GRAM),
            Triple("zucca", 800.0, MeasurementUnit.GRAM),
            Triple("cipolla", 2.0, MeasurementUnit.PIECE),
            Triple("brodo vegetale", 2000.0, MeasurementUnit.MILLILITER),
            Triple("parmigiano", 80.0, MeasurementUnit.GRAM),
            Triple("burro", 40.0, MeasurementUnit.GRAM),
        )
        assertEquals(10, itemsToBuy.size)
        assertEquals(expected.sortedBy { it.first }, itemsToBuy.sortedBy { it.first })

        assertEquals(listOf("olio extravergine", "sale"), items.filter { it.isStaple }.map { it.name }.sorted())
        assertEquals("2 l", items.single { it.name == "brodo vegetale" }.displayQuantity)
        assertEquals("q.b.", items.single { it.name == "basilico" }.displayQuantity)
    }

    @Test
    fun aggregationAcrossRecipes() = runTest {
        mealPlan.addToPlan(databaseRule.saveSampleRecipe("Frittata di zucchine"), plannedServings = 2)
        mealPlan.addToPlan(databaseRule.saveSampleRecipe("Risotto alla zucca"), plannedServings = 2)
        databaseRule.recipeRepository.saveRecipe(recipeFromLines("Non pianificata", "1 cipolla rossa"))
        mealPlan.addToPlan(databaseRule.saveSampleRecipe("Insalata di ceci"), plannedServings = 2)

        val items = shoppingList.observeShoppingListItems().first()
        val parmigiano = items.single { it.name == "parmigiano" }
        assertEquals("80 g", parmigiano.displayQuantity)
        assertEquals(listOf("Frittata di zucchine", "Risotto alla zucca"), parmigiano.recipeTitles)

        // Known limitation (§12): synonyms are not merged.
        assertEquals(1.0, items.single { it.name == "cipolla" }.totalQuantity)
        assertEquals(1.0, items.single { it.name == "cipolla rossa" }.totalQuantity)
    }

    @Test
    fun kilogramsMergeWithGrams() = runTest {
        val firstId = databaseRule.recipeRepository.saveRecipe(recipeFromLines("Pane", "500 g farina"))
        val secondId = databaseRule.recipeRepository.saveRecipe(recipeFromLines("Pizza, versione grande", "1 kg farina"))
        mealPlan.addToPlan(firstId)
        mealPlan.addToPlan(secondId)

        val farina = shoppingList.observeShoppingListItems().first().filter { it.name == "farina" }
        assertEquals(1, farina.size)
        assertEquals(MeasurementUnit.GRAM, farina.single().baseUnit)
        assertEquals("1,5 kg", farina.single().displayQuantity)
        // Commas inside titles survive GROUP_CONCAT.
        assertEquals(listOf("Pane", "Pizza, versione grande"), farina.single().recipeTitles)
    }

    @Test
    fun changingPlannedServingsReEmits() = runTest {
        val risottoId = databaseRule.saveSampleRecipe("Risotto alla zucca")
        mealPlan.addToPlan(risottoId, plannedServings = 2)
        val flow = shoppingList.observeShoppingListItems()
        assertEquals(400.0, flow.first().single { it.name == "zucca" }.totalQuantity)

        mealPlan.updatePlannedServings(risottoId, 3)
        assertEquals(600.0, flow.first { list -> list.single { it.name == "zucca" }.totalQuantity == 600.0 }
            .single { it.name == "zucca" }.totalQuantity)
    }

    @Test
    fun checksAreKeyedByNameAndUnitAndClearedWithThePlan() = runTest {
        val risottoId = databaseRule.saveSampleRecipe("Risotto alla zucca")
        mealPlan.addToPlan(risottoId)
        val zucca = shoppingList.observeShoppingListItems().first().single { it.name == "zucca" }
        shoppingList.setChecked(zucca, isChecked = true)

        val checkedList = ShoppingList.from(shoppingList.observeShoppingListItems().first())
        assertEquals(1, checkedList.checkedCount)
        assertEquals(6, checkedList.totalCount)

        mealPlan.clearPlan()
        mealPlan.addToPlan(risottoId)
        assertFalse(shoppingList.observeShoppingListItems().first().single { it.name == "zucca" }.isChecked)
    }

    @Test
    fun draftsCannotBePlanned() = runTest {
        val draftId = databaseRule.recipeRepository.saveRecipe(
            recipeFromLines("Bozza", "100 g farina").copy(status = RecipeStatus.NEEDS_REVIEW),
        )
        assertFalse(mealPlan.addToPlan(draftId))
        assertNull(databaseRule.recipeRepository.getRecipe(draftId)!!.plannedServings)
    }

    @Test
    fun staplesCanBeEdited() = runTest {
        mealPlan.addToPlan(databaseRule.saveSampleRecipe("Pollo al limone"))
        shoppingList.addStaple("Farina")
        shoppingList.removeStaple("sale")
        val items = shoppingList.observeShoppingListItems().first()
        assertEquals(listOf("farina"), items.filter { it.isStaple }.map { it.name })
        assertFalse(items.single { it.name == "sale" }.isStaple)
    }
}
