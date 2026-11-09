package com.example.core.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

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
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = EduTypography,
        shapes = EduShapes,
        content = content
    )
}
