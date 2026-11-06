package com.example.recipes.ui.recipelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipes.data.repository.MealPlanRepository
import com.example.recipes.data.repository.RecipeRepository
import com.example.recipes.domain.Recipe
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RecipeListUiState(
    val isLoading: Boolean = true,
    val hasAnyRecipe: Boolean = false,
    val recipes: List<Recipe> = emptyList(),
    val tags: List<String> = emptyList(),
    val selectedTag: String? = null,
    val searchQuery: String = "",
)

@HiltViewModel
class RecipeListViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val mealPlanRepository: MealPlanRepository,
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val selectedTag = MutableStateFlow<String?>(null)

    val uiState: StateFlow<RecipeListUiState> = combine(
        recipeRepository.observeRecipes(),
        recipeRepository.observeTagNames(),
        searchQuery,
        selectedTag,
    ) { recipes, tags, query, tag ->
        val activeTag = tag?.takeIf { it in tags }
        RecipeListUiState(
            isLoading = false,
            hasAnyRecipe = recipes.isNotEmpty(),
            recipes = recipes.filter { recipe -> matchesTag(recipe, activeTag) && matchesQuery(recipe, query) },
            tags = tags,
            selectedTag = activeTag,
            searchQuery = query,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecipeListUiState())

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
    }

    /** Tapping the selected tag clears the filter; null selects "Tutte". */
    fun onTagClick(tag: String?) {
        selectedTag.value = if (tag == null || tag == selectedTag.value) null else tag
    }

    fun addToPlan(recipeId: String) {
        viewModelScope.launch { mealPlanRepository.addToPlan(recipeId) }
    }

    fun renameTag(oldName: String, newName: String) {
        viewModelScope.launch {
            recipeRepository.renameTag(oldName, newName)
            selectedTag.value = newName.trim().lowercase()
        }
    }

    private fun matchesTag(recipe: Recipe, tag: String?) = tag == null || tag in recipe.tags

    private fun matchesQuery(recipe: Recipe, query: String): Boolean {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isEmpty()) return true
        return recipe.title.contains(normalizedQuery, ignoreCase = true) ||
            recipe.tags.any { it.contains(normalizedQuery, ignoreCase = true) } ||
            recipe.ingredients.any { it.name.contains(normalizedQuery, ignoreCase = true) }
    }
}
