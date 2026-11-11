package com.example.features.exams.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.EduError
import com.example.core.theme.EduErrorContainer
import com.example.core.theme.EduShapes
import com.example.core.theme.EduSpacing
import com.example.core.theme.EduSuccess
import com.example.core.theme.EduSuccessContainer
import com.example.features.exams.domain.ExamPaper
import com.example.features.exams.domain.ExamSchedule
import com.example.features.exams.domain.ExamType
import com.example.features.exams.domain.HallTicket
import com.example.shared.components.EduButton
import com.example.shared.components.EduButtonVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateExamScheduleDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        examType: ExamType,
        academicSession: String,
        gradeLevel: String,
        startDate: String,
        endDate: String
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(ExamType.MIDTERM) }
    var typeExpanded by remember { mutableStateOf(false) }
    var academicSession by remember { mutableStateOf("2026-2027") }
    var gradeLevel by remember { mutableStateOf("Grade 10") }
    var startDate by remember { mutableStateOf("2026-05-04") }
    var endDate by remember { mutableStateOf("2026-05-08") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val gradeOptions = listOf("Grade 9", "Grade 10", "Grade 11", "Grade 12", "Year 9", "Year 10", "Year 11")
    var gradeExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    text = "Schedule Examination Series",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (validationError != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = EduErrorContainer),
                        shape = EduShapes.small
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = EduError)
                            Text(text = validationError ?: "", color = EduError, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Series Title *") },
                    placeholder = { Text("e.g. Midterm Assessment Series 2026") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("exam_title_input"),
                    singleLine = true
                )

                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedType.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Examination Type *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                            .testTag("exam_type_dropdown")
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        ExamType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.displayName) },
                                onClick = {
                                    selectedType = type
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = gradeExpanded,
                    onExpandedChange = { gradeExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = gradeLevel,
                        onValueChange = { gradeLevel = it },
                        label = { Text("Grade / Cohort *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = gradeExpanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryEditable)
                            .fillMaxWidth()
                            .testTag("exam_grade_dropdown")
                    )
                    ExposedDropdownMenu(
                        expanded = gradeExpanded,
                        onDismissRequest = { gradeExpanded = false }
                    ) {
                        gradeOptions.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt) },
                                onClick = {
                                    gradeLevel = opt
                                    gradeExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = academicSession,
                    onValueChange = { academicSession = it },
                    label = { Text("Academic Session") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("exam_session_input"),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text("Start Date") },
                        placeholder = { Text("YYYY-MM-DD") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("exam_start_date_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { endDate = it },
                        label = { Text("End Date") },
                        placeholder = { Text("YYYY-MM-DD") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("exam_end_date_input"),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            EduButton(
                text = "Create Series",
                onClick = {
                    if (title.isBlank()) {
                        validationError = "Please enter examination series title"
                        return@EduButton
                    }
                    if (gradeLevel.isBlank()) {
                        validationError = "Please specify cohort / grade level"
                        return@EduButton
                    }
                    onConfirm(
                        title.trim(),
                        selectedType,
                        academicSession.trim(),
                        gradeLevel.trim(),
                        startDate.trim(),
                        endDate.trim()
                    )
                },
                modifier = Modifier.testTag("submit_create_exam_button")
            )
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_create_exam_button")
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddExamPaperDialog(
    schedule: ExamSchedule,
    onDismiss: () -> Unit,
    conflictWarning: String?,
    onValidateConflict: (date: String, startTime: String, endTime: String, room: String) -> Unit,
    onConfirm: (
        subjectName: String,
        subjectCode: String,
        examDate: String,
        startTime: String,
        endTime: String,
        durationMinutes: Int,
        totalMarks: Int,
        passingMarks: Int,
        roomNumber: String,
        invigilatorName: String,
        instructions: String
    ) -> Unit
) {
    var subjectName by remember { mutableStateOf("") }
    var subjectCode by remember { mutableStateOf("") }
    var examDate by remember { mutableStateOf(schedule.startDate) }
    var startTime by remember { mutableStateOf("09:00 AM") }
    var endTime by remember { mutableStateOf("12:00 PM") }
    var durationMinutesStr by remember { mutableStateOf("180") }
    var totalMarksStr by remember { mutableStateOf("100") }
    var passingMarksStr by remember { mutableStateOf("40") }
    var roomNumber by remember { mutableStateOf("Examination Hall A") }
    var invigilatorName by remember { mutableStateOf("Senior Faculty Staff") }
    var instructions by remember { mutableStateOf("Standard examination regulations apply. ID card required.") }
    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Add Examination Paper",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${schedule.title} (${schedule.gradeLevel})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Conflict Alert Banner
                if (conflictWarning != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = EduErrorContainer),
                        shape = EduShapes.small
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = EduError)
                            Text(text = conflictWarning, color = EduError, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (validationError != null) {
                    Text(text = validationError ?: "", color = EduError, style = MaterialTheme.typography.bodySmall)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = subjectName,
                        onValueChange = { subjectName = it },
                        label = { Text("Subject Name *") },
                        placeholder = { Text("e.g. Pure Mathematics") },
                        modifier = Modifier
                            .weight(1.4f)
                            .testTag("paper_subject_name_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = subjectCode,
                        onValueChange = { subjectCode = it },
                        label = { Text("Code *") },
                        placeholder = { Text("MTH-101") },
                        modifier = Modifier
                            .weight(0.8f)
                            .testTag("paper_subject_code_input"),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = examDate,
                    onValueChange = {
                        examDate = it
                        onValidateConflict(it, startTime, endTime, roomNumber)
                    },
                    label = { Text("Exam Date (YYYY-MM-DD) *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("paper_date_input"),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = {
                            startTime = it
                            onValidateConflict(examDate, it, endTime, roomNumber)
                        },
                        label = { Text("Start Time *") },
                        placeholder = { Text("09:00 AM") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("paper_start_time_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = {
                            endTime = it
                            onValidateConflict(examDate, startTime, it, roomNumber)
                        },
                        label = { Text("End Time *") },
                        placeholder = { Text("12:00 PM") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("paper_end_time_input"),
                        singleLine = true
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = totalMarksStr,
                        onValueChange = { totalMarksStr = it },
                        label = { Text("Total Marks") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("paper_total_marks_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = passingMarksStr,
                        onValueChange = { passingMarksStr = it },
                        label = { Text("Passing Marks") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("paper_pass_marks_input"),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = roomNumber,
                    onValueChange = {
                        roomNumber = it
                        onValidateConflict(examDate, startTime, endTime, it)
                    },
                    label = { Text("Hall / Room Number *") },
                    placeholder = { Text("Room 301 - West Wing") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("paper_room_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = invigilatorName,
                    onValueChange = { invigilatorName = it },
                    label = { Text("Invigilator / Supervisor") },
                    placeholder = { Text("Prof. Alan Turing") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("paper_invigilator_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    label = { Text("Candidate Instructions") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("paper_instructions_input"),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            EduButton(
                text = "Add to Timetable",
                onClick = {
                    if (subjectName.isBlank() || examDate.isBlank() || startTime.isBlank() || roomNumber.isBlank()) {
                        validationError = "Subject, date, time slot, and room are required."
                        return@EduButton
                    }
                    val duration = durationMinutesStr.toIntOrNull() ?: 180
                    val totalMarks = totalMarksStr.toIntOrNull() ?: 100
                    val passingMarks = passingMarksStr.toIntOrNull() ?: 40

                    onConfirm(
                        subjectName.trim(),
                        subjectCode.trim().ifBlank { subjectName.take(3).uppercase() + "-101" },
                        examDate.trim(),
                        startTime.trim(),
                        endTime.trim(),
                        duration,
                        totalMarks,
                        passingMarks,
                        roomNumber.trim(),
                        invigilatorName.trim(),
                        instructions.trim()
                    )
                },
                modifier = Modifier.testTag("submit_add_paper_button")
            )
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_add_paper_button")
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun HallTicketDialog(
    ticket: HallTicket,
    onDismiss: () -> Unit
) {
    var showPrintConfirmation by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with Institutional crest layout
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = EduShapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = ticket.schoolName.uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "OFFICIAL EXAMINATION ADMIT CARD / HALL TICKET",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(EduSuccessContainer)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = ticket.ticketNumber,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = EduSuccess
                                )
                            }
                            Text(
                                text = "Session: ${ticket.academicSession}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Candidate Details Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = EduShapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "CANDIDATE INFORMATION",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        HorizontalDivider()

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1.2f)) {
                                Text("Student Name", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(ticket.studentName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            }
                            Column(modifier = Modifier.weight(0.8f)) {
                                Text("Roll Number", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(ticket.rollNumber, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1.2f)) {
                                Text("Admission Number", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(ticket.admissionNumber, style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.Monospace)
                            }
                            Column(modifier = Modifier.weight(0.8f)) {
                                Text("Class / Cohort", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${ticket.gradeLevel} - ${ticket.section}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                // Examination Schedule Table
                Text(
                    text = "PAPERS & SCHEDULE TIMETABLE",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                if (ticket.papers.isEmpty()) {
                    Text(
                        text = "No papers scheduled for this series yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ticket.papers.forEach { paper ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = EduShapes.small,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = paper.subjectName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${paper.examDate} | ${paper.startTime} - ${paper.endTime}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Room: ${paper.roomNumber}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = "Max: ${paper.totalMarks}M",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Verification & Authority Seal
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = EduShapes.small,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EduSuccess, modifier = Modifier.size(16.dp))
                            Text(
                                text = "Digitally Verified & Authorized",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = EduSuccess
                            )
                        }
                        Text(
                            text = "Authority: ${ticket.verifiedBy} | Issued: ${ticket.issuedDate}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Candidate must bring this physical or authenticated digital admit card alongside institution photo ID to every session.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }

                if (showPrintConfirmation) {
                    Text(
                        text = "Admit Card export dispatched to print queue and saved to digital student locker.",
                        style = MaterialTheme.typography.bodySmall,
                        color = EduSuccess,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EduButton(
                    text = "Export Admit Card",
                    onClick = { showPrintConfirmation = true },
                    variant = EduButtonVariant.OUTLINED,
                    leadingIcon = { Icon(Icons.Default.Print, contentDescription = null) },
                    modifier = Modifier.testTag("export_hall_ticket_button")
                )
                EduButton(
                    text = "Close",
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_hall_ticket_dialog")
                )
            }
        }
    )
}
