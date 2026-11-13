package com.example.features.branding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.security.Permission
import com.example.core.security.TenantContext
import com.example.core.security.UserRole
import com.example.core.theme.BrandThemeController
import com.example.features.branding.data.BrandingRepository
import com.example.features.branding.domain.BrandPalettePreset
import com.example.features.branding.domain.CrestEmblemStyle
import com.example.features.branding.domain.DashboardLayoutDensity
import com.example.features.branding.domain.ReportCardHeaderStyle
import com.example.features.branding.domain.SchoolBrandingConfig
import com.example.features.branding.domain.UiCornerStyle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class BrandingTab(val label: String) {
    IDENTITY("Identity & Locale"),
    THEME_PALETTE("Theme & Crest"),
    DOCUMENTS_LAYOUT("Receipts & Layout")
}

data class BrandingUiState(
    val activeConfig: SchoolBrandingConfig? = null,
    val draftConfig: SchoolBrandingConfig? = null,
    val selectedTab: BrandingTab = BrandingTab.IDENTITY,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val showLiveCardPreviewDialog: Boolean = false,
    val showResetConfirmDialog: Boolean = false,
    val statusMessage: String? = null,
    val errorMessage: String? = null
) {
    val hasUnsavedChanges: Boolean
        get() = activeConfig != null && draftConfig != null && activeConfig != draftConfig
}

