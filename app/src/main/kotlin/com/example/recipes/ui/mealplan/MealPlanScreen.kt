package com.example.recipes.ui.mealplan

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.recipes.R
import com.example.recipes.domain.PlannedRecipe
import com.example.recipes.ui.components.CardTitle
import com.example.recipes.ui.components.EmptyStateCard
import com.example.recipes.ui.components.GlassCard
import com.example.recipes.ui.components.LabelButton
import com.example.recipes.ui.components.MountainScreen
import com.example.recipes.ui.components.PillButton
import com.example.recipes.ui.components.PillStyle
import com.example.recipes.ui.components.PrimaryButton
import com.example.recipes.ui.components.ScreenTitle
import com.example.recipes.ui.components.ServingsStepper
import com.example.recipes.ui.theme.RecipesColors

@Composable
fun MealPlanScreen(
    onRecipeClick: (String) -> Unit,
    onGoToRecipes: () -> Unit,
    onGoToShoppingList: () -> Unit,
    viewModel: MealPlanViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MountainScreen {
        item {
            ScreenTitle(text = stringResource(R.string.plan_title)) {
                if (uiState.plannedRecipes.isNotEmpty()) {
                    LabelButton(text = stringResource(R.string.plan_clear), onClick = viewModel::clearPlan, color = RecipesColors.warningText)
                }
            }
        }
        if (uiState.isLoading) return@MountainScreen
        if (uiState.plannedRecipes.isEmpty()) {
            item {
                EmptyStateCard(
                    message = stringResource(R.string.plan_empty),
                    buttonText = stringResource(R.string.action_go_to_recipes),
                    onButtonClick = onGoToRecipes,
                    modifier = Modifier.padding(top = 120.dp),
                )
            }
            return@MountainScreen
        }
        item { Spacer(Modifier.height(96.dp)) }
        items(uiState.plannedRecipes, key = { it.recipeId }) { plannedRecipe ->
            PlannedRecipeCard(
                plannedRecipe = plannedRecipe,
                onClick = { onRecipeClick(plannedRecipe.recipeId) },
                onRemove = { viewModel.remove(plannedRecipe) },
                onDecrease = { viewModel.decreaseServings(plannedRecipe) },
                onIncrease = { viewModel.increaseServings(plannedRecipe) },
            )
        }
        item {
            LabelButton(
                text = stringResource(R.string.plan_add_more),
                onClick = onGoToRecipes,
                iconResource = R.drawable.ic_add,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            PrimaryButton(
                text = stringResource(R.string.plan_go_to_shopping_list),
                onClick = onGoToShoppingList,
                iconResource = R.drawable.ic_shopping,
            )
        }
    }
}

@Composable
private fun PlannedRecipeCard(
    plannedRecipe: PlannedRecipe,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
) {
    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CardTitle(text = plannedRecipe.title, modifier = Modifier.weight(1f).clickable(onClick = onClick))
            PillButton(text = stringResource(R.string.action_take_off), onClick = onRemove, style = PillStyle.Warning)
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            ServingsStepper(servings = plannedRecipe.plannedServings, onDecrease = onDecrease, onIncrease = onIncrease)
            if (plannedRecipe.plannedServings != plannedRecipe.recipeServings) {
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(
                        text = stringResource(R.string.plan_recipe_for, plannedRecipe.recipeServings),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}
