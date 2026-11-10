package com.example.features.students.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.theme.EduShapes
import com.example.core.theme.EduSpacing
import com.example.features.students.domain.StudentProfile
import com.example.shared.components.BadgeType
import com.example.shared.components.EduBadge
import com.example.shared.components.EduButton
import com.example.shared.components.EduButtonVariant
import com.example.shared.components.EduCard

@Composable
fun StudentEnrollmentDialog(
    onDismiss: () -> Unit,
    onStudentEnrolled: (StudentProfile) -> Unit,
    viewModel: StudentEnrollmentViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.size(EduSpacing.xs))
                        Text(
                            text = "Student Admission Wizard",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("btn_close_enrollment_wizard")) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }
                Spacer(modifier = Modifier.height(EduSpacing.xs))
                Text(
                    text = "Step ${uiState.currentStep + 1} of ${uiState.totalSteps}: ${getStepTitle(uiState.currentStep)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { (uiState.currentStep + 1) / uiState.totalSteps.toFloat() },
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(EduSpacing.sm)
            ) {
                if (uiState.errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.errorContainer, shape = EduShapes.small)
                            .padding(EduSpacing.sm)
                    ) {
                        Text(
                            text = uiState.errorMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.testTag("txt_enrollment_error")
                        )
                    }
                }

                when (uiState.currentStep) {
                    0 -> StepZeroAcademicAndPersonal(viewModel, uiState)
                    1 -> StepOneGuardianContacts(viewModel, uiState)
                    2 -> StepTwoHealthAndMedical(viewModel, uiState)
                    3 -> StepThreeReviewAndFinalize(viewModel, uiState)
                }
            }
        },
        confirmButton = {
            if (uiState.currentStep < uiState.totalSteps - 1) {
                EduButton(
                    text = "Next Step",
                    onClick = { viewModel.nextStep() },
                    variant = EduButtonVariant.PRIMARY,
                    testTag = "btn_wizard_next"
                )
            } else {
                EduButton(
                    text = if (uiState.isSubmitting) "Enrolling..." else "Complete Admission",
                    onClick = {
                        viewModel.enrollStudent(context) { enrolled ->
                            onStudentEnrolled(enrolled)
                        }
                    },
                    variant = EduButtonVariant.PRIMARY,
                    enabled = !uiState.isSubmitting,
                    testTag = "btn_wizard_submit"
                )
            }
        },
        dismissButton = {
            if (uiState.currentStep > 0) {
                EduButton(
                    text = "Previous",
                    onClick = { viewModel.previousStep() },
                    variant = EduButtonVariant.OUTLINED,
                    testTag = "btn_wizard_previous"
                )
            } else {
                EduButton(
                    text = "Cancel",
                    onClick = onDismiss,
                    variant = EduButtonVariant.OUTLINED,
                    testTag = "btn_wizard_cancel"
                )
            }
        },
        shape = EduShapes.large,
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
private fun StepZeroAcademicAndPersonal(
    viewModel: StudentEnrollmentViewModel,
    uiState: StudentEnrollmentUiState
) {
    Column(verticalArrangement = Arrangement.spacedBy(EduSpacing.xs)) {
        OutlinedTextField(
            value = uiState.firstName,
            onValueChange = { viewModel.onFirstNameChange(it) },
            label = { Text("First Name *") },
            modifier = Modifier.fillMaxWidth().testTag("input_student_first_name"),
            shape = EduShapes.small,
            singleLine = true
        )

        OutlinedTextField(
            value = uiState.lastName,
            onValueChange = { viewModel.onLastNameChange(it) },
            label = { Text("Last Name *") },
            modifier = Modifier.fillMaxWidth().testTag("input_student_last_name"),
            shape = EduShapes.small,
            singleLine = true
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(EduSpacing.xs)) {
            OutlinedTextField(
                value = uiState.dateOfBirth,
                onValueChange = { viewModel.onDateOfBirthChange(it) },
                label = { Text("Date of Birth") },
                placeholder = { Text("YYYY-MM-DD") },
                modifier = Modifier.weight(1f).testTag("input_student_dob"),
                shape = EduShapes.small,
                singleLine = true
            )
            OutlinedTextField(
                value = uiState.gender,
                onValueChange = { viewModel.onGenderChange(it) },
                label = { Text("Gender") },
                modifier = Modifier.weight(1f).testTag("input_student_gender"),
                shape = EduShapes.small,
                singleLine = true
            )
        }

        Text(text = "Grade Level & Section", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(EduSpacing.xs)
        ) {
            listOf("Grade 9", "Grade 10", "Grade 11", "Grade 12").forEach { grade ->
                FilterChip(
                    selected = uiState.gradeLevel == grade,
                    onClick = { viewModel.onGradeLevelChange(grade) },
                    label = { Text(grade) },
                    modifier = Modifier.testTag("chip_grade_$grade")
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(EduSpacing.xs)
        ) {
            listOf("Section A", "Section B", "Section C", "Science").forEach { sec ->
                FilterChip(
                    selected = uiState.section == sec,
                    onClick = { viewModel.onSectionChange(sec) },
                    label = { Text(sec) },
                    modifier = Modifier.testTag("chip_sec_$sec")
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "System Identifiers", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            IconButton(onClick = { viewModel.autoGenerateNumbers() }, modifier = Modifier.size(24.dp).testTag("btn_refresh_roll")) {
                Icon(imageVector = Icons.Default.Autorenew, contentDescription = "Regenerate Numbers", tint = MaterialTheme.colorScheme.primary)
            }
        }

        OutlinedTextField(
            value = uiState.rollNumber,
            onValueChange = { viewModel.onRollNumberChange(it) },
            label = { Text("Roll Number *") },
            modifier = Modifier.fillMaxWidth().testTag("input_student_roll"),
            shape = EduShapes.small,
            singleLine = true
        )

        OutlinedTextField(
            value = uiState.admissionNumber,
            onValueChange = { viewModel.onAdmissionNumberChange(it) },
            label = { Text("Admission Code") },
            modifier = Modifier.fillMaxWidth().testTag("input_student_admission"),
            shape = EduShapes.small,
            singleLine = true
        )
    }
}

@Composable
private fun StepOneGuardianContacts(
    viewModel: StudentEnrollmentViewModel,
    uiState: StudentEnrollmentUiState
) {
    Column(verticalArrangement = Arrangement.spacedBy(EduSpacing.xs)) {
        OutlinedTextField(
            value = uiState.guardianName,
            onValueChange = { viewModel.onGuardianNameChange(it) },
            label = { Text("Guardian Full Name *") },
            modifier = Modifier.fillMaxWidth().testTag("input_guardian_name"),
            shape = EduShapes.small,
            singleLine = true
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(EduSpacing.xs)) {
            OutlinedTextField(
                value = uiState.guardianRelationship,
                onValueChange = { viewModel.onGuardianRelationshipChange(it) },
                label = { Text("Relationship") },
                placeholder = { Text("Father, Mother...") },
                modifier = Modifier.weight(1f).testTag("input_guardian_rel"),
                shape = EduShapes.small,
                singleLine = true
            )
            OutlinedTextField(
                value = uiState.guardianPhone,
                onValueChange = { viewModel.onGuardianPhoneChange(it) },
                label = { Text("Telephone *") },
                placeholder = { Text("+1 555-0100") },
                modifier = Modifier.weight(1f).testTag("input_guardian_phone"),
                shape = EduShapes.small,
                singleLine = true
            )
        }

        OutlinedTextField(
            value = uiState.guardianEmail,
            onValueChange = { viewModel.onGuardianEmailChange(it) },
            label = { Text("Guardian Email") },
            placeholder = { Text("guardian@example.com") },
            modifier = Modifier.fillMaxWidth().testTag("input_guardian_email"),
            shape = EduShapes.small,
            singleLine = true
        )

        OutlinedTextField(
            value = uiState.emergencyContact,
            onValueChange = { viewModel.onEmergencyContactChange(it) },
            label = { Text("Emergency Contact Phone") },
            placeholder = { Text("+1 555-0199") },
            modifier = Modifier.fillMaxWidth().testTag("input_emergency_contact"),
            shape = EduShapes.small,
            singleLine = true
        )

        OutlinedTextField(
            value = uiState.address,
            onValueChange = { viewModel.onAddressChange(it) },
            label = { Text("Home Residential Address *") },
            placeholder = { Text("Street address, City, Postal Code") },
            modifier = Modifier.fillMaxWidth().testTag("input_student_address"),
            shape = EduShapes.small,
            minLines = 2
        )
    }
}

@Composable
private fun StepTwoHealthAndMedical(
    viewModel: StudentEnrollmentViewModel,
    uiState: StudentEnrollmentUiState
) {
    Column(verticalArrangement = Arrangement.spacedBy(EduSpacing.xs)) {
        Text(text = "Blood Group", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(EduSpacing.xs)
        ) {
            listOf("O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-").forEach { bg ->
                FilterChip(
                    selected = uiState.bloodGroup == bg,
                    onClick = { viewModel.onBloodGroupChange(bg) },
                    label = { Text(bg) },
                    modifier = Modifier.testTag("chip_blood_$bg")
                )
            }
        }

        Spacer(modifier = Modifier.height(EduSpacing.xs))
        Text(
            text = "Medical Alerts, Allergies & Dietary Restrictions",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Enter any life-threatening allergies, regular prescriptions (e.g., EpiPen, inhaler), or conditions the school infirmary must know.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = uiState.medicalNotes,
            onValueChange = { viewModel.onMedicalNotesChange(it) },
            label = { Text("Medical Alert Notes") },
            placeholder = { Text("E.g. Peanut allergy, Asthma inhaler kept in bag...") },
            modifier = Modifier.fillMaxWidth().testTag("input_medical_notes"),
            shape = EduShapes.small,
            minLines = 3
        )
    }
}

@Composable
private fun StepThreeReviewAndFinalize(
    viewModel: StudentEnrollmentViewModel,
    uiState: StudentEnrollmentUiState
) {
    Column(verticalArrangement = Arrangement.spacedBy(EduSpacing.xs)) {
        EduCard(testTag = "card_enrollment_summary") {
            Text(text = "${uiState.firstName} ${uiState.lastName}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = "${uiState.gradeLevel} • ${uiState.section} • Roll: ${uiState.rollNumber}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(4.dp))
            ReviewRow("Admission Code", uiState.admissionNumber)
            ReviewRow("Date of Birth", uiState.dateOfBirth)
            ReviewRow("Gender / Blood", "${uiState.gender} • ${uiState.bloodGroup}")
            ReviewRow("Guardian", "${uiState.guardianName} (${uiState.guardianRelationship})")
            ReviewRow("Guardian Phone", uiState.guardianPhone)
            ReviewRow("Emergency Phone", uiState.emergencyContact.ifBlank { uiState.guardianPhone })
            ReviewRow("Medical Notes", uiState.medicalNotes)
        }

        Spacer(modifier = Modifier.height(EduSpacing.xs))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = uiState.hasConfirmedDocuments,
                onCheckedChange = { viewModel.onConfirmedDocumentsToggle(it) },
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("chk_confirm_docs")
            )
            Text(
                text = "Verified birth certificate, previous report cards and residency proof.",
                style = MaterialTheme.typography.bodySmall
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = uiState.hasAcceptedCodeOfConduct,
                onCheckedChange = { viewModel.onAcceptedCodeOfConductToggle(it) },
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("chk_code_conduct")
            )
            Text(
                text = "Student and guardian agree to institutional code of conduct & attendance policy.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun ReviewRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
    }
}

private fun getStepTitle(step: Int): String {
    return when (step) {
        0 -> "Academic & Personal Info"
        1 -> "Guardian Contacts"
        2 -> "Health & Medical Alerts"
        3 -> "Review & Confirmation"
        else -> "Enrollment"
    }
}
