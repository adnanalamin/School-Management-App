package com.example.features.auth.domain

import com.example.core.error.AppError
import com.example.shared.models.UserProfile

/**
 * Standardized authentication operation results.
 */
sealed class AuthResult<out T> {
    data class Success<T>(val data: T) : AuthResult<T>()
    data class Failure(val error: AppError) : AuthResult<Nothing>()
    data object Loading : AuthResult<Nothing>()
}
