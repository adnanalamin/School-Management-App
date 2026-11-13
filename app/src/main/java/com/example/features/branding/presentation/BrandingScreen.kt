package com.example.features.branding.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.security.Permission
import com.example.core.security.TenantContext
import com.example.core.theme.EduShapes
import com.example.core.theme.EduSpacing
import com.example.features.branding.domain.DashboardLayoutDensity
import com.example.features.branding.domain.ReportCardHeaderStyle
import com.example.features.branding.domain.UiCornerStyle
import com.example.shared.components.BadgeType
import com.example.shared.components.EduBadge
import com.example.shared.components.EduButton
import com.example.shared.components.EduButtonVariant
import com.example.shared.components.EduCard

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BrandingScreen(
    viewModel: BrandingViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val canEdit = TenantContext.hasPermission(Permission.SCHOOL_CONFIG_EDIT)
    val draft = uiState.draftConfig

    LaunchedEffect(uiState.statusMessage, uiState.errorMessage) {
        val msg = uiState.errorMessage ?: uiState.statusMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissStatusMessage()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("branding_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "School Customization & Branding",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = draft?.formattedBrandSummary ?: "White-label tenant studio",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("btn_branding_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.openLiveCardPreview() },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("btn_branding_preview_doc")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "Preview branded receipt and report card"
                        )
                    }
                    if (canEdit) {
                        IconButton(
                            onClick = { viewModel.openResetConfirmDialog() },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("btn_branding_reset")
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = "Reset branding to defaults"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            if (canEdit && draft != null) {
                Surface(
                    tonalElevation = 4.dp,
                    shadowElevation = 6.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = EduSpacing.lg, vertical = EduSpacing.md),
                        horizontalArrangement = Arrangement.spacedBy(EduSpacing.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (uiState.hasUnsavedChanges) {
                            EduButton(
                                text = "Discard",
                                onClick = { viewModel.discardDraftChanges() },
                                variant = EduButtonVariant.OUTLINED,
                                modifier = Modifier.weight(1f),
                                testTag = "btn_branding_discard"
                            )
                        }
                        EduButton(
                            text = if (uiState.hasUnsavedChanges) "Apply & Save Brand" else "Brand Saved",
                            onClick = { viewModel.saveBrandingChanges() },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            enabled = uiState.hasUnsavedChanges,
                            variant = EduButtonVariant.PRIMARY,
                            modifier = Modifier.weight(1.4f),
                            testTag = "btn_branding_save"
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        if (draft == null) {
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = EduSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(EduSpacing.md)
        ) {
            item {
                Spacer(modifier = Modifier.height(EduSpacing.xs))
                LivePortalHeaderPreviewCard(config = draft)
            }

            // Tab Strip
            item {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(EduSpacing.sm),
                    verticalArrangement = Arrangement.spacedBy(EduSpacing.xs)
                ) {
                    BrandingTab.entries.forEach { tab ->
                        FilterChip(
                            selected = uiState.selectedTab == tab,
                            onClick = { viewModel.selectTab(tab) },
                            label = { Text(tab.label) },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("branding_tab_${tab.name}")
                        )
                    }
                }
            }

            when (uiState.selectedTab) {
                BrandingTab.IDENTITY -> {
                    item {
                        EduCard(testTag = "card_branding_identity") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "School Profile & Motto",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                EduBadge(
                                    text = draft.schoolId,
                                    type = BadgeType.INFO
                                )
                            }
                            Spacer(modifier = Modifier.height(EduSpacing.md))

                            OutlinedTextField(
                                value = draft.schoolDisplayName,
                                onValueChange = { viewModel.updateIdentityFields(schoolDisplayName = it) },
                                label = { Text("Institution Display Name") },
                                singleLine = true,
                                enabled = canEdit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_brand_display_name")
                            )
                            Spacer(modifier = Modifier.height(EduSpacing.sm))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(EduSpacing.sm)
                            ) {
                                OutlinedTextField(
                                    value = draft.schoolCode,
                                    onValueChange = { viewModel.updateIdentityFields(schoolCode = it) },
                                    label = { Text("Short Code") },
                                    singleLine = true,
                                    enabled = canEdit,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("input_brand_code")
                                )
                                OutlinedTextField(
                                    value = draft.academicSession,
                                    onValueChange = { viewModel.updateIdentityFields(academicSession = it) },
                                    label = { Text("Academic Session") },
                                    singleLine = true,
                                    enabled = canEdit,
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .testTag("input_brand_session")
                                )
                            }
                            Spacer(modifier = Modifier.height(EduSpacing.sm))

                            OutlinedTextField(
                                value = draft.motto,
                                onValueChange = { viewModel.updateIdentityFields(motto = it) },
                                label = { Text("Institutional Motto / Tagline") },
                                singleLine = true,
                                enabled = canEdit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_brand_motto")
                            )
                            Spacer(modifier = Modifier.height(EduSpacing.sm))

                            OutlinedTextField(
                                value = draft.supportEmail,
                                onValueChange = { viewModel.updateIdentityFields(supportEmail = it) },
                                label = { Text("Support / Branding Contact Email") },
                                singleLine = true,
                                enabled = canEdit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_brand_email")
                            )
                            Spacer(modifier = Modifier.height(EduSpacing.sm))

                            OutlinedTextField(
                                value = draft.websiteUrl,
                                onValueChange = { viewModel.updateIdentityFields(websiteUrl = it) },
                                label = { Text("Official Portal Website") },
                                singleLine = true,
                                enabled = canEdit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_brand_website")
                            )
                        }
                    }

                    item {
                        EduCard(testTag = "card_branding_regional") {
                            Text(
                                text = "Regional & Bursar Defaults",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(EduSpacing.md))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(EduSpacing.sm)
                            ) {
                                OutlinedTextField(
                                    value = draft.currencyCode,
                                    onValueChange = { viewModel.updateIdentityFields(currencyCode = it.uppercase()) },
                                    label = { Text("Currency Code") },
                                    singleLine = true,
                                    enabled = canEdit,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("input_brand_currency_code")
                                )
                                OutlinedTextField(
                                    value = draft.currencySymbol,
                                    onValueChange = { viewModel.updateIdentityFields(currencySymbol = it) },
                                    label = { Text("Symbol") },
                                    singleLine = true,
                                    enabled = canEdit,
                                    modifier = Modifier
                                        .weight(0.7f)
                                        .testTag("input_brand_currency_symbol")
                                )
                            }
                            Spacer(modifier = Modifier.height(EduSpacing.sm))

                            OutlinedTextField(
                                value = draft.timezone,
                                onValueChange = { viewModel.updateIdentityFields(timezone = it) },
                                label = { Text("Campus Timezone (IANA)") },
                                singleLine = true,
                                enabled = canEdit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_brand_timezone")
                            )
                        }
                    }
                }

                BrandingTab.THEME_PALETTE -> {
                    item {
                        EduCard(testTag = "card_branding_palette") {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Color Palette Presets",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(EduSpacing.sm))
                            Text(
                                text = "Applies immediately to Material 3 primary, secondary, and container surfaces once saved.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(EduSpacing.md))

                            PalettePresetSelectorList(
                                selectedPreset = draft.palettePreset,
                                onSelectPreset = { if (canEdit) viewModel.selectPalettePreset(it) }
                            )
                        }
                    }

                    item {
                        EduCard(testTag = "card_branding_crest_shape") {
                            Text(
                                text = "Institutional Crest Emblem",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(EduSpacing.sm))
                            CrestEmblemSelector(
                                selectedEmblem = draft.crestEmblem,
                                brandPrimaryColor = Color(draft.palettePreset.primaryColorLong),
                                onSelectEmblem = { if (canEdit) viewModel.selectCrestEmblem(it) }
                            )

                            Spacer(modifier = Modifier.height(EduSpacing.md))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(modifier = Modifier.height(EduSpacing.md))

                            Text(
                                text = "Component Corner Geometry",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(EduSpacing.sm))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(EduSpacing.sm),
                                verticalArrangement = Arrangement.spacedBy(EduSpacing.xs)
                            ) {
                                UiCornerStyle.entries.forEach { cornerStyle ->
                                    FilterChip(
                                        selected = draft.cornerStyle == cornerStyle,
                                        onClick = { if (canEdit) viewModel.selectCornerStyle(cornerStyle) },
                                        label = { Text(cornerStyle.displayName) },
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .testTag("corner_style_${cornerStyle.name}")
                                    )
                                }
                            }
                        }
                    }
                }

                BrandingTab.DOCUMENTS_LAYOUT -> {
                    item {
                        EduCard(testTag = "card_branding_header_style") {
                            Text(
                                text = "Report Card & Receipt Letterhead",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(EduSpacing.sm))

                            Column(verticalArrangement = Arrangement.spacedBy(EduSpacing.sm)) {
                                ReportCardHeaderStyle.entries.forEach { style ->
                                    val isSelected = draft.reportHeaderStyle == style
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .minimumInteractiveComponentSize()
                                            .clickable { if (canEdit) viewModel.selectReportHeaderStyle(style) }
                                            .testTag("header_style_${style.name}"),
                                        shape = EduShapes.medium,
                                        tonalElevation = if (isSelected) 2.dp else 0.dp,
                                        border = BorderStroke(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                        )
                                    ) {
                                        Column(modifier = Modifier.padding(EduSpacing.md)) {
                                            Text(
                                                text = style.displayName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = style.subtitle,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(EduSpacing.md))
                            EduButton(
                                text = "Preview Receipt & Transcript",
                                onClick = { viewModel.openLiveCardPreview() },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Visibility,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                variant = EduButtonVariant.OUTLINED,
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "btn_open_doc_preview_card"
                            )
                        }
                    }

                    item {
                        EduCard(testTag = "card_branding_layout_toggles") {
                            Text(
                                text = "Dashboard Density & Visibility",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(EduSpacing.sm))

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(EduSpacing.sm),
                                verticalArrangement = Arrangement.spacedBy(EduSpacing.xs)
                            ) {
                                DashboardLayoutDensity.entries.forEach { density ->
                                    FilterChip(
                                        selected = draft.layoutDensity == density,
                                        onClick = { if (canEdit) viewModel.selectLayoutDensity(density) },
                                        label = { Text(density.displayName) },
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .testTag("layout_density_${density.name}")
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(EduSpacing.sm))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                            BrandingSwitchRow(
                                title = "Display School Code in Header",
                                subtitle = "Shows ${draft.schoolCode} badge in top portal bars",
                                checked = draft.showSchoolCodeInHeader,
                                enabled = canEdit,
                                onCheckedChange = { viewModel.toggleShowSchoolCodeInHeader() },
                                testTag = "switch_show_school_code"
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                            BrandingSwitchRow(
                                title = "Print Motto on Fee Receipts",
                                subtitle = "Includes institutional tagline on PDF/CSV receipts",
                                checked = draft.showMottoOnReceipts,
                                enabled = canEdit,
                                onCheckedChange = { viewModel.toggleShowMottoOnReceipts() },
                                testTag = "switch_show_motto_receipts"
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                            BrandingSwitchRow(
                                title = "Default Dark Mode Workspace",
                                subtitle = "Enables high-contrast dark theme across school devices",
                                checked = draft.darkModeDefault,
                                enabled = canEdit,
                                onCheckedChange = { viewModel.toggleDarkModeDefault() },
                                testTag = "switch_dark_mode_default"
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(EduSpacing.lg))
            }
        }
    }

    if (uiState.showLiveCardPreviewDialog && draft != null) {
        ReportCardHeaderPreviewDialog(
            config = draft,
            onDismiss = { viewModel.closeLiveCardPreview() }
        )
    }

    if (uiState.showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.closeResetConfirmDialog() },
            title = { Text("Reset School Branding?", fontWeight = FontWeight.Bold) },
            text = {
                Text("This restores the default institutional theme palette, crest emblem, and letterhead settings for your campus.")
            },
            confirmButton = {
                EduButton(
                    text = "Restore Defaults",
                    onClick = { viewModel.confirmResetToDefault() },
                    variant = EduButtonVariant.PRIMARY,
                    testTag = "btn_confirm_branding_reset"
                )
            },
            dismissButton = {
                EduButton(
                    text = "Cancel",
                    onClick = { viewModel.closeResetConfirmDialog() },
                    variant = EduButtonVariant.OUTLINED,
                    testTag = "btn_cancel_branding_reset"
                )
            },
            shape = EduShapes.large
        )
    }
}

@Composable
private fun BrandingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean,
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
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .testTag(testTag)
        )
    }
}
