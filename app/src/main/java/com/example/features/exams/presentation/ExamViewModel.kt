package com.example.features.exams.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.security.Permission
import com.example.core.security.TenantContext
import com.example.features.exams.data.ExamRepository
import com.example.features.exams.domain.ExamPaper
import com.example.features.exams.domain.ExamSchedule
import com.example.features.exams.domain.ExamStatus
import com.example.features.exams.domain.ExamType
import com.example.features.exams.domain.HallTicket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class ExamUiState(
    val schedules: List<ExamSchedule> = emptyList(),
    val searchQuery: String = "",
    val selectedTypeFilter: ExamType? = null,
    val selectedGradeFilter: String? = null,
    val isLoading: Boolean = false,
    val statusMessage: String? = null,
    val errorMessage: String? = null,
    val showCreateScheduleDialog: Boolean = false,
    val showAddPaperDialog: Boolean = false,
    val showHallTicketDialog: Boolean = false,
    val selectedScheduleForPaper: ExamSchedule? = null,
    val selectedScheduleForHallTicket: ExamSchedule? = null,
    val generatedHallTicket: HallTicket? = null,
    val conflictWarning: String? = null
) {
    val filteredSchedules: List<ExamSchedule>
        get() = schedules.filter { sched ->
            val matchesSearch = if (searchQuery.isBlank()) true else {
                val q = searchQuery.trim().lowercase()
                sched.title.lowercase().contains(q) ||
                sched.gradeLevel.lowercase().contains(q) ||
                sched.academicSession.lowercase().contains(q) ||
                sched.papers.any { it.subjectName.lowercase().contains(q) || it.roomNumber.lowercase().contains(q) }
            }
            val matchesType = selectedTypeFilter == null || sched.examType == selectedTypeFilter
            val matchesGrade = selectedGradeFilter == null || sched.gradeLevel.contains(selectedGradeFilter, ignoreCase = true)
            matchesSearch && matchesType && matchesGrade
        }

    val totalSchedules: Int
        get() = schedules.size

    val totalPapers: Int
        get() = schedules.sumOf { it.papers.size }

    val activeRoomsCount: Int
        get() = schedules.flatMap { it.papers }.map { it.roomNumber.trim() }.filter { it.isNotBlank() }.distinct().size

    val scheduledSeriesCount: Int
        get() = schedules.count { it.status == ExamStatus.SCHEDULED }
}

