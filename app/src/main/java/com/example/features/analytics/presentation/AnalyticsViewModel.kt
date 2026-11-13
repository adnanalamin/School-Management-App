package com.example.features.analytics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.security.Permission
import com.example.core.security.TenantContext
import com.example.core.security.UserRole
import com.example.features.analytics.data.AnalyticsRepository
import com.example.features.analytics.domain.AnalyticsDomain
import com.example.features.analytics.domain.ExecutiveReportSnapshot
import com.example.features.analytics.domain.ReportPeriod
import com.example.features.analytics.domain.SchoolAnalyticsBundle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class AnalyticsUiState(
    val selectedDomain: AnalyticsDomain = AnalyticsDomain.OVERVIEW,
    val selectedPeriod: ReportPeriod = ReportPeriod.TERM_1,
    val bundle: SchoolAnalyticsBundle? = null,
    val reports: List<ExecutiveReportSnapshot> = emptyList(),
    val isLoading: Boolean = false,
    val statusMessage: String? = null,
    val errorMessage: String? = null,
    val showGenerateReportDialog: Boolean = false,
    val showReportPreviewDialog: Boolean = false,
    val selectedReportForPreview: ExecutiveReportSnapshot? = null,
    val exportedCsvPreview: String = ""
) {
    val filteredReports: List<ExecutiveReportSnapshot>
        get() = if (selectedDomain == AnalyticsDomain.OVERVIEW) {
            reports
        } else {
            reports.filter { it.domain == selectedDomain || it.domain == AnalyticsDomain.OVERVIEW }
        }

    val totalReportsCount: Int
        get() = reports.size
}

class AnalyticsViewModel(
    private val analyticsRepository: AnalyticsRepository = AnalyticsRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        loadAnalytics()
    }

    fun loadAnalytics() {
        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        val period = _uiState.value.selectedPeriod
        val initialBundle = analyticsRepository.getAnalyticsBundle(schoolId, period)
        val preSeededReports = analyticsRepository.getPreSeededReports(schoolId)

        _uiState.update {
            it.copy(
                bundle = initialBundle,
                reports = preSeededReports,
                isLoading = true
            )
        }

        viewModelScope.launch {
            try {
                val remoteReports = analyticsRepository.getReports(schoolId)
                _uiState.update {
                    it.copy(
                        bundle = analyticsRepository.getAnalyticsBundle(schoolId, it.selectedPeriod),
                        reports = remoteReports,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to load executive analytics"
                    )
                }
            }
        }
    }

    fun selectDomain(domain: AnalyticsDomain) {
        _uiState.update { it.copy(selectedDomain = domain) }
    }

    fun selectPeriod(period: ReportPeriod) {
        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        val updatedBundle = analyticsRepository.getAnalyticsBundle(schoolId, period)
        _uiState.update {
            it.copy(
                selectedPeriod = period,
                bundle = updatedBundle
            )
        }
    }

    fun openGenerateReportDialog() {
        if (!TenantContext.hasPermission(Permission.ANALYTICS_EXPORT)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires ANALYTICS_EXPORT permission") }
            return
        }
        _uiState.update { it.copy(showGenerateReportDialog = true, errorMessage = null) }
    }

    fun closeGenerateReportDialog() {
        _uiState.update { it.copy(showGenerateReportDialog = false) }
    }

    fun openReportPreview(report: ExecutiveReportSnapshot) {
        if (!TenantContext.hasPermission(Permission.ANALYTICS_VIEW)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires ANALYTICS_VIEW permission") }
            return
        }
        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        val currentBundle = _uiState.value.bundle ?: analyticsRepository.getAnalyticsBundle(schoolId, report.period)
        val csv = analyticsRepository.buildCsvExportPayload(currentBundle, report)
        _uiState.update {
            it.copy(
                selectedReportForPreview = report,
                exportedCsvPreview = csv,
                showReportPreviewDialog = true
            )
        }
    }

    fun closeReportPreview() {
        _uiState.update {
            it.copy(
                showReportPreviewDialog = false,
                selectedReportForPreview = null,
                exportedCsvPreview = ""
            )
        }
    }

    fun generateReport(
        title: String,
        domain: AnalyticsDomain,
        period: ReportPeriod,
        exportFormat: String,
        summaryNotes: String
    ) {
        if (!TenantContext.hasPermission(Permission.ANALYTICS_EXPORT)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires ANALYTICS_EXPORT permission") }
            return
        }

        if (title.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Report title is required.") }
            return
        }

        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        val bundle = analyticsRepository.getAnalyticsBundle(schoolId, period)
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val prefix = if (schoolId.contains("oxford")) "OXF" else "SCH"
        val code = "RPT-$prefix-${UUID.randomUUID().toString().take(4).uppercase()}"

        val headline = when (domain) {
            AnalyticsDomain.OVERVIEW -> "${bundle.averageAttendancePct}% Attendance • ${bundle.averageGpa} GPA • ${bundle.feeCollectionRatePct}% Fees"
            AnalyticsDomain.ACADEMICS -> "Avg GPA ${bundle.averageGpa} • ${bundle.passRatePct}% Pass Rate"
            AnalyticsDomain.ATTENDANCE -> "${bundle.averageAttendancePct}% Attendance • ${bundle.pendingLeavesCount} Pending Leaves"
            AnalyticsDomain.FINANCE -> "${bundle.collectedRevenueLabel} Collected • ${bundle.pendingRevenueLabel} Pending"
            AnalyticsDomain.OPERATIONS -> "${bundle.libraryUtilizationPct}% Library • ${bundle.fleetUtilizationPct}% Fleet"
        }

        val snapshot = ExecutiveReportSnapshot(
            id = "rpt_${UUID.randomUUID().toString().take(8)}",
            schoolId = schoolId,
            reportCode = code,
            title = title.trim(),
            domain = domain,
            period = period,
            generatedBy = personaDisplayName(TenantContext.activeUserRole.value),
            generatedDate = todayStr,
            headlineKpi = headline,
            summaryNotes = summaryNotes.ifBlank { "Executive analytics snapshot generated for ${period.displayName}." }.trim(),
            exportFormat = exportFormat.uppercase(),
            createdAt = System.currentTimeMillis()
        )

        _uiState.update { state ->
            state.copy(
                reports = listOf(snapshot) + state.reports,
                showGenerateReportDialog = false,
                statusMessage = "Generated ${snapshot.exportFormat} report $code.",
                errorMessage = null
            )
        }

        viewModelScope.launch {
            analyticsRepository.saveReport(snapshot)
        }
    }

    fun deleteReport(reportId: String) {
        if (!TenantContext.hasPermission(Permission.ANALYTICS_EXPORT)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires ANALYTICS_EXPORT permission") }
            return
        }

        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        _uiState.update { state ->
            state.copy(
                reports = state.reports.filterNot { it.id == reportId },
                showReportPreviewDialog = false,
                selectedReportForPreview = null,
                statusMessage = "Executive report removed."
            )
        }

        viewModelScope.launch {
            analyticsRepository.deleteReport(schoolId, reportId)
        }
    }

    fun dismissStatusMessage() {
        _uiState.update { it.copy(statusMessage = null, errorMessage = null) }
    }

    private fun personaDisplayName(role: UserRole): String {
        return when (role) {
            UserRole.SUPER_ADMIN -> "Platform Super Admin"
            UserRole.SCHOOL_ADMIN -> "Dr. Robert Vance"
            UserRole.ACCOUNTANT -> "Marcus Vance (Finance)"
            UserRole.TEACHER -> "Dr. Sarah Jenkins"
            else -> role.displayName
        }
    }
}
