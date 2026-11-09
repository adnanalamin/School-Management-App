package com.example.features.foundation

import androidx.lifecycle.ViewModel
import com.example.core.security.TenantContext
import com.example.core.security.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FoundationViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FoundationUiState())
    val uiState: StateFlow<FoundationUiState> = _uiState.asStateFlow()

    init {
        // Initialize tenant context with default school
        val initialSchool = _uiState.value.availableSchools.first()
        TenantContext.setTenant(
            schoolId = initialSchool.id,
            role = _uiState.value.selectedRole,
            userId = "usr_admin_001"
        )
    }

    fun selectSchool(schoolId: String) {
        _uiState.update { it.copy(selectedSchoolId = schoolId) }
        TenantContext.setTenant(
            schoolId = schoolId,
            role = _uiState.value.selectedRole,
            userId = "usr_admin_001"
        )
    }

    fun selectRole(role: UserRole) {
        _uiState.update { it.copy(selectedRole = role) }
        TenantContext.setTenant(
            schoolId = _uiState.value.selectedSchoolId,
            role = role,
            userId = "usr_${role.name.lowercase()}_001"
        )
    }

    fun setTab(index: Int) {
        _uiState.update { it.copy(activeTab = index) }
    }

    fun toggleDarkMode() {
        _uiState.update { it.copy(isDarkMode = !it.isDarkMode) }
    }

    fun onTestInputChanged(value: String) {
        val error = when {
            value.isBlank() -> null
            value.length < 3 -> "Must be at least 3 characters"
            else -> null
        }
        _uiState.update { it.copy(testInputText = value, testInputError = error) }
    }
}
