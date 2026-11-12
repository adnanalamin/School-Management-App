package com.example.features.homework.presentation

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.features.homework.domain.HomeworkAssignment
import com.example.features.homework.domain.HomeworkSubmission
import com.example.features.homework.domain.SubmissionStatus

@Composable
fun CreateHomeworkDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        description: String,
        subjectCode: String,
        subjectName: String,
        gradeLevel: String,
        section: String,
        dueDate: String,
        totalMarks: Double,
        attachmentTitle: String
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var subjectName by remember { mutableStateOf("Advanced Physics") }
    var subjectCode by remember { mutableStateOf("PHY-101") }
    var gradeLevel by remember { mutableStateOf("Grade 10") }
    var section by remember { mutableStateOf("Section A") }
    var dueDate by remember { mutableStateOf("2026-04-05") }
    var totalMarksStr by remember { mutableStateOf("50") }
    var attachmentTitle by remember { mutableStateOf("Coursework_Guidelines.pdf") }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("dialog_create_homework"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Assignment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Assign New Coursework",
                    style = MaterialTheme.typography.titleMedium,
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
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Assignment Title *") },
                    placeholder = { Text("e.g. Lab Report: Optics & Refraction") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_homework_title")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Instructions & Rubric Prompt *") },
                    placeholder = { Text("Explain problem scope and citation requirements...") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("input_homework_description")
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = subjectName,
                        onValueChange = { subjectName = it },
                        label = { Text("Subject Name") },
                        modifier = Modifier.weight(1f).testTag("input_homework_subject_name")
                    )
                    OutlinedTextField(
                        value = subjectCode,
                        onValueChange = { subjectCode = it },
                        label = { Text("Code") },
                        modifier = Modifier.weight(0.6f).testTag("input_homework_subject_code")
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = gradeLevel,
                        onValueChange = { gradeLevel = it },
                        label = { Text("Grade Level") },
                        modifier = Modifier.weight(1f).testTag("input_homework_grade")
                    )
                    OutlinedTextField(
                        value = section,
                        onValueChange = { section = it },
                        label = { Text("Section") },
                        modifier = Modifier.weight(0.7f).testTag("input_homework_section")
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { dueDate = it },
                        label = { Text("Due Date (YYYY-MM-DD)") },
                        modifier = Modifier.weight(1.2f).testTag("input_homework_due_date")
                    )
                    OutlinedTextField(
                        value = totalMarksStr,
                        onValueChange = { totalMarksStr = it },
                        label = { Text("Max Marks") },
                        modifier = Modifier.weight(0.8f).testTag("input_homework_total_marks")
                    )
                }

                OutlinedTextField(
                    value = attachmentTitle,
                    onValueChange = { attachmentTitle = it },
                    label = { Text("Reference Materials / Handout") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.AttachFile, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().testTag("input_homework_attachment")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val marks = totalMarksStr.toDoubleOrNull() ?: 50.0
                    onConfirm(
                        title,
                        description,
                        subjectCode,
                        subjectName,
                        gradeLevel,
                        section,
                        dueDate,
                        marks,
                        attachmentTitle
                    )
                },
                enabled = title.isNotBlank() && description.isNotBlank(),
                modifier = Modifier.testTag("btn_confirm_create_homework")
            ) {
                Text("Assign Coursework")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_cancel_create_homework")
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SubmitHomeworkDialog(
    assignment: HomeworkAssignment,
    onDismiss: () -> Unit,
    onSubmit: (
        studentName: String,
        rollNumber: String,
        attachmentTitle: String,
        studentNotes: String
    ) -> Unit
) {
    var studentName by remember { mutableStateOf("Amina Rahman") }
    var rollNumber by remember { mutableStateOf("OXF-10A-001") }
    var attachmentTitle by remember { mutableStateOf("${studentName.replace(" ", "_")}_Deliverable.pdf") }
    var studentNotes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("dialog_submit_homework"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Submit Deliverable",
                    style = MaterialTheme.typography.titleMedium,
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
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = assignment.title,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${assignment.subjectName} • Due: ${assignment.dueDate} • Max: ${assignment.totalMarks.toInt()} pts",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                OutlinedTextField(
                    value = studentName,
                    onValueChange = {
                        studentName = it
                        attachmentTitle = "${it.replace(" ", "_")}_Deliverable.pdf"
                    },
                    label = { Text("Student Full Name *") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().testTag("input_submit_student_name")
                )

                OutlinedTextField(
                    value = rollNumber,
                    onValueChange = { rollNumber = it },
                    label = { Text("Roll / Student ID *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_submit_roll_number")
                )

                OutlinedTextField(
                    value = attachmentTitle,
                    onValueChange = { attachmentTitle = it },
                    label = { Text("Deliverable File Name *") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.AttachFile, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().testTag("input_submit_attachment_title")
                )

                OutlinedTextField(
                    value = studentNotes,
                    onValueChange = { studentNotes = it },
                    label = { Text("Submission Remarks / Methodology") },
                    placeholder = { Text("Add any notes for the instructor regarding approach...") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth().testTag("input_submit_notes")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(studentName, rollNumber, attachmentTitle, studentNotes) },
                enabled = studentName.isNotBlank() && rollNumber.isNotBlank() && attachmentTitle.isNotBlank(),
                modifier = Modifier.testTag("btn_confirm_submit_homework")
            ) {
                Text("Turn In Deliverable")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_cancel_submit_homework")
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun GradeSubmissionDialog(
    assignment: HomeworkAssignment,
    submission: HomeworkSubmission,
    onDismiss: () -> Unit,
    onGrade: (marksAwarded: Double, feedback: String) -> Unit
) {
    var marksStr by remember { mutableStateOf(submission.marksAwarded?.toString() ?: "${assignment.totalMarks.toInt() * 0.9}") }
    var feedback by remember { mutableStateOf(submission.feedback.ifBlank { "Thorough work demonstrated. Good analysis." }) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("dialog_grade_submission"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Grade,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Evaluate Deliverable",
                    style = MaterialTheme.typography.titleMedium,
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
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = submission.studentName,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "Roll: ${submission.rollNumber} • Submitted: ${submission.submissionDate}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (submission.attachmentTitle.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = submission.attachmentTitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        if (submission.studentNotes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Notes: \"${submission.studentNotes}\"",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = marksStr,
                    onValueChange = { marksStr = it },
                    label = { Text("Score Awarded (out of ${assignment.totalMarks.toInt()}) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_grade_score")
                )

                OutlinedTextField(
                    value = feedback,
                    onValueChange = { feedback = it },
                    label = { Text("Instructor Feedback / Rubric Notes *") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("input_grade_feedback")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val score = marksStr.toDoubleOrNull() ?: 0.0
                    val clamped = score.coerceIn(0.0, assignment.totalMarks)
                    onGrade(clamped, feedback)
                },
                modifier = Modifier.testTag("btn_confirm_grade_submission")
            ) {
                Text("Submit Evaluation")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_cancel_grade_submission")
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun HomeworkDetailDialog(
    assignment: HomeworkAssignment,
    onDismiss: () -> Unit,
    onSubmitClick: () -> Unit,
    onGradeClick: (HomeworkSubmission) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("dialog_homework_detail"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = assignment.subjectName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = assignment.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Grade: ${assignment.gradeLevel} (${assignment.section})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "Due: ${assignment.dueDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Assignment Instructions:",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.labelLarge
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = assignment.description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (assignment.attachmentTitle.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = assignment.attachmentTitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Student Submissions (${assignment.submissionCount}/${assignment.totalEnrolledStudents})",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "${assignment.submissionRatePercentage.toInt()}% Turn-in",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (assignment.submissions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No submissions recorded yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                } else {
                    assignment.submissions.forEach { sub ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = sub.studentName,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "Roll: ${sub.rollNumber} • ${sub.submissionDate}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                    if (sub.status == SubmissionStatus.GRADED) {
                                        Text(
                                            text = "${sub.marksAwarded?.toInt()}/${assignment.totalMarks.toInt()} pts",
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.tertiary,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    } else {
                                        OutlinedButton(
                                            onClick = { onGradeClick(sub) },
                                            modifier = Modifier.height(32.dp).testTag("btn_grade_${sub.rollNumber}")
                                        ) {
                                            Text("Grade", fontSize = 11.sp)
                                        }
                                    }
                                }
                                if (sub.feedback.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Feedback: ${sub.feedback}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSubmitClick,
                modifier = Modifier.testTag("btn_turn_in_deliverable_from_detail")
            ) {
                Text("Turn In Deliverable")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
