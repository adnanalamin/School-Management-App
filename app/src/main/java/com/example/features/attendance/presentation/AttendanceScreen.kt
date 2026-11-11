package com.example.features.attendance.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.security.Permission
import com.example.core.security.TenantContext
import com.example.core.theme.EduElevation
import com.example.core.theme.EduError
import com.example.core.theme.EduErrorContainer
import com.example.core.theme.EduShapes
import com.example.core.theme.EduSpacing
import com.example.core.theme.EduSuccess
import com.example.core.theme.EduSuccessContainer
import com.example.features.attendance.domain.AttendanceStatus
import com.example.features.attendance.domain.StudentAttendanceEntry
import com.example.shared.components.EduButton
import com.example.shared.components.EduButtonVariant
import com.example.shared.components.EduStatCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    viewModel: AttendanceViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val hasMarkPermission = TenantContext.hasPermission(Permission.ATTENDANCE_MARK)
    val hasEditPermission = TenantContext.hasPermission(Permission.ATTENDANCE_EDIT)

    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatusMessage()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearErrorMessage()
        }
    }

    val displayDateFormatted = remember(uiState.selectedDate) {
        try {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val formatter = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.US)
            val parsed = parser.parse(uiState.selectedDate)
            if (parsed != null) formatter.format(parsed) else uiState.selectedDate
        } catch (e: Exception) {
            uiState.selectedDate
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("attendance_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Attendance Register",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${uiState.activeRecord?.className ?: "Class"} • ${uiState.activeRecord?.sectionName ?: "Section"}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("attendance_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.openAuditDialog() },
                        modifier = Modifier.testTag("open_audit_log_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Audit Log",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
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
            // 1. Date Navigator Card
            item {
                Spacer(modifier = Modifier.height(EduSpacing.xs))
                DateNavigatorCard(
                    displayDate = displayDateFormatted,
                    onPrevious = { viewModel.onPreviousDay() },
                    onNext = { viewModel.onNextDay() }
                )
            }

            // 2. Class & Section Filter Chips
            item {
                ClassAndSectionFilters(
                    classes = uiState.availableClasses,
                    selectedClassId = uiState.selectedClassId,
                    sections = uiState.availableSections,
                    selectedSectionId = uiState.selectedSectionId,
                    onClassSelect = { viewModel.onClassSelected(it) },
                    onSectionSelect = { viewModel.onSectionSelected(it) }
                )
            }

            // 3. Executive Stat Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(EduSpacing.sm)
                ) {
                    EduStatCard(
                        title = "Present Rate",
                        value = "${uiState.presentPercentage}%",
                        icon = Icons.Default.Percent,
                        modifier = Modifier.weight(1f),
                        iconTint = EduSuccess,
                        iconContainerColor = EduSuccessContainer,
                        trendText = "${uiState.presentCount} Present",
                        isPositiveTrend = true,
                        testTag = "stat_card_present_rate"
                    )
                    EduStatCard(
                        title = "Enrolled",
                        value = "${uiState.totalEnrolled}",
                        icon = Icons.Default.People,
                        modifier = Modifier.weight(1f),
                        subtitle = "In this section",
                        testTag = "stat_card_total_enrolled"
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(EduSpacing.sm)
                ) {
                    EduStatCard(
                        title = "Absentees",
                        value = "${uiState.absentCount}",
                        icon = Icons.Default.Close,
                        modifier = Modifier.weight(1f),
                        iconTint = EduError,
                        iconContainerColor = EduErrorContainer,
                        trendText = if (uiState.absentCount > 0) "Needs follow-up" else "All accounted",
                        isPositiveTrend = uiState.absentCount == 0,
                        testTag = "stat_card_absentees"
                    )
                    EduStatCard(
                        title = "Late / Excused",
                        value = "${uiState.lateCount + uiState.excusedCount}",
                        icon = Icons.Default.Schedule,
                        modifier = Modifier.weight(1f),
                        subtitle = "${uiState.lateCount} Late • ${uiState.excusedCount} Excused",
                        testTag = "stat_card_late_excused"
                    )
                }
            }

            // 4. Finalization & Lock Banner
            item {
                if (uiState.isFinalized) {
                    FinalizedLockBanner(
                        isFinalized = true,
                        hasEditPermission = hasEditPermission,
                        onUnlock = { viewModel.openReopenDialog() }
                    )
                }
            }

            // 5. Bulk Roll Call Toolbar
            item {
                BulkActionToolbar(
                    isFinalized = uiState.isFinalized,
                    hasMarkPermission = hasMarkPermission,
                    onMarkAllPresent = { viewModel.markAllPresent() },
                    onMarkAllAbsent = { viewModel.markAllAbsent() },
                    onSaveDraft = { viewModel.saveDraft() },
                    onFinalize = { viewModel.finalizeRegister() }
                )
            }

            // 6. Search Bar & Count
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChange(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("attendance_search_input"),
                        placeholder = { Text("Search by name, roll no, or status...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear search")
                                }
                            }
                        },
                        singleLine = true,
                        shape = EduShapes.medium
                    )
                }
            }

            // 7. Student Roll Call List
            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(EduSpacing.xl),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (uiState.filteredEntries.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(EduSpacing.xl),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No students found matching current filters.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(uiState.filteredEntries, key = { it.studentId }) { entry ->
                    StudentRollCallCard(
                        entry = entry,
                        isReadOnly = uiState.isFinalized,
                        onStatusChanged = { status -> viewModel.updateStudentStatus(entry.studentId, status) },
                        onEditRemarks = { viewModel.openRemarksDialog(entry) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(EduSpacing.xxl))
            }
        }
    }

    // Modal Dialogs
    if (uiState.showAuditDialog) {
        AttendanceAuditDialog(
            auditTrail = uiState.activeRecord?.auditTrail ?: emptyList(),
            className = uiState.activeRecord?.className ?: "",
            sectionName = uiState.activeRecord?.sectionName ?: "",
            date = uiState.selectedDate,
            onDismiss = { viewModel.closeAuditDialog() }
        )
    }

    if (uiState.showRemarksDialog && uiState.selectedEntryForRemarks != null) {
        StudentRemarksDialog(
            student = uiState.selectedEntryForRemarks!!,
            initialRemarks = uiState.remarksInput,
            onRemarksChanged = { viewModel.onRemarksInputChanged(it) },
            onSave = { viewModel.saveRemarks() },
            onDismiss = { viewModel.closeRemarksDialog() }
        )
    }

    if (uiState.showReopenDialog) {
        ReopenAttendanceDialog(
            onConfirm = { reason -> viewModel.reopenRegister(reason) },
            onDismiss = { viewModel.closeReopenDialog() }
        )
    }
}

