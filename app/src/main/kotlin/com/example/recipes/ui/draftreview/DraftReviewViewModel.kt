package com.example.recipes.ui.draftreview

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipes.data.repository.RecipeRepository
import com.example.recipes.domain.Recipe
import com.example.recipes.domain.RecipeStatus
import com.example.recipes.ui.components.EditableIngredient
import com.example.recipes.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReviewIngredient(
    val ingredient: EditableIngredient,
    /** The user accepted this uncertain row as it is (or after editing it). */
    val isConfirmed: Boolean = false,
    val isEditing: Boolean = false,
) {
    val isUncertain: Boolean get() = ingredient.reviewNote != null
    val needsAttention: Boolean get() = isUncertain && !isConfirmed
}

data class DraftReviewUiState(
    val isLoading: Boolean = true,
    val recipe: Recipe? = null,
    val title: String = "",
    val ingredients: List<ReviewIngredient> = emptyList(),
) {
    val rowsToCheckCount: Int get() = ingredients.count { it.needsAttention }
    val canSaveAsVerified: Boolean get() = recipe != null && rowsToCheckCount == 0 && title.isNotBlank()
}

sealed interface DraftReviewEvent {
    data class SavedAsDraft(val recipeId: String) : DraftReviewEvent
    data class SavedAsVerified(val recipeId: String) : DraftReviewEvent
    data class OpenEditor(val recipeId: String) : DraftReviewEvent
}

@HiltViewModel
class DraftReviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val recipeRepository: RecipeRepository,
) : ViewModel() {

    private val recipeId: String = checkNotNull(savedStateHandle[Routes.RECIPE_ID_ARGUMENT])

    private val mutableUiState = MutableStateFlow(DraftReviewUiState())
    val uiState: StateFlow<DraftReviewUiState> = mutableUiState.asStateFlow()

    private val eventChannel = Channel<DraftReviewEvent>(Channel.BUFFERED)
    val events: Flow<DraftReviewEvent> = eventChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            val recipe = recipeRepository.getRecipe(recipeId)
            mutableUiState.value = DraftReviewUiState(
                isLoading = false,
                recipe = recipe,
                title = recipe?.title.orEmpty(),
                ingredients = recipe?.ingredients.orEmpty().map { ReviewIngredient(EditableIngredient.from(it)) },
            )
        }
    }

    fun onTitleChange(title: String) = mutableUiState.update { it.copy(title = title) }

    fun confirm(key: String) = updateRow(key) { it.copy(isConfirmed = true, isEditing = false) }

    fun startEditing(key: String) = updateRow(key) { it.copy(isEditing = true) }

    /** Editing an uncertain row counts as checking it once the user taps "Conferma". */
    fun onIngredientChange(updated: EditableIngredient) = updateRow(updated.key) { it.copy(ingredient = updated) }

    fun remove(key: String) = mutableUiState.update { state ->
        state.copy(ingredients = state.ingredients.filterNot { it.ingredient.key == key })
    }

    /** Keeps the recipe as a draft; rows already confirmed lose their review note. */
    fun saveDraft() {
        viewModelScope.launch {
            val savedId = saveKeepingDraft()
            if (savedId != null) eventChannel.send(DraftReviewEvent.SavedAsDraft(savedId))
        }
    }

    /** Saves what was checked so far, then hands over to the full editor. */
    fun editRest() {
        viewModelScope.launch {
            val savedId = saveKeepingDraft()
            if (savedId != null) eventChannel.send(DraftReviewEvent.OpenEditor(savedId))
        }
    }

    private suspend fun saveKeepingDraft(): String? =
        save(RecipeStatus.NEEDS_REVIEW) { row -> if (row.isConfirmed) null else row.ingredient.reviewNote }

    /** Enabled only when every uncertain row is resolved; clears all review notes. */
    fun saveAsVerified() {
        if (!uiState.value.canSaveAsVerified) return
        viewModelScope.launch {
            val savedId = save(RecipeStatus.VERIFIED) { null }
            if (savedId != null) eventChannel.send(DraftReviewEvent.SavedAsVerified(savedId))
        }
    }

    private suspend fun save(status: RecipeStatus, reviewNote: (ReviewIngredient) -> String?): String? {
        val state = uiState.value
        val recipe = state.recipe ?: return null
        return recipeRepository.saveRecipe(
            recipe.copy(
                title = state.title.ifBlank { recipe.title },
                status = status,
                ingredients = state.ingredients.map { row -> row.ingredient.toIngredient(reviewNote = reviewNote(row)) },
            ),
        )
    }

    private fun updateRow(key: String, transform: (ReviewIngredient) -> ReviewIngredient) =
        mutableUiState.update { state ->
            state.copy(ingredients = state.ingredients.map { if (it.ingredient.key == key) transform(it) else it })
        }
}
