package com.example.recipes.data.importing

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.jsoup.Jsoup
import org.jsoup.parser.Parser

/** A recipe as found in a web page, before it is turned into a draft. */
data class ImportedRecipe(
    val title: String,
    val servings: Int,
    val ingredientLines: List<String>,
    val instructionSteps: List<String>,
    val sourceUrl: String,
)

/**
 * Finds a schema.org `Recipe` in the `<script type="application/ld+json">` blocks of a page
 * (spec §8.1): at the top level, inside arrays, inside `@graph`, or with `@type` given as a list.
 */
object JsonLdRecipeParser {

    const val DEFAULT_SERVINGS = 2

    private val json = Json { isLenient = true; ignoreUnknownKeys = true }
    private val firstInteger = Regex("\\d+")

    fun parse(html: String, sourceUrl: String): ImportedRecipe? {
        val document = Jsoup.parse(html, sourceUrl)
        return document.select("script[type=application/ld+json]")
            .asSequence()
            .mapNotNull { script -> runCatching { json.parseToJsonElement(script.data().trim()) }.getOrNull() }
            .mapNotNull(::findRecipeObject)
            .mapNotNull { recipeObject -> toImportedRecipe(recipeObject, sourceUrl) }
            .firstOrNull()
    }

    private fun findRecipeObject(element: JsonElement): JsonObject? = when (element) {
        is JsonArray -> element.firstNotNullOfOrNull(::findRecipeObject)
        is JsonObject -> when {
            isRecipe(element) -> element
            else -> element["@graph"]?.let(::findRecipeObject)
                ?: element["mainEntity"]?.let(::findRecipeObject)
        }
        else -> null
    }

    private fun isRecipe(jsonObject: JsonObject): Boolean = when (val type = jsonObject["@type"]) {
        is JsonPrimitive -> type.content.endsWith("Recipe")
        is JsonArray -> type.any { it is JsonPrimitive && it.content.endsWith("Recipe") }
        else -> false
    }

    private fun toImportedRecipe(recipe: JsonObject, sourceUrl: String): ImportedRecipe? {
        val title = recipe["name"].firstText()?.let(::cleanText).orEmpty()
        val ingredientLines = (recipe["recipeIngredient"] ?: recipe["ingredients"]).texts()
            .map(::cleanText)
            .filter { it.isNotEmpty() }
        val instructionSteps = instructionSteps(recipe["recipeInstructions"])
        if (title.isEmpty() && ingredientLines.isEmpty()) return null
        return ImportedRecipe(
            title = title,
            servings = servings(recipe["recipeYield"]),
            ingredientLines = ingredientLines,
            instructionSteps = instructionSteps,
            sourceUrl = sourceUrl,
        )
    }

    /** `recipeYield` may be a number, a string such as "4 persone", or a list of either: take the first integer. */
    private fun servings(element: JsonElement?): Int =
        element.texts()
            .firstNotNullOfOrNull { text -> firstInteger.find(text)?.value?.toIntOrNull() }
            ?.takeIf { it >= 1 }
            ?: DEFAULT_SERVINGS

    /** A string, a list of strings, `HowToStep[]` or `HowToSection[]` (whose steps are flattened). */
    private fun instructionSteps(element: JsonElement?): List<String> = when (element) {
        null, JsonNull -> emptyList()
        is JsonPrimitive -> splitInstructionText(element.content)
        is JsonArray -> element.flatMap(::instructionSteps)
        is JsonObject -> {
            val itemList = element["itemListElement"]
            when {
                itemList != null -> instructionSteps(itemList)
                else -> (element["text"] ?: element["name"]).firstText()?.let(::splitInstructionText).orEmpty()
            }
        }
    }

    /** Instruction text may contain HTML, line breaks or already numbered steps. */
    private fun splitInstructionText(text: String): List<String> {
        val withLineBreaks = text.replace(Regex("(?i)<br\\s*/?>|</p>|</li>"), "\n")
        return withLineBreaks.lines()
            .map { line -> cleanText(line).replace(Regex("^\\d+\\s*[.)]\\s*"), "") }
            .filter { it.isNotEmpty() }
    }

    private fun JsonElement?.texts(): List<String> = when (this) {
        null, JsonNull -> emptyList()
        is JsonPrimitive -> listOf(content)
        is JsonArray -> flatMap { it.texts() }
        is JsonObject -> listOfNotNull((this["text"] ?: this["name"]).firstText())
    }

    private fun JsonElement?.firstText(): String? = texts().firstOrNull()

    /** Strips tags, decodes entities such as `&egrave;` and collapses whitespace. */
    private fun cleanText(text: String): String =
        Jsoup.parse(Parser.unescapeEntities(text, false)).text().replace(' ', ' ').replace(Regex("\\s+"), " ").trim()
}
