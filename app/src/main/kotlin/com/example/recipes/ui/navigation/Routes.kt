package com.example.recipes.ui.navigation

import android.net.Uri

object Routes {
    const val RECIPES = "recipes"
    const val RECIPE_DETAIL = "recipes/{recipeId}"
    const val RECIPE_NEW = "recipes/new?sourceUrl={sourceUrl}"
    const val RECIPE_EDIT = "recipes/{recipeId}/edit"
    const val RECIPE_REVIEW = "recipes/{recipeId}/review"
    const val PLAN = "plan"
    const val SHOPPING = "shopping"

    const val RECIPE_ID_ARGUMENT = "recipeId"
    const val SOURCE_URL_ARGUMENT = "sourceUrl"

    fun recipeDetail(recipeId: String) = "recipes/$recipeId"
    fun recipeEdit(recipeId: String) = "recipes/$recipeId/edit"
    fun recipeReview(recipeId: String) = "recipes/$recipeId/review"
    fun newRecipe(sourceUrl: String? = null) =
        if (sourceUrl == null) "recipes/new" else "recipes/new?sourceUrl=${Uri.encode(sourceUrl)}"
}
