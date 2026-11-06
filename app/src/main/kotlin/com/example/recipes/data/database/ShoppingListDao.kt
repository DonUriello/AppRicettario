package com.example.recipes.data.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.recipes.domain.MeasurementUnit
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingListDao {

    /**
     * Aggregates the ingredients of every planned recipe (spec §5.1).
     *
     * - The alias is `base_unit`, not `unit`: an alias equal to a column name would make
     *   `GROUP BY` resolve to the column and kilograms would not merge with grams.
     * - `TO_TASTE` rows have a null quantity, so `SUM` stays null.
     * - Staples are flagged with `is_staple` instead of being filtered out, so one query feeds both sections.
     * - Commas inside titles are swapped for the unit separator because `GROUP_CONCAT(DISTINCT ...)`
     *   only supports the default comma separator; [ShoppingListRow] restores them.
     */
    @Query(
        """
        SELECT LOWER(ingredient.name) AS name,
               CASE ingredient.unit
                   WHEN 'KILOGRAM' THEN 'GRAM'
                   WHEN 'LITER'    THEN 'MILLILITER'
                   ELSE ingredient.unit
               END AS base_unit,
               SUM(
                   ingredient.quantity
                   * CASE WHEN ingredient.unit IN ('KILOGRAM', 'LITER') THEN 1000 ELSE 1 END
                   * meal_plan.planned_servings * 1.0 / recipe.servings
               ) AS total_quantity,
               MIN(ingredient.aisle) AS aisle,
               GROUP_CONCAT(DISTINCT REPLACE(recipe.title, ',', char(31))) AS recipe_titles,
               MAX(LOWER(ingredient.name) IN (SELECT staple_item.name FROM staple_item)) AS is_staple,
               MAX(EXISTS(
                   SELECT 1 FROM shopping_list_check
                   WHERE shopping_list_check.name = LOWER(ingredient.name)
                     AND shopping_list_check.base_unit = CASE ingredient.unit
                         WHEN 'KILOGRAM' THEN 'GRAM'
                         WHEN 'LITER'    THEN 'MILLILITER'
                         ELSE ingredient.unit
                     END
               )) AS is_checked
        FROM ingredient
        JOIN recipe    ON recipe.id = ingredient.recipe_id
        JOIN meal_plan ON meal_plan.recipe_id = recipe.id
        GROUP BY LOWER(ingredient.name), base_unit
        ORDER BY name
        """,
    )
    fun observeShoppingListRows(): Flow<List<ShoppingListRow>>

    @Upsert
    suspend fun upsertCheck(check: ShoppingListCheckEntity)

    @Query("DELETE FROM shopping_list_check WHERE name = :name AND base_unit = :baseUnit")
    suspend fun deleteCheck(name: String, baseUnit: MeasurementUnit)

    @Query("DELETE FROM shopping_list_check")
    suspend fun deleteAllChecks()
}
