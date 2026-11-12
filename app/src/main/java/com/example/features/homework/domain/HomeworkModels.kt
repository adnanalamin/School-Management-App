package com.example.features.homework.domain

import com.example.core.tenant.TenantScopedEntity

/**
 * Lifecycle status of a homework assignment.
 */
enum class HomeworkStatus(val displayName: String) {
    DRAFT("Draft"),
    ASSIGNED("Active Assignment"),
    DUE_SOON("Due Soon"),
    CLOSED("Submission Closed");

    companion object {
        fun fromString(value: String?): HomeworkStatus {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: ASSIGNED
        }
    }
}

/**
 * Lifecycle evaluation status of an individual student's submission.
 */
enum class SubmissionStatus(val displayName: String) {
    PENDING("Not Submitted"),
    SUBMITTED("Submitted - Pending Review"),
    GRADED("Graded & Evaluated"),
    LATE("Submitted Late"),
    RESUBMISSION_REQUESTED("Resubmission Required");

    companion object {
        fun fromString(value: String?): SubmissionStatus {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: PENDING
        }
    }
}

/**
 * Individual student's homework submission deliverable.
 */
data class HomeworkSubmission(
    val studentId: String,
    val studentName: String,
    val rollNumber: String,
    val submissionDate: String = "",
    val attachmentTitle: String = "",
    val studentNotes: String = "",
    val status: SubmissionStatus = SubmissionStatus.SUBMITTED,
    val marksAwarded: Double? = null,
    val feedback: String = "",
    val evaluatedBy: String = "",
    val evaluatedAt: Long? = null
) {
    fun toFirestoreMap(): Map<String, Any?> = mapOf(
        "studentId" to studentId,
        "studentName" to studentName,
        "rollNumber" to rollNumber,
        "submissionDate" to submissionDate,
        "attachmentTitle" to attachmentTitle,
        "studentNotes" to studentNotes,
        "status" to status.name,
        "marksAwarded" to marksAwarded,
        "feedback" to feedback,
        "evaluatedBy" to evaluatedBy,
        "evaluatedAt" to evaluatedAt
    )

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromFirestoreMap(data: Map<String, Any?>): HomeworkSubmission {
            return HomeworkSubmission(
                studentId = data["studentId"] as? String ?: "",
                studentName = data["studentName"] as? String ?: "",
                rollNumber = data["rollNumber"] as? String ?: "",
                submissionDate = data["submissionDate"] as? String ?: "",
                attachmentTitle = data["attachmentTitle"] as? String ?: "",
                studentNotes = data["studentNotes"] as? String ?: "",
                status = SubmissionStatus.fromString(data["status"] as? String),
                marksAwarded = (data["marksAwarded"] as? Number)?.toDouble(),
                feedback = data["feedback"] as? String ?: "",
                evaluatedBy = data["evaluatedBy"] as? String ?: "",
                evaluatedAt = (data["evaluatedAt"] as? Number)?.toLong()
            )
        }
    }
}

/**
 * Homework and Coursework assignment entity.
 * Scoped to `schools/{schoolId}/homework/{homeworkId}` (Subcollection 11).
 */
data class HomeworkAssignment(
    override val id: String,
    override val schoolId: String,
    val title: String,
    val description: String,
    val subjectCode: String,
    val subjectName: String,
    val gradeLevel: String,
    val section: String,
    val assignedDate: String,
    val dueDate: String,
    val totalMarks: Double = 50.0,
    val teacherId: String,
    val teacherName: String,
    val status: HomeworkStatus = HomeworkStatus.ASSIGNED,
    val attachmentTitle: String = "",
    val submissions: List<HomeworkSubmission> = emptyList(),
    val totalEnrolledStudents: Int = 30,
    val createdAt: Long = System.currentTimeMillis()
) : TenantScopedEntity {

    val submissionCount: Int
        get() = submissions.count { it.status != SubmissionStatus.PENDING }

    val gradedCount: Int
        get() = submissions.count { it.status == SubmissionStatus.GRADED }

    val pendingReviewCount: Int
        get() = submissions.count { it.status == SubmissionStatus.SUBMITTED || it.status == SubmissionStatus.LATE }

    val submissionRatePercentage: Double
        get() = if (totalEnrolledStudents > 0) {
            (submissionCount.toDouble() / totalEnrolledStudents.toDouble()) * 100.0
        } else 0.0

    val averageScore: Double
        get() {
            val graded = submissions.mapNotNull { it.marksAwarded }
            return if (graded.isNotEmpty()) graded.average() else 0.0
        }

    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "id" to id,
        "schoolId" to schoolId,
        "title" to title,
        "description" to description,
        "subjectCode" to subjectCode,
        "subjectName" to subjectName,
        "gradeLevel" to gradeLevel,
        "section" to section,
        "assignedDate" to assignedDate,
        "dueDate" to dueDate,
        "totalMarks" to totalMarks,
        "teacherId" to teacherId,
        "teacherName" to teacherName,
        "status" to status.name,
        "attachmentTitle" to attachmentTitle,
        "submissions" to submissions.map { it.toFirestoreMap() },
        "totalEnrolledStudents" to totalEnrolledStudents,
        "createdAt" to createdAt
    )

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromFirestoreMap(id: String, schoolId: String, data: Map<String, Any>): HomeworkAssignment {
            val rawSubmissions = data["submissions"] as? List<Map<String, Any?>> ?: emptyList()
            val parsedSubmissions = rawSubmissions.map { HomeworkSubmission.fromFirestoreMap(it) }

            return HomeworkAssignment(
                id = id,
                schoolId = schoolId,
                title = data["title"] as? String ?: "",
                description = data["description"] as? String ?: "",
                subjectCode = data["subjectCode"] as? String ?: "",
                subjectName = data["subjectName"] as? String ?: "",
                gradeLevel = data["gradeLevel"] as? String ?: "",
                section = data["section"] as? String ?: "",
                assignedDate = data["assignedDate"] as? String ?: "",
                dueDate = data["dueDate"] as? String ?: "",
                totalMarks = (data["totalMarks"] as? Number)?.toDouble() ?: 50.0,
                teacherId = data["teacherId"] as? String ?: "",
                teacherName = data["teacherName"] as? String ?: "",
                status = HomeworkStatus.fromString(data["status"] as? String),
                attachmentTitle = data["attachmentTitle"] as? String ?: "",
                submissions = parsedSubmissions,
                totalEnrolledStudents = (data["totalEnrolledStudents"] as? Number)?.toInt() ?: 30,
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}
