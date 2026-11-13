package com.example.features.analytics.domain

import com.example.core.tenant.TenantScopedEntity

/**
 * Operational domains covered by Executive Reports & Data Analytics.
 */
enum class AnalyticsDomain(val displayName: String) {
    OVERVIEW("Executive Overview"),
    ACADEMICS("Academic & GPA"),
    ATTENDANCE("Attendance Trends"),
    FINANCE("Tuition & Collections"),
    OPERATIONS("Library, Fleet & Leaves");

    companion object {
        fun fromString(value: String?): AnalyticsDomain {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: OVERVIEW
        }
    }
}

/**
 * Reporting timeframes supported by the analytics engine.
 */
enum class ReportPeriod(val displayName: String, val attendanceOffset: Float, val revenueScale: Float) {
    TERM_1("Term 1 (2026-27)", 0f, 1.0f),
    MONTHLY("Last 30 Days", 0.6f, 0.35f),
    ANNUAL("Full Academic Year", -0.4f, 2.8f);

    companion object {
        fun fromString(value: String?): ReportPeriod {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: TERM_1
        }
    }
}

/**
 * Single data point for bar/trend visualizations.
 */
data class AnalyticsChartPoint(
    val label: String,
    val value: Float,
    val secondaryValue: Float = 0f,
    val unitSuffix: String = ""
)

/**
 * Department-level executive scorecard row.
 */
data class DepartmentMetricRow(
    val department: String,
    val primaryMetric: String,
    val secondaryMetric: String,
    val healthScore: Float
)

/**
 * Multi-module institutional analytics bundle computed strictly for a single schoolId tenant.
 */
data class SchoolAnalyticsBundle(
    val schoolId: String,
    val schoolName: String,
    val period: ReportPeriod,
    val totalStudents: Int,
    val activeFaculty: Int,
    val averageAttendancePct: Float,
    val feeCollectionRatePct: Float,
    val collectedRevenueAmount: Double,
    val pendingRevenueAmount: Double,
    val averageGpa: Float,
    val passRatePct: Float,
    val libraryUtilizationPct: Float,
    val fleetUtilizationPct: Float,
    val pendingLeavesCount: Int,
    val attendanceTrend: List<AnalyticsChartPoint>,
    val revenueBreakdown: List<AnalyticsChartPoint>,
    val gpaDistribution: List<AnalyticsChartPoint>,
    val departmentMetrics: List<DepartmentMetricRow>
) {
    val collectedRevenueLabel: String
        get() = "$${collectedRevenueAmount.toInt()}"

    val pendingRevenueLabel: String
        get() = "$${pendingRevenueAmount.toInt()}"
}

/**
 * Persisted Executive Report Snapshot stored in `schools/{schoolId}/reports/{reportId}`.
 */
data class ExecutiveReportSnapshot(
    override val id: String,
    override val schoolId: String,
    val reportCode: String,
    val title: String,
    val domain: AnalyticsDomain = AnalyticsDomain.OVERVIEW,
    val period: ReportPeriod = ReportPeriod.TERM_1,
    val generatedBy: String,
    val generatedDate: String,
    val headlineKpi: String,
    val summaryNotes: String,
    val exportFormat: String = "CSV",
    val createdAt: Long = System.currentTimeMillis()
) : TenantScopedEntity {

    val isCsvExport: Boolean
        get() = exportFormat.equals("CSV", ignoreCase = true)

    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "id" to id,
        "schoolId" to schoolId,
        "reportCode" to reportCode,
        "title" to title,
        "domain" to domain.name,
        "period" to period.name,
        "generatedBy" to generatedBy,
        "generatedDate" to generatedDate,
        "headlineKpi" to headlineKpi,
        "summaryNotes" to summaryNotes,
        "exportFormat" to exportFormat,
        "createdAt" to createdAt
    )

    companion object {
        fun fromFirestoreMap(id: String, schoolId: String, data: Map<String, Any>): ExecutiveReportSnapshot {
            return ExecutiveReportSnapshot(
                id = id,
                schoolId = schoolId,
                reportCode = data["reportCode"] as? String ?: "",
                title = data["title"] as? String ?: "",
                domain = AnalyticsDomain.fromString(data["domain"] as? String),
                period = ReportPeriod.fromString(data["period"] as? String),
                generatedBy = data["generatedBy"] as? String ?: "",
                generatedDate = data["generatedDate"] as? String ?: "",
                headlineKpi = data["headlineKpi"] as? String ?: "",
                summaryNotes = data["summaryNotes"] as? String ?: "",
                exportFormat = data["exportFormat"] as? String ?: "CSV",
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}
