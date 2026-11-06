package com.example.recipes.data.database

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.recipes.domain.Aisle
import com.example.recipes.domain.MeasurementUnit

data class RecipeWithDetails(
    @Embedded val recipe: RecipeEntity,
    @Relation(parentColumn = "id", entityColumn = "recipe_id")
    val ingredients: List<IngredientEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(RecipeTagEntity::class, parentColumn = "recipe_id", entityColumn = "tag_id"),
    )
    val tags: List<TagEntity>,
    @Relation(parentColumn = "id", entityColumn = "recipe_id")
    val mealPlanEntry: MealPlanEntryEntity?,
)

data class ShoppingListRow(
    val name: String,
    @ColumnInfo(name = "base_unit") val baseUnit: MeasurementUnit,
    @ColumnInfo(name = "total_quantity") val totalQuantity: Double?,
    val aisle: Aisle,
    /** Recipe titles joined with [RECIPE_TITLE_SEPARATOR]. */
    @ColumnInfo(name = "recipe_titles") val recipeTitles: String,
    @ColumnInfo(name = "is_staple") val isStaple: Boolean,
    @ColumnInfo(name = "is_checked") val isChecked: Boolean,
) {
    companion object {
        /** Unit separator character: never typed in a recipe title. */
        const val RECIPE_TITLE_SEPARATOR = "\u001F"
    }
}
