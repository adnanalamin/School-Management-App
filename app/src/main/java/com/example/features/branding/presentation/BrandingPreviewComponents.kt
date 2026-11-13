package com.example.features.branding.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.theme.EduShapes
import com.example.core.theme.EduSpacing
import com.example.features.branding.domain.BrandPalettePreset
import com.example.features.branding.domain.CrestEmblemStyle
import com.example.features.branding.domain.ReportCardHeaderStyle
import com.example.features.branding.domain.SchoolBrandingConfig
import com.example.shared.components.EduButton
import com.example.shared.components.EduButtonVariant

fun CrestEmblemStyle.toIcon(): ImageVector = when (this) {
    CrestEmblemStyle.SHIELD_CLASSIC -> Icons.Default.Shield
    CrestEmblemStyle.TORCH_ACADEMIC -> Icons.Default.LocalFireDepartment
    CrestEmblemStyle.LAUREL_STEM -> Icons.Default.EmojiEvents
    CrestEmblemStyle.COMPASS_GLOBAL -> Icons.Default.Explore
    CrestEmblemStyle.CROWN_HERITAGE -> Icons.Default.WorkspacePremium
}

@Composable
fun LivePortalHeaderPreviewCard(
    config: SchoolBrandingConfig,
    modifier: Modifier = Modifier
) {
    val primaryColor = Color(config.palettePreset.primaryColorLong)
    val secondaryColor = Color(config.palettePreset.secondaryColorLong)
    val cardShape = RoundedCornerShape(config.cornerStyle.cardRadiusDp.dp)
    val badgeShape = RoundedCornerShape(config.cornerStyle.buttonRadiusDp.dp)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("branding_live_preview_card"),
        shape = cardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(primaryColor, secondaryColor)
                    )
                )
                .padding(EduSpacing.lg)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(EduSpacing.sm)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = config.crestEmblem.toIcon(),
                                    contentDescription = config.crestEmblem.displayName,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(EduSpacing.md))
                        Column {
                            Text(
                                text = config.schoolDisplayName.ifBlank { "School Name Preview" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            if (config.motto.isNotBlank()) {
                                Text(
                                    text = "\"${config.motto}\"",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontStyle = FontStyle.Italic,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.88f)
                                )
                            }
                        }
                    }

                    if (config.showSchoolCodeInHeader && config.schoolCode.isNotBlank()) {
                        Surface(
                            shape = badgeShape,
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.22f)
                        ) {
                            Text(
                                text = config.schoolCode,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Session: ${config.academicSession}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                    )
                    Text(
                        text = "Currency: ${config.currencyCode} (${config.currencySymbol})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

@Composable
fun PalettePresetSelectorList(
    selectedPreset: BrandPalettePreset,
    onSelectPreset: (BrandPalettePreset) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(EduSpacing.sm)) {
        BrandPalettePreset.entries.forEach { preset ->
            val isSelected = preset == selectedPreset
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .clip(EduShapes.medium)
                    .clickable { onSelectPreset(preset) }
                    .testTag("palette_preset_${preset.name}"),
                shape = EduShapes.medium,
                tonalElevation = if (isSelected) 3.dp else 0.dp,
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) Color(preset.primaryColorLong) else MaterialTheme.colorScheme.outlineVariant
                ),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = EduSpacing.md, vertical = EduSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(preset.primaryColorLong))
                                .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected palette",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(Color(preset.secondaryColorLong))
                        )
                        Spacer(modifier = Modifier.width(EduSpacing.md))
                        Column {
                            Text(
                                text = preset.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = preset.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = preset.primaryColorHex,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CrestEmblemSelector(
    selectedEmblem: CrestEmblemStyle,
    brandPrimaryColor: Color,
    onSelectEmblem: (CrestEmblemStyle) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(EduSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(EduSpacing.sm)
    ) {
        CrestEmblemStyle.entries.forEach { emblem ->
            val isSelected = emblem == selectedEmblem
            Surface(
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clip(EduShapes.medium)
                    .clickable { onSelectEmblem(emblem) }
                    .testTag("crest_emblem_${emblem.name}"),
                shape = EduShapes.medium,
                color = if (isSelected) brandPrimaryColor.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = if (isSelected) brandPrimaryColor else MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = emblem.toIcon(),
                        contentDescription = emblem.displayName,
                        tint = if (isSelected) brandPrimaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = emblem.displayName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun ReportCardHeaderPreviewDialog(
    config: SchoolBrandingConfig,
    onDismiss: () -> Unit
) {
    val primaryColor = Color(config.palettePreset.primaryColorLong)
    val cardShape = RoundedCornerShape(config.cornerStyle.cardRadiusDp.dp)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Branded Document & Receipt Preview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(EduSpacing.md)) {
                Surface(
                    shape = cardShape,
                    border = BorderStroke(1.5.dp, primaryColor.copy(alpha = 0.5f)),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Header by ReportCardHeaderStyle
                        val headerBg = when (config.reportHeaderStyle) {
                            ReportCardHeaderStyle.MODERN_BANNER -> primaryColor
                            ReportCardHeaderStyle.CLASSIC_CREST -> primaryColor.copy(alpha = 0.1f)
                            ReportCardHeaderStyle.MINIMAL_MONOCHROME -> MaterialTheme.colorScheme.surfaceVariant
                        }
                        val headerTextColor = when (config.reportHeaderStyle) {
                            ReportCardHeaderStyle.MODERN_BANNER -> MaterialTheme.colorScheme.onPrimary
                            ReportCardHeaderStyle.CLASSIC_CREST -> primaryColor
                            ReportCardHeaderStyle.MINIMAL_MONOCHROME -> MaterialTheme.colorScheme.onSurface
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(headerBg)
                                .padding(EduSpacing.md),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = config.crestEmblem.toIcon(),
                                contentDescription = config.crestEmblem.displayName,
                                tint = headerTextColor,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(EduSpacing.sm))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = config.schoolDisplayName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = headerTextColor
                                )
                                Text(
                                    text = "${config.schoolCode} • Academic Year ${config.academicSession}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = headerTextColor.copy(alpha = 0.85f)
                                )
                            }
                        }

                        Column(
                            modifier = Modifier.padding(EduSpacing.md),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "OFFICIAL TUITION RECEIPT & TRANSCRIPT SAMPLE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Tuition Term 1", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    text = "${config.currencySymbol}1,250.00 ${config.currencyCode}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Support Contact", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    text = config.supportEmail.ifBlank { "admin@school.edu" },
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            if (config.showMottoOnReceipts && config.motto.isNotBlank()) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                Text(
                                    text = "\"${config.motto}\"",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontStyle = FontStyle.Italic,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            EduButton(
                text = "Close Preview",
                onClick = onDismiss,
                variant = EduButtonVariant.PRIMARY,
                testTag = "btn_close_branding_preview"
            )
        },
        shape = EduShapes.large
    )
}
