package com.example.features.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.security.TenantContext
import com.example.features.schools.data.FirebaseSchoolRepository
import com.example.features.schools.domain.SchoolRepository
import com.example.shared.models.TenantSchool
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isDarkMode: Boolean = false,
    val selectedLanguage: String = "English (US)",
    val notificationsEnabled: Boolean = true,
    val attendanceAlertsEnabled: Boolean = true,
    val feeRemindersEnabled: Boolean = true,
    val biometricUnlockEnabled: Boolean = false,
    val cachedRecordsCount: Int = 184,
    val appVersion: String = "1.0.0 (Phase 5 Build 105)",
    val showSchoolDetailsDialog: Boolean = false,
    val activeSchool: TenantSchool = TenantSchool(
        id = "sch_oxford_01",
        name = "Oxford International Academy",
        code = "OXF-101",
        address = "742 Evergreen Terrace, Educational District",
        phone = "+1 (555) 234-5678",
        email = "info@oxfordacademy.edu",
        website = "https://oxfordacademy.edu",
        establishedYear = 1995,
        schoolType = "K-12 Institution",
        academicSession = "2026-2027",
        currency = "USD",
        currencySymbol = "$",
        timezone = "America/New_York"
    )
)

class SettingsViewModel(
    private val repositoryProvider: ((Context) -> SchoolRepository)? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun loadSchoolDetails(context: Context) {
        val repo = repositoryProvider?.invoke(context) ?: FirebaseSchoolRepository(context)
        val activeSchoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"

        viewModelScope.launch {
            val result = repo.getSchoolById(activeSchoolId)
            if (result.isSuccess) {
                _uiState.update { it.copy(activeSchool = result.getOrThrow()) }
            }
        }
    }

    fun toggleDarkMode() {
        _uiState.update { it.copy(isDarkMode = !it.isDarkMode) }
    }

    fun toggleNotifications() {
        _uiState.update { it.copy(notificationsEnabled = !it.notificationsEnabled) }
    }

    fun toggleAttendanceAlerts() {
        _uiState.update { it.copy(attendanceAlertsEnabled = !it.attendanceAlertsEnabled) }
    }

    fun toggleFeeReminders() {
        _uiState.update { it.copy(feeRemindersEnabled = !it.feeRemindersEnabled) }
    }

    fun selectLanguage(language: String) {
        _uiState.update { it.copy(selectedLanguage = language) }
    }

    fun clearCache() {
        _uiState.update { it.copy(cachedRecordsCount = 0) }
    }

    fun openSchoolDetailsDialog() {
        _uiState.update { it.copy(showSchoolDetailsDialog = true) }
    }

    fun closeSchoolDetailsDialog() {
        _uiState.update { it.copy(showSchoolDetailsDialog = false) }
    }
}
