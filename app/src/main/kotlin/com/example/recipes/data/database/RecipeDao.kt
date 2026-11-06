package com.example.recipes.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {

    @Transaction
    @Query("SELECT * FROM recipe ORDER BY title COLLATE NOCASE")
    fun observeAllWithDetails(): Flow<List<RecipeWithDetails>>

    @Transaction
    @Query("SELECT * FROM recipe WHERE id = :recipeId")
    fun observeWithDetails(recipeId: String): Flow<RecipeWithDetails?>

    @Transaction
    @Query("SELECT * FROM recipe WHERE id = :recipeId")
    suspend fun getWithDetails(recipeId: String): RecipeWithDetails?

    @Query("SELECT * FROM recipe WHERE id = :recipeId")
    suspend fun getRecipe(recipeId: String): RecipeEntity?

    @Query("SELECT COUNT(*) FROM recipe")
    suspend fun countRecipes(): Int

    @Upsert
    suspend fun upsertRecipe(recipe: RecipeEntity)

    @Query("DELETE FROM recipe WHERE id = :recipeId")
    suspend fun deleteRecipe(recipeId: String)

    @Query("DELETE FROM ingredient WHERE recipe_id = :recipeId")
    suspend fun deleteIngredientsOfRecipe(recipeId: String)

    @Insert
    suspend fun insertIngredients(ingredients: List<IngredientEntity>)
}
