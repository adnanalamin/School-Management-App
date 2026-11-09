package com.example.shared.models

/**
 * TenantSchool represents an isolated educational institution in the EduManage SaaS platform.
 */
data class TenantSchool(
    val id: String,
    val name: String,
    val code: String,
    val logoUrl: String? = null,
    val slogan: String? = null,
    val address: String = "",
    val phone: String = "",
    val email: String = "",
    val website: String = "",
    val establishedYear: Int = 2000,
    val schoolType: String = "K-12", // e.g., Primary, High School, College, Madrasah
    val academicSession: String = "2026-2027",
    val currency: String = "USD",
    val timezone: String = "UTC",
    val primaryColorHex: String? = "#1E3A8A",
    val secondaryColorHex: String? = "#0F766E",
    val status: SchoolStatus = SchoolStatus.ACTIVE,
    val subscriptionPlan: String = "PRO",
    val createdAt: Long = System.currentTimeMillis()
)

enum class SchoolStatus {
    PENDING_APPROVAL,
    ACTIVE,
    SUSPENDED,
    ARCHIVED
}
