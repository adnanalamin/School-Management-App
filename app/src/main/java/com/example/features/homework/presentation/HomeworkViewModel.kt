package com.example.features.homework.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.security.Permission
import com.example.core.security.TenantContext
import com.example.features.homework.data.HomeworkRepository
import com.example.features.homework.domain.HomeworkAssignment
import com.example.features.homework.domain.HomeworkStatus
import com.example.features.homework.domain.HomeworkSubmission
import com.example.features.homework.domain.SubmissionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class HomeworkUiState(
    val assignments: List<HomeworkAssignment> = emptyList(),
    val searchQuery: String = "",
    val selectedStatusFilter: HomeworkStatus? = null,
    val selectedSubjectFilter: String? = null,
    val selectedGradeFilter: String? = null,
    val isLoading: Boolean = false,
    val statusMessage: String? = null,
    val errorMessage: String? = null,
    val showCreateDialog: Boolean = false,
    val showSubmitDialog: Boolean = false,
    val showGradeDialog: Boolean = false,
    val showDetailDialog: Boolean = false,
    val selectedAssignmentForAction: HomeworkAssignment? = null,
    val selectedSubmissionForGrading: HomeworkSubmission? = null
) {
    val filteredAssignments: List<HomeworkAssignment>
        get() = assignments.filter { hw ->
            val matchesSearch = if (searchQuery.isBlank()) true else {
                val q = searchQuery.trim().lowercase()
                hw.title.lowercase().contains(q) ||
                hw.description.lowercase().contains(q) ||
                hw.subjectName.lowercase().contains(q) ||
                hw.teacherName.lowercase().contains(q) ||
                hw.gradeLevel.lowercase().contains(q)
            }
            val matchesStatus = selectedStatusFilter == null || hw.status == selectedStatusFilter
            val matchesSubject = selectedSubjectFilter == null || hw.subjectCode.equals(selectedSubjectFilter, ignoreCase = true)
            val matchesGrade = selectedGradeFilter == null || hw.gradeLevel.contains(selectedGradeFilter, ignoreCase = true)
            matchesSearch && matchesStatus && matchesSubject && matchesGrade
        }

    val totalAssignmentsCount: Int
        get() = assignments.size

    val activeAssignmentsCount: Int
        get() = assignments.count { it.status == HomeworkStatus.ASSIGNED || it.status == HomeworkStatus.DUE_SOON }

    val totalSubmissionsCount: Int
        get() = assignments.sumOf { it.submissionCount }

    val pendingReviewCount: Int
        get() = assignments.sumOf { it.pendingReviewCount }

    val overallSubmissionRate: Double
        get() {
            val totalCapacity = assignments.sumOf { it.totalEnrolledStudents }
            return if (totalCapacity > 0) {
                (totalSubmissionsCount.toDouble() / totalCapacity.toDouble()) * 100.0
            } else 0.0
        }
}

