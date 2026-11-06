package com.example.recipes.ui.shoppinglist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipes.data.repository.ShoppingListRepository
import com.example.recipes.domain.ShoppingList
import com.example.recipes.domain.ShoppingListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ShoppingListUiState(
    val isLoading: Boolean = true,
    val shoppingList: ShoppingList = ShoppingList.EMPTY,
    val stapleNames: List<String> = emptyList(),
)

@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    private val shoppingListRepository: ShoppingListRepository,
) : ViewModel() {

    val uiState: StateFlow<ShoppingListUiState> = combine(
        shoppingListRepository.observeShoppingList(),
        shoppingListRepository.observeStapleNames(),
    ) { shoppingList, stapleNames ->
        ShoppingListUiState(isLoading = false, shoppingList = shoppingList, stapleNames = stapleNames)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ShoppingListUiState())

    fun toggleChecked(item: ShoppingListItem) {
        viewModelScope.launch { shoppingListRepository.setChecked(item, !item.isChecked) }
    }

    fun clearChecks() {
        viewModelScope.launch { shoppingListRepository.clearChecks() }
    }

    fun addStaple(name: String) {
        viewModelScope.launch { shoppingListRepository.addStaple(name) }
    }

    fun removeStaple(name: String) {
        viewModelScope.launch { shoppingListRepository.removeStaple(name) }
    }
}
