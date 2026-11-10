package com.example.features.teachers.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.core.theme.EduSpacing
import com.example.features.teachers.domain.TeacherProfile
import com.example.shared.components.BadgeType
import com.example.shared.components.EduBadge
import com.example.shared.components.EduButton
import com.example.shared.components.EduButtonVariant

@Composable
fun TeacherOnboardingDialog(
    viewModel: TeacherOnboardingViewModel,
    onDismiss: () -> Unit,
    onSuccess: (TeacherProfile) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
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
                .testTag("teacher_onboarding_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = EduSpacing.lg, vertical = EduSpacing.md),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Add Faculty & Staff",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Step ${state.currentStep + 1} of 4: ${getStepTitle(state.currentStep)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_onboarding_btn")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Progress Bar
                LinearProgressIndicator(
                    progress = { (state.currentStep + 1) / 4f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = EduSpacing.lg),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )

                Spacer(modifier = Modifier.height(EduSpacing.md))

                // Scrollable Form Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = EduSpacing.lg)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(EduSpacing.md)
                ) {
                    // Error Banner
                    state.errorMessage?.let { msg ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().testTag("onboarding_error_banner")
                        ) {
                            Text(
                                text = msg,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    when (state.currentStep) {
                        0 -> Step0PersonalDetails(state, viewModel)
                        1 -> Step1AcademicEmployment(state, viewModel)
                        2 -> Step2SubjectClassAllocation(state, viewModel)
                        3 -> Step3ReviewConfirm(state)
                    }
                }

                HorizontalDivider()

                // Footer Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(EduSpacing.lg),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (state.currentStep > 0) {
                        EduButton(
                            text = "Previous",
                            onClick = { viewModel.previousStep() },
                            variant = EduButtonVariant.OUTLINED,
                            leadingIcon = { Icon(Icons.Default.ArrowBack, contentDescription = null) },
                            testTag = "onboarding_prev_btn"
                        )
                    } else {
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    if (state.currentStep < 3) {
                        EduButton(
                            text = "Next Step",
                            onClick = { viewModel.nextStep() },
                            trailingIcon = { Icon(Icons.Default.ArrowForward, contentDescription = null) },
                            testTag = "onboarding_next_btn"
                        )
                    } else {
                        EduButton(
                            text = if (state.isSubmitting) "Registering..." else "Confirm & Register",
                            onClick = { viewModel.submitOnboarding(onSuccess) },
                            enabled = !state.isSubmitting,
                            trailingIcon = if (!state.isSubmitting) {
                                { Icon(Icons.Default.Check, contentDescription = null) }
                            } else null,
                            testTag = "onboarding_submit_btn"
                        )
                    }
                }
            }
        }
    }
}

private fun getStepTitle(step: Int): String {
    return when (step) {
        0 -> "Personal & Contact"
        1 -> "Academic & Employment"
        2 -> "Subject & Class Allocation"
        3 -> "Review & Finalize"
        else -> ""
    }
}

@Composable
private fun Step0PersonalDetails(
    state: TeacherOnboardingUiState,
    viewModel: TeacherOnboardingViewModel
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(EduSpacing.md)
    ) {
        OutlinedTextField(
            value = state.firstName,
            onValueChange = { viewModel.onFirstNameChange(it) },
            label = { Text("First Name *") },
            modifier = Modifier.weight(1f).testTag("teacher_first_name_input"),
            singleLine = true
        )
        OutlinedTextField(
            value = state.lastName,
            onValueChange = { viewModel.onLastNameChange(it) },
            label = { Text("Last Name *") },
            modifier = Modifier.weight(1f).testTag("teacher_last_name_input"),
            singleLine = true
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(EduSpacing.md)
    ) {
        OutlinedTextField(
            value = state.gender,
            onValueChange = { viewModel.onGenderChange(it) },
            label = { Text("Gender") },
            modifier = Modifier.weight(1f).testTag("teacher_gender_input"),
            singleLine = true
        )
        OutlinedTextField(
            value = state.bloodGroup,
            onValueChange = { viewModel.onBloodGroupChange(it) },
            label = { Text("Blood Group") },
            modifier = Modifier.weight(1f).testTag("teacher_blood_group_input"),
            singleLine = true
        )
    }

    OutlinedTextField(
        value = state.dateOfBirth,
        onValueChange = { viewModel.onDateOfBirthChange(it) },
        label = { Text("Date of Birth (YYYY-MM-DD)") },
        modifier = Modifier.fillMaxWidth().testTag("teacher_dob_input"),
        singleLine = true
    )

    OutlinedTextField(
        value = state.email,
        onValueChange = { viewModel.onEmailChange(it) },
        label = { Text("Institutional Email *") },
        modifier = Modifier.fillMaxWidth().testTag("teacher_email_input"),
        singleLine = true
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(EduSpacing.md)
    ) {
        OutlinedTextField(
            value = state.phone,
            onValueChange = { viewModel.onPhoneChange(it) },
            label = { Text("Contact Phone *") },
            modifier = Modifier.weight(1f).testTag("teacher_phone_input"),
            singleLine = true
        )
        OutlinedTextField(
            value = state.emergencyContact,
            onValueChange = { viewModel.onEmergencyContactChange(it) },
            label = { Text("Emergency Contact") },
            modifier = Modifier.weight(1f).testTag("teacher_emergency_input"),
            singleLine = true
        )
    }

    OutlinedTextField(
        value = state.address,
        onValueChange = { viewModel.onAddressChange(it) },
        label = { Text("Residential Address") },
        modifier = Modifier.fillMaxWidth().testTag("teacher_address_input"),
        minLines = 2
    )
}

