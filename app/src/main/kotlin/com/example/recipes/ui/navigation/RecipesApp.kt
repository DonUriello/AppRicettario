package com.example.recipes.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.recipes.R
import com.example.recipes.ui.components.BottomBarItem
import com.example.recipes.ui.components.FloatingBottomBar
import com.example.recipes.ui.mealplan.MealPlanScreen
import com.example.recipes.ui.recipedetail.RecipeDetailScreen
import com.example.recipes.ui.recipeeditor.RecipeEditorScreen
import com.example.recipes.ui.recipelist.RecipeListScreen
import com.example.recipes.ui.shoppinglist.ShoppingListScreen
import com.example.recipes.ui.theme.GradientBackground

private val bottomBarRoutes = setOf(Routes.RECIPES, Routes.PLAN, Routes.SHOPPING)

@Composable
fun RecipesApp(appViewModel: AppViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val plannedCount by appViewModel.plannedCount.collectAsStateWithLifecycle()

    GradientBackground {
        NavHost(navController = navController, startDestination = Routes.RECIPES, modifier = Modifier.fillMaxSize()) {
            composable(Routes.RECIPES) {
                RecipeListScreen(
                    onRecipeClick = { navController.navigate(Routes.recipeDetail(it)) },
                    onNewRecipeClick = { navController.navigate(Routes.RECIPE_NEW) },
                    // Draft review arrives in phase 2; until then drafts open in the editor.
                    onReviewDraftClick = { navController.navigate(Routes.recipeEdit(it)) },
                )
            }
            composable(Routes.RECIPE_NEW) {
                RecipeEditorScreen(
                    onCancel = { navController.popBackStack() },
                    onSaved = { recipeId ->
                        navController.navigate(Routes.recipeDetail(recipeId)) {
                            popUpTo(Routes.RECIPE_NEW) { inclusive = true }
                        }
                    },
                    onDeleted = { navController.popBackStack(Routes.RECIPES, inclusive = false) },
                )
            }
            composable(
                Routes.RECIPE_DETAIL,
                arguments = listOf(navArgument(Routes.RECIPE_ID_ARGUMENT) { type = NavType.StringType }),
            ) {
                RecipeDetailScreen(
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(Routes.recipeEdit(it)) },
                )
            }
            composable(
                Routes.RECIPE_EDIT,
                arguments = listOf(navArgument(Routes.RECIPE_ID_ARGUMENT) { type = NavType.StringType }),
            ) {
                RecipeEditorScreen(
                    onCancel = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                    onDeleted = { navController.popBackStack(Routes.RECIPES, inclusive = false) },
                )
            }
            composable(Routes.PLAN) {
                MealPlanScreen(
                    onRecipeClick = { navController.navigate(Routes.recipeDetail(it)) },
                    onGoToRecipes = { navController.navigateToTab(Routes.RECIPES) },
                    onGoToShoppingList = { navController.navigateToTab(Routes.SHOPPING) },
                )
            }
            composable(Routes.SHOPPING) {
                ShoppingListScreen(onGoToRecipes = { navController.navigateToTab(Routes.RECIPES) })
            }
        }

        if (currentRoute in bottomBarRoutes) {
            val items = listOf(
                BottomBarItem(Routes.RECIPES, stringResource(R.string.navigation_recipes), R.drawable.ic_recipes),
                BottomBarItem(
                    Routes.PLAN,
                    if (plannedCount > 0) {
                        stringResource(R.string.navigation_plan_with_count, plannedCount)
                    } else {
                        stringResource(R.string.navigation_plan)
                    },
                    R.drawable.ic_plan,
                ),
                BottomBarItem(Routes.SHOPPING, stringResource(R.string.navigation_shopping), R.drawable.ic_shopping),
            )
            Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                FloatingBottomBar(
                    items = items,
                    selectedRoute = currentRoute,
                    onItemClick = { navController.navigateToTab(it.route) },
                )
            }
        }
    }
}

private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
