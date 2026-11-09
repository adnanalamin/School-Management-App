package com.example.core.navigation

import com.example.core.security.TenantContext
import com.example.core.security.UserRole

/**
 * RouteGuard evaluates route authorization and tenant status before navigation.
 * Guarantees that unauthorized users cannot navigate to privileged screens.
 */
object RouteGuard {

    sealed class GuardResult {
        data object Authorized : GuardResult()
        data class Redirect(val targetScreen: AppScreen, val reason: String) : GuardResult()
    }

    /**
     * Checks if the active session is permitted to navigate to destination.
     */
    fun evaluate(destination: AppScreen): GuardResult {
        // Public screens accessible without session
        if (destination == AppScreen.Splash ||
            destination == AppScreen.Onboarding ||
            destination == AppScreen.Login ||
            destination == AppScreen.Register ||
            destination == AppScreen.Foundation ||
            destination == AppScreen.DesignSystem
        ) {
            return GuardResult.Authorized
        }

        // Check if school tenant is bound to session
        val schoolId = TenantContext.activeSchoolId.value
        if (schoolId == null) {
            return GuardResult.Redirect(
                targetScreen = AppScreen.Login,
                reason = "Please sign in to access your school workspace."
            )
        }

        // Check Role-based authorization
        val role = TenantContext.activeUserRole.value
        if (!destination.allowedRoles.contains(role)) {
            return GuardResult.Redirect(
                targetScreen = AppScreen.MainDashboard,
                reason = "Access restricted: '${role.displayName}' cannot access ${destination.title}."
            )
        }

        // Check granular permission requirement
        destination.requiredPermission?.let { permission ->
            if (!TenantContext.hasPermission(permission)) {
                return GuardResult.Redirect(
                    targetScreen = AppScreen.MainDashboard,
                    reason = "Missing required permission: ${permission.key}"
                )
            }
        }

        return GuardResult.Authorized
    }
}
