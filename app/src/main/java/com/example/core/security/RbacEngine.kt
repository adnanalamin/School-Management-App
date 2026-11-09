package com.example.core.security

/**
 * Result of evaluating a specific action against a user's role and permissions.
 */
data class RbacEvaluationResult(
    val isAllowed: Boolean,
    val role: UserRole,
    val permission: Permission,
    val reason: String,
    val securityLevelRequired: Int
)

/**
 * Summary metadata for a specific user role.
 */
data class RoleSummary(
    val role: UserRole,
    val permissionCount: Int,
    val totalAvailablePermissions: Int,
    val securityLevel: Int,
    val primaryDescription: String
)

/**
 * RbacEngine implements the centralized authorization logic for all 9 roles and granular permissions.
 */
object RbacEngine {

    /**
     * Functional categories grouping all permissions for UX clarity and security audits.
     */
    val categories: Map<String, List<Permission>> = mapOf(
        "Institution & System Settings" to listOf(
            Permission.SCHOOL_CONFIG_VIEW,
            Permission.SCHOOL_CONFIG_EDIT
        ),
        "Student Lifecycle & Enrollment" to listOf(
            Permission.STUDENTS_VIEW,
            Permission.STUDENTS_CREATE,
            Permission.STUDENTS_UPDATE,
            Permission.STUDENTS_DELETE
        ),
        "Faculty & Human Resources" to listOf(
            Permission.TEACHERS_VIEW,
            Permission.TEACHERS_CREATE,
            Permission.TEACHERS_UPDATE,
            Permission.TEACHERS_DELETE
        ),
        "Academic Classes & Schedules" to listOf(
            Permission.CLASSES_VIEW,
            Permission.CLASSES_MANAGE
        ),
        "Attendance Management" to listOf(
            Permission.ATTENDANCE_VIEW,
            Permission.ATTENDANCE_MARK,
            Permission.ATTENDANCE_EDIT
        ),
        "Tuition & Financial Accounting" to listOf(
            Permission.FEES_VIEW,
            Permission.FEES_CREATE,
            Permission.FEES_COLLECT,
            Permission.FEES_REPORTS
        ),
        "Exams, Grading & Report Cards" to listOf(
            Permission.EXAMS_VIEW,
            Permission.EXAMS_MANAGE,
            Permission.RESULTS_VIEW,
            Permission.RESULTS_ENTER,
            Permission.RESULTS_PUBLISH
        ),
        "Homework & Submissions" to listOf(
            Permission.HOMEWORK_VIEW,
            Permission.HOMEWORK_CREATE,
            Permission.HOMEWORK_SUBMIT,
            Permission.HOMEWORK_GRADE
        ),
        "Notices & School Circulars" to listOf(
            Permission.NOTICES_VIEW,
            Permission.NOTICES_PUBLISH
        ),
        "Library & Learning Resources" to listOf(
            Permission.LIBRARY_VIEW,
            Permission.LIBRARY_MANAGE
        ),
        "Fleet Transport & Bus Logistics" to listOf(
            Permission.TRANSPORT_VIEW,
            Permission.TRANSPORT_MANAGE
        ),
        "Communication & Channels" to listOf(
            Permission.MESSAGES_VIEW,
            Permission.MESSAGES_SEND
        )
    )

    /**
     * Returns the complete set of permissions assigned to a given role.
     */
    fun getPermissionsForRole(role: UserRole): Set<Permission> {
        return Permission.getPermissionsForRole(role)
    }

    /**
     * Checks if a role is authorized for a specific permission.
     */
    fun isActionAllowed(role: UserRole, permission: Permission): Boolean {
        return getPermissionsForRole(role).contains(permission)
    }

    /**
     * Evaluates permission and produces an audit trail explanation.
     */
    fun evaluateAction(role: UserRole, permission: Permission): RbacEvaluationResult {
        val allowed = isActionAllowed(role, permission)
        val reason = if (allowed) {
            "Action '${permission.key}' is permitted for role '${role.displayName}'."
        } else {
            "Access Denied: Role '${role.displayName}' lacks '${permission.key}'. Required security clearance: Level ${role.level}."
        }
        return RbacEvaluationResult(
            isAllowed = allowed,
            role = role,
            permission = permission,
            reason = reason,
            securityLevelRequired = role.level
        )
    }

    /**
     * Returns structured role summary and privilege density.
     */
    fun getRoleSummary(role: UserRole): RoleSummary {
        val perms = getPermissionsForRole(role)
        val description = when (role) {
            UserRole.SUPER_ADMIN -> "Platform owner with unrestricted omnipotent administrative authority."
            UserRole.SCHOOL_ADMIN -> "Principal / Executive officer managing entire school operations and staff."
            UserRole.TEACHER -> "Academic instructor managing classes, attendance, grading, and assignments."
            UserRole.ACCOUNTANT -> "Bursar managing billing, student tuition fees, invoices, and ledgers."
            UserRole.LIBRARIAN -> "Resource officer cataloging books, managing circulation, and library assets."
            UserRole.TRANSPORT_MANAGER -> "Logistics officer managing bus fleets, drivers, routes, and transit."
            UserRole.STAFF -> "Operational support staff with general announcements and attendance visibility."
            UserRole.PARENT -> "Guardian with access to ward's grades, fee balances, bus route, and attendance."
            UserRole.STUDENT -> "Learner accessing class timetable, homework submissions, results, and books."
        }
        return RoleSummary(
            role = role,
            permissionCount = perms.size,
            totalAvailablePermissions = Permission.entries.size,
            securityLevel = role.level,
            primaryDescription = description
        )
    }

    /**
     * Returns all 9 platform roles sorted by security hierarchy level (highest first).
     */
    fun getAllRolesHierarchical(): List<UserRole> {
        return UserRole.entries.sortedByDescending { it.level }
    }
}
