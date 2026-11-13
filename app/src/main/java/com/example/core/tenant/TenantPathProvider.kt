package com.example.core.tenant

import com.example.core.constants.AppConstants
import com.example.core.error.AppError
import com.example.core.security.TenantContext

/**
 * TenantPathProvider enforces strict multi-tenant Firestore hierarchy.
 * Guarantees that every isolated sub-collection path is scoped strictly to a validated schoolId:
 * schools/{schoolId}/{subcollection}
 *
 * Prevents cross-school data leakage and invalid unscoped root reads.
 */
object TenantPathProvider {

    /**
     * Validates that a schoolId conforms to the multi-tenant identifier specification.
     */
    fun validateSchoolId(schoolId: String): String {
        val clean = schoolId.trim()
        if (clean.isBlank()) {
            throw AppError.SecurityViolation("Operation blocked: Missing required school tenant identifier.")
        }
        if (!clean.matches(Regex("^[a-zA-Z0-9_-]{3,64}$"))) {
            throw AppError.SecurityViolation("Operation blocked: Invalid tenant identifier format '$clean'.")
        }
        return clean
    }

    /**
     * Ensures an active tenant is selected in TenantContext or returns the specified fallback.
     */
    fun requireActiveSchoolId(): String {
        val active = TenantContext.activeSchoolId.value
        if (active.isNullOrBlank()) {
            throw AppError.SecurityViolation("Access denied: No active school workspace session bound.")
        }
        return validateSchoolId(active)
    }

    /**
     * Root School Document path: schools/{schoolId}
     */
    fun schoolPath(schoolId: String = requireActiveSchoolId()): String {
        return "${AppConstants.COLLECTION_SCHOOLS}/${validateSchoolId(schoolId)}"
    }

    // --- The 15 Isolated Sub-Collections required by Phase 6 ---

    fun studentsPath(schoolId: String = requireActiveSchoolId()): String =
        "${schoolPath(schoolId)}/${AppConstants.SUB_COLLECTION_STUDENTS}"

    fun teachersPath(schoolId: String = requireActiveSchoolId()): String =
        "${schoolPath(schoolId)}/${AppConstants.SUB_COLLECTION_TEACHERS}"

    fun parentsPath(schoolId: String = requireActiveSchoolId()): String =
        "${schoolPath(schoolId)}/${AppConstants.SUB_COLLECTION_PARENTS}"

    fun classesPath(schoolId: String = requireActiveSchoolId()): String =
        "${schoolPath(schoolId)}/${AppConstants.SUB_COLLECTION_CLASSES}"

    fun sectionsPath(schoolId: String = requireActiveSchoolId()): String =
        "${schoolPath(schoolId)}/${AppConstants.SUB_COLLECTION_SECTIONS}"

    fun attendancePath(schoolId: String = requireActiveSchoolId()): String =
        "${schoolPath(schoolId)}/${AppConstants.SUB_COLLECTION_ATTENDANCE}"

    fun feesPath(schoolId: String = requireActiveSchoolId()): String =
        "${schoolPath(schoolId)}/${AppConstants.SUB_COLLECTION_FEES}"

    fun examsPath(schoolId: String = requireActiveSchoolId()): String =
        "${schoolPath(schoolId)}/${AppConstants.SUB_COLLECTION_EXAMS}"

    fun resultsPath(schoolId: String = requireActiveSchoolId()): String =
        "${schoolPath(schoolId)}/${AppConstants.SUB_COLLECTION_RESULTS}"

    fun noticesPath(schoolId: String = requireActiveSchoolId()): String =
        "${schoolPath(schoolId)}/${AppConstants.SUB_COLLECTION_NOTICES}"

    fun homeworkPath(schoolId: String = requireActiveSchoolId()): String =
        "${schoolPath(schoolId)}/${AppConstants.SUB_COLLECTION_HOMEWORK}"

    fun libraryPath(schoolId: String = requireActiveSchoolId()): String =
        "${schoolPath(schoolId)}/${AppConstants.SUB_COLLECTION_LIBRARY}"

    fun transportPath(schoolId: String = requireActiveSchoolId()): String =
        "${schoolPath(schoolId)}/${AppConstants.SUB_COLLECTION_TRANSPORT}"

    fun eventsPath(schoolId: String = requireActiveSchoolId()): String =
        "${schoolPath(schoolId)}/${AppConstants.SUB_COLLECTION_EVENTS}"

    fun documentsPath(schoolId: String = requireActiveSchoolId()): String =
        "${schoolPath(schoolId)}/${AppConstants.SUB_COLLECTION_DOCUMENTS}"

    fun leavesPath(schoolId: String = requireActiveSchoolId()): String =
        "${schoolPath(schoolId)}/${AppConstants.SUB_COLLECTION_LEAVES}"

    /**
     * Cross-School Firewall: Verifies whether the requested school target matches the active session.
     */
    fun verifyTenantBoundary(targetSchoolId: String) {
        val active = TenantContext.activeSchoolId.value
        if (active != null && active != targetSchoolId) {
            throw AppError.SecurityViolation(
                "Cross-School Access Blocked: Active tenant is '$active', but attempted to query '$targetSchoolId'."
            )
        }
    }
}
