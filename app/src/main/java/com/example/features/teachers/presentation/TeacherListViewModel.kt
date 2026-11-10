package com.example.features.teachers.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.security.Permission
import com.example.core.security.TenantContext
import com.example.features.teachers.data.TeacherRepository
import com.example.features.teachers.domain.TeacherProfile
import com.example.features.teachers.domain.TeacherStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class TeacherSortOption(val displayName: String) {
    NAME_ASC("Name (A-Z)"),
    NAME_DESC("Name (Z-A)"),
    EXPERIENCE_DESC("Experience (High-Low)"),
    DEPARTMENT_ASC("Department (A-Z)")
}

data class TeacherListUiState(
    val allTeachers: List<TeacherProfile> = emptyList(),
    val filteredTeachers: List<TeacherProfile> = emptyList(),
    val departmentsList: List<String> = emptyList(),
    val selectedDepartment: String? = null,
    val selectedStatus: TeacherStatus? = null,
    val searchQuery: String = "",
    val sortOption: TeacherSortOption = TeacherSortOption.NAME_ASC,
    val totalFacultyCount: Int = 0,
    val activeFacultyCount: Int = 0,
    val classTeachersCount: Int = 0,
    val departmentsCount: Int = 0,
    val selectedTeacherForDetail: TeacherProfile? = null,
    val teacherToArchive: TeacherProfile? = null,
    val showOnboardingDialog: Boolean = false,
    val isLoading: Boolean = false,
    val feedbackMessage: String? = null,
    val errorMessage: String? = null
)

