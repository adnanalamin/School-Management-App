package com.example.features.schools.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.error.ErrorHandler
import com.example.core.security.TenantContext
import com.example.core.security.UserRole
import com.example.core.utils.ValidationUtils
import com.example.features.schools.data.FirebaseSchoolRepository
import com.example.features.schools.domain.SchoolRepository
import com.example.shared.models.SchoolStatus
import com.example.shared.models.TenantSchool
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SchoolRegistrationUiState(
    val currentStep: Int = 0,
    // Step 0: Basic Profile & Brand
    val schoolName: String = "",
    val schoolCode: String = "",
    val slogan: String = "",
    val logoUrl: String = "",
    val establishedYear: String = "2015",
    val schoolType: String = "K-12 Institution",
    // Step 1: Contact & Physical Location
    val address: String = "",
    val phone: String = "",
    val email: String = "",
    val website: String = "",
    // Step 2: Regional & Academic Operations
    val academicSession: String = "2026-2027",
    val currency: String = "USD ($)",
    val currencyCode: String = "USD",
    val currencySymbol: String = "$",
    val timezone: String = "America/New_York (UTC-5)",
    // Step 3: Primary Admin Info
    val adminName: String = "",
    val adminEmail: String = "",
    val adminPassword: String = "",
    // Status & Previews
    val previewSchoolId: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isRegistrationCompleted: Boolean = false,
    val createdSchool: TenantSchool? = null
)

