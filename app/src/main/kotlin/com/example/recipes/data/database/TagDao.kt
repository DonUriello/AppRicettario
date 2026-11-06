package com.example.recipes.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

    @Query("SELECT * FROM tag ORDER BY name")
    fun observeAll(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tag WHERE name = :name")
    suspend fun getByName(name: String): TagEntity?

    @Query("SELECT * FROM tag ORDER BY name")
    suspend fun getAll(): List<TagEntity>

    @Insert
    suspend fun insertTag(tag: TagEntity)

    @Query("UPDATE tag SET name = :newName, updated_at = :updatedAt WHERE id = :tagId")
    suspend fun renameTag(tagId: String, newName: String, updatedAt: Long)

    @Query("DELETE FROM tag WHERE id = :tagId")
    suspend fun deleteTag(tagId: String)

    @Query("DELETE FROM tag WHERE id NOT IN (SELECT tag_id FROM recipe_tag)")
    suspend fun deleteUnusedTags()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRecipeTags(recipeTags: List<RecipeTagEntity>)

    @Query("DELETE FROM recipe_tag WHERE recipe_id = :recipeId")
    suspend fun deleteRecipeTagsOfRecipe(recipeId: String)

    @Query("SELECT * FROM recipe_tag")
    suspend fun getAllRecipeTags(): List<RecipeTagEntity>

    /** Copies every link of [sourceTagId] onto [targetTagId], skipping recipes that already have the target. */
    @Query(
        """
        INSERT OR IGNORE INTO recipe_tag (recipe_id, tag_id, updated_at)
        SELECT recipe_id, :targetTagId, :updatedAt FROM recipe_tag WHERE tag_id = :sourceTagId
        """,
    )
    suspend fun copyRecipeTags(sourceTagId: String, targetTagId: String, updatedAt: Long)
}
