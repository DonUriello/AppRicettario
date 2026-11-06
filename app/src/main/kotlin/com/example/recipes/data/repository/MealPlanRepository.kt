package com.example.recipes.data.repository

import androidx.room.withTransaction
import com.example.recipes.data.database.AppDatabase
import com.example.recipes.data.database.MealPlanEntryEntity
import com.example.recipes.domain.PlannedRecipe
import com.example.recipes.domain.RecipeStatus
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class MealPlanRepository @Inject constructor(
    private val database: AppDatabase,
) {
    private val mealPlanDao = database.mealPlanDao()
    private val recipeDao = database.recipeDao()

    fun observePlannedRecipes(): Flow<List<PlannedRecipe>> =
        mealPlanDao.observePlannedRecipes().map { rows ->
            rows.map { row ->
                PlannedRecipe(
                    recipeId = row.recipeId,
                    title = row.title,
                    recipeServings = row.recipeServings,
                    plannedServings = row.plannedServings,
                )
            }
        }

    fun observePlannedCount(): Flow<Int> = mealPlanDao.observePlannedCount()

    /**
     * Adds a recipe to "Da cucinare". Only verified recipes can be planned.
     * Returns false when the recipe does not exist or is still a draft.
     */
    suspend fun addToPlan(recipeId: String, plannedServings: Int? = null): Boolean {
        val recipe = recipeDao.getRecipe(recipeId) ?: return false
        if (recipe.status != RecipeStatus.VERIFIED) return false
        mealPlanDao.upsertEntry(
            MealPlanEntryEntity(
                recipeId = recipeId,
                plannedServings = (plannedServings ?: recipe.servings).coerceAtLeast(1),
                updatedAt = System.currentTimeMillis(),
            ),
        )
        return true
    }

    suspend fun updatePlannedServings(recipeId: String, plannedServings: Int) {
        mealPlanDao.updatePlannedServings(recipeId, plannedServings.coerceAtLeast(1), System.currentTimeMillis())
    }

    suspend fun removeFromPlan(recipeId: String) = mealPlanDao.deleteEntry(recipeId)

    /** Empties the plan and clears the shopping list checks. */
    suspend fun clearPlan() {
        database.withTransaction {
            mealPlanDao.deleteAllEntries()
            database.shoppingListDao().deleteAllChecks()
        }
    }
}
