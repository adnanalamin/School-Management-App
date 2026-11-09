package com.example.features.designsystem

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.EduError
import com.example.core.theme.EduInfo
import com.example.core.theme.EduShapes
import com.example.core.theme.EduSpacing
import com.example.core.theme.EduSuccess
import com.example.core.theme.EduWarning
import com.example.shared.components.BadgeType
import com.example.shared.components.EduBadge
import com.example.shared.components.EduButton
import com.example.shared.components.EduButtonVariant
import com.example.shared.components.EduCard
import com.example.shared.components.EduConfirmDialog
import com.example.shared.components.EduDropdown
import com.example.shared.components.EduEmptyState
import com.example.shared.components.EduFilterBottomSheet
import com.example.shared.components.EduPasswordInput
import com.example.shared.components.EduProgressBar
import com.example.shared.components.EduPromptDialog
import com.example.shared.components.EduSearchInput
import com.example.shared.components.EduShimmerCard
import com.example.shared.components.EduStatCard
import com.example.shared.components.EduTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesignSystemScreen(
    viewModel: DesignSystemViewModel,
    modifier: Modifier = Modifier,
    onBackToFoundation: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "EduManage Design System",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Phase 2 Enterprise Component Suite",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleDarkMode() },
                        modifier = Modifier.testTag("ds_theme_toggle")
                    ) {
                        Icon(
                            imageVector = if (uiState.isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Action Feedback Pill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = EduSpacing.lg, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Last Action: ${uiState.lastActionLog}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                    EduBadge(
                        text = if (uiState.isDarkMode) "DARK" else "LIGHT",
                        type = BadgeType.PRIMARY
                    )
                }
            }

            // Tab bar
            val tabs = listOf("Cards & Stats", "Form Inputs", "Dialogs & Sheets", "Shimmer & Feedback")
            PrimaryTabRow(
                selectedTabIndex = uiState.selectedCategoryTab,
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = uiState.selectedCategoryTab == index,
                        onClick = { viewModel.setCategoryTab(index) },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (uiState.selectedCategoryTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.testTag("ds_tab_$index")
                    )
                }
            }

            // Tab Content
            when (uiState.selectedCategoryTab) {
                0 -> CardsAndStatsTab()
                1 -> FormInputsTab(viewModel, uiState)
                2 -> DialogsAndSheetsTab(viewModel)
                3 -> ShimmerAndFeedbackTab(viewModel, uiState)
            }
        }
    }

    // Modal dialogs & bottom sheets
    if (uiState.showConfirmDialog) {
        EduConfirmDialog(
            title = "Archive Student Record?",
            message = "This will mark the student as inactive in the active tenant. Academic history and transcripts will be preserved.",
            onConfirm = { viewModel.onConfirmAction() },
            onDismiss = { viewModel.closeConfirmDialog() },
            isDestructive = true,
            confirmText = "Archive Student"
        )
    }

    if (uiState.showPromptDialog) {
        EduPromptDialog(
            title = "Create New Section",
            message = "Enter the section designation (e.g., Section D - Morning Shift):",
            initialValue = "",
            label = "Section Name",
            onConfirm = { viewModel.onPromptSubmit(it) },
            onDismiss = { viewModel.closePromptDialog() },
            confirmText = "Create"
        )
    }

    if (uiState.showFilterBottomSheet) {
        EduFilterBottomSheet(
            title = "Filter Directory",
            onDismiss = { viewModel.closeFilterBottomSheet() },
            onApply = { viewModel.applyFilters() },
            onReset = { viewModel.resetFilters() }
        ) {
            Text(
                text = "Enrollment Status",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(EduSpacing.sm))

            val statuses = listOf("All", "Active", "Pending", "Suspended")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(EduSpacing.sm)
            ) {
                statuses.forEach { status ->
                    val isSelected = status == uiState.selectedStatusFilter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            )
                            .clickable { viewModel.setStatusFilter(status) }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = status,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CardsAndStatsTab() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(EduSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(EduSpacing.md)
    ) {
        item {
            Text(
                text = "Executive Stat & Trend Cards",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(EduSpacing.md)
            ) {
                EduStatCard(
                    title = "Total Students",
                    value = "1,480",
                    icon = Icons.Default.School,
                    trendText = "+8.4%",
                    isPositiveTrend = true,
                    subtitle = "vs last session",
                    modifier = Modifier.weight(1f),
                    testTag = "stat_students"
                )
                EduStatCard(
                    title = "Faculty & Staff",
                    value = "94",
                    icon = Icons.Default.Group,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    iconContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    trendText = "+2 new",
                    isPositiveTrend = true,
                    subtitle = "this term",
                    modifier = Modifier.weight(1f),
                    testTag = "stat_faculty"
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(EduSpacing.md)
            ) {
                EduStatCard(
                    title = "Daily Attendance",
                    value = "94.2%",
                    icon = Icons.Default.CheckCircle,
                    iconTint = EduSuccess,
                    iconContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    trendText = "+1.8%",
                    isPositiveTrend = true,
                    subtitle = "vs yesterday",
                    modifier = Modifier.weight(1f),
                    testTag = "stat_attendance"
                )
                EduStatCard(
                    title = "Pending Dues",
                    value = "$14,250",
                    icon = Icons.Default.AttachMoney,
                    iconTint = EduWarning,
                    iconContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    trendText = "-4.5%",
                    isPositiveTrend = true,
                    subtitle = "collection target",
                    modifier = Modifier.weight(1f),
                    testTag = "stat_fees"
                )
            }
        }

        item {
            EduCard(testTag = "card_interactive_summary") {
                Text(
                    text = "Interactive Tonal Card",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(EduSpacing.xs))
                Text(
                    text = "Standard elevated card component with customizable border, tonal container, and child content padding.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(EduSpacing.md))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EduBadge(text = "ACADEMIC 2026-2027", type = BadgeType.PRIMARY)
                    EduBadge(text = "SYSTEM ACTIVE", type = BadgeType.SUCCESS)
                }
            }
        }
    }
}

@Composable
private fun FormInputsTab(viewModel: DesignSystemViewModel, uiState: DesignSystemUiState) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(EduSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(EduSpacing.md)
    ) {
        item {
            Text(
                text = "Form Inputs & Controls",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            EduSearchInput(
                query = uiState.searchQuery,
                onQueryChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = "Search students, staff, classes...",
                testTag = "ds_search_input"
            )
        }

        item {
            EduDropdown(
                label = "Assigned Class & Section",
                options = uiState.availableClasses,
                selectedOption = uiState.selectedClass,
                onOptionSelected = { viewModel.onClassSelected(it) },
                leadingIcon = Icons.Default.School,
                testTag = "ds_class_dropdown"
            )
        }

        item {
            EduTextField(
                value = uiState.textInputValue,
                onValueChange = { viewModel.onTextInputChanged(it) },
                label = "Student Full Name",
                placeholder = "Enter legal full name",
                errorMessage = uiState.textInputError,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                testTag = "ds_name_input"
            )
        }

        item {
            EduPasswordInput(
                value = uiState.passwordValue,
                onValueChange = { viewModel.onPasswordChanged(it) },
                label = "Account Security Password",
                placeholder = "Minimum 8 characters",
                testTag = "ds_password_input"
            )
        }
    }
}

@Composable
private fun DialogsAndSheetsTab(viewModel: DesignSystemViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(EduSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(EduSpacing.md)
    ) {
        item {
            Text(
                text = "Modal Dialogs & Bottom Sheets",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            EduCard(testTag = "card_dialog_actions") {
                Text(
                    text = "Trigger Confirmation Dialogs",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(EduSpacing.xs))
                Text(
                    text = "Accessible AlertDialogs with explicit confirm and dismiss actions, supporting warning and destructive states.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(EduSpacing.md))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(EduSpacing.sm)
                ) {
                    EduButton(
                        text = "Destructive Confirm",
                        onClick = { viewModel.openConfirmDialog() },
                        variant = EduButtonVariant.DANGER,
                        modifier = Modifier.weight(1f),
                        testTag = "trigger_confirm_dialog"
                    )
                    EduButton(
                        text = "Input Prompt",
                        onClick = { viewModel.openPromptDialog() },
                        variant = EduButtonVariant.OUTLINED,
                        modifier = Modifier.weight(1f),
                        testTag = "trigger_prompt_dialog"
                    )
                }
            }
        }

        item {
            EduCard(testTag = "card_sheet_actions") {
                Text(
                    text = "Trigger Filter Bottom Sheet",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(EduSpacing.xs))
                Text(
                    text = "Modal bottom sheet adhering to M3 navigation bars with customizable filter chips, reset, and apply triggers.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(EduSpacing.md))
                EduButton(
                    text = "Open Filter Bottom Sheet",
                    onClick = { viewModel.openFilterBottomSheet() },
                    variant = EduButtonVariant.PRIMARY,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "trigger_filter_sheet"
                )
            }
        }
    }
}

@Composable
private fun ShimmerAndFeedbackTab(viewModel: DesignSystemViewModel, uiState: DesignSystemUiState) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(EduSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(EduSpacing.md)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Skeleton Shimmer Loader",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                EduButton(
                    text = if (uiState.isShimmerLoading) "Show Real Data" else "Simulate Loading",
                    onClick = { viewModel.toggleShimmerLoading() },
                    variant = EduButtonVariant.OUTLINED,
                    testTag = "toggle_shimmer_btn"
                )
            }
        }

        item {
            if (uiState.isShimmerLoading) {
                Column(verticalArrangement = Arrangement.spacedBy(EduSpacing.sm)) {
                    EduShimmerCard()
                    EduShimmerCard()
                    EduShimmerCard()
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(EduSpacing.sm)) {
                    StudentPreviewCard(name = "Amina Rahman", roll = "101", className = "Grade 10 - A")
                    StudentPreviewCard(name = "Tanvir Ahmed", roll = "102", className = "Grade 10 - A")
                    StudentPreviewCard(name = "Zubaida Islam", roll = "103", className = "Grade 10 - B")
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(EduSpacing.sm))
            Text(
                text = "Progress & Performance Meters",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            EduCard(testTag = "card_progress_meters") {
                EduProgressBar(
                    label = "Monthly Student Attendance Target",
                    progress = 0.942f,
                    valueDisplay = "94.2%",
                    color = EduSuccess
                )
                Spacer(modifier = Modifier.height(EduSpacing.md))
                EduProgressBar(
                    label = "Q1 Tuition Fee Collection",
                    progress = 0.785f,
                    valueDisplay = "78.5%",
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(EduSpacing.md))
                EduProgressBar(
                    label = "Syllabus Completion",
                    progress = 0.650f,
                    valueDisplay = "65.0%",
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(EduSpacing.sm))
            Text(
                text = "Empty State Component",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            EduCard(testTag = "card_empty_state_demo") {
                EduEmptyState(
                    icon = Icons.Default.FolderOpen,
                    title = "No Homework Submissions Yet",
                    message = "Students enrolled in Grade 10 - Science have not submitted any deliverables for this assignment.",
                    actionText = "Send Reminder",
                    onActionClick = {}
                )
            }
        }
    }
}

@Composable
private fun StudentPreviewCard(name: String, roll: String, className: String) {
    EduCard(testTag = "student_preview_$roll") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name.take(2).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(EduSpacing.md))
                Column {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Roll: $roll • $className",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            EduBadge(text = "PRESENT", type = BadgeType.SUCCESS)
        }
    }
}
