package com.example.recipes.ui

import android.content.Intent
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.example.recipes.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** "Condividi → Ricettario" from another app opens the import with the shared text. */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w393dp-h852dp-xxhdpi")
class ShareIntentTest {

    @get:Rule val composeRule = createEmptyComposeRule()

    private fun shareIntent(text: String) =
        Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
            .setAction(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_TEXT, text)

    private fun waitForText(text: String) = composeRule.waitUntil(10_000) {
        composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
    }

    @Test
    fun sharedTextWithoutLinkIsShownWithAnExplanation() {
        ActivityScenario.launch<MainActivity>(shareIntent("Ricordati di comprare il pane")).use {
            waitForText("Questo non sembra un link valido.")
            composeRule.onNodeWithText("Ricordati di comprare il pane").assertExists()
        }
    }

    @Test
    fun onlyTheLinkIsKeptFromSharedText() {
        // ".invalid" never resolves, so no real page is downloaded; the import ends with a download error.
        ActivityScenario.launch<MainActivity>(shareIntent("Pasta e patate della nonna https://ricette.invalid/pasta-e-patate")).use {
            waitForText("https://ricette.invalid/pasta-e-patate")
            composeRule.onNodeWithText("Pasta e patate della nonna", substring = true).assertDoesNotExist()
        }
    }
}
