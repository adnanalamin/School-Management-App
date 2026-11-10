package com.example.features.teachers.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.security.TenantContext
import com.example.features.teachers.data.TeacherRepository
import com.example.features.teachers.domain.TeacherProfile
import com.example.features.teachers.domain.TeacherStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class TeacherOnboardingUiState(
    val currentStep: Int = 0,
    // Step 0: Personal & Contact
    val firstName: String = "",
    val lastName: String = "",
    val gender: String = "Female",
    val dateOfBirth: String = "1990-05-15",
    val email: String = "",
    val phone: String = "",
    val emergencyContact: String = "",
    val address: String = "",
    val bloodGroup: String = "O+",
    // Step 1: Academic & Employment
    val department: String = "Science & Mathematics",
    val designation: String = "Senior Faculty",
    val qualification: String = "M.Sc. Physics, B.Ed.",
    val experienceYears: String = "6",
    val joiningDate: String = "2026-08-15",
    val salaryTier: String = "Tier 2 - Senior Faculty",
    val employeeId: String = "",
    // Step 2: Subject & Class Allocation
    val primarySubject: String = "Physics",
    val additionalSubjectsInput: String = "Calculus, General Science",
    val isClassTeacher: Boolean = true,
    val assignedGrade: String = "Grade 10",
    val assignedSection: String = "Section A",
    // State indicators
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
)

class TeacherOnboardingViewModel(
    private val repository: TeacherRepository = TeacherRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherOnboardingUiState())
    val uiState: StateFlow<TeacherOnboardingUiState> = _uiState.asStateFlow()

    init {
        generateAutoEmployeeId()
    }

    fun onFirstNameChange(value: String) {
        _uiState.update { it.copy(firstName = value, errorMessage = null) }
    }

    fun onLastNameChange(value: String) {
        _uiState.update { it.copy(lastName = value, errorMessage = null) }
    }

    fun onGenderChange(value: String) {
        _uiState.update { it.copy(gender = value) }
    }

    fun onDateOfBirthChange(value: String) {
        _uiState.update { it.copy(dateOfBirth = value) }
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, errorMessage = null) }
    }

    fun onPhoneChange(value: String) {
        _uiState.update { it.copy(phone = value, errorMessage = null) }
    }

    fun onEmergencyContactChange(value: String) {
        _uiState.update { it.copy(emergencyContact = value) }
    }

    fun onAddressChange(value: String) {
        _uiState.update { it.copy(address = value) }
    }

    fun onBloodGroupChange(value: String) {
        _uiState.update { it.copy(bloodGroup = value) }
    }

    fun onDepartmentChange(value: String) {
        _uiState.update { it.copy(department = value) }
        generateAutoEmployeeId()
    }

    fun onDesignationChange(value: String) {
        _uiState.update { it.copy(designation = value) }
    }

    fun onQualificationChange(value: String) {
        _uiState.update { it.copy(qualification = value, errorMessage = null) }
    }

    fun onExperienceYearsChange(value: String) {
        _uiState.update { it.copy(experienceYears = value) }
    }

    fun onJoiningDateChange(value: String) {
        _uiState.update { it.copy(joiningDate = value) }
    }

    fun onSalaryTierChange(value: String) {
        _uiState.update { it.copy(salaryTier = value) }
    }

    fun onPrimarySubjectChange(value: String) {
        _uiState.update { it.copy(primarySubject = value, errorMessage = null) }
    }

    fun onAdditionalSubjectsInputChange(value: String) {
        _uiState.update { it.copy(additionalSubjectsInput = value) }
    }

    fun onIsClassTeacherToggle(value: Boolean) {
        _uiState.update { it.copy(isClassTeacher = value) }
    }

    fun onAssignedGradeChange(value: String) {
        _uiState.update { it.copy(assignedGrade = value) }
    }

    fun onAssignedSectionChange(value: String) {
        _uiState.update { it.copy(assignedSection = value) }
    }

    private fun generateAutoEmployeeId() {
        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        val empId = repository.generateEmployeeId(schoolId, _uiState.value.department)
        _uiState.update { it.copy(employeeId = empId) }
    }

    fun nextStep(): Boolean {
        val state = _uiState.value
        when (state.currentStep) {
            0 -> {
                if (state.firstName.isBlank()) {
                    _uiState.update { it.copy(errorMessage = "Teacher first name is required.") }
                    return false
                }
                if (state.lastName.isBlank()) {
                    _uiState.update { it.copy(errorMessage = "Teacher last name is required.") }
                    return false
                }
                if (state.email.isBlank()) {
                    _uiState.update { it.copy(errorMessage = "Faculty email is required.") }
                    return false
                }
                if (state.phone.isBlank()) {
                    _uiState.update { it.copy(errorMessage = "Contact phone number is required.") }
                    return false
                }
            }
            1 -> {
                if (state.qualification.isBlank()) {
                    _uiState.update { it.copy(errorMessage = "Academic qualification is required.") }
                    return false
                }
            }
            2 -> {
                if (state.primarySubject.isBlank()) {
                    _uiState.update { it.copy(errorMessage = "Primary subject is required.") }
                    return false
                }
            }
        }
        if (state.currentStep < 3) {
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

    fun submitOnboarding(onSuccess: (TeacherProfile) -> Unit) {
        val state = _uiState.value
        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        val teacherId = "tea_${UUID.randomUUID().toString().take(8)}"

        val additional = state.additionalSubjectsInput
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val newTeacher = TeacherProfile(
            id = teacherId,
            schoolId = schoolId,
            employeeId = state.employeeId.ifBlank { repository.generateEmployeeId(schoolId, state.department) },
            firstName = state.firstName.trim(),
            lastName = state.lastName.trim(),
            fullName = "${state.firstName.trim()} ${state.lastName.trim()}".trim(),
            designation = state.designation.trim(),
            department = state.department.trim(),
            primarySubject = state.primarySubject.trim(),
            additionalSubjects = additional,
            qualification = state.qualification.trim(),
            joiningDate = state.joiningDate,
            experienceYears = state.experienceYears.toIntOrNull() ?: 1,
            email = state.email.trim(),
            phone = state.phone.trim(),
            emergencyContact = state.emergencyContact.trim(),
            address = state.address.trim(),
            assignedClassId = if (state.isClassTeacher) state.assignedGrade else null,
            assignedSectionId = if (state.isClassTeacher) state.assignedSection else null,
            isClassTeacher = state.isClassTeacher,
            bloodGroup = state.bloodGroup,
            salaryTier = state.salaryTier,
            status = TeacherStatus.ACTIVE
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            val result = repository.saveTeacher(newTeacher)
            if (result.isSuccess) {
                _uiState.update { it.copy(isSubmitting = false) }
                onSuccess(newTeacher)
            } else {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = "Failed to save teacher: ${result.exceptionOrNull()?.message}"
                    )
                }
            }
        }
    }
}
