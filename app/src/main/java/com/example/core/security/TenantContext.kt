package com.example.core.security

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * TenantContext is the core multi-tenant security barrier in EduManage.
 * It holds the verified active school ID and tenant session details.
 * Every repository and network operation must validate against this context
 * to prevent cross-school data pollution or illegal tenant queries.
 */
object TenantContext {
    private val _activeSchoolId = MutableStateFlow<String?>(null)
    val activeSchoolId: StateFlow<String?> = _activeSchoolId.asStateFlow()

    private val _activeUserRole = MutableStateFlow(UserRole.SCHOOL_ADMIN)
    val activeUserRole: StateFlow<UserRole> = _activeUserRole.asStateFlow()

    private val _activeUserId = MutableStateFlow<String?>("usr_admin_demo")
    val activeUserId: StateFlow<String?> = _activeUserId.asStateFlow()

    /**
     * Set the verified tenant school context after authentication.
     */
    fun setTenant(schoolId: String, role: UserRole, userId: String) {
        _activeSchoolId.value = schoolId
        _activeUserRole.value = role
        _activeUserId.value = userId
    }

    /**
     * Clear context on logout or tenant switch.
     */
    fun clear() {
        _activeSchoolId.value = null
        _activeUserRole.value = UserRole.STUDENT
        _activeUserId.value = null
    }

    /**
     * Validates that an active school tenant is set before executing tenant-bound operations.
     * Throws IllegalStateException if tenant boundary is missing.
     */
    fun requireTenantId(): String {
        return _activeSchoolId.value ?: throw IllegalStateException("Multi-Tenant Error: No active school tenant is bound to the current session.")
    }

    /**
     * Verifies if current user has the specified permission.
     */
    fun hasPermission(permission: Permission): Boolean {
        val permissions = Permission.getPermissionsForRole(_activeUserRole.value)
        return permissions.contains(permission)
    }
}
