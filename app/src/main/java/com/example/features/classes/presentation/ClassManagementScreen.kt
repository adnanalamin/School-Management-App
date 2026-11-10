package com.example.features.classes.presentation

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
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.School
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.security.Permission
import com.example.core.security.TenantContext
import com.example.core.theme.EduSpacing
import com.example.core.theme.EduSuccess
import com.example.core.theme.EduWarning
import com.example.features.classes.domain.AcademicClass
import com.example.features.classes.domain.ClassSection
import com.example.features.classes.domain.SubjectAllocation
import com.example.shared.components.BadgeType
import com.example.shared.components.EduBadge
import com.example.shared.components.EduButton
import com.example.shared.components.EduButtonVariant
import com.example.shared.components.EduCard
import com.example.shared.components.EduStatCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassManagementScreen(
    viewModel: ClassManagementViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val activeSchool = TenantContext.activeSchoolId.value ?: "Oxford Academy"

    LaunchedEffect(Unit) {
        viewModel.loadAcademicDataFromNetwork()
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
        modifier = modifier.fillMaxSize().testTag("class_management_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Academic Structure & Routine",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$activeSchool • Subcollections: /classes & /sections",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("nav_back_classes_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (viewModel.hasPermission(Permission.CLASSES_MANAGE)) {
                        Row {
                            when (uiState.selectedTab) {
                                ClassManagementTab.CLASSES -> {
                                    EduButton(
                                        text = "Add Class",
                                        onClick = { viewModel.openCreateClassDialog() },
                                        leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) },
                                        testTag = "btn_topbar_add_class",
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                }
                                ClassManagementTab.SECTIONS -> {
                                    EduButton(
                                        text = "Add Section",
                                        onClick = { viewModel.openCreateSectionDialog() },
                                        leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) },
                                        testTag = "btn_topbar_add_section",
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                }
                                ClassManagementTab.SUBJECTS -> {
                                    EduButton(
                                        text = "Allocate",
                                        onClick = { viewModel.openAllocateSubjectDialog() },
                                        leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) },
                                        testTag = "btn_topbar_allocate_subject",
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                }
                            }
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
            // Executive KPI Summary Cards
            item {
                Spacer(modifier = Modifier.height(EduSpacing.xs))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(EduSpacing.md)
                ) {
                    EduStatCard(
                        title = "Classes / Grades",
                        value = uiState.totalClassesCount.toString(),
                        icon = Icons.Default.School,
                        trendText = "Academic Levels",
                        isPositiveTrend = true,
                        modifier = Modifier.weight(1f),
                        testTag = "kpi_classes_count"
                    )
                    EduStatCard(
                        title = "Sections",
                        value = uiState.totalSectionsCount.toString(),
                        icon = Icons.Default.MeetingRoom,
                        iconTint = MaterialTheme.colorScheme.secondary,
                        iconContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        trendText = "Classrooms",
                        isPositiveTrend = true,
                        modifier = Modifier.weight(1f),
                        testTag = "kpi_sections_count"
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(EduSpacing.md)
                ) {
                    EduStatCard(
                        title = "Allocations",
                        value = uiState.totalAllocationsCount.toString(),
                        icon = Icons.Default.Book,
                        iconTint = EduSuccess,
                        iconContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        trendText = "Weekly Workloads",
                        isPositiveTrend = true,
                        modifier = Modifier.weight(1f),
                        testTag = "kpi_allocations_count"
                    )
                    EduStatCard(
                        title = "Total Enrolled",
                        value = uiState.totalEnrolledStudents.toString(),
                        icon = Icons.Default.AssignmentInd,
                        iconTint = EduWarning,
                        iconContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        trendText = "${uiState.classTeachersAssignedCount} In-Charges",
                        isPositiveTrend = true,
                        modifier = Modifier.weight(1f),
                        testTag = "kpi_total_enrolled"
                    )
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    placeholder = { Text("Search classes, sections, rooms, or subjects...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("classes_search_input"),
                    singleLine = true
                )
            }

            // Filter Chips by Class
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = uiState.selectedClassFilter == null,
                        onClick = { viewModel.onClassFilterChange(null) },
                        label = { Text("All Grades") },
                        modifier = Modifier.testTag("chip_grade_all")
                    )
                    uiState.classes.forEach { cls ->
                        FilterChip(
                            selected = uiState.selectedClassFilter == cls.id,
                            onClick = { viewModel.onClassFilterChange(cls.id) },
                            label = { Text("${cls.name} (${cls.code})") },
                            modifier = Modifier.testTag("chip_grade_${cls.code}")
                        )
                    }
                    if (uiState.searchQuery.isNotEmpty() || uiState.selectedClassFilter != null) {
                        TextButton(onClick = { viewModel.clearFilters() }) {
                            Text("Clear", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // Navigation Tab Row
            item {
                TabRow(
                    selectedTabIndex = uiState.selectedTab.ordinal,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    ClassManagementTab.entries.forEach { tab ->
                        Tab(
                            selected = uiState.selectedTab == tab,
                            onClick = { viewModel.selectTab(tab) },
                            text = {
                                Text(
                                    text = tab.title,
                                    fontWeight = if (uiState.selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.testTag("tab_${tab.name}")
                        )
                    }
                }
            }

            // Tab Content
            when (uiState.selectedTab) {
                ClassManagementTab.CLASSES -> {
                    if (uiState.filteredClasses.isEmpty()) {
                        item { EmptyStateBox("No academic classes found matching criteria.") }
                    } else {
                        items(uiState.filteredClasses, key = { it.id }) { cls ->
                            AcademicClassCard(
                                academicClass = cls,
                                onAddSection = { viewModel.openCreateSectionDialog(cls.id) },
                                onDelete = { viewModel.promptDeleteClass(cls) },
                                canManage = viewModel.hasPermission(Permission.CLASSES_MANAGE)
                            )
                        }
                    }
                }

                ClassManagementTab.SECTIONS -> {
                    if (uiState.filteredSections.isEmpty()) {
                        item { EmptyStateBox("No class sections found matching criteria.") }
                    } else {
                        items(uiState.filteredSections, key = { it.id }) { sec ->
                            ClassSectionCard(
                                section = sec,
                                onInspect = { viewModel.inspectSection(sec) },
                                onDelete = { viewModel.promptDeleteSection(sec) },
                                canManage = viewModel.hasPermission(Permission.CLASSES_MANAGE)
                            )
                        }
                    }
                }

                ClassManagementTab.SUBJECTS -> {
                    if (uiState.filteredAllocations.isEmpty()) {
                        item { EmptyStateBox("No subject allocations found matching criteria.") }
                    } else {
                        items(uiState.filteredAllocations, key = { it.id }) { alloc ->
                            SubjectAllocationCard(allocation = alloc)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(EduSpacing.xl))
            }
        }
    }

    // Dialogs
    if (uiState.showCreateClassDialog) {
        CreateClassDialog(
            onDismiss = { viewModel.closeCreateClassDialog() },
            onConfirm = { name, code, gradeNum, stream ->
                viewModel.createClass(name, code, gradeNum, stream)
            }
        )
    }

    if (uiState.showCreateSectionDialog) {
        CreateSectionDialog(
            classes = uiState.classes,
            preSelectedClassId = uiState.selectedClassFilter,
            onDismiss = { viewModel.closeCreateSectionDialog() },
            onConfirm = { classId, sectionName, roomNumber, capacity, teacherId, teacherName ->
                viewModel.createSection(classId, sectionName, roomNumber, capacity, teacherId, teacherName)
            }
        )
    }

    if (uiState.showAllocateSubjectDialog) {
        AllocateSubjectDialog(
            sections = uiState.sections,
            onDismiss = { viewModel.closeAllocateSubjectDialog() },
            onConfirm = { sectionId, subjectName, subjectCode, teacherId, teacherName, periodsPerWeek, isElective, roomNumber ->
                viewModel.createSubjectAllocation(sectionId, subjectName, subjectCode, teacherId, teacherName, periodsPerWeek, isElective, roomNumber)
            }
        )
    }

    uiState.selectedSectionForInspection?.let { sec ->
        val allocs = uiState.subjectAllocations.filter { it.sectionId == sec.id }
        SectionInspectionDialog(
            section = sec,
            allocations = allocs,
            onDismiss = { viewModel.closeSectionInspection() }
        )
    }

    uiState.classToDelete?.let { cls ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteDialog() },
            title = { Text("Delete Academic Class") },
            text = { Text("Are you sure you want to remove ${cls.name} (${cls.code})? All linked sections will be orphaned.") },
            confirmButton = {
                EduButton(
                    text = "Delete",
                    onClick = { viewModel.confirmDeleteClass() },
                    variant = EduButtonVariant.DANGER,
                    testTag = "confirm_delete_class_btn"
                )
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    uiState.sectionToDelete?.let { sec ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteDialog() },
            title = { Text("Delete Class Section") },
            text = { Text("Are you sure you want to remove section ${sec.displayName} located in ${sec.roomNumber}?") },
            confirmButton = {
                EduButton(
                    text = "Delete",
                    onClick = { viewModel.confirmDeleteSection() },
                    variant = EduButtonVariant.DANGER,
                    testTag = "confirm_delete_section_btn"
                )
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun AcademicClassCard(
    academicClass: AcademicClass,
    onAddSection: () -> Unit,
    onDelete: () -> Unit,
    canManage: Boolean
) {
    EduCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("class_card_${academicClass.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = academicClass.code,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = academicClass.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${academicClass.stream} • Level ${academicClass.numericGrade}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                EduBadge(text = "${academicClass.sectionsCount} Sec", type = BadgeType.PRIMARY)
                EduBadge(text = "${academicClass.totalStudents} Students", type = BadgeType.INFO)
            }
        }

        if (canManage) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onAddSection,
                    modifier = Modifier.testTag("add_sec_to_${academicClass.id}")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Section")
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp).testTag("delete_class_${academicClass.id}")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun ClassSectionCard(
    section: ClassSection,
    onInspect: () -> Unit,
    onDelete: () -> Unit,
    canManage: Boolean
) {
    EduCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onInspect() }
            .testTag("section_card_${section.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = section.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = section.roomNumber,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                EduBadge(
                    text = "${section.currentEnrollment}/${section.capacity} Enrolled",
                    type = if (section.occupancyPercentage > 90f) BadgeType.WARNING else BadgeType.SUCCESS
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Capacity Bar
            LinearProgressIndicator(
                progress = { section.occupancyPercentage / 100f },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = if (section.occupancyPercentage > 90f) EduWarning else EduSuccess,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AssignmentInd,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = section.classTeacherName ?: "No Teacher Assigned",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onInspect,
                        modifier = Modifier.size(32.dp).testTag("inspect_section_${section.id}")
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = "Inspect", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }
                    if (canManage) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(32.dp).testTag("delete_section_${section.id}")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubjectAllocationCard(allocation: SubjectAllocation) {
    EduCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("allocation_card_${allocation.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = allocation.subjectName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    EduBadge(text = allocation.subjectCode, type = BadgeType.PRIMARY)
                    if (allocation.isElective) {
                        Spacer(modifier = Modifier.width(4.dp))
                        EduBadge(text = "Elective", type = BadgeType.WARNING)
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "${allocation.sectionName} • ${allocation.roomNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Instructor: ${allocation.teacherName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            EduBadge(text = "${allocation.periodsPerWeek} periods/wk", type = BadgeType.INFO)
        }
    }
}

@Composable
private fun EmptyStateBox(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = EduSpacing.xl)
            .testTag("classes_empty_state"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
