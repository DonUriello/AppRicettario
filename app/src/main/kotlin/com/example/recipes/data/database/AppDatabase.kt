package com.example.recipes.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        RecipeEntity::class,
        IngredientEntity::class,
        TagEntity::class,
        RecipeTagEntity::class,
        MealPlanEntryEntity::class,
        StapleItemEntity::class,
        ShoppingListCheckEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun tagDao(): TagDao
    abstract fun mealPlanDao(): MealPlanDao
    abstract fun shoppingListDao(): ShoppingListDao
    abstract fun stapleItemDao(): StapleItemDao

    companion object {
        private const val DATABASE_NAME = "recipes.db"

        val DEFAULT_STAPLES = listOf("sale", "olio extravergine", "pepe")

        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                .addCallback(DefaultStaplesCallback)
                .build()

        fun createInMemory(context: Context): AppDatabase =
            Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
                .addCallback(DefaultStaplesCallback)
                .build()
    }

    /** Inserts the default staples once, when the database file is first created. */
    private object DefaultStaplesCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            val now = System.currentTimeMillis()
            DEFAULT_STAPLES.forEach { stapleName ->
                db.execSQL(
                    "INSERT OR IGNORE INTO staple_item (name, updated_at) VALUES (?, ?)",
                    arrayOf<Any>(stapleName, now),
                )
            }
        }
    }
}