class ExamViewModel(
    private val examRepository: ExamRepository = ExamRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExamUiState())
    val uiState: StateFlow<ExamUiState> = _uiState.asStateFlow()

    init {
        // Synchronous initial loading of pre-seeded exams for immediate UI responsiveness & tests
        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        val initialExams = examRepository.getPreSeededExams(schoolId)
        _uiState.update { it.copy(schedules = initialExams) }
        loadExams()
    }

    fun loadExams() {
        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val exams = examRepository.getExams(schoolId = schoolId)
                _uiState.update { it.copy(schedules = exams, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to fetch examination schedules"
                    )
                }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setTypeFilter(type: ExamType?) {
        _uiState.update { it.copy(selectedTypeFilter = type) }
    }

    fun setGradeFilter(grade: String?) {
        _uiState.update { it.copy(selectedGradeFilter = grade) }
    }

    fun clearFilters() {
        _uiState.update { it.copy(searchQuery = "", selectedTypeFilter = null, selectedGradeFilter = null) }
    }

    fun openCreateScheduleDialog() {
        if (!TenantContext.hasPermission(Permission.EXAMS_MANAGE)) {
            _uiState.update { it.copy(errorMessage = "Permission denied: Requires EXAMS_MANAGE permission") }
            return
        }
        _uiState.update { it.copy(showCreateScheduleDialog = true, errorMessage = null, conflictWarning = null) }
    }

    fun closeCreateScheduleDialog() {
        _uiState.update { it.copy(showCreateScheduleDialog = false, conflictWarning = null) }
    }

    fun openAddPaperDialog(schedule: ExamSchedule) {
        if (!TenantContext.hasPermission(Permission.EXAMS_MANAGE)) {
            _uiState.update { it.copy(errorMessage = "Permission denied: Requires EXAMS_MANAGE permission") }
            return
        }
        _uiState.update {
            it.copy(
                showAddPaperDialog = true,
                selectedScheduleForPaper = schedule,
                errorMessage = null,
                conflictWarning = null
            )
        }
    }

    fun closeAddPaperDialog() {
        _uiState.update {
            it.copy(
                showAddPaperDialog = false,
                selectedScheduleForPaper = null,
                conflictWarning = null
            )
        }
    }

    fun openHallTicketDialog(schedule: ExamSchedule) {
        _uiState.update {
            it.copy(
                showHallTicketDialog = true,
                selectedScheduleForHallTicket = schedule,
                errorMessage = null
            )
        }
    }

    fun closeHallTicketDialog() {
        _uiState.update {
            it.copy(
                showHallTicketDialog = false,
                selectedScheduleForHallTicket = null,
                generatedHallTicket = null
            )
        }
    }

    fun validatePaperConflict(
        existingPapers: List<ExamPaper>,
        date: String,
        startTime: String,
        endTime: String,
        room: String
    ): String? {
        val conflict = examRepository.checkTimetableConflict(existingPapers, date, startTime, endTime, room)
        _uiState.update { it.copy(conflictWarning = conflict) }
        return conflict
    }

    fun createExamSchedule(
        title: String,
        examType: ExamType,
        academicSession: String,
        gradeLevel: String,
        startDate: String,
        endDate: String
    ) {
        if (!TenantContext.hasPermission(Permission.EXAMS_MANAGE)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires EXAMS_MANAGE permission") }
            return
        }

        if (title.isBlank() || gradeLevel.isBlank() || startDate.isBlank() || endDate.isBlank()) {
            _uiState.update { it.copy(errorMessage = "All examination series fields are required") }
            return
        }

        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        val cleanSuffix = if (schoolId.length >= 3) schoolId.substring(schoolId.length - 3) else "001"
        val newSchedule = ExamSchedule(
            id = "ex_${cleanSuffix}_${System.currentTimeMillis().toString().takeLast(6)}",
            schoolId = schoolId,
            title = title.trim(),
            examType = examType,
            academicSession = academicSession.ifBlank { "2026-2027" },
            gradeLevel = gradeLevel.trim(),
            startDate = startDate.trim(),
            endDate = endDate.trim(),
            status = ExamStatus.SCHEDULED,
            papers = emptyList(),
            createdAt = System.currentTimeMillis()
        )

        // Update state synchronously for immediate test and UI response
        _uiState.update {
            it.copy(
                schedules = listOf(newSchedule) + it.schedules,
                showCreateScheduleDialog = false,
                statusMessage = "Examination series '${newSchedule.title}' created successfully.",
                errorMessage = null
            )
        }

        viewModelScope.launch {
            examRepository.saveExam(newSchedule)
        }
    }

    fun addPaperToSchedule(
        scheduleId: String,
        subjectName: String,
        subjectCode: String,
        examDate: String,
        startTime: String,
        endTime: String,
        durationMinutes: Int,
        totalMarks: Int,
        passingMarks: Int,
        roomNumber: String,
        invigilatorName: String,
        instructions: String = "No unauthorized electronic devices allowed."
    ): Boolean {
        if (!TenantContext.hasPermission(Permission.EXAMS_MANAGE)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires EXAMS_MANAGE permission") }
            return false
        }

        if (subjectName.isBlank() || examDate.isBlank() || startTime.isBlank() || roomNumber.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Subject, date, time slot, and room are mandatory.") }
            return false
        }

        val targetSchedule = _uiState.value.schedules.firstOrNull { it.id == scheduleId }
        if (targetSchedule == null) {
            _uiState.update { it.copy(errorMessage = "Examination series not found.") }
            return false
        }

        // Conflict check
        val conflict = examRepository.checkTimetableConflict(
            existingPapers = targetSchedule.papers,
            newDate = examDate,
            newStartTime = startTime,
            newEndTime = endTime,
            newRoom = roomNumber
        )

        if (conflict != null) {
            _uiState.update { it.copy(conflictWarning = conflict, errorMessage = conflict) }
            return false
        }

        val newPaper = ExamPaper(
            id = "p_${UUID.randomUUID().toString().take(8)}",
            subjectName = subjectName.trim(),
            subjectCode = subjectCode.trim(),
            examDate = examDate.trim(),
            startTime = startTime.trim(),
            endTime = endTime.trim(),
            durationMinutes = durationMinutes,
            totalMarks = totalMarks,
            passingMarks = passingMarks,
            roomNumber = roomNumber.trim(),
            invigilatorId = "inv_${UUID.randomUUID().toString().take(6)}",
            invigilatorName = invigilatorName.trim().ifBlank { "Unassigned Staff" },
            instructions = instructions.trim()
        )

        val updatedPapers = targetSchedule.papers + newPaper
        val updatedSchedule = targetSchedule.copy(
            papers = updatedPapers,
            totalPapers = updatedPapers.size
        )

        // Update UI state synchronously
        _uiState.update { state ->
            val updatedList = state.schedules.map { if (it.id == scheduleId) updatedSchedule else it }
            state.copy(
                schedules = updatedList,
                showAddPaperDialog = false,
                selectedScheduleForPaper = null,
                conflictWarning = null,
                statusMessage = "Paper '${newPaper.subjectName}' added to timetable.",
                errorMessage = null
            )
        }

        viewModelScope.launch {
            examRepository.saveExam(updatedSchedule)
        }
        return true
    }

    fun generateStudentHallTicket(
        schedule: ExamSchedule,
        studentId: String = "stu_oxf_01",
        studentName: String = "Alexander Hamilton",
        rollNumber: String = "10-A-01",
        admissionNumber: String = "ADM-2024-001",
        gradeLevel: String = schedule.gradeLevel,
        section: String = "Section A"
    ) {
        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        val ticket = examRepository.generateHallTicket(
            schoolId = schoolId,
            exam = schedule,
            studentId = studentId,
            studentName = studentName,
            rollNumber = rollNumber,
            admissionNumber = admissionNumber,
            gradeLevel = gradeLevel,
            section = section
        )

        _uiState.update {
            it.copy(
                generatedHallTicket = ticket,
                showHallTicketDialog = true,
                selectedScheduleForHallTicket = schedule,
                statusMessage = "Hall ticket generated for $studentName (${ticket.ticketNumber})"
            )
        }
    }

    fun deleteSchedule(scheduleId: String) {
        if (!TenantContext.hasPermission(Permission.EXAMS_MANAGE)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires EXAMS_MANAGE permission") }
            return
        }

        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        _uiState.update { state ->
            state.copy(
                schedules = state.schedules.filter { it.id != scheduleId },
                statusMessage = "Examination schedule deleted successfully."
            )
        }

        viewModelScope.launch {
            examRepository.deleteExam(schoolId, scheduleId)
        }
    }

    fun dismissMessages() {
        _uiState.update { it.copy(statusMessage = null, errorMessage = null, conflictWarning = null) }
    }
}
