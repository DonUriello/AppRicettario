package com.example.recipes.ui

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToKey
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.SavedStateHandle
import androidx.test.core.app.ApplicationProvider
import com.example.recipes.data.DatabaseTestRule
import com.example.recipes.data.importing.Fixtures
import com.example.recipes.data.importing.HtmlDownloader
import com.example.recipes.data.importing.WebImportResult
import com.example.recipes.data.importing.WebRecipeImporter
import com.example.recipes.domain.MeasurementUnit
import com.example.recipes.domain.RecipeStatus
import com.example.recipes.ui.draftreview.DraftReviewScreen
import com.example.recipes.ui.draftreview.DraftReviewViewModel
import com.example.recipes.ui.navigation.Routes
import com.example.recipes.ui.theme.RecipesTheme
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Import a page, then resolve its uncertain rows on the draft review screen. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w393dp-h852dp-xxhdpi")
class DraftReviewTest {

    @get:Rule val databaseRule = DatabaseTestRule()
    @get:Rule val composeRule = createComposeRule()

    private var verifiedRecipeId: String? = null
    private var savedAsDraft = false

    private fun importFixture(fixture: String): String = runBlocking {
        val importer = WebRecipeImporter(
            HtmlDownloader { Fixtures.read(fixture) },
            databaseRule.recipeRepository,
            ApplicationProvider.getApplicationContext(),
        )
        (importer.import("https://www.ricette-esempio.it/carbonara") as WebImportResult.Imported).recipeId
    }

    private fun showReviewScreen(recipeId: String) {
        val viewModel = DraftReviewViewModel(
            SavedStateHandle(mapOf(Routes.RECIPE_ID_ARGUMENT to recipeId)),
            databaseRule.recipeRepository,
        )
        composeRule.setContent {
            RecipesTheme {
                DraftReviewScreen(
                    onCancel = {},
                    onSavedAsDraft = { savedAsDraft = true },
                    onSavedAsVerified = { verifiedRecipeId = it },
                    onEditRest = {},
                    viewModel = viewModel,
                )
            }
        }
    }

    private fun waitForText(text: String) = composeRule.waitUntil(10_000) {
        composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
    }

    /**
     * Waits for a screen callback. The save runs on Room's executor and the event then reaches the
     * composition's effect dispatcher, which `waitUntil` alone does not advance; `waitForIdle` does.
     */
    private fun eventually(condition: () -> Boolean) {
        val deadline = System.currentTimeMillis() + 10_000
        while (true) {
            composeRule.waitForIdle()
            if (condition()) return
            check(System.currentTimeMillis() < deadline) { "Condition not met within 10 s" }
            Thread.sleep(20)
        }
    }

    /** Brings the uncertain row to the top, clear of the fixed bottom button. */
    private fun scrollUncertainRowToTop(recipeId: String) {
        val ingredientId = runBlocking { databaseRule.recipeRepository.getRecipe(recipeId)!! }
            .ingredients.single { it.reviewNote != null }.id
        composeRule.onNode(hasScrollAction()).performScrollToKey(ingredientId)
    }

    /** The banner may have scrolled away; the fixed bottom button is always composed. */
    private fun waitUntilVerifiedSaveIsEnabled() = composeRule.waitUntil(10_000) {
        runCatching { composeRule.onNodeWithText("Salva come verificata").assertIsEnabled() }.isSuccess
    }

    private fun screenshot(name: String) {
        composeRule.waitForIdle()
        val bitmap = composeRule.onRoot().captureToImage().asAndroidBitmap()
        val directory = File("build/screenshots").apply { mkdirs() }
        File(directory, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    @Test
    fun confirmingEveryUncertainRowEnablesSaveAsVerified() {
        val recipeId = importFixture("recipe-string-instructions.html")
        showReviewScreen(recipeId)

        waitForText("1 riga da controllare")
        composeRule.onNodeWithText("Salva come verificata").assertIsNotEnabled()
        screenshot("9-draft-review")
        scrollUncertainRowToTop(recipeId)

        composeRule.onAllNodesWithText("Conferma").onFirst().performClick()
        waitUntilVerifiedSaveIsEnabled()
        composeRule.onNodeWithText("Salva come verificata").assertIsEnabled().performClick()

        eventually { verifiedRecipeId != null }
        val recipe = runBlocking { databaseRule.recipeRepository.getRecipe(recipeId)!! }
        assertEquals(RecipeStatus.VERIFIED, recipe.status)
        assertEquals(0, recipe.ingredientsToReviewCount)
    }

    @Test
    fun removingAnUncertainRowAndSavingAsDraft() {
        val recipeId = importFixture("recipe-string-instructions.html")
        showReviewScreen(recipeId)
        waitForText("1 riga da controllare")

        scrollUncertainRowToTop(recipeId)
        composeRule.onNodeWithText("Rimuovi").performClick()
        waitUntilVerifiedSaveIsEnabled()
        composeRule.onNodeWithText("Salva bozza").performClick()

        eventually { savedAsDraft }
        val recipe = runBlocking { databaseRule.recipeRepository.getRecipe(recipeId)!! }
        assertEquals(RecipeStatus.NEEDS_REVIEW, recipe.status)
        assertFalse(recipe.ingredients.any { it.name == "un pizzico di sale" })
        assertNull(verifiedRecipeId)
    }

    @Test
    fun editingAnUncertainRowThenConfirming() {
        val recipeId = importFixture("recipe-string-instructions.html")
        showReviewScreen(recipeId)
        waitForText("1 riga da controllare")

        scrollUncertainRowToTop(recipeId)
        composeRule.onNodeWithText("Modifica").performClick()
        scrollUncertainRowToTop(recipeId)
        composeRule.onNode(hasText("Quantità") and hasSetTextAction()).performTextInput("1")
        composeRule.onNodeWithText("Conferma").performClick()
        waitUntilVerifiedSaveIsEnabled()
        composeRule.onNodeWithText("Salva come verificata").performClick()

        eventually { verifiedRecipeId != null }
        val salt = runBlocking { databaseRule.recipeRepository.getRecipe(recipeId)!! }
            .ingredients.single { it.name == "un pizzico di sale" }
        assertEquals(1.0, salt.quantity)
        assertEquals(MeasurementUnit.PIECE, salt.unit)
        assertNull(salt.reviewNote)
    }
}
