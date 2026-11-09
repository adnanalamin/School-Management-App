package com.example.core.error

/**
 * Standardized domain error hierarchy for EduManage SaaS.
 */
sealed class AppError(
    val userFriendlyMessage: String,
    val technicalDetails: String? = null,
    val causeThrowable: Throwable? = null
) : Exception(userFriendlyMessage, causeThrowable) {

    class NetworkError(
        userMessage: String = "Network connection issue. Please check your internet and try again.",
        technical: String? = null,
        cause: Throwable? = null
    ) : AppError(userMessage, technical, cause)

    class TenantIsolationError(
        userMessage: String = "Access denied: Unauthorized school tenant request.",
        technical: String? = null
    ) : AppError(userMessage, technical)

    class AuthenticationError(
        userMessage: String = "Invalid login credentials. Please verify your details.",
        technical: String? = null,
        cause: Throwable? = null
    ) : AppError(userMessage, technical, cause)

    class AuthorizationError(
        userMessage: String = "You do not have permission to perform this action.",
        technical: String? = null
    ) : AppError(userMessage, technical)

    class ValidationError(
        userMessage: String,
        val fieldErrors: Map<String, String> = emptyMap()
    ) : AppError(userMessage)

    class ResourceNotFoundError(
        userMessage: String = "The requested record was not found.",
        technical: String? = null
    ) : AppError(userMessage, technical)

    class ServerError(
        userMessage: String = "Something went wrong on our servers. Please try again shortly.",
        technical: String? = null,
        cause: Throwable? = null
    ) : AppError(userMessage, technical, cause)

    class UnknownError(
        userMessage: String = "An unexpected error occurred. Please try again.",
        technical: String? = null,
        cause: Throwable? = null
    ) : AppError(userMessage, technical, cause)
}
