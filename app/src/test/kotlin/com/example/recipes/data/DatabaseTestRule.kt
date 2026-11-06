package com.example.recipes.data

import androidx.test.core.app.ApplicationProvider
import com.example.recipes.data.database.AppDatabase
import com.example.recipes.data.database.DatabaseSeeder
import com.example.recipes.data.repository.MealPlanRepository
import com.example.recipes.data.repository.RecipeRepository
import com.example.recipes.data.repository.ShoppingListRepository
import org.junit.rules.ExternalResource

/** A fresh in-memory database (with the default staples) and the repositories on top of it. */
class DatabaseTestRule : ExternalResource() {
    lateinit var database: AppDatabase
    lateinit var recipeRepository: RecipeRepository
    lateinit var mealPlanRepository: MealPlanRepository
    lateinit var shoppingListRepository: ShoppingListRepository

    override fun before() {
        database = AppDatabase.createInMemory(ApplicationProvider.getApplicationContext())
        recipeRepository = RecipeRepository(database)
        mealPlanRepository = MealPlanRepository(database)
        shoppingListRepository = ShoppingListRepository(database)
    }

    override fun after() {
        database.close()
    }

    /** Saves the sample recipe with [title] from the seed data and returns its id. */
    suspend fun saveSampleRecipe(title: String): String =
        recipeRepository.saveRecipe(DatabaseSeeder.sampleRecipes.single { it.title == title })
}
