package com.example.features.audit.domain

/**
 * Audit severity level for a security or performance diagnostic check.
 */
enum class AuditSeverity(val label: String) {
    PASS("PASS"),
    INFO("INFO"),
    WARNING("WARN"),
    CRITICAL("CRITICAL")
}

/**
 * Security & performance audit domains covered by Phase 25.
 */
enum class AuditCategory(val displayName: String) {
    MULTI_TENANCY("Tenant Isolation"),
    RBAC_SECURITY("RBAC & Guards"),
    FIRESTORE_RULES("Rules & Schema"),
    AUTH_COMPLIANCE("Auth & Input"),
    PERFORMANCE_TUNING("Compose & Perf")
}

/**
 * Individual programmatic diagnostic result.
 */
data class SecurityAuditCheck(
    val id: String,
    val title: String,
    val category: AuditCategory,
    val severity: AuditSeverity,
    val detail: String,
    val remediation: String = ""
)

/**
 * Live JVM micro-benchmark and Compose optimization metrics.
 */
data class PerformanceMetricSnapshot(
    val rbacEval1kTimeMs: Double,
    val tenantBoundary1kTimeMs: Double,
    val validation1kTimeMs: Double,
    val categorizedPermissionsCount: Int,
    val protectedRoutesCount: Int,
    val lazyKeyedListsVerified: Int
) {
    val totalBenchmarkMs: Double
        get() = rbacEval1kTimeMs + tenantBoundary1kTimeMs + validation1kTimeMs
}

/**
 * Overall platform compliance summary score.
 */
data class ComplianceSummary(
    val totalChecks: Int,
    val passedChecks: Int,
    val warningChecks: Int,
    val criticalChecks: Int,
    val scorePercent: Int,
    val gradeLabel: String
)
