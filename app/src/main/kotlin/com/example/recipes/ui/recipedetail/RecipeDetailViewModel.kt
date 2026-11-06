package com.example.recipes.ui.recipedetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipes.data.repository.MealPlanRepository
import com.example.recipes.data.repository.RecipeRepository
import com.example.recipes.domain.Recipe
import com.example.recipes.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RecipeDetailUiState(
    val isLoading: Boolean = true,
    val recipe: Recipe? = null,
    /** Servings used to scale the displayed quantities; local to this screen. */
    val displayedServings: Int = 1,
)

@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    recipeRepository: RecipeRepository,
    private val mealPlanRepository: MealPlanRepository,
) : ViewModel() {

    private val recipeId: String = checkNotNull(savedStateHandle[Routes.RECIPE_ID_ARGUMENT])

    /** Null until the user touches the stepper: the screen then follows the recipe (or plan) servings. */
    private val chosenServings = MutableStateFlow<Int?>(null)

    val uiState: StateFlow<RecipeDetailUiState> = combine(
        recipeRepository.observeRecipe(recipeId),
        chosenServings,
    ) { recipe, chosen ->
        RecipeDetailUiState(
            isLoading = false,
            recipe = recipe,
            displayedServings = chosen ?: recipe?.plannedServings ?: recipe?.servings ?: 1,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecipeDetailUiState())

    fun increaseServings() {
        chosenServings.value = uiState.value.displayedServings + 1
    }

    fun decreaseServings() {
        chosenServings.value = (uiState.value.displayedServings - 1).coerceAtLeast(1)
    }

    /** Adds the recipe to the plan, or updates its planned servings, at the displayed servings. */
    fun addToPlan() {
        val servings = uiState.value.displayedServings
        viewModelScope.launch { mealPlanRepository.addToPlan(recipeId, servings) }
    }
}
