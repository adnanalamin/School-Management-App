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

    private val _customGrants = MutableStateFlow<Set<Permission>>(emptySet())
    val customGrants: StateFlow<Set<Permission>> = _customGrants.asStateFlow()

    private val _customRevocations = MutableStateFlow<Set<Permission>>(emptySet())
    val customRevocations: StateFlow<Set<Permission>> = _customRevocations.asStateFlow()

    /**
     * Set the verified tenant school context after authentication.
     */
    fun setTenant(schoolId: String, role: UserRole, userId: String) {
        _activeSchoolId.value = schoolId
        _activeUserRole.value = role
        _activeUserId.value = userId
        resetOverrides()
    }

    /**
     * Clear context on logout or tenant switch.
     */
    fun clear() {
        _activeSchoolId.value = null
        _activeUserRole.value = UserRole.STUDENT
        _activeUserId.value = null
        resetOverrides()
    }

    /**
     * Dynamic RBAC Testing: Temporarily grant a permission in the active session.
     */
    fun grantPermission(permission: Permission) {
        _customRevocations.value = _customRevocations.value - permission
        _customGrants.value = _customGrants.value + permission
    }

    /**
     * Dynamic RBAC Testing: Temporarily revoke a permission from the active session.
     */
    fun revokePermission(permission: Permission) {
        _customGrants.value = _customGrants.value - permission
        _customRevocations.value = _customRevocations.value + permission
    }

    /**
     * Resets any dynamic overrides back to pure role-based permissions.
     */
    fun resetOverrides() {
        _customGrants.value = emptySet()
        _customRevocations.value = emptySet()
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
     * Evaluates base role permissions + custom grants - custom revocations.
     */
    fun hasPermission(permission: Permission): Boolean {
        if (_customRevocations.value.contains(permission)) return false
        if (_customGrants.value.contains(permission)) return true
        val basePermissions = Permission.getPermissionsForRole(_activeUserRole.value)
        return basePermissions.contains(permission)
    }
}
