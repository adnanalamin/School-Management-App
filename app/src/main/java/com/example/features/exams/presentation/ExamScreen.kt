package com.example.features.exams.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
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
import com.example.features.exams.domain.ExamPaper
import com.example.features.exams.domain.ExamSchedule
import com.example.features.exams.domain.ExamStatus
import com.example.features.exams.domain.ExamType
import com.example.shared.components.EduButton
import com.example.shared.components.EduButtonVariant
import com.example.shared.components.EduStatCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamScreen(
    viewModel: ExamViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val hasManagePermission = TenantContext.hasPermission(Permission.EXAMS_MANAGE)

    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissMessages()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissMessages()
        }
    }

    Scaffold(
        modifier = modifier.testTag("exam_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Examinations & Timetable",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tenant: ${(TenantContext.activeSchoolId.value ?: "").take(15)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("exam_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (hasManagePermission) {
                        EduButton(
                            text = "+ Schedule Exam",
                            onClick = { viewModel.openCreateScheduleDialog() },
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .testTag("schedule_exam_button")
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
                .padding(horizontal = EduSpacing.md)
        ) {
            Spacer(modifier = Modifier.height(EduSpacing.sm))

            // 4 Stat Cards Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(EduSpacing.sm)
            ) {
                EduStatCard(
                    title = "Total Series",
                    value = uiState.totalSchedules.toString(),
                    subtitle = "All cohorts",
                    icon = Icons.Default.DateRange,
                    modifier = Modifier.weight(1f)
                )
                EduStatCard(
                    title = "Scheduled",
                    value = uiState.scheduledSeriesCount.toString(),
                    subtitle = "Upcoming",
                    icon = Icons.Default.EventNote,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(EduSpacing.sm))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(EduSpacing.sm)
            ) {
                EduStatCard(
                    title = "Papers Listed",
                    value = uiState.totalPapers.toString(),
                    subtitle = "In timetable",
                    icon = Icons.AutoMirrored.Filled.Notes,
                    modifier = Modifier.weight(1f)
                )
                EduStatCard(
                    title = "Active Halls",
                    value = uiState.activeRoomsCount.toString(),
                    subtitle = "Distinct rooms",
                    icon = Icons.Default.MeetingRoom,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(EduSpacing.md))

            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("exam_search_input"),
                placeholder = { Text("Search by title, subject, cohort, or hall...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = EduShapes.medium
            )

            Spacer(modifier = Modifier.height(EduSpacing.sm))

            // Type Filter Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = uiState.selectedTypeFilter == null,
                    onClick = { viewModel.setTypeFilter(null) },
                    label = { Text("All Types") },
                    colors = FilterChipDefaults.filterChipColors(),
                    modifier = Modifier.testTag("exam_chip_all")
                )
                ExamType.entries.forEach { type ->
                    FilterChip(
                        selected = uiState.selectedTypeFilter == type,
                        onClick = { viewModel.setTypeFilter(if (uiState.selectedTypeFilter == type) null else type) },
                        label = { Text(type.displayName) },
                        colors = FilterChipDefaults.filterChipColors(),
                        modifier = Modifier.testTag("exam_chip_${type.name.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(EduSpacing.md))

            // Content List
            if (uiState.isLoading && uiState.schedules.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.filteredSchedules.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "No Examination Schedules Found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (uiState.searchQuery.isNotEmpty()) "Try adjusting your search criteria" else "Click '+ Schedule Exam' to build a timetable",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(EduSpacing.md)
                ) {
                    items(
                        items = uiState.filteredSchedules,
                        key = { it.id }
                    ) { schedule ->
                        ExamScheduleCard(
                            schedule = schedule,
                            hasManagePermission = hasManagePermission,
                            onAddPaper = { viewModel.openAddPaperDialog(schedule) },
                            onGenerateHallTicket = { viewModel.generateStudentHallTicket(schedule) },
                            onDelete = { viewModel.deleteSchedule(schedule.id) }
                        )
                    }
                }
            }
        }
    }

    // Dialogs
    if (uiState.showCreateScheduleDialog) {
        CreateExamScheduleDialog(
            onDismiss = { viewModel.closeCreateScheduleDialog() },
            onConfirm = { title, examType, academicSession, gradeLevel, startDate, endDate ->
                viewModel.createExamSchedule(
                    title = title,
                    examType = examType,
                    academicSession = academicSession,
                    gradeLevel = gradeLevel,
                    startDate = startDate,
                    endDate = endDate
                )
            }
        )
    }

    if (uiState.showAddPaperDialog && uiState.selectedScheduleForPaper != null) {
        AddExamPaperDialog(
            schedule = uiState.selectedScheduleForPaper!!,
            conflictWarning = uiState.conflictWarning,
            onDismiss = { viewModel.closeAddPaperDialog() },
            onValidateConflict = { date, start, end, room ->
                viewModel.validatePaperConflict(
                    existingPapers = uiState.selectedScheduleForPaper!!.papers,
                    date = date,
                    startTime = start,
                    endTime = end,
                    room = room
                )
            },
            onConfirm = { subjectName, subjectCode, examDate, startTime, endTime, duration, totalMarks, passMarks, room, invigilator, instructions ->
                viewModel.addPaperToSchedule(
                    scheduleId = uiState.selectedScheduleForPaper!!.id,
                    subjectName = subjectName,
                    subjectCode = subjectCode,
                    examDate = examDate,
                    startTime = startTime,
                    endTime = endTime,
                    durationMinutes = duration,
                    totalMarks = totalMarks,
                    passingMarks = passMarks,
                    roomNumber = room,
                    invigilatorName = invigilator,
                    instructions = instructions
                )
            }
        )
    }

    if (uiState.showHallTicketDialog && uiState.generatedHallTicket != null) {
        HallTicketDialog(
            ticket = uiState.generatedHallTicket!!,
            onDismiss = { viewModel.closeHallTicketDialog() }
        )
    }
}

@Composable
fun ExamScheduleCard(
    schedule: ExamSchedule,
    hasManagePermission: Boolean,
    onAddPaper: () -> Unit,
    onGenerateHallTicket: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val statusColor = when (schedule.status) {
        ExamStatus.SCHEDULED -> MaterialTheme.colorScheme.primary
        ExamStatus.ONGOING -> Color(0xFFF57C00) // Amber
        ExamStatus.COMPLETED -> EduSuccess
        ExamStatus.CANCELLED -> EduError
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("exam_schedule_card_${schedule.id}"),
        shape = EduShapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = EduElevation.level2)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Title & Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = schedule.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = schedule.gradeLevel,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = schedule.academicSession,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Type & Status Chips
                Column(horizontalAlignment = Alignment.End) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(statusColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = schedule.status.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = schedule.examType.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Date Range & Papers Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${schedule.startDate} to ${schedule.endDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Notes,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${schedule.papers.size} Papers Scheduled",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            // Action Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EduButton(
                        text = "Hall Ticket",
                        onClick = onGenerateHallTicket,
                        variant = EduButtonVariant.OUTLINED,
                        leadingIcon = { Icon(Icons.Default.ConfirmationNumber, contentDescription = null) },
                        modifier = Modifier.testTag("hall_ticket_button_${schedule.id}")
                    )

                    if (hasManagePermission) {
                        EduButton(
                            text = "+ Paper",
                            onClick = onAddPaper,
                            variant = EduButtonVariant.SECONDARY,
                            leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) },
                            modifier = Modifier.testTag("add_paper_button_${schedule.id}")
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (hasManagePermission) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.testTag("delete_exam_button_${schedule.id}")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Exam", tint = EduError)
                        }
                    }

                    IconButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.testTag("expand_papers_button_${schedule.id}")
                    ) {
                        Icon(
                            if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (expanded) "Collapse Timetable" else "Expand Timetable"
                        )
                    }
                }
            }

            // Expandable Timetable Papers
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "EXAMINATION TIMETABLE PAPERS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (schedule.papers.isEmpty()) {
                        Text(
                            text = "No papers added yet. Click '+ Paper' above to schedule subjects.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        schedule.papers.forEach { paper ->
                            PaperItemRow(paper = paper)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaperItemRow(paper: ExamPaper) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = EduShapes.small,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = paper.subjectName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "(${paper.subjectCode})",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = "${paper.totalMarks} Marks",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${paper.examDate} | ${paper.startTime} - ${paper.endTime}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.MeetingRoom,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = paper.roomNumber,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (paper.invigilatorName.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Invigilator: ${paper.invigilatorName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
