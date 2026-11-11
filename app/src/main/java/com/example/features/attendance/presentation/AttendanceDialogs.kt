package com.example.features.attendance.presentation

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.EduError
import com.example.core.theme.EduErrorContainer
import com.example.core.theme.EduShapes
import com.example.core.theme.EduSpacing
import com.example.core.theme.EduSuccess
import com.example.core.theme.EduSuccessContainer
import com.example.features.attendance.domain.AttendanceAuditLog
import com.example.features.attendance.domain.StudentAttendanceEntry
import com.example.shared.components.EduButton
import com.example.shared.components.EduButtonVariant
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * AttendanceAuditDialog displays an immutable chronological audit trail of all changes,
 * submissions, and unlock actions for the active attendance register.
 */
@Composable
fun AttendanceAuditDialog(
    auditTrail: List<AttendanceAuditLog>,
    className: String,
    sectionName: String,
    date: String,
    onDismiss: () -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("hh:mm a, MMM dd", Locale.US) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("attendance_audit_dialog"),
        shape = EduShapes.extraLarge,
        containerColor = MaterialTheme.colorScheme.surface,
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Attendance Audit Log",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$className ($sectionName) • $date",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            if (auditTrail.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(EduSpacing.lg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No audit log entries recorded yet for this roster session.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(vertical = EduSpacing.xs),
                    verticalArrangement = Arrangement.spacedBy(EduSpacing.sm)
                ) {
                    items(auditTrail.reversed()) { log ->
                        AuditLogRow(log = log, timeFormat = timeFormat)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dismiss_audit_dialog")
            ) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun AuditLogRow(
    log: AttendanceAuditLog,
    timeFormat: SimpleDateFormat
) {
    val isFinalized = log.action == "FINALIZED"
    val isReopened = log.action == "REOPENED_FOR_EDIT"

    val (badgeBg, badgeText, badgeColor) = when {
        isFinalized -> Triple(EduSuccessContainer, "FINALIZED", EduSuccess)
        isReopened -> Triple(EduErrorContainer, "REOPENED", EduError)
        else -> Triple(MaterialTheme.colorScheme.surfaceVariant, log.action, MaterialTheme.colorScheme.onSurfaceVariant)
    }

    val icon = when {
        isFinalized -> Icons.Default.Lock
        isReopened -> Icons.Default.LockOpen
        else -> Icons.Default.Notes
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = EduShapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier.padding(EduSpacing.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = badgeColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(EduSpacing.xs))
                    Text(
                        text = log.actorName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeBg)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = badgeColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(EduSpacing.xs))

            if (log.notes.isNotBlank()) {
                Text(
                    text = log.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(EduSpacing.xs))
            }

            Text(
                text = timeFormat.format(Date(log.timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

/**
 * Dialog to add or update medical/excusal remarks for an individual student.
 */
@Composable
fun StudentRemarksDialog(
    student: StudentAttendanceEntry,
    initialRemarks: String,
    onRemarksChanged: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("student_remarks_dialog"),
        shape = EduShapes.extraLarge,
        containerColor = MaterialTheme.colorScheme.surface,
        icon = {
            Icon(
                imageVector = Icons.Default.Notes,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Attendance Remarks",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${student.studentName} (${student.rollNumber})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "Provide medical excuses, transport delay details, or administrative notes for this student's attendance entry.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(EduSpacing.md))
                OutlinedTextField(
                    value = initialRemarks,
                    onValueChange = onRemarksChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("remarks_text_input"),
                    placeholder = { Text("e.g. Fever excusal, Doctor appointment...") },
                    minLines = 3,
                    maxLines = 5,
                    shape = EduShapes.medium
                )
            }
        },
        confirmButton = {
            EduButton(
                text = "Save Remarks",
                onClick = onSave,
                testTag = "save_remarks_button"
            )
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_remarks_button")
            ) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Dialog to unlock a finalized attendance register for corrections with a mandatory revision audit reason.
 */
@Composable
fun ReopenAttendanceDialog(
    onConfirm: (reason: String) -> Unit,
    onDismiss: () -> Unit
) {
    var reason by remember { mutableStateOf("") }
    var hasError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("reopen_attendance_dialog"),
        shape = EduShapes.extraLarge,
        containerColor = MaterialTheme.colorScheme.surface,
        icon = {
            Icon(
                imageVector = Icons.Default.LockOpen,
                contentDescription = null,
                tint = EduError,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "Unlock Finalized Register",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "This attendance roster is currently finalized and locked. Unlocking it will log an audit event with your identity.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(EduSpacing.md))
                OutlinedTextField(
                    value = reason,
                    onValueChange = {
                        reason = it
                        if (hasError && it.isNotBlank()) hasError = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reopen_reason_input"),
                    label = { Text("Reason for Reopening *") },
                    placeholder = { Text("e.g., Parent presented official doctor's note") },
                    isError = hasError,
                    supportingText = if (hasError) {
                        { Text("Reason is mandatory for audit logging") }
                    } else null,
                    minLines = 2,
                    maxLines = 4,
                    shape = EduShapes.medium
                )
            }
        },
        confirmButton = {
            EduButton(
                text = "Unlock Register",
                variant = EduButtonVariant.DANGER,
                onClick = {
                    if (reason.trim().isBlank()) {
                        hasError = true
                    } else {
                        onConfirm(reason.trim())
                    }
                },
                testTag = "confirm_unlock_button"
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
