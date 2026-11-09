package com.example.features.rbac

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.security.Permission
import com.example.core.security.RbacEngine
import com.example.core.security.RbacEvaluationResult
import com.example.core.security.RoleSummary
import com.example.core.security.TenantContext
import com.example.core.security.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RbacMatrixUiState(
    val inspectedRole: UserRole = UserRole.SCHOOL_ADMIN,
    val activeSessionRole: UserRole = UserRole.SCHOOL_ADMIN,
    val roleSummary: RoleSummary = RbacEngine.getRoleSummary(UserRole.SCHOOL_ADMIN),
    val allRoles: List<UserRole> = RbacEngine.getAllRolesHierarchical(),
    val categories: List<String> = RbacEngine.categories.keys.toList(),
    val selectedCategory: String? = null,
    val searchQuery: String = "",
    val inspectedRolePermissions: Set<Permission> = RbacEngine.getPermissionsForRole(UserRole.SCHOOL_ADMIN),
    val testActionPermission: Permission = Permission.FEES_COLLECT,
    val simulationResult: RbacEvaluationResult? = null,
    val customGrants: Set<Permission> = emptySet(),
    val customRevocations: Set<Permission> = emptySet(),
    val notificationFeedback: String? = null
)

class RbacMatrixViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RbacMatrixUiState())
    val uiState: StateFlow<RbacMatrixUiState> = _uiState.asStateFlow()

    init {
        // Collect current active session role and overrides from TenantContext
        viewModelScope.launch {
            TenantContext.activeUserRole.collect { activeRole ->
                _uiState.update { it.copy(activeSessionRole = activeRole) }
            }
        }
        viewModelScope.launch {
            TenantContext.customGrants.collect { grants ->
                _uiState.update { it.copy(customGrants = grants) }
            }
        }
        viewModelScope.launch {
            TenantContext.customRevocations.collect { revs ->
                _uiState.update { it.copy(customRevocations = revs) }
            }
        }
        runSimulation(Permission.FEES_COLLECT)
    }

    fun selectInspectedRole(role: UserRole) {
        val perms = RbacEngine.getPermissionsForRole(role)
        val summary = RbacEngine.getRoleSummary(role)
        _uiState.update {
            it.copy(
                inspectedRole = role,
                inspectedRolePermissions = perms,
                roleSummary = summary
            )
        }
        runSimulation()
    }

    fun selectCategory(category: String?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setSimulationPermission(permission: Permission) {
        _uiState.update { it.copy(testActionPermission = permission) }
        runSimulation(permission)
    }

    fun runSimulation(permission: Permission? = null) {
        val targetPerm = permission ?: _uiState.value.testActionPermission
        val currentRole = _uiState.value.inspectedRole
        val result = RbacEngine.evaluateAction(currentRole, targetPerm)
        _uiState.update {
            it.copy(
                testActionPermission = targetPerm,
                simulationResult = result
            )
        }
    }

    fun assumeRoleForActiveSession(role: UserRole) {
        val currentSchool = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        TenantContext.setTenant(
            schoolId = currentSchool,
            role = role,
            userId = "usr_${role.name.lowercase()}_sim"
        )
        _uiState.update {
            it.copy(
                notificationFeedback = "Active Session switched to ${role.displayName}. Bottom bar & navigation gates updated."
            )
        }
    }

    fun togglePermissionOverride(permission: Permission) {
        val currentEffective = TenantContext.hasPermission(permission)
        if (currentEffective) {
            TenantContext.revokePermission(permission)
            _uiState.update {
                it.copy(notificationFeedback = "Revoked '${permission.key}' from active session.")
            }
        } else {
            TenantContext.grantPermission(permission)
            _uiState.update {
                it.copy(notificationFeedback = "Granted '${permission.key}' to active session.")
            }
        }
    }

    fun resetAllOverrides() {
        TenantContext.resetOverrides()
        _uiState.update {
            it.copy(notificationFeedback = "Reset all dynamic overrides back to default role permissions.")
        }
    }

    fun dismissFeedback() {
        _uiState.update { it.copy(notificationFeedback = null) }
    }

    fun getFilteredPermissions(): List<Permission> {
        val state = _uiState.value
        val sourceList = if (state.selectedCategory != null) {
            RbacEngine.categories[state.selectedCategory] ?: Permission.entries
        } else {
            Permission.entries
        }

        return if (state.searchQuery.isBlank()) {
            sourceList
        } else {
            sourceList.filter { perm ->
                perm.key.contains(state.searchQuery, ignoreCase = true) ||
                    perm.description.contains(state.searchQuery, ignoreCase = true)
            }
        }
    }
}
