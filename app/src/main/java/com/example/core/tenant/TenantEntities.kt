package com.example.core.tenant

/**
 * Common interface enforced on all multi-tenant documents across the 15 isolated subcollections.
 */
interface TenantScopedEntity {
    val id: String
    val schoolId: String
}

// 1. Students: schools/{schoolId}/students/{studentId}
data class StudentEntity(
    override val id: String,
    override val schoolId: String,
    val name: String,
    val rollNumber: String,
    val className: String,
    val section: String,
    val parentEmail: String,
    val parentPhone: String = "",
    val status: String = "ACTIVE"
) : TenantScopedEntity {
    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "id" to id, "schoolId" to schoolId, "name" to name, "rollNumber" to rollNumber,
        "className" to className, "section" to section, "parentEmail" to parentEmail,
        "parentPhone" to parentPhone, "status" to status
    )
}

// 2. Teachers: schools/{schoolId}/teachers/{teacherId}
data class TeacherEntity(
    override val id: String,
    override val schoolId: String,
    val name: String,
    val employeeId: String,
    val email: String,
    val subject: String,
    val phone: String = "",
    val designation: String = "Senior Faculty"
) : TenantScopedEntity {
    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "id" to id, "schoolId" to schoolId, "name" to name, "employeeId" to employeeId,
        "email" to email, "subject" to subject, "phone" to phone, "designation" to designation
    )
}

// 3. Parents: schools/{schoolId}/parents/{parentId}
data class ParentEntity(
    override val id: String,
    override val schoolId: String,
    val name: String,
    val email: String,
    val phone: String,
    val studentIds: List<String> = emptyList()
) : TenantScopedEntity {
    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "id" to id, "schoolId" to schoolId, "name" to name, "email" to email,
        "phone" to phone, "studentIds" to studentIds
    )
}

// 4. Classes: schools/{schoolId}/classes/{classId}
data class ClassEntity(
    override val id: String,
    override val schoolId: String,
    val name: String,
    val gradeLevel: Int,
    val classTeacherId: String = ""
) : TenantScopedEntity {
    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "id" to id, "schoolId" to schoolId, "name" to name, "gradeLevel" to gradeLevel,
        "classTeacherId" to classTeacherId
    )
}

// 5. Sections: schools/{schoolId}/sections/{sectionId}
data class SectionEntity(
    override val id: String,
    override val schoolId: String,
    val classId: String,
    val name: String,
    val roomNumber: String = ""
) : TenantScopedEntity {
    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "id" to id, "schoolId" to schoolId, "classId" to classId, "name" to name, "roomNumber" to roomNumber
    )
}

// 6. Attendance: schools/{schoolId}/attendance/{attendanceId}
data class AttendanceEntity(
    override val id: String,
    override val schoolId: String,
    val date: String,
    val studentId: String,
    val studentName: String,
    val status: String, // PRESENT, ABSENT, LATE, EXCUSED
    val markedBy: String = ""
) : TenantScopedEntity {
    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "id" to id, "schoolId" to schoolId, "date" to date, "studentId" to studentId,
        "studentName" to studentName, "status" to status, "markedBy" to markedBy
    )
}

// 7. Fees: schools/{schoolId}/fees/{feeId}
data class FeeEntity(
    override val id: String,
    override val schoolId: String,
    val studentId: String,
    val studentName: String,
    val title: String,
    val amount: Double,
    val dueDate: String,
    val status: String // PAID, PENDING, OVERDUE
) : TenantScopedEntity {
    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "id" to id, "schoolId" to schoolId, "studentId" to studentId, "studentName" to studentName,
        "title" to title, "amount" to amount, "dueDate" to dueDate, "status" to status
    )
}

// 8. Exams: schools/{schoolId}/exams/{examId}
data class ExamEntity(
    override val id: String,
    override val schoolId: String,
    val title: String,
    val term: String,
    val startDate: String,
    val endDate: String
) : TenantScopedEntity {
    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "id" to id, "schoolId" to schoolId, "title" to title, "term" to term,
        "startDate" to startDate, "endDate" to endDate
    )
}

// 9. Results: schools/{schoolId}/results/{resultId}
data class ResultEntity(
    override val id: String,
    override val schoolId: String,
    val examId: String,
    val studentId: String,
    val studentName: String,
    val subject: String,
    val marksObtained: Double,
    val totalMarks: Double,
    val grade: String
) : TenantScopedEntity {
    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "id" to id, "schoolId" to schoolId, "examId" to examId, "studentId" to studentId,
        "studentName" to studentName, "subject" to subject, "marksObtained" to marksObtained,
        "totalMarks" to totalMarks, "grade" to grade
    )
}

// 10. Notices: schools/{schoolId}/notices/{noticeId}
data class NoticeEntity(
    override val id: String,
    override val schoolId: String,
    val title: String,
    val content: String,
    val date: String,
    val targetAudience: String, // ALL, TEACHERS, STUDENTS, PARENTS
    val isUrgent: Boolean = false
) : TenantScopedEntity {
    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "id" to id, "schoolId" to schoolId, "title" to title, "content" to content,
        "date" to date, "targetAudience" to targetAudience, "isUrgent" to isUrgent
    )
}

// 11. Homework: schools/{schoolId}/homework/{homeworkId}
data class HomeworkEntity(
    override val id: String,
    override val schoolId: String,
    val classId: String,
    val subject: String,
    val title: String,
    val description: String,
    val dueDate: String
) : TenantScopedEntity {
    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "id" to id, "schoolId" to schoolId, "classId" to classId, "subject" to subject,
        "title" to title, "description" to description, "dueDate" to dueDate
    )
}

// 12. Library: schools/{schoolId}/library/{bookId}
data class LibraryBookEntity(
    override val id: String,
    override val schoolId: String,
    val title: String,
    val author: String,
    val isbn: String,
    val copiesTotal: Int,
    val copiesAvailable: Int
) : TenantScopedEntity {
    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "id" to id, "schoolId" to schoolId, "title" to title, "author" to author,
        "isbn" to isbn, "copiesTotal" to copiesTotal, "copiesAvailable" to copiesAvailable
    )
}

// 13. Transport: schools/{schoolId}/transport/{routeId}
data class TransportRouteEntity(
    override val id: String,
    override val schoolId: String,
    val routeName: String,
    val vehicleNumber: String,
    val driverName: String,
    val driverPhone: String,
    val stopsCount: Int = 5
) : TenantScopedEntity {
    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "id" to id, "schoolId" to schoolId, "routeName" to routeName, "vehicleNumber" to vehicleNumber,
        "driverName" to driverName, "driverPhone" to driverPhone, "stopsCount" to stopsCount
    )
}

// 14. Events: schools/{schoolId}/events/{eventId}
data class EventEntity(
    override val id: String,
    override val schoolId: String,
    val title: String,
    val date: String,
    val location: String,
    val category: String // SPORTS, ACADEMIC, CULTURAL, HOLIDAY
) : TenantScopedEntity {
    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "id" to id, "schoolId" to schoolId, "title" to title, "date" to date,
        "location" to location, "category" to category
    )
}

// 15. Documents: schools/{schoolId}/documents/{docId}
data class SchoolDocumentEntity(
    override val id: String,
    override val schoolId: String,
    val title: String,
    val category: String, // ACCREDITATION, CURRICULUM, POLICY, AFFILIATION
    val fileFormat: String = "PDF",
    val uploadDate: String = "2026-02-15"
) : TenantScopedEntity {
    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "id" to id, "schoolId" to schoolId, "title" to title, "category" to category,
        "fileFormat" to fileFormat, "uploadDate" to uploadDate
    )
}
