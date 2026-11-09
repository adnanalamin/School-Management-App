package com.example.features.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.EduSpacing
import com.example.shared.components.BadgeType
import com.example.shared.components.EduBadge
import com.example.shared.components.EduButton
import com.example.shared.components.EduButtonVariant
import com.example.shared.components.EduCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateToFoundation: () -> Unit,
    onNavigateToDesignSystem: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("settings_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "School & App Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = EduSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(EduSpacing.md)
        ) {
            // General Preferences
            item {
                Text(
                    text = "Preferences & Display",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                EduCard(testTag = "card_preferences") {
                    SettingToggleRow(
                        title = "Dark Theme",
                        subtitle = "Toggle high-contrast dark color palette",
                        checked = uiState.isDarkMode,
                        onCheckedChange = { viewModel.toggleDarkMode() },
                        testTag = "switch_dark_mode"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    SettingClickableRow(
                        title = "Language / ভাষা",
                        subtitle = uiState.selectedLanguage,
                        onClick = {
                            val nextLang = if (uiState.selectedLanguage.startsWith("English")) "Bengali (বাংলা)" else "English (US)"
                            viewModel.selectLanguage(nextLang)
                        },
                        testTag = "btn_change_language"
                    )
                }
            }

            // Notification Settings
            item {
                Text(
                    text = "Push Notifications & Alerts",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                EduCard(testTag = "card_notifications") {
                    SettingToggleRow(
                        title = "Push Notifications",
                        subtitle = "Global notifications for exams, notices, and events",
                        checked = uiState.notificationsEnabled,
                        onCheckedChange = { viewModel.toggleNotifications() },
                        testTag = "switch_push_notifications"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    SettingToggleRow(
                        title = "Daily Attendance SMS/Alerts",
                        subtitle = "Alert guardians upon student morning check-in",
                        checked = uiState.attendanceAlertsEnabled,
                        onCheckedChange = { viewModel.toggleAttendanceAlerts() },
                        testTag = "switch_attendance_alerts"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    SettingToggleRow(
                        title = "Fee Due Reminders",
                        subtitle = "Send automated payment notifications 3 days before deadline",
                        checked = uiState.feeRemindersEnabled,
                        onCheckedChange = { viewModel.toggleFeeReminders() },
                        testTag = "switch_fee_reminders"
                    )
                }
            }

            // Storage & Offline Cache
            item {
                Text(
                    text = "Storage & Synchronization",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                EduCard(testTag = "card_storage") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Offline Cached Records", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(text = "${uiState.cachedRecordsCount} items cached locally for offline use", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        EduButton(
                            text = "Clear Cache",
                            onClick = { viewModel.clearCache() },
                            variant = EduButtonVariant.OUTLINED,
                            testTag = "btn_clear_cache"
                        )
                    }
                }
            }

            // Architecture & Development Navigation
            item {
                Text(
                    text = "Architecture & Testing Tools",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                EduCard(testTag = "card_dev_tools") {
                    SettingClickableRow(
                        title = "Foundation & System Status Hub",
                        subtitle = "Inspect multi-tenant isolation, RBAC matrix & roadmap",
                        onClick = onNavigateToFoundation,
                        testTag = "nav_to_foundation_link"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    SettingClickableRow(
                        title = "Design Tokens & Components Suite",
                        subtitle = "Interactive component catalog, modals, and shimmer testbed",
                        onClick = onNavigateToDesignSystem,
                        testTag = "nav_to_design_system_link"
                    )
                }
            }

            item {
                Text(
                    text = uiState.appVersion,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = EduSpacing.sm)
                )
            }
        }
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = EduSpacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
private fun SettingClickableRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = EduSpacing.md)
            .testTag(testTag),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            imageVector = Icons.AutoMirrored.Default.ArrowForwardIos,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
    }
}
