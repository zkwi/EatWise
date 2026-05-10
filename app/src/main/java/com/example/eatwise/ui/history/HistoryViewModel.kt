package com.example.eatwise.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eatwise.data.repository.MealRepository
import com.example.eatwise.domain.model.MealRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistoryUiState(
    val records: List<MealRecord> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val favoriteFirst: Boolean = false,
)

class HistoryViewModel(
    private val mealRepository: MealRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            mealRepository.observeAll().collect { records ->
                _uiState.update { state ->
                    state.copy(records = sort(records, state.favoriteFirst), isLoading = false)
                }
            }
        }
    }

    fun toggleFavoriteFirst() {
        _uiState.update { it.copy(favoriteFirst = !it.favoriteFirst, records = sort(it.records, !it.favoriteFirst)) }
    }

    fun toggleFavorite(record: MealRecord) {
        viewModelScope.launch { mealRepository.setFavorite(record.id, !record.isFavorite) }
    }

    fun delete(record: MealRecord) {
        viewModelScope.launch { mealRepository.deleteById(record.id) }
    }

    private fun sort(records: List<MealRecord>, favoriteFirst: Boolean): List<MealRecord> =
        if (favoriteFirst) records.sortedWith(compareByDescending<MealRecord> { it.isFavorite }.thenByDescending { it.createdAt }) else records
}
