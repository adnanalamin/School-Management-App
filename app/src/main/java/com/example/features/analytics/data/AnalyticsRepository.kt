package com.example.features.analytics.data

import android.content.Context
import android.util.Log
import com.example.R
import com.example.core.error.ErrorHandler
import com.example.core.tenant.TenantPathProvider
import com.example.features.analytics.domain.AnalyticsChartPoint
import com.example.features.analytics.domain.AnalyticsDomain
import com.example.features.analytics.domain.DepartmentMetricRow
import com.example.features.analytics.domain.ExecutiveReportSnapshot
import com.example.features.analytics.domain.ReportPeriod
import com.example.features.analytics.domain.SchoolAnalyticsBundle
import com.example.features.leave.data.LeaveRepository
import com.example.features.library.data.LibraryRepository
import com.example.features.transport.data.TransportRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.math.roundToInt

class AnalyticsRepository(
    private val context: Context? = null,
    private val firestoreInstance: FirebaseFirestore? = null
) {
    private val db: FirebaseFirestore by lazy {
        firestoreInstance ?: try {
            val dbId = context?.getString(R.string.firestore_database_id)
            if (dbId != null) FirebaseFirestore.getInstance(dbId) else FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w("AnalyticsRepository", "Fallback to default Firestore instance", e)
            FirebaseFirestore.getInstance()
        }
    }

    fun getAnalyticsBundle(
        schoolId: String = TenantPathProvider.requireActiveSchoolId(),
        period: ReportPeriod = ReportPeriod.TERM_1
    ): SchoolAnalyticsBundle {
        TenantPathProvider.verifyTenantBoundary(schoolId)

        val libraryBooks = LibraryRepository(context, firestoreInstance).getPreSeededBooks(schoolId)
        val totalCopies = libraryBooks.sumOf { it.totalCopies }.coerceAtLeast(1)
        val borrowedCopies = libraryBooks.sumOf { (it.totalCopies - it.availableCopies).coerceAtLeast(0) }
        val libUtilPct = ((borrowedCopies.toFloat() / totalCopies.toFloat()) * 100f).coerceIn(0f, 100f)

        val routes = TransportRepository(context, firestoreInstance).getPreSeededRoutes(schoolId)
        val totalSeats = routes.sumOf { it.capacity }.coerceAtLeast(1)
        val occupiedSeats = routes.sumOf { it.passengers.size }
        val fleetUtilPct = ((occupiedSeats.toFloat() / totalSeats.toFloat()) * 100f).coerceIn(0f, 100f)

        val leaves = LeaveRepository(context, firestoreInstance).getPreSeededLeaveRequests(schoolId)
        val pendingLeaves = leaves.count { it.isPending }

        val isOxford = schoolId.contains("oxford") || schoolId.startsWith("sch_oxf")
        val schoolName = if (isOxford) "Oxford International Academy" else "Cambridge High School"

        val baseStudents = if (isOxford) 1480 else 920
        val baseFaculty = if (isOxford) 94 else 62
        val baseAtt = ((if (isOxford) 94.2f else 92.6f) + period.attendanceOffset).coerceIn(75f, 99.5f)
        val collectedRev = ((if (isOxford) 84500.0 else 56200.0) * period.revenueScale)
        val pendingRev = ((if (isOxford) 14250.0 else 9400.0) * (if (period == ReportPeriod.MONTHLY) 0.55f else 1.0f))
        val collectionRate = ((collectedRev / (collectedRev + pendingRev)) * 100.0).toFloat()
        val avgGpa = if (isOxford) 3.68f else 3.54f
        val passRate = if (isOxford) 97.4f else 95.8f

        val attendanceTrend = if (isOxford) {
            listOf(
                AnalyticsChartPoint("Mon", (93.8f + period.attendanceOffset).coerceAtMost(99f), unitSuffix = "%"),
                AnalyticsChartPoint("Tue", (95.1f + period.attendanceOffset).coerceAtMost(99f), unitSuffix = "%"),
                AnalyticsChartPoint("Wed", (94.4f + period.attendanceOffset).coerceAtMost(99f), unitSuffix = "%"),
                AnalyticsChartPoint("Thu", (94.9f + period.attendanceOffset).coerceAtMost(99f), unitSuffix = "%"),
                AnalyticsChartPoint("Fri", (92.8f + period.attendanceOffset).coerceAtMost(99f), unitSuffix = "%")
            )
        } else {
            listOf(
                AnalyticsChartPoint("Mon", (92.0f + period.attendanceOffset).coerceAtMost(99f), unitSuffix = "%"),
                AnalyticsChartPoint("Tue", (93.4f + period.attendanceOffset).coerceAtMost(99f), unitSuffix = "%"),
                AnalyticsChartPoint("Wed", (92.9f + period.attendanceOffset).coerceAtMost(99f), unitSuffix = "%"),
                AnalyticsChartPoint("Thu", (93.1f + period.attendanceOffset).coerceAtMost(99f), unitSuffix = "%"),
                AnalyticsChartPoint("Fri", (91.5f + period.attendanceOffset).coerceAtMost(99f), unitSuffix = "%")
            )
        }

        val scale = period.revenueScale
        val revenueBreakdown = if (isOxford) {
            listOf(
                AnalyticsChartPoint("Grade 9", 24800f * scale, 3900f, "$"),
                AnalyticsChartPoint("Grade 10", 29400f * scale, 4200f, "$"),
                AnalyticsChartPoint("Grade 11", 16800f * scale, 3150f, "$"),
                AnalyticsChartPoint("Grade 12", 13500f * scale, 3000f, "$")
            )
        } else {
            listOf(
                AnalyticsChartPoint("Year 9", 15400f * scale, 2400f, "$"),
                AnalyticsChartPoint("Year 10", 19200f * scale, 2900f, "$"),
                AnalyticsChartPoint("Year 11", 12100f * scale, 2200f, "$"),
                AnalyticsChartPoint("Year 12", 9500f * scale, 1900f, "$")
            )
        }

        val gpaDistribution = if (isOxford) {
            listOf(
                AnalyticsChartPoint("A+ (3.9+)", 28f, unitSuffix = "%"),
                AnalyticsChartPoint("A (3.6+)", 36f, unitSuffix = "%"),
                AnalyticsChartPoint("B+ (3.2+)", 22f, unitSuffix = "%"),
                AnalyticsChartPoint("B (2.8+)", 11f, unitSuffix = "%"),
                AnalyticsChartPoint("C (<2.8)", 3f, unitSuffix = "%")
            )
        } else {
            listOf(
                AnalyticsChartPoint("A+ (3.9+)", 24f, unitSuffix = "%"),
                AnalyticsChartPoint("A (3.6+)", 33f, unitSuffix = "%"),
                AnalyticsChartPoint("B+ (3.2+)", 25f, unitSuffix = "%"),
                AnalyticsChartPoint("B (2.8+)", 13f, unitSuffix = "%"),
                AnalyticsChartPoint("C (<2.8)", 5f, unitSuffix = "%")
            )
        }

        val deptMetrics = listOf(
            DepartmentMetricRow(
                department = "Academic Performance",
                primaryMetric = "Avg GPA ${((avgGpa * 100f).roundToInt() / 100f)}",
                secondaryMetric = "Pass Rate ${passRate.roundToInt()}%",
                healthScore = passRate / 100f
            ),
            DepartmentMetricRow(
                department = "Daily Attendance",
                primaryMetric = "${((baseAtt * 10f).roundToInt() / 10f)}% Present",
                secondaryMetric = "$pendingLeaves pending leave(s)",
                healthScore = (baseAtt / 100f).coerceIn(0f, 1f)
            ),
            DepartmentMetricRow(
                department = "Tuition Collections",
                primaryMetric = "${collectionRate.roundToInt()}% Collected",
                secondaryMetric = "$${collectedRev.toInt()} received",
                healthScore = (collectionRate / 100f).coerceIn(0f, 1f)
            ),
            DepartmentMetricRow(
                department = "Library & Campus Fleet",
                primaryMetric = "${libUtilPct.roundToInt()}% Books Out",
                secondaryMetric = "${fleetUtilPct.roundToInt()}% Bus Occupancy",
                healthScore = ((libUtilPct + fleetUtilPct) / 200f).coerceIn(0.2f, 1f)
            )
        )

        return SchoolAnalyticsBundle(
            schoolId = schoolId,
            schoolName = schoolName,
            period = period,
            totalStudents = baseStudents,
            activeFaculty = baseFaculty,
            averageAttendancePct = (baseAtt * 10f).roundToInt() / 10f,
            feeCollectionRatePct = (collectionRate * 10f).roundToInt() / 10f,
            collectedRevenueAmount = collectedRev,
            pendingRevenueAmount = pendingRev,
            averageGpa = avgGpa,
            passRatePct = passRate,
            libraryUtilizationPct = (libUtilPct * 10f).roundToInt() / 10f,
            fleetUtilizationPct = (fleetUtilPct * 10f).roundToInt() / 10f,
            pendingLeavesCount = pendingLeaves,
            attendanceTrend = attendanceTrend,
            revenueBreakdown = revenueBreakdown,
            gpaDistribution = gpaDistribution,
            departmentMetrics = deptMetrics
        )
    }

    suspend fun getReports(
        schoolId: String = TenantPathProvider.requireActiveSchoolId()
    ): List<ExecutiveReportSnapshot> {
        return try {
            TenantPathProvider.verifyTenantBoundary(schoolId)
            val path = TenantPathProvider.reportsPath(schoolId)
            val snapshot = db.collection(path).get().await()
            if (snapshot.isEmpty) {
                getPreSeededReports(schoolId)
            } else {
                snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    ExecutiveReportSnapshot.fromFirestoreMap(doc.id, schoolId, data)
                }
            }
        } catch (e: Exception) {
            Log.w("AnalyticsRepository", "Error retrieving executive reports from Firestore, fallback to preseeded", e)
            getPreSeededReports(schoolId)
        }
    }

    suspend fun saveReport(report: ExecutiveReportSnapshot): Result<ExecutiveReportSnapshot> {
        return try {
            TenantPathProvider.verifyTenantBoundary(report.schoolId)
            val path = TenantPathProvider.reportsPath(report.schoolId)
            db.collection(path).document(report.id).set(report.toFirestoreMap()).await()
            Result.success(report)
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    suspend fun deleteReport(schoolId: String, reportId: String): Result<Unit> {
        return try {
            TenantPathProvider.verifyTenantBoundary(schoolId)
            val path = TenantPathProvider.reportsPath(schoolId)
            db.collection(path).document(reportId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    fun buildCsvExportPayload(
        bundle: SchoolAnalyticsBundle,
        report: ExecutiveReportSnapshot
    ): String {
        return buildString {
            appendLine("ReportCode,SchoolId,Domain,Period,GeneratedBy,GeneratedDate")
            appendLine("${report.reportCode},${bundle.schoolId},${report.domain.name},${report.period.name},${report.generatedBy},${report.generatedDate}")
            appendLine("Metric,PrimaryValue,SecondaryValue")
            appendLine("TotalStudents,${bundle.totalStudents},ActiveFaculty=${bundle.activeFaculty}")
            appendLine("AverageAttendancePct,${bundle.averageAttendancePct}%,PendingLeaves=${bundle.pendingLeavesCount}")
            appendLine("FeeCollectionRatePct,${bundle.feeCollectionRatePct}%,Collected=${bundle.collectedRevenueLabel};Pending=${bundle.pendingRevenueLabel}")
            appendLine("AcademicGpa,${bundle.averageGpa},PassRate=${bundle.passRatePct}%")
            appendLine("Operations,LibraryUtil=${bundle.libraryUtilizationPct}%,FleetUtil=${bundle.fleetUtilizationPct}%")
        }.trim()
    }

    fun getPreSeededReports(schoolId: String): List<ExecutiveReportSnapshot> {
        return if (schoolId.contains("oxford") || schoolId.startsWith("sch_oxf")) {
            listOf(
                ExecutiveReportSnapshot(
                    id = "rpt_oxf_01",
                    schoolId = schoolId,
                    reportCode = "RPT-OXF-201",
                    title = "Term 1 Institutional Executive Scorecard",
                    domain = AnalyticsDomain.OVERVIEW,
                    period = ReportPeriod.TERM_1,
                    generatedBy = "Dr. Robert Vance",
                    generatedDate = "2026-04-08",
                    headlineKpi = "94.2% Attendance • 3.68 Avg GPA • 85.6% Fees Collected",
                    summaryNotes = "Board-ready quarterly rollup across academic results, daily attendance, and bursar fee collections.",
                    exportFormat = "PDF",
                    createdAt = 1712534400000L
                ),
                ExecutiveReportSnapshot(
                    id = "rpt_oxf_02",
                    schoolId = schoolId,
                    reportCode = "RPT-OXF-202",
                    title = "Bursar Tuition & Pending Ledger Audit",
                    domain = AnalyticsDomain.FINANCE,
                    period = ReportPeriod.TERM_1,
                    generatedBy = "Marcus Vance (Finance)",
                    generatedDate = "2026-04-06",
                    headlineKpi = "$84,500 Collected • $14,250 Outstanding",
                    summaryNotes = "Grade-level tuition reconciliation and overdue invoice aging breakdown.",
                    exportFormat = "CSV",
                    createdAt = 1712361600000L
                ),
                ExecutiveReportSnapshot(
                    id = "rpt_oxf_03",
                    schoolId = schoolId,
                    reportCode = "RPT-OXF-203",
                    title = "Senior School GPA & Honor Roll Distribution",
                    domain = AnalyticsDomain.ACADEMICS,
                    period = ReportPeriod.ANNUAL,
                    generatedBy = "Dr. Sarah Jenkins",
                    generatedDate = "2026-04-03",
                    headlineKpi = "97.4% Pass Rate • 64% A / A+ Cohort",
                    summaryNotes = "Term examination performance benchmark across STEM and Humanities cohorts.",
                    exportFormat = "PDF",
                    createdAt = 1712102400000L
                )
            )
        } else {
            listOf(
                ExecutiveReportSnapshot(
                    id = "rpt_cam_01",
                    schoolId = schoolId,
                    reportCode = "RPT-CAM-101",
                    title = "Cambridge High Term 1 Executive Digest",
                    domain = AnalyticsDomain.OVERVIEW,
                    period = ReportPeriod.TERM_1,
                    generatedBy = "Prof. Eleanor Vance",
                    generatedDate = "2026-04-07",
                    headlineKpi = "92.6% Attendance • 3.54 Avg GPA • 85.7% Fees Collected",
                    summaryNotes = "Multi-department executive scorecard for Cambridge campus governance.",
                    exportFormat = "PDF",
                    createdAt = 1712448000000L
                ),
                ExecutiveReportSnapshot(
                    id = "rpt_cam_02",
                    schoolId = schoolId,
                    reportCode = "RPT-CAM-102",
                    title = "Cambridge Transport & Library Utilization CSV",
                    domain = AnalyticsDomain.OPERATIONS,
                    period = ReportPeriod.MONTHLY,
                    generatedBy = "Eleanor Rigby",
                    generatedDate = "2026-04-05",
                    headlineKpi = "Fleet & Library Capacity Utilization Audit",
                    summaryNotes = "Monthly operational utilization metrics for campus transit routes and book circulation.",
                    exportFormat = "CSV",
                    createdAt = 1712275200000L
                )
            )
        }
    }
}
