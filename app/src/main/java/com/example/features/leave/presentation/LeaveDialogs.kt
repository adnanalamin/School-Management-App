package com.example.features.leave.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.security.Permission
import com.example.core.security.TenantContext
import com.example.core.security.UserRole
import com.example.features.leave.domain.LeaveApplicantType
import com.example.features.leave.domain.LeaveCategory
import com.example.features.leave.domain.LeaveRequest
import com.example.features.leave.domain.LeaveStatus

@Composable
fun ApplyLeaveDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        applicantName: String,
        applicantType: LeaveApplicantType,
        departmentOrGrade: String,
        category: LeaveCategory,
        startDate: String,
        endDate: String,
        totalDays: Int,
        reason: String,
        attachmentNote: String
    ) -> Unit
) {
    val activeRole = TenantContext.activeUserRole.value
    val defaultType = when (activeRole) {
        UserRole.STUDENT, UserRole.PARENT -> LeaveApplicantType.STUDENT
        UserRole.TEACHER -> LeaveApplicantType.FACULTY
        else -> LeaveApplicantType.STAFF
    }
    val defaultName = when (activeRole) {
        UserRole.STUDENT, UserRole.PARENT -> "Amina Rahman"
        UserRole.TEACHER -> "Dr. Sarah Jenkins"
        UserRole.LIBRARIAN -> "Eleanor Rigby"
        UserRole.TRANSPORT_MANAGER -> "Arthur Pendelton"
        else -> ""
    }
    val defaultDept = when (activeRole) {
        UserRole.STUDENT, UserRole.PARENT -> "Grade 10 - Section A"
        UserRole.TEACHER -> "Physics Department"
        else -> "Administration"
    }

    var applicantName by remember { mutableStateOf(defaultName) }
    var selectedType by remember { mutableStateOf(defaultType) }
    var departmentOrGrade by remember { mutableStateOf(defaultDept) }
    var selectedCategory by remember { mutableStateOf(LeaveCategory.SICK) }
    var startDate by remember { mutableStateOf("2026-04-15") }
    var endDate by remember { mutableStateOf("2026-04-16") }
    var totalDaysInput by remember { mutableStateOf("2") }
    var reason by remember { mutableStateOf("") }
    var attachmentNote by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("dialog_apply_leave"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.EventBusy,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Submit Leave Application",
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
                    value = applicantName,
                    onValueChange = { applicantName = it },
                    label = { Text("Applicant Full Name *") },
                    placeholder = { Text("e.g. Amina Rahman") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_leave_applicant")
                )

                Text(
                    text = "Applicant Role",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    LeaveApplicantType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.displayName, fontSize = 11.sp) },
                            modifier = Modifier.testTag("chip_apply_type_${type.name.lowercase()}")
                        )
                    }
                }

                OutlinedTextField(
                    value = departmentOrGrade,
                    onValueChange = { departmentOrGrade = it },
                    label = { Text("Grade / Section or Department") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_leave_dept")
                )

                Text(
                    text = "Leave Category",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    LeaveCategory.entries.forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category.displayName, fontSize = 11.sp) },
                            modifier = Modifier.testTag("chip_apply_cat_${category.name.lowercase()}")
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text("From *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("input_leave_start_date")
                    )
                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { endDate = it },
                        label = { Text("To *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("input_leave_end_date")
                    )
                    OutlinedTextField(
                        value = totalDaysInput,
                        onValueChange = { totalDaysInput = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Days") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(0.6f).testTag("input_leave_days")
                    )
                }

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason for Leave *") },
                    placeholder = { Text("Provide medical, family, or academic duty details...") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth().testTag("input_leave_reason")
                )

                OutlinedTextField(
                    value = attachmentNote,
                    onValueChange = { attachmentNote = it },
                    label = { Text("Supporting Document Reference (Optional)") },
                    placeholder = { Text("e.g. Medical_Note_Dr_Lin.pdf") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_leave_attachment")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val days = totalDaysInput.toIntOrNull() ?: 1
                    onConfirm(
                        applicantName,
                        selectedType,
                        departmentOrGrade,
                        selectedCategory,
                        startDate,
                        endDate,
                        days,
                        reason,
                        attachmentNote
                    )
                },
                enabled = applicantName.isNotBlank() && startDate.isNotBlank() && endDate.isNotBlank() && reason.isNotBlank(),
                modifier = Modifier.minimumInteractiveComponentSize().testTag("btn_confirm_apply_leave")
            ) {
                Text("Submit Request")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.minimumInteractiveComponentSize().testTag("btn_cancel_apply_leave")
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ReviewLeaveDialog(
    leaveRequest: LeaveRequest,
    onDismiss: () -> Unit,
    onApprove: (remarks: String) -> Unit,
    onReject: (remarks: String) -> Unit
) {
    var remarks by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("dialog_review_leave"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Gavel,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Review ${leaveRequest.requestCode}",
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
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "${leaveRequest.applicantName} (${leaveRequest.applicantType.displayName})",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${leaveRequest.departmentOrGrade} • ${leaveRequest.category.displayName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${leaveRequest.startDate} to ${leaveRequest.endDate} (${leaveRequest.totalDays} days)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = leaveRequest.reason,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Decision Remarks (Required to Decline)") },
                    placeholder = { Text("e.g. Approved with substitute coverage...") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth().testTag("input_review_remarks")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onApprove(remarks) },
                modifier = Modifier.minimumInteractiveComponentSize().testTag("btn_confirm_approve_leave")
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Approve")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.minimumInteractiveComponentSize().testTag("btn_cancel_review_leave")
                ) {
                    Text("Cancel")
                }
                OutlinedButton(
                    onClick = { onReject(remarks) },
                    enabled = remarks.isNotBlank(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.minimumInteractiveComponentSize().testTag("btn_confirm_reject_leave")
                ) {
                    Text("Decline")
                }
            }
        }
    )
}

@Composable
fun LeaveDetailDialog(
    leaveRequest: LeaveRequest,
    onDismiss: () -> Unit,
    onOpenReview: () -> Unit,
    onWithdraw: () -> Unit
) {
    val statusColors = when (leaveRequest.status) {
        LeaveStatus.PENDING -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        LeaveStatus.APPROVED -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        LeaveStatus.REJECTED -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        LeaveStatus.CANCELLED -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("dialog_leave_detail"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = leaveRequest.requestCode,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = leaveRequest.applicantName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = leaveRequest.status.displayName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColors.second,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(statusColors.first)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "${leaveRequest.applicantType.displayName} • ${leaveRequest.departmentOrGrade}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${leaveRequest.startDate} to ${leaveRequest.endDate} (${leaveRequest.totalDays} day${if (leaveRequest.totalDays == 1) "" else "s"})",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(
                            text = "Category: ${leaveRequest.category.displayName} • Applied ${leaveRequest.submittedDate}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
                        Text(
                            text = leaveRequest.reason,
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (leaveRequest.attachmentNote.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AttachFile,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = leaveRequest.attachmentNote,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                if (leaveRequest.reviewerName.isNotBlank() || leaveRequest.reviewRemarks.isNotBlank()) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Approval Audit Trail",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Reviewed by: ${leaveRequest.reviewerName} (${leaveRequest.reviewedDate})",
                                style = MaterialTheme.typography.bodySmall
                            )
                            if (leaveRequest.reviewRemarks.isNotBlank()) {
                                Text(
                                    text = "Remarks: ${leaveRequest.reviewRemarks}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (leaveRequest.isPending && TenantContext.hasPermission(Permission.LEAVES_APPROVE)) {
                Button(
                    onClick = onOpenReview,
                    modifier = Modifier.minimumInteractiveComponentSize().testTag("btn_detail_review_leave")
                ) {
                    Text("Approve / Decline")
                }
            } else {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Text("Done")
                }
            }
        },
        dismissButton = {
            if (leaveRequest.canWithdraw && TenantContext.hasPermission(Permission.LEAVES_APPLY)) {
                OutlinedButton(
                    onClick = onWithdraw,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.minimumInteractiveComponentSize().testTag("btn_detail_withdraw_leave")
                ) {
                    Text("Withdraw")
                }
            }
        }
    )
}
