package com.example.features.settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SettingsUiState(
    val isDarkMode: Boolean = false,
    val selectedLanguage: String = "English (US)",
    val notificationsEnabled: Boolean = true,
    val attendanceAlertsEnabled: Boolean = true,
    val feeRemindersEnabled: Boolean = true,
    val biometricUnlockEnabled: Boolean = false,
    val cachedRecordsCount: Int = 184,
    val appVersion: String = "1.0.0 (Phase 3 Build 103)"
)

class SettingsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

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
}
