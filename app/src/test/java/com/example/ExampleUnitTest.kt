package com.example

import androidx.compose.ui.unit.dp
import com.example.core.config.AppConfig
import com.example.core.config.EnvironmentType
import com.example.core.error.AppError
import com.example.core.navigation.AppScreen
import com.example.core.navigation.RouteGuard
import com.example.core.security.Permission
import com.example.core.security.TenantContext
import com.example.core.security.UserRole
import com.example.core.theme.EduElevation
import com.example.core.theme.EduSpacing
import com.example.core.utils.ValidationUtils
import com.example.features.auth.LoginViewModel
import com.example.features.auth.RegisterViewModel
import com.example.features.auth.domain.AuthResult
import com.example.features.designsystem.DesignSystemViewModel
import com.example.features.onboarding.OnboardingViewModel
import com.example.shared.models.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * JVM unit tests for EduManage core architecture, RBAC, Design System tokens, Auth flows, and ViewModels.
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

    @Test
    fun verifyAuthResultPolymorphism() {
        val profile = UserProfile(
            uid = "usr_test_100",
            email = "admin@oxf.edu",
            displayName = "Admin Test",
            schoolId = "sch_oxf",
            role = UserRole.SCHOOL_ADMIN
        )
        val success: AuthResult<UserProfile> = AuthResult.Success(profile)
        assertTrue(success is AuthResult.Success)
        assertEquals("usr_test_100", (success as AuthResult.Success).data.uid)

        val failure: AuthResult<UserProfile> = AuthResult.Failure(AppError.AuthenticationError("Invalid credentials"))
        assertTrue(failure is AuthResult.Failure)
        assertEquals("Invalid credentials", (failure as AuthResult.Failure).error.message)
    }

    @Test
    fun verifyForgotPasswordDialogLifecycle() {
        val viewModel = LoginViewModel()
        assertFalse(viewModel.uiState.value.showForgotPasswordDialog)

        viewModel.openForgotPasswordDialog()
        assertTrue(viewModel.uiState.value.showForgotPasswordDialog)
        assertEquals(viewModel.uiState.value.email, viewModel.uiState.value.resetEmail)

        viewModel.onResetEmailChanged("newemail@school.edu")
        assertEquals("newemail@school.edu", viewModel.uiState.value.resetEmail)

        viewModel.dismissForgotPasswordDialog()
        assertFalse(viewModel.uiState.value.showForgotPasswordDialog)
    }

    @Test
    fun verifyRegisterViewModelValidation() {
        val viewModel = RegisterViewModel()
        var successTriggered = false

        // Empty form submission should produce validation error
        viewModel.registerSchool { successTriggered = true }
        assertFalse(successTriggered)
        assertNotNull(viewModel.uiState.value.errorMessage)
        assertEquals("School Name is required.", viewModel.uiState.value.errorMessage)

        viewModel.onSchoolNameChanged("Heritage High School")
        viewModel.onSchoolCodeChanged("HHS101")
        viewModel.onAdminNameChanged("Principal Jane")
        viewModel.onEmailChanged("principal@hhs.edu")
        viewModel.onPasswordChanged("Secure@2026")

        assertEquals("Heritage High School", viewModel.uiState.value.schoolName)
        assertEquals("HHS101", viewModel.uiState.value.schoolCode)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun verifyUniqueSchoolIdGenerationFormat() {
        val repo = com.example.features.schools.data.FirebaseSchoolRepository()
        val id1 = repo.generateUniqueSchoolId("CIS101")
        val id2 = repo.generateUniqueSchoolId("CIS101")

        assertTrue(id1.startsWith("sch_cis101_"))
        assertTrue(id2.startsWith("sch_cis101_"))
        assertFalse(id1 == id2) // Guaranteed unique timestamp and random suffix
    }

    @Test
    fun verifyTenantSchoolFirestoreSerializationAndDeserialization() {
        val school = com.example.shared.models.TenantSchool(
            id = "sch_cambridge_101",
            name = "Cambridge High School",
            code = "CHS-101",
            slogan = "Inspiring Minds",
            address = "12 King's Parade",
            phone = "+44 1223 337733",
            email = "admissions@cambridgehigh.ac.uk",
            website = "https://cambridgehigh.ac.uk",
            establishedYear = 1988,
            schoolType = "Secondary High School",
            academicSession = "2026-2027",
            currency = "GBP",
            currencySymbol = "£",
            timezone = "Europe/London",
            status = com.example.shared.models.SchoolStatus.ACTIVE,
            adminUid = "usr_cambridge_admin"
        )

        val map = school.toFirestoreMap()
        assertEquals("sch_cambridge_101", map["id"])
        assertEquals("CHS-101", map["code"])
        assertEquals("GBP", map["currency"])
        assertEquals("Europe/London", map["timezone"])
        assertEquals(1988, map["establishedYear"])

        val deserialized = com.example.shared.models.TenantSchool.fromFirestoreMap(school.id, map)
        assertEquals(school.id, deserialized.id)
        assertEquals(school.name, deserialized.name)
        assertEquals(school.code, deserialized.code)
        assertEquals(school.currency, deserialized.currency)
        assertEquals(school.currencySymbol, deserialized.currencySymbol)
        assertEquals(school.establishedYear, deserialized.establishedYear)
        assertEquals(school.schoolType, deserialized.schoolType)
        assertEquals(school.adminUid, deserialized.adminUid)
    }

    @Test
    fun verifySchoolRegistrationViewModelStepProgressionAndValidation() {
        val vm = com.example.features.schools.presentation.SchoolRegistrationViewModel()
        assertEquals(0, vm.uiState.value.currentStep)

        // Attempting to advance Step 0 without school name or code should fail
        assertFalse(vm.nextStep())
        assertNotNull(vm.uiState.value.errorMessage)

        vm.onSchoolNameChanged("St. Jude's International School")
        vm.onSchoolCodeChanged("STJ101")
        vm.onEstablishedYearChanged("2010")
        assertNull(vm.uiState.value.errorMessage)

        // Advance to Step 1 (Contact)
        assertTrue(vm.nextStep())
        assertEquals(1, vm.uiState.value.currentStep)

        // Attempting Step 1 without address should fail
        assertFalse(vm.nextStep())
        assertEquals("Campus Address is required.", vm.uiState.value.errorMessage)

        vm.onAddressChanged("45 St Jude Avenue, Metro")
        vm.onPhoneChanged("+1 555 987 6543")
        vm.onEmailChanged("contact@stjudes.edu")
        assertNull(vm.uiState.value.errorMessage)

        // Advance to Step 2 (Operations & Region)
        assertTrue(vm.nextStep())
        assertEquals(2, vm.uiState.value.currentStep)

        // Step 2 has valid pre-populated defaults
        assertTrue(vm.nextStep())
        assertEquals(3, vm.uiState.value.currentStep)

        // Step 3 (Admin Info)
        assertFalse(vm.nextStep())
        assertEquals("Administrator full name is required.", vm.uiState.value.errorMessage)

        vm.onAdminNameChanged("Father Thomas")
        vm.onAdminEmailChanged("thomas@stjudes.edu")
        vm.onAdminPasswordChanged("StJudes@2026")
        assertNull(vm.uiState.value.errorMessage)

        // Test step rollback
        assertTrue(vm.previousStep())
        assertEquals(2, vm.uiState.value.currentStep)
    }
}

