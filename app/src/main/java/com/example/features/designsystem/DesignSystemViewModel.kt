package com.example.features.designsystem

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DesignSystemViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DesignSystemUiState())
    val uiState: StateFlow<DesignSystemUiState> = _uiState.asStateFlow()

    fun toggleDarkMode() {
        _uiState.update { it.copy(isDarkMode = !it.isDarkMode) }
    }

    fun toggleShimmerLoading() {
        _uiState.update { it.copy(isShimmerLoading = !it.isShimmerLoading) }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onTextInputChanged(text: String) {
        val error = when {
            text.isBlank() -> "Name cannot be empty"
            text.length < 3 -> "Name must be at least 3 letters"
            else -> null
        }
        _uiState.update { it.copy(textInputValue = text, textInputError = error) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(passwordValue = password) }
    }

    fun onClassSelected(className: String) {
        _uiState.update { it.copy(selectedClass = className, lastActionLog = "Selected class: $className") }
    }

    fun setCategoryTab(index: Int) {
        _uiState.update { it.copy(selectedCategoryTab = index) }
    }

    fun openConfirmDialog() {
        _uiState.update { it.copy(showConfirmDialog = true) }
    }

    fun closeConfirmDialog() {
        _uiState.update { it.copy(showConfirmDialog = false) }
    }

    fun onConfirmAction() {
        _uiState.update { it.copy(showConfirmDialog = false, lastActionLog = "Confirmed destructive action") }
    }

    fun openPromptDialog() {
        _uiState.update { it.copy(showPromptDialog = true) }
    }

    fun closePromptDialog() {
        _uiState.update { it.copy(showPromptDialog = false) }
    }

    fun onPromptSubmit(value: String) {
        _uiState.update { it.copy(showPromptDialog = false, lastActionLog = "Created section: $value") }
    }

    fun openFilterBottomSheet() {
        _uiState.update { it.copy(showFilterBottomSheet = true) }
    }

    fun closeFilterBottomSheet() {
        _uiState.update { it.copy(showFilterBottomSheet = false) }
    }

    fun setStatusFilter(status: String) {
        _uiState.update { it.copy(selectedStatusFilter = status) }
    }

    fun applyFilters() {
        _uiState.update {
            it.copy(
                showFilterBottomSheet = false,
                lastActionLog = "Applied filter: ${it.selectedStatusFilter}"
            )
        }
    }

    fun resetFilters() {
        _uiState.update {
            it.copy(
                selectedStatusFilter = "All",
                showFilterBottomSheet = false,
                lastActionLog = "Filters reset"
            )
        }
    }
}
