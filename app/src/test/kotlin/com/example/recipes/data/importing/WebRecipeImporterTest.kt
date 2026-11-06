package com.example.recipes.data.importing

import androidx.test.core.app.ApplicationProvider
import com.example.recipes.data.DatabaseTestRule
import com.example.recipes.domain.MeasurementUnit
import com.example.recipes.domain.RecipeStatus
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class WebRecipeImporterTest {

    @get:Rule val databaseRule = DatabaseTestRule()

    private val requestedUrls = mutableListOf<String>()

    private fun importer(downloader: HtmlDownloader) =
        WebRecipeImporter(downloader, databaseRule.recipeRepository, ApplicationProvider.getApplicationContext())

    private fun fixtureDownloader(fixture: String) = HtmlDownloader { url ->
        requestedUrls += url
        Fixtures.read(fixture)
    }

    @Test
    fun savesADraftToReview() = runTest {
        val result = importer(fixtureDownloader("recipe-string-instructions.html"))
            .import("  www.ricette-esempio.it/carbonara ")

        assertEquals(listOf("https://www.ricette-esempio.it/carbonara"), requestedUrls)
        val recipeId = (result as WebImportResult.Imported).recipeId
        val recipe = databaseRule.recipeRepository.getRecipe(recipeId)!!
        assertEquals(RecipeStatus.NEEDS_REVIEW, recipe.status)
        assertEquals("Spaghetti alla carbonara", recipe.title)
        assertEquals(4, recipe.servings)
        assertEquals("https://www.ricette-esempio.it/carbonara", recipe.sourceUrl)
        assertTrue(recipe.instructions.startsWith("1. Taglia il guanciale"))
        assertTrue(recipe.instructions.contains("\n3. Scola la pasta"))

        val spaghetti = recipe.ingredients.first()
        assertEquals(320.0, spaghetti.quantity)
        assertEquals(MeasurementUnit.GRAM, spaghetti.unit)
        assertNull(spaghetti.reviewNote)

        // "q.b." is an explicit "to taste": no doubt there.
        assertNull(recipe.ingredients.single { it.name == "Pepe nero" }.reviewNote)

        // No quantity at all: flagged with the original line.
        val salt = recipe.ingredients.single { it.name == "un pizzico di sale" }
        assertEquals(MeasurementUnit.TO_TASTE, salt.unit)
        assertEquals("Quantità non indicata nella pagina: \"un pizzico di sale\"", salt.reviewNote)
        assertEquals(1, recipe.ingredientsToReviewCount)
    }

    @Test
    fun reportsPagesWithoutRecipe() = runTest {
        assertEquals(WebImportResult.NoRecipeFound, importer(fixtureDownloader("no-recipe.html")).import("https://example.com"))
        assertEquals(0, databaseRule.recipeRepository.countRecipes())
    }

    @Test
    fun reportsDownloadFailures() = runTest {
        val failingDownloader = HtmlDownloader { throw IOException("offline") }
        assertEquals(WebImportResult.DownloadFailed, importer(failingDownloader).import("https://example.com/ricetta"))
    }

    @Test
    fun rejectsTextThatIsNotALink() = runTest {
        val downloader = fixtureDownloader("recipe-graph.html")
        assertEquals(WebImportResult.InvalidUrl, importer(downloader).import("pasta e fagioli"))
        assertEquals(WebImportResult.InvalidUrl, importer(downloader).import("ftp://example.com/ricetta"))
        assertEquals(WebImportResult.InvalidUrl, importer(downloader).import(""))
        assertTrue(requestedUrls.isEmpty())
    }

    @Test
    fun normalizesUrls() {
        assertEquals("https://example.com/", WebRecipeImporter.normalizeUrl("example.com"))
        assertEquals("http://example.com/ricetta", WebRecipeImporter.normalizeUrl("http://example.com/ricetta"))
        assertNull(WebRecipeImporter.normalizeUrl("localhost"))
    }
}
