package com.example.recipes.ui

import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.recipes.MainActivity
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Acceptance criterion 7, end to end on the real app (Hilt, Room, navigation):
 * create recipe → add to plan → change servings → tick items → "Copia".
 * Screenshots of each step are written to app/build/screenshots for visual review.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w393dp-h852dp-xxhdpi")
class MainFlowTest {

    @get:Rule val composeRule = createAndroidComposeRule<MainActivity>()

    private fun waitForText(text: String, substring: Boolean = false) {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText(text, substring = substring).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun scrollTo(text: String): SemanticsNodeInteraction {
        composeRule.onNode(hasScrollAction()).performScrollToNode(hasText(text))
        return composeRule.onNodeWithText(text)
    }

    private fun textField(label: String): SemanticsNodeInteraction =
        composeRule.onAllNodes(hasSetTextAction() and hasText(label)).onFirst()

    private fun screenshot(name: String) {
        composeRule.waitForIdle()
        val bitmap = composeRule.onRoot().captureToImage().asAndroidBitmap()
        val directory = File("build/screenshots").apply { mkdirs() }
        File(directory, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    @Test
    fun createPlanScaleTickAndCopy() {
        // The debug seed data fills the list in the background.
        waitForText("Risotto alla zucca")
        screenshot("1-recipe-list")

        composeRule.onNodeWithText("Nuova").performClick()
        waitForText("Nuova ricetta")
        composeRule.onNodeWithText("Salva").assertIsNotEnabled()
        textField("Titolo").performTextInput("Pasta e patate")
        textField("Aggiungi tag").performTextInput("primo,")

        scrollTo("Incolla una lista").performClick()
        waitForText("Aggiungi alla ricetta")
        composeRule.onAllNodes(hasSetTextAction() and hasText("Ingredienti")).onFirst()
            .performTextInput("200 g pasta\n1 kg patate\nsale q.b.")
        screenshot("2-paste-list")
        composeRule.onNodeWithText("Aggiungi alla ricetta").performClick()
        composeRule.waitForIdle()
        screenshot("3-editor")

        composeRule.onNodeWithText("Salva").assertIsEnabled().performClick()

        // Recipe detail.
        waitForText("Aggiungi a Da cucinare")
        composeRule.onNodeWithText("1 kg").assertExists()
        screenshot("4-recipe-detail")
        composeRule.onNodeWithText("Aggiungi a Da cucinare").performClick()
        waitForText("Già in Da cucinare")

        composeRule.onNodeWithText("Ricette").performClick()
        waitForText("In programma")
        composeRule.onNodeWithText("Da cucinare (1)").performClick()

        // Meal plan: 2 → 3 servings.
        waitForText("Pasta e patate")
        composeRule.onNodeWithText("Più").performClick()
        waitForText("ricetta per 2")
        screenshot("5-meal-plan")
        composeRule.onNodeWithText("Vai alla lista spesa").performClick()

        // Shopping list, scaled ×1.5.
        waitForText("0 di 2 presi")
        composeRule.onNodeWithText("300 g").assertExists()
        composeRule.onNodeWithText("1,5 kg").assertExists()
        composeRule.onNodeWithText("pasta").performClick()
        waitForText("1 di 2 presi")
        screenshot("6-shopping-list")

        composeRule.onNodeWithText("Copia").performClick()
        val clipboard = ApplicationProvider.getApplicationContext<Context>()
            .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        assertEquals("Frutta e verdura\n- patate, 1,5 kg", clipboard.primaryClip!!.getItemAt(0).text.toString())

        scrollTo("Di solito ce l'hai in casa")
        composeRule.onNodeWithText("sale").assertExists()
        screenshot("7-shopping-list-staples")
    }

    @Test
    fun emptyPlanShowsEmptyState() {
        waitForText("Risotto alla zucca")
        // The bottom bar tab has no count while nothing is planned.
        composeRule.onNode(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab) and hasText("Da cucinare")).performClick()
        waitForText("Non hai ancora scelto cosa cucinare")
        composeRule.onNodeWithText("Vai alle ricette").assertExists()
        screenshot("8-empty-plan")
    }
}