class HomeworkViewModel(
    private val homeworkRepository: HomeworkRepository = HomeworkRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeworkUiState())
    val uiState: StateFlow<HomeworkUiState> = _uiState.asStateFlow()

    init {
        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        val seeded = homeworkRepository.getPreSeededHomework(schoolId)
        _uiState.update { it.copy(assignments = seeded) }
        loadHomework()
    }

    fun loadHomework() {
        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val list = homeworkRepository.getHomeworkList(schoolId = schoolId)
                _uiState.update { it.copy(assignments = list, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to load homework assignments"
                    )
                }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setStatusFilter(status: HomeworkStatus?) {
        _uiState.update { it.copy(selectedStatusFilter = status) }
    }

    fun setSubjectFilter(subjectCode: String?) {
        _uiState.update { it.copy(selectedSubjectFilter = subjectCode) }
    }

    fun setGradeFilter(grade: String?) {
        _uiState.update { it.copy(selectedGradeFilter = grade) }
    }

    fun clearFilters() {
        _uiState.update {
            it.copy(
                searchQuery = "",
                selectedStatusFilter = null,
                selectedSubjectFilter = null,
                selectedGradeFilter = null
            )
        }
    }

    fun openCreateDialog() {
        if (!TenantContext.hasPermission(Permission.HOMEWORK_CREATE)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires HOMEWORK_CREATE permission") }
            return
        }
        _uiState.update { it.copy(showCreateDialog = true, errorMessage = null) }
    }

    fun closeCreateDialog() {
        _uiState.update { it.copy(showCreateDialog = false) }
    }

    fun openSubmitDialog(assignment: HomeworkAssignment) {
        if (!TenantContext.hasPermission(Permission.HOMEWORK_SUBMIT)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires HOMEWORK_SUBMIT permission") }
            return
        }
        _uiState.update {
            it.copy(
                selectedAssignmentForAction = assignment,
                showSubmitDialog = true,
                errorMessage = null
            )
        }
    }

    fun closeSubmitDialog() {
        _uiState.update {
            it.copy(
                showSubmitDialog = false,
                selectedAssignmentForAction = null
            )
        }
    }

    fun openGradingDialog(assignment: HomeworkAssignment, submission: HomeworkSubmission) {
        if (!TenantContext.hasPermission(Permission.HOMEWORK_GRADE)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires HOMEWORK_GRADE permission") }
            return
        }
        _uiState.update {
            it.copy(
                selectedAssignmentForAction = assignment,
                selectedSubmissionForGrading = submission,
                showGradeDialog = true,
                errorMessage = null
            )
        }
    }

    fun closeGradingDialog() {
        _uiState.update {
            it.copy(
                showGradeDialog = false,
                selectedAssignmentForAction = null,
                selectedSubmissionForGrading = null
            )
        }
    }

    fun openAssignmentDetail(assignment: HomeworkAssignment) {
        _uiState.update {
            it.copy(
                selectedAssignmentForAction = assignment,
                showDetailDialog = true
            )
        }
    }

    fun closeAssignmentDetail() {
        _uiState.update {
            it.copy(
                showDetailDialog = false,
                selectedAssignmentForAction = null
            )
        }
    }

    fun createAssignment(
        title: String,
        description: String,
        subjectCode: String,
        subjectName: String,
        gradeLevel: String,
        section: String,
        dueDate: String,
        totalMarks: Double = 50.0,
        attachmentTitle: String = ""
    ) {
        if (!TenantContext.hasPermission(Permission.HOMEWORK_CREATE)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires HOMEWORK_CREATE permission") }
            return
        }

        if (title.isBlank() || subjectName.isBlank() || dueDate.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Assignment title, subject, and due date are required.") }
            return
        }

        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val todayStr = dateFormat.format(Date())

        val newAssignment = HomeworkAssignment(
            id = "hw_${UUID.randomUUID().toString().take(8)}",
            schoolId = schoolId,
            title = title.trim(),
            description = description.trim(),
            subjectCode = subjectCode.ifBlank { "GEN-100" }.trim().uppercase(),
            subjectName = subjectName.trim(),
            gradeLevel = gradeLevel.ifBlank { "Grade 10" }.trim(),
            section = section.ifBlank { "Section A" }.trim(),
            assignedDate = todayStr,
            dueDate = dueDate.trim(),
            totalMarks = totalMarks.coerceAtLeast(10.0),
            teacherId = "tea_oxf_active",
            teacherName = "Faculty Department Head",
            status = HomeworkStatus.ASSIGNED,
            attachmentTitle = attachmentTitle.trim(),
            totalEnrolledStudents = 32,
            submissions = emptyList(),
            createdAt = System.currentTimeMillis()
        )

        _uiState.update { state ->
            state.copy(
                assignments = listOf(newAssignment) + state.assignments,
                showCreateDialog = false,
                statusMessage = "Assignment '${newAssignment.title}' assigned to ${newAssignment.gradeLevel}.",
                errorMessage = null
            )
        }

        viewModelScope.launch {
            homeworkRepository.saveHomework(newAssignment)
        }
    }

    fun submitStudentWork(
        assignmentId: String,
        studentName: String,
        rollNumber: String,
        attachmentTitle: String,
        studentNotes: String
    ) {
        if (!TenantContext.hasPermission(Permission.HOMEWORK_SUBMIT)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires HOMEWORK_SUBMIT permission") }
            return
        }

        if (studentName.isBlank() || rollNumber.isBlank() || attachmentTitle.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Student Name, Roll Number, and Deliverable file are required.") }
            return
        }

        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val submission = HomeworkSubmission(
            studentId = "stu_${UUID.randomUUID().toString().take(6)}",
            studentName = studentName.trim(),
            rollNumber = rollNumber.trim().uppercase(),
            submissionDate = dateFormat.format(Date()),
            attachmentTitle = attachmentTitle.trim(),
            studentNotes = studentNotes.trim(),
            status = SubmissionStatus.SUBMITTED
        )

        _uiState.update { state ->
            val updatedList = state.assignments.map { hw ->
                if (hw.id == assignmentId) {
                    val filtered = hw.submissions.filterNot { it.rollNumber == submission.rollNumber }
                    hw.copy(submissions = filtered + submission)
                } else hw
            }
            state.copy(
                assignments = updatedList,
                showSubmitDialog = false,
                selectedAssignmentForAction = null,
                statusMessage = "Deliverable submitted successfully for '${submission.studentName}'.",
                errorMessage = null
            )
        }

        viewModelScope.launch {
            homeworkRepository.submitHomework(schoolId, assignmentId, submission)
        }
    }

    fun gradeStudentSubmission(
        assignmentId: String,
        studentId: String,
        marksAwarded: Double,
        feedback: String
    ) {
        if (!TenantContext.hasPermission(Permission.HOMEWORK_GRADE)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires HOMEWORK_GRADE permission") }
            return
        }

        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        _uiState.update { state ->
            val updatedList = state.assignments.map { hw ->
                if (hw.id == assignmentId) {
                    val updatedSubmissions = hw.submissions.map { sub ->
                        if (sub.studentId == studentId) {
                            sub.copy(
                                marksAwarded = marksAwarded,
                                feedback = feedback.trim(),
                                status = SubmissionStatus.GRADED,
                                evaluatedBy = "Faculty Evaluator",
                                evaluatedAt = System.currentTimeMillis()
                            )
                        } else sub
                    }
                    hw.copy(submissions = updatedSubmissions)
                } else hw
            }
            state.copy(
                assignments = updatedList,
                showGradeDialog = false,
                selectedAssignmentForAction = null,
                selectedSubmissionForGrading = null,
                statusMessage = "Submission graded: $marksAwarded marks awarded.",
                errorMessage = null
            )
        }

        viewModelScope.launch {
            homeworkRepository.gradeSubmission(
                schoolId = schoolId,
                homeworkId = assignmentId,
                studentId = studentId,
                marks = marksAwarded,
                feedback = feedback,
                evaluatorName = "Faculty Evaluator"
            )
        }
    }

    fun deleteAssignment(assignmentId: String) {
        if (!TenantContext.hasPermission(Permission.HOMEWORK_CREATE)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires HOMEWORK_CREATE permission") }
            return
        }

        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        _uiState.update { state ->
            state.copy(
                assignments = state.assignments.filterNot { it.id == assignmentId },
                statusMessage = "Assignment removed."
            )
        }

        viewModelScope.launch {
            homeworkRepository.deleteHomework(schoolId, assignmentId)
        }
    }

    fun dismissStatusMessage() {
        _uiState.update { it.copy(statusMessage = null, errorMessage = null) }
    }
}
