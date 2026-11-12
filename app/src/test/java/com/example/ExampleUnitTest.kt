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

        // Submit student work
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

        // Grade submission
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
}






