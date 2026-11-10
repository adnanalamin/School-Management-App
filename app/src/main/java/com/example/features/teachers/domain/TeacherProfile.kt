package com.example.features.teachers.domain

import com.example.core.tenant.TenantScopedEntity

enum class TeacherStatus(val displayName: String) {
    ACTIVE("Active"),
    ON_LEAVE("On Leave"),
    RESIGNED("Resigned"),
    RETIRED("Retired")
}

data class TeacherProfile(
    override val id: String,
    override val schoolId: String,
    val employeeId: String,
    val firstName: String,
    val lastName: String,
    val fullName: String = "$firstName $lastName".trim(),
    val designation: String,
    val department: String,
    val primarySubject: String,
    val additionalSubjects: List<String> = emptyList(),
    val qualification: String,
    val joiningDate: String = "2024-08-15",
    val experienceYears: Int = 1,
    val email: String,
    val phone: String,
    val emergencyContact: String = "",
    val address: String = "",
    val assignedClassId: String? = null,
    val assignedSectionId: String? = null,
    val isClassTeacher: Boolean = !assignedClassId.isNullOrBlank(),
    val bloodGroup: String = "O+",
    val salaryTier: String = "Tier 1 - Senior Faculty",
    val status: TeacherStatus = TeacherStatus.ACTIVE,
    val photoUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
) : TenantScopedEntity {

    fun toFirestoreMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "id" to id,
            "schoolId" to schoolId,
            "employeeId" to employeeId,
            "firstName" to firstName,
            "lastName" to lastName,
            "fullName" to fullName,
            "designation" to designation,
            "department" to department,
            "primarySubject" to primarySubject,
            "additionalSubjects" to additionalSubjects,
            "qualification" to qualification,
            "joiningDate" to joiningDate,
            "experienceYears" to experienceYears,
            "email" to email,
            "phone" to phone,
            "emergencyContact" to emergencyContact,
            "address" to address,
            "isClassTeacher" to isClassTeacher,
            "bloodGroup" to bloodGroup,
            "salaryTier" to salaryTier,
            "status" to status.name,
            "photoUrl" to photoUrl,
            "createdAt" to createdAt
        )
        assignedClassId?.let { map["assignedClassId"] = it }
        assignedSectionId?.let { map["assignedSectionId"] = it }
        return map
    }

    companion object {
        fun fromFirestoreMap(docId: String, schoolId: String, map: Map<String, Any?>): TeacherProfile {
            val fName = map["firstName"] as? String ?: ""
            val lName = map["lastName"] as? String ?: ""
            val full = map["fullName"] as? String ?: "$fName $lName".trim()
            val statusStr = map["status"] as? String ?: "ACTIVE"
            val status = try {
                TeacherStatus.valueOf(statusStr.uppercase())
            } catch (e: Exception) {
                TeacherStatus.ACTIVE
            }

            @Suppress("UNCHECKED_CAST")
            val additional = (map["additionalSubjects"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()

            val classId = map["assignedClassId"] as? String
            val sectionId = map["assignedSectionId"] as? String

            return TeacherProfile(
                id = docId,
                schoolId = schoolId,
                employeeId = map["employeeId"] as? String ?: "",
                firstName = fName,
                lastName = lName,
                fullName = full.ifBlank { fName },
                designation = map["designation"] as? String ?: "Faculty Member",
                department = map["department"] as? String ?: "General Education",
                primarySubject = map["primarySubject"] as? String ?: "General",
                additionalSubjects = additional,
                qualification = map["qualification"] as? String ?: "Bachelor of Education",
                joiningDate = map["joiningDate"] as? String ?: "2024-08-15",
                experienceYears = (map["experienceYears"] as? Number)?.toInt() ?: 1,
                email = map["email"] as? String ?: "",
                phone = map["phone"] as? String ?: "",
                emergencyContact = map["emergencyContact"] as? String ?: "",
                address = map["address"] as? String ?: "",
                assignedClassId = classId,
                assignedSectionId = sectionId,
                isClassTeacher = (map["isClassTeacher"] as? Boolean) ?: (!classId.isNullOrBlank()),
                bloodGroup = map["bloodGroup"] as? String ?: "O+",
                salaryTier = map["salaryTier"] as? String ?: "Tier 1 - Senior Faculty",
                status = status,
                photoUrl = map["photoUrl"] as? String ?: "",
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}
