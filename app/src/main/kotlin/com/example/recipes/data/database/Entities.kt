package com.example.recipes.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.recipes.domain.Aisle
import com.example.recipes.domain.MeasurementUnit
import com.example.recipes.domain.RecipeStatus

@Entity(tableName = "recipe")
data class RecipeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val servings: Int,
    @ColumnInfo(name = "source_url") val sourceUrl: String?,
    val instructions: String,
    val status: RecipeStatus,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)

@Entity(
    tableName = "ingredient",
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipe_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("recipe_id")],
)
data class IngredientEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "recipe_id") val recipeId: String,
    /** Null when [unit] is [MeasurementUnit.TO_TASTE]. */
    val quantity: Double?,
    val unit: MeasurementUnit,
    val name: String,
    val aisle: Aisle,
    val position: Int,
    @ColumnInfo(name = "review_note") val reviewNote: String?,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)

@Entity(tableName = "tag", indices = [Index(value = ["name"], unique = true)])
data class TagEntity(
    @PrimaryKey val id: String,
    /** Unique, lowercase. */
    val name: String,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)

@Entity(
    tableName = "recipe_tag",
    primaryKeys = ["recipe_id", "tag_id"],
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipe_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tag_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("tag_id")],
)
data class RecipeTagEntity(
    @ColumnInfo(name = "recipe_id") val recipeId: String,
    @ColumnInfo(name = "tag_id") val tagId: String,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)

/** One row per recipe currently planned ("Da cucinare"). */
@Entity(
    tableName = "meal_plan",
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipe_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class MealPlanEntryEntity(
    @PrimaryKey @ColumnInfo(name = "recipe_id") val recipeId: String,
    @ColumnInfo(name = "planned_servings") val plannedServings: Int,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)

/** Things always at home (e.g. "sale"). */
@Entity(tableName = "staple_item")
data class StapleItemEntity(
    /** Lowercase. */
    @PrimaryKey val name: String,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)

/** An item ticked off in the shopping list. */
@Entity(tableName = "shopping_list_check", primaryKeys = ["name", "base_unit"])
data class ShoppingListCheckEntity(
    val name: String,
    @ColumnInfo(name = "base_unit") val baseUnit: MeasurementUnit,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)
