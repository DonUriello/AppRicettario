package com.example.recipes.ui.navigation

object Routes {
    const val RECIPES = "recipes"
    const val RECIPE_DETAIL = "recipes/{recipeId}"
    const val RECIPE_NEW = "recipes/new"
    const val RECIPE_EDIT = "recipes/{recipeId}/edit"
    const val PLAN = "plan"
    const val SHOPPING = "shopping"

    const val RECIPE_ID_ARGUMENT = "recipeId"

    fun recipeDetail(recipeId: String) = "recipes/$recipeId"
    fun recipeEdit(recipeId: String) = "recipes/$recipeId/edit"
}
