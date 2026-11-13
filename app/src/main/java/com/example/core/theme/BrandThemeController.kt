package com.example.core.theme

import com.example.features.branding.domain.BrandPalettePreset
import com.example.features.branding.domain.SchoolBrandingConfig
import com.example.features.branding.domain.UiCornerStyle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Reactive theme override holder so school-level white-label branding updates propagate live
 * across EduManageTheme without restarting the activity.
 */
data class BrandThemeOverride(
    val schoolId: String? = null,
    val palettePreset: BrandPalettePreset = BrandPalettePreset.OXFORD_NAVY,
    val cornerStyle: UiCornerStyle = UiCornerStyle.BALANCED,
    val forceDarkMode: Boolean? = null
)

object BrandThemeController {
    private val _overrideState = MutableStateFlow<BrandThemeOverride?>(null)
    val overrideState: StateFlow<BrandThemeOverride?> = _overrideState.asStateFlow()

    fun applyBranding(config: SchoolBrandingConfig) {
        _overrideState.value = BrandThemeOverride(
            schoolId = config.schoolId,
            palettePreset = config.palettePreset,
            cornerStyle = config.cornerStyle,
            forceDarkMode = if (config.darkModeDefault) true else null
        )
    }

    fun resetToDefault() {
        _overrideState.value = null
    }
}
