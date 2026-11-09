package com.example.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.security.TenantContext
import com.example.core.security.UserRole
import com.example.core.utils.ValidationUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterUiState(
    val schoolName: String = "",
    val schoolCode: String = "",
    val adminName: String = "",
    val email: String = "",
    val phone: String = "",
    val schoolType: String = "K-12 Institution",
    val availableSchoolTypes: List<String> = listOf(
        "K-12 Institution",
        "Primary & Middle School",
        "Secondary High School",
        "Collegiate Academy",
        "Vocational & Technical"
    ),
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class RegisterViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onSchoolNameChanged(name: String) {
        _uiState.update { it.copy(schoolName = name, errorMessage = null) }
    }

    fun onSchoolCodeChanged(code: String) {
        _uiState.update { it.copy(schoolCode = code.uppercase(), errorMessage = null) }
    }

    fun onAdminNameChanged(name: String) {
        _uiState.update { it.copy(adminName = name, errorMessage = null) }
    }

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onPhoneChanged(phone: String) {
        _uiState.update { it.copy(phone = phone, errorMessage = null) }
    }

    fun onSchoolTypeSelected(type: String) {
        _uiState.update { it.copy(schoolType = type) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun registerSchool(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.schoolName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "School Name is required.") }
            return
        }
        if (!ValidationUtils.isValidSchoolCode(state.schoolCode)) {
            _uiState.update { it.copy(errorMessage = "School Code must be 3-10 alphanumeric characters (e.g. BHS101).") }
            return
        }
        if (state.adminName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Administrator Name is required.") }
            return
        }
        if (!ValidationUtils.isValidEmail(state.email)) {
            _uiState.update { it.copy(errorMessage = "Please provide a valid work email.") }
            return
        }
        if (!ValidationUtils.isValidPassword(state.password)) {
            _uiState.update { it.copy(errorMessage = "Password must be at least 8 characters.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            delay(1200) // Simulate school registration in Firestore

            val newSchoolId = "sch_${state.schoolCode.lowercase()}_${System.currentTimeMillis() % 1000}"
            TenantContext.setTenant(
                schoolId = newSchoolId,
                role = UserRole.SCHOOL_ADMIN,
                userId = "usr_admin_${System.currentTimeMillis() % 1000}"
            )

            _uiState.update { it.copy(isLoading = false) }
            onSuccess()
        }
    }
}
