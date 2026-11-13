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

    @Test
    fun verifyTenantPathProviderBuildsAll15Subcollections() {
        val schoolId = "sch_oxford_01"
        TenantContext.setTenant(schoolId, UserRole.SCHOOL_ADMIN, "usr_admin_01")

        assertEquals("schools/sch_oxford_01", com.example.core.tenant.TenantPathProvider.schoolPath(schoolId))
        assertEquals("schools/sch_oxford_01/students", com.example.core.tenant.TenantPathProvider.studentsPath(schoolId))
        assertEquals("schools/sch_oxford_01/teachers", com.example.core.tenant.TenantPathProvider.teachersPath(schoolId))
        assertEquals("schools/sch_oxford_01/parents", com.example.core.tenant.TenantPathProvider.parentsPath(schoolId))
        assertEquals("schools/sch_oxford_01/classes", com.example.core.tenant.TenantPathProvider.classesPath(schoolId))
        assertEquals("schools/sch_oxford_01/sections", com.example.core.tenant.TenantPathProvider.sectionsPath(schoolId))
        assertEquals("schools/sch_oxford_01/attendance", com.example.core.tenant.TenantPathProvider.attendancePath(schoolId))
        assertEquals("schools/sch_oxford_01/fees", com.example.core.tenant.TenantPathProvider.feesPath(schoolId))
        assertEquals("schools/sch_oxford_01/exams", com.example.core.tenant.TenantPathProvider.examsPath(schoolId))
        assertEquals("schools/sch_oxford_01/results", com.example.core.tenant.TenantPathProvider.resultsPath(schoolId))
        assertEquals("schools/sch_oxford_01/notices", com.example.core.tenant.TenantPathProvider.noticesPath(schoolId))
        assertEquals("schools/sch_oxford_01/homework", com.example.core.tenant.TenantPathProvider.homeworkPath(schoolId))
        assertEquals("schools/sch_oxford_01/library", com.example.core.tenant.TenantPathProvider.libraryPath(schoolId))
        assertEquals("schools/sch_oxford_01/transport", com.example.core.tenant.TenantPathProvider.transportPath(schoolId))
        assertEquals("schools/sch_oxford_01/events", com.example.core.tenant.TenantPathProvider.eventsPath(schoolId))
        assertEquals("schools/sch_oxford_01/documents", com.example.core.tenant.TenantPathProvider.documentsPath(schoolId))
    }

    @Test
    fun verifyTenantPathProviderRejectsBlankOrInvalidSchoolId() {
        var blankRejected = false
        try {
            com.example.core.tenant.TenantPathProvider.validateSchoolId("")
        } catch (e: AppError.SecurityViolation) {
            blankRejected = true
        }
        assertTrue(blankRejected)

        var invalidCharsRejected = false
        try {
            com.example.core.tenant.TenantPathProvider.validateSchoolId("sch/../../evil")
        } catch (e: AppError.SecurityViolation) {
            invalidCharsRejected = true
        }
        assertTrue(invalidCharsRejected)
    }

    @Test
    fun verifyTenantDataRepositoryCrossTenantFirewallEnforcement() {
        val repo = com.example.core.tenant.TenantDataRepository()
        TenantContext.setTenant("sch_oxford_01", UserRole.SCHOOL_ADMIN, "usr_admin_01")

        // Oxford data isolation check
        val oxfordStudents = repo.getPreSeededStudents("sch_oxford_01")
        assertEquals(4, oxfordStudents.size)
        assertTrue(oxfordStudents.all { it.schoolId == "sch_oxford_01" })

        // Cambridge data isolation check
        val cambridgeStudents = repo.getPreSeededStudents("sch_cambridge_101")
        assertEquals(3, cambridgeStudents.size)
        assertTrue(cambridgeStudents.all { it.schoolId == "sch_cambridge_101" })

        // Cross-Tenant Boundary Firewall check: Session is locked to Oxford, query for Cambridge should throw
        var crossTenantBlocked = false
        try {
            com.example.core.tenant.TenantPathProvider.verifyTenantBoundary("sch_cambridge_101")
        } catch (e: AppError.SecurityViolation) {
            crossTenantBlocked = true
            assertTrue(e.userFriendlyMessage.contains("Cross-School Access Blocked"))
        }
        assertTrue(crossTenantBlocked)
    }

    @Test
    fun verifyTenantArchitectureViewModelSimulationAndMetrics() {
        TenantContext.setTenant("sch_oxford_01", UserRole.SCHOOL_ADMIN, "usr_admin_01")
        val vm = com.example.features.tenant.TenantArchitectureViewModel()

        assertEquals("sch_oxford_01", vm.uiState.value.activeTenantId)
        assertEquals(15, vm.uiState.value.subcollectionsList.size)
        assertEquals("schools/sch_oxford_01/students", vm.uiState.value.currentPath)

        // Switch to Cambridge
        vm.switchActiveTenant("sch_cambridge_101")
        assertEquals("sch_cambridge_101", vm.uiState.value.activeTenantId)
        assertEquals("schools/sch_cambridge_101/students", vm.uiState.value.currentPath)

        // Run cross-tenant breach attempt simulation
        vm.simulateCrossTenantBreachAttempt()
        assertTrue(vm.uiState.value.isAttackBlocked)
        assertNotNull(vm.uiState.value.simulatedAttackMessage)
        assertTrue(vm.uiState.value.simulatedAttackMessage!!.contains("BLOCKED"))
    }

    @Test
    fun verifyAllNineRolesDefinedWithDistinctSecurityLevels() {
        val allRoles = UserRole.entries
        assertEquals(9, allRoles.size)

        val hierarchical = com.example.core.security.RbacEngine.getAllRolesHierarchical()
        assertEquals(9, hierarchical.size)
        assertEquals(UserRole.SUPER_ADMIN, hierarchical.first())
        assertEquals(100, hierarchical.first().level)
        assertEquals(UserRole.STUDENT, hierarchical.last())
        assertEquals(10, hierarchical.last().level)

        // Verify all 9 roles exist in hierarchy
        assertTrue(allRoles.contains(UserRole.SUPER_ADMIN))
        assertTrue(allRoles.contains(UserRole.SCHOOL_ADMIN))
        assertTrue(allRoles.contains(UserRole.TEACHER))
        assertTrue(allRoles.contains(UserRole.ACCOUNTANT))
        assertTrue(allRoles.contains(UserRole.LIBRARIAN))
        assertTrue(allRoles.contains(UserRole.TRANSPORT_MANAGER))
        assertTrue(allRoles.contains(UserRole.STAFF))
        assertTrue(allRoles.contains(UserRole.PARENT))
        assertTrue(allRoles.contains(UserRole.STUDENT))
    }

    @Test
    fun verifyGranularPermissionsCategoryCoverage() {
        val categories = com.example.core.security.RbacEngine.categories
        assertEquals(12, categories.size)

        val totalCategorized = categories.values.flatten().toSet()
        assertEquals(Permission.entries.size, totalCategorized.size)
    }

    @Test
    fun verifyRolePermissionsLeastPrivilegePrinciple() {
        // Super Admin has all permissions
        val superAdminPerms = com.example.core.security.RbacEngine.getPermissionsForRole(UserRole.SUPER_ADMIN)
        assertEquals(Permission.entries.size, superAdminPerms.size)

        // Accountant has finance permissions but not academic or admin ones
        val accountantPerms = com.example.core.security.RbacEngine.getPermissionsForRole(UserRole.ACCOUNTANT)
        assertTrue(accountantPerms.contains(Permission.FEES_VIEW))
        assertTrue(accountantPerms.contains(Permission.FEES_COLLECT))
        assertTrue(accountantPerms.contains(Permission.FEES_REPORTS))
        assertFalse(accountantPerms.contains(Permission.STUDENTS_DELETE))
        assertFalse(accountantPerms.contains(Permission.TEACHERS_CREATE))
        assertFalse(accountantPerms.contains(Permission.ATTENDANCE_MARK))

        // Librarian has library permissions only
        val librarianPerms = com.example.core.security.RbacEngine.getPermissionsForRole(UserRole.LIBRARIAN)
        assertTrue(librarianPerms.contains(Permission.LIBRARY_VIEW))
        assertTrue(librarianPerms.contains(Permission.LIBRARY_MANAGE))
        assertFalse(librarianPerms.contains(Permission.FEES_COLLECT))
        assertFalse(librarianPerms.contains(Permission.EXAMS_MANAGE))

        // Transport Manager has transport permissions only
        val transportPerms = com.example.core.security.RbacEngine.getPermissionsForRole(UserRole.TRANSPORT_MANAGER)
        assertTrue(transportPerms.contains(Permission.TRANSPORT_VIEW))
        assertTrue(transportPerms.contains(Permission.TRANSPORT_MANAGE))
        assertFalse(transportPerms.contains(Permission.RESULTS_ENTER))
        assertFalse(transportPerms.contains(Permission.FEES_COLLECT))

        // Parent has ward visibility but cannot collect fees or edit grades
        val parentPerms = com.example.core.security.RbacEngine.getPermissionsForRole(UserRole.PARENT)
        assertTrue(parentPerms.contains(Permission.FEES_VIEW))
        assertTrue(parentPerms.contains(Permission.RESULTS_VIEW))
        assertTrue(parentPerms.contains(Permission.ATTENDANCE_VIEW))
        assertFalse(parentPerms.contains(Permission.FEES_COLLECT))
        assertFalse(parentPerms.contains(Permission.RESULTS_ENTER))

        // Student can submit homework but cannot create it
        val studentPerms = com.example.core.security.RbacEngine.getPermissionsForRole(UserRole.STUDENT)
        assertTrue(studentPerms.contains(Permission.HOMEWORK_SUBMIT))
        assertFalse(studentPerms.contains(Permission.HOMEWORK_CREATE))
        assertFalse(studentPerms.contains(Permission.FEES_COLLECT))
    }

    @Test
    fun verifyRbacEngineActionEvaluationAuditTrail() {
        val deniedResult = com.example.core.security.RbacEngine.evaluateAction(UserRole.STUDENT, Permission.FEES_COLLECT)
        assertFalse(deniedResult.isAllowed)
        assertTrue(deniedResult.reason.contains("Access Denied"))

        val allowedResult = com.example.core.security.RbacEngine.evaluateAction(UserRole.ACCOUNTANT, Permission.FEES_COLLECT)
        assertTrue(allowedResult.isAllowed)
        assertTrue(allowedResult.reason.contains("is permitted"))
    }

    @Test
    fun verifyDynamicPermissionOverridesAndActiveSessionSwitching() {
        TenantContext.setTenant("sch_oxford_01", UserRole.STUDENT, "usr_student_01")
        assertFalse(TenantContext.hasPermission(Permission.FEES_COLLECT))
        assertTrue(TenantContext.hasPermission(Permission.HOMEWORK_SUBMIT))

        // Dynamic Grant Override
        TenantContext.grantPermission(Permission.FEES_COLLECT)
        assertTrue(TenantContext.hasPermission(Permission.FEES_COLLECT))

        // Dynamic Revoke Override
        TenantContext.revokePermission(Permission.HOMEWORK_SUBMIT)
        assertFalse(TenantContext.hasPermission(Permission.HOMEWORK_SUBMIT))

        // Reset
        TenantContext.resetOverrides()
        assertFalse(TenantContext.hasPermission(Permission.FEES_COLLECT))
        assertTrue(TenantContext.hasPermission(Permission.HOMEWORK_SUBMIT))
    }

    @Test
    fun verifyRbacMatrixViewModelInspectionAndFiltering() {
        val vm = com.example.features.rbac.RbacMatrixViewModel()
        assertEquals(UserRole.SCHOOL_ADMIN, vm.uiState.value.inspectedRole)

        vm.selectInspectedRole(UserRole.ACCOUNTANT)
        assertEquals(UserRole.ACCOUNTANT, vm.uiState.value.inspectedRole)
        assertTrue(vm.uiState.value.inspectedRolePermissions.contains(Permission.FEES_COLLECT))

        vm.setSearchQuery("fees")
        val filtered = vm.getFilteredPermissions()
        assertTrue(filtered.all { it.key.contains("fees") || it.description.contains("fee", ignoreCase = true) })

        // Simulation
        vm.setSimulationPermission(Permission.STUDENTS_DELETE)
        assertNotNull(vm.uiState.value.simulationResult)
        assertFalse(vm.uiState.value.simulationResult!!.isAllowed)

        // Session role assumption
        vm.assumeRoleForActiveSession(UserRole.TEACHER)
        assertEquals(UserRole.TEACHER, TenantContext.activeUserRole.value)
    }

    @Test
    fun verifyStudentProfileFirestoreSerializationAndDeserialization() {
        val student = com.example.features.students.domain.StudentProfile(
            id = "stu_oxf_test_01",
            schoolId = "sch_oxford_01",
            firstName = "Lucas",
            lastName = "Vance",
            fullName = "Lucas Vance",
            rollNumber = "OXF-09B-004",
            admissionNumber = "ADM-OXF-2026-1004",
            gradeLevel = "Grade 9",
            section = "Section B",
            dateOfBirth = "2011-11-03",
            gender = "Male",
            bloodGroup = "AB-",
            status = com.example.features.students.domain.StudentStatus.ACTIVE,
            guardianName = "Dr. Marcus Vance",
            guardianRelationship = "Father",
            guardianPhone = "+1 555-0104",
            guardianEmail = "vance.parent@gmail.com",
            address = "104 Harvard Square, Cambridge, MA",
            emergencyContact = "+1 555-0106",
            medicalNotes = "Wears corrective eyewear.",
            enrollmentDate = "2025-08-22"
        )

        val map = student.toFirestoreMap()
        assertEquals("stu_oxf_test_01", map["id"])
        assertEquals("sch_oxford_01", map["schoolId"])
        assertEquals("Lucas Vance", map["fullName"])
        assertEquals("OXF-09B-004", map["rollNumber"])
        assertEquals("Grade 9", map["gradeLevel"])
        assertEquals("Section B", map["section"])
        assertEquals("ACTIVE", map["status"])

        val deserialized = com.example.features.students.domain.StudentProfile.fromFirestoreMap(
            student.id,
            student.schoolId,
            map
        )
        assertEquals(student.id, deserialized.id)
        assertEquals(student.schoolId, deserialized.schoolId)
        assertEquals(student.firstName, deserialized.firstName)
        assertEquals(student.lastName, deserialized.lastName)
        assertEquals(student.fullName, deserialized.fullName)
        assertEquals(student.rollNumber, deserialized.rollNumber)
        assertEquals(student.admissionNumber, deserialized.admissionNumber)
        assertEquals(student.gradeLevel, deserialized.gradeLevel)
        assertEquals(student.section, deserialized.section)
        assertEquals(student.bloodGroup, deserialized.bloodGroup)
        assertEquals(student.status, deserialized.status)
        assertEquals(student.guardianName, deserialized.guardianName)
        assertEquals(student.medicalNotes, deserialized.medicalNotes)
    }

    @Test
    fun verifyStudentRepositoryRollNumberAndIdentifierGeneration() {
        val repo = com.example.features.students.data.StudentRepository()
        val schoolId = "sch_oxford_01"

        val roll = repo.generateUniqueRollNumber(schoolId, "Grade 10", "Section A")
        assertTrue(roll.startsWith("OXF-10A-"))

        val adm = repo.generateAdmissionNumber(schoolId)
        assertTrue(adm.startsWith("ADM-OXF-2026-"))
    }

    @Test
    fun verifyStudentListViewModelFilteringAndSorting() {
        TenantContext.setTenant("sch_oxford_01", UserRole.SCHOOL_ADMIN, "usr_admin_01")
        val vm = com.example.features.students.presentation.StudentListViewModel()

        assertEquals(5, vm.uiState.value.totalStudentsCount)
        assertEquals(5, vm.uiState.value.filteredStudents.size)

        // Filter by Grade
        vm.onGradeFilterChange("Grade 10")
        assertEquals(2, vm.uiState.value.filteredStudents.size)
        assertTrue(vm.uiState.value.filteredStudents.all { it.gradeLevel == "Grade 10" })

        // Filter by Section
        vm.onSectionFilterChange("Section A")
        assertEquals(2, vm.uiState.value.filteredStudents.size)

        // Search query
        vm.onSearchQueryChange("Amina")
        assertEquals(1, vm.uiState.value.filteredStudents.size)
        assertEquals("Amina Rahman", vm.uiState.value.filteredStudents.first().fullName)

        // Clear filters
        vm.clearFilters()
        assertEquals(5, vm.uiState.value.filteredStudents.size)

        // Sort test
        vm.onSortChange(com.example.features.students.presentation.StudentSortOption.NAME_DESC)
        assertEquals("Sophia Chen", vm.uiState.value.filteredStudents.first().fullName)
    }

    @Test
    fun verifyStudentEnrollmentWizardStepValidationAndProgression() {
        TenantContext.setTenant("sch_oxford_01", UserRole.SCHOOL_ADMIN, "usr_admin_01")
        val vm = com.example.features.students.presentation.StudentEnrollmentViewModel()

        assertEquals(0, vm.uiState.value.currentStep)

        // Empty first name should fail validation
        assertFalse(vm.nextStep())
        assertEquals("Student first name is required.", vm.uiState.value.errorMessage)

        vm.onFirstNameChange("Daniel")
        vm.onLastNameChange("Craig")
        assertNull(vm.uiState.value.errorMessage)

        // Advance to Step 1 (Guardian)
        assertTrue(vm.nextStep())
        assertEquals(1, vm.uiState.value.currentStep)

        // Empty guardian name should fail validation
        assertFalse(vm.nextStep())
        assertEquals("Guardian full name is required.", vm.uiState.value.errorMessage)

        vm.onGuardianNameChange("Timothy Craig")
        vm.onGuardianPhoneChange("+1 555-7788")
        vm.onAddressChange("12 West End Ave")
        assertNull(vm.uiState.value.errorMessage)

        // Advance to Step 2 (Health & Medical)
        assertTrue(vm.nextStep())
        assertEquals(2, vm.uiState.value.currentStep)

        // Advance to Step 3 (Review & Confirmation)
        assertTrue(vm.nextStep())
        assertEquals(3, vm.uiState.value.currentStep)

        // Test step rollback
        assertTrue(vm.previousStep())
        assertEquals(2, vm.uiState.value.currentStep)
    }

    @Test
    fun verifyTeacherProfileFirestoreSerializationAndDeserialization() {
        val teacher = com.example.features.teachers.domain.TeacherProfile(
            id = "tea_test_99",
            schoolId = "sch_oxford_01",
            employeeId = "OXF-SCI-101",
            firstName = "Sarah",
            lastName = "Jenkins",
            fullName = "Dr. Sarah Jenkins",
            designation = "Head of Department",
            department = "Science & Mathematics",
            primarySubject = "Advanced Physics",
            additionalSubjects = listOf("AP Physics", "Astronomy"),
            qualification = "Ph.D. Astrophysics, M.Sc. Physics",
            joiningDate = "2019-08-15",
            experienceYears = 12,
            email = "teacher.sarah@oxfordacademy.edu",
            phone = "+1 555-0201",
            emergencyContact = "+1 555-0209",
            address = "77 Commonwealth Ave, Boston, MA",
            assignedClassId = "Grade 10",
            assignedSectionId = "Section A",
            isClassTeacher = true,
            bloodGroup = "O+",
            salaryTier = "Tier 1 - Executive Faculty",
            status = com.example.features.teachers.domain.TeacherStatus.ACTIVE
        )

        val map = teacher.toFirestoreMap()
        assertEquals("tea_test_99", map["id"])
        assertEquals("sch_oxford_01", map["schoolId"])
        assertEquals("OXF-SCI-101", map["employeeId"])
        assertEquals("Dr. Sarah Jenkins", map["fullName"])
        assertEquals("Advanced Physics", map["primarySubject"])
        assertEquals("ACTIVE", map["status"])
        assertEquals(12, map["experienceYears"])
        assertEquals(true, map["isClassTeacher"])
        assertEquals("Grade 10", map["assignedClassId"])

        val deserialized = com.example.features.teachers.domain.TeacherProfile.fromFirestoreMap(
            teacher.id,
            teacher.schoolId,
            map
        )
        assertEquals(teacher.id, deserialized.id)
        assertEquals(teacher.schoolId, deserialized.schoolId)
        assertEquals(teacher.employeeId, deserialized.employeeId)
        assertEquals(teacher.firstName, deserialized.firstName)
        assertEquals(teacher.lastName, deserialized.lastName)
        assertEquals(teacher.fullName, deserialized.fullName)
        assertEquals(teacher.designation, deserialized.designation)
        assertEquals(teacher.department, deserialized.department)
        assertEquals(teacher.primarySubject, deserialized.primarySubject)
        assertEquals(2, deserialized.additionalSubjects.size)
        assertTrue(deserialized.additionalSubjects.contains("AP Physics"))
        assertEquals(teacher.qualification, deserialized.qualification)
        assertEquals(teacher.experienceYears, deserialized.experienceYears)
        assertEquals(teacher.email, deserialized.email)
        assertEquals(teacher.phone, deserialized.phone)
        assertEquals(teacher.assignedClassId, deserialized.assignedClassId)
        assertEquals(teacher.assignedSectionId, deserialized.assignedSectionId)
        assertTrue(deserialized.isClassTeacher)
        assertEquals(teacher.status, deserialized.status)
    }

    @Test
    fun verifyTeacherRepositoryEmployeeIdGenerationAndDepartmentFiltering() {
        val repo = com.example.features.teachers.data.TeacherRepository()
        val schoolId = "sch_oxford_01"

        val sciId = repo.generateEmployeeId(schoolId, "Science & Mathematics")
        assertTrue(sciId.startsWith("OXF-SCI-"))

        val lanId = repo.generateEmployeeId(schoolId, "Languages & Literature")
        assertTrue(lanId.startsWith("OXF-LAN-"))

        val humId = repo.generateEmployeeId(schoolId, "Social Sciences & Humanities")
        assertTrue(humId.startsWith("OXF-HUM-"))

        val oxfordTeachers = repo.getPreSeededTeachers("sch_oxford_01")
        assertEquals(5, oxfordTeachers.size)
        assertTrue(oxfordTeachers.all { it.schoolId == "sch_oxford_01" })

        val cambridgeTeachers = repo.getPreSeededTeachers("sch_cambridge_101")
        assertEquals(3, cambridgeTeachers.size)
        assertTrue(cambridgeTeachers.all { it.schoolId == "sch_cambridge_101" })
    }

    @Test
    fun verifyTeacherListViewModelFilteringAndSorting() {
        TenantContext.setTenant("sch_oxford_01", UserRole.SCHOOL_ADMIN, "usr_admin_01")
        val vm = com.example.features.teachers.presentation.TeacherListViewModel()

        assertEquals(5, vm.uiState.value.totalFacultyCount)
        assertEquals(5, vm.uiState.value.filteredTeachers.size)

        // Department filter
        vm.onDepartmentFilterChange("Science & Mathematics")
        assertEquals(3, vm.uiState.value.filteredTeachers.size)
        assertTrue(vm.uiState.value.filteredTeachers.all { it.department == "Science & Mathematics" })

        // Status filter
        vm.onStatusFilterChange(com.example.features.teachers.domain.TeacherStatus.ON_LEAVE)
        assertEquals(1, vm.uiState.value.filteredTeachers.size)
        assertEquals("Elena Rostova", vm.uiState.value.filteredTeachers.first().fullName)

        // Clear filters
        vm.clearFilters()
        assertEquals(5, vm.uiState.value.filteredTeachers.size)

        // Search query
        vm.onSearchQueryChange("Physics")
        assertEquals(1, vm.uiState.value.filteredTeachers.size)
        assertEquals("Dr. Sarah Jenkins", vm.uiState.value.filteredTeachers.first().fullName)

        vm.clearFilters()

        // Sort by experience high-to-low
        vm.onSortChange(com.example.features.teachers.presentation.TeacherSortOption.EXPERIENCE_DESC)
        assertEquals("Dr. Sarah Jenkins", vm.uiState.value.filteredTeachers.first().fullName)
        assertEquals(12, vm.uiState.value.filteredTeachers.first().experienceYears)

        // Detail dialog selection
        val teacher = vm.uiState.value.filteredTeachers.first()
        vm.selectTeacherForDetail(teacher)
        assertEquals(teacher.id, vm.uiState.value.selectedTeacherForDetail?.id)
        vm.clearSelectedTeacher()
        assertNull(vm.uiState.value.selectedTeacherForDetail)
    }

    @Test
    fun verifyTeacherOnboardingWizardStepValidationAndProgression() {
        TenantContext.setTenant("sch_oxford_01", UserRole.SCHOOL_ADMIN, "usr_admin_01")
        val vm = com.example.features.teachers.presentation.TeacherOnboardingViewModel()

        assertEquals(0, vm.uiState.value.currentStep)

        // Step 0: Blank first name should fail
        assertFalse(vm.nextStep())
        assertEquals("Teacher first name is required.", vm.uiState.value.errorMessage)

        vm.onFirstNameChange("Katherine")
        assertFalse(vm.nextStep())
        assertEquals("Teacher last name is required.", vm.uiState.value.errorMessage)

        vm.onLastNameChange("Johnson")
        assertFalse(vm.nextStep())
        assertEquals("Faculty email is required.", vm.uiState.value.errorMessage)

        vm.onEmailChange("katherine.j@oxfordacademy.edu")
        assertFalse(vm.nextStep())
        assertEquals("Contact phone number is required.", vm.uiState.value.errorMessage)

        vm.onPhoneChange("+1 555-0301")
        assertNull(vm.uiState.value.errorMessage)

        // Advance to Step 1 (Academic & Employment)
        assertTrue(vm.nextStep())
        assertEquals(1, vm.uiState.value.currentStep)

        // Step 1: Blank qualification should fail
        vm.onQualificationChange("")
        assertFalse(vm.nextStep())
        assertEquals("Academic qualification is required.", vm.uiState.value.errorMessage)

        vm.onQualificationChange("Ph.D. Applied Mathematics")
        assertNull(vm.uiState.value.errorMessage)

        // Advance to Step 2 (Subject & Class Allocation)
        assertTrue(vm.nextStep())
        assertEquals(2, vm.uiState.value.currentStep)

        // Step 2: Blank primary subject should fail
        vm.onPrimarySubjectChange("")
        assertFalse(vm.nextStep())
        assertEquals("Primary subject is required.", vm.uiState.value.errorMessage)

        vm.onPrimarySubjectChange("Orbital Mechanics & Calculus")
        assertNull(vm.uiState.value.errorMessage)

        // Advance to Step 3 (Review & Confirmation)
        assertTrue(vm.nextStep())
        assertEquals(3, vm.uiState.value.currentStep)

        // Rollback test
        assertTrue(vm.previousStep())
        assertEquals(2, vm.uiState.value.currentStep)
    }

    @Test
    fun verifyAcademicClassAndSectionFirestoreSerializationAndDeserialization() {
        val academicClass = com.example.features.classes.domain.AcademicClass(
            id = "cls_test_10",
            schoolId = "sch_oxford_01",
            name = "Grade 10",
            code = "G10",
            numericGrade = 10,
            stream = "Science & Tech",
            sectionsCount = 2,
            totalStudents = 62,
            academicYear = "2026-2027"
        )

        val classMap = academicClass.toFirestoreMap()
        assertEquals("cls_test_10", classMap["id"])
        assertEquals("sch_oxford_01", classMap["schoolId"])
        assertEquals("Grade 10", classMap["name"])
        assertEquals("G10", classMap["code"])
        assertEquals(10, classMap["numericGrade"])
        assertEquals("Science & Tech", classMap["stream"])
        assertEquals(2, classMap["sectionsCount"])
        assertEquals(62, classMap["totalStudents"])

        val deserializedClass = com.example.features.classes.domain.AcademicClass.fromFirestoreMap(
            academicClass.id,
            academicClass.schoolId,
            classMap
        )
        assertEquals(academicClass.id, deserializedClass.id)
        assertEquals(academicClass.name, deserializedClass.name)
        assertEquals(academicClass.code, deserializedClass.code)
        assertEquals(academicClass.numericGrade, deserializedClass.numericGrade)
        assertEquals(academicClass.stream, deserializedClass.stream)

        val section = com.example.features.classes.domain.ClassSection(
            id = "sec_test_10a",
            schoolId = "sch_oxford_01",
            classId = "cls_test_10",
            className = "Grade 10",
            name = "Section A",
            roomNumber = "Room 301",
            capacity = 35,
            currentEnrollment = 31,
            classTeacherId = "tea_oxf_01",
            classTeacherName = "Dr. Sarah Jenkins"
        )

        val sectionMap = section.toFirestoreMap()
        assertEquals("sec_test_10a", sectionMap["id"])
        assertEquals("Grade 10", sectionMap["className"])
        assertEquals("Section A", sectionMap["name"])
        assertEquals(35, sectionMap["capacity"])
        assertEquals(31, sectionMap["currentEnrollment"])
        assertEquals("tea_oxf_01", sectionMap["classTeacherId"])

        val deserializedSection = com.example.features.classes.domain.ClassSection.fromFirestoreMap(
            section.id,
            section.schoolId,
            sectionMap
        )
        assertEquals(section.id, deserializedSection.id)
        assertEquals(section.displayName, deserializedSection.displayName)
        assertEquals(section.roomNumber, deserializedSection.roomNumber)
        assertEquals(section.classTeacherName, deserializedSection.classTeacherName)
        assertEquals(section.capacity, deserializedSection.capacity)
        assertEquals(section.currentEnrollment, deserializedSection.currentEnrollment)
    }

    @Test
    fun verifySubjectAllocationFirestoreSerializationAndDeserialization() {
        val allocation = com.example.features.classes.domain.SubjectAllocation(
            id = "sub_test_01",
            schoolId = "sch_oxford_01",
            classId = "cls_oxf_g10",
            sectionId = "sec_oxf_10a",
            sectionName = "Grade 10 - Section A",
            subjectName = "Advanced Physics",
            subjectCode = "PHY-101",
            teacherId = "tea_oxf_01",
            teacherName = "Dr. Sarah Jenkins",
            periodsPerWeek = 5,
            isElective = false,
            roomNumber = "Physics Lab 1"
        )

        val map = allocation.toFirestoreMap()
        assertEquals("sub_test_01", map["id"])
        assertEquals("Advanced Physics", map["subjectName"])
        assertEquals("PHY-101", map["subjectCode"])
        assertEquals(5, map["periodsPerWeek"])
        assertEquals(false, map["isElective"])

        val deserialized = com.example.features.classes.domain.SubjectAllocation.fromFirestoreMap(
            allocation.id,
            allocation.schoolId,
            map
        )
        assertEquals(allocation.id, deserialized.id)
        assertEquals(allocation.subjectName, deserialized.subjectName)
        assertEquals(allocation.subjectCode, deserialized.subjectCode)
        assertEquals(allocation.teacherName, deserialized.teacherName)
        assertEquals(allocation.periodsPerWeek, deserialized.periodsPerWeek)
        assertEquals(allocation.isElective, deserialized.isElective)
    }

    @Test
    fun verifyClassRepositoryPreSeededClassesSectionsAndAllocations() {
        val repo = com.example.features.classes.data.ClassRepository()
        val schoolId = "sch_oxford_01"

        val classes = repo.getPreSeededClasses(schoolId)
        assertEquals(4, classes.size)
        assertTrue(classes.all { it.schoolId == schoolId })

        val sections = repo.getPreSeededSections(schoolId)
        assertEquals(4, sections.size)
        assertTrue(sections.all { it.schoolId == schoolId })

        val allocations = repo.getPreSeededAllocations(schoolId)
        assertEquals(6, allocations.size)
        assertTrue(allocations.all { it.schoolId == schoolId })

        val cambridgeClasses = repo.getPreSeededClasses("sch_cambridge_101")
        assertEquals(3, cambridgeClasses.size)
        assertTrue(cambridgeClasses.all { it.schoolId == "sch_cambridge_101" })
    }

    @Test
    fun verifyClassManagementViewModelTabSwitchingFilteringAndMetrics() {
        TenantContext.setTenant("sch_oxford_01", UserRole.SCHOOL_ADMIN, "usr_admin_01")
        val vm = com.example.features.classes.presentation.ClassManagementViewModel()

        assertEquals(4, vm.uiState.value.totalClassesCount)
        assertEquals(4, vm.uiState.value.totalSectionsCount)
        assertEquals(6, vm.uiState.value.totalAllocationsCount)
        assertEquals(116, vm.uiState.value.totalEnrolledStudents)
        assertEquals(4, vm.uiState.value.classTeachersAssignedCount)

        // Tab selection
        vm.selectTab(com.example.features.classes.presentation.ClassManagementTab.SECTIONS)
        assertEquals(com.example.features.classes.presentation.ClassManagementTab.SECTIONS, vm.uiState.value.selectedTab)

        vm.selectTab(com.example.features.classes.presentation.ClassManagementTab.SUBJECTS)
        assertEquals(com.example.features.classes.presentation.ClassManagementTab.SUBJECTS, vm.uiState.value.selectedTab)

        // Class filtering
        vm.onClassFilterChange("cls_oxf_g10")
        assertEquals("cls_oxf_g10", vm.uiState.value.selectedClassFilter)
        assertEquals(1, vm.uiState.value.filteredClasses.size)
        assertEquals(2, vm.uiState.value.filteredSections.size)
        assertEquals(4, vm.uiState.value.filteredAllocations.size)

        // Clear filter
        vm.clearFilters()
        assertNull(vm.uiState.value.selectedClassFilter)
        assertEquals(4, vm.uiState.value.filteredClasses.size)

        // Search query
        vm.onSearchQueryChange("Physics")
        assertEquals(1, vm.uiState.value.filteredAllocations.size)
        assertEquals("Advanced Physics", vm.uiState.value.filteredAllocations.first().subjectName)

        // Section inspection
        val sectionToInspect = vm.uiState.value.sections.first()
        vm.inspectSection(sectionToInspect)
        assertEquals(sectionToInspect.id, vm.uiState.value.selectedSectionForInspection?.id)
        vm.closeSectionInspection()
        assertNull(vm.uiState.value.selectedSectionForInspection)
    }

    @Test
    fun verifyClassManagementViewModelValidationForClassAndSectionCreation() {
        TenantContext.setTenant("sch_oxford_01", UserRole.SCHOOL_ADMIN, "usr_admin_01")
        val vm = com.example.features.classes.presentation.ClassManagementViewModel()

        // Blank class name should fail
        vm.createClass("", "G08", 8, "General")
        assertEquals("Class Name and Code are required.", vm.uiState.value.errorMessage)

        // Blank section name should fail
        vm.createSection("cls_oxf_g10", "", "Room 303", 35, null, null)
        assertEquals("Class, Section Name, and Room Number are required.", vm.uiState.value.errorMessage)

        // Blank subject allocation should fail
        vm.createSubjectAllocation("sec_oxf_10a", "", "PHY-101", "tea_01", "Dr. Jenkins", 5, false, "Room 301")
        assertEquals("Section, Subject Name, and Subject Code are required.", vm.uiState.value.errorMessage)
    }

    @Test
    fun verifyAttendanceModelsFirestoreSerializationAndDeserialization() {
        val entry = com.example.features.attendance.domain.StudentAttendanceEntry(
            studentId = "stu_oxf_01",
            studentName = "Amina Rahman",
            rollNumber = "OXF-10A-001",
            status = com.example.features.attendance.domain.AttendanceStatus.PRESENT,
            remarks = "Prompt on time"
        )
        val entryMap = entry.toFirestoreMap()
        assertEquals("stu_oxf_01", entryMap["studentId"])
        assertEquals("Amina Rahman", entryMap["studentName"])
        assertEquals("PRESENT", entryMap["status"])
        assertEquals("Prompt on time", entryMap["remarks"])

        val deserializedEntry = com.example.features.attendance.domain.StudentAttendanceEntry.fromFirestoreMap(entryMap)
        assertEquals(entry.studentId, deserializedEntry.studentId)
        assertEquals(entry.studentName, deserializedEntry.studentName)
        assertEquals(entry.status, deserializedEntry.status)
        assertEquals(entry.remarks, deserializedEntry.remarks)

        val auditLog = com.example.features.attendance.domain.AttendanceAuditLog(
            timestamp = 1711270000000L,
            actorId = "tea_oxf_01",
            actorName = "Dr. Sarah Jenkins",
            action = "FINALIZED",
            notes = "Morning roll call verified"
        )
        val auditMap = auditLog.toFirestoreMap()
        assertEquals("FINALIZED", auditMap["action"])
        assertEquals("Dr. Sarah Jenkins", auditMap["actorName"])

        val deserializedAudit = com.example.features.attendance.domain.AttendanceAuditLog.fromFirestoreMap(auditMap)
        assertEquals(auditLog.action, deserializedAudit.action)
        assertEquals(auditLog.notes, deserializedAudit.notes)

        val record = com.example.features.attendance.domain.AttendanceRecord.createWithMetrics(
            id = "att_test_10a",
            schoolId = "sch_oxford_01",
            date = "2026-03-24",
            classId = "cls_oxf_g10",
            className = "Grade 10",
            sectionId = "sec_oxf_10a",
            sectionName = "Section A",
            markedById = "tea_oxf_01",
            markedByName = "Dr. Sarah Jenkins",
            entries = listOf(
                entry,
                com.example.features.attendance.domain.StudentAttendanceEntry(
                    studentId = "stu_oxf_02",
                    studentName = "Oliver Smith",
                    rollNumber = "OXF-10A-002",
                    status = com.example.features.attendance.domain.AttendanceStatus.ABSENT
                )
            ),
            auditTrail = listOf(auditLog),
            isFinalized = true
        )

        assertEquals(2, record.totalStudents)
        assertEquals(1, record.presentCount)
        assertEquals(1, record.absentCount)
        assertEquals(50.0f, record.attendancePercentage)

        val recordMap = record.toFirestoreMap()
        val deserializedRecord = com.example.features.attendance.domain.AttendanceRecord.fromFirestoreMap(
            record.id,
            record.schoolId,
            recordMap
        )
        assertEquals(record.id, deserializedRecord.id)
        assertEquals(record.date, deserializedRecord.date)
        assertEquals(record.className, deserializedRecord.className)
        assertEquals(record.isFinalized, deserializedRecord.isFinalized)
        assertEquals(2, deserializedRecord.entries.size)
        assertEquals(1, deserializedRecord.auditTrail.size)
        assertEquals(50.0f, deserializedRecord.attendancePercentage)
    }

    @Test
    fun verifyAttendanceRepositoryPreSeededRecordsAndTenantFirewall() {
        val repo = com.example.features.attendance.data.AttendanceRepository()
        val oxfordRecords = repo.getPreSeededRecords("sch_oxford_01")
        assertEquals(3, oxfordRecords.size)
        assertTrue(oxfordRecords.all { it.schoolId == "sch_oxford_01" })

        val cambridgeRecords = repo.getPreSeededRecords("sch_cambridge_101")
        assertEquals(1, cambridgeRecords.size)
        assertTrue(cambridgeRecords.all { it.schoolId == "sch_cambridge_101" })

        TenantContext.setTenant("sch_oxford_01", UserRole.SCHOOL_ADMIN, "usr_admin_01")
        var firewallBlocked = false
        try {
            com.example.core.tenant.TenantPathProvider.verifyTenantBoundary("sch_cambridge_101")
        } catch (e: AppError.SecurityViolation) {
            firewallBlocked = true
        }
        assertTrue(firewallBlocked)
    }

    @Test
    fun verifyAttendanceViewModelRollCallStatusTogglingAndMetrics() {
        TenantContext.setTenant("sch_oxford_01", UserRole.SCHOOL_ADMIN, "usr_admin_01")
        val vm = com.example.features.attendance.presentation.AttendanceViewModel()

        assertEquals("2026-03-24", vm.uiState.value.selectedDate)
        assertEquals(5, vm.uiState.value.editableEntries.size)
        assertEquals(5, vm.uiState.value.totalEnrolled)

        // Today record is finalized initially: test unlocking first
        vm.openReopenDialog()
        assertTrue(vm.uiState.value.showReopenDialog)
        vm.reopenRegister("Correction for test")
        assertFalse(vm.uiState.value.isFinalized)

        // Now test toggling student status
        val firstStudentId = vm.uiState.value.editableEntries.first().studentId
        vm.updateStudentStatus(firstStudentId, com.example.features.attendance.domain.AttendanceStatus.ABSENT)
        val updatedFirst = vm.uiState.value.editableEntries.first { it.studentId == firstStudentId }
        assertEquals(com.example.features.attendance.domain.AttendanceStatus.ABSENT, updatedFirst.status)
        assertEquals(1, vm.uiState.value.absentCount)

        // Bulk Mark All Present
        vm.markAllPresent()
        assertTrue(vm.uiState.value.editableEntries.all { it.status == com.example.features.attendance.domain.AttendanceStatus.PRESENT })
        assertEquals(5, vm.uiState.value.presentCount)
        assertEquals(0, vm.uiState.value.absentCount)
        assertEquals(100.0f, vm.uiState.value.presentPercentage)

        // Bulk Mark All Absent
        vm.markAllAbsent()
        assertTrue(vm.uiState.value.editableEntries.all { it.status == com.example.features.attendance.domain.AttendanceStatus.ABSENT })
        assertEquals(0, vm.uiState.value.presentCount)
        assertEquals(5, vm.uiState.value.absentCount)
        assertEquals(0.0f, vm.uiState.value.presentPercentage)
    }

    @Test
    fun verifyAttendanceViewModelFinalizationAndAuditLock() {
        TenantContext.setTenant("sch_oxford_01", UserRole.SCHOOL_ADMIN, "usr_admin_01")
        val vm = com.example.features.attendance.presentation.AttendanceViewModel()

        // Remarks updating
        val student = vm.uiState.value.editableEntries.first()
        vm.openRemarksDialog(student)
        assertTrue(vm.uiState.value.showRemarksDialog)
        assertEquals(student.studentId, vm.uiState.value.selectedEntryForRemarks?.studentId)

        vm.onRemarksInputChanged("Medical excusal certificate verified")
        vm.saveRemarks()
        assertFalse(vm.uiState.value.showRemarksDialog)
        val updatedStudent = vm.uiState.value.editableEntries.first { it.studentId == student.studentId }
        assertEquals("Medical excusal certificate verified", updatedStudent.remarks)

        // Finalize register
        vm.finalizeRegister()
        assertTrue(vm.uiState.value.isFinalized)

        // Attempting to change status when finalized should be blocked
        vm.updateStudentStatus(student.studentId, com.example.features.attendance.domain.AttendanceStatus.ABSENT)
        assertNotNull(vm.uiState.value.errorMessage)
        assertTrue(vm.uiState.value.errorMessage!!.contains("finalized and locked"))
    }

    @Test
    fun verifyFeeInvoiceModelGrossNetCalculationsAndSerialization() {
        val items = listOf(
            com.example.features.fees.domain.FeeItem("fi_01", com.example.features.fees.domain.FeeCategory.TUITION, "Tuition", 1000.0),
            com.example.features.fees.domain.FeeItem("fi_02", com.example.features.fees.domain.FeeCategory.LABORATORY, "Lab", 200.0)
        )
        val invoice = com.example.features.fees.domain.FeeInvoice.createInvoice(
            id = "inv_test_01",
            schoolId = "sch_oxford_01",
            invoiceNumber = "INV-OXF-2026-9999",
            studentId = "stu_01",
            studentName = "Amina Rahman",
            rollNumber = "OXF-10A-001",
            gradeLevel = "Grade 10",
            section = "Section A",
            academicTerm = "Term 1",
            dueDate = "2026-04-15",
            items = items,
            discountAmount = 100.0,
            fineAmount = 50.0,
            paidAmount = 300.0
        )

        assertEquals(1150.0, invoice.totalAmount, 0.001)
        assertEquals(300.0, invoice.paidAmount, 0.001)
        assertEquals(850.0, invoice.dueAmount, 0.001)
        assertEquals(com.example.features.fees.domain.InvoiceStatus.PARTIAL, invoice.status)

        val map = invoice.toFirestoreMap()
        assertEquals("inv_test_01", map["id"])
        assertEquals("INV-OXF-2026-9999", map["invoiceNumber"])
        assertEquals(1150.0, map["totalAmount"])
        assertEquals(850.0, map["dueAmount"])
        assertEquals("PARTIAL", map["status"])

        val deserialized = com.example.features.fees.domain.FeeInvoice.fromFirestoreMap(invoice.id, invoice.schoolId, map)
        assertEquals(invoice.id, deserialized.id)
        assertEquals(invoice.invoiceNumber, deserialized.invoiceNumber)
        assertEquals(invoice.totalAmount, deserialized.totalAmount, 0.001)
        assertEquals(invoice.dueAmount, deserialized.dueAmount, 0.001)
        assertEquals(invoice.status, deserialized.status)
        assertEquals(2, deserialized.items.size)
    }

    @Test
    fun verifyFeeRepositoryPreSeededInvoicesAndTenantFirewall() {
        val repo = com.example.features.fees.data.FeeRepository()
        val oxfordInvoices = repo.getPreSeededInvoices("sch_oxford_01")
        assertEquals(5, oxfordInvoices.size)
        assertTrue(oxfordInvoices.all { it.schoolId == "sch_oxford_01" })

        val cambridgeInvoices = repo.getPreSeededInvoices("sch_cambridge_101")
        assertEquals(3, cambridgeInvoices.size)
        assertTrue(cambridgeInvoices.all { it.schoolId == "sch_cambridge_101" })

        TenantContext.setTenant("sch_oxford_01", UserRole.SCHOOL_ADMIN, "usr_admin_01")
        var firewallBlocked = false
        try {
            com.example.core.tenant.TenantPathProvider.verifyTenantBoundary("sch_cambridge_101")
        } catch (e: AppError.SecurityViolation) {
            firewallBlocked = true
        }
        assertTrue(firewallBlocked)
    }

    @Test
    fun verifyFeeViewModelFilteringMetricsAndPaymentCollection() {
        TenantContext.setTenant("sch_oxford_01", UserRole.SCHOOL_ADMIN, "usr_admin_01")
        val vm = com.example.features.fees.presentation.FeeViewModel()

        assertEquals(5, vm.uiState.value.invoices.size)
        assertEquals(5, vm.uiState.value.filteredInvoices.size)
        assertTrue(vm.uiState.value.totalBilled > 0.0)
        assertTrue(vm.uiState.value.totalCollected > 0.0)
        assertTrue(vm.uiState.value.totalOutstanding > 0.0)
        assertEquals(1, vm.uiState.value.overdueCount)

        // Status Filter
        vm.onStatusFilterChange(com.example.features.fees.domain.InvoiceStatus.OVERDUE)
        assertEquals(1, vm.uiState.value.filteredInvoices.size)
        assertEquals("inv_oxf_03", vm.uiState.value.filteredInvoices.first().id)

        vm.onStatusFilterChange(com.example.features.fees.domain.InvoiceStatus.PAID)
        assertEquals(2, vm.uiState.value.filteredInvoices.size)

        // Clear filter
        vm.clearFilters()
        assertEquals(5, vm.uiState.value.filteredInvoices.size)

        // Search Filter
        vm.onSearchQueryChange("Amina")
        assertEquals(1, vm.uiState.value.filteredInvoices.size)
        assertEquals("Amina Rahman", vm.uiState.value.filteredInvoices.first().studentName)

        vm.clearFilters()

        // Test Payment Collection on partial invoice
        val partialInvoice = vm.uiState.value.invoices.first { it.status == com.example.features.fees.domain.InvoiceStatus.PARTIAL }
        val remainingDue = partialInvoice.dueAmount
        val initialPaid = partialInvoice.paidAmount

        vm.openPaymentDialog(partialInvoice)
        assertTrue(vm.uiState.value.showPaymentDialog)
        assertEquals(partialInvoice.id, vm.uiState.value.selectedInvoiceForPayment?.id)

        // Settle remaining balance
        vm.recordPayment(partialInvoice.id, remainingDue, com.example.features.fees.domain.PaymentMethod.CASH, "Final settlement")
        assertFalse(vm.uiState.value.showPaymentDialog)
        assertTrue(vm.uiState.value.showReceiptDialog)
        assertNotNull(vm.uiState.value.selectedTransactionForReceipt)

        val updatedInvoice = vm.uiState.value.invoices.first { it.id == partialInvoice.id }
        assertEquals(initialPaid + remainingDue, updatedInvoice.paidAmount, 0.001)
        assertEquals(0.0, updatedInvoice.dueAmount, 0.001)
        assertEquals(com.example.features.fees.domain.InvoiceStatus.PAID, updatedInvoice.status)
        assertEquals(2, updatedInvoice.transactions.size)
    }

    @Test
    fun verifyFeeViewModelInvoiceCreationValidation() {
        TenantContext.setTenant("sch_oxford_01", UserRole.SCHOOL_ADMIN, "usr_admin_01")
        val vm = com.example.features.fees.presentation.FeeViewModel()

        val initialCount = vm.uiState.value.invoices.size

        // Blank name should fail
        vm.createInvoice(studentId = "", studentName = "", rollNumber = "", gradeLevel = "Grade 10", section = "Section A", academicTerm = "Term 1", dueDate = "2026-05-01", tuitionFee = 1000.0)
        assertEquals("Student Name, Due Date, and Tuition amount are required.", vm.uiState.value.errorMessage)
        assertEquals(initialCount, vm.uiState.value.invoices.size)

        // Zero tuition should fail
        vm.createInvoice(studentId = "", studentName = "Valid Name", rollNumber = "R-10", gradeLevel = "Grade 10", section = "Section A", academicTerm = "Term 1", dueDate = "2026-05-01", tuitionFee = 0.0)
        assertEquals("Student Name, Due Date, and Tuition amount are required.", vm.uiState.value.errorMessage)

        // Valid creation
        vm.createInvoice(
            studentId = "stu_dan_01",
            studentName = "Daniel Craig",
            rollNumber = "OXF-10A-009",
            gradeLevel = "Grade 10",
            section = "Section A",
            academicTerm = "Term 1 (2026)",
            dueDate = "2026-05-15",
            tuitionFee = 1300.0,
            labFee = 200.0,
            libraryFee = 50.0,
            transportFee = 0.0,
            sportsFee = 0.0,
            discountAmount = 50.0
        )
        assertEquals(initialCount + 1, vm.uiState.value.invoices.size)
        val created = vm.uiState.value.invoices.last()
        assertEquals("Daniel Craig", created.studentName)
        assertEquals(1500.0, created.totalAmount, 0.001)
        assertEquals(1500.0, created.dueAmount, 0.001)
        assertEquals(com.example.features.fees.domain.InvoiceStatus.PENDING, created.status)
    }

    @Test
    fun verifyExamScheduleAndPaperFirestoreSerializationAndDeserialization() {
        val paper = com.example.features.exams.domain.ExamPaper(
            id = "p_test_phy",
            subjectName = "Advanced Physics",
            subjectCode = "PHY-101",
            examDate = "2026-05-10",
            startTime = "09:00 AM",
            endTime = "12:00 PM",
            durationMinutes = 180,
            totalMarks = 100,
            passingMarks = 40,
            roomNumber = "West Wing 301",
            invigilatorId = "tea_oxf_01",
            invigilatorName = "Dr. Sarah Jenkins",
            instructions = "Non-programmable calculators approved."
        )

        val paperMap = paper.toFirestoreMap()
        assertEquals("p_test_phy", paperMap["id"])
        assertEquals("Advanced Physics", paperMap["subjectName"])
        assertEquals("PHY-101", paperMap["subjectCode"])
        assertEquals(180, paperMap["durationMinutes"])
        assertEquals("West Wing 301", paperMap["roomNumber"])

        val deserializedPaper = com.example.features.exams.domain.ExamPaper.fromFirestoreMap(paperMap)
        assertEquals(paper.id, deserializedPaper.id)
        assertEquals(paper.subjectName, deserializedPaper.subjectName)
        assertEquals(paper.subjectCode, deserializedPaper.subjectCode)
        assertEquals(paper.startTime, deserializedPaper.startTime)
        assertEquals(paper.endTime, deserializedPaper.endTime)
        assertEquals(paper.roomNumber, deserializedPaper.roomNumber)
        assertEquals(paper.invigilatorName, deserializedPaper.invigilatorName)

        val schedule = com.example.features.exams.domain.ExamSchedule(
            id = "ex_test_mid_10",
            schoolId = "sch_oxford_01",
            title = "Midterm Examination Series 2026",
            examType = com.example.features.exams.domain.ExamType.MIDTERM,
            academicSession = "2026-2027",
            gradeLevel = "Grade 10",
            startDate = "2026-05-10",
            endDate = "2026-05-14",
            status = com.example.features.exams.domain.ExamStatus.SCHEDULED,
            papers = listOf(paper)
        )

        val scheduleMap = schedule.toFirestoreMap()
        assertEquals("ex_test_mid_10", scheduleMap["id"])
        assertEquals("sch_oxford_01", scheduleMap["schoolId"])
        assertEquals("MIDTERM", scheduleMap["examType"])
        assertEquals("SCHEDULED", scheduleMap["status"])
        assertEquals(1, (scheduleMap["papers"] as List<*>).size)

        val deserializedSchedule = com.example.features.exams.domain.ExamSchedule.fromFirestoreMap(
            schedule.id,
            schedule.schoolId,
            scheduleMap
        )
        assertEquals(schedule.id, deserializedSchedule.id)
        assertEquals(schedule.title, deserializedSchedule.title)
        assertEquals(schedule.examType, deserializedSchedule.examType)
        assertEquals(schedule.academicSession, deserializedSchedule.academicSession)
        assertEquals(schedule.gradeLevel, deserializedSchedule.gradeLevel)
        assertEquals(1, deserializedSchedule.papers.size)
        assertEquals("Advanced Physics", deserializedSchedule.papers.first().subjectName)
    }

    @Test
    fun verifyExamRepositoryTimetableConflictDetectionAndPreSeededData() {
        val repo = com.example.features.exams.data.ExamRepository()

        // Test Pre-seeded data isolation
        val oxfordExams = repo.getPreSeededExams("sch_oxford_01")
        assertEquals(2, oxfordExams.size)
        assertTrue(oxfordExams.all { it.schoolId == "sch_oxford_01" })

        val cambridgeExams = repo.getPreSeededExams("sch_cambridge_101")
        assertEquals(1, cambridgeExams.size)
        assertTrue(cambridgeExams.all { it.schoolId == "sch_cambridge_101" })

        // Test Timetable Conflict Detection Engine
        val existingPapers = listOf(
            com.example.features.exams.domain.ExamPaper(
                id = "p_01",
                subjectName = "Physics",
                subjectCode = "PHY-101",
                examDate = "2026-05-10",
                startTime = "09:00 AM",
                endTime = "12:00 PM",
                roomNumber = "Hall A",
                invigilatorId = "t1",
                invigilatorName = "Teacher 1"
            )
        )

        // Conflict 1: Same date, same time, same room (Room Conflict)
        val roomConflict = repo.checkTimetableConflict(
            existingPapers = existingPapers,
            newDate = "2026-05-10",
            newStartTime = "09:00 AM",
            newEndTime = "12:00 PM",
            newRoom = "Hall A"
        )
        assertNotNull(roomConflict)
        assertTrue(roomConflict!!.contains("Room Conflict"))

        // Conflict 2: Same date, same time, different room (Cohort Time Conflict)
        val timeConflict = repo.checkTimetableConflict(
            existingPapers = existingPapers,
            newDate = "2026-05-10",
            newStartTime = "09:00 AM",
            newEndTime = "12:00 PM",
            newRoom = "Hall B"
        )
        assertNotNull(timeConflict)
        assertTrue(timeConflict!!.contains("Time Conflict"))

        // No Conflict: Different date or different time
        val noConflictDate = repo.checkTimetableConflict(
            existingPapers = existingPapers,
            newDate = "2026-05-11",
            newStartTime = "09:00 AM",
            newEndTime = "12:00 PM",
            newRoom = "Hall A"
        )
        assertNull(noConflictDate)

        val noConflictTime = repo.checkTimetableConflict(
            existingPapers = existingPapers,
            newDate = "2026-05-10",
            newStartTime = "01:00 PM",
            newEndTime = "04:00 PM",
            newRoom = "Hall C"
        )
        assertNull(noConflictTime)
    }

    @Test
    fun verifyExamViewModelFilteringCreationAndHallTicketGeneration() {
        TenantContext.setTenant("sch_oxford_01", UserRole.SCHOOL_ADMIN, "usr_admin_01")
        val vm = com.example.features.exams.presentation.ExamViewModel()

        assertEquals(2, vm.uiState.value.schedules.size)
        assertEquals(2, vm.uiState.value.filteredSchedules.size)
        assertEquals(5, vm.uiState.value.totalPapers)
        assertEquals(2, vm.uiState.value.scheduledSeriesCount)

        // Type Filter
        vm.setTypeFilter(com.example.features.exams.domain.ExamType.MIDTERM)
        assertEquals(1, vm.uiState.value.filteredSchedules.size)
        assertEquals("ex_oxf_mid_g10", vm.uiState.value.filteredSchedules.first().id)

        vm.clearFilters()
        assertEquals(2, vm.uiState.value.filteredSchedules.size)

        // Search Filter
        vm.setSearchQuery("Chemistry")
        assertEquals(1, vm.uiState.value.filteredSchedules.size)
        assertEquals("ex_oxf_mid_g10", vm.uiState.value.filteredSchedules.first().id)

        vm.clearFilters()

        // Create new examination series
        vm.createExamSchedule(
            title = "Periodic Assessment 1",
            examType = com.example.features.exams.domain.ExamType.PERIODIC,
            academicSession = "2026-2027",
            gradeLevel = "Grade 9",
            startDate = "2026-06-01",
            endDate = "2026-06-05"
        )
        assertEquals(3, vm.uiState.value.schedules.size)
        val created = vm.uiState.value.schedules.first()
        assertEquals("Periodic Assessment 1", created.title)
        assertEquals("Grade 9", created.gradeLevel)

        // Add Paper to newly created series
        val addPaperSuccess = vm.addPaperToSchedule(
            scheduleId = created.id,
            subjectName = "Introductory Biology",
            subjectCode = "BIO-091",
            examDate = "2026-06-01",
            startTime = "09:00 AM",
            endTime = "11:30 AM",
            durationMinutes = 150,
            totalMarks = 80,
            passingMarks = 32,
            roomNumber = "Room 101",
            invigilatorName = "Prof. Alan Turing"
        )
        assertTrue(addPaperSuccess)
        val updatedSchedule = vm.uiState.value.schedules.first { it.id == created.id }
        assertEquals(1, updatedSchedule.papers.size)
        assertEquals("Introductory Biology", updatedSchedule.papers.first().subjectName)

        // Test Conflict Detection on Adding Overlapping Paper
        val conflictAdd = vm.addPaperToSchedule(
            scheduleId = created.id,
            subjectName = "General Science",
            subjectCode = "SCI-092",
            examDate = "2026-06-01",
            startTime = "09:00 AM",
            endTime = "11:30 AM",
            durationMinutes = 150,
            totalMarks = 80,
            passingMarks = 32,
            roomNumber = "Room 101",
            invigilatorName = "Prof. Alan Turing"
        )
        assertFalse(conflictAdd)
        assertNotNull(vm.uiState.value.conflictWarning)

        // Test Student Hall Ticket / Admit Card Generation
        vm.generateStudentHallTicket(
            schedule = updatedSchedule,
            studentId = "stu_oxf_01",
            studentName = "Alexander Hamilton",
            rollNumber = "OXF-09A-001",
            admissionNumber = "ADM-OXF-2026-001",
            gradeLevel = "Grade 9",
            section = "Section A"
        )

        assertTrue(vm.uiState.value.showHallTicketDialog)
        val ticket = vm.uiState.value.generatedHallTicket
        assertNotNull(ticket)
        assertEquals("Alexander Hamilton", ticket!!.studentName)
        assertEquals("OXF-09A-001", ticket.rollNumber)
        assertEquals("ADM-OXF-2026-001", ticket.admissionNumber)
        assertEquals("Grade 9", ticket.gradeLevel)
        assertEquals("Section A", ticket.section)
        assertTrue(ticket.ticketNumber.startsWith("HT-"))
        assertEquals(1, ticket.papers.size)
        assertEquals("Introductory Biology", ticket.papers.first().subjectName)
    }

    @Test
    fun verifyStudentResultAndSubjectScoreFirestoreSerializationAndGradeCalculation() {
        val scores = listOf(
            com.example.features.results.domain.SubjectScore(
                subjectCode = "PHY-101",
                subjectName = "Physics",
                marksObtained = 92.0,
                totalMarks = 100.0
            ),
            com.example.features.results.domain.SubjectScore(
                subjectCode = "MAT-101",
                subjectName = "Calculus",
                marksObtained = 85.0,
                totalMarks = 100.0
            )
        )

        assertEquals("A+", scores[0].grade)
        assertEquals(4.0, scores[0].gradePoint, 0.001)
        assertEquals("A", scores[1].grade)
        assertEquals(3.7, scores[1].gradePoint, 0.001)

        val result = com.example.features.results.domain.StudentResult(
            id = "res_test_01",
            schoolId = "sch_oxford_01",
            examId = "ex_mid_10",
            examTitle = "Midterm Series 2026",
            academicSession = "2026-2027",
            studentId = "stu_01",
            studentName = "Amina Rahman",
            rollNumber = "OXF-10A-001",
            gradeLevel = "Grade 10",
            section = "Section A",
            subjectScores = scores,
            rankInClass = 1
        )

        assertEquals(177.0, result.totalObtained, 0.001)
        assertEquals(200.0, result.totalMaxMarks, 0.001)
        assertEquals(88.5, result.percentage, 0.001)
        assertEquals(3.85, result.gpa, 0.001)
        assertEquals("A", result.overallGrade)

        val map = result.toFirestoreMap()
        assertEquals("res_test_01", map["id"])
        assertEquals("sch_oxford_01", map["schoolId"])
        assertEquals("Amina Rahman", map["studentName"])
        assertEquals(88.5, map["percentage"])

        val deserialized = com.example.features.results.domain.StudentResult.fromFirestoreMap(
            result.id,
            result.schoolId,
            map
        )
        assertEquals(result.id, deserialized.id)
        assertEquals(result.studentName, deserialized.studentName)
        assertEquals(result.overallGrade, deserialized.overallGrade)
        assertEquals(2, deserialized.subjectScores.size)
    }

    @Test
    fun verifyResultRepositoryPreSeededDataAndReportCardGeneration() {
        val repo = com.example.features.results.data.ResultRepository()

        val oxfordResults = repo.getPreSeededResults("sch_oxford_01")
        assertEquals(3, oxfordResults.size)
        assertTrue(oxfordResults.all { it.schoolId == "sch_oxford_01" })

        val cambridgeResults = repo.getPreSeededResults("sch_cambridge_101")
        assertEquals(1, cambridgeResults.size)
        assertTrue(cambridgeResults.all { it.schoolId == "sch_cambridge_101" })

        val firstStudent = oxfordResults.first()
        val reportCard = repo.generateReportCard("sch_oxford_01", firstStudent, attendanceRate = 98.0)
        assertNotNull(reportCard)
        assertEquals("Oxford Academy", reportCard.schoolName)
        assertEquals("OXF-2026", reportCard.schoolCode)
        assertEquals(firstStudent.studentName, reportCard.studentResult.studentName)
        assertEquals(98.0, reportCard.attendancePercentage, 0.001)
        assertTrue(reportCard.honorsRemark.isNotEmpty())
    }

    @Test
    fun verifyResultViewModelMetricsFilteringAndScoreEntry() {
        TenantContext.setTenant("sch_oxford_01", UserRole.SCHOOL_ADMIN, "usr_admin_01")
        val vm = com.example.features.results.presentation.ResultViewModel()

        assertEquals(3, vm.uiState.value.results.size)
        assertEquals(3, vm.uiState.value.filteredResults.size)
        assertTrue(vm.uiState.value.classAveragePercentage > 0.0)
        assertTrue(vm.uiState.value.highestPercentage > 0.0)
        assertEquals(100.0, vm.uiState.value.passRatePercentage, 0.001)

        // Search filtering
        vm.setSearchQuery("Rahman")
        assertEquals(1, vm.uiState.value.filteredResults.size)
        assertEquals("Amina Rahman", vm.uiState.value.filteredResults.first().studentName)

        vm.clearFilters()
        assertEquals(3, vm.uiState.value.filteredResults.size)

        // Status filtering
        vm.setStatusFilter(com.example.features.results.domain.ResultStatus.PUBLISHED)
        assertEquals(3, vm.uiState.value.filteredResults.size)

        // Report Card generation dialog
        val target = vm.uiState.value.results.first()
        vm.openReportCard(target)
        assertTrue(vm.uiState.value.showReportCardDialog)
        assertNotNull(vm.uiState.value.generatedReportCard)
        assertEquals(target.studentName, vm.uiState.value.generatedReportCard!!.studentResult.studentName)
        vm.closeReportCardDialog()
        assertFalse(vm.uiState.value.showReportCardDialog)

        // Score entry
        val initialCount = vm.uiState.value.results.size
        vm.addStudentResult(
            studentName = "Charlotte Bronte",
            rollNumber = "OXF-10A-008",
            gradeLevel = "Grade 10",
            section = "Section A",
            examTitle = "Midterm Examination Series 2026",
            academicSession = "2026-2027",
            scores = listOf(
                com.example.features.results.domain.SubjectScore("LIT-101", "English Literature", 95.0, 100.0),
                com.example.features.results.domain.SubjectScore("HIS-101", "World History", 91.0, 100.0)
            ),
            teacherRemarks = "Outstanding literary analysis."
        )
        assertEquals(initialCount + 1, vm.uiState.value.results.size)
        val created = vm.uiState.value.results.first { it.studentName == "Charlotte Bronte" }
        assertEquals("Charlotte Bronte", created.studentName)
        assertEquals(93.0, created.percentage, 0.001)
        assertEquals("A+", created.overallGrade)
    }

    @Test
    fun verifySchoolNoticeFirestoreSerializationAndEnumMapping() {
        val notice = com.example.features.notices.domain.SchoolNotice(
            id = "not_test_01",
            schoolId = "sch_oxford_01",
            title = "Annual Sports Meet Schedule",
            content = "The annual track and field competitions will commence on Saturday.",
            category = com.example.features.notices.domain.NoticeCategory.EVENT,
            priority = com.example.features.notices.domain.NoticePriority.URGENT,
            targetAudience = com.example.features.notices.domain.NoticeTargetAudience.ALL,
            authorId = "usr_admin_01",
            authorName = "Sports Directorate",
            isPinned = true,
            publishedDate = "2026-03-25",
            attachmentTitle = "Sports_Schedule.pdf"
        )

        val map = notice.toFirestoreMap()
        assertEquals("not_test_01", map["id"])
        assertEquals("sch_oxford_01", map["schoolId"])
        assertEquals("EVENT", map["category"])
        assertEquals("URGENT", map["priority"])
        assertEquals("ALL", map["targetAudience"])
        assertEquals(true, map["isPinned"])

        val deserialized = com.example.features.notices.domain.SchoolNotice.fromFirestoreMap(
            notice.id,
            notice.schoolId,
            map
        )
        assertEquals(notice.id, deserialized.id)
        assertEquals(notice.title, deserialized.title)
        assertEquals(notice.category, deserialized.category)
        assertEquals(notice.priority, deserialized.priority)
        assertEquals(notice.targetAudience, deserialized.targetAudience)
        assertEquals(notice.isPinned, deserialized.isPinned)
        assertEquals(notice.attachmentTitle, deserialized.attachmentTitle)
    }

    @Test
    fun verifyNoticeRepositoryPreSeededNoticesAndTenantIsolation() {
        val repo = com.example.features.notices.data.NoticeRepository()

        val oxfordNotices = repo.getPreSeededNotices("sch_oxford_01")
        assertEquals(3, oxfordNotices.size)
        assertTrue(oxfordNotices.all { it.schoolId == "sch_oxford_01" })

        val cambridgeNotices = repo.getPreSeededNotices("sch_cambridge_101")
        assertEquals(1, cambridgeNotices.size)
        assertTrue(cambridgeNotices.all { it.schoolId == "sch_cambridge_101" })

        // Category filter
        val examNotices = oxfordNotices.filter { it.category == com.example.features.notices.domain.NoticeCategory.EXAMINATION }
        assertEquals(1, examNotices.size)
        assertEquals("Midterm Examination Series Instructions & Timetable Release", examNotices.first().title)
    }

    @Test
    fun verifyNoticeViewModelSearchFilteringPinningAndPublishing() {
        TenantContext.setTenant("sch_oxford_01", UserRole.SCHOOL_ADMIN, "usr_admin_01")
        val vm = com.example.features.notices.presentation.NoticeViewModel()

        assertEquals(3, vm.uiState.value.notices.size)
        assertEquals(3, vm.uiState.value.filteredNotices.size)
        assertEquals(1, vm.uiState.value.urgentNoticesCount)
        assertEquals(2, vm.uiState.value.pinnedNoticesCount)

        // Search query
        vm.setSearchQuery("Robotics")
        assertEquals(1, vm.uiState.value.filteredNotices.size)
        assertEquals("Annual Science Exhibition & Robotics Fair 2026", vm.uiState.value.filteredNotices.first().title)

        vm.clearFilters()
        assertEquals(3, vm.uiState.value.filteredNotices.size)

        // Category filter
        vm.setCategoryFilter(com.example.features.notices.domain.NoticeCategory.ACADEMIC)
        assertEquals(1, vm.uiState.value.filteredNotices.size)
        assertEquals(com.example.features.notices.domain.NoticeCategory.ACADEMIC, vm.uiState.value.filteredNotices.first().category)

        vm.clearFilters()

        // Toggle pin
        val firstNotice = vm.uiState.value.notices.first { !it.isPinned }
        val idToPin = firstNotice.id
        vm.togglePinNotice(idToPin)
        val pinned = vm.uiState.value.notices.first { it.id == idToPin }
        assertTrue(pinned.isPinned)

        // Publish new notice
        val initialCount = vm.uiState.value.notices.size
        vm.createNotice(
            title = "Spring Equinox Recess Notice",
            content = "The school campus will remain closed on Friday for the Vernal Equinox observance. Regular classes resume Monday.",
            category = com.example.features.notices.domain.NoticeCategory.HOLIDAY,
            priority = com.example.features.notices.domain.NoticePriority.IMPORTANT,
            targetAudience = com.example.features.notices.domain.NoticeTargetAudience.ALL,
            isPinned = false,
            attachmentTitle = "Recess_Notice.pdf"
        )

        assertEquals(initialCount + 1, vm.uiState.value.notices.size)
        val created = vm.uiState.value.notices.first { it.title == "Spring Equinox Recess Notice" }
        assertEquals(com.example.features.notices.domain.NoticeCategory.HOLIDAY, created.category)
        assertEquals(com.example.features.notices.domain.NoticePriority.IMPORTANT, created.priority)

        // Detail dialog
        vm.openNoticeDetail(created)
        assertTrue(vm.uiState.value.showNoticeDetailDialog)
        assertEquals(created.id, vm.uiState.value.selectedNoticeForDetail?.id)
        vm.closeNoticeDetailDialog()
        assertFalse(vm.uiState.value.showNoticeDetailDialog)
    }

    @Test
    fun verifyHomeworkAssignmentAndSubmissionSerializationAndMetrics() {
        val submission = com.example.features.homework.domain.HomeworkSubmission(
            studentId = "stu_01",
            studentName = "Amina Rahman",
            rollNumber = "OXF-10A-001",
            submissionDate = "2026-03-24",
            attachmentTitle = "Amina_Lab_Report.pdf",
            studentNotes = "Included regression analysis in appendix.",
            status = com.example.features.homework.domain.SubmissionStatus.GRADED,
            marksAwarded = 48.0,
            feedback = "Superb lab write-up",
            evaluatedBy = "Dr. Sarah Jenkins",
            evaluatedAt = 1711280000000L
        )

        val subMap = submission.toFirestoreMap()
        assertEquals("stu_01", subMap["studentId"])
        assertEquals("Amina Rahman", subMap["studentName"])
        assertEquals("GRADED", subMap["status"])
        assertEquals(48.0, subMap["marksAwarded"])

        val deserializedSub = com.example.features.homework.domain.HomeworkSubmission.fromFirestoreMap(subMap)
        assertEquals(submission.studentId, deserializedSub.studentId)
        assertEquals(submission.studentName, deserializedSub.studentName)
        assertEquals(submission.status, deserializedSub.status)
        assertEquals(submission.marksAwarded, deserializedSub.marksAwarded)

        val assignment = com.example.features.homework.domain.HomeworkAssignment(
            id = "hw_test_01",
            schoolId = "sch_oxford_01",
            title = "Optics & Lens Refraction",
            description = "Calculate focal length with convex and concave lenses.",
            subjectCode = "PHY-101",
            subjectName = "Advanced Physics",
            gradeLevel = "Grade 10",
            section = "Section A",
            assignedDate = "2026-03-24",
            dueDate = "2026-03-31",
            totalMarks = 50.0,
            teacherId = "tea_01",
            teacherName = "Dr. Sarah Jenkins",
            status = com.example.features.homework.domain.HomeworkStatus.ASSIGNED,
            attachmentTitle = "Optics_Lab_Guide.pdf",
            submissions = listOf(submission),
            totalEnrolledStudents = 32
        )

        assertEquals(1, assignment.submissionCount)
        assertEquals(1, assignment.gradedCount)
        assertEquals(0, assignment.pendingReviewCount)
        assertEquals(3.125, assignment.submissionRatePercentage, 0.001)
        assertEquals(48.0, assignment.averageScore, 0.001)

        val assignMap = assignment.toFirestoreMap()
        assertEquals("hw_test_01", assignMap["id"])
        assertEquals("sch_oxford_01", assignMap["schoolId"])
        assertEquals("Optics & Lens Refraction", assignMap["title"])
        assertEquals(50.0, assignMap["totalMarks"])
        assertEquals("ASSIGNED", assignMap["status"])

        val deserializedAssign = com.example.features.homework.domain.HomeworkAssignment.fromFirestoreMap(
            assignment.id,
            assignment.schoolId,
            assignMap
        )
        assertEquals(assignment.id, deserializedAssign.id)
        assertEquals(assignment.title, deserializedAssign.title)
        assertEquals(assignment.subjectName, deserializedAssign.subjectName)
        assertEquals(1, deserializedAssign.submissions.size)
        assertEquals("Amina Rahman", deserializedAssign.submissions.first().studentName)
    }

    @Test
    fun verifyHomeworkRepositoryPreSeededAssignmentsAndTenantIsolation() {
        val repo = com.example.features.homework.data.HomeworkRepository()

        val oxfordHomework = repo.getPreSeededHomework("sch_oxford_01")
        assertEquals(3, oxfordHomework.size)
        assertTrue(oxfordHomework.all { it.schoolId == "sch_oxford_01" })

        val cambridgeHomework = repo.getPreSeededHomework("sch_cambridge_101")
        assertEquals(1, cambridgeHomework.size)
        assertTrue(cambridgeHomework.all { it.schoolId == "sch_cambridge_101" })

        // Subject filter
        val physics = oxfordHomework.filter { it.subjectCode == "PHY-101" }
        assertEquals(1, physics.size)
        assertEquals("Lab Report: Simple Harmonic Motion & Pendulum Dynamics", physics.first().title)
    }

    @Test
    fun verifyHomeworkViewModelSearchFilteringAssignmentCreationAndGradingFlow() {
        TenantContext.setTenant("sch_oxford_01", UserRole.SCHOOL_ADMIN, "usr_admin_01")
        val vm = com.example.features.homework.presentation.HomeworkViewModel()

        assertEquals(3, vm.uiState.value.assignments.size)
        assertEquals(3, vm.uiState.value.filteredAssignments.size)
        assertEquals(2, vm.uiState.value.activeAssignmentsCount)
        assertEquals(5, vm.uiState.value.totalSubmissionsCount)

        // Search filtering
        vm.setSearchQuery("Pendulum")
        assertEquals(1, vm.uiState.value.filteredAssignments.size)
        assertEquals("hw_oxf_phy_01", vm.uiState.value.filteredAssignments.first().id)

        vm.clearFilters()
        assertEquals(3, vm.uiState.value.filteredAssignments.size)

        // Status filtering
        vm.setStatusFilter(com.example.features.homework.domain.HomeworkStatus.CLOSED)
        assertEquals(1, vm.uiState.value.filteredAssignments.size)
        assertEquals("hw_oxf_eng_03", vm.uiState.value.filteredAssignments.first().id)

        vm.clearFilters()

        // Create new assignment
        val initialCount = vm.uiState.value.assignments.size
        vm.createAssignment(
            title = "Chemistry Organic Synthesis Mechanisms",
            description = "Draw full electron arrow pushing mechanisms for aldol condensation and electrophilic aromatic substitution.",
            subjectCode = "CHM-104",
            subjectName = "Organic Chemistry",
            gradeLevel = "Grade 10",
            section = "Section A",
            dueDate = "2026-04-10",
            totalMarks = 60.0,
            attachmentTitle = "Reaction_Mechanisms_Worksheet.pdf"
        )

        assertEquals(initialCount + 1, vm.uiState.value.assignments.size)
        val created = vm.uiState.value.assignments.first { it.title == "Chemistry Organic Synthesis Mechanisms" }
        assertEquals("CHM-104", created.subjectCode)
        assertEquals(60.0, created.totalMarks, 0.001)

        // Submit student work (requires STUDENT role / HOMEWORK_SUBMIT permission)
        TenantContext.setTenant("sch_oxford_01", UserRole.STUDENT, "stu_010")
        vm.submitStudentWork(
            assignmentId = created.id,
            studentName = "Isaac Newton",
            rollNumber = "OXF-10A-010",
            attachmentTitle = "Newton_Chemistry_Mechanisms.pdf",
            studentNotes = "Included all resonance structures for carbocation intermediate."
        )

        val updatedAssignment = vm.uiState.value.assignments.first { it.id == created.id }
        assertEquals(1, updatedAssignment.submissions.size)
        val submittedWork = updatedAssignment.submissions.first()
        assertEquals("Isaac Newton", submittedWork.studentName)
        assertEquals(com.example.features.homework.domain.SubmissionStatus.SUBMITTED, submittedWork.status)

        // Grade submission (requires TEACHER or SCHOOL_ADMIN role / HOMEWORK_GRADE permission)
        TenantContext.setTenant("sch_oxford_01", UserRole.TEACHER, "tea_01")
        vm.gradeStudentSubmission(
            assignmentId = created.id,
            studentId = submittedWork.studentId,
            marksAwarded = 58.0,
            feedback = "Flawless stereochemical control and arrow push accuracy."
        )

        val gradedAssignment = vm.uiState.value.assignments.first { it.id == created.id }
        val evaluatedWork = gradedAssignment.submissions.first()
        assertEquals(com.example.features.homework.domain.SubmissionStatus.GRADED, evaluatedWork.status)
        assertEquals(58.0, evaluatedWork.marksAwarded!!, 0.001)
        assertEquals("Flawless stereochemical control and arrow push accuracy.", evaluatedWork.feedback)

        // Detail dialog
        vm.openAssignmentDetail(gradedAssignment)
        assertTrue(vm.uiState.value.showDetailDialog)
        assertEquals(gradedAssignment.id, vm.uiState.value.selectedAssignmentForAction?.id)
        vm.closeAssignmentDetail()
        assertFalse(vm.uiState.value.showDetailDialog)
    }

    @Test
    fun verifyLibraryBookAndLoanRecordFirestoreSerializationAndMetrics() {
        val activeLoan = com.example.features.library.domain.BookLoanRecord(
            loanId = "ln_test_01",
            borrowerId = "stu_01",
            borrowerName = "Amina Rahman",
            borrowerRole = "Student",
            rollOrStaffId = "OXF-10A-001",
            issueDate = "2026-03-10",
            dueDate = "2026-03-24",
            returnDate = "",
            status = com.example.features.library.domain.LoanStatus.OVERDUE,
            fineAmount = 3.50,
            notes = "Two days overdue"
        )

        val loanMap = activeLoan.toFirestoreMap()
        assertEquals("ln_test_01", loanMap["loanId"])
        assertEquals("Amina Rahman", loanMap["borrowerName"])
        assertEquals("OVERDUE", loanMap["status"])
        assertEquals(3.50, loanMap["fineAmount"])

        val deserializedLoan = com.example.features.library.domain.BookLoanRecord.fromFirestoreMap(loanMap)
        assertEquals(activeLoan.loanId, deserializedLoan.loanId)
        assertEquals(activeLoan.borrowerName, deserializedLoan.borrowerName)
        assertEquals(activeLoan.status, deserializedLoan.status)
        assertEquals(activeLoan.fineAmount, deserializedLoan.fineAmount, 0.001)

        val book = com.example.features.library.domain.LibraryBook(
            id = "lib_test_01",
            schoolId = "sch_oxford_01",
            isbn = "978-0-13-468599-1",
            title = "Effective Kotlin",
            author = "Marcin Moskala",
            publisher = "Leanpub",
            category = com.example.features.library.domain.BookCategory.COMPUTER_SCIENCE,
            shelfLocation = "Wing C • Rack CS-01",
            totalCopies = 10,
            availableCopies = 7,
            publicationYear = 2023,
            description = "Best practices for Kotlin engineering.",
            loans = listOf(activeLoan)
        )

        assertEquals(3, book.issuedCopiesCount)
        assertEquals(1, book.activeLoansCount)
        assertEquals(1, book.overdueLoansCount)
        assertTrue(book.isAvailable)

        val bookMap = book.toFirestoreMap()
        assertEquals("lib_test_01", bookMap["id"])
        assertEquals("sch_oxford_01", bookMap["schoolId"])
        assertEquals("COMPUTER_SCIENCE", bookMap["category"])
        assertEquals(10, bookMap["totalCopies"])
        assertEquals(7, bookMap["availableCopies"])

        val deserializedBook = com.example.features.library.domain.LibraryBook.fromFirestoreMap(
            book.id,
            book.schoolId,
            bookMap
        )
        assertEquals(book.id, deserializedBook.id)
        assertEquals(book.title, deserializedBook.title)
        assertEquals(book.category, deserializedBook.category)
        assertEquals(1, deserializedBook.loans.size)
        assertEquals("Amina Rahman", deserializedBook.loans.first().borrowerName)
    }

    @Test
    fun verifyLibraryRepositoryPreSeededBooksAndTenantIsolation() {
        val repo = com.example.features.library.data.LibraryRepository()

        val oxfordBooks = repo.getPreSeededBooks("sch_oxford_01")
        assertEquals(4, oxfordBooks.size)
        assertTrue(oxfordBooks.all { it.schoolId == "sch_oxford_01" })

        val cambridgeBooks = repo.getPreSeededBooks("sch_cambridge_101")
        assertEquals(2, cambridgeBooks.size)
        assertTrue(cambridgeBooks.all { it.schoolId == "sch_cambridge_101" })

        val scienceBooks = oxfordBooks.filter { it.category == com.example.features.library.domain.BookCategory.SCIENCE }
        assertEquals(1, scienceBooks.size)
        assertEquals("University Physics with Modern Physics", scienceBooks.first().title)
    }

    @Test
    fun verifyLibraryViewModelFilteringCatalogingIssueReturnAndRbac() {
        TenantContext.setTenant("sch_oxford_01", UserRole.LIBRARIAN, "lib_staff_01")
        val vm = com.example.features.library.presentation.LibraryViewModel()

        assertEquals(4, vm.uiState.value.totalTitlesCount)
        assertEquals(41, vm.uiState.value.totalPhysicalCopies)
        assertEquals(37, vm.uiState.value.availableCopiesCount)
        assertEquals(4, vm.uiState.value.activeIssuedCount)
        assertEquals(1, vm.uiState.value.overdueLoansCount)

        // Search filtering
        vm.setSearchQuery("Algorithms")
        assertEquals(1, vm.uiState.value.filteredBooks.size)
        assertEquals("lib_oxf_02", vm.uiState.value.filteredBooks.first().id)

        vm.clearFilters()
        assertEquals(4, vm.uiState.value.filteredBooks.size)

        // Category filtering
        vm.setCategoryFilter(com.example.features.library.domain.BookCategory.SCIENCE)
        assertEquals(1, vm.uiState.value.filteredBooks.size)
        assertEquals("lib_oxf_01", vm.uiState.value.filteredBooks.first().id)

        vm.clearFilters()

        // Catalog new book
        vm.openAddBookDialog()
        assertTrue(vm.uiState.value.showAddBookDialog)
        vm.addBook(
            isbn = "978-0-307-27767-1",
            title = "Cosmos",
            author = "Carl Sagan",
            publisher = "Random House",
            category = com.example.features.library.domain.BookCategory.SCIENCE,
            shelfLocation = "Wing B • Rack S-15",
            totalCopies = 2,
            publicationYear = 2013,
            description = "Classic exploration of astronomy and scientific thought."
        )
        assertFalse(vm.uiState.value.showAddBookDialog)
        assertEquals(5, vm.uiState.value.totalTitlesCount)

        val catalogedBook = vm.uiState.value.books.first { it.title == "Cosmos" }
        assertEquals(2, catalogedBook.availableCopies)

        // Issue a copy
        vm.openIssueBookDialog(catalogedBook)
        assertTrue(vm.uiState.value.showIssueBookDialog)
        vm.issueBook(
            bookId = catalogedBook.id,
            borrowerName = "Isaac Newton",
            rollOrStaffId = "OXF-10A-010",
            borrowerRole = "Student",
            dueDate = "2026-04-15",
            notes = "Astrophysics reading assignment"
        )
        assertFalse(vm.uiState.value.showIssueBookDialog)

        val afterIssue = vm.uiState.value.books.first { it.id == catalogedBook.id }
        assertEquals(1, afterIssue.availableCopies)
        assertEquals(1, afterIssue.loans.size)
        val issuedLoan = afterIssue.loans.first()
        assertEquals(com.example.features.library.domain.LoanStatus.ISSUED, issuedLoan.status)

        // Availability filter
        vm.toggleOnlyAvailableFilter()
        assertTrue(vm.uiState.value.onlyAvailableFilter)
        assertTrue(vm.uiState.value.filteredBooks.all { it.isAvailable })
        vm.clearFilters()

        // Return / Check in the copy
        vm.openBookDetail(afterIssue)
        assertTrue(vm.uiState.value.showBookDetailDialog)
        vm.returnBook(
            bookId = afterIssue.id,
            loanId = issuedLoan.loanId,
            fineAmount = 0.0
        )

        val afterReturn = vm.uiState.value.books.first { it.id == catalogedBook.id }
        assertEquals(2, afterReturn.availableCopies)
        assertEquals(com.example.features.library.domain.LoanStatus.RETURNED, afterReturn.loans.first().status)
        vm.closeBookDetail()
        assertFalse(vm.uiState.value.showBookDetailDialog)

        // RBAC enforcement: Student cannot catalog or issue books
        TenantContext.setTenant("sch_oxford_01", UserRole.STUDENT, "stu_010")
        vm.dismissStatusMessage()
        vm.openAddBookDialog()
        assertFalse(vm.uiState.value.showAddBookDialog)
        assertTrue(vm.uiState.value.errorMessage?.contains("Unauthorized") == true)

        vm.dismissStatusMessage()
        vm.addBook(
            isbn = "978-0-000-00000-0",
            title = "Unauthorized Book",
            author = "Nobody",
            publisher = "None",
            category = com.example.features.library.domain.BookCategory.FICTION,
            shelfLocation = "A-1",
            totalCopies = 1,
            publicationYear = 2024,
            description = ""
        )
        assertTrue(vm.uiState.value.errorMessage?.contains("Unauthorized") == true)
        assertEquals(5, vm.uiState.value.totalTitlesCount)
    }

    @Test
    fun verifyTransportRouteStopsPassengersFirestoreSerializationAndMetrics() {
        val stop = com.example.features.transport.domain.TransportStop(
            stopId = "stp_test_01",
            stopName = "Magdalen Bridge Stop",
            pickupTime = "07:25 AM",
            dropTime = "03:40 PM",
            landmark = "High Street East",
            monthlyFee = 85.0
        )

        val stopMap = stop.toFirestoreMap()
        assertEquals("stp_test_01", stopMap["stopId"])
        assertEquals("Magdalen Bridge Stop", stopMap["stopName"])
        assertEquals(85.0, stopMap["monthlyFee"])

        val deserializedStop = com.example.features.transport.domain.TransportStop.fromFirestoreMap(stopMap)
        assertEquals(stop.stopId, deserializedStop.stopId)
        assertEquals(stop.stopName, deserializedStop.stopName)
        assertEquals(stop.monthlyFee, deserializedStop.monthlyFee, 0.001)

        val passenger = com.example.features.transport.domain.TransportPassenger(
            passengerId = "pax_test_01",
            studentId = "stu_01",
            studentName = "Amina Rahman",
            rollNumber = "OXF-10A-001",
            gradeLevel = "Grade 10 - A",
            stopId = "stp_test_01",
            stopName = "Magdalen Bridge Stop",
            guardianContact = "+44 7700 900401",
            isMorningPickup = true,
            isAfternoonDrop = false
        )

        val paxMap = passenger.toFirestoreMap()
        assertEquals("pax_test_01", paxMap["passengerId"])
        assertEquals("Amina Rahman", paxMap["studentName"])
        assertEquals(false, paxMap["isAfternoonDrop"])

        val deserializedPax = com.example.features.transport.domain.TransportPassenger.fromFirestoreMap(paxMap)
        assertEquals(passenger.passengerId, deserializedPax.passengerId)
        assertEquals(passenger.rollNumber, deserializedPax.rollNumber)
        assertFalse(deserializedPax.isAfternoonDrop)

        val route = com.example.features.transport.domain.TransportRoute(
            id = "rt_test_01",
            schoolId = "sch_oxford_01",
            routeCode = "RT-OXF-99",
            routeName = "East Cowley Express",
            vehicleNumber = "OXF-BUS-999",
            vehicleType = com.example.features.transport.domain.VehicleType.ELECTRIC_COACH,
            capacity = 20,
            driverName = "Arthur Pendelton",
            driverPhone = "+44 7700 900112",
            attendantName = "Martha Evans",
            status = com.example.features.transport.domain.RouteStatus.ACTIVE,
            stops = listOf(stop),
            passengers = listOf(passenger)
        )

        assertEquals(1, route.occupiedSeatsCount)
        assertEquals(19, route.availableSeatsCount)
        assertEquals(5.0, route.occupancyRatePercentage, 0.001)
        assertTrue(route.hasAvailableSeat)

        val routeMap = route.toFirestoreMap()
        assertEquals("rt_test_01", routeMap["id"])
        assertEquals("sch_oxford_01", routeMap["schoolId"])
        assertEquals("ELECTRIC_COACH", routeMap["vehicleType"])
        assertEquals("ACTIVE", routeMap["status"])
        assertEquals(20, routeMap["capacity"])

        val deserializedRoute = com.example.features.transport.domain.TransportRoute.fromFirestoreMap(
            route.id,
            route.schoolId,
            routeMap
        )
        assertEquals(route.id, deserializedRoute.id)
        assertEquals(route.routeCode, deserializedRoute.routeCode)
        assertEquals(route.vehicleType, deserializedRoute.vehicleType)
        assertEquals(1, deserializedRoute.stops.size)
        assertEquals(1, deserializedRoute.passengers.size)
        assertEquals("Amina Rahman", deserializedRoute.passengers.first().studentName)
    }

    @Test
    fun verifyTransportRepositoryPreSeededRoutesAndTenantIsolation() {
        val repo = com.example.features.transport.data.TransportRepository()

        val oxfordRoutes = repo.getPreSeededRoutes("sch_oxford_01")
        assertEquals(3, oxfordRoutes.size)
        assertTrue(oxfordRoutes.all { it.schoolId == "sch_oxford_01" })

        val cambridgeRoutes = repo.getPreSeededRoutes("sch_cambridge_101")
        assertEquals(2, cambridgeRoutes.size)
        assertTrue(cambridgeRoutes.all { it.schoolId == "sch_cambridge_101" })

        val delayedRoutes = oxfordRoutes.filter { it.status == com.example.features.transport.domain.RouteStatus.DELAYED }
        assertEquals(1, delayedRoutes.size)
        assertEquals("South Abingdon Link", delayedRoutes.first().routeName)
    }

    @Test
    fun verifyTransportViewModelFilteringFleetCreationBoardingStatusAndRbac() {
        TenantContext.setTenant("sch_oxford_01", UserRole.TRANSPORT_MANAGER, "trn_mgr_01")
        val vm = com.example.features.transport.presentation.TransportViewModel()

        assertEquals(3, vm.uiState.value.totalRoutesCount)
        assertEquals(2, vm.uiState.value.activeRoutesCount)
        assertEquals(94, vm.uiState.value.totalFleetCapacity)
        assertEquals(4, vm.uiState.value.assignedPassengersCount)
        assertEquals(90, vm.uiState.value.availableSeatsTotal)

        // Search filtering by stop/corridor
        vm.setSearchQuery("Summertown")
        assertEquals(1, vm.uiState.value.filteredRoutes.size)
        assertEquals("rt_oxf_02", vm.uiState.value.filteredRoutes.first().id)

        vm.clearFilters()
        assertEquals(3, vm.uiState.value.filteredRoutes.size)

        // Status filtering
        vm.setStatusFilter(com.example.features.transport.domain.RouteStatus.DELAYED)
        assertEquals(1, vm.uiState.value.filteredRoutes.size)
        assertEquals("rt_oxf_03", vm.uiState.value.filteredRoutes.first().id)

        vm.clearFilters()

        // Only open active seats filter
        vm.toggleOnlyAvailableSeatsFilter()
        assertEquals(2, vm.uiState.value.filteredRoutes.size)
        assertTrue(vm.uiState.value.filteredRoutes.all { it.hasAvailableSeat })
        vm.clearFilters()

        // Register a new fleet route
        vm.openAddRouteDialog()
        assertTrue(vm.uiState.value.showAddRouteDialog)
        vm.addRoute(
            routeCode = "RT-OXF-04",
            routeName = "Iffley & Cowley Marsh Loop",
            vehicleNumber = "OXF-BUS-404",
            vehicleType = com.example.features.transport.domain.VehicleType.ELECTRIC_COACH,
            capacity = 30,
            driverName = "Evelyn Reed",
            driverPhone = "+44 7700 900555",
            attendantName = "Samuel Brooks",
            departureTime = "07:20 AM",
            arrivalTime = "08:08 AM",
            primaryStopName = "Iffley Lock Stop",
            primaryStopPickupTime = "07:28 AM",
            primaryStopMonthlyFee = 90.0,
            notes = "Wheelchair ramp equipped"
        )
        assertFalse(vm.uiState.value.showAddRouteDialog)
        assertEquals(4, vm.uiState.value.totalRoutesCount)

        val createdRoute = vm.uiState.value.routes.first { it.routeCode == "RT-OXF-04" }
        assertEquals(30, createdRoute.availableSeatsCount)
        assertEquals(1, createdRoute.stops.size)

        // Board a student passenger
        vm.openAssignPassengerDialog(createdRoute)
        assertTrue(vm.uiState.value.showAssignPassengerDialog)
        vm.assignPassenger(
            routeId = createdRoute.id,
            studentName = "Isaac Newton",
            rollNumber = "OXF-10A-010",
            gradeLevel = "Grade 10 - A",
            stopId = createdRoute.stops.first().stopId,
            stopName = createdRoute.stops.first().stopName,
            guardianContact = "+44 7700 900666",
            isMorningPickup = true,
            isAfternoonDrop = true
        )
        assertFalse(vm.uiState.value.showAssignPassengerDialog)

        val afterBoard = vm.uiState.value.routes.first { it.id == createdRoute.id }
        assertEquals(1, afterBoard.occupiedSeatsCount)
        assertEquals(29, afterBoard.availableSeatsCount)
        val boardedPax = afterBoard.passengers.first()
        assertEquals("Isaac Newton", boardedPax.studentName)

        // Inspect manifest, add stop, change status, and remove passenger
        vm.openRouteDetail(afterBoard)
        assertTrue(vm.uiState.value.showRouteDetailDialog)
        vm.openAddStopDialog(afterBoard)
        assertTrue(vm.uiState.value.showAddStopDialog)
        vm.addStopToRoute(
            routeId = afterBoard.id,
            stopName = "Carfax Tower Stop",
            pickupTime = "07:45 AM",
            dropTime = "03:30 PM",
            landmark = "Queen Street Corner",
            monthlyFee = 75.0
        )
        assertFalse(vm.uiState.value.showAddStopDialog)
        assertEquals(2, vm.uiState.value.routes.first { it.id == createdRoute.id }.stops.size)

        vm.updateRouteStatus(afterBoard.id, com.example.features.transport.domain.RouteStatus.DELAYED)
        assertEquals(
            com.example.features.transport.domain.RouteStatus.DELAYED,
            vm.uiState.value.routes.first { it.id == createdRoute.id }.status
        )

        vm.removePassenger(afterBoard.id, boardedPax.passengerId)
        val afterRemove = vm.uiState.value.routes.first { it.id == createdRoute.id }
        assertEquals(0, afterRemove.occupiedSeatsCount)
        assertEquals(30, afterRemove.availableSeatsCount)
        vm.closeRouteDetail()
        assertFalse(vm.uiState.value.showRouteDetailDialog)

        // RBAC enforcement: Parent has TRANSPORT_VIEW only, cannot manage fleet or assign riders
        TenantContext.setTenant("sch_oxford_01", UserRole.PARENT, "par_001")
        vm.dismissStatusMessage()
        vm.openAddRouteDialog()
        assertFalse(vm.uiState.value.showAddRouteDialog)
        assertTrue(vm.uiState.value.errorMessage?.contains("Unauthorized") == true)

        vm.dismissStatusMessage()
        vm.updateRouteStatus(createdRoute.id, com.example.features.transport.domain.RouteStatus.ACTIVE)
        assertTrue(vm.uiState.value.errorMessage?.contains("Unauthorized") == true)
        assertEquals(
            com.example.features.transport.domain.RouteStatus.DELAYED,
            vm.uiState.value.routes.first { it.id == createdRoute.id }.status
        )
    }

    @Test
    fun verifySchoolEventAndRsvpFirestoreSerializationAndCapacityMetrics() {
        val rsvp = com.example.features.events.domain.EventRsvpRecord(
            rsvpId = "rsvp_test_01",
            userId = "par_001",
            attendeeName = "David Rahman",
            attendeeRole = "Parent",
            guestsCount = 3,
            notes = "Front row seating requested"
        )

        val rsvpMap = rsvp.toFirestoreMap()
        assertEquals("rsvp_test_01", rsvpMap["rsvpId"])
        assertEquals("David Rahman", rsvpMap["attendeeName"])
        assertEquals(3, rsvpMap["guestsCount"])

        val deserializedRsvp = com.example.features.events.domain.EventRsvpRecord.fromFirestoreMap(rsvpMap)
        assertEquals(rsvp.rsvpId, deserializedRsvp.rsvpId)
        assertEquals(rsvp.attendeeName, deserializedRsvp.attendeeName)
        assertEquals(3, deserializedRsvp.guestsCount)

        val event = com.example.features.events.domain.SchoolEvent(
            id = "evt_test_01",
            schoolId = "sch_oxford_01",
            title = "Annual Winter Concert",
            description = "Orchestra and choir showcase.",
            category = com.example.features.events.domain.EventCategory.CULTURAL,
            status = com.example.features.events.domain.EventStatus.UPCOMING,
            eventDate = "2026-12-12",
            startTime = "06:00 PM",
            endTime = "08:30 PM",
            venue = "Main Auditorium",
            organizerName = "Music Department",
            targetAudience = "All Families",
            isHoliday = false,
            rsvpRequired = true,
            maxCapacity = 5,
            rsvps = listOf(rsvp)
        )

        assertEquals(3, event.totalRsvpHeadcount)
        assertEquals(2, event.remainingRsvpSlots)
        assertTrue(event.isRsvpOpen)

        val eventMap = event.toFirestoreMap()
        assertEquals("evt_test_01", eventMap["id"])
        assertEquals("sch_oxford_01", eventMap["schoolId"])
        assertEquals("CULTURAL", eventMap["category"])
        assertEquals("UPCOMING", eventMap["status"])
        assertEquals(5, eventMap["maxCapacity"])

        val deserializedEvent = com.example.features.events.domain.SchoolEvent.fromFirestoreMap(
            event.id,
            event.schoolId,
            eventMap
        )
        assertEquals(event.id, deserializedEvent.id)
        assertEquals(event.title, deserializedEvent.title)
        assertEquals(event.category, deserializedEvent.category)
        assertEquals(1, deserializedEvent.rsvps.size)
        assertEquals("David Rahman", deserializedEvent.rsvps.first().attendeeName)
    }

    @Test
    fun verifyEventRepositoryPreSeededEventsAndTenantIsolation() {
        val repo = com.example.features.events.data.EventRepository()

        val oxfordEvents = repo.getPreSeededEvents("sch_oxford_01")
        assertEquals(4, oxfordEvents.size)
        assertTrue(oxfordEvents.all { it.schoolId == "sch_oxford_01" })

        val cambridgeEvents = repo.getPreSeededEvents("sch_cambridge_101")
        assertEquals(2, cambridgeEvents.size)
        assertTrue(cambridgeEvents.all { it.schoolId == "sch_cambridge_101" })

        val holidays = oxfordEvents.filter { it.isHoliday }
        assertEquals(1, holidays.size)
        assertEquals("Founder's Day & Spring Bank Holiday", holidays.first().title)
    }

    @Test
    fun verifyEventViewModelFilteringCreationRsvpCapacityStatusAndRbac() {
        TenantContext.setTenant("sch_oxford_01", UserRole.SCHOOL_ADMIN, "usr_admin_01")
        val vm = com.example.features.events.presentation.EventViewModel()

        assertEquals(4, vm.uiState.value.totalEventsCount)
        assertEquals(3, vm.uiState.value.upcomingEventsCount)
        assertEquals(1, vm.uiState.value.holidaysCount)
        assertEquals(7, vm.uiState.value.totalRsvpCount)

        // Search filtering
        vm.setSearchQuery("Robotics")
        assertEquals(1, vm.uiState.value.filteredEvents.size)
        assertEquals("evt_oxf_04", vm.uiState.value.filteredEvents.first().id)

        vm.clearFilters()
        assertEquals(4, vm.uiState.value.filteredEvents.size)

        // Category & holiday filtering
        vm.setCategoryFilter(com.example.features.events.domain.EventCategory.SPORTS)
        assertEquals(1, vm.uiState.value.filteredEvents.size)
        assertEquals("evt_oxf_01", vm.uiState.value.filteredEvents.first().id)
        vm.clearFilters()

        vm.toggleOnlyHolidaysFilter()
        assertEquals(1, vm.uiState.value.filteredEvents.size)
        assertEquals("evt_oxf_03", vm.uiState.value.filteredEvents.first().id)
        vm.clearFilters()

        // Create new event with RSVP capacity
        vm.openCreateEventDialog()
        assertTrue(vm.uiState.value.showCreateEventDialog)
        vm.createEvent(
            title = "Graduation & Prize Giving Gala",
            description = "Senior class awards and valedictory address.",
            category = com.example.features.events.domain.EventCategory.ACADEMIC,
            eventDate = "2026-06-20",
            startTime = "05:00 PM",
            endTime = "08:00 PM",
            venue = "Oxford Great Hall",
            organizerName = "Dr. Robert Vance",
            targetAudience = "Grade 12 Families",
            isHoliday = false,
            rsvpRequired = true,
            maxCapacity = 4
        )
        assertFalse(vm.uiState.value.showCreateEventDialog)
        assertEquals(5, vm.uiState.value.totalEventsCount)

        val createdEvent = vm.uiState.value.events.first { it.title == "Graduation & Prize Giving Gala" }
        assertEquals(4, createdEvent.remainingRsvpSlots)

        // Parent role has EVENTS_VIEW and can submit RSVP
        TenantContext.setTenant("sch_oxford_01", UserRole.PARENT, "par_010")
        vm.openRsvpDialog(createdEvent)
        assertTrue(vm.uiState.value.showRsvpDialog)
        vm.submitRsvp(
            eventId = createdEvent.id,
            attendeeName = "Claire Smith",
            attendeeRole = "Parent",
            guestsCount = 3,
            notes = "Reserved seating"
        )
        assertFalse(vm.uiState.value.showRsvpDialog)

        val afterFirstRsvp = vm.uiState.value.events.first { it.id == createdEvent.id }
        assertEquals(3, afterFirstRsvp.totalRsvpHeadcount)
        assertEquals(1, afterFirstRsvp.remainingRsvpSlots)

        // Exceeding capacity should be blocked cleanly with errorMessage
        vm.dismissStatusMessage()
        vm.submitRsvp(
            eventId = createdEvent.id,
            attendeeName = "Late Guest",
            attendeeRole = "Parent",
            guestsCount = 2,
            notes = ""
        )
        assertTrue(vm.uiState.value.errorMessage?.contains("remaining") == true)
        assertEquals(3, vm.uiState.value.events.first { it.id == createdEvent.id }.totalRsvpHeadcount)

        // RBAC enforcement: Parent cannot create events or update status (requires EVENTS_MANAGE)
        vm.dismissStatusMessage()
        vm.openCreateEventDialog()
        assertFalse(vm.uiState.value.showCreateEventDialog)
        assertTrue(vm.uiState.value.errorMessage?.contains("Unauthorized") == true)

        vm.dismissStatusMessage()
        vm.updateEventStatus(createdEvent.id, com.example.features.events.domain.EventStatus.CANCELLED)
        assertTrue(vm.uiState.value.errorMessage?.contains("Unauthorized") == true)
        assertEquals(
            com.example.features.events.domain.EventStatus.UPCOMING,
            vm.uiState.value.events.first { it.id == createdEvent.id }.status
        )

        // Admin updates status and verifies detail dialog
        TenantContext.setTenant("sch_oxford_01", UserRole.SCHOOL_ADMIN, "usr_admin_01")
        vm.openEventDetail(afterFirstRsvp)
        assertTrue(vm.uiState.value.showEventDetailDialog)
        vm.updateEventStatus(createdEvent.id, com.example.features.events.domain.EventStatus.ONGOING)
        assertEquals(
            com.example.features.events.domain.EventStatus.ONGOING,
            vm.uiState.value.events.first { it.id == createdEvent.id }.status
        )
        vm.closeEventDetail()
        assertFalse(vm.uiState.value.showEventDetailDialog)
    }

    @Test
    fun verifySchoolDocumentFirestoreSerializationAndCredentialHelpers() {
        val certDoc = com.example.features.documents.domain.SchoolDocument(
            id = "doc_test_01",
            schoolId = "sch_oxford_01",
            documentCode = "CERT-OXF-999",
            title = "Transfer & Character Certificate",
            description = "Official verified academic transfer certificate.",
            category = com.example.features.documents.domain.DocumentCategory.CERTIFICATE,
            verificationStatus = com.example.features.documents.domain.DocumentVerificationStatus.VERIFIED,
            targetGradeOrDepartment = "Grade 10 - Section A",
            issuedToName = "Amina Rahman",
            issuedToId = "OXF-10A-001",
            fileName = "Transfer_Cert_Amina.pdf",
            fileSizeLabel = "610 KB",
            academicYear = "2026-2027",
            issuedBy = "Office of the Registrar",
            issuedDate = "2026-04-01",
            downloadCount = 5,
            isPinned = true,
            createdAt = 1711929600000L
        )

        assertTrue(certDoc.isOfficialCredential)
        assertTrue(certDoc.isActive)

        val map = certDoc.toFirestoreMap()
        val restored = com.example.features.documents.domain.SchoolDocument.fromFirestoreMap(
            id = "doc_test_01",
            schoolId = "sch_oxford_01",
            data = map
        )

        assertEquals(certDoc.documentCode, restored.documentCode)
        assertEquals(certDoc.title, restored.title)
        assertEquals(com.example.features.documents.domain.DocumentCategory.CERTIFICATE, restored.category)
        assertEquals(com.example.features.documents.domain.DocumentVerificationStatus.VERIFIED, restored.verificationStatus)
        assertEquals("Amina Rahman", restored.issuedToName)
        assertEquals("OXF-10A-001", restored.issuedToId)
        assertEquals(5, restored.downloadCount)
        assertTrue(restored.isPinned)

        val revokedSyllabus = certDoc.copy(
            category = com.example.features.documents.domain.DocumentCategory.SYLLABUS,
            verificationStatus = com.example.features.documents.domain.DocumentVerificationStatus.REVOKED
        )
        assertFalse(revokedSyllabus.isOfficialCredential)
        assertFalse(revokedSyllabus.isActive)
    }

    @Test
    fun verifyDocumentRepositoryPreSeededDocumentsAndTenantIsolation() {
        val repo = com.example.features.documents.data.DocumentRepository()

        val oxfordDocs = repo.getPreSeededDocuments("sch_oxford_01")
        assertEquals(4, oxfordDocs.size)
        assertTrue(oxfordDocs.all { it.schoolId == "sch_oxford_01" })
        assertTrue(oxfordDocs.any { it.id == "doc_oxf_01" && it.category == com.example.features.documents.domain.DocumentCategory.SYLLABUS })
        assertTrue(oxfordDocs.any { it.id == "doc_oxf_02" && it.isOfficialCredential })

        val cambridgeDocs = repo.getPreSeededDocuments("sch_cambridge_101")
        assertEquals(2, cambridgeDocs.size)
        assertTrue(cambridgeDocs.all { it.schoolId == "sch_cambridge_101" })
        assertTrue(cambridgeDocs.none { it.id.startsWith("doc_oxf") })
    }

    @Test
    fun verifyDocumentViewModelFilteringPublishingDownloadsPinningAndRbac() {
        TenantContext.setTenant("sch_oxford_01", UserRole.SCHOOL_ADMIN, "usr_admin_01")
        val vm = com.example.features.documents.presentation.DocumentViewModel()

        // Verify pre-seeded counts for Oxford
        assertEquals(4, vm.uiState.value.totalDocumentsCount)
        assertEquals(3, vm.uiState.value.verifiedDocumentsCount)
        assertEquals(2, vm.uiState.value.credentialsCount)
        assertEquals(163, vm.uiState.value.totalDownloadsCount)

        // Search query filtering
        vm.setSearchQuery("Bonafide")
        assertEquals(1, vm.uiState.value.filteredDocuments.size)
        assertEquals("doc_oxf_02", vm.uiState.value.filteredDocuments.first().id)

        // Category filtering
        vm.clearFilters()
        vm.setCategoryFilter(com.example.features.documents.domain.DocumentCategory.POLICY)
        assertEquals(1, vm.uiState.value.filteredDocuments.size)
        assertEquals("doc_oxf_04", vm.uiState.value.filteredDocuments.first().id)

        // Pinned-only filtering
        vm.clearFilters()
        vm.toggleOnlyPinnedFilter()
        assertEquals(2, vm.uiState.value.filteredDocuments.size)
        assertTrue(vm.uiState.value.filteredDocuments.all { it.isPinned })
        vm.clearFilters()

        // Publish a new document as SCHOOL_ADMIN
        vm.openCreateDocumentDialog()
        assertTrue(vm.uiState.value.showCreateDocumentDialog)
        vm.publishDocument(
            documentCode = "syl-oxf-chem10",
            title = "Grade 10 Organic Chemistry Lab Manual",
            description = "Safety guidelines and titration worksheets.",
            category = com.example.features.documents.domain.DocumentCategory.SYLLABUS,
            targetGradeOrDepartment = "Grade 10 • Science",
            issuedToName = "",
            issuedToId = "",
            fileName = "Oxford_G10_Chem_Manual.pdf",
            fileSizeLabel = "1.7 MB",
            isPinned = false
        )
        assertFalse(vm.uiState.value.showCreateDocumentDialog)
        assertEquals(5, vm.uiState.value.totalDocumentsCount)

        val createdDoc = vm.uiState.value.documents.first { it.documentCode == "SYL-OXF-CHEM10" }
        assertEquals("Grade 10 Organic Chemistry Lab Manual", createdDoc.title)
        assertEquals(0, createdDoc.downloadCount)

        // Switch to STUDENT role (has DOCUMENTS_VIEW, lacks DOCUMENTS_MANAGE)
        TenantContext.setTenant("sch_oxford_01", UserRole.STUDENT, "stu_oxf_001")
        vm.recordDownload(createdDoc.id)
        val afterDownload = vm.uiState.value.documents.first { it.id == createdDoc.id }
        assertEquals(1, afterDownload.downloadCount)
        assertEquals(164, vm.uiState.value.totalDownloadsCount)

        // RBAC blocks student from creating/modifying documents
        vm.dismissStatusMessage()
        vm.openCreateDocumentDialog()
        assertFalse(vm.uiState.value.showCreateDocumentDialog)
        assertTrue(vm.uiState.value.errorMessage?.contains("Unauthorized") == true)

        vm.dismissStatusMessage()
        vm.publishDocument(
            documentCode = "FAKE-01",
            title = "Unauthorized Doc",
            description = "",
            category = com.example.features.documents.domain.DocumentCategory.FORM,
            targetGradeOrDepartment = "",
            issuedToName = "",
            issuedToId = "",
            fileName = "fake.pdf",
            fileSizeLabel = "100 KB",
            isPinned = false
        )
        assertTrue(vm.uiState.value.errorMessage?.contains("Unauthorized") == true)
        assertEquals(5, vm.uiState.value.totalDocumentsCount)

        vm.dismissStatusMessage()
        vm.updateVerificationStatus(createdDoc.id, com.example.features.documents.domain.DocumentVerificationStatus.ARCHIVED)
        assertTrue(vm.uiState.value.errorMessage?.contains("Unauthorized") == true)

        vm.dismissStatusMessage()
        vm.togglePin(createdDoc.id)
        assertTrue(vm.uiState.value.errorMessage?.contains("Unauthorized") == true)

        // Switch back to SCHOOL_ADMIN for detail, pin toggle, and status update
        TenantContext.setTenant("sch_oxford_01", UserRole.SCHOOL_ADMIN, "usr_admin_01")
        vm.openDocumentDetail(afterDownload)
        assertTrue(vm.uiState.value.showDocumentDetailDialog)

        vm.togglePin(createdDoc.id)
        assertTrue(vm.uiState.value.documents.first { it.id == createdDoc.id }.isPinned)

        vm.updateVerificationStatus(createdDoc.id, com.example.features.documents.domain.DocumentVerificationStatus.ARCHIVED)
        assertEquals(
            com.example.features.documents.domain.DocumentVerificationStatus.ARCHIVED,
            vm.uiState.value.documents.first { it.id == createdDoc.id }.verificationStatus
        )
        vm.closeDocumentDetail()
        assertFalse(vm.uiState.value.showDocumentDetailDialog)
    }

    @Test
    fun verifyLeaveRequestFirestoreSerializationAndWorkflowHelpers() {
        val pendingLeave = com.example.features.leave.domain.LeaveRequest(
            id = "lv_test_01",
            schoolId = "sch_oxford_01",
            requestCode = "LV-OXF-555",
            applicantId = "stu_oxf_001",
            applicantName = "Amina Rahman",
            applicantType = com.example.features.leave.domain.LeaveApplicantType.STUDENT,
            departmentOrGrade = "Grade 10 - Section A",
            category = com.example.features.leave.domain.LeaveCategory.SICK,
            startDate = "2026-04-12",
            endDate = "2026-04-14",
            totalDays = 3,
            reason = "Recovered from viral fever.",
            attachmentNote = "Clinic_Note.pdf",
            status = com.example.features.leave.domain.LeaveStatus.PENDING,
            submittedDate = "2026-04-11",
            createdAt = 1712793600000L
        )

        assertTrue(pendingLeave.isPending)
        assertTrue(pendingLeave.canWithdraw)
        assertFalse(pendingLeave.isApproved)

        val map = pendingLeave.toFirestoreMap()
        val restored = com.example.features.leave.domain.LeaveRequest.fromFirestoreMap(
            id = "lv_test_01",
            schoolId = "sch_oxford_01",
            data = map
        )

        assertEquals("LV-OXF-555", restored.requestCode)
        assertEquals("Amina Rahman", restored.applicantName)
        assertEquals(com.example.features.leave.domain.LeaveApplicantType.STUDENT, restored.applicantType)
        assertEquals(com.example.features.leave.domain.LeaveCategory.SICK, restored.category)
        assertEquals(com.example.features.leave.domain.LeaveStatus.PENDING, restored.status)
        assertEquals(3, restored.totalDays)
        assertEquals("Clinic_Note.pdf", restored.attachmentNote)

        val approved = restored.copy(
            status = com.example.features.leave.domain.LeaveStatus.APPROVED,
            reviewerName = "Dr. Robert Vance"
        )
        assertTrue(approved.isApproved)
        assertFalse(approved.isPending)
        assertFalse(approved.canWithdraw)
    }

    @Test
    fun verifyLeaveRepositoryPreSeededRecordsAndTenantIsolation() {
        val repo = com.example.features.leave.data.LeaveRepository()

        val oxfordLeaves = repo.getPreSeededLeaveRequests("sch_oxford_01")
        assertEquals(4, oxfordLeaves.size)
        assertTrue(oxfordLeaves.all { it.schoolId == "sch_oxford_01" })
        assertTrue(oxfordLeaves.any { it.id == "lv_oxf_01" && it.isPending })
        assertTrue(oxfordLeaves.any { it.id == "lv_oxf_02" && it.isApproved })

        val cambridgeLeaves = repo.getPreSeededLeaveRequests("sch_cambridge_101")
        assertEquals(2, cambridgeLeaves.size)
        assertTrue(cambridgeLeaves.all { it.schoolId == "sch_cambridge_101" })
        assertTrue(cambridgeLeaves.none { it.id.startsWith("lv_oxf") })

        assertEquals("schools/sch_oxford_01/leaves", com.example.core.tenant.TenantPathProvider.leavesPath("sch_oxford_01"))
    }

    @Test
    fun verifyLeaveViewModelSubmissionApprovalWithdrawalFilteringAndRbac() {
        TenantContext.setTenant("sch_oxford_01", UserRole.SCHOOL_ADMIN, "usr_admin_01")
        val vm = com.example.features.leave.presentation.LeaveViewModel()

        // Verify pre-seeded counts for Oxford
        assertEquals(4, vm.uiState.value.totalCount)
        assertEquals(2, vm.uiState.value.pendingCount)
        assertEquals(1, vm.uiState.value.approvedCount)
        assertEquals(3, vm.uiState.value.totalApprovedDays)

        // Search and filter checks
        vm.setSearchQuery("Symposium")
        assertEquals(1, vm.uiState.value.filteredLeaveRequests.size)
        assertEquals("lv_oxf_02", vm.uiState.value.filteredLeaveRequests.first().id)

        vm.clearFilters()
        vm.setStatusFilter(com.example.features.leave.domain.LeaveStatus.PENDING)
        assertEquals(2, vm.uiState.value.filteredLeaveRequests.size)

        vm.clearFilters()
        vm.setApplicantTypeFilter(com.example.features.leave.domain.LeaveApplicantType.STAFF)
        assertEquals(1, vm.uiState.value.filteredLeaveRequests.size)
        assertEquals("lv_oxf_04", vm.uiState.value.filteredLeaveRequests.first().id)
        vm.clearFilters()

        // Switch to STUDENT role (has LEAVES_APPLY, lacks LEAVES_APPROVE)
        TenantContext.setTenant("sch_oxford_01", UserRole.STUDENT, "stu_oxf_001")
        vm.openApplyLeaveDialog()
        assertTrue(vm.uiState.value.showApplyLeaveDialog)
        vm.submitLeaveRequest(
            applicantName = "Amina Rahman",
            applicantType = com.example.features.leave.domain.LeaveApplicantType.STUDENT,
            departmentOrGrade = "Grade 10 - Section A",
            category = com.example.features.leave.domain.LeaveCategory.ACADEMIC,
            startDate = "2026-05-04",
            endDate = "2026-05-05",
            totalDays = 2,
            reason = "Attending Regional Science Olympiad Finals.",
            attachmentNote = "Olympiad_Admit_Card.pdf"
        )
        assertFalse(vm.uiState.value.showApplyLeaveDialog)
        assertEquals(5, vm.uiState.value.totalCount)
        assertEquals(3, vm.uiState.value.pendingCount)

        val submittedLeave = vm.uiState.value.leaveRequests.first {
            it.reason.contains("Science Olympiad Finals")
        }

        // RBAC: Student cannot approve/reject or open review dialog
        vm.dismissStatusMessage()
        vm.openReviewDialog(submittedLeave)
        assertFalse(vm.uiState.value.showReviewDialog)
        assertTrue(vm.uiState.value.errorMessage?.contains("Unauthorized") == true)

        vm.dismissStatusMessage()
        vm.approveLeaveRequest(submittedLeave.id, "Self-approved")
        assertTrue(vm.uiState.value.errorMessage?.contains("Unauthorized") == true)
        assertEquals(com.example.features.leave.domain.LeaveStatus.PENDING, vm.uiState.value.leaveRequests.first { it.id == submittedLeave.id }.status)

        // Student can withdraw their own pending leave, or let's submit a second one to test both withdrawal and approval
        vm.submitLeaveRequest(
            applicantName = "Amina Rahman",
            applicantType = com.example.features.leave.domain.LeaveApplicantType.STUDENT,
            departmentOrGrade = "Grade 10 - Section A",
            category = com.example.features.leave.domain.LeaveCategory.CASUAL,
            startDate = "2026-05-10",
            endDate = "2026-05-10",
            totalDays = 1,
            reason = "Personal dental checkup."
        )
        val withdrawableLeave = vm.uiState.value.leaveRequests.first { it.reason == "Personal dental checkup." }
        vm.withdrawLeaveRequest(withdrawableLeave.id)
        assertEquals(
            com.example.features.leave.domain.LeaveStatus.CANCELLED,
            vm.uiState.value.leaveRequests.first { it.id == withdrawableLeave.id }.status
        )

        // Switch to SCHOOL_ADMIN to approve submittedLeave and verify rejection requires remarks
        TenantContext.setTenant("sch_oxford_01", UserRole.SCHOOL_ADMIN, "usr_admin_01")
        vm.dismissStatusMessage()
        vm.rejectLeaveRequest("lv_oxf_03", "")
        assertTrue(vm.uiState.value.errorMessage?.contains("reason") == true)

        vm.openReviewDialog(submittedLeave)
        assertTrue(vm.uiState.value.showReviewDialog)
        vm.approveLeaveRequest(submittedLeave.id, "Approved for Olympiad participation.")
        assertFalse(vm.uiState.value.showReviewDialog)

        val approvedOlympiad = vm.uiState.value.leaveRequests.first { it.id == submittedLeave.id }
        assertEquals(com.example.features.leave.domain.LeaveStatus.APPROVED, approvedOlympiad.status)
        assertEquals(5, vm.uiState.value.totalApprovedDays)

        // Detail dialog lifecycle
        vm.openDetailDialog(approvedOlympiad)
        assertTrue(vm.uiState.value.showDetailDialog)
        vm.closeDetailDialog()
        assertFalse(vm.uiState.value.showDetailDialog)
    }

    @Test
    fun verifyMessageThreadFirestoreSerializationAndHelpers() {
        val thread = com.example.features.messages.domain.MessageThread(
            id = "msg_test_01",
            schoolId = "sch_oxford_01",
            threadCode = "MSG-OXF-888",
            subject = "Lab Coat Safety Protocol",
            channelType = com.example.features.messages.domain.MessageChannelType.CLASS_CHANNEL,
            priority = com.example.features.messages.domain.MessagePriority.IMPORTANT,
            participantsLabel = "Dr. Sarah Jenkins • Grade 10 Science",
            participantRoles = listOf("TEACHER", "STUDENT"),
            messages = listOf(
                com.example.features.messages.domain.ChatMessageEntry(
                    id = "m_1",
                    senderId = "tch_01",
                    senderName = "Dr. Sarah Jenkins",
                    senderRole = "Faculty",
                    body = "Please bring goggles to tomorrow's optics lab.",
                    sentAtLabel = "Today, 08:30 AM",
                    timestamp = 1712647800000L
                )
            ),
            unreadCount = 1,
            isPinned = true,
            isArchived = false,
            lastActivityLabel = "Today, 08:30 AM",
            createdAt = 1712647800000L,
            updatedAt = 1712647800000L
        )

        assertTrue(thread.hasUnread)
        assertEquals(1, thread.messageCount)
        assertEquals("Please bring goggles to tomorrow's optics lab.", thread.latestMessagePreview)

        val map = thread.toFirestoreMap()
        val restored = com.example.features.messages.domain.MessageThread.fromFirestoreMap(
            id = "msg_test_01",
            schoolId = "sch_oxford_01",
            data = map
        )

        assertEquals("MSG-OXF-888", restored.threadCode)
        assertEquals("Lab Coat Safety Protocol", restored.subject)
        assertEquals(com.example.features.messages.domain.MessageChannelType.CLASS_CHANNEL, restored.channelType)
        assertEquals(com.example.features.messages.domain.MessagePriority.IMPORTANT, restored.priority)
        assertEquals(1, restored.messages.size)
        assertEquals("Dr. Sarah Jenkins", restored.messages.first().senderName)
        assertTrue(restored.isPinned)
        assertFalse(restored.isArchived)
    }

    @Test
    fun verifyMessageRepositoryPreSeededThreadsAndTenantIsolation() {
        val repo = com.example.features.messages.data.MessageRepository()

        val oxfordThreads = repo.getPreSeededThreads("sch_oxford_01")
        assertEquals(4, oxfordThreads.size)
        assertTrue(oxfordThreads.all { it.schoolId == "sch_oxford_01" })
        assertTrue(oxfordThreads.any { it.id == "msg_oxf_01" && it.hasUnread })

        val cambridgeThreads = repo.getPreSeededThreads("sch_cambridge_101")
        assertEquals(2, cambridgeThreads.size)
        assertTrue(cambridgeThreads.all { it.schoolId == "sch_cambridge_101" })
        assertTrue(cambridgeThreads.none { it.id.startsWith("msg_oxf") })

        assertEquals("schools/sch_oxford_01/messages", com.example.core.tenant.TenantPathProvider.messagesPath("sch_oxford_01"))
    }

    @Test
    fun verifyMessageViewModelFilteringComposeReplyModerationAndRbac() {
        TenantContext.setTenant("sch_oxford_01", UserRole.SCHOOL_ADMIN, "usr_admin_01")
        val vm = com.example.features.messages.presentation.MessageViewModel()

        // Verify pre-seeded counts for Oxford
        assertEquals(4, vm.uiState.value.totalThreadsCount)
        assertEquals(3, vm.uiState.value.unreadMessagesTotal)
        assertEquals(2, vm.uiState.value.pinnedCount)
        assertEquals(7, vm.uiState.value.totalMessagesCount)

        // Filtering tests
        vm.setSearchQuery("Detour")
        assertEquals(1, vm.uiState.value.filteredThreads.size)
        assertEquals("msg_oxf_02", vm.uiState.value.filteredThreads.first().id)

        vm.clearFilters()
        vm.toggleOnlyUnreadFilter()
        assertEquals(2, vm.uiState.value.filteredThreads.size)

        vm.clearFilters()
        vm.setChannelFilter(com.example.features.messages.domain.MessageChannelType.FACULTY_GROUP)
        assertEquals(1, vm.uiState.value.filteredThreads.size)
        assertEquals("msg_oxf_03", vm.uiState.value.filteredThreads.first().id)
        vm.clearFilters()

        // Opening an unread thread marks it read
        val unreadThread = vm.uiState.value.threads.first { it.id == "msg_oxf_01" }
        vm.openThread(unreadThread)
        assertTrue(vm.uiState.value.showThreadDialog)
        assertEquals(0, vm.uiState.value.threads.first { it.id == "msg_oxf_01" }.unreadCount)
        assertEquals(1, vm.uiState.value.unreadMessagesTotal)
        vm.closeThread()
        assertFalse(vm.uiState.value.showThreadDialog)

        // Switch to TEACHER role (has MESSAGES_SEND, lacks MESSAGES_MODERATE)
        TenantContext.setTenant("sch_oxford_01", UserRole.TEACHER, "tch_oxf_001")
        vm.openComposeDialog()
        assertTrue(vm.uiState.value.showComposeDialog)
        vm.createThread(
            subject = "Chemistry Lab Coat Inspection",
            channelType = com.example.features.messages.domain.MessageChannelType.CLASS_CHANNEL,
            priority = com.example.features.messages.domain.MessagePriority.NORMAL,
            participantsLabel = "Grade 10 - Section A",
            initialMessageBody = "All students must bring lab coats on Wednesday."
        )
        assertFalse(vm.uiState.value.showComposeDialog)
        assertEquals(5, vm.uiState.value.totalThreadsCount)

        val newThread = vm.uiState.value.threads.first { it.subject == "Chemistry Lab Coat Inspection" }
        assertEquals(1, newThread.messageCount)

        // Student replies to the thread
        TenantContext.setTenant("sch_oxford_01", UserRole.STUDENT, "stu_oxf_001")
        vm.openThread(newThread)
        vm.sendReply(newThread.id, "Understood, I will bring mine.")
        assertEquals(2, vm.uiState.value.threads.first { it.id == newThread.id }.messageCount)
        assertEquals("Understood, I will bring mine.", vm.uiState.value.threads.first { it.id == newThread.id }.latestMessagePreview)

        // RBAC blocks Student from moderating (pinning, archiving, deleting)
        vm.dismissStatusMessage()
        vm.togglePin(newThread.id)
        assertTrue(vm.uiState.value.errorMessage?.contains("Unauthorized") == true)
        assertFalse(vm.uiState.value.threads.first { it.id == newThread.id }.isPinned)

        vm.dismissStatusMessage()
        vm.toggleArchive(newThread.id)
        assertTrue(vm.uiState.value.errorMessage?.contains("Unauthorized") == true)
        assertFalse(vm.uiState.value.threads.first { it.id == newThread.id }.isArchived)

        // Switch back to SCHOOL_ADMIN to moderate (pin and archive)
        TenantContext.setTenant("sch_oxford_01", UserRole.SCHOOL_ADMIN, "usr_admin_01")
        vm.togglePin(newThread.id)
        assertTrue(vm.uiState.value.threads.first { it.id == newThread.id }.isPinned)

        vm.toggleArchive(newThread.id)
        assertTrue(vm.uiState.value.threads.first { it.id == newThread.id }.isArchived)
        assertEquals(4, vm.uiState.value.totalThreadsCount)
        assertFalse(vm.uiState.value.filteredThreads.any { it.id == newThread.id })

        vm.toggleIncludeArchived()
        assertTrue(vm.uiState.value.filteredThreads.any { it.id == newThread.id })
        vm.closeThread()
    }

    @Test
    fun verifyExecutiveReportSnapshotFirestoreSerializationAndHelpers() {
        val snapshot = com.example.features.analytics.domain.ExecutiveReportSnapshot(
            id = "rpt_test_01",
            schoolId = "sch_oxford_01",
            reportCode = "RPT-OXF-999",
            title = "Q2 Bursar Fee Audit",
            domain = com.example.features.analytics.domain.AnalyticsDomain.FINANCE,
            period = com.example.features.analytics.domain.ReportPeriod.MONTHLY,
            generatedBy = "Marcus Vance (Finance)",
            generatedDate = "2026-04-10",
            headlineKpi = "$29,575 Collected • $7,837 Pending",
            summaryNotes = "Monthly collection reconciliation report.",
            exportFormat = "CSV",
            createdAt = 1712707200000L
        )

        assertTrue(snapshot.isCsvExport)
        val map = snapshot.toFirestoreMap()
        val restored = com.example.features.analytics.domain.ExecutiveReportSnapshot.fromFirestoreMap(
            id = "rpt_test_01",
            schoolId = "sch_oxford_01",
            data = map
        )

        assertEquals("RPT-OXF-999", restored.reportCode)
        assertEquals("Q2 Bursar Fee Audit", restored.title)
        assertEquals(com.example.features.analytics.domain.AnalyticsDomain.FINANCE, restored.domain)
        assertEquals(com.example.features.analytics.domain.ReportPeriod.MONTHLY, restored.period)
        assertEquals("Marcus Vance (Finance)", restored.generatedBy)
        assertEquals("CSV", restored.exportFormat)
        assertTrue(restored.isCsvExport)

        val pdfSnapshot = snapshot.copy(exportFormat = "PDF")
        assertFalse(pdfSnapshot.isCsvExport)
    }

    @Test
    fun verifyAnalyticsRepositoryTenantMetricsCsvAndReportsIsolation() {
        val repo = com.example.features.analytics.data.AnalyticsRepository()

        TenantContext.setTenant("sch_oxford_01", UserRole.SCHOOL_ADMIN, "usr_admin_01")
        val oxfordBundle = repo.getAnalyticsBundle(
            schoolId = "sch_oxford_01",
            period = com.example.features.analytics.domain.ReportPeriod.TERM_1
        )
        assertEquals("sch_oxford_01", oxfordBundle.schoolId)
        assertEquals(1480, oxfordBundle.totalStudents)
        assertEquals(3.68f, oxfordBundle.averageGpa, 0.01f)
        assertEquals(5, oxfordBundle.attendanceTrend.size)
        assertEquals(4, oxfordBundle.revenueBreakdown.size)
        assertEquals(5, oxfordBundle.gpaDistribution.size)
        assertEquals(4, oxfordBundle.departmentMetrics.size)

        // Cross-tenant boundary enforcement
        var securityViolationThrown = false
        try {
            repo.getAnalyticsBundle(
                schoolId = "sch_cambridge_101",
                period = com.example.features.analytics.domain.ReportPeriod.TERM_1
            )
        } catch (_: AppError.SecurityViolation) {
            securityViolationThrown = true
        }
        assertTrue(securityViolationThrown)

        TenantContext.setTenant("sch_cambridge_101", UserRole.SCHOOL_ADMIN, "usr_cam_admin")
        val cambridgeBundle = repo.getAnalyticsBundle(
            schoolId = "sch_cambridge_101",
            period = com.example.features.analytics.domain.ReportPeriod.TERM_1
        )
        assertEquals("sch_cambridge_101", cambridgeBundle.schoolId)
        assertEquals(920, cambridgeBundle.totalStudents)
        assertEquals(3.54f, cambridgeBundle.averageGpa, 0.01f)
        assertEquals(5, cambridgeBundle.attendanceTrend.size)
        assertEquals(4, cambridgeBundle.revenueBreakdown.size)
        assertEquals(5, cambridgeBundle.gpaDistribution.size)
        assertEquals(4, cambridgeBundle.departmentMetrics.size)

        val oxfordReports = repo.getPreSeededReports("sch_oxford_01")
        assertEquals(3, oxfordReports.size)
        assertTrue(oxfordReports.all { it.schoolId == "sch_oxford_01" })

        val cambridgeReports = repo.getPreSeededReports("sch_cambridge_101")
        assertEquals(2, cambridgeReports.size)
        assertTrue(cambridgeReports.all { it.schoolId == "sch_cambridge_101" })
        assertTrue(cambridgeReports.none { it.id.startsWith("rpt_oxf") })

        assertEquals(
            "schools/sch_oxford_01/reports",
            com.example.core.tenant.TenantPathProvider.reportsPath("sch_oxford_01")
        )

        val csv = repo.buildCsvExportPayload(oxfordBundle, oxfordReports.first())
        assertTrue(csv.contains("ReportCode"))
        assertTrue(csv.contains("sch_oxford_01"))
        assertTrue(csv.contains("TotalStudents"))
    }

    @Test
    fun verifyAnalyticsViewModelPeriodDomainReportGenerationCsvAndRbac() {
        TenantContext.setTenant("sch_oxford_01", UserRole.SCHOOL_ADMIN, "usr_admin_01")
        val vm = com.example.features.analytics.presentation.AnalyticsViewModel()

        // Verify pre-seeded state for Oxford
        assertEquals(3, vm.uiState.value.totalReportsCount)
        assertNotNull(vm.uiState.value.bundle)
        assertEquals(94.2f, vm.uiState.value.bundle!!.averageAttendancePct, 0.01f)
        val termCollected = vm.uiState.value.bundle!!.collectedRevenueAmount

        // Period switch updates revenue scale (MONTHLY = 0.35f)
        vm.selectPeriod(com.example.features.analytics.domain.ReportPeriod.MONTHLY)
        val monthlyCollected = vm.uiState.value.bundle!!.collectedRevenueAmount
        assertEquals(termCollected * 0.35, monthlyCollected, 1.0)

        // Domain filtering shows matching domain + OVERVIEW reports
        vm.selectDomain(com.example.features.analytics.domain.AnalyticsDomain.FINANCE)
        assertTrue(vm.uiState.value.filteredReports.all {
            it.domain == com.example.features.analytics.domain.AnalyticsDomain.FINANCE ||
                it.domain == com.example.features.analytics.domain.AnalyticsDomain.OVERVIEW
        })
        assertEquals(2, vm.uiState.value.filteredReports.size)

        // Report preview opens and generates CSV payload
        val firstReport = vm.uiState.value.reports.first()
        vm.openReportPreview(firstReport)
        assertTrue(vm.uiState.value.showReportPreviewDialog)
        assertTrue(vm.uiState.value.exportedCsvPreview.contains(firstReport.reportCode))
        vm.closeReportPreview()
        assertFalse(vm.uiState.value.showReportPreviewDialog)

        // Switch to TEACHER role (has ANALYTICS_VIEW, lacks ANALYTICS_EXPORT)
        TenantContext.setTenant("sch_oxford_01", UserRole.TEACHER, "tch_oxf_001")
        vm.dismissStatusMessage()
        vm.openGenerateReportDialog()
        assertFalse(vm.uiState.value.showGenerateReportDialog)
        assertTrue(vm.uiState.value.errorMessage?.contains("Unauthorized") == true)

        vm.dismissStatusMessage()
        vm.generateReport(
            title = "Unauthorized Teacher Export",
            domain = com.example.features.analytics.domain.AnalyticsDomain.ACADEMICS,
            period = com.example.features.analytics.domain.ReportPeriod.TERM_1,
            exportFormat = "CSV",
            summaryNotes = "Should be blocked"
        )
        assertTrue(vm.uiState.value.errorMessage?.contains("Unauthorized") == true)
        assertEquals(3, vm.uiState.value.totalReportsCount)

        vm.dismissStatusMessage()
        vm.deleteReport(firstReport.id)
        assertTrue(vm.uiState.value.errorMessage?.contains("Unauthorized") == true)
        assertEquals(3, vm.uiState.value.totalReportsCount)

        // Switch to ACCOUNTANT role (has ANALYTICS_EXPORT)
        TenantContext.setTenant("sch_oxford_01", UserRole.ACCOUNTANT, "acc_oxf_001")
        vm.dismissStatusMessage()
        vm.openGenerateReportDialog()
        assertTrue(vm.uiState.value.showGenerateReportDialog)
        vm.generateReport(
            title = "Bursar Term Audit",
            domain = com.example.features.analytics.domain.AnalyticsDomain.FINANCE,
            period = com.example.features.analytics.domain.ReportPeriod.TERM_1,
            exportFormat = "CSV",
            summaryNotes = "Quarterly check"
        )
        assertFalse(vm.uiState.value.showGenerateReportDialog)
        assertEquals(4, vm.uiState.value.totalReportsCount)

        val generated = vm.uiState.value.reports.first { it.title == "Bursar Term Audit" }
        assertEquals("CSV", generated.exportFormat)
        assertTrue(generated.isCsvExport)

        vm.deleteReport(generated.id)
        assertEquals(3, vm.uiState.value.totalReportsCount)
    }

    @Test
    fun verifySchoolBrandingConfigFirestoreSerializationAndHelpers() {
        val defaultConfig = com.example.features.branding.domain.SchoolBrandingConfig(
            id = "brand_profile",
            schoolId = "sch_oxford_01",
            schoolDisplayName = "Oxford International Academy",
            schoolCode = "OXF-101",
            motto = "Knowledge & Integrity",
            palettePreset = com.example.features.branding.domain.BrandPalettePreset.OXFORD_NAVY,
            cornerStyle = com.example.features.branding.domain.UiCornerStyle.BALANCED
        )
        assertTrue(defaultConfig.isDefaultTheme)

        val customConfig = defaultConfig.copy(
            palettePreset = com.example.features.branding.domain.BrandPalettePreset.EMERALD_STEM,
            crestEmblem = com.example.features.branding.domain.CrestEmblemStyle.COMPASS_GLOBAL,
            cornerStyle = com.example.features.branding.domain.UiCornerStyle.ROUNDED,
            layoutDensity = com.example.features.branding.domain.DashboardLayoutDensity.EXECUTIVE,
            reportHeaderStyle = com.example.features.branding.domain.ReportCardHeaderStyle.MODERN_BANNER,
            currencyCode = "EUR",
            currencySymbol = "€",
            darkModeDefault = true
        )
        assertFalse(customConfig.isDefaultTheme)
        assertTrue(customConfig.formattedBrandSummary.contains("Emerald STEM Academy"))

        val map = customConfig.toFirestoreMap()
        val restored = com.example.features.branding.domain.SchoolBrandingConfig.fromFirestoreMap(
            id = "brand_profile",
            schoolId = "sch_oxford_01",
            data = map
        )

        assertEquals("Oxford International Academy", restored.schoolDisplayName)
        assertEquals("OXF-101", restored.schoolCode)
        assertEquals(com.example.features.branding.domain.BrandPalettePreset.EMERALD_STEM, restored.palettePreset)
        assertEquals(com.example.features.branding.domain.CrestEmblemStyle.COMPASS_GLOBAL, restored.crestEmblem)
        assertEquals(com.example.features.branding.domain.UiCornerStyle.ROUNDED, restored.cornerStyle)
        assertEquals(com.example.features.branding.domain.DashboardLayoutDensity.EXECUTIVE, restored.layoutDensity)
        assertEquals(com.example.features.branding.domain.ReportCardHeaderStyle.MODERN_BANNER, restored.reportHeaderStyle)
        assertEquals("EUR", restored.currencyCode)
        assertEquals("€", restored.currencySymbol)
        assertTrue(restored.darkModeDefault)
    }

    @Test
    fun verifyBrandingRepositoryTenantDefaultsAndBoundaryIsolation() {
        val repo = com.example.features.branding.data.BrandingRepository()

        TenantContext.setTenant("sch_oxford_01", UserRole.SCHOOL_ADMIN, "usr_admin_01")
        val oxfordDefault = repo.getDefaultBrandingForSchool("sch_oxford_01")
        assertEquals("OXF-101", oxfordDefault.schoolCode)
        assertEquals(com.example.features.branding.domain.BrandPalettePreset.OXFORD_NAVY, oxfordDefault.palettePreset)

        var securityViolationThrown = false
        try {
            repo.getDefaultBrandingForSchool("sch_cambridge_101")
        } catch (_: AppError.SecurityViolation) {
            securityViolationThrown = true
        }
        assertTrue(securityViolationThrown)

        TenantContext.setTenant("sch_cambridge_101", UserRole.SCHOOL_ADMIN, "usr_cam_admin")
        val cambridgeDefault = repo.getDefaultBrandingForSchool("sch_cambridge_101")
        assertEquals("CAM-204", cambridgeDefault.schoolCode)
        assertEquals(com.example.features.branding.domain.BrandPalettePreset.CAMBRIDGE_CRIMSON, cambridgeDefault.palettePreset)

        assertEquals(
            "schools/sch_oxford_01/branding",
            com.example.core.tenant.TenantPathProvider.brandingPath("sch_oxford_01")
        )
    }

    @Test
    fun verifyBrandingViewModelDraftUpdatesLiveThemeControllerValidationAndRbac() {
        com.example.core.theme.BrandThemeController.resetToDefault()
        TenantContext.setTenant("sch_oxford_01", UserRole.SCHOOL_ADMIN, "usr_admin_01")
        val vm = com.example.features.branding.presentation.BrandingViewModel()

        assertNotNull(vm.uiState.value.activeConfig)
        assertFalse(vm.uiState.value.hasUnsavedChanges)
        assertEquals("OXF-101", vm.uiState.value.activeConfig?.schoolCode)

        // Update identity & palette draft fields
        vm.selectTab(com.example.features.branding.presentation.BrandingTab.THEME_PALETTE)
        vm.selectPalettePreset(com.example.features.branding.domain.BrandPalettePreset.EMERALD_STEM)
        vm.selectCrestEmblem(com.example.features.branding.domain.CrestEmblemStyle.COMPASS_GLOBAL)
        vm.selectCornerStyle(com.example.features.branding.domain.UiCornerStyle.ROUNDED)
        vm.updateIdentityFields(
            schoolDisplayName = "Oxford Global STEM Academy",
            motto = "Innovate & Inspire"
        )
        assertTrue(vm.uiState.value.hasUnsavedChanges)

        // Validation blocks blank display name
        vm.updateIdentityFields(schoolDisplayName = "")
        vm.saveBrandingChanges()
        assertTrue(vm.uiState.value.errorMessage?.contains("empty") == true)

        // Restore valid name and save -> updates live BrandThemeController
        vm.dismissStatusMessage()
        vm.updateIdentityFields(schoolDisplayName = "Oxford Global STEM Academy")
        vm.saveBrandingChanges()
        assertFalse(vm.uiState.value.hasUnsavedChanges)
        assertEquals("Oxford Global STEM Academy", vm.uiState.value.activeConfig?.schoolDisplayName)
        assertEquals(
            com.example.features.branding.domain.BrandPalettePreset.EMERALD_STEM,
            com.example.core.theme.BrandThemeController.overrideState.value?.palettePreset
        )
        assertEquals(
            com.example.features.branding.domain.UiCornerStyle.ROUNDED,
            com.example.core.theme.BrandThemeController.overrideState.value?.cornerStyle
        )

        // Preview dialog lifecycle
        vm.openLiveCardPreview()
        assertTrue(vm.uiState.value.showLiveCardPreviewDialog)
        vm.closeLiveCardPreview()
        assertFalse(vm.uiState.value.showLiveCardPreviewDialog)

        // RBAC blocks Teacher from editing or resetting branding
        TenantContext.setTenant("sch_oxford_01", UserRole.TEACHER, "tch_oxf_001")
        vm.dismissStatusMessage()
        vm.selectPalettePreset(com.example.features.branding.domain.BrandPalettePreset.ROYAL_SCHOLAR)
        vm.saveBrandingChanges()
        assertTrue(vm.uiState.value.errorMessage?.contains("Unauthorized") == true)

        vm.dismissStatusMessage()
        vm.openResetConfirmDialog()
        assertFalse(vm.uiState.value.showResetConfirmDialog)
        assertTrue(vm.uiState.value.errorMessage?.contains("Unauthorized") == true)

        // School Admin resets branding back to defaults
        TenantContext.setTenant("sch_oxford_01", UserRole.SCHOOL_ADMIN, "usr_admin_01")
        vm.dismissStatusMessage()
        vm.openResetConfirmDialog()
        assertTrue(vm.uiState.value.showResetConfirmDialog)
        vm.confirmResetToDefault()
        assertFalse(vm.uiState.value.showResetConfirmDialog)
        assertEquals(
            com.example.features.branding.domain.BrandPalettePreset.OXFORD_NAVY,
            vm.uiState.value.activeConfig?.palettePreset
        )
        com.example.core.theme.BrandThemeController.resetToDefault()
    }
}
