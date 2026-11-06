package com.example.recipes.data.importing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class JsonLdRecipeParserTest {

    private fun parse(fixture: String, url: String = "https://example.com/ricetta") =
        JsonLdRecipeParser.parse(Fixtures.read(fixture), url)

    @Test
    fun stringInstructions() {
        val recipe = parse("recipe-string-instructions.html")!!
        assertEquals("Spaghetti alla carbonara", recipe.title)
        assertEquals(4, recipe.servings)
        assertEquals("https://example.com/ricetta", recipe.sourceUrl)
        assertEquals(6, recipe.ingredientLines.size)
        assertEquals("Spaghetti 320 g", recipe.ingredientLines.first())
        assertEquals(
            listOf(
                "Taglia il guanciale a listarelle e rosolalo in padella.",
                "Sbatti i tuorli con il pecorino e il pepe.",
                "Scola la pasta al dente e mantecala fuori dal fuoco: la crema non deve diventare una frittata è importante!",
            ),
            recipe.instructionSteps,
        )
    }

    @Test
    fun howToStepsInATopLevelArrayWithTypeList() {
        val recipe = parse("recipe-howtostep.html")!!
        assertEquals("Pollo al curry", recipe.title)
        assertEquals(6, recipe.servings)
        assertEquals(
            listOf("Taglia il pollo a cubetti.", "Rosola la cipolla.", "Aggiungi curry e latte di cocco e cuoci 20 minuti."),
            recipe.instructionSteps,
        )
    }

    @Test
    fun graphWithSectionsSkippingInvalidBlocks() {
        val recipe = parse("recipe-graph.html")!!
        assertEquals("Lasagne alla bolognese", recipe.title)
        assertEquals(8, recipe.servings)
        assertEquals(listOf("500 g sfoglia per lasagne", "1 l besciamella", "Parmigiano 100 g"), recipe.ingredientLines)
        assertEquals(4, recipe.instructionSteps.size)
        assertEquals("Inforna a 180 gradi per 40 minuti.", recipe.instructionSteps.last())
    }

    @Test
    fun pageWithoutRecipe() {
        assertNull(parse("no-recipe.html"))
        assertNull(JsonLdRecipeParser.parse("<html><body>Niente qui</body></html>", "https://example.com"))
    }

    @Test
    fun missingYieldDefaultsToTwo() {
        val html = """
            <script type="application/ld+json">
            {"@type": "Recipe", "name": "Tè freddo", "recipeIngredient": ["1 l acqua"]}
            </script>
        """.trimIndent()
        val recipe = JsonLdRecipeParser.parse(html, "https://example.com")!!
        assertEquals(JsonLdRecipeParser.DEFAULT_SERVINGS, recipe.servings)
        assertEquals(emptyList<String>(), recipe.instructionSteps)
    }
}
