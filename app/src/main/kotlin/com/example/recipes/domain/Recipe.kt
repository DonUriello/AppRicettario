package com.example.recipes.domain

data class Ingredient(
    val id: String,
    val quantity: Double?,
    val unit: MeasurementUnit,
    val name: String,
    val aisle: Aisle,
    val reviewNote: String? = null,
)

data class Recipe(
    val id: String,
    val title: String,
    val servings: Int,
    val sourceUrl: String?,
    val instructions: String,
    val status: RecipeStatus,
    /** In display order. */
    val ingredients: List<Ingredient>,
    /** Lowercase, sorted. */
    val tags: List<String>,
    /** Null when the recipe is not in "Da cucinare". */
    val plannedServings: Int?,
) {
    val isPlanned: Boolean get() = plannedServings != null
    val isDraft: Boolean get() = status == RecipeStatus.NEEDS_REVIEW
    val ingredientsToReviewCount: Int get() = ingredients.count { it.reviewNote != null }
}

data class PlannedRecipe(
    val recipeId: String,
    val title: String,
    val recipeServings: Int,
    val plannedServings: Int,
)

/** Splits instructions text into steps, dropping any leading "1." / "1)" numbering. */
object InstructionSteps {
    private val leadingNumber = Regex("^\\s*\\d+\\s*[.)]\\s*")

    fun split(instructions: String): List<String> =
        instructions.lines()
            .map { line -> line.replace(leadingNumber, "").trim() }
            .filter { it.isNotEmpty() }
}

/** Scales a quantity from the recipe's base servings to the chosen servings. */
fun scaleQuantity(quantity: Double?, fromServings: Int, toServings: Int): Double? =
    quantity?.let { it * toServings / fromServings }
