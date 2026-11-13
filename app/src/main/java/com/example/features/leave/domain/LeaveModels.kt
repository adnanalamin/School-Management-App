package com.example.features.leave.domain

import com.example.core.tenant.TenantScopedEntity

/**
 * Identifies whether the leave application is for a student, teaching faculty, or non-teaching staff.
 */
enum class LeaveApplicantType(val displayName: String) {
    STUDENT("Student"),
    FACULTY("Faculty"),
    STAFF("Staff");

    companion object {
        fun fromString(value: String?): LeaveApplicantType {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: STUDENT
        }
    }
}

/**
 * Classification of leave request reason.
 */
enum class LeaveCategory(val displayName: String) {
    SICK("Medical / Sick"),
    CASUAL("Personal / Family"),
    EMERGENCY("Emergency"),
    ACADEMIC("Academic / Duty"),
    PARENTAL("Parental Leave");

    companion object {
        fun fromString(value: String?): LeaveCategory {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: CASUAL
        }
    }
}

/**
 * Approval workflow status for a leave request.
 */
enum class LeaveStatus(val displayName: String) {
    PENDING("Pending Review"),
    APPROVED("Approved"),
    REJECTED("Declined"),
    CANCELLED("Withdrawn");

    companion object {
        fun fromString(value: String?): LeaveStatus {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: PENDING
        }
    }
}

/**
 * Multi-tenant Leave Request and Approval Workflow record.
 * Scoped to `schools/{schoolId}/leaves/{leaveId}`.
 */
data class LeaveRequest(
    override val id: String,
    override val schoolId: String,
    val requestCode: String,
    val applicantId: String,
    val applicantName: String,
    val applicantType: LeaveApplicantType = LeaveApplicantType.STUDENT,
    val departmentOrGrade: String,
    val category: LeaveCategory = LeaveCategory.CASUAL,
    val startDate: String,
    val endDate: String,
    val totalDays: Int = 1,
    val reason: String,
    val attachmentNote: String = "",
    val status: LeaveStatus = LeaveStatus.PENDING,
    val reviewerName: String = "",
    val reviewRemarks: String = "",
    val submittedDate: String,
    val reviewedDate: String = "",
    val createdAt: Long = System.currentTimeMillis()
) : TenantScopedEntity {

    val isPending: Boolean
        get() = status == LeaveStatus.PENDING

    val isApproved: Boolean
        get() = status == LeaveStatus.APPROVED

    val canWithdraw: Boolean
        get() = status == LeaveStatus.PENDING

    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "id" to id,
        "schoolId" to schoolId,
        "requestCode" to requestCode,
        "applicantId" to applicantId,
        "applicantName" to applicantName,
        "applicantType" to applicantType.name,
        "departmentOrGrade" to departmentOrGrade,
        "category" to category.name,
        "startDate" to startDate,
        "endDate" to endDate,
        "totalDays" to totalDays,
        "reason" to reason,
        "attachmentNote" to attachmentNote,
        "status" to status.name,
        "reviewerName" to reviewerName,
        "reviewRemarks" to reviewRemarks,
        "submittedDate" to submittedDate,
        "reviewedDate" to reviewedDate,
        "createdAt" to createdAt
    )

    companion object {
        fun fromFirestoreMap(id: String, schoolId: String, data: Map<String, Any>): LeaveRequest {
            return LeaveRequest(
                id = id,
                schoolId = schoolId,
                requestCode = data["requestCode"] as? String ?: "",
                applicantId = data["applicantId"] as? String ?: "",
                applicantName = data["applicantName"] as? String ?: "",
                applicantType = LeaveApplicantType.fromString(data["applicantType"] as? String),
                departmentOrGrade = data["departmentOrGrade"] as? String ?: "",
                category = LeaveCategory.fromString(data["category"] as? String),
                startDate = data["startDate"] as? String ?: "",
                endDate = data["endDate"] as? String ?: "",
                totalDays = (data["totalDays"] as? Number)?.toInt() ?: 1,
                reason = data["reason"] as? String ?: "",
                attachmentNote = data["attachmentNote"] as? String ?: "",
                status = LeaveStatus.fromString(data["status"] as? String),
                reviewerName = data["reviewerName"] as? String ?: "",
                reviewRemarks = data["reviewRemarks"] as? String ?: "",
                submittedDate = data["submittedDate"] as? String ?: "",
                reviewedDate = data["reviewedDate"] as? String ?: "",
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}
