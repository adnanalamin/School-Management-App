package com.example.features.foundation

import com.example.core.config.AppConfig
import com.example.core.security.Permission
import com.example.core.security.UserRole
import com.example.shared.models.SchoolStatus
import com.example.shared.models.TenantSchool

data class FoundationUiState(
    val appConfig: AppConfig = AppConfig.current,
    val availableSchools: List<TenantSchool> = listOf(
        TenantSchool(
            id = "sch_oxford_01",
            name = "Oxford International Academy",
            code = "OXF-101",
            schoolType = "K-12",
            academicSession = "2026-2027",
            status = SchoolStatus.ACTIVE,
            currency = "USD",
            timezone = "America/New_York",
            subscriptionPlan = "ENTERPRISE"
        ),
        TenantSchool(
            id = "sch_stmary_02",
            name = "St. Mary High School",
            code = "SMH-202",
            schoolType = "Secondary",
            academicSession = "2026-2027",
            status = SchoolStatus.ACTIVE,
            currency = "GBP",
            timezone = "Europe/London",
            subscriptionPlan = "PRO"
        ),
        TenantSchool(
            id = "sch_dhaka_03",
            name = "Dhaka Model Collegiate School",
            code = "DMC-303",
            schoolType = "K-12",
            academicSession = "2026",
            status = SchoolStatus.ACTIVE,
            currency = "BDT",
            timezone = "Asia/Dhaka",
            subscriptionPlan = "BASIC"
        )
    ),
    val selectedSchoolId: String = "sch_oxford_01",
    val selectedRole: UserRole = UserRole.SCHOOL_ADMIN,
    val isDarkMode: Boolean = false,
    val testInputText: String = "",
    val testInputError: String? = null,
    val activeTab: Int = 0 // 0: Overview/Architecture, 1: RBAC Matrix, 2: Design System, 3: Roadmap
) {
    val currentSchool: TenantSchool?
        get() = availableSchools.firstOrNull { it.id == selectedSchoolId }

    val grantedPermissions: Set<Permission>
        get() = Permission.getPermissionsForRole(selectedRole)
}
