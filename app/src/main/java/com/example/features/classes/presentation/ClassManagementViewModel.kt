package com.example.features.classes.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.security.Permission
import com.example.core.security.TenantContext
import com.example.features.classes.data.ClassRepository
import com.example.features.classes.domain.AcademicClass
import com.example.features.classes.domain.ClassSection
import com.example.features.classes.domain.SubjectAllocation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

enum class ClassManagementTab(val title: String) {
    CLASSES("Classes & Grades"),
    SECTIONS("Sections & Rooms"),
    SUBJECTS("Subject Allocations")
}

data class ClassManagementUiState(
    val classes: List<AcademicClass> = emptyList(),
    val sections: List<ClassSection> = emptyList(),
    val subjectAllocations: List<SubjectAllocation> = emptyList(),
    val filteredClasses: List<AcademicClass> = emptyList(),
    val filteredSections: List<ClassSection> = emptyList(),
    val filteredAllocations: List<SubjectAllocation> = emptyList(),
    val selectedTab: ClassManagementTab = ClassManagementTab.CLASSES,
    val selectedClassFilter: String? = null,
    val searchQuery: String = "",
    val totalClassesCount: Int = 0,
    val totalSectionsCount: Int = 0,
    val totalAllocationsCount: Int = 0,
    val totalEnrolledStudents: Int = 0,
    val classTeachersAssignedCount: Int = 0,
    val showCreateClassDialog: Boolean = false,
    val showCreateSectionDialog: Boolean = false,
    val showAllocateSubjectDialog: Boolean = false,
    val classToDelete: AcademicClass? = null,
    val sectionToDelete: ClassSection? = null,
    val allocationToDelete: SubjectAllocation? = null,
    val selectedSectionForInspection: ClassSection? = null,
    val isLoading: Boolean = false,
    val feedbackMessage: String? = null,
    val errorMessage: String? = null
)

