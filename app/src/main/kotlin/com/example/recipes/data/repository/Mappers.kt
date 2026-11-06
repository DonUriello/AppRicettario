package com.example.recipes.data.repository

import com.example.recipes.data.database.IngredientEntity
import com.example.recipes.data.database.RecipeWithDetails
import com.example.recipes.data.database.ShoppingListRow
import com.example.recipes.domain.Ingredient
import com.example.recipes.domain.Recipe
import com.example.recipes.domain.ShoppingListItem

internal fun RecipeWithDetails.toRecipe(): Recipe = Recipe(
    id = recipe.id,
    title = recipe.title,
    servings = recipe.servings,
    sourceUrl = recipe.sourceUrl,
    instructions = recipe.instructions,
    status = recipe.status,
    ingredients = ingredients.sortedBy { it.position }.map { it.toIngredient() },
    tags = tags.map { it.name }.sorted(),
    plannedServings = mealPlanEntry?.plannedServings,
)

internal fun IngredientEntity.toIngredient(): Ingredient = Ingredient(
    id = id,
    quantity = quantity,
    unit = unit,
    name = name,
    aisle = aisle,
    reviewNote = reviewNote,
)

internal fun ShoppingListRow.toShoppingListItem(): ShoppingListItem = ShoppingListItem(
    name = name,
    baseUnit = baseUnit,
    totalQuantity = totalQuantity,
    aisle = aisle,
    recipeTitles = recipeTitles
        .split(',')
        .map { title -> title.replace(ShoppingListRow.RECIPE_TITLE_SEPARATOR, ",") }
        .sorted(),
    isStaple = isStaple,
    isChecked = isChecked,
)
