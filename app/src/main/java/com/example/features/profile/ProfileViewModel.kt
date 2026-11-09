package com.example.features.profile

import androidx.lifecycle.ViewModel
import com.example.core.security.Permission
import com.example.core.security.TenantContext
import com.example.core.security.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ProfileUiState(
    val name: String = "Dr. Robert Vance",
    val email: String = "admin@oxfordacademy.edu",
    val phone: String = "+1 (555) 234-5678",
    val role: UserRole = UserRole.SCHOOL_ADMIN,
    val schoolName: String = "Oxford International Academy",
    val schoolCode: String = "OXF-101",
    val academicSession: String = "2026-2027",
    val memberSince: String = "12 Aug 2024",
    val showRoleSwitchDialog: Boolean = false
) {
    val permissions: Set<Permission>
        get() = Permission.getPermissionsForRole(role)
}

class ProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        refreshProfile()
    }

    fun refreshProfile() {
        val currentRole = TenantContext.activeUserRole.value
        val name = when (currentRole) {
            UserRole.SUPER_ADMIN -> "Platform Super Admin"
            UserRole.SCHOOL_ADMIN -> "Dr. Robert Vance"
            UserRole.TEACHER -> "Sarah Jenkins, M.Ed."
            UserRole.STUDENT -> "Amina Rahman"
            UserRole.PARENT -> "David Rahman"
            UserRole.ACCOUNTANT -> "Marcus Vance, CPA"
            else -> "Staff Member"
        }
        val email = when (currentRole) {
            UserRole.SUPER_ADMIN -> "superadmin@edumanage.com"
            UserRole.SCHOOL_ADMIN -> "admin@oxfordacademy.edu"
            UserRole.TEACHER -> "teacher.sarah@oxfordacademy.edu"
            UserRole.STUDENT -> "student.amina@oxfordacademy.edu"
            UserRole.PARENT -> "parent.david@gmail.com"
            UserRole.ACCOUNTANT -> "accounts@oxfordacademy.edu"
            else -> "staff.john@oxfordacademy.edu"
        }

        _uiState.update {
            it.copy(
                role = currentRole,
                name = name,
                email = email
            )
        }
    }

    fun openRoleSwitchDialog() {
        _uiState.update { it.copy(showRoleSwitchDialog = true) }
    }

    fun closeRoleSwitchDialog() {
        _uiState.update { it.copy(showRoleSwitchDialog = false) }
    }

    fun switchRole(newRole: UserRole) {
        TenantContext.setTenant(
            schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01",
            role = newRole,
            userId = "usr_${newRole.name.lowercase()}_001"
        )
        _uiState.update { it.copy(showRoleSwitchDialog = false) }
        refreshProfile()
    }

    fun signOut(onSignedOut: () -> Unit) {
        TenantContext.clear()
        onSignedOut()
    }
}