class SchoolRegistrationViewModel(
    private val repositoryProvider: ((Context) -> SchoolRepository)? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(SchoolRegistrationUiState())
    val uiState: StateFlow<SchoolRegistrationUiState> = _uiState.asStateFlow()

    val availableSchoolTypes = listOf(
        "K-12 Institution",
        "Primary & Middle School",
        "Secondary High School",
        "Collegiate Academy",
        "International Baccalaureate (IB)",
        "Vocational & Technical Institute"
    )

    val availableAcademicSessions = listOf(
        "2026-2027",
        "2025-2026",
        "2026 (Annual Jan-Dec)",
        "2027 (Annual Jan-Dec)"
    )

    val availableCurrencies = listOf(
        "USD ($)" to ("USD" to "$"),
        "EUR (€)" to ("EUR" to "€"),
        "GBP (£)" to ("GBP" to "£"),
        "CAD ($)" to ("CAD" to "$"),
        "AUD ($)" to ("AUD" to "$"),
        "INR (₹)" to ("INR" to "₹"),
        "AED (د.إ)" to ("AED" to "د.إ"),
        "SAR (﷼)" to ("SAR" to "﷼"),
        "JPY (¥)" to ("JPY" to "¥")
    )

    val availableTimezones = listOf(
        "America/New_York (UTC-5)",
        "America/Chicago (UTC-6)",
        "America/Los_Angeles (UTC-8)",
        "Europe/London (UTC+0)",
        "Europe/Paris (UTC+1)",
        "Asia/Dubai (UTC+4)",
        "Asia/Kolkata (UTC+5:30)",
        "Asia/Dhaka (UTC+6)",
        "Asia/Singapore (UTC+8)",
        "Asia/Tokyo (UTC+9)"
    )

    fun onSchoolNameChanged(name: String) {
        _uiState.update {
            it.copy(
                schoolName = name,
                errorMessage = null,
                previewSchoolId = generatePreviewId(it.schoolCode.ifBlank { name })
            )
        }
    }

    fun onSchoolCodeChanged(code: String) {
        val sanitized = code.uppercase().replace(Regex("[^A-Z0-9-]"), "").take(10)
        _uiState.update {
            it.copy(
                schoolCode = sanitized,
                errorMessage = null,
                previewSchoolId = generatePreviewId(sanitized)
            )
        }
    }

    fun onSloganChanged(slogan: String) {
        _uiState.update { it.copy(slogan = slogan) }
    }

    fun onLogoUrlChanged(url: String) {
        _uiState.update { it.copy(logoUrl = url) }
    }

    fun onEstablishedYearChanged(year: String) {
        val digitsOnly = year.filter { it.isDigit() }.take(4)
        _uiState.update { it.copy(establishedYear = digitsOnly, errorMessage = null) }
    }

    fun onSchoolTypeSelected(type: String) {
        _uiState.update { it.copy(schoolType = type) }
    }

    fun onAddressChanged(address: String) {
        _uiState.update { it.copy(address = address, errorMessage = null) }
    }

    fun onPhoneChanged(phone: String) {
        _uiState.update { it.copy(phone = phone, errorMessage = null) }
    }

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onWebsiteChanged(website: String) {
        _uiState.update { it.copy(website = website, errorMessage = null) }
    }

    fun onAcademicSessionSelected(session: String) {
        _uiState.update { it.copy(academicSession = session) }
    }

    fun onCurrencySelected(displayCurrency: String) {
        val match = availableCurrencies.firstOrNull { it.first == displayCurrency }
        val code = match?.second?.first ?: "USD"
        val symbol = match?.second?.second ?: "$"
        _uiState.update {
            it.copy(
                currency = displayCurrency,
                currencyCode = code,
                currencySymbol = symbol
            )
        }
    }

    fun onTimezoneSelected(tz: String) {
        _uiState.update { it.copy(timezone = tz) }
    }

    fun onAdminNameChanged(name: String) {
        _uiState.update { it.copy(adminName = name, errorMessage = null) }
    }

    fun onAdminEmailChanged(email: String) {
        _uiState.update { it.copy(adminEmail = email, errorMessage = null) }
    }

    fun onAdminPasswordChanged(password: String) {
        _uiState.update { it.copy(adminPassword = password, errorMessage = null) }
    }

    fun nextStep(): Boolean {
        val state = _uiState.value
        when (state.currentStep) {
            0 -> {
                if (state.schoolName.isBlank()) {
                    _uiState.update { it.copy(errorMessage = "School Name is required.") }
                    return false
                }
                if (!ValidationUtils.isValidSchoolCode(state.schoolCode)) {
                    _uiState.update { it.copy(errorMessage = "School Code must be 3-10 alphanumeric characters (e.g. CIS101).") }
                    return false
                }
                val yearInt = state.establishedYear.toIntOrNull()
                if (yearInt == null || yearInt < 1800 || yearInt > 2026) {
                    _uiState.update { it.copy(errorMessage = "Established Year must be between 1800 and 2026.") }
                    return false
                }
            }
            1 -> {
                if (state.address.isBlank()) {
                    _uiState.update { it.copy(errorMessage = "Campus Address is required.") }
                    return false
                }
                if (state.phone.isBlank()) {
                    _uiState.update { it.copy(errorMessage = "Official Contact Phone is required.") }
                    return false
                }
                if (!ValidationUtils.isValidEmail(state.email)) {
                    _uiState.update { it.copy(errorMessage = "Please enter a valid official institutional email.") }
                    return false
                }
            }
            2 -> {
                // Regional settings are pre-populated with valid defaults
            }
            3 -> {
                if (state.adminName.isBlank()) {
                    _uiState.update { it.copy(errorMessage = "Administrator full name is required.") }
                    return false
                }
                if (!ValidationUtils.isValidEmail(state.adminEmail)) {
                    _uiState.update { it.copy(errorMessage = "Please enter a valid administrator work email.") }
                    return false
                }
                if (!ValidationUtils.isValidPassword(state.adminPassword)) {
                    _uiState.update { it.copy(errorMessage = "Password must be at least 8 characters.") }
                    return false
                }
            }
        }

        if (state.currentStep < 3) {
            _uiState.update { it.copy(currentStep = it.currentStep + 1, errorMessage = null) }
            return true
        }
        return false
    }

    fun previousStep(): Boolean {
        if (_uiState.value.currentStep > 0) {
            _uiState.update { it.copy(currentStep = it.currentStep - 1, errorMessage = null) }
            return true
        }
        return false
    }

    fun submitRegistration(context: Context, onSuccess: () -> Unit) {
        val state = _uiState.value
        val repo = repositoryProvider?.invoke(context) ?: FirebaseSchoolRepository(context)

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val generatedId = repo.generateUniqueSchoolId(state.schoolCode)
            val currentAuthUid = try { Firebase.auth.currentUser?.uid } catch (_: Exception) { null }
            val adminUid = currentAuthUid ?: "usr_admin_${System.currentTimeMillis() % 1000}"

            val school = TenantSchool(
                id = generatedId,
                name = state.schoolName.trim(),
                code = state.schoolCode.trim().uppercase(),
                slogan = state.slogan.trim().ifBlank { null },
                logoUrl = state.logoUrl.trim().ifBlank { null },
                address = state.address.trim(),
                phone = state.phone.trim(),
                email = state.email.trim(),
                website = state.website.trim(),
                establishedYear = state.establishedYear.toIntOrNull() ?: 2015,
                schoolType = state.schoolType,
                academicSession = state.academicSession,
                currency = state.currencyCode,
                currencySymbol = state.currencySymbol,
                timezone = state.timezone,
                status = SchoolStatus.ACTIVE,
                subscriptionPlan = "PRO",
                adminUid = adminUid
            )

            val result = repo.registerSchool(school, adminUid)
            if (result.isSuccess) {
                val savedSchool = result.getOrThrow()
                // Lock tenant context
                TenantContext.setTenant(
                    schoolId = savedSchool.id,
                    role = UserRole.SCHOOL_ADMIN,
                    userId = adminUid
                )
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRegistrationCompleted = true,
                        createdSchool = savedSchool
                    )
                }
                onSuccess()
            } else {
                val error = result.exceptionOrNull()
                val message = error?.message ?: "Failed to register school. Please verify your connection."
                _uiState.update { it.copy(isLoading = false, errorMessage = message) }
            }
        }
    }

    private fun generatePreviewId(code: String): String {
        val clean = code.trim().lowercase().replace(Regex("[^a-z0-9]"), "").take(8)
        val prefix = if (clean.isEmpty()) "sch" else "sch_$clean"
        return "${prefix}_${System.currentTimeMillis() % 100000}"
    }
}
