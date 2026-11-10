package com.example.features.students.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.security.Permission
import com.example.core.security.TenantContext
import com.example.core.tenant.TenantPathProvider
import com.example.features.students.data.StudentRepository
import com.example.features.students.domain.StudentProfile
import com.example.features.students.domain.StudentStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class StudentSortOption(val displayName: String) {
    NAME_ASC("Name (A-Z)"),
    NAME_DESC("Name (Z-A)"),
    ROLL_ASC("Roll Number"),
    GRADE_ASC("Grade Level")
}

data class StudentListUiState(
    val students: List<StudentProfile> = emptyList(),
    val filteredStudents: List<StudentProfile> = emptyList(),
    val searchQuery: String = "",
    val selectedGrade: String = "All",
    val selectedSection: String = "All",
    val selectedStatus: String = "All",
    val sortOption: StudentSortOption = StudentSortOption.NAME_ASC,
    val availableGrades: List<String> = listOf("All", "Grade 9", "Grade 10", "Grade 11", "Grade 12"),
    val availableSections: List<String> = listOf("All", "Section A", "Section B", "Section C", "Science"),
    val availableStatuses: List<String> = listOf("All", "ACTIVE", "INACTIVE", "SUSPENDED", "TRANSFERRED"),
    val selectedStudentDetail: StudentProfile? = null,
    val studentToDelete: StudentProfile? = null,
    val showDeleteConfirmDialog: Boolean = false,
    val showEnrollmentWizard: Boolean = false,
    val totalStudentsCount: Int = 0,
    val activeCount: Int = 0,
    val femaleCount: Int = 0,
    val maleCount: Int = 0,
    val isLoading: Boolean = false,
    val notificationFeedback: String? = null
)

