package com.example.recipes.ui.recipeeditor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipes.data.repository.RecipeRepository
import com.example.recipes.domain.IngredientTextParser
import com.example.recipes.domain.MeasurementUnit
import com.example.recipes.domain.QuantityFormatter
import com.example.recipes.domain.Recipe
import com.example.recipes.domain.RecipeStatus
import com.example.recipes.domain.TagNames
import com.example.recipes.ui.components.EditableIngredient
import com.example.recipes.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecipeEditorUiState(
    val isLoading: Boolean = true,
    val isNewRecipe: Boolean = true,
    val title: String = "",
    val sourceUrl: String = "",
    val servings: Int = 2,
    val tags: List<String> = emptyList(),
    val tagInput: String = "",
    val ingredients: List<EditableIngredient> = emptyList(),
    val instructions: String = "",
    val status: RecipeStatus = RecipeStatus.VERIFIED,
    val isPasteSheetOpen: Boolean = false,
    val pasteText: String = "",
    val isConfirmingDelete: Boolean = false,
) {
    val canSave: Boolean get() = !isLoading && title.isNotBlank()
}

sealed interface RecipeEditorEvent {
    data class Saved(val recipeId: String) : RecipeEditorEvent
    data object Deleted : RecipeEditorEvent
}

@HiltViewModel
class RecipeEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val recipeRepository: RecipeRepository,
) : ViewModel() {

    private val recipeId: String? = savedStateHandle[Routes.RECIPE_ID_ARGUMENT]

    /** Pre-filled when a web import found no recipe and the user chose to write it by hand. */
    private val prefilledSourceUrl: String = savedStateHandle.get<String>(Routes.SOURCE_URL_ARGUMENT).orEmpty()

    private val mutableUiState = MutableStateFlow(
        RecipeEditorUiState(isLoading = recipeId != null, isNewRecipe = recipeId == null, sourceUrl = prefilledSourceUrl),
    )
    val uiState: StateFlow<RecipeEditorUiState> = mutableUiState.asStateFlow()

    val existingTags: StateFlow<List<String>> =
        recipeRepository.observeTagNames().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val eventChannel = Channel<RecipeEditorEvent>(Channel.BUFFERED)
    val events: Flow<RecipeEditorEvent> = eventChannel.receiveAsFlow()

    init {
        if (recipeId != null) {
            viewModelScope.launch {
                val recipe = recipeRepository.getRecipe(recipeId)
                mutableUiState.value = if (recipe == null) {
                    RecipeEditorUiState(isLoading = false, isNewRecipe = true)
                } else {
                    recipe.toEditorState()
                }
            }
        }
    }

    fun onTitleChange(title: String) = mutableUiState.update { it.copy(title = title) }

    fun onSourceUrlChange(sourceUrl: String) = mutableUiState.update { it.copy(sourceUrl = sourceUrl) }

    fun increaseServings() = mutableUiState.update { it.copy(servings = it.servings + 1) }

    fun decreaseServings() = mutableUiState.update { it.copy(servings = (it.servings - 1).coerceAtLeast(1)) }

    fun onInstructionsChange(instructions: String) = mutableUiState.update { it.copy(instructions = instructions) }

    /** Typing a comma adds what comes before it as a tag. */
    fun onTagInputChange(input: String) {
        if (input.contains(',')) {
            val parts = input.split(',')
            parts.dropLast(1).forEach(::addTag)
            mutableUiState.update { it.copy(tagInput = parts.last().trimStart()) }
        } else {
            mutableUiState.update { it.copy(tagInput = input) }
        }
    }

    fun submitTagInput() {
        addTag(uiState.value.tagInput)
        mutableUiState.update { it.copy(tagInput = "") }
    }

    fun addTag(rawTag: String) {
        val tag = TagNames.normalize(rawTag)
        if (tag.isEmpty()) return
        mutableUiState.update { state ->
            if (tag in state.tags) state.copy(tagInput = "") else state.copy(tags = state.tags + tag, tagInput = "")
        }
    }

    fun removeTag(tag: String) = mutableUiState.update { it.copy(tags = it.tags - tag) }

    fun addEmptyIngredient() = mutableUiState.update { state ->
        state.copy(ingredients = state.ingredients + EditableIngredient.empty())
    }

    fun updateIngredient(updated: EditableIngredient) = mutableUiState.update { state ->
        state.copy(ingredients = state.ingredients.map { if (it.key == updated.key) updated else it })
    }

    fun removeIngredient(key: String) = mutableUiState.update { state ->
        state.copy(ingredients = state.ingredients.filterNot { it.key == key })
    }

    fun openPasteSheet() = mutableUiState.update { it.copy(isPasteSheetOpen = true) }

    fun closePasteSheet() = mutableUiState.update { it.copy(isPasteSheetOpen = false) }

    fun onPasteTextChange(text: String) = mutableUiState.update { it.copy(pasteText = text) }

    /** Parses the pasted lines (spec §5.3) and appends them to the ingredients. */
    fun addPastedIngredients() = mutableUiState.update { state ->
        val parsedIngredients = IngredientTextParser.parseLines(state.pasteText).map { parsed ->
            EditableIngredient(
                key = EditableIngredient.newKey(),
                quantityText = QuantityFormatter.formatForEditing(parsed.quantity),
                unit = parsed.unit,
                name = parsed.name,
                aisle = parsed.aisle,
            )
        }
        state.copy(ingredients = state.ingredients + parsedIngredients, pasteText = "", isPasteSheetOpen = false)
    }

    fun requestDelete() = mutableUiState.update { it.copy(isConfirmingDelete = true) }

    fun cancelDelete() = mutableUiState.update { it.copy(isConfirmingDelete = false) }

    fun confirmDelete() {
        val idToDelete = recipeId ?: return
        viewModelScope.launch {
            recipeRepository.deleteRecipe(idToDelete)
            eventChannel.send(RecipeEditorEvent.Deleted)
        }
    }

    fun save() {
        val state = uiState.value
        if (!state.canSave) return
        // A pending tag that was typed but not confirmed is still saved.
        val tags = (state.tags + TagNames.normalize(state.tagInput)).filter { it.isNotEmpty() }.distinct()
        viewModelScope.launch {
            val savedId = recipeRepository.saveRecipe(
                Recipe(
                    id = recipeId.orEmpty(),
                    title = state.title,
                    servings = state.servings,
                    sourceUrl = state.sourceUrl,
                    instructions = state.instructions,
                    status = state.status,
                    ingredients = state.ingredients.map { it.toIngredient() },
                    tags = tags,
                    plannedServings = null,
                ),
            )
            eventChannel.send(RecipeEditorEvent.Saved(savedId))
        }
    }

    private fun Recipe.toEditorState() = RecipeEditorUiState(
        isLoading = false,
        isNewRecipe = false,
        title = title,
        sourceUrl = sourceUrl.orEmpty(),
        servings = servings,
        tags = tags,
        ingredients = ingredients.map(EditableIngredient::from),
        instructions = instructions,
        status = status,
    )
}
