package com.example.recipes.data.repository

import com.example.recipes.data.database.AppDatabase
import com.example.recipes.data.database.ShoppingListCheckEntity
import com.example.recipes.data.database.StapleItemEntity
import com.example.recipes.domain.ShoppingList
import com.example.recipes.domain.ShoppingListItem
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class ShoppingListRepository @Inject constructor(
    database: AppDatabase,
) {
    private val shoppingListDao = database.shoppingListDao()
    private val stapleItemDao = database.stapleItemDao()

    /** Every aggregated row, staples included (flagged with [ShoppingListItem.isStaple]). */
    fun observeShoppingListItems(): Flow<List<ShoppingListItem>> =
        shoppingListDao.observeShoppingListRows().map { rows -> rows.map { it.toShoppingListItem() } }

    fun observeShoppingList(): Flow<ShoppingList> = observeShoppingListItems().map(ShoppingList::from)

    suspend fun setChecked(item: ShoppingListItem, isChecked: Boolean) {
        if (isChecked) {
            shoppingListDao.upsertCheck(
                ShoppingListCheckEntity(
                    name = item.name,
                    baseUnit = item.baseUnit,
                    updatedAt = System.currentTimeMillis(),
                ),
            )
        } else {
            shoppingListDao.deleteCheck(item.name, item.baseUnit)
        }
    }

    suspend fun clearChecks() = shoppingListDao.deleteAllChecks()

    fun observeStapleNames(): Flow<List<String>> =
        stapleItemDao.observeAll().map { staples -> staples.map { it.name } }

    suspend fun addStaple(name: String) {
        val normalizedName = name.trim().lowercase()
        if (normalizedName.isEmpty()) return
        stapleItemDao.insert(StapleItemEntity(name = normalizedName, updatedAt = System.currentTimeMillis()))
    }

    suspend fun removeStaple(name: String) = stapleItemDao.delete(name)
}