@Composable
private fun DateNavigatorCard(
    displayDate: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("date_navigator_card"),
        shape = EduShapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = EduElevation.level1)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = EduSpacing.sm, vertical = EduSpacing.xs),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onPrevious,
                modifier = Modifier.testTag("prev_date_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Previous Day",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = displayDate,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Daily Roll Call Session",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onNext,
                modifier = Modifier.testTag("next_date_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Next Day",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun ClassAndSectionFilters(
    classes: List<com.example.features.classes.domain.AcademicClass>,
    selectedClassId: String?,
    sections: List<com.example.features.classes.domain.ClassSection>,
    selectedSectionId: String?,
    onClassSelect: (String) -> Unit,
    onSectionSelect: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Academic Class",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(EduSpacing.xs)
        ) {
            classes.forEach { cls ->
                val isSelected = cls.id == selectedClassId
                FilterChip(
                    selected = isSelected,
                    onClick = { onClassSelect(cls.id) },
                    label = { Text(cls.name) },
                    modifier = Modifier.testTag("filter_class_${cls.id}"),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(EduSpacing.xs))

        Text(
            text = "Section",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(EduSpacing.xs)
        ) {
            sections.forEach { sec ->
                val isSelected = sec.id == selectedSectionId
                FilterChip(
                    selected = isSelected,
                    onClick = { onSectionSelect(sec.id) },
                    label = { Text(sec.name) },
                    modifier = Modifier.testTag("filter_section_${sec.id}"),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )
            }
        }
    }
}

@Composable
private fun FinalizedLockBanner(
    isFinalized: Boolean,
    hasEditPermission: Boolean,
    onUnlock: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("finalized_lock_banner"),
        shape = EduShapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(EduSpacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(EduSpacing.sm))
                Column {
                    Text(
                        text = "Register Finalized & Locked",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = "Entries are read-only. Unlocking creates an audit revision.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            if (hasEditPermission) {
                EduButton(
                    text = "Unlock",
                    variant = EduButtonVariant.OUTLINED,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.LockOpen,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    onClick = onUnlock,
                    testTag = "unlock_register_button"
                )
            }
        }
    }
}

@Composable
private fun BulkActionToolbar(
    isFinalized: Boolean,
    hasMarkPermission: Boolean,
    onMarkAllPresent: () -> Unit,
    onMarkAllAbsent: () -> Unit,
    onSaveDraft: () -> Unit,
    onFinalize: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bulk_action_toolbar"),
        shape = EduShapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = EduElevation.level1)
    ) {
        Column(modifier = Modifier.padding(EduSpacing.md)) {
            Text(
                text = "Register Actions",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(EduSpacing.xs))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(EduSpacing.xs)
            ) {
                EduButton(
                    text = "All Present",
                    variant = EduButtonVariant.OUTLINED,
                    enabled = !isFinalized && hasMarkPermission,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = EduSuccess
                        )
                    },
                    onClick = onMarkAllPresent,
                    modifier = Modifier.weight(1f),
                    testTag = "mark_all_present_button"
                )

                EduButton(
                    text = "All Absent",
                    variant = EduButtonVariant.OUTLINED,
                    enabled = !isFinalized && hasMarkPermission,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = EduError
                        )
                    },
                    onClick = onMarkAllAbsent,
                    modifier = Modifier.weight(1f),
                    testTag = "mark_all_absent_button"
                )

                if (!isFinalized && hasMarkPermission) {
                    EduButton(
                        text = "Finalize",
                        variant = EduButtonVariant.PRIMARY,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        onClick = onFinalize,
                        modifier = Modifier.weight(1.1f),
                        testTag = "finalize_register_button"
                    )
                }
            }
        }
    }
}

