package com.example.features.teachers.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.core.theme.EduSpacing
import com.example.features.teachers.domain.TeacherProfile
import com.example.features.teachers.domain.TeacherStatus
import com.example.shared.components.BadgeType
import com.example.shared.components.EduBadge
import com.example.shared.components.EduButton
import com.example.shared.components.EduButtonVariant

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TeacherDetailDialog(
    teacher: TeacherProfile,
    onDismiss: () -> Unit,
    onArchive: () -> Unit,
    canArchive: Boolean
) {
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .height(680.dp)
                .clip(RoundedCornerShape(20.dp))
                .testTag("teacher_detail_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = EduSpacing.lg, vertical = EduSpacing.md),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Faculty Profile (360°)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_detail_btn")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close Profile")
                    }
                }

                HorizontalDivider()

                // Profile Content Scroll
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = EduSpacing.lg)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(EduSpacing.md)
                ) {
                    Spacer(modifier = Modifier.height(EduSpacing.xs))

                    // Header Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                val initials = "${teacher.firstName.take(1)}${teacher.lastName.take(1)}".uppercase()
                                Text(
                                    text = initials,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(EduSpacing.md))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = teacher.fullName,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${teacher.designation} • ${teacher.department}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    EduBadge(text = teacher.employeeId, type = BadgeType.PRIMARY)
                                    val statusBadge = when (teacher.status) {
                                        TeacherStatus.ACTIVE -> BadgeType.SUCCESS
                                        TeacherStatus.ON_LEAVE -> BadgeType.WARNING
                                        TeacherStatus.RESIGNED, TeacherStatus.RETIRED -> BadgeType.ERROR
                                    }
                                    EduBadge(text = teacher.status.displayName, type = statusBadge)
                                }
                            }
                        }
                    }

                    // Section 1: Academic & Professional Credentials
                    DetailSectionCard(title = "Academic & Employment Credentials", icon = Icons.Default.School) {
                        DetailItemRow("Highest Qualification", teacher.qualification)
                        DetailItemRow("Teaching Experience", "${teacher.experienceYears} Years")
                        DetailItemRow("Joining Date", teacher.joiningDate)
                        DetailItemRow("Salary Grade", teacher.salaryTier)
                    }

                    // Section 2: Subject & Class In-Charge Allocations
                    DetailSectionCard(title = "Subject & Class In-Charge Allocations", icon = Icons.Default.Badge) {
                        DetailItemRow("Primary Subject", teacher.primarySubject)
                        if (teacher.additionalSubjects.isNotEmpty()) {
                            Text(
                                text = "Additional Subjects",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                teacher.additionalSubjects.forEach { subj ->
                                    EduBadge(text = subj, type = BadgeType.INFO)
                                }
                            }
                        }
                        DetailItemRow(
                            "Class In-Charge Role",
                            if (teacher.isClassTeacher) "${teacher.assignedClassId ?: "Grade"} - ${teacher.assignedSectionId ?: "Section"}" else "Not Assigned"
                        )
                    }

                    // Section 3: Contact & Personal Details
                    DetailSectionCard(title = "Contact & Personal Information", icon = Icons.Default.Phone) {
                        DetailItemRow("Institutional Email", teacher.email)
                        DetailItemRow("Contact Phone", teacher.phone)
                        if (teacher.emergencyContact.isNotBlank()) {
                            DetailItemRow("Emergency Contact", teacher.emergencyContact)
                        }
                        if (teacher.address.isNotBlank()) {
                            DetailItemRow("Residential Address", teacher.address)
                        }
                        DetailItemRow("Blood Group", teacher.bloodGroup)
                    }

                    Spacer(modifier = Modifier.height(EduSpacing.sm))
                }

                HorizontalDivider()

                // Footer Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(EduSpacing.lg),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (canArchive) {
                        EduButton(
                            text = "Archive Faculty",
                            onClick = onArchive,
                            variant = EduButtonVariant.DANGER,
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                            testTag = "detail_archive_btn"
                        )
                    } else {
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    EduButton(
                        text = "Close",
                        onClick = onDismiss,
                        variant = EduButtonVariant.PRIMARY,
                        testTag = "detail_close_btn"
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun DetailItemRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1.3f)
        )
    }
}
