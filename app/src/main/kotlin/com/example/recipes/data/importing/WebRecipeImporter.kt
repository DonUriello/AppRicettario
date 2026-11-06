package com.example.recipes.data.importing

import android.content.Context
import com.example.recipes.R
import com.example.recipes.data.repository.RecipeRepository
import com.example.recipes.domain.Ingredient
import com.example.recipes.domain.IngredientTextParser
import com.example.recipes.domain.Recipe
import com.example.recipes.domain.RecipeStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

sealed interface WebImportResult {
    data class Imported(val recipeId: String) : WebImportResult
    data object InvalidUrl : WebImportResult
    data object DownloadFailed : WebImportResult
    data object NoRecipeFound : WebImportResult
}

/** Downloads a page, extracts its JSON-LD recipe and saves it as a draft to review (spec §8.1). */
class WebRecipeImporter @Inject constructor(
    private val htmlDownloader: HtmlDownloader,
    private val recipeRepository: RecipeRepository,
    @ApplicationContext private val context: Context,
) {

    suspend fun import(rawUrl: String): WebImportResult {
        val url = normalizeUrl(rawUrl) ?: return WebImportResult.InvalidUrl
        val html = try {
            htmlDownloader.download(url)
        } catch (exception: IOException) {
            return WebImportResult.DownloadFailed
        }
        val importedRecipe = JsonLdRecipeParser.parse(html, url) ?: return WebImportResult.NoRecipeFound
        val draft = buildDraft(importedRecipe) { originalLine ->
            context.getString(R.string.review_note_missing_quantity, originalLine)
        }
        return WebImportResult.Imported(recipeRepository.saveRecipe(draft))
    }

    companion object {
        /** Adds "https://" when missing; returns null when the text is not an http(s) link. */
        fun normalizeUrl(rawUrl: String): String? {
            val trimmed = rawUrl.trim()
            if (trimmed.isEmpty() || trimmed.any { it.isWhitespace() }) return null
            val withScheme = if (trimmed.contains("://")) trimmed else "https://$trimmed"
            val httpUrl = withScheme.toHttpUrlOrNull() ?: return null
            if (!httpUrl.host.contains('.')) return null
            return httpUrl.toString()
        }

        /**
         * Maps an [ImportedRecipe] to a [RecipeStatus.NEEDS_REVIEW] recipe. Ingredients that had no
         * quantity at all get a review note built by [reviewNoteForMissingQuantity] from the original line.
         */
        fun buildDraft(importedRecipe: ImportedRecipe, reviewNoteForMissingQuantity: (String) -> String): Recipe =
            Recipe(
                id = "",
                title = importedRecipe.title,
                servings = importedRecipe.servings,
                sourceUrl = importedRecipe.sourceUrl,
                instructions = importedRecipe.instructionSteps
                    .mapIndexed { index, step -> "${index + 1}. $step" }
                    .joinToString("\n"),
                status = RecipeStatus.NEEDS_REVIEW,
                ingredients = importedRecipe.ingredientLines.mapNotNull { line ->
                    val parsed = IngredientTextParser.parseLine(line) ?: return@mapNotNull null
                    Ingredient(
                        id = "",
                        quantity = parsed.quantity,
                        unit = parsed.unit,
                        name = parsed.name,
                        aisle = parsed.aisle,
                        reviewNote = if (parsed.quantityWasMissing) reviewNoteForMissingQuantity(line) else null,
                    )
                },
                tags = emptyList(),
                plannedServings = null,
            )
    }
}
