package com.example.features.students.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.security.TenantContext
import com.example.features.students.data.StudentRepository
import com.example.features.students.domain.StudentProfile
import com.example.features.students.domain.StudentStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class StudentEnrollmentUiState(
    val currentStep: Int = 0,
    val totalSteps: Int = 4,

    // Step 0: Academic & Personal
    val firstName: String = "",
    val lastName: String = "",
    val dateOfBirth: String = "2011-03-15",
    val gender: String = "Female",
    val gradeLevel: String = "Grade 10",
    val section: String = "Section A",
    val rollNumber: String = "",
    val admissionNumber: String = "",

    // Step 1: Guardian & Emergency
    val guardianName: String = "",
    val guardianRelationship: String = "Father",
    val guardianPhone: String = "",
    val guardianEmail: String = "",
    val emergencyContact: String = "",
    val address: String = "",

    // Step 2: Health & Medical
    val bloodGroup: String = "O+",
    val medicalNotes: String = "None reported",

    // Step 3: Confirmation Checklist
    val hasConfirmedDocuments: Boolean = true,
    val hasAcceptedCodeOfConduct: Boolean = true,

    // Validation & State
    val errorMessage: String? = null,
    val isSubmitting: Boolean = false
)

class StudentEnrollmentViewModel(
    private val repositoryProvider: ((Context?) -> StudentRepository)? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentEnrollmentUiState())
    val uiState: StateFlow<StudentEnrollmentUiState> = _uiState.asStateFlow()

    init {
        autoGenerateNumbers()
    }

    fun onFirstNameChange(value: String) {
        _uiState.update { it.copy(firstName = value, errorMessage = null) }
    }

    fun onLastNameChange(value: String) {
        _uiState.update { it.copy(lastName = value, errorMessage = null) }
    }

    fun onDateOfBirthChange(value: String) {
        _uiState.update { it.copy(dateOfBirth = value, errorMessage = null) }
    }

    fun onGenderChange(value: String) {
        _uiState.update { it.copy(gender = value) }
    }

    fun onGradeLevelChange(value: String) {
        _uiState.update { it.copy(gradeLevel = value) }
        autoGenerateNumbers()
    }

    fun onSectionChange(value: String) {
        _uiState.update { it.copy(section = value) }
        autoGenerateNumbers()
    }

    fun onRollNumberChange(value: String) {
        _uiState.update { it.copy(rollNumber = value, errorMessage = null) }
    }

    fun onAdmissionNumberChange(value: String) {
        _uiState.update { it.copy(admissionNumber = value, errorMessage = null) }
    }

    fun onGuardianNameChange(value: String) {
        _uiState.update { it.copy(guardianName = value, errorMessage = null) }
    }

    fun onGuardianRelationshipChange(value: String) {
        _uiState.update { it.copy(guardianRelationship = value) }
    }

    fun onGuardianPhoneChange(value: String) {
        _uiState.update { it.copy(guardianPhone = value, errorMessage = null) }
    }

    fun onGuardianEmailChange(value: String) {
        _uiState.update { it.copy(guardianEmail = value, errorMessage = null) }
    }

    fun onEmergencyContactChange(value: String) {
        _uiState.update { it.copy(emergencyContact = value, errorMessage = null) }
    }

    fun onAddressChange(value: String) {
        _uiState.update { it.copy(address = value, errorMessage = null) }
    }

    fun onBloodGroupChange(value: String) {
        _uiState.update { it.copy(bloodGroup = value) }
    }

    fun onMedicalNotesChange(value: String) {
        _uiState.update { it.copy(medicalNotes = value) }
    }

    fun onConfirmedDocumentsToggle(checked: Boolean) {
        _uiState.update { it.copy(hasConfirmedDocuments = checked) }
    }

    fun onAcceptedCodeOfConductToggle(checked: Boolean) {
        _uiState.update { it.copy(hasAcceptedCodeOfConduct = checked) }
    }

    fun autoGenerateNumbers() {
        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        val repo = repositoryProvider?.invoke(null) ?: StudentRepository()
        val roll = repo.generateUniqueRollNumber(schoolId, _uiState.value.gradeLevel, _uiState.value.section)
        val adm = repo.generateAdmissionNumber(schoolId)
        _uiState.update { it.copy(rollNumber = roll, admissionNumber = adm) }
    }

    fun nextStep(): Boolean {
        val state = _uiState.value
        when (state.currentStep) {
            0 -> {
                if (state.firstName.trim().isBlank()) {
                    _uiState.update { it.copy(errorMessage = "Student first name is required.") }
                    return false
                }
                if (state.lastName.trim().isBlank()) {
                    _uiState.update { it.copy(errorMessage = "Student last name is required.") }
                    return false
                }
                if (state.rollNumber.trim().isBlank()) {
                    _uiState.update { it.copy(errorMessage = "Roll number is required.") }
                    return false
                }
            }
            1 -> {
                if (state.guardianName.trim().isBlank()) {
                    _uiState.update { it.copy(errorMessage = "Guardian full name is required.") }
                    return false
                }
                if (state.guardianPhone.trim().isBlank()) {
                    _uiState.update { it.copy(errorMessage = "Guardian contact telephone is required.") }
                    return false
                }
                if (state.address.trim().isBlank()) {
                    _uiState.update { it.copy(errorMessage = "Residential address is required.") }
                    return false
                }
            }
            2 -> {
                // Medical is optional with default "None reported"
            }
        }

        if (state.currentStep < state.totalSteps - 1) {
            _uiState.update { it.copy(currentStep = it.currentStep + 1, errorMessage = null) }
            return true
        }
        return false
    }

    fun previousStep(): Boolean {
        if (_uiState.value.currentStep > 0) {
            _uiState.update { it.copy(currentStep = it.currentStep - 1, errorMessage = null) }
            return true
        }
        return false
    }

    fun enrollStudent(context: Context? = null, onSuccess: (StudentProfile) -> Unit) {
        val state = _uiState.value
        if (!state.hasAcceptedCodeOfConduct || !state.hasConfirmedDocuments) {
            _uiState.update { it.copy(errorMessage = "Please review and accept institutional policies and documents verification.") }
            return
        }

        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        val uniqueId = "stu_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val newProfile = StudentProfile(
            id = uniqueId,
            schoolId = schoolId,
            firstName = state.firstName.trim(),
            lastName = state.lastName.trim(),
            fullName = "${state.firstName.trim()} ${state.lastName.trim()}",
            rollNumber = state.rollNumber.trim(),
            admissionNumber = state.admissionNumber.trim(),
            gradeLevel = state.gradeLevel,
            section = state.section,
            dateOfBirth = state.dateOfBirth,
            gender = state.gender,
            bloodGroup = state.bloodGroup,
            status = StudentStatus.ACTIVE,
            guardianName = state.guardianName.trim(),
            guardianRelationship = state.guardianRelationship,
            guardianPhone = state.guardianPhone.trim(),
            guardianEmail = state.guardianEmail.trim().ifBlank { "guardian@edumanage.com" },
            address = state.address.trim(),
            emergencyContact = state.emergencyContact.trim().ifBlank { state.guardianPhone.trim() },
            medicalNotes = state.medicalNotes.trim().ifBlank { "None reported" },
            enrollmentDate = "2026-08-15"
        )

        val repo = repositoryProvider?.invoke(context) ?: StudentRepository(context)

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val result = repo.saveStudent(newProfile)
            _uiState.update { it.copy(isSubmitting = false) }
            if (result.isSuccess) {
                onSuccess(newProfile)
            } else {
                _uiState.update { it.copy(errorMessage = "Failed to enroll student: ${result.exceptionOrNull()?.message}") }
            }
        }
    }
}