class ClassManagementViewModel(
    private val repository: ClassRepository = ClassRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClassManagementUiState())
    val uiState: StateFlow<ClassManagementUiState> = _uiState.asStateFlow()

    private var activeSchoolId: String = "sch_oxford_01"

    init {
        val currentSchool = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        activeSchoolId = currentSchool
        val initialClasses = repository.getPreSeededClasses(currentSchool)
        val initialSections = repository.getPreSeededSections(currentSchool)
        val initialAllocations = repository.getPreSeededAllocations(currentSchool)
        applyLoadedData(initialClasses, initialSections, initialAllocations)
        loadAcademicDataFromNetwork()
    }

    fun loadAcademicDataFromNetwork() {
        val schoolId = TenantContext.activeSchoolId.value ?: activeSchoolId
        activeSchoolId = schoolId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val classes = repository.getClasses(schoolId)
                val sections = repository.getSections(schoolId)
                val allocations = repository.getSubjectAllocations(schoolId)
                applyLoadedData(classes, sections, allocations)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    private fun applyLoadedData(
        classes: List<AcademicClass>,
        sections: List<ClassSection>,
        allocations: List<SubjectAllocation>
    ) {
        _uiState.update { current ->
            val totalStudents = sections.sumOf { it.currentEnrollment }
            val classTeachers = sections.count { !it.classTeacherId.isNullOrBlank() }
            val (fClasses, fSections, fAllocations) = filterAll(
                classes = classes,
                sections = sections,
                allocations = allocations,
                query = current.searchQuery,
                classFilter = current.selectedClassFilter
            )
            current.copy(
                classes = classes,
                sections = sections,
                subjectAllocations = allocations,
                filteredClasses = fClasses,
                filteredSections = fSections,
                filteredAllocations = fAllocations,
                totalClassesCount = classes.size,
                totalSectionsCount = sections.size,
                totalAllocationsCount = allocations.size,
                totalEnrolledStudents = totalStudents,
                classTeachersAssignedCount = classTeachers,
                isLoading = false
            )
        }
    }

    fun selectTab(tab: ClassManagementTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { current ->
            val (fClasses, fSections, fAllocations) = filterAll(
                classes = current.classes,
                sections = current.sections,
                allocations = current.subjectAllocations,
                query = query,
                classFilter = current.selectedClassFilter
            )
            current.copy(
                searchQuery = query,
                filteredClasses = fClasses,
                filteredSections = fSections,
                filteredAllocations = fAllocations
            )
        }
    }

    fun onClassFilterChange(classId: String?) {
        _uiState.update { current ->
            val newFilter = if (current.selectedClassFilter == classId) null else classId
            val (fClasses, fSections, fAllocations) = filterAll(
                classes = current.classes,
                sections = current.sections,
                allocations = current.subjectAllocations,
                query = current.searchQuery,
                classFilter = newFilter
            )
            current.copy(
                selectedClassFilter = newFilter,
                filteredClasses = fClasses,
                filteredSections = fSections,
                filteredAllocations = fAllocations
            )
        }
    }

    fun clearFilters() {
        _uiState.update { current ->
            val (fClasses, fSections, fAllocations) = filterAll(
                classes = current.classes,
                sections = current.sections,
                allocations = current.subjectAllocations,
                query = "",
                classFilter = null
            )
            current.copy(
                searchQuery = "",
                selectedClassFilter = null,
                filteredClasses = fClasses,
                filteredSections = fSections,
                filteredAllocations = fAllocations
            )
        }
    }

    // Dialog control
    fun openCreateClassDialog() {
        if (!TenantContext.hasPermission(Permission.CLASSES_MANAGE)) {
            _uiState.update { it.copy(errorMessage = "Permission Denied: classes.manage is required.") }
            return
        }
        _uiState.update { it.copy(showCreateClassDialog = true) }
    }

    fun closeCreateClassDialog() {
        _uiState.update { it.copy(showCreateClassDialog = false) }
    }

    fun openCreateSectionDialog(preSelectedClassId: String? = null) {
        if (!TenantContext.hasPermission(Permission.CLASSES_MANAGE)) {
            _uiState.update { it.copy(errorMessage = "Permission Denied: classes.manage is required.") }
            return
        }
        _uiState.update { it.copy(showCreateSectionDialog = true, selectedClassFilter = preSelectedClassId ?: it.selectedClassFilter) }
    }

    fun closeCreateSectionDialog() {
        _uiState.update { it.copy(showCreateSectionDialog = false) }
    }

    fun openAllocateSubjectDialog() {
        if (!TenantContext.hasPermission(Permission.CLASSES_MANAGE)) {
            _uiState.update { it.copy(errorMessage = "Permission Denied: classes.manage is required.") }
            return
        }
        _uiState.update { it.copy(showAllocateSubjectDialog = true) }
    }

    fun closeAllocateSubjectDialog() {
        _uiState.update { it.copy(showAllocateSubjectDialog = false) }
    }

    fun inspectSection(section: ClassSection) {
        _uiState.update { it.copy(selectedSectionForInspection = section) }
    }

    fun closeSectionInspection() {
        _uiState.update { it.copy(selectedSectionForInspection = null) }
    }

    // Creation operations
    fun createClass(name: String, code: String, gradeNum: Int, stream: String) {
        if (name.isBlank() || code.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Class Name and Code are required.") }
            return
        }
        val schoolId = TenantContext.activeSchoolId.value ?: activeSchoolId
        val newClass = AcademicClass(
            id = "cls_${UUID.randomUUID().toString().take(8)}",
            schoolId = schoolId,
            name = name.trim(),
            code = code.trim().uppercase(),
            numericGrade = gradeNum,
            stream = stream.trim().ifBlank { "General" },
            sectionsCount = 0,
            totalStudents = 0
        )
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showCreateClassDialog = false) }
            val result = repository.saveClass(newClass)
            if (result.isSuccess) {
                val updatedClasses = _uiState.value.classes + newClass
                applyLoadedData(updatedClasses, _uiState.value.sections, _uiState.value.subjectAllocations)
                _uiState.update { it.copy(feedbackMessage = "Academic Class ${newClass.name} added successfully.") }
            } else {
                _uiState.update { it.copy(errorMessage = "Failed to create class: ${result.exceptionOrNull()?.message}", isLoading = false) }
            }
        }
    }

    fun createSection(classId: String, sectionName: String, roomNumber: String, capacity: Int, teacherId: String?, teacherName: String?) {
        if (classId.isBlank() || sectionName.isBlank() || roomNumber.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Class, Section Name, and Room Number are required.") }
            return
        }
        val targetClass = _uiState.value.classes.firstOrNull { it.id == classId }
        val className = targetClass?.name ?: "Grade"
        val schoolId = TenantContext.activeSchoolId.value ?: activeSchoolId

        val newSection = ClassSection(
            id = "sec_${UUID.randomUUID().toString().take(8)}",
            schoolId = schoolId,
            classId = classId,
            className = className,
            name = sectionName.trim(),
            roomNumber = roomNumber.trim(),
            capacity = capacity,
            currentEnrollment = 0,
            classTeacherId = teacherId,
            classTeacherName = teacherName
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showCreateSectionDialog = false) }
            val result = repository.saveSection(newSection)
            if (result.isSuccess) {
                val updatedSections = _uiState.value.sections + newSection
                // Update parent class section count
                val updatedClasses = _uiState.value.classes.map { cls ->
                    if (cls.id == classId) cls.copy(sectionsCount = cls.sectionsCount + 1) else cls
                }
                applyLoadedData(updatedClasses, updatedSections, _uiState.value.subjectAllocations)
                _uiState.update { it.copy(feedbackMessage = "Section ${newSection.displayName} added to $roomNumber.") }
            } else {
                _uiState.update { it.copy(errorMessage = "Failed to create section: ${result.exceptionOrNull()?.message}", isLoading = false) }
            }
        }
    }

    fun createSubjectAllocation(
        sectionId: String,
        subjectName: String,
        subjectCode: String,
        teacherId: String,
        teacherName: String,
        periodsPerWeek: Int,
        isElective: Boolean,
        roomNumber: String
    ) {
        if (sectionId.isBlank() || subjectName.isBlank() || subjectCode.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Section, Subject Name, and Subject Code are required.") }
            return
        }
        val section = _uiState.value.sections.firstOrNull { it.id == sectionId }
        val schoolId = TenantContext.activeSchoolId.value ?: activeSchoolId

        val newAlloc = SubjectAllocation(
            id = "sub_${UUID.randomUUID().toString().take(8)}",
            schoolId = schoolId,
            classId = section?.classId ?: "",
            sectionId = sectionId,
            sectionName = section?.displayName ?: "Section",
            subjectName = subjectName.trim(),
            subjectCode = subjectCode.trim().uppercase(),
            teacherId = teacherId,
            teacherName = teacherName,
            periodsPerWeek = periodsPerWeek,
            isElective = isElective,
            roomNumber = roomNumber.trim().ifBlank { section?.roomNumber ?: "Classroom" }
        )

        val updatedAllocations = _uiState.value.subjectAllocations + newAlloc
        applyLoadedData(_uiState.value.classes, _uiState.value.sections, updatedAllocations)
        _uiState.update {
            it.copy(
                showAllocateSubjectDialog = false,
                feedbackMessage = "Subject ${newAlloc.subjectName} allocated to ${newAlloc.teacherName} for ${newAlloc.sectionName}."
            )
        }
    }

    // Delete operations
    fun promptDeleteClass(academicClass: AcademicClass) {
        if (!TenantContext.hasPermission(Permission.CLASSES_MANAGE)) {
            _uiState.update { it.copy(errorMessage = "Permission Denied: classes.manage is required.") }
            return
        }
        _uiState.update { it.copy(classToDelete = academicClass) }
    }

    fun dismissDeleteDialog() {
        _uiState.update { it.copy(classToDelete = null, sectionToDelete = null, allocationToDelete = null) }
    }

    fun confirmDeleteClass() {
        val cls = _uiState.value.classToDelete ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, classToDelete = null) }
            val result = repository.deleteClass(cls.schoolId, cls.id)
            if (result.isSuccess) {
                val updatedClasses = _uiState.value.classes.filter { it.id != cls.id }
                val updatedSections = _uiState.value.sections.filter { it.classId != cls.id }
                val updatedAllocations = _uiState.value.subjectAllocations.filter { it.classId != cls.id }
                applyLoadedData(updatedClasses, updatedSections, updatedAllocations)
                _uiState.update { it.copy(feedbackMessage = "Class ${cls.name} deleted.") }
            } else {
                _uiState.update { it.copy(errorMessage = "Failed to delete class: ${result.exceptionOrNull()?.message}", isLoading = false) }
            }
        }
    }

    fun promptDeleteSection(section: ClassSection) {
        if (!TenantContext.hasPermission(Permission.CLASSES_MANAGE)) {
            _uiState.update { it.copy(errorMessage = "Permission Denied: classes.manage is required.") }
            return
        }
        _uiState.update { it.copy(sectionToDelete = section) }
    }

    fun confirmDeleteSection() {
        val sec = _uiState.value.sectionToDelete ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, sectionToDelete = null) }
            val result = repository.deleteSection(sec.schoolId, sec.id)
            if (result.isSuccess) {
                val updatedSections = _uiState.value.sections.filter { it.id != sec.id }
                val updatedAllocations = _uiState.value.subjectAllocations.filter { it.sectionId != sec.id }
                applyLoadedData(_uiState.value.classes, updatedSections, updatedAllocations)
                _uiState.update { it.copy(feedbackMessage = "Section ${sec.displayName} removed.") }
            } else {
                _uiState.update { it.copy(errorMessage = "Failed to delete section: ${result.exceptionOrNull()?.message}", isLoading = false) }
            }
        }
    }

    fun dismissFeedback() {
        _uiState.update { it.copy(feedbackMessage = null, errorMessage = null) }
    }

    fun hasPermission(permission: Permission): Boolean {
        return TenantContext.hasPermission(permission)
    }

    private fun filterAll(
        classes: List<AcademicClass>,
        sections: List<ClassSection>,
        allocations: List<SubjectAllocation>,
        query: String,
        classFilter: String?
    ): Triple<List<AcademicClass>, List<ClassSection>, List<SubjectAllocation>> {
        val q = query.trim().lowercase()

        val filteredClasses = classes.filter { cls ->
            val matchQ = q.isEmpty() || cls.name.lowercase().contains(q) || cls.code.lowercase().contains(q) || cls.stream.lowercase().contains(q)
            val matchFilter = classFilter == null || cls.id == classFilter
            matchQ && matchFilter
        }

        val filteredSections = sections.filter { sec ->
            val matchQ = q.isEmpty() || sec.displayName.lowercase().contains(q) || sec.roomNumber.lowercase().contains(q) || (sec.classTeacherName?.lowercase()?.contains(q) == true)
            val matchFilter = classFilter == null || sec.classId == classFilter
            matchQ && matchFilter
        }

        val filteredAllocations = allocations.filter { alloc ->
            val matchQ = q.isEmpty() || alloc.subjectName.lowercase().contains(q) || alloc.subjectCode.lowercase().contains(q) || alloc.teacherName.lowercase().contains(q) || alloc.sectionName.lowercase().contains(q)
            val matchFilter = classFilter == null || alloc.classId == classFilter
            matchQ && matchFilter
        }

        return Triple(filteredClasses, filteredSections, filteredAllocations)
    }
}
