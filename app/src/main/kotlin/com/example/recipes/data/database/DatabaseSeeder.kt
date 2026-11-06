package com.example.recipes.data.database

import com.example.recipes.data.repository.RecipeRepository
import com.example.recipes.domain.Ingredient
import com.example.recipes.domain.IngredientTextParser
import com.example.recipes.domain.Recipe
import com.example.recipes.domain.RecipeStatus
import javax.inject.Inject

/** Sample recipes for debug builds (spec §10). Default staples are inserted by [AppDatabase] itself. */
class DatabaseSeeder @Inject constructor(
    private val recipeRepository: RecipeRepository,
) {
    suspend fun seedIfEmpty() {
        if (recipeRepository.countRecipes() > 0) return
        sampleRecipes.forEach { recipeRepository.saveRecipe(it) }
    }

    companion object {
        val sampleRecipes: List<Recipe> = listOf(
            sampleRecipe(
                title = "Spaghetti al pomodoro",
                sourceUrl = "https://www.giallozafferano.it/",
                tags = listOf("primo", "veloce"),
                instructions = "1. Soffriggi l'aglio nell'olio a fuoco basso.\n" +
                    "2. Aggiungi la passata e cuoci per 15 minuti.\n" +
                    "3. Scola gli spaghetti al dente e mantecali con il sugo.\n" +
                    "4. Basilico fresco a fine cottura.",
                ingredientLines = listOf(
                    "180 g spaghetti", "400 g passata di pomodoro", "1 spicchio aglio", "basilico q.b.",
                    "olio extravergine q.b.", "sale q.b.",
                ),
            ),
            sampleRecipe(
                title = "Frittata di zucchine",
                tags = listOf("secondo", "vegetariano"),
                ingredientLines = listOf(
                    "4 uova", "300 g zucchine", "40 g parmigiano", "olio extravergine q.b.", "sale q.b.",
                ),
            ),
            sampleRecipe(
                title = "Pollo al limone",
                tags = listOf("secondo"),
                instructions = "1. Infarina le fettine.\n2. Rosolale nel burro.\n3. Sfuma con il succo di limone.",
                ingredientLines = listOf("400 g petto di pollo", "1 limone", "30 g farina", "20 g burro", "sale q.b."),
            ),
            sampleRecipe(
                title = "Risotto alla zucca",
                tags = listOf("primo", "vegetariano"),
                ingredientLines = listOf(
                    "160 g riso carnaroli", "400 g zucca", "1 cipolla", "1 l brodo vegetale", "40 g parmigiano",
                    "20 g burro",
                ),
            ),
            sampleRecipe(
                title = "Insalata di ceci",
                tags = listOf("piatto unico", "veloce"),
                ingredientLines = listOf(
                    "400 g ceci lessati", "200 g pomodorini", "1 cipolla rossa", "olio extravergine q.b.", "sale q.b.",
                ),
            ),
        )

        private fun sampleRecipe(
            title: String,
            tags: List<String>,
            ingredientLines: List<String>,
            instructions: String = "",
            sourceUrl: String? = null,
            servings: Int = 2,
        ) = Recipe(
            id = "",
            title = title,
            servings = servings,
            sourceUrl = sourceUrl,
            instructions = instructions,
            status = RecipeStatus.VERIFIED,
            ingredients = ingredientLines.mapNotNull(IngredientTextParser::parseLine).map { parsed ->
                Ingredient(
                    id = "",
                    quantity = parsed.quantity,
                    unit = parsed.unit,
                    name = parsed.name,
                    aisle = parsed.aisle,
                )
            },
            tags = tags,
            plannedServings = null,
        )
    }
}
