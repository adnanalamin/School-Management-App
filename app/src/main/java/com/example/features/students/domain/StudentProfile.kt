package com.example.features.students.domain

import com.example.core.tenant.TenantScopedEntity

enum class StudentStatus(val displayName: String) {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    SUSPENDED("Suspended"),
    ALUMNI("Alumni"),
    TRANSFERRED("Transferred")
}

data class StudentProfile(
    override val id: String,
    override val schoolId: String,
    val firstName: String,
    val lastName: String,
    val fullName: String = "$firstName $lastName".trim(),
    val rollNumber: String,
    val admissionNumber: String,
    val gradeLevel: String,
    val section: String,
    val dateOfBirth: String,
    val gender: String,
    val bloodGroup: String = "O+",
    val status: StudentStatus = StudentStatus.ACTIVE,
    val guardianName: String,
    val guardianRelationship: String = "Parent",
    val guardianPhone: String,
    val guardianEmail: String,
    val address: String,
    val emergencyContact: String,
    val medicalNotes: String = "None reported",
    val enrollmentDate: String = "2026-08-15",
    val photoUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
) : TenantScopedEntity {

    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "id" to id,
        "schoolId" to schoolId,
        "firstName" to firstName,
        "lastName" to lastName,
        "fullName" to fullName,
        "rollNumber" to rollNumber,
        "admissionNumber" to admissionNumber,
        "gradeLevel" to gradeLevel,
        "section" to section,
        "dateOfBirth" to dateOfBirth,
        "gender" to gender,
        "bloodGroup" to bloodGroup,
        "status" to status.name,
        "guardianName" to guardianName,
        "guardianRelationship" to guardianRelationship,
        "guardianPhone" to guardianPhone,
        "guardianEmail" to guardianEmail,
        "address" to address,
        "emergencyContact" to emergencyContact,
        "medicalNotes" to medicalNotes,
        "enrollmentDate" to enrollmentDate,
        "photoUrl" to photoUrl,
        "createdAt" to createdAt
    )

    companion object {
        fun fromFirestoreMap(docId: String, schoolId: String, map: Map<String, Any?>): StudentProfile {
            val fName = map["firstName"] as? String ?: ""
            val lName = map["lastName"] as? String ?: ""
            val full = map["fullName"] as? String ?: "$fName $lName".trim()
            val statusStr = map["status"] as? String ?: "ACTIVE"
            val status = try {
                StudentStatus.valueOf(statusStr.uppercase())
            } catch (e: Exception) {
                StudentStatus.ACTIVE
            }

            return StudentProfile(
                id = docId,
                schoolId = schoolId,
                firstName = fName,
                lastName = lName,
                fullName = full.ifBlank { fName },
                rollNumber = map["rollNumber"] as? String ?: "",
                admissionNumber = map["admissionNumber"] as? String ?: "",
                gradeLevel = map["gradeLevel"] as? String ?: "Grade 10",
                section = map["section"] as? String ?: "A",
                dateOfBirth = map["dateOfBirth"] as? String ?: "2010-01-01",
                gender = map["gender"] as? String ?: "Unspecified",
                bloodGroup = map["bloodGroup"] as? String ?: "O+",
                status = status,
                guardianName = map["guardianName"] as? String ?: "",
                guardianRelationship = map["guardianRelationship"] as? String ?: "Parent",
                guardianPhone = map["guardianPhone"] as? String ?: "",
                guardianEmail = map["guardianEmail"] as? String ?: "",
                address = map["address"] as? String ?: "",
                emergencyContact = map["emergencyContact"] as? String ?: "",
                medicalNotes = map["medicalNotes"] as? String ?: "None reported",
                enrollmentDate = map["enrollmentDate"] as? String ?: "2026-08-15",
                photoUrl = map["photoUrl"] as? String ?: "",
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}
