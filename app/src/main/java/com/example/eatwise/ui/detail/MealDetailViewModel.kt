package com.example.eatwise.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eatwise.data.repository.MealRepository
import com.example.eatwise.domain.model.MealRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailUiState(
    val record: MealRecord? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

class MealDetailViewModel(
    private val recordId: String,
    private val mealRepository: MealRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            mealRepository.observeById(recordId).collect { record ->
                _uiState.update {
                    it.copy(record = record, isLoading = false, errorMessage = if (record == null) "记录不存在或已删除。" else null)
                }
            }
        }
    }

    fun toggleFavorite() {
        val record = uiState.value.record ?: return
        viewModelScope.launch { mealRepository.setFavorite(record.id, !record.isFavorite) }
    }

    fun delete(onDeleted: () -> Unit) {
        viewModelScope.launch {
            mealRepository.deleteById(recordId)
            onDeleted()
        }
    }
}
