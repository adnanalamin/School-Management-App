package com.example.features.documents.domain

import com.example.core.tenant.TenantScopedEntity

/**
 * Classification categories for institutional documents, syllabi, ID cards, and official certificates.
 */
enum class DocumentCategory(val displayName: String) {
    SYLLABUS("Course Syllabus"),
    CERTIFICATE("Official Certificate"),
    ID_CARD("Student / Staff ID Card"),
    POLICY("Policy & Handbook"),
    CIRCULAR_ARCHIVE("Academic Archive"),
    FORM("Permission & Consent Form");

    companion object {
        fun fromString(value: String?): DocumentCategory {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: SYLLABUS
        }
    }
}

/**
 * Authenticity and publication verification status for institutional records.
 */
enum class DocumentVerificationStatus(val displayName: String) {
    VERIFIED("Verified & Issued"),
    PENDING_REVIEW("Pending Review"),
    ARCHIVED("Archived"),
    REVOKED("Revoked");

    companion object {
        fun fromString(value: String?): DocumentVerificationStatus {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: VERIFIED
        }
    }
}

/**
 * Multi-tenant institutional document, curriculum syllabus, or issued credential.
 * Scoped to `schools/{schoolId}/documents/{docId}` (Subcollection 15).
 */
data class SchoolDocument(
    override val id: String,
    override val schoolId: String,
    val documentCode: String,
    val title: String,
    val description: String,
    val category: DocumentCategory = DocumentCategory.SYLLABUS,
    val verificationStatus: DocumentVerificationStatus = DocumentVerificationStatus.VERIFIED,
    val targetGradeOrDepartment: String = "All Grades",
    val issuedToName: String = "",
    val issuedToId: String = "",
    val fileName: String,
    val fileSizeLabel: String = "1.2 MB",
    val academicYear: String = "2026-2027",
    val issuedBy: String,
    val issuedDate: String,
    val downloadCount: Int = 0,
    val isPinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) : TenantScopedEntity {

    val isOfficialCredential: Boolean
        get() = category == DocumentCategory.CERTIFICATE || category == DocumentCategory.ID_CARD

    val isActive: Boolean
        get() = verificationStatus == DocumentVerificationStatus.VERIFIED ||
            verificationStatus == DocumentVerificationStatus.PENDING_REVIEW

    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "id" to id,
        "schoolId" to schoolId,
        "documentCode" to documentCode,
        "title" to title,
        "description" to description,
        "category" to category.name,
        "verificationStatus" to verificationStatus.name,
        "targetGradeOrDepartment" to targetGradeOrDepartment,
        "issuedToName" to issuedToName,
        "issuedToId" to issuedToId,
        "fileName" to fileName,
        "fileSizeLabel" to fileSizeLabel,
        "academicYear" to academicYear,
        "issuedBy" to issuedBy,
        "issuedDate" to issuedDate,
        "downloadCount" to downloadCount,
        "isPinned" to isPinned,
        "createdAt" to createdAt
    )

    companion object {
        fun fromFirestoreMap(id: String, schoolId: String, data: Map<String, Any>): SchoolDocument {
            return SchoolDocument(
                id = id,
                schoolId = schoolId,
                documentCode = data["documentCode"] as? String ?: "",
                title = data["title"] as? String ?: "",
                description = data["description"] as? String ?: "",
                category = DocumentCategory.fromString(data["category"] as? String),
                verificationStatus = DocumentVerificationStatus.fromString(data["verificationStatus"] as? String),
                targetGradeOrDepartment = data["targetGradeOrDepartment"] as? String ?: "All Grades",
                issuedToName = data["issuedToName"] as? String ?: "",
                issuedToId = data["issuedToId"] as? String ?: "",
                fileName = data["fileName"] as? String ?: "",
                fileSizeLabel = data["fileSizeLabel"] as? String ?: "1.2 MB",
                academicYear = data["academicYear"] as? String ?: "2026-2027",
                issuedBy = data["issuedBy"] as? String ?: "",
                issuedDate = data["issuedDate"] as? String ?: "",
                downloadCount = (data["downloadCount"] as? Number)?.toInt() ?: 0,
                isPinned = data["isPinned"] as? Boolean ?: false,
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}