class BrandingViewModel(
    private val brandingRepository: BrandingRepository = BrandingRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(BrandingUiState())
    val uiState: StateFlow<BrandingUiState> = _uiState.asStateFlow()

    init {
        loadBranding()
    }

    fun loadBranding() {
        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        val initialDefault = brandingRepository.getDefaultBrandingForSchool(schoolId)

        _uiState.update {
            it.copy(
                activeConfig = initialDefault,
                draftConfig = initialDefault,
                isLoading = true
            )
        }

        viewModelScope.launch {
            try {
                val remoteConfig = brandingRepository.getBrandingConfig(schoolId)
                _uiState.update {
                    it.copy(
                        activeConfig = remoteConfig,
                        draftConfig = remoteConfig,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to load school branding configuration."
                    )
                }
            }
        }
    }

    fun selectTab(tab: BrandingTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun updateIdentityFields(
        schoolDisplayName: String? = null,
        schoolCode: String? = null,
        motto: String? = null,
        supportEmail: String? = null,
        websiteUrl: String? = null,
        academicSession: String? = null,
        currencyCode: String? = null,
        currencySymbol: String? = null,
        timezone: String? = null
    ) {
        _uiState.update { state ->
            val current = state.draftConfig ?: return@update state
            state.copy(
                draftConfig = current.copy(
                    schoolDisplayName = schoolDisplayName ?: current.schoolDisplayName,
                    schoolCode = schoolCode ?: current.schoolCode,
                    motto = motto ?: current.motto,
                    supportEmail = supportEmail ?: current.supportEmail,
                    websiteUrl = websiteUrl ?: current.websiteUrl,
                    academicSession = academicSession ?: current.academicSession,
                    currencyCode = currencyCode ?: current.currencyCode,
                    currencySymbol = currencySymbol ?: current.currencySymbol,
                    timezone = timezone ?: current.timezone
                ),
                errorMessage = null
            )
        }
    }

    fun selectPalettePreset(preset: BrandPalettePreset) {
        _uiState.update { state ->
            val current = state.draftConfig ?: return@update state
            state.copy(draftConfig = current.copy(palettePreset = preset))
        }
    }

    fun selectCrestEmblem(emblem: CrestEmblemStyle) {
        _uiState.update { state ->
            val current = state.draftConfig ?: return@update state
            state.copy(draftConfig = current.copy(crestEmblem = emblem))
        }
    }

    fun selectCornerStyle(cornerStyle: UiCornerStyle) {
        _uiState.update { state ->
            val current = state.draftConfig ?: return@update state
            state.copy(draftConfig = current.copy(cornerStyle = cornerStyle))
        }
    }

    fun selectLayoutDensity(density: DashboardLayoutDensity) {
        _uiState.update { state ->
            val current = state.draftConfig ?: return@update state
            state.copy(draftConfig = current.copy(layoutDensity = density))
        }
    }

    fun selectReportHeaderStyle(headerStyle: ReportCardHeaderStyle) {
        _uiState.update { state ->
            val current = state.draftConfig ?: return@update state
            state.copy(draftConfig = current.copy(reportHeaderStyle = headerStyle))
        }
    }

    fun toggleShowSchoolCodeInHeader() {
        _uiState.update { state ->
            val current = state.draftConfig ?: return@update state
            state.copy(draftConfig = current.copy(showSchoolCodeInHeader = !current.showSchoolCodeInHeader))
        }
    }

    fun toggleShowMottoOnReceipts() {
        _uiState.update { state ->
            val current = state.draftConfig ?: return@update state
            state.copy(draftConfig = current.copy(showMottoOnReceipts = !current.showMottoOnReceipts))
        }
    }

    fun toggleDarkModeDefault() {
        _uiState.update { state ->
            val current = state.draftConfig ?: return@update state
            state.copy(draftConfig = current.copy(darkModeDefault = !current.darkModeDefault))
        }
    }

    fun openLiveCardPreview() {
        _uiState.update { it.copy(showLiveCardPreviewDialog = true) }
    }

    fun closeLiveCardPreview() {
        _uiState.update { it.copy(showLiveCardPreviewDialog = false) }
    }

    fun openResetConfirmDialog() {
        if (!TenantContext.hasPermission(Permission.SCHOOL_CONFIG_EDIT)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires SCHOOL_CONFIG_EDIT permission") }
            return
        }
        _uiState.update { it.copy(showResetConfirmDialog = true) }
    }

    fun closeResetConfirmDialog() {
        _uiState.update { it.copy(showResetConfirmDialog = false) }
    }

    fun discardDraftChanges() {
        _uiState.update { state ->
            state.copy(
                draftConfig = state.activeConfig,
                statusMessage = "Draft branding changes discarded.",
                errorMessage = null
            )
        }
    }

    fun saveBrandingChanges() {
        if (!TenantContext.hasPermission(Permission.SCHOOL_CONFIG_EDIT)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires SCHOOL_CONFIG_EDIT permission") }
            return
        }

        val draft = _uiState.value.draftConfig ?: return
        if (draft.schoolDisplayName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "School display name cannot be empty.") }
            return
        }
        if (draft.schoolCode.isBlank()) {
            _uiState.update { it.copy(errorMessage = "School short code cannot be empty.") }
            return
        }
        if (draft.supportEmail.isNotBlank() && !draft.supportEmail.contains("@")) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid institutional support email.") }
            return
        }

        val updatedConfig = draft.copy(
            schoolDisplayName = draft.schoolDisplayName.trim(),
            schoolCode = draft.schoolCode.trim().uppercase(),
            motto = draft.motto.trim(),
            supportEmail = draft.supportEmail.trim(),
            websiteUrl = draft.websiteUrl.trim(),
            lastUpdatedBy = personaDisplayName(TenantContext.activeUserRole.value),
            updatedAt = System.currentTimeMillis()
        )

        BrandThemeController.applyBranding(updatedConfig)

        _uiState.update {
            it.copy(
                activeConfig = updatedConfig,
                draftConfig = updatedConfig,
                statusMessage = "Branding & theme applied for ${updatedConfig.schoolDisplayName}.",
                errorMessage = null
            )
        }

        viewModelScope.launch {
            brandingRepository.saveBrandingConfig(updatedConfig)
        }
    }

    fun confirmResetToDefault() {
        if (!TenantContext.hasPermission(Permission.SCHOOL_CONFIG_EDIT)) {
            _uiState.update {
                it.copy(
                    showResetConfirmDialog = false,
                    errorMessage = "Unauthorized: Requires SCHOOL_CONFIG_EDIT permission"
                )
            }
            return
        }

        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        val defaults = brandingRepository.getDefaultBrandingForSchool(schoolId)
        BrandThemeController.applyBranding(defaults)

        _uiState.update {
            it.copy(
                activeConfig = defaults,
                draftConfig = defaults,
                showResetConfirmDialog = false,
                statusMessage = "Restored default institutional branding for ${defaults.schoolDisplayName}.",
                errorMessage = null
            )
        }

        viewModelScope.launch {
            brandingRepository.saveBrandingConfig(defaults)
        }
    }

    fun dismissStatusMessage() {
        _uiState.update { it.copy(statusMessage = null, errorMessage = null) }
    }

    private fun personaDisplayName(role: UserRole): String {
        return when (role) {
            UserRole.SUPER_ADMIN -> "Platform Super Admin"
            UserRole.SCHOOL_ADMIN -> "Dr. Robert Vance"
            else -> role.displayName
        }
    }
}
