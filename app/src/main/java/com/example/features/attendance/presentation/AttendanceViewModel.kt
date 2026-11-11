package com.example.features.attendance.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.security.Permission
import com.example.core.security.TenantContext
import com.example.core.tenant.TenantPathProvider
import com.example.features.attendance.data.AttendanceRepository
import com.example.features.attendance.domain.AttendanceAuditLog
import com.example.features.attendance.domain.AttendanceRecord
import com.example.features.attendance.domain.AttendanceStatus
import com.example.features.attendance.domain.StudentAttendanceEntry
import com.example.features.classes.data.ClassRepository
import com.example.features.classes.domain.AcademicClass
import com.example.features.classes.domain.ClassSection
import com.example.features.students.data.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class AttendanceUiState(
    val selectedDate: String = "2026-03-24",
    val availableClasses: List<AcademicClass> = emptyList(),
    val availableSections: List<ClassSection> = emptyList(),
    val selectedClassId: String? = null,
    val selectedSectionId: String? = null,
    val activeRecord: AttendanceRecord? = null,
    val editableEntries: List<StudentAttendanceEntry> = emptyList(),
    val isFinalized: Boolean = false,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val statusMessage: String? = null,
    val errorMessage: String? = null,
    val showAuditDialog: Boolean = false,
    val showRemarksDialog: Boolean = false,
    val showReopenDialog: Boolean = false,
    val selectedEntryForRemarks: StudentAttendanceEntry? = null,
    val remarksInput: String = "",
    val totalEnrolled: Int = 0,
    val presentCount: Int = 0,
    val absentCount: Int = 0,
    val lateCount: Int = 0,
    val excusedCount: Int = 0,
    val presentPercentage: Float = 0.0f
) {
    val filteredEntries: List<StudentAttendanceEntry>
        get() = if (searchQuery.isBlank()) {
            editableEntries
        } else {
            val query = searchQuery.trim().lowercase()
            editableEntries.filter {
                it.studentName.lowercase().contains(query) ||
                it.rollNumber.lowercase().contains(query) ||
                it.status.displayName.lowercase().contains(query)
            }
        }
}

