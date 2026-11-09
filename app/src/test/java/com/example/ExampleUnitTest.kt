package com.example

import androidx.compose.ui.unit.dp
import com.example.core.config.AppConfig
import com.example.core.config.EnvironmentType
import com.example.core.navigation.AppScreen
import com.example.core.navigation.RouteGuard
import com.example.core.security.Permission
import com.example.core.security.TenantContext
import com.example.core.security.UserRole
import com.example.core.theme.EduElevation
import com.example.core.theme.EduSpacing
import com.example.core.utils.ValidationUtils
import com.example.features.auth.LoginViewModel
import com.example.features.designsystem.DesignSystemViewModel
import com.example.features.onboarding.OnboardingViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * JVM unit tests for EduManage core architecture, RBAC, Design System tokens, and ViewModels.
 */
class ExampleUnitTest {

    @Test
    fun verifySuperAdminHasAllPermissions() {
        val permissions = Permission.getPermissionsForRole(UserRole.SUPER_ADMIN)
        assertEquals(Permission.entries.size, permissions.size)
    }

    @Test
    fun verifyStudentRoleHasLimitedPermissions() {
        val permissions = Permission.getPermissionsForRole(UserRole.STUDENT)
        assertFalse(permissions.contains(Permission.FEES_COLLECT))
        assertFalse(permissions.contains(Permission.STUDENTS_CREATE))
        assertFalse(permissions.contains(Permission.TEACHERS_CREATE))
        assertTrue(permissions.contains(Permission.HOMEWORK_SUBMIT))
        assertTrue(permissions.contains(Permission.CLASSES_VIEW))
    }

    @Test
    fun verifyTenantContextIsolationEnforcement() {
        TenantContext.clear()
        val result = RouteGuard.evaluate(AppScreen.StudentsList)
        assertTrue(result is RouteGuard.GuardResult.Redirect)
    }

    @Test
    fun verifyAppConfigDefaults() {
        val config = AppConfig.current
        assertEquals(EnvironmentType.DEVELOPMENT, config.environment)
        assertTrue(config.isMultiTenantStrictIsolationEnabled)
    }

    @Test
    fun verifyDesignSystemSpacingAndElevationTokens() {
        assertEquals(48.dp, EduSpacing.touchTargetMin)
        assertEquals(16.dp, EduSpacing.lg)
        assertEquals(1.dp, EduElevation.level1)
        assertEquals(6.dp, EduElevation.level3)
    }

    @Test
    fun verifyFormValidationRules() {
        assertTrue(ValidationUtils.isValidEmail("admin@edumanage.com"))
        assertFalse(ValidationUtils.isValidEmail("invalid-email"))
        assertTrue(ValidationUtils.isValidPassword("StrongPass123!"))
        assertFalse(ValidationUtils.isValidPassword("short"))
        assertTrue(ValidationUtils.isValidSchoolCode("OXF101"))
        assertFalse(ValidationUtils.isValidSchoolCode("bad code!!"))
    }

    @Test
    fun verifyDesignSystemViewModelInteractions() {
        val viewModel = DesignSystemViewModel()
        assertEquals("Grade 10 - Science", viewModel.uiState.value.selectedClass)

        viewModel.onClassSelected("Grade 9 - Science")
        assertEquals("Grade 9 - Science", viewModel.uiState.value.selectedClass)

        viewModel.onTextInputChanged("ab")
        assertNotNull(viewModel.uiState.value.textInputError)

        viewModel.onTextInputChanged("Valid Name")
        assertNull(viewModel.uiState.value.textInputError)

        viewModel.openConfirmDialog()
        assertTrue(viewModel.uiState.value.showConfirmDialog)
        viewModel.closeConfirmDialog()
        assertFalse(viewModel.uiState.value.showConfirmDialog)
    }

    @Test
    fun verifyRouteGuardPublicAndProtectedDestinations() {
        // Public routes
        assertTrue(RouteGuard.evaluate(AppScreen.Splash) is RouteGuard.GuardResult.Authorized)
        assertTrue(RouteGuard.evaluate(AppScreen.Onboarding) is RouteGuard.GuardResult.Authorized)
        assertTrue(RouteGuard.evaluate(AppScreen.Login) is RouteGuard.GuardResult.Authorized)
        assertTrue(RouteGuard.evaluate(AppScreen.Register) is RouteGuard.GuardResult.Authorized)

        // Unauthenticated check
        TenantContext.clear()
        val redirectResult = RouteGuard.evaluate(AppScreen.MainDashboard)
        assertTrue(redirectResult is RouteGuard.GuardResult.Redirect)

        // Authenticated check with Student role attempting to access Fees
        TenantContext.setTenant("sch_oxford_01", UserRole.STUDENT, "usr_student_01")
        val feeAccessResult = RouteGuard.evaluate(AppScreen.Fees)
        assertTrue(feeAccessResult is RouteGuard.GuardResult.Redirect)

        // Authenticated check with School Admin accessing MainDashboard and Fees
        TenantContext.setTenant("sch_oxford_01", UserRole.SCHOOL_ADMIN, "usr_admin_01")
        assertTrue(RouteGuard.evaluate(AppScreen.MainDashboard) is RouteGuard.GuardResult.Authorized)
        assertTrue(RouteGuard.evaluate(AppScreen.Fees) is RouteGuard.GuardResult.Authorized)
    }

    @Test
    fun verifyOnboardingPagination() {
        val viewModel = OnboardingViewModel()
        assertEquals(0, viewModel.uiState.value.currentPage)
        assertTrue(viewModel.nextPage())
        assertEquals(1, viewModel.uiState.value.currentPage)
        assertTrue(viewModel.nextPage())
        assertEquals(2, viewModel.uiState.value.currentPage)
        assertFalse(viewModel.nextPage()) // At end
    }

    @Test
    fun verifyLoginDemoRolePresets() {
        val viewModel = LoginViewModel()
        viewModel.selectDemoRole(UserRole.TEACHER)
        assertEquals("teacher.sarah@oxfordacademy.edu", viewModel.uiState.value.email)
        assertEquals(UserRole.TEACHER, viewModel.uiState.value.selectedRole)

        viewModel.selectDemoRole(UserRole.STUDENT)
        assertEquals("student.amina@oxfordacademy.edu", viewModel.uiState.value.email)
        assertEquals(UserRole.STUDENT, viewModel.uiState.value.selectedRole)
    }
}
