package com.example.core.error

import android.util.Log

/**
 * Centralized error handler to sanitize technical exceptions into user-friendly messages.
 */
object ErrorHandler {
    private const val TAG = "EduManageError"

    fun sanitize(throwable: Throwable): AppError {
        Log.e(TAG, "Unhandled error caught: ${throwable.message}", throwable)
        return when (throwable) {
            is AppError -> throwable
            is java.net.UnknownHostException,
            is java.net.SocketTimeoutException,
            is java.io.IOException -> {
                AppError.NetworkError(
                    technical = "${throwable::class.java.simpleName}: ${throwable.message}",
                    cause = throwable
                )
            }
            is SecurityException -> {
                AppError.AuthorizationError(
                    technical = throwable.message
                )
            }
            is IllegalArgumentException -> {
                AppError.ValidationError(
                    userMessage = throwable.message ?: "Invalid data provided."
                )
            }
            else -> {
                AppError.UnknownError(
                    technical = "${throwable::class.java.simpleName}: ${throwable.message}",
                    cause = throwable
                )
            }
        }
    }
}
