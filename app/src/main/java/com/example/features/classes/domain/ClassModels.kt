package com.example.features.classes.domain

import com.example.core.tenant.TenantScopedEntity

/**
 * Academic Class entity (e.g., Grade 10, Grade 11).
 * Subcollection: schools/{schoolId}/classes/{classId}
 */
data class AcademicClass(
    override val id: String,
    override val schoolId: String,
    val name: String,
    val code: String,
    val numericGrade: Int,
    val stream: String = "General",
    val sectionsCount: Int = 1,
    val totalStudents: Int = 0,
    val academicYear: String = "2026-2027",
    val createdAt: Long = System.currentTimeMillis()
) : TenantScopedEntity {

    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "id" to id,
        "schoolId" to schoolId,
        "name" to name,
        "code" to code,
        "numericGrade" to numericGrade,
        "stream" to stream,
        "sectionsCount" to sectionsCount,
        "totalStudents" to totalStudents,
        "academicYear" to academicYear,
        "createdAt" to createdAt
    )

    companion object {
        fun fromFirestoreMap(docId: String, schoolId: String, map: Map<String, Any?>): AcademicClass {
            return AcademicClass(
                id = docId,
                schoolId = schoolId,
                name = map["name"] as? String ?: "Class",
                code = map["code"] as? String ?: "CLS",
                numericGrade = (map["numericGrade"] as? Number)?.toInt() ?: 1,
                stream = map["stream"] as? String ?: "General",
                sectionsCount = (map["sectionsCount"] as? Number)?.toInt() ?: 1,
                totalStudents = (map["totalStudents"] as? Number)?.toInt() ?: 0,
                academicYear = map["academicYear"] as? String ?: "2026-2027",
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}

/**
 * Class Section entity (e.g., Section A, Section B).
 * Subcollection: schools/{schoolId}/sections/{sectionId}
 */
data class ClassSection(
    override val id: String,
    override val schoolId: String,
    val classId: String,
    val className: String,
    val name: String,
    val roomNumber: String,
    val capacity: Int = 35,
    val currentEnrollment: Int = 0,
    val classTeacherId: String? = null,
    val classTeacherName: String? = null,
    val academicYear: String = "2026-2027",
    val createdAt: Long = System.currentTimeMillis()
) : TenantScopedEntity {

    val displayName: String = "$className - $name"
    val occupancyPercentage: Float
        get() = if (capacity > 0) (currentEnrollment.toFloat() / capacity.toFloat()) * 100f else 0f

    fun toFirestoreMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "id" to id,
            "schoolId" to schoolId,
            "classId" to classId,
            "className" to className,
            "name" to name,
            "roomNumber" to roomNumber,
            "capacity" to capacity,
            "currentEnrollment" to currentEnrollment,
            "academicYear" to academicYear,
            "createdAt" to createdAt
        )
        classTeacherId?.let { map["classTeacherId"] = it }
        classTeacherName?.let { map["classTeacherName"] = it }
        return map
    }

    companion object {
        fun fromFirestoreMap(docId: String, schoolId: String, map: Map<String, Any?>): ClassSection {
            return ClassSection(
                id = docId,
                schoolId = schoolId,
                classId = map["classId"] as? String ?: "",
                className = map["className"] as? String ?: "",
                name = map["name"] as? String ?: "A",
                roomNumber = map["roomNumber"] as? String ?: "Room 101",
                capacity = (map["capacity"] as? Number)?.toInt() ?: 35,
                currentEnrollment = (map["currentEnrollment"] as? Number)?.toInt() ?: 0,
                classTeacherId = map["classTeacherId"] as? String,
                classTeacherName = map["classTeacherName"] as? String,
                academicYear = map["academicYear"] as? String ?: "2026-2027",
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}

/**
 * Subject Allocation mapping linking section, subject, faculty, and weekly workload.
 */
data class SubjectAllocation(
    val id: String,
    val schoolId: String,
    val classId: String,
    val sectionId: String,
    val sectionName: String,
    val subjectName: String,
    val subjectCode: String,
    val teacherId: String,
    val teacherName: String,
    val periodsPerWeek: Int = 4,
    val isElective: Boolean = false,
    val roomNumber: String = "Classroom"
) {
    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "id" to id,
        "schoolId" to schoolId,
        "classId" to classId,
        "sectionId" to sectionId,
        "sectionName" to sectionName,
        "subjectName" to subjectName,
        "subjectCode" to subjectCode,
        "teacherId" to teacherId,
        "teacherName" to teacherName,
        "periodsPerWeek" to periodsPerWeek,
        "isElective" to isElective,
        "roomNumber" to roomNumber
    )

    companion object {
        fun fromFirestoreMap(docId: String, schoolId: String, map: Map<String, Any?>): SubjectAllocation {
            return SubjectAllocation(
                id = docId,
                schoolId = schoolId,
                classId = map["classId"] as? String ?: "",
                sectionId = map["sectionId"] as? String ?: "",
                sectionName = map["sectionName"] as? String ?: "",
                subjectName = map["subjectName"] as? String ?: "",
                subjectCode = map["subjectCode"] as? String ?: "",
                teacherId = map["teacherId"] as? String ?: "",
                teacherName = map["teacherName"] as? String ?: "Assigned Faculty",
                periodsPerWeek = (map["periodsPerWeek"] as? Number)?.toInt() ?: 4,
                isElective = (map["isElective"] as? Boolean) ?: false,
                roomNumber = map["roomNumber"] as? String ?: "Classroom"
            )
        }
    }
}
