package com.example.recipes.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipes.data.repository.MealPlanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/** App-wide state shown in the bottom bar. */
@HiltViewModel
class AppViewModel @Inject constructor(mealPlanRepository: MealPlanRepository) : ViewModel() {
    val plannedCount: StateFlow<Int> =
        mealPlanRepository.observePlannedCount().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
}
