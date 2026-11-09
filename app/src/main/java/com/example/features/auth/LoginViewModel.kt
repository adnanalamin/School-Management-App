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

data class LoginUiState(
    val schoolCode: String = "OXF-101",
    val email: String = "admin@oxfordacademy.edu",
    val password: String = "Admin@2026",
    val selectedRole: UserRole = UserRole.SCHOOL_ADMIN,
    val rememberMe: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoginSuccessful: Boolean = false
)

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onSchoolCodeChanged(code: String) {
        _uiState.update { it.copy(schoolCode = code.uppercase(), errorMessage = null) }
    }

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun toggleRememberMe() {
        _uiState.update { it.copy(rememberMe = !it.rememberMe) }
    }

    fun selectDemoRole(role: UserRole) {
        val (demoEmail, demoPass) = when (role) {
            UserRole.SUPER_ADMIN -> "superadmin@edumanage.com" to "Super@2026"
            UserRole.SCHOOL_ADMIN -> "admin@oxfordacademy.edu" to "Admin@2026"
            UserRole.TEACHER -> "teacher.sarah@oxfordacademy.edu" to "Teacher@2026"
            UserRole.ACCOUNTANT -> "accounts@oxfordacademy.edu" to "Accounts@2026"
            UserRole.LIBRARIAN -> "library@oxfordacademy.edu" to "Library@2026"
            UserRole.TRANSPORT_MANAGER -> "transport@oxfordacademy.edu" to "Transport@2026"
            UserRole.STAFF -> "staff.john@oxfordacademy.edu" to "Staff@2026"
            UserRole.PARENT -> "parent.david@gmail.com" to "Parent@2026"
            UserRole.STUDENT -> "student.amina@oxfordacademy.edu" to "Student@2026"
        }
        _uiState.update {
            it.copy(
                selectedRole = role,
                email = demoEmail,
                password = demoPass,
                errorMessage = null
            )
        }
    }

    fun signIn(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.schoolCode.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter your School Code (e.g. OXF-101)") }
            return
        }
        if (!ValidationUtils.isValidEmail(state.email)) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid email address.") }
            return
        }
        if (state.password.length < 6) {
            _uiState.update { it.copy(errorMessage = "Password must be at least 6 characters.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            delay(1000) // Simulate authentication & tenant verification

            // Lock tenant security boundary
            TenantContext.setTenant(
                schoolId = "sch_oxford_01",
                role = state.selectedRole,
                userId = "usr_${state.selectedRole.name.lowercase()}_001"
            )

            _uiState.update { it.copy(isLoading = false, isLoginSuccessful = true) }
            onSuccess()
        }
    }
}
