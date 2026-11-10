package com.example.features.teachers.presentation

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.security.Permission
import com.example.core.security.TenantContext
import com.example.core.theme.EduSpacing
import com.example.core.theme.EduSuccess
import com.example.core.theme.EduWarning
import com.example.features.teachers.domain.TeacherProfile
import com.example.features.teachers.domain.TeacherStatus
import com.example.shared.components.BadgeType
import com.example.shared.components.EduBadge
import com.example.shared.components.EduButton
import com.example.shared.components.EduButtonVariant
import com.example.shared.components.EduCard
import com.example.shared.components.EduStatCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherListScreen(
    viewModel: TeacherListViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val activeSchool = TenantContext.activeSchoolId.value ?: "Oxford Academy"

    LaunchedEffect(Unit) {
        viewModel.loadTeachersFromNetwork()
    }

    LaunchedEffect(uiState.feedbackMessage, uiState.errorMessage) {
        uiState.feedbackMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissFeedback()
        }
        uiState.errorMessage?.let { err ->
            snackbarHostState.showSnackbar("Error: $err")
            viewModel.dismissFeedback()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("teacher_list_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Faculty & Staff Directory",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$activeSchool • Subcollection: /teachers",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("nav_back_teachers_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (viewModel.hasPermission(Permission.TEACHERS_CREATE)) {
                        EduButton(
                            text = "Add Faculty",
                            onClick = { viewModel.openOnboardingDialog() },
                            leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) },
                            testTag = "add_faculty_top_bar_btn",
                            modifier = Modifier.padding(end = 8.dp)
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
            // KPI Stat Cards (2x2)
            item {
                Spacer(modifier = Modifier.height(EduSpacing.xs))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(EduSpacing.md)
                ) {
                    EduStatCard(
                        title = "Total Faculty",
                        value = uiState.totalFacultyCount.toString(),
                        icon = Icons.Default.Group,
                        trendText = "Permanent staff",
                        isPositiveTrend = true,
                        modifier = Modifier.weight(1f),
                        testTag = "kpi_total_faculty"
                    )
                    EduStatCard(
                        title = "Active Today",
                        value = uiState.activeFacultyCount.toString(),
                        icon = Icons.Default.Person,
                        iconTint = EduSuccess,
                        iconContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        trendText = "On Campus",
                        isPositiveTrend = true,
                        modifier = Modifier.weight(1f),
                        testTag = "kpi_active_faculty"
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(EduSpacing.md)
                ) {
                    EduStatCard(
                        title = "Class In-Charges",
                        value = uiState.classTeachersCount.toString(),
                        icon = Icons.Default.AssignmentInd,
                        iconTint = MaterialTheme.colorScheme.secondary,
                        iconContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        trendText = "Assigned",
                        isPositiveTrend = true,
                        modifier = Modifier.weight(1f),
                        testTag = "kpi_class_teachers"
                    )
                    EduStatCard(
                        title = "Departments",
                        value = uiState.departmentsCount.toString(),
                        icon = Icons.Default.Domain,
                        iconTint = EduWarning,
                        iconContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        trendText = "Academic wings",
                        isPositiveTrend = true,
                        modifier = Modifier.weight(1f),
                        testTag = "kpi_departments"
                    )
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    placeholder = { Text("Search by name, ID, subject, or department...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("teacher_search_input"),
                    singleLine = true
                )
            }

            // Department Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = uiState.selectedDepartment == null,
                        onClick = { viewModel.onDepartmentFilterChange(null) },
                        label = { Text("All Wings") },
                        colors = FilterChipDefaults.filterChipColors(),
                        modifier = Modifier.testTag("filter_dept_all")
                    )

                    uiState.departmentsList.forEach { dept ->
                        FilterChip(
                            selected = uiState.selectedDepartment == dept,
                            onClick = { viewModel.onDepartmentFilterChange(dept) },
                            label = { Text(dept) },
                            modifier = Modifier.testTag("filter_dept_${dept.replace(" ", "_")}")
                        )
                    }
                }
            }

            // Status Filter & Sort Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        TeacherStatus.entries.forEach { status ->
                            FilterChip(
                                selected = uiState.selectedStatus == status,
                                onClick = { viewModel.onStatusFilterChange(status) },
                                label = { Text(status.displayName) },
                                modifier = Modifier.testTag("filter_status_${status.name}")
                            )
                        }
                    }

                    if (uiState.searchQuery.isNotEmpty() || uiState.selectedDepartment != null || uiState.selectedStatus != null) {
                        TextButton(
                            onClick = { viewModel.clearFilters() },
                            modifier = Modifier.testTag("clear_filters_btn")
                        ) {
                            Text("Clear", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // Section Header & Counter
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Faculty Members (${uiState.filteredTeachers.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Sorted by ${uiState.sortOption.displayName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Empty State
            if (uiState.filteredTeachers.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = EduSpacing.xl)
                            .testTag("teachers_empty_state"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(EduSpacing.sm))
                            Text(
                                text = "No faculty records found",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Try adjusting your search criteria or register a new faculty member.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Faculty Cards List
            items(uiState.filteredTeachers, key = { it.id }) { teacher ->
                TeacherCard(
                    teacher = teacher,
                    onViewDetail = { viewModel.selectTeacherForDetail(teacher) },
                    onArchive = { viewModel.promptArchiveTeacher(teacher) },
                    canArchive = viewModel.hasPermission(Permission.TEACHERS_DELETE)
                )
            }

            item {
                Spacer(modifier = Modifier.height(EduSpacing.xl))
            }
        }
    }

    // Detail Dialog
    uiState.selectedTeacherForDetail?.let { teacher ->
        TeacherDetailDialog(
            teacher = teacher,
            onDismiss = { viewModel.clearSelectedTeacher() },
            onArchive = {
                viewModel.clearSelectedTeacher()
                viewModel.promptArchiveTeacher(teacher)
            },
            canArchive = viewModel.hasPermission(Permission.TEACHERS_DELETE)
        )
    }

    // Onboarding Dialog
    if (uiState.showOnboardingDialog) {
        val onboardingViewModel: TeacherOnboardingViewModel = viewModel()
        TeacherOnboardingDialog(
            viewModel = onboardingViewModel,
            onDismiss = { viewModel.closeOnboardingDialog() },
            onSuccess = { newTeacher ->
                viewModel.onTeacherOnboarded(newTeacher)
            }
        )
    }

    // Archive Confirmation Dialog
    uiState.teacherToArchive?.let { teacher ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissArchiveDialog() },
            title = { Text("Archive Faculty Member") },
            text = {
                Text("Are you sure you want to archive ${teacher.fullName} (${teacher.employeeId}) from ${teacher.department}? This will revoke their teaching allocation.")
            },
            confirmButton = {
                EduButton(
                    text = "Archive",
                    onClick = { viewModel.confirmArchiveTeacher() },
                    variant = EduButtonVariant.DANGER,
                    testTag = "confirm_archive_dialog_btn"
                )
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.dismissArchiveDialog() },
                    modifier = Modifier.testTag("cancel_archive_dialog_btn")
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun TeacherCard(
    teacher: TeacherProfile,
    onViewDetail: () -> Unit,
    onArchive: () -> Unit,
    canArchive: Boolean
) {
    EduCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewDetail() }
            .testTag("teacher_card_${teacher.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar Initials
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    val initials = "${teacher.firstName.take(1)}${teacher.lastName.take(1)}".uppercase()
                    Text(
                        text = initials,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(EduSpacing.md))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = teacher.fullName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        val statusBadge = when (teacher.status) {
                            TeacherStatus.ACTIVE -> BadgeType.SUCCESS
                            TeacherStatus.ON_LEAVE -> BadgeType.WARNING
                            TeacherStatus.RESIGNED, TeacherStatus.RETIRED -> BadgeType.ERROR
                        }
                        EduBadge(text = teacher.status.displayName, type = statusBadge)
                    }

                    Text(
                        text = "${teacher.designation} • ${teacher.department}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(EduSpacing.sm))

            // Sub-badges row: Employee ID, Primary Subject, Class In-Charge, Experience
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                EduBadge(text = teacher.employeeId, type = BadgeType.PRIMARY)
                EduBadge(text = teacher.primarySubject, type = BadgeType.INFO)
                if (teacher.isClassTeacher) {
                    EduBadge(
                        text = "Class: ${teacher.assignedClassId ?: ""}-${teacher.assignedSectionId ?: ""}",
                        type = BadgeType.SUCCESS
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${teacher.experienceYears} yrs exp",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(EduSpacing.sm))

            // Quick Actions Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = teacher.email,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onViewDetail,
                        modifier = Modifier.size(32.dp).testTag("view_teacher_${teacher.id}")
                    ) {
                        Icon(
                            Icons.Default.Visibility,
                            contentDescription = "View Profile",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (canArchive) {
                        IconButton(
                            onClick = onArchive,
                            modifier = Modifier.size(32.dp).testTag("archive_teacher_${teacher.id}")
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Archive Teacher",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
