package com.example.recipes.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RecipeRepositoryTest {

    @get:Rule val databaseRule = DatabaseTestRule()

    private val recipes get() = databaseRule.recipeRepository

    @Test
    fun renamingTagToExistingNameMergesThem() = runTest {
        val spaghettiId = databaseRule.saveSampleRecipe("Spaghetti al pomodoro") // primo, veloce
        val frittataId = databaseRule.saveSampleRecipe("Frittata di zucchine") // secondo, vegetariano
        val risottoId = databaseRule.saveSampleRecipe("Risotto alla zucca") // primo, vegetariano

        recipes.renameTag("vegetariano", "Primo")

        assertEquals(listOf("primo", "secondo", "veloce"), recipes.observeTagNames().first())
        assertEquals(listOf("primo", "veloce"), recipes.getRecipe(spaghettiId)!!.tags)
        assertEquals(listOf("primo", "secondo"), recipes.getRecipe(frittataId)!!.tags)
        assertEquals(listOf("primo"), recipes.getRecipe(risottoId)!!.tags)
    }

    @Test
    fun renamingTagToNewNameKeepsLinks() = runTest {
        val spaghettiId = databaseRule.saveSampleRecipe("Spaghetti al pomodoro")
        recipes.renameTag("veloce", "  In 20 minuti ")
        assertEquals(listOf("in 20 minuti", "primo"), recipes.getRecipe(spaghettiId)!!.tags)
    }

    @Test
    fun deletingRecipeRemovesItsTagLinks() = runTest {
        val spaghettiId = databaseRule.saveSampleRecipe("Spaghetti al pomodoro")
        val frittataId = databaseRule.saveSampleRecipe("Frittata di zucchine")
        databaseRule.mealPlanRepository.addToPlan(spaghettiId)

        recipes.deleteRecipe(spaghettiId)

        val remainingLinks = databaseRule.database.tagDao().getAllRecipeTags()
        assertTrue(remainingLinks.all { it.recipeId == frittataId })
        assertEquals(2, remainingLinks.size)
        assertNull(recipes.getRecipe(spaghettiId))
        assertEquals(0, databaseRule.mealPlanRepository.observePlannedCount().first())
        // Tags no longer used by any recipe are cleaned up.
        assertEquals(listOf("secondo", "vegetariano"), recipes.observeTagNames().first())
    }

    @Test
    fun savingKeepsIngredientOrderAndNormalizesTags() = runTest {
        val spaghettiId = databaseRule.saveSampleRecipe("Spaghetti al pomodoro")
        val saved = recipes.getRecipe(spaghettiId)!!
        assertEquals(
            listOf("spaghetti", "passata di pomodoro", "aglio", "basilico", "olio extravergine", "sale"),
            saved.ingredients.map { it.name },
        )
        val editedId = recipes.saveRecipe(saved.copy(tags = listOf("Primo", "primo ", "Estate")))
        assertEquals(spaghettiId, editedId)
        assertEquals(listOf("estate", "primo"), recipes.getRecipe(spaghettiId)!!.tags)
    }
}