class TeacherListViewModel(
    private val repository: TeacherRepository = TeacherRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherListUiState())
    val uiState: StateFlow<TeacherListUiState> = _uiState.asStateFlow()

    private var activeSchoolId: String = "sch_oxford_01"

    init {
        val currentSchool = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        activeSchoolId = currentSchool
        val initialTeachers = repository.getPreSeededTeachers(currentSchool)
        applyLoadedTeachers(initialTeachers)
        loadTeachersFromNetwork()
    }

    fun loadTeachersFromNetwork() {
        val schoolId = TenantContext.activeSchoolId.value ?: activeSchoolId
        activeSchoolId = schoolId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val teachers = repository.getTeachers(schoolId)
                applyLoadedTeachers(teachers)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    private fun applyLoadedTeachers(teachers: List<TeacherProfile>) {
        val departments = teachers.map { it.department }.distinct().sorted()
        _uiState.update { current ->
            val filtered = filterAndSort(
                teachers = teachers,
                query = current.searchQuery,
                dept = current.selectedDepartment,
                status = current.selectedStatus,
                sort = current.sortOption
            )
            current.copy(
                allTeachers = teachers,
                filteredTeachers = filtered,
                departmentsList = departments,
                totalFacultyCount = teachers.size,
                activeFacultyCount = teachers.count { it.status == TeacherStatus.ACTIVE },
                classTeachersCount = teachers.count { it.isClassTeacher },
                departmentsCount = departments.size,
                isLoading = false
            )
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { current ->
            val filtered = filterAndSort(
                teachers = current.allTeachers,
                query = query,
                dept = current.selectedDepartment,
                status = current.selectedStatus,
                sort = current.sortOption
            )
            current.copy(searchQuery = query, filteredTeachers = filtered)
        }
    }

    fun onDepartmentFilterChange(dept: String?) {
        _uiState.update { current ->
            val newDept = if (current.selectedDepartment == dept) null else dept
            val filtered = filterAndSort(
                teachers = current.allTeachers,
                query = current.searchQuery,
                dept = newDept,
                status = current.selectedStatus,
                sort = current.sortOption
            )
            current.copy(selectedDepartment = newDept, filteredTeachers = filtered)
        }
    }

    fun onStatusFilterChange(status: TeacherStatus?) {
        _uiState.update { current ->
            val newStatus = if (current.selectedStatus == status) null else status
            val filtered = filterAndSort(
                teachers = current.allTeachers,
                query = current.searchQuery,
                dept = current.selectedDepartment,
                status = newStatus,
                sort = current.sortOption
            )
            current.copy(selectedStatus = newStatus, filteredTeachers = filtered)
        }
    }

    fun onSortChange(sort: TeacherSortOption) {
        _uiState.update { current ->
            val filtered = filterAndSort(
                teachers = current.allTeachers,
                query = current.searchQuery,
                dept = current.selectedDepartment,
                status = current.selectedStatus,
                sort = sort
            )
            current.copy(sortOption = sort, filteredTeachers = filtered)
        }
    }

    fun clearFilters() {
        _uiState.update { current ->
            val filtered = filterAndSort(
                teachers = current.allTeachers,
                query = "",
                dept = null,
                status = null,
                sort = TeacherSortOption.NAME_ASC
            )
            current.copy(
                searchQuery = "",
                selectedDepartment = null,
                selectedStatus = null,
                sortOption = TeacherSortOption.NAME_ASC,
                filteredTeachers = filtered
            )
        }
    }

    fun selectTeacherForDetail(teacher: TeacherProfile) {
        _uiState.update { it.copy(selectedTeacherForDetail = teacher) }
    }

    fun clearSelectedTeacher() {
        _uiState.update { it.copy(selectedTeacherForDetail = null) }
    }

    fun promptArchiveTeacher(teacher: TeacherProfile) {
        if (!TenantContext.hasPermission(Permission.TEACHERS_DELETE)) {
            _uiState.update { it.copy(errorMessage = "Permission Denied: teachers.delete is required.") }
            return
        }
        _uiState.update { it.copy(teacherToArchive = teacher) }
    }

    fun dismissArchiveDialog() {
        _uiState.update { it.copy(teacherToArchive = null) }
    }

    fun confirmArchiveTeacher() {
        val teacher = _uiState.value.teacherToArchive ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, teacherToArchive = null) }
            val result = repository.deleteTeacher(teacher.schoolId, teacher.id)
            if (result.isSuccess) {
                val updated = _uiState.value.allTeachers.filter { it.id != teacher.id }
                applyLoadedTeachers(updated)
                _uiState.update {
                    it.copy(
                        feedbackMessage = "Faculty member ${teacher.fullName} archived successfully.",
                        isLoading = false
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        errorMessage = "Failed to archive faculty: ${result.exceptionOrNull()?.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun openOnboardingDialog() {
        if (!TenantContext.hasPermission(Permission.TEACHERS_CREATE)) {
            _uiState.update { it.copy(errorMessage = "Permission Denied: teachers.create is required.") }
            return
        }
        _uiState.update { it.copy(showOnboardingDialog = true) }
    }

    fun closeOnboardingDialog() {
        _uiState.update { it.copy(showOnboardingDialog = false) }
    }

    fun onTeacherOnboarded(newTeacher: TeacherProfile) {
        val updated = listOf(newTeacher) + _uiState.value.allTeachers.filter { it.id != newTeacher.id }
        applyLoadedTeachers(updated)
        _uiState.update {
            it.copy(
                showOnboardingDialog = false,
                feedbackMessage = "Faculty member ${newTeacher.fullName} added to ${newTeacher.department}."
            )
        }
    }

    fun dismissFeedback() {
        _uiState.update { it.copy(feedbackMessage = null, errorMessage = null) }
    }

    fun hasPermission(permission: Permission): Boolean {
        return TenantContext.hasPermission(permission)
    }

    private fun filterAndSort(
        teachers: List<TeacherProfile>,
        query: String,
        dept: String?,
        status: TeacherStatus?,
        sort: TeacherSortOption
    ): List<TeacherProfile> {
        val q = query.trim().lowercase()
        return teachers
            .filter { teacher ->
                val matchesQuery = q.isEmpty() ||
                        teacher.fullName.lowercase().contains(q) ||
                        teacher.employeeId.lowercase().contains(q) ||
                        teacher.primarySubject.lowercase().contains(q) ||
                        teacher.department.lowercase().contains(q)
                val matchesDept = dept == null || teacher.department == dept
                val matchesStatus = status == null || teacher.status == status
                matchesQuery && matchesDept && matchesStatus
            }
            .let { list ->
                when (sort) {
                    TeacherSortOption.NAME_ASC -> list.sortedBy { it.fullName }
                    TeacherSortOption.NAME_DESC -> list.sortedByDescending { it.fullName }
                    TeacherSortOption.EXPERIENCE_DESC -> list.sortedByDescending { it.experienceYears }
                    TeacherSortOption.DEPARTMENT_ASC -> list.sortedWith(compareBy({ it.department }, { it.fullName }))
                }
            }
    }
}
