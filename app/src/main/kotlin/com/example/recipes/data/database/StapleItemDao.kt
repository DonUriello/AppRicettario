package com.example.recipes.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StapleItemDao {

    @Query("SELECT * FROM staple_item ORDER BY name")
    fun observeAll(): Flow<List<StapleItemEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(stapleItem: StapleItemEntity)

    @Query("DELETE FROM staple_item WHERE name = :name")
    suspend fun delete(name: String)
}