class StudentListViewModel(
    private val repositoryProvider: ((Context?) -> StudentRepository)? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentListUiState())
    val uiState: StateFlow<StudentListUiState> = _uiState.asStateFlow()

    private var activeSchoolId: String = "sch_oxford_01"

    init {
        val repo = repositoryProvider?.invoke(null) ?: StudentRepository()
        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        activeSchoolId = schoolId
        val initialList = repo.getPreSeededStudents(schoolId)
        val initialFiltered = applyFiltersAndSort(
            source = initialList,
            query = "",
            grade = "All",
            section = "All",
            status = "All",
            sort = StudentSortOption.NAME_ASC
        )
        _uiState.update {
            it.copy(
                students = initialList,
                filteredStudents = initialFiltered,
                totalStudentsCount = initialList.size,
                activeCount = initialList.count { s -> s.status == StudentStatus.ACTIVE },
                femaleCount = initialList.count { s -> s.gender.equals("Female", ignoreCase = true) },
                maleCount = initialList.count { s -> s.gender.equals("Male", ignoreCase = true) }
            )
        }
        loadStudents()
    }

    fun loadStudents(context: Context? = null) {
        val repo = repositoryProvider?.invoke(context) ?: StudentRepository(context)
        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        activeSchoolId = schoolId

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val list = repo.getStudents(schoolId)
            _uiState.update { state ->
                val filtered = applyFiltersAndSort(
                    source = list,
                    query = state.searchQuery,
                    grade = state.selectedGrade,
                    section = state.selectedSection,
                    status = state.selectedStatus,
                    sort = state.sortOption
                )
                val active = list.count { it.status == StudentStatus.ACTIVE }
                val female = list.count { it.gender.equals("Female", ignoreCase = true) }
                val male = list.count { it.gender.equals("Male", ignoreCase = true) }

                state.copy(
                    isLoading = false,
                    students = list,
                    filteredStudents = filtered,
                    totalStudentsCount = list.size,
                    activeCount = active,
                    femaleCount = female,
                    maleCount = male
                )
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { state ->
            val filtered = applyFiltersAndSort(
                source = state.students,
                query = query,
                grade = state.selectedGrade,
                section = state.selectedSection,
                status = state.selectedStatus,
                sort = state.sortOption
            )
            state.copy(searchQuery = query, filteredStudents = filtered)
        }
    }

    fun onGradeFilterChange(grade: String) {
        _uiState.update { state ->
            val filtered = applyFiltersAndSort(
                source = state.students,
                query = state.searchQuery,
                grade = grade,
                section = state.selectedSection,
                status = state.selectedStatus,
                sort = state.sortOption
            )
            state.copy(selectedGrade = grade, filteredStudents = filtered)
        }
    }

    fun onSectionFilterChange(section: String) {
        _uiState.update { state ->
            val filtered = applyFiltersAndSort(
                source = state.students,
                query = state.searchQuery,
                grade = state.selectedGrade,
                section = section,
                status = state.selectedStatus,
                sort = state.sortOption
            )
            state.copy(selectedSection = section, filteredStudents = filtered)
        }
    }

    fun onStatusFilterChange(status: String) {
        _uiState.update { state ->
            val filtered = applyFiltersAndSort(
                source = state.students,
                query = state.searchQuery,
                grade = state.selectedGrade,
                section = state.selectedSection,
                status = status,
                sort = state.sortOption
            )
            state.copy(selectedStatus = status, filteredStudents = filtered)
        }
    }

    fun onSortChange(option: StudentSortOption) {
        _uiState.update { state ->
            val filtered = applyFiltersAndSort(
                source = state.students,
                query = state.searchQuery,
                grade = state.selectedGrade,
                section = state.selectedSection,
                status = state.selectedStatus,
                sort = option
            )
            state.copy(sortOption = option, filteredStudents = filtered)
        }
    }

    fun selectStudentForDetail(student: StudentProfile) {
        _uiState.update { it.copy(selectedStudentDetail = student) }
    }

    fun closeStudentDetail() {
        _uiState.update { it.copy(selectedStudentDetail = null) }
    }

    fun promptDeleteStudent(student: StudentProfile) {
        if (!TenantContext.hasPermission(Permission.STUDENTS_DELETE)) {
            _uiState.update {
                it.copy(notificationFeedback = "Access Denied: You do not have permission to delete student records.")
            }
            return
        }
        _uiState.update {
            it.copy(
                studentToDelete = student,
                showDeleteConfirmDialog = true
            )
        }
    }

    fun cancelDelete() {
        _uiState.update {
            it.copy(
                studentToDelete = null,
                showDeleteConfirmDialog = false
            )
        }
    }

    fun confirmDeleteStudent(context: Context? = null) {
        val target = _uiState.value.studentToDelete ?: return
        val repo = repositoryProvider?.invoke(context) ?: StudentRepository(context)
        val schoolId = target.schoolId

        viewModelScope.launch {
            val result = repo.deleteStudent(schoolId, target.id)
            if (result.isSuccess) {
                val updatedList = _uiState.value.students.filter { it.id != target.id }
                val filtered = applyFiltersAndSort(
                    source = updatedList,
                    query = _uiState.value.searchQuery,
                    grade = _uiState.value.selectedGrade,
                    section = _uiState.value.selectedSection,
                    status = _uiState.value.selectedStatus,
                    sort = _uiState.value.sortOption
                )
                _uiState.update {
                    it.copy(
                        students = updatedList,
                        filteredStudents = filtered,
                        totalStudentsCount = updatedList.size,
                        studentToDelete = null,
                        showDeleteConfirmDialog = false,
                        selectedStudentDetail = null,
                        notificationFeedback = "Student ${target.fullName} archived successfully."
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        showDeleteConfirmDialog = false,
                        notificationFeedback = "Failed to delete student: ${result.exceptionOrNull()?.message}"
                    )
                }
            }
        }
    }

    fun openEnrollmentWizard() {
        if (!TenantContext.hasPermission(Permission.STUDENTS_CREATE)) {
            _uiState.update {
                it.copy(notificationFeedback = "Access Denied: You do not have permission to enroll students.")
            }
            return
        }
        _uiState.update { it.copy(showEnrollmentWizard = true) }
    }

    fun closeEnrollmentWizard() {
        _uiState.update { it.copy(showEnrollmentWizard = false) }
    }

    fun onStudentEnrolled(newStudent: StudentProfile) {
        val updatedList = listOf(newStudent) + _uiState.value.students.filter { it.id != newStudent.id }
        val filtered = applyFiltersAndSort(
            source = updatedList,
            query = _uiState.value.searchQuery,
            grade = _uiState.value.selectedGrade,
            section = _uiState.value.selectedSection,
            status = _uiState.value.selectedStatus,
            sort = _uiState.value.sortOption
        )
        _uiState.update {
            it.copy(
                students = updatedList,
                filteredStudents = filtered,
                totalStudentsCount = updatedList.size,
                activeCount = updatedList.count { s -> s.status == StudentStatus.ACTIVE },
                showEnrollmentWizard = false,
                notificationFeedback = "Student ${newStudent.fullName} successfully enrolled (${newStudent.rollNumber})."
            )
        }
    }

    fun dismissFeedback() {
        _uiState.update { it.copy(notificationFeedback = null) }
    }

    fun clearFilters() {
        _uiState.update { state ->
            val filtered = applyFiltersAndSort(
                source = state.students,
                query = "",
                grade = "All",
                section = "All",
                status = "All",
                sort = StudentSortOption.NAME_ASC
            )
            state.copy(
                searchQuery = "",
                selectedGrade = "All",
                selectedSection = "All",
                selectedStatus = "All",
                sortOption = StudentSortOption.NAME_ASC,
                filteredStudents = filtered
            )
        }
    }

    private fun applyFiltersAndSort(
        source: List<StudentProfile>,
        query: String,
        grade: String,
        section: String,
        status: String,
        sort: StudentSortOption
    ): List<StudentProfile> {
        return source.filter { student ->
            val matchesQuery = query.isBlank() ||
                student.fullName.contains(query, ignoreCase = true) ||
                student.rollNumber.contains(query, ignoreCase = true) ||
                student.admissionNumber.contains(query, ignoreCase = true) ||
                student.guardianName.contains(query, ignoreCase = true) ||
                student.guardianPhone.contains(query, ignoreCase = true)

            val matchesGrade = grade == "All" || student.gradeLevel.equals(grade, ignoreCase = true)
            val matchesSection = section == "All" || student.section.equals(section, ignoreCase = true)
            val matchesStatus = status == "All" || student.status.name.equals(status, ignoreCase = true)

            matchesQuery && matchesGrade && matchesSection && matchesStatus
        }.let { list ->
            when (sort) {
                StudentSortOption.NAME_ASC -> list.sortedBy { it.fullName }
                StudentSortOption.NAME_DESC -> list.sortedByDescending { it.fullName }
                StudentSortOption.ROLL_ASC -> list.sortedBy { it.rollNumber }
                StudentSortOption.GRADE_ASC -> list.sortedBy { it.gradeLevel }
            }
        }
    }
}
