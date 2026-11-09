package com.example.features.auth.domain

import com.example.shared.models.UserProfile
import kotlinx.coroutines.flow.Flow

/**
 * Authentication repository interface defining cloud authentication,
 * tenant session binding, password reset, and Google identity federation.
 */
interface AuthRepository {
    val authState: Flow<UserProfile?>
    fun getCurrentUser(): UserProfile?
    suspend fun signInWithGoogle(idToken: String, schoolId: String? = null): Result<UserProfile>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun sendEmailVerification(): Result<Unit>
    suspend fun signOut(): Result<Unit>
    suspend fun validateAndBindTenant(schoolCode: String): Result<String>
}