class AttendanceViewModel(
    private val attendanceRepository: AttendanceRepository = AttendanceRepository(),
    private val classRepository: ClassRepository = ClassRepository(),
    private val studentRepository: StudentRepository = StudentRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AttendanceUiState())
    val uiState: StateFlow<AttendanceUiState> = _uiState.asStateFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    init {
        // Synchronously initialize defaults for JVM test determinism and instant rendering
        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        val initialClasses = classRepository.getPreSeededClasses(schoolId)
        val initialSections = classRepository.getPreSeededSections(schoolId)
        val defaultClass = initialClasses.firstOrNull { it.id.contains("g10") } ?: initialClasses.firstOrNull()
        val defaultSection = initialSections.firstOrNull { it.classId == defaultClass?.id } ?: initialSections.firstOrNull()

        val seededRecords = attendanceRepository.getPreSeededRecords(schoolId)
        val todayRecord = seededRecords.firstOrNull {
            it.date == "2026-03-24" && (defaultSection == null || it.sectionId == defaultSection.id)
        } ?: seededRecords.firstOrNull()

        if (todayRecord != null) {
            val metrics = computeMetrics(todayRecord.entries)
            _uiState.value = AttendanceUiState(
                selectedDate = todayRecord.date,
                availableClasses = initialClasses,
                availableSections = initialSections.filter { it.classId == todayRecord.classId },
                selectedClassId = todayRecord.classId,
                selectedSectionId = todayRecord.sectionId,
                activeRecord = todayRecord,
                editableEntries = todayRecord.entries,
                isFinalized = todayRecord.isFinalized,
                totalEnrolled = metrics.total,
                presentCount = metrics.present,
                absentCount = metrics.absent,
                lateCount = metrics.late,
                excusedCount = metrics.excused,
                presentPercentage = metrics.percentage
            )
        } else {
            _uiState.value = AttendanceUiState(
                selectedDate = "2026-03-24",
                availableClasses = initialClasses,
                availableSections = initialSections,
                selectedClassId = defaultClass?.id,
                selectedSectionId = defaultSection?.id
            )
        }

        loadDataAsync()
    }

    fun loadDataAsync() {
        viewModelScope.launch {
            try {
                val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
                val classes = classRepository.getClasses(schoolId)
                val sections = classRepository.getSections(schoolId)

                val selectedClassId = _uiState.value.selectedClassId ?: classes.firstOrNull()?.id
                val filteredSections = sections.filter { it.classId == selectedClassId }
                val selectedSectionId = _uiState.value.selectedSectionId ?: filteredSections.firstOrNull()?.id

                _uiState.update {
                    it.copy(
                        availableClasses = classes,
                        availableSections = filteredSections,
                        selectedClassId = selectedClassId,
                        selectedSectionId = selectedSectionId
                    )
                }

                loadRosterForSelection()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun onDateChanged(newDate: String) {
        _uiState.update { it.copy(selectedDate = newDate) }
        loadRosterForSelection()
    }

    fun onPreviousDay() {
        val current = try {
            dateFormat.parse(_uiState.value.selectedDate) ?: Date()
        } catch (e: Exception) {
            Date()
        }
        val cal = Calendar.getInstance().apply {
            time = current
            add(Calendar.DAY_OF_YEAR, -1)
        }
        onDateChanged(dateFormat.format(cal.time))
    }

    fun onNextDay() {
        val current = try {
            dateFormat.parse(_uiState.value.selectedDate) ?: Date()
        } catch (e: Exception) {
            Date()
        }
        val cal = Calendar.getInstance().apply {
            time = current
            add(Calendar.DAY_OF_YEAR, 1)
        }
        onDateChanged(dateFormat.format(cal.time))
    }

    fun onClassSelected(classId: String) {
        viewModelScope.launch {
            val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
            val sections = classRepository.getSections(schoolId, classId)
            val newSectionId = sections.firstOrNull()?.id
            _uiState.update {
                it.copy(
                    selectedClassId = classId,
                    availableSections = sections,
                    selectedSectionId = newSectionId
                )
            }
            loadRosterForSelection()
        }
    }

    fun onSectionSelected(sectionId: String) {
        _uiState.update { it.copy(selectedSectionId = sectionId) }
        loadRosterForSelection()
    }

    private fun loadRosterForSelection() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
                val date = _uiState.value.selectedDate
                val classId = _uiState.value.selectedClassId
                val sectionId = _uiState.value.selectedSectionId

                val records = attendanceRepository.getAttendanceRecords(schoolId, date, classId, sectionId)
                val existingRecord = records.firstOrNull()

                if (existingRecord != null) {
                    val metrics = computeMetrics(existingRecord.entries)
                    _uiState.update {
                        it.copy(
                            activeRecord = existingRecord,
                            editableEntries = existingRecord.entries,
                            isFinalized = existingRecord.isFinalized,
                            totalEnrolled = metrics.total,
                            presentCount = metrics.present,
                            absentCount = metrics.absent,
                            lateCount = metrics.late,
                            excusedCount = metrics.excused,
                            presentPercentage = metrics.percentage,
                            isLoading = false
                        )
                    }
                } else {
                    // Generate fresh roster draft from enrolled students
                    val allStudents = studentRepository.getStudents(schoolId)
                    val selectedClass = _uiState.value.availableClasses.firstOrNull { it.id == classId }
                    val selectedSection = _uiState.value.availableSections.firstOrNull { it.id == sectionId }

                    val rosterStudents = allStudents.filter { s ->
                        (selectedClass == null || s.gradeLevel.contains(selectedClass.name, ignoreCase = true) || s.gradeLevel.contains(selectedClass.numericGrade.toString()))
                    }.ifEmpty {
                        allStudents.take(5)
                    }

                    val freshEntries = rosterStudents.map { s ->
                        StudentAttendanceEntry(
                            studentId = s.id,
                            studentName = s.fullName,
                            rollNumber = s.rollNumber,
                            status = AttendanceStatus.PRESENT
                        )
                    }

                    val newRecordId = "att_${schoolId.replace("sch_", "")}_${sectionId ?: "sec"}_${date.replace("-", "")}"
                    val draftRecord = AttendanceRecord.createWithMetrics(
                        id = newRecordId,
                        schoolId = schoolId,
                        date = date,
                        classId = classId ?: "cls_01",
                        className = selectedClass?.name ?: "Grade 10",
                        sectionId = sectionId ?: "sec_01",
                        sectionName = selectedSection?.name ?: "Section A",
                        markedById = TenantContext.activeUserId.value ?: "usr_admin",
                        markedByName = "Faculty Member",
                        entries = freshEntries,
                        isFinalized = false
                    )

                    val metrics = computeMetrics(freshEntries)
                    _uiState.update {
                        it.copy(
                            activeRecord = draftRecord,
                            editableEntries = freshEntries,
                            isFinalized = false,
                            totalEnrolled = metrics.total,
                            presentCount = metrics.present,
                            absentCount = metrics.absent,
                            lateCount = metrics.late,
                            excusedCount = metrics.excused,
                            presentPercentage = metrics.percentage,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun updateStudentStatus(studentId: String, newStatus: AttendanceStatus) {
        if (_uiState.value.isFinalized) {
            _uiState.update { it.copy(errorMessage = "Attendance register is finalized and locked. Unlock or reopen to modify entries.") }
            return
        }
        val currentEntries = _uiState.value.editableEntries.map { entry ->
            if (entry.studentId == studentId) {
                entry.copy(status = newStatus, timestamp = System.currentTimeMillis())
            } else entry
        }
        val metrics = computeMetrics(currentEntries)
        _uiState.update {
            it.copy(
                editableEntries = currentEntries,
                presentCount = metrics.present,
                absentCount = metrics.absent,
                lateCount = metrics.late,
                excusedCount = metrics.excused,
                presentPercentage = metrics.percentage,
                statusMessage = null
            )
        }
    }

    fun markAllPresent() {
        if (_uiState.value.isFinalized) {
            _uiState.update { it.copy(errorMessage = "Register is finalized and locked.") }
            return
        }
        val currentEntries = _uiState.value.editableEntries.map { it.copy(status = AttendanceStatus.PRESENT) }
        val metrics = computeMetrics(currentEntries)
        _uiState.update {
            it.copy(
                editableEntries = currentEntries,
                presentCount = metrics.present,
                absentCount = metrics.absent,
                lateCount = metrics.late,
                excusedCount = metrics.excused,
                presentPercentage = metrics.percentage,
                statusMessage = "All students marked as Present."
            )
        }
    }

    fun markAllAbsent() {
        if (_uiState.value.isFinalized) {
            _uiState.update { it.copy(errorMessage = "Register is finalized and locked.") }
            return
        }
        val currentEntries = _uiState.value.editableEntries.map { it.copy(status = AttendanceStatus.ABSENT) }
        val metrics = computeMetrics(currentEntries)
        _uiState.update {
            it.copy(
                editableEntries = currentEntries,
                presentCount = metrics.present,
                absentCount = metrics.absent,
                lateCount = metrics.late,
                excusedCount = metrics.excused,
                presentPercentage = metrics.percentage,
                statusMessage = "All students marked as Absent."
            )
        }
    }

    fun openRemarksDialog(entry: StudentAttendanceEntry) {
        _uiState.update {
            it.copy(
                showRemarksDialog = true,
                selectedEntryForRemarks = entry,
                remarksInput = entry.remarks
            )
        }
    }

    fun onRemarksInputChanged(remarks: String) {
        _uiState.update { it.copy(remarksInput = remarks) }
    }

    fun saveRemarks() {
        val target = _uiState.value.selectedEntryForRemarks ?: return
        val currentEntries = _uiState.value.editableEntries.map { entry ->
            if (entry.studentId == target.studentId) {
                entry.copy(remarks = _uiState.value.remarksInput.trim())
            } else entry
        }
        _uiState.update {
            it.copy(
                editableEntries = currentEntries,
                showRemarksDialog = false,
                selectedEntryForRemarks = null,
                remarksInput = "",
                statusMessage = "Remarks updated for ${target.studentName}."
            )
        }
    }

    fun closeRemarksDialog() {
        _uiState.update {
            it.copy(
                showRemarksDialog = false,
                selectedEntryForRemarks = null,
                remarksInput = ""
            )
        }
    }

    fun openAuditDialog() {
        _uiState.update { it.copy(showAuditDialog = true) }
    }

    fun closeAuditDialog() {
        _uiState.update { it.copy(showAuditDialog = false) }
    }

    fun openReopenDialog() {
        if (!TenantContext.hasPermission(Permission.ATTENDANCE_EDIT)) {
            _uiState.update { it.copy(errorMessage = "Permission denied: Modifying finalized registers requires ATTENDANCE_EDIT authority.") }
            return
        }
        _uiState.update { it.copy(showReopenDialog = true) }
    }

    fun closeReopenDialog() {
        _uiState.update { it.copy(showReopenDialog = false) }
    }

    fun saveDraft() {
        val active = _uiState.value.activeRecord ?: return
        val entries = _uiState.value.editableEntries
        val schoolId = active.schoolId
        val actorName = TenantContext.activeUserRole.value.name

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val updatedRecord = AttendanceRecord.createWithMetrics(
                id = active.id,
                schoolId = schoolId,
                date = _uiState.value.selectedDate,
                classId = active.classId,
                className = active.className,
                sectionId = active.sectionId,
                sectionName = active.sectionName,
                markedById = TenantContext.activeUserId.value ?: "usr_01",
                markedByName = actorName,
                entries = entries,
                auditTrail = active.auditTrail + AttendanceAuditLog(
                    actorId = TenantContext.activeUserId.value ?: "usr_01",
                    actorName = actorName,
                    action = "DRAFT_SAVED",
                    notes = "Roster draft saved with ${entries.size} students"
                ),
                isFinalized = false
            )
            val result = attendanceRepository.saveAttendanceRecord(updatedRecord)
            result.fold(
                onSuccess = { saved ->
                    _uiState.update {
                        it.copy(
                            activeRecord = saved,
                            isLoading = false,
                            statusMessage = "Draft roster saved successfully."
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = err.message) }
                }
            )
        }
    }

    fun finalizeRegister() {
        if (!TenantContext.hasPermission(Permission.ATTENDANCE_MARK)) {
            _uiState.update { it.copy(errorMessage = "Permission denied: Marking attendance requires ATTENDANCE_MARK role.") }
            return
        }
        val active = _uiState.value.activeRecord ?: return
        val entries = _uiState.value.editableEntries
        val schoolId = active.schoolId
        val actorName = TenantContext.activeUserRole.value.name

        val finalizedRecord = AttendanceRecord.createWithMetrics(
            id = active.id,
            schoolId = schoolId,
            date = _uiState.value.selectedDate,
            classId = active.classId,
            className = active.className,
            sectionId = active.sectionId,
            sectionName = active.sectionName,
            markedById = TenantContext.activeUserId.value ?: "usr_01",
            markedByName = actorName,
            entries = entries,
            auditTrail = active.auditTrail + AttendanceAuditLog(
                actorId = TenantContext.activeUserId.value ?: "usr_01",
                actorName = actorName,
                action = "FINALIZED",
                notes = "Official roll call finalized and verified by $actorName"
            ),
            isFinalized = true
        )

        _uiState.update {
            it.copy(
                activeRecord = finalizedRecord,
                isFinalized = true,
                statusMessage = "Attendance finalized and locked for audit."
            )
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = attendanceRepository.saveAttendanceRecord(finalizedRecord)
            result.fold(
                onSuccess = { saved ->
                    _uiState.update { it.copy(activeRecord = saved, isLoading = false) }
                },
                onFailure = { err ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = err.message) }
                }
            )
        }
    }

    fun reopenRegister(reason: String) {
        if (!TenantContext.hasPermission(Permission.ATTENDANCE_EDIT)) {
            _uiState.update { it.copy(errorMessage = "Permission denied: Reopening finalized attendance requires ATTENDANCE_EDIT role.") }
            return
        }
        val active = _uiState.value.activeRecord ?: return
        val schoolId = active.schoolId
        val actorId = TenantContext.activeUserId.value ?: "usr_01"
        val actorName = TenantContext.activeUserRole.value.name

        val reopenedAudit = AttendanceAuditLog(
            actorId = actorId,
            actorName = actorName,
            action = "REOPENED_FOR_EDIT",
            notes = "Unlocked by authorized user. Reason: ${reason.ifBlank { "Administrative revision" }}"
        )
        val reopenedRecord = active.copy(
            isFinalized = false,
            auditTrail = active.auditTrail + reopenedAudit
        )

        _uiState.update {
            it.copy(
                activeRecord = reopenedRecord,
                isFinalized = false,
                showReopenDialog = false,
                statusMessage = "Register unlocked for edits. Audit entry logged."
            )
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = attendanceRepository.saveAttendanceRecord(reopenedRecord)
            result.fold(
                onSuccess = { saved ->
                    _uiState.update { it.copy(activeRecord = saved, isLoading = false) }
                },
                onFailure = { err ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = err.message) }
                }
            )
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private data class Metrics(
        val total: Int,
        val present: Int,
        val absent: Int,
        val late: Int,
        val excused: Int,
        val percentage: Float
    )

    private fun computeMetrics(entries: List<StudentAttendanceEntry>): Metrics {
        val total = entries.size
        val present = entries.count { it.status == AttendanceStatus.PRESENT }
        val absent = entries.count { it.status == AttendanceStatus.ABSENT }
        val late = entries.count { it.status == AttendanceStatus.LATE }
        val excused = entries.count { it.status == AttendanceStatus.EXCUSED }
        val effectivePresent = present + late
        val percentage = if (total > 0) {
            ((effectivePresent.toFloat() / total.toFloat()) * 1000f).toInt() / 10f
        } else 0.0f
        return Metrics(total, present, absent, late, excused, percentage)
    }
}
