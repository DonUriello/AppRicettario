package com.example.recipes.data.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

data class PlannedRecipeRow(
    val recipeId: String,
    val title: String,
    val recipeServings: Int,
    val plannedServings: Int,
)

@Dao
interface MealPlanDao {

    @Query(
        """
        SELECT recipe.id AS recipeId, recipe.title AS title, recipe.servings AS recipeServings,
               meal_plan.planned_servings AS plannedServings
        FROM meal_plan
        JOIN recipe ON recipe.id = meal_plan.recipe_id
        ORDER BY recipe.title COLLATE NOCASE
        """,
    )
    fun observePlannedRecipes(): Flow<List<PlannedRecipeRow>>

    @Query("SELECT COUNT(*) FROM meal_plan")
    fun observePlannedCount(): Flow<Int>

    @Upsert
    suspend fun upsertEntry(entry: MealPlanEntryEntity)

    @Query("UPDATE meal_plan SET planned_servings = :plannedServings, updated_at = :updatedAt WHERE recipe_id = :recipeId")
    suspend fun updatePlannedServings(recipeId: String, plannedServings: Int, updatedAt: Long)

    @Query("DELETE FROM meal_plan WHERE recipe_id = :recipeId")
    suspend fun deleteEntry(recipeId: String)

    @Query("DELETE FROM meal_plan")
    suspend fun deleteAllEntries()
}
