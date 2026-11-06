package com.example.recipes.ui.mealplan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipes.data.repository.MealPlanRepository
import com.example.recipes.domain.PlannedRecipe
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MealPlanUiState(
    val isLoading: Boolean = true,
    val plannedRecipes: List<PlannedRecipe> = emptyList(),
)

@HiltViewModel
class MealPlanViewModel @Inject constructor(
    private val mealPlanRepository: MealPlanRepository,
) : ViewModel() {

    val uiState: StateFlow<MealPlanUiState> = mealPlanRepository.observePlannedRecipes()
        .map { MealPlanUiState(isLoading = false, plannedRecipes = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MealPlanUiState())

    fun increaseServings(plannedRecipe: PlannedRecipe) = updateServings(plannedRecipe, plannedRecipe.plannedServings + 1)

    fun decreaseServings(plannedRecipe: PlannedRecipe) = updateServings(plannedRecipe, plannedRecipe.plannedServings - 1)

    fun remove(plannedRecipe: PlannedRecipe) {
        viewModelScope.launch { mealPlanRepository.removeFromPlan(plannedRecipe.recipeId) }
    }

    fun clearPlan() {
        viewModelScope.launch { mealPlanRepository.clearPlan() }
    }

    private fun updateServings(plannedRecipe: PlannedRecipe, servings: Int) {
        if (servings < 1) return
        viewModelScope.launch { mealPlanRepository.updatePlannedServings(plannedRecipe.recipeId, servings) }
    }
}
