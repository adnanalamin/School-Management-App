package com.example.features.results.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.security.Permission
import com.example.core.security.TenantContext
import com.example.features.results.data.ResultRepository
import com.example.features.results.domain.GradeScale
import com.example.features.results.domain.ReportCard
import com.example.features.results.domain.ResultStatus
import com.example.features.results.domain.StudentResult
import com.example.features.results.domain.SubjectScore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class ResultUiState(
    val results: List<StudentResult> = emptyList(),
    val searchQuery: String = "",
    val selectedStatusFilter: ResultStatus? = null,
    val selectedGradeFilter: String? = null,
    val isLoading: Boolean = false,
    val statusMessage: String? = null,
    val errorMessage: String? = null,
    val showScoreEntryDialog: Boolean = false,
    val showReportCardDialog: Boolean = false,
    val selectedResultForReportCard: StudentResult? = null,
    val generatedReportCard: ReportCard? = null
) {
    val filteredResults: List<StudentResult>
        get() = results.filter { res ->
            val matchesSearch = if (searchQuery.isBlank()) true else {
                val q = searchQuery.trim().lowercase()
                res.studentName.lowercase().contains(q) ||
                res.rollNumber.lowercase().contains(q) ||
                res.examTitle.lowercase().contains(q) ||
                res.gradeLevel.lowercase().contains(q) ||
                res.subjectScores.any { it.subjectName.lowercase().contains(q) }
            }
            val matchesStatus = selectedStatusFilter == null || res.status == selectedStatusFilter
            val matchesGrade = selectedGradeFilter == null || res.gradeLevel.contains(selectedGradeFilter, ignoreCase = true)
            matchesSearch && matchesStatus && matchesGrade
        }

    val totalResultsCount: Int
        get() = results.size

    val classAveragePercentage: Double
        get() = if (results.isNotEmpty()) results.map { it.percentage }.average() else 0.0

    val highestPercentage: Double
        get() = if (results.isNotEmpty()) results.maxOf { it.percentage } else 0.0

    val passRatePercentage: Double
        get() {
            if (results.isEmpty()) return 0.0
            val passing = results.count { it.percentage >= 40.0 }
            return (passing.toDouble() / results.size.toDouble()) * 100.0
        }

    val publishedCount: Int
        get() = results.count { it.status == ResultStatus.PUBLISHED }
}

class ResultViewModel(
    private val resultRepository: ResultRepository = ResultRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResultUiState())
    val uiState: StateFlow<ResultUiState> = _uiState.asStateFlow()

    init {
        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        val seeded = resultRepository.getPreSeededResults(schoolId)
        _uiState.update { it.copy(results = seeded) }
        loadResults()
    }

    fun loadResults() {
        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val list = resultRepository.getResults(schoolId = schoolId)
                _uiState.update { it.copy(results = list, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to fetch student results"
                    )
                }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setStatusFilter(status: ResultStatus?) {
        _uiState.update { it.copy(selectedStatusFilter = status) }
    }

    fun setGradeFilter(grade: String?) {
        _uiState.update { it.copy(selectedGradeFilter = grade) }
    }

    fun clearFilters() {
        _uiState.update { it.copy(searchQuery = "", selectedStatusFilter = null, selectedGradeFilter = null) }
    }

    fun openScoreEntryDialog() {
        if (!TenantContext.hasPermission(Permission.RESULTS_ENTER)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires RESULTS_ENTER permission") }
            return
        }
        _uiState.update { it.copy(showScoreEntryDialog = true, errorMessage = null) }
    }

    fun closeScoreEntryDialog() {
        _uiState.update { it.copy(showScoreEntryDialog = false) }
    }

    fun openReportCard(result: StudentResult) {
        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        val report = resultRepository.generateReportCard(schoolId, result)
        _uiState.update {
            it.copy(
                selectedResultForReportCard = result,
                generatedReportCard = report,
                showReportCardDialog = true
            )
        }
    }

    fun closeReportCardDialog() {
        _uiState.update {
            it.copy(
                showReportCardDialog = false,
                selectedResultForReportCard = null,
                generatedReportCard = null
            )
        }
    }

    fun addStudentResult(
        studentName: String,
        rollNumber: String,
        gradeLevel: String,
        section: String,
        examTitle: String,
        academicSession: String,
        scores: List<SubjectScore>,
        teacherRemarks: String
    ) {
        if (!TenantContext.hasPermission(Permission.RESULTS_ENTER)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires RESULTS_ENTER permission") }
            return
        }

        if (studentName.isBlank() || rollNumber.isBlank() || scores.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Student name, roll number, and at least one subject score are required.") }
            return
        }

        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        val newId = "res_${UUID.randomUUID().toString().take(8)}"
        val newResult = StudentResult.createWithComputedMetrics(
            id = newId,
            schoolId = schoolId,
            examId = "ex_active_session",
            examTitle = examTitle.ifBlank { "Midterm Examination Series 2026" },
            academicSession = academicSession.ifBlank { "2026-2027" },
            studentId = "stu_${UUID.randomUUID().toString().take(6)}",
            studentName = studentName.trim(),
            rollNumber = rollNumber.trim(),
            gradeLevel = gradeLevel.trim(),
            section = section.trim(),
            subjectScores = scores,
            teacherRemarks = teacherRemarks.trim().ifBlank { "Satisfactory performance." }
        )

        // Update list synchronously with updated ranks
        _uiState.update { state ->
            val updatedList = (state.results + newResult).sortedByDescending { it.percentage }
                .mapIndexed { idx, item -> item.copy(rankInClass = idx + 1) }
            state.copy(
                results = updatedList,
                showScoreEntryDialog = false,
                statusMessage = "Score sheet recorded for ${newResult.studentName} (GPA: ${String.format("%.2f", newResult.gpa)}).",
                errorMessage = null
            )
        }

        viewModelScope.launch {
            resultRepository.saveResult(newResult)
        }
    }

    fun publishAllPendingResults() {
        if (!TenantContext.hasPermission(Permission.RESULTS_PUBLISH)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires RESULTS_PUBLISH permission") }
            return
        }

        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        _uiState.update { state ->
            val updated = state.results.map { it.copy(status = ResultStatus.PUBLISHED) }
            state.copy(
                results = updated,
                statusMessage = "All ${updated.size} candidate gradebooks officially published."
            )
        }

        viewModelScope.launch {
            resultRepository.publishAllResults(schoolId, "ex_active_session")
        }
    }

    fun dismissMessages() {
        _uiState.update { it.copy(statusMessage = null, errorMessage = null) }
    }
}
