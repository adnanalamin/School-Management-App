package com.example.core.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.features.branding.domain.BrandPalettePreset
import com.example.features.branding.domain.UiCornerStyle

private val LightColorScheme = lightColorScheme(
    primary = EduPrimaryLight,
    onPrimary = EduOnPrimaryLight,
    primaryContainer = EduPrimaryContainerLight,
    onPrimaryContainer = EduOnPrimaryContainerLight,
    secondary = EduSecondaryLight,
    onSecondary = EduOnSecondaryLight,
    secondaryContainer = EduSecondaryContainerLight,
    onSecondaryContainer = EduOnSecondaryContainerLight,
    tertiary = EduTertiaryLight,
    onTertiary = EduOnTertiaryLight,
    tertiaryContainer = EduTertiaryContainerLight,
    onTertiaryContainer = EduOnTertiaryContainerLight,
    background = EduBackgroundLight,
    onBackground = EduOnBackgroundLight,
    surface = EduSurfaceLight,
    onSurface = EduOnSurfaceLight,
    surfaceVariant = EduSurfaceVariantLight,
    onSurfaceVariant = EduOnSurfaceVariantLight,
    outline = EduOutlineLight,
    outlineVariant = EduOutlineVariantLight
)

private val DarkColorScheme = darkColorScheme(
    primary = EduPrimaryDark,
    onPrimary = EduOnPrimaryDark,
    primaryContainer = EduPrimaryContainerDark,
    onPrimaryContainer = EduOnPrimaryContainerDark,
    secondary = EduSecondaryDark,
    onSecondary = EduOnSecondaryDark,
    secondaryContainer = EduSecondaryContainerDark,
    onSecondaryContainer = EduOnSecondaryContainerDark,
    tertiary = EduTertiaryDark,
    onTertiary = EduOnTertiaryDark,
    tertiaryContainer = EduTertiaryContainerDark,
    onTertiaryContainer = EduOnTertiaryContainerDark,
    background = EduBackgroundDark,
    onBackground = EduOnBackgroundDark,
    surface = EduSurfaceDark,
    onSurface = EduOnSurfaceDark,
    surfaceVariant = EduSurfaceVariantDark,
    onSurfaceVariant = EduOnSurfaceVariantDark,
    outline = EduOutlineDark,
    outlineVariant = EduOutlineVariantDark
)

@Composable
fun EduManageTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Default to false to preserve consistent SaaS brand identity
    content: @Composable () -> Unit
) {
    val brandOverride by BrandThemeController.overrideState.collectAsState()
    val effectiveDark = brandOverride?.forceDarkMode ?: darkTheme

    val baseScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (effectiveDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        effectiveDark -> DarkColorScheme
        else -> LightColorScheme
    }

    val preset = brandOverride?.palettePreset
    val colorScheme = if (preset != null && preset != BrandPalettePreset.OXFORD_NAVY) {
        val brandPrimary = Color(preset.primaryColorLong)
        val brandSecondary = Color(preset.secondaryColorLong)
        val brandTertiary = Color(preset.tertiaryColorLong)
        val brandContainer = Color(preset.primaryContainerLightLong)
        if (effectiveDark) {
            baseScheme.copy(
                primary = brandContainer,
                onPrimary = brandPrimary,
                primaryContainer = brandPrimary,
                onPrimaryContainer = brandContainer,
                secondary = brandSecondary,
                tertiary = brandTertiary
            )
        } else {
            baseScheme.copy(
                primary = brandPrimary,
                primaryContainer = brandContainer,
                onPrimaryContainer = brandPrimary,
                secondary = brandSecondary,
                tertiary = brandTertiary
            )
        }
    } else {
        baseScheme
    }

    val shapes = when (brandOverride?.cornerStyle) {
        UiCornerStyle.SHARP -> Shapes(
            extraSmall = RoundedCornerShape(2.dp),
            small = RoundedCornerShape(4.dp),
            medium = RoundedCornerShape(6.dp),
            large = RoundedCornerShape(8.dp),
            extraLarge = RoundedCornerShape(12.dp)
        )
        UiCornerStyle.ROUNDED -> Shapes(
            extraSmall = RoundedCornerShape(8.dp),
            small = RoundedCornerShape(12.dp),
            medium = RoundedCornerShape(20.dp),
            large = RoundedCornerShape(24.dp),
            extraLarge = RoundedCornerShape(32.dp)
        )
        else -> EduShapes
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = EduTypography,
        shapes = shapes,
        content = content
    )
}
