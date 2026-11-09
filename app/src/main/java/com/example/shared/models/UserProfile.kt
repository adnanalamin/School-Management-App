package com.example.shared.models

import com.example.core.security.UserRole

/**
 * UserProfile represents an authenticated user linked to a school tenant.
 */
data class UserProfile(
    val uid: String,
    val email: String,
    val displayName: String,
    val schoolId: String?,
    val role: UserRole,
    val phone: String = "",
    val photoUrl: String? = null,
    val assignedClassIds: List<String> = emptyList(),
    val assignedStudentIds: List<String> = emptyList(), // For parents linked to their children
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
