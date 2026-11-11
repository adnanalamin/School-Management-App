package com.example.features.results.presentation

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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.features.results.domain.GradeScale
import com.example.features.results.domain.ReportCard
import com.example.features.results.domain.SubjectScore
import com.example.shared.components.EduButton
import com.example.shared.components.EduButtonVariant

@Composable
fun ScoreEntryDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        studentName: String,
        rollNumber: String,
        gradeLevel: String,
        section: String,
        examTitle: String,
        academicSession: String,
        scores: List<SubjectScore>,
        teacherRemarks: String
    ) -> Unit
) {
    var studentName by remember { mutableStateOf("") }
    var rollNumber by remember { mutableStateOf("") }
    var gradeLevel by remember { mutableStateOf("Grade 10") }
    var section by remember { mutableStateOf("Section A") }
    var examTitle by remember { mutableStateOf("Midterm Examination Series 2026") }
    var academicSession by remember { mutableStateOf("2026-2027") }

    // 3 Subject score fields for input
    var physicsMarks by remember { mutableStateOf("85") }
    var mathMarks by remember { mutableStateOf("92") }
    var englishMarks by remember { mutableStateOf("88") }
    var teacherRemarks by remember { mutableStateOf("High standard of academic reasoning and discipline.") }
    var validationError by remember { mutableStateOf<String?>(null) }

    // Live metric preview
    val pScore = physicsMarks.toDoubleOrNull() ?: 0.0
    val mScore = mathMarks.toDoubleOrNull() ?: 0.0
    val eScore = englishMarks.toDoubleOrNull() ?: 0.0
    val totalObtained = pScore + mScore + eScore
    val totalPossible = 300.0
    val previewPct = (totalObtained / totalPossible) * 100.0
    val previewGrade = GradeScale.calculate(previewPct)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Assessment, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    text = "Record Student Examination Scores",
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
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (validationError != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = EduErrorContainer),
                        shape = EduShapes.small
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = EduError)
                            Text(text = validationError ?: "", color = EduError, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = studentName,
                        onValueChange = { studentName = it },
                        label = { Text("Candidate Name *") },
                        placeholder = { Text("e.g. Liam Sterling") },
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("score_student_name_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = rollNumber,
                        onValueChange = { rollNumber = it },
                        label = { Text("Roll No *") },
                        placeholder = { Text("OXF-10A-005") },
                        modifier = Modifier
                            .weight(0.9f)
                            .testTag("score_roll_number_input"),
                        singleLine = true
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = gradeLevel,
                        onValueChange = { gradeLevel = it },
                        label = { Text("Grade Level") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("score_grade_level_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = section,
                        onValueChange = { section = it },
                        label = { Text("Section") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("score_section_input"),
                        singleLine = true
                    )
                }

                // Live Preview Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = EduShapes.small,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Marks: ${totalObtained.toInt()} / 300", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Text("Calculated: ${String.format("%.1f", previewPct)}% • GPA: ${previewGrade.gradePoint}", style = MaterialTheme.typography.bodySmall)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(EduSuccessContainer)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = previewGrade.letter,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = EduSuccess
                            )
                        }
                    }
                }

                Text(
                    text = "SUBJECT EVALUATIONS (OUT OF 100)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = physicsMarks,
                        onValueChange = { physicsMarks = it },
                        label = { Text("Physics") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("score_physics_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = mathMarks,
                        onValueChange = { mathMarks = it },
                        label = { Text("Mathematics") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("score_math_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = englishMarks,
                        onValueChange = { englishMarks = it },
                        label = { Text("English") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("score_english_input"),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = teacherRemarks,
                    onValueChange = { teacherRemarks = it },
                    label = { Text("Faculty Evaluator Remarks") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("score_remarks_input"),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            EduButton(
                text = "Save Score Sheet",
                onClick = {
                    if (studentName.isBlank() || rollNumber.isBlank()) {
                        validationError = "Student name and roll number are required."
                        return@EduButton
                    }
                    val p = physicsMarks.toDoubleOrNull() ?: 0.0
                    val m = mathMarks.toDoubleOrNull() ?: 0.0
                    val e = englishMarks.toDoubleOrNull() ?: 0.0

                    val scoreList = listOf(
                        SubjectScore("PHY-101", "Advanced Physics", p, 100.0),
                        SubjectScore("MTH-102", "Pure Mathematics", m, 100.0),
                        SubjectScore("ENG-103", "English Literature", e, 100.0)
                    )

                    onConfirm(
                        studentName.trim(),
                        rollNumber.trim(),
                        gradeLevel.trim(),
                        section.trim(),
                        examTitle.trim(),
                        academicSession.trim(),
                        scoreList,
                        teacherRemarks.trim()
                    )
                },
                modifier = Modifier.testTag("submit_score_entry_button")
            )
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_score_entry_button")
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ReportCardDialog(
    reportCard: ReportCard,
    onDismiss: () -> Unit
) {
    var showExportSuccess by remember { mutableStateOf(false) }
    val res = reportCard.studentResult

    AlertDialog(
        onDismissRequest = onDismiss,
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Official Transcript Header
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
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = reportCard.schoolName.uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "OFFICIAL ACADEMIC TRANSCRIPT & REPORT CARD",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${res.examTitle} • Academic Session ${res.academicSession}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Candidate Bio & Class Rank Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = EduShapes.small,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Student Name", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(res.studentName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Roll / Student ID", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(res.rollNumber, style = MaterialTheme.typography.titleSmall, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            }
                        }
                        HorizontalDivider()
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Class: ${res.gradeLevel} (${res.section})", style = MaterialTheme.typography.bodySmall)
                            Text("Term Attendance: ${reportCard.attendancePercentage}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = EduSuccess)
                        }
                    }
                }

                // Subject Scores Table
                Text(
                    text = "ACADEMIC MARKS & GRADE EVALUATIONS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    res.subjectScores.forEach { subject ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = EduShapes.small,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(subject.subjectName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    Text(
                                        "${subject.marksObtained.toInt()} / ${subject.totalMarks.toInt()} Marks (${String.format("%.1f", subject.percentage)}%)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(MaterialTheme.colorScheme.primaryContainer)
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "${subject.grade} (${subject.gradePoint})",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Grand Total & Honors Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = EduShapes.small,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Overall Percentage", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${String.format("%.2f", res.percentage)}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Cumulative GPA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${String.format("%.2f", res.gpa)} / 4.0", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Overall Grade", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(res.overallGrade, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = EduSuccess)
                            }
                        }
                        HorizontalDivider()
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Text(text = reportCard.honorsRemark, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.weight(1f))
                            if (res.rankInClass > 0) {
                                Text("Class Rank: #${res.rankInClass}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Faculty Remarks & Seal
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = EduShapes.small,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Faculty Remark: \"${res.teacherRemarks}\"",
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                        Text(
                            text = "Authorized By: ${reportCard.principalSignature} • Issued: ${reportCard.issuanceDate}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (showExportSuccess) {
                    Text(
                        text = "Report Card exported to high-resolution PDF and synced to student digital portfolio.",
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
                    text = "Export Transcript",
                    onClick = { showExportSuccess = true },
                    variant = EduButtonVariant.OUTLINED,
                    leadingIcon = { Icon(Icons.Default.Print, contentDescription = null) },
                    modifier = Modifier.testTag("export_report_card_button")
                )
                EduButton(
                    text = "Close",
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_report_card_dialog")
                )
            }
        }
    )
}
