package com.example.features.students.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.security.Permission
import com.example.core.security.TenantContext
import com.example.core.theme.EduShapes
import com.example.core.theme.EduSpacing
import com.example.features.students.domain.StudentProfile
import com.example.features.students.domain.StudentStatus
import com.example.shared.components.BadgeType
import com.example.shared.components.EduBadge
import com.example.shared.components.EduButton
import com.example.shared.components.EduButtonVariant
import com.example.shared.components.EduCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentListScreen(
    viewModel: StudentListViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadStudents(context)
    }

    LaunchedEffect(uiState.notificationFeedback) {
        uiState.notificationFeedback?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissFeedback()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("student_list_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Student Management (SIS)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${uiState.filteredStudents.size} enrolled students listed",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("btn_students_back")) {
                        Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (TenantContext.hasPermission(Permission.STUDENTS_CREATE)) {
                        IconButton(
                            onClick = { viewModel.openEnrollmentWizard() },
                            modifier = Modifier.testTag("btn_top_enroll_student")
                        ) {
                            Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "Enroll Student", tint = MaterialTheme.colorScheme.primary)
                        }
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
            // SIS KPI Executive Banner
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(EduSpacing.sm)
                ) {
                    MetricBox(
                        title = "Total Enrolled",
                        value = "${uiState.totalStudentsCount}",
                        modifier = Modifier.weight(1f),
                        testTag = "kpi_total_students"
                    )
                    MetricBox(
                        title = "Active Roster",
                        value = "${uiState.activeCount}",
                        badge = "ACTIVE",
                        modifier = Modifier.weight(1f),
                        testTag = "kpi_active_students"
                    )
                    MetricBox(
                        title = "Gender Split",
                        value = "${uiState.femaleCount}F • ${uiState.maleCount}M",
                        modifier = Modifier.weight(1.2f),
                        testTag = "kpi_gender_split"
                    )
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    modifier = Modifier.fillMaxWidth().testTag("input_search_students"),
                    placeholder = { Text("Search by name, roll, guardian, or phone...") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    shape = EduShapes.medium,
                    singleLine = true
                )
            }

            // Filter Chips Carousel: Grades
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "Grade Level Filter", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(EduSpacing.xs)
                    ) {
                        uiState.availableGrades.forEach { grade ->
                            FilterChip(
                                selected = uiState.selectedGrade == grade,
                                onClick = { viewModel.onGradeFilterChange(grade) },
                                label = { Text(grade) },
                                modifier = Modifier.testTag("chip_filter_grade_$grade")
                            )
                        }
                    }
                }
            }

            // Filter Chips Carousel: Section & Status
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(EduSpacing.xs)
                ) {
                    uiState.availableSections.forEach { sec ->
                        FilterChip(
                            selected = uiState.selectedSection == sec,
                            onClick = { viewModel.onSectionFilterChange(sec) },
                            label = { Text(sec) },
                            modifier = Modifier.testTag("chip_filter_sec_$sec")
                        )
                    }
                    uiState.availableStatuses.forEach { st ->
                        FilterChip(
                            selected = uiState.selectedStatus == st,
                            onClick = { viewModel.onStatusFilterChange(st) },
                            label = { Text(st) },
                            modifier = Modifier.testTag("chip_filter_status_$st")
                        )
                    }
                }
            }

            // Student Records List
            if (uiState.filteredStudents.isEmpty()) {
                item {
                    EduCard(testTag = "card_no_students") {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(EduSpacing.lg),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(EduSpacing.xs)
                        ) {
                            Icon(imageVector = Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                            Text(text = "No students match your criteria", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(text = "Try adjusting your search query, grade level, or section filters.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            EduButton(
                                text = "Reset Filters",
                                onClick = { viewModel.clearFilters() },
                                variant = EduButtonVariant.OUTLINED,
                                testTag = "btn_reset_filters"
                            )
                        }
                    }
                }
            } else {
                items(uiState.filteredStudents, key = { it.id }) { student ->
                    StudentCardItem(
                        student = student,
                        onClick = { viewModel.selectStudentForDetail(student) },
                        onDeleteClick = { viewModel.promptDeleteStudent(student) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(EduSpacing.xl))
            }
        }
    }

    // 360-Degree Student Profile Detail Dialog
    uiState.selectedStudentDetail?.let { student ->
        StudentDetailDialog(
            student = student,
            onDismiss = { viewModel.closeStudentDetail() },
            onDelete = {
                viewModel.closeStudentDetail()
                viewModel.promptDeleteStudent(student)
            }
        )
    }

    // Delete Confirmation Dialog
    if (uiState.showDeleteConfirmDialog && uiState.studentToDelete != null) {
        val target = uiState.studentToDelete!!
        AlertDialog(
            onDismissRequest = { viewModel.cancelDelete() },
            icon = { Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text(text = "Archive Student Record?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Are you sure you want to remove ${target.fullName} (${target.rollNumber})? This operation will remove the student from active grade lists and attendance tracking.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                EduButton(
                    text = "Confirm Archive",
                    onClick = { viewModel.confirmDeleteStudent(context) },
                    variant = EduButtonVariant.DANGER,
                    testTag = "btn_confirm_delete_student"
                )
            },
            dismissButton = {
                EduButton(
                    text = "Cancel",
                    onClick = { viewModel.cancelDelete() },
                    variant = EduButtonVariant.OUTLINED,
                    testTag = "btn_cancel_delete_student"
                )
            },
            shape = EduShapes.large,
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // Enrollment Wizard Dialog
    if (uiState.showEnrollmentWizard) {
        StudentEnrollmentDialog(
            onDismiss = { viewModel.closeEnrollmentWizard() },
            onStudentEnrolled = { newStudent ->
                viewModel.onStudentEnrolled(newStudent)
            }
        )
    }
}

@Composable
private fun MetricBox(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    badge: String? = null,
    testTag: String
) {
    EduCard(modifier = modifier, testTag = testTag) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (badge != null) {
                    EduBadge(text = badge, type = BadgeType.SUCCESS)
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun StudentCardItem(
    student: StudentProfile,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    EduCard(
        modifier = Modifier.clickable { onClick() },
        testTag = "card_student_${student.id}"
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Initials Avatar
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = student.fullName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.size(EduSpacing.sm))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = student.fullName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    EduBadge(
                        text = student.status.displayName,
                        type = when (student.status) {
                            StudentStatus.ACTIVE -> BadgeType.SUCCESS
                            StudentStatus.INACTIVE -> BadgeType.WARNING
                            StudentStatus.SUSPENDED -> BadgeType.ERROR
                            else -> BadgeType.PRIMARY
                        }
                    )
                }

                Text(
                    text = "Roll: ${student.rollNumber} • ${student.gradeLevel} (${student.section})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = "${student.guardianName} (${student.guardianPhone})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (TenantContext.hasPermission(Permission.STUDENTS_DELETE)) {
                IconButton(onClick = onDeleteClick, modifier = Modifier.testTag("btn_delete_${student.id}")) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Archive student", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun StudentDetailDialog(
    student: StudentProfile,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = student.fullName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.size(EduSpacing.xs))
                    Column {
                        Text(text = student.fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(text = student.admissionNumber, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                EduBadge(text = student.status.displayName, type = BadgeType.SUCCESS)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(EduSpacing.xs)
            ) {
                Text(text = "Academic & School Roster", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                DetailRow("Grade & Section", "${student.gradeLevel} - ${student.section}")
                DetailRow("Roll Number", student.rollNumber)
                DetailRow("Admission Code", student.admissionNumber)
                DetailRow("Enrollment Date", student.enrollmentDate)

                Spacer(modifier = Modifier.height(EduSpacing.xs))
                Text(text = "Personal & Guardian Information", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                DetailRow("Date of Birth", student.dateOfBirth)
                DetailRow("Gender / Blood", "${student.gender} • ${student.bloodGroup}")
                DetailRow("Guardian Name", "${student.guardianName} (${student.guardianRelationship})")
                DetailRow("Guardian Phone", student.guardianPhone)
                DetailRow("Guardian Email", student.guardianEmail)
                DetailRow("Address", student.address)

                Spacer(modifier = Modifier.height(EduSpacing.xs))
                Text(text = "Medical & Emergency Alerts", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f), shape = EduShapes.small)
                        .padding(EduSpacing.sm)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.MedicalServices, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.size(4.dp))
                            Text(text = "Emergency Telephone: ${student.emergencyContact}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = student.medicalNotes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }
        },
        confirmButton = {
            EduButton(
                text = "Close Profile",
                onClick = onDismiss,
                variant = EduButtonVariant.PRIMARY,
                testTag = "btn_close_student_detail"
            )
        },
        dismissButton = {
            if (TenantContext.hasPermission(Permission.STUDENTS_DELETE)) {
                EduButton(
                    text = "Archive Record",
                    onClick = onDelete,
                    variant = EduButtonVariant.DANGER,
                    testTag = "btn_delete_from_detail"
                )
            }
        },
        shape = EduShapes.large,
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
    }
}