@Composable
private fun Step1AcademicEmployment(
    state: TeacherOnboardingUiState,
    viewModel: TeacherOnboardingViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "System Assigned Employee ID",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = state.employeeId,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            EduBadge(text = "AUTO-GENERATED", type = BadgeType.PRIMARY)
        }
    }

    OutlinedTextField(
        value = state.department,
        onValueChange = { viewModel.onDepartmentChange(it) },
        label = { Text("Academic Department *") },
        modifier = Modifier.fillMaxWidth().testTag("teacher_department_input"),
        singleLine = true
    )

    OutlinedTextField(
        value = state.designation,
        onValueChange = { viewModel.onDesignationChange(it) },
        label = { Text("Faculty Designation *") },
        modifier = Modifier.fillMaxWidth().testTag("teacher_designation_input"),
        singleLine = true
    )

    OutlinedTextField(
        value = state.qualification,
        onValueChange = { viewModel.onQualificationChange(it) },
        label = { Text("Highest Academic Qualifications *") },
        modifier = Modifier.fillMaxWidth().testTag("teacher_qualification_input"),
        singleLine = true
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(EduSpacing.md)
    ) {
        OutlinedTextField(
            value = state.experienceYears,
            onValueChange = { viewModel.onExperienceYearsChange(it) },
            label = { Text("Experience (Years)") },
            modifier = Modifier.weight(1f).testTag("teacher_experience_input"),
            singleLine = true
        )
        OutlinedTextField(
            value = state.joiningDate,
            onValueChange = { viewModel.onJoiningDateChange(it) },
            label = { Text("Joining Date") },
            modifier = Modifier.weight(1f).testTag("teacher_joining_input"),
            singleLine = true
        )
    }

    OutlinedTextField(
        value = state.salaryTier,
        onValueChange = { viewModel.onSalaryTierChange(it) },
        label = { Text("Faculty Salary Tier") },
        modifier = Modifier.fillMaxWidth().testTag("teacher_salary_tier_input"),
        singleLine = true
    )
}

@Composable
private fun Step2SubjectClassAllocation(
    state: TeacherOnboardingUiState,
    viewModel: TeacherOnboardingViewModel
) {
    OutlinedTextField(
        value = state.primarySubject,
        onValueChange = { viewModel.onPrimarySubjectChange(it) },
        label = { Text("Primary Teaching Subject *") },
        modifier = Modifier.fillMaxWidth().testTag("teacher_primary_subject_input"),
        singleLine = true
    )

    OutlinedTextField(
        value = state.additionalSubjectsInput,
        onValueChange = { viewModel.onAdditionalSubjectsInputChange(it) },
        label = { Text("Additional Subjects (comma separated)") },
        modifier = Modifier.fillMaxWidth().testTag("teacher_additional_subjects_input"),
        singleLine = true
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Assign as Class In-Charge",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Appoint faculty member as primary Class Teacher",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = state.isClassTeacher,
                    onCheckedChange = { viewModel.onIsClassTeacherToggle(it) },
                    modifier = Modifier.testTag("teacher_is_class_teacher_switch")
                )
            }

            if (state.isClassTeacher) {
                Spacer(modifier = Modifier.height(EduSpacing.md))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(EduSpacing.md)
                ) {
                    OutlinedTextField(
                        value = state.assignedGrade,
                        onValueChange = { viewModel.onAssignedGradeChange(it) },
                        label = { Text("Assigned Grade") },
                        modifier = Modifier.weight(1f).testTag("teacher_assigned_grade_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = state.assignedSection,
                        onValueChange = { viewModel.onAssignedSectionChange(it) },
                        label = { Text("Assigned Section") },
                        modifier = Modifier.weight(1f).testTag("teacher_assigned_section_input"),
                        singleLine = true
                    )
                }
            }
        }
    }
}

@Composable
private fun Step3ReviewConfirm(state: TeacherOnboardingUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Faculty Profile Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(EduSpacing.sm))

            SummaryRow("Full Name", "${state.firstName} ${state.lastName}")
            SummaryRow("Employee ID", state.employeeId)
            SummaryRow("Department", state.department)
            SummaryRow("Designation", state.designation)
            SummaryRow("Primary Subject", state.primarySubject)
            SummaryRow("Qualifications", state.qualification)
            SummaryRow("Experience", "${state.experienceYears} Years")
            SummaryRow("Class In-Charge", if (state.isClassTeacher) "${state.assignedGrade} - ${state.assignedSection}" else "None")
            SummaryRow("Email", state.email)
            SummaryRow("Phone", state.phone)
            SummaryRow("Joining Date", state.joiningDate)
            SummaryRow("Salary Tier", state.salaryTier)
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}
