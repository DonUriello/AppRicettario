package com.example.recipes.ui.recipelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipes.data.importing.SharedText
import com.example.recipes.data.importing.WebImportResult
import com.example.recipes.data.importing.WebRecipeImporter
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

enum class LinkImportProblem { INVALID_URL, DOWNLOAD_FAILED, NO_RECIPE_FOUND }

data class LinkImportUiState(
    val url: String = "",
    val isImporting: Boolean = false,
    val problem: LinkImportProblem? = null,
) {
    val canImport: Boolean get() = url.isNotBlank() && !isImporting
}

sealed interface LinkImportEvent {
    data class DraftCreated(val recipeId: String) : LinkImportEvent
    data class CreateManually(val sourceUrl: String) : LinkImportEvent
}

@HiltViewModel
class LinkImportViewModel @Inject constructor(
    private val webRecipeImporter: WebRecipeImporter,
) : ViewModel() {

    private val mutableUiState = MutableStateFlow(LinkImportUiState())
    val uiState: StateFlow<LinkImportUiState> = mutableUiState.asStateFlow()

    private val eventChannel = Channel<LinkImportEvent>(Channel.BUFFERED)
    val events: Flow<LinkImportEvent> = eventChannel.receiveAsFlow()

    fun onUrlChange(url: String) = mutableUiState.update { it.copy(url = url, problem = null) }

    /** Text shared from another app: keep only its link and start the import right away. */
    fun importSharedText(sharedText: String) {
        if (uiState.value.isImporting) return
        mutableUiState.value = LinkImportUiState(url = SharedText.linkOrText(sharedText))
        import()
    }

    fun import() {
        val url = uiState.value.url
        if (!uiState.value.canImport) return
        mutableUiState.update { it.copy(isImporting = true, problem = null) }
        viewModelScope.launch {
            val problem = when (val result = webRecipeImporter.import(url)) {
                is WebImportResult.Imported -> {
                    eventChannel.send(LinkImportEvent.DraftCreated(result.recipeId))
                    mutableUiState.value = LinkImportUiState()
                    return@launch
                }
                WebImportResult.InvalidUrl -> LinkImportProblem.INVALID_URL
                WebImportResult.DownloadFailed -> LinkImportProblem.DOWNLOAD_FAILED
                WebImportResult.NoRecipeFound -> LinkImportProblem.NO_RECIPE_FOUND
            }
            mutableUiState.update { it.copy(isImporting = false, problem = problem) }
        }
    }

    /** After "no structured recipe found": open the editor with the link pre-filled. */
    fun createManually() {
        val sourceUrl = WebRecipeImporter.normalizeUrl(uiState.value.url) ?: uiState.value.url.trim()
        viewModelScope.launch {
            eventChannel.send(LinkImportEvent.CreateManually(sourceUrl))
            mutableUiState.value = LinkImportUiState()
        }
    }
}