@Composable
private fun StudentRollCallCard(
    entry: StudentAttendanceEntry,
    isReadOnly: Boolean,
    onStatusChanged: (AttendanceStatus) -> Unit,
    onEditRemarks: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("student_card_${entry.studentId}"),
        shape = EduShapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = EduElevation.level1)
    ) {
        Column(modifier = Modifier.padding(EduSpacing.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        val initials = entry.studentName
                            .split(" ")
                            .filter { it.isNotBlank() }
                            .take(2)
                            .map { it.first().uppercaseChar() }
                            .joinToString("")
                        Text(
                            text = initials.ifBlank { "ST" },
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.width(EduSpacing.sm))

                    Column {
                        Text(
                            text = entry.studentName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Roll: ${entry.rollNumber}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 4-Pill Status Segmented Control
                StatusPillSelector(
                    selectedStatus = entry.status,
                    isReadOnly = isReadOnly,
                    onStatusSelect = onStatusChanged,
                    studentId = entry.studentId
                )
            }

            // Remarks row if present or editable
            Spacer(modifier = Modifier.height(EduSpacing.xs))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (entry.remarks.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notes,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = entry.remarks,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                if (!isReadOnly) {
                    IconButton(
                        onClick = onEditRemarks,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("remarks_button_${entry.studentId}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notes,
                            contentDescription = "Edit Remarks",
                            tint = if (entry.remarks.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusPillSelector(
    selectedStatus: AttendanceStatus,
    isReadOnly: Boolean,
    onStatusSelect: (AttendanceStatus) -> Unit,
    studentId: String
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        AttendanceStatus.entries.forEach { status ->
            val isSelected = status == selectedStatus
            val (activeBg, activeText) = when (status) {
                AttendanceStatus.PRESENT -> Pair(EduSuccess, Color.White)
                AttendanceStatus.ABSENT -> Pair(EduError, Color.White)
                AttendanceStatus.LATE -> Pair(Color(0xFFE65100), Color.White) // Amber 900
                AttendanceStatus.EXCUSED -> Pair(Color(0xFF1565C0), Color.White) // Blue 800
            }

            Box(
                modifier = Modifier
                    .size(width = 34.dp, height = 32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSelected) activeBg else Color.Transparent)
                    .then(
                        if (!isReadOnly) Modifier.clickable { onStatusSelect(status) }
                        else Modifier
                    )
                    .testTag("status_pill_${studentId}_${status.code}"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = status.code,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) activeText else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
