package com.example.features.audit.presentation

import androidx.lifecycle.ViewModel
import com.example.core.security.Permission
import com.example.core.security.TenantContext
import com.example.features.audit.data.SecurityAuditEngine
import com.example.features.audit.domain.AuditCategory
import com.example.features.audit.domain.ComplianceSummary
import com.example.features.audit.domain.PerformanceMetricSnapshot
import com.example.features.audit.domain.SecurityAuditCheck
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SecurityAuditUiState(
    val checks: List<SecurityAuditCheck> = emptyList(),
    val selectedCategory: AuditCategory? = null,
    val summary: ComplianceSummary = ComplianceSummary(0, 0, 0, 0, 100, "A+"),
    val performanceMetrics: PerformanceMetricSnapshot = PerformanceMetricSnapshot(0.0, 0.0, 0.0, 0, 0, 0),
    val lastRunTimestampLabel: String = "",
    val statusMessage: String? = null,
    val errorMessage: String? = null
) {
    val filteredChecks: List<SecurityAuditCheck>
        get() = if (selectedCategory == null) checks else checks.filter { it.category == selectedCategory }
}

class SecurityAuditViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SecurityAuditUiState())
    val uiState: StateFlow<SecurityAuditUiState> = _uiState.asStateFlow()

    init {
        runFullAudit(showFeedback = false)
    }

    fun selectCategory(category: AuditCategory?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun runFullAudit(showFeedback: Boolean = true) {
        if (!TenantContext.hasPermission(Permission.SCHOOL_CONFIG_VIEW)) {
            _uiState.update {
                it.copy(errorMessage = "Unauthorized: Requires SCHOOL_CONFIG_VIEW permission to run audit.")
            }
            return
        }

        val report = SecurityAuditEngine.runComprehensiveAudit()
        val timeLabel = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())

        _uiState.update {
            it.copy(
                checks = report.checks,
                summary = report.summary,
                performanceMetrics = report.performanceMetrics,
                lastRunTimestampLabel = timeLabel,
                statusMessage = if (showFeedback) {
                    "Security & Performance Audit passed ${report.summary.passedChecks}/${report.summary.totalChecks} checks (${report.summary.gradeLabel})."
                } else {
                    null
                },
                errorMessage = null
            )
        }
    }

    fun dismissStatusMessage() {
        _uiState.update { it.copy(statusMessage = null, errorMessage = null) }
    }
}
