package com.example.recipes.data.repository

import androidx.room.withTransaction
import com.example.recipes.data.database.AppDatabase
import com.example.recipes.data.database.IngredientEntity
import com.example.recipes.data.database.RecipeEntity
import com.example.recipes.data.database.RecipeTagEntity
import com.example.recipes.data.database.TagEntity
import com.example.recipes.domain.MeasurementUnit
import com.example.recipes.domain.Recipe
import com.example.recipes.domain.TagNames
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class RecipeRepository @Inject constructor(
    private val database: AppDatabase,
) {
    private val recipeDao = database.recipeDao()
    private val tagDao = database.tagDao()

    fun observeRecipes(): Flow<List<Recipe>> =
        recipeDao.observeAllWithDetails().map { recipes -> recipes.map { it.toRecipe() } }

    fun observeRecipe(recipeId: String): Flow<Recipe?> =
        recipeDao.observeWithDetails(recipeId).map { it?.toRecipe() }

    suspend fun getRecipe(recipeId: String): Recipe? = recipeDao.getWithDetails(recipeId)?.toRecipe()

    suspend fun countRecipes(): Int = recipeDao.countRecipes()

    fun observeTagNames(): Flow<List<String>> = tagDao.observeAll().map { tags -> tags.map { it.name } }

    /**
     * Inserts or replaces a recipe with its ingredients and tags.
     * A blank [Recipe.id] creates a new recipe. Returns the recipe id.
     */
    suspend fun saveRecipe(recipe: Recipe): String {
        val recipeId = recipe.id.ifBlank { newId() }
        val now = System.currentTimeMillis()
        database.withTransaction {
            recipeDao.upsertRecipe(
                RecipeEntity(
                    id = recipeId,
                    title = recipe.title.trim(),
                    servings = recipe.servings.coerceAtLeast(1),
                    sourceUrl = recipe.sourceUrl?.trim()?.ifBlank { null },
                    instructions = recipe.instructions.trim(),
                    status = recipe.status,
                    updatedAt = now,
                ),
            )
            recipeDao.deleteIngredientsOfRecipe(recipeId)
            recipeDao.insertIngredients(
                recipe.ingredients
                    .filter { it.name.isNotBlank() }
                    .mapIndexed { position, ingredient ->
                        IngredientEntity(
                            id = ingredient.id.ifBlank { newId() },
                            recipeId = recipeId,
                            quantity = if (ingredient.unit == MeasurementUnit.TO_TASTE) null else ingredient.quantity,
                            unit = if (ingredient.quantity == null) MeasurementUnit.TO_TASTE else ingredient.unit,
                            name = ingredient.name.trim(),
                            aisle = ingredient.aisle,
                            position = position,
                            reviewNote = ingredient.reviewNote,
                            updatedAt = now,
                        )
                    },
            )
            tagDao.deleteRecipeTagsOfRecipe(recipeId)
            val tagIds = recipe.tags
                .map(TagNames::normalize)
                .filter { it.isNotEmpty() }
                .distinct()
                .map { tagName -> findOrCreateTag(tagName, now).id }
            tagDao.insertRecipeTags(tagIds.map { tagId -> RecipeTagEntity(recipeId, tagId, now) })
            tagDao.deleteUnusedTags()
        }
        return recipeId
    }

    /** Deletes the recipe; ingredients, tag links and the meal plan entry go with it (CASCADE). */
    suspend fun deleteRecipe(recipeId: String) {
        database.withTransaction {
            recipeDao.deleteRecipe(recipeId)
            tagDao.deleteUnusedTags()
        }
    }

    /**
     * Renames a tag. If a tag with the new name already exists, the two are merged:
     * links move to the existing tag and the old tag is deleted.
     */
    suspend fun renameTag(oldName: String, newName: String) {
        val normalizedOldName = TagNames.normalize(oldName)
        val normalizedNewName = TagNames.normalize(newName)
        if (normalizedNewName.isEmpty() || normalizedOldName == normalizedNewName) return
        val now = System.currentTimeMillis()
        database.withTransaction {
            val tag = tagDao.getByName(normalizedOldName) ?: return@withTransaction
            val existingTag = tagDao.getByName(normalizedNewName)
            if (existingTag == null) {
                tagDao.renameTag(tag.id, normalizedNewName, now)
            } else {
                tagDao.copyRecipeTags(sourceTagId = tag.id, targetTagId = existingTag.id, updatedAt = now)
                tagDao.deleteTag(tag.id)
            }
        }
    }

    private suspend fun findOrCreateTag(tagName: String, now: Long): TagEntity =
        tagDao.getByName(tagName) ?: TagEntity(id = newId(), name = tagName, updatedAt = now).also {
            tagDao.insertTag(it)
        }

    private fun newId(): String = UUID.randomUUID().toString()
}
