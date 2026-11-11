package com.example.features.exams.domain

import com.example.core.tenant.TenantScopedEntity

/**
 * Examination series classifications.
 */
enum class ExamType(val displayName: String, val code: String) {
    MIDTERM("Midterm Examination", "MID"),
    FINAL("Final Term Examination", "FIN"),
    PERIODIC("Periodic Assessment", "PER"),
    PRACTICAL("Laboratory & Practical", "LAB"),
    MOCK("Preparatory / Mock Exam", "MCK");

    companion object {
        fun fromString(value: String?): ExamType {
            return entries.firstOrNull {
                it.name.equals(value, ignoreCase = true) || it.code.equals(value, ignoreCase = true)
            } ?: MIDTERM
        }
    }
}

/**
 * Execution status of an examination series or paper.
 */
enum class ExamStatus(val displayName: String) {
    SCHEDULED("Scheduled"),
    ONGOING("In Progress"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    companion object {
        fun fromString(value: String?): ExamStatus {
            return entries.firstOrNull {
                it.name.equals(value, ignoreCase = true)
            } ?: SCHEDULED
        }
    }
}

/**
 * Individual subject examination paper in the timetable.
 */
data class ExamPaper(
    val id: String,
    val subjectName: String,
    val subjectCode: String,
    val examDate: String, // Format: YYYY-MM-DD
    val startTime: String, // e.g. "09:00 AM"
    val endTime: String, // e.g. "12:00 PM"
    val durationMinutes: Int = 180,
    val totalMarks: Int = 100,
    val passingMarks: Int = 40,
    val roomNumber: String,
    val invigilatorId: String,
    val invigilatorName: String,
    val instructions: String = "No electronic calculators permitted. Arrive 15 minutes prior to commencement."
) {
    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "id" to id,
        "subjectName" to subjectName,
        "subjectCode" to subjectCode,
        "examDate" to examDate,
        "startTime" to startTime,
        "endTime" to endTime,
        "durationMinutes" to durationMinutes,
        "totalMarks" to totalMarks,
        "passingMarks" to passingMarks,
        "roomNumber" to roomNumber,
        "invigilatorId" to invigilatorId,
        "invigilatorName" to invigilatorName,
        "instructions" to instructions
    )

    companion object {
        fun fromFirestoreMap(data: Map<String, Any>): ExamPaper {
            return ExamPaper(
                id = data["id"] as? String ?: "",
                subjectName = data["subjectName"] as? String ?: "",
                subjectCode = data["subjectCode"] as? String ?: "",
                examDate = data["examDate"] as? String ?: "",
                startTime = data["startTime"] as? String ?: "",
                endTime = data["endTime"] as? String ?: "",
                durationMinutes = (data["durationMinutes"] as? Number)?.toInt() ?: 180,
                totalMarks = (data["totalMarks"] as? Number)?.toInt() ?: 100,
                passingMarks = (data["passingMarks"] as? Number)?.toInt() ?: 40,
                roomNumber = data["roomNumber"] as? String ?: "",
                invigilatorId = data["invigilatorId"] as? String ?: "",
                invigilatorName = data["invigilatorName"] as? String ?: "",
                instructions = data["instructions"] as? String ?: ""
            )
        }
    }
}

/**
 * ExamSchedule represents a complete examination series (e.g. Midterms for Grade 10).
 * Scoped to `schools/{schoolId}/exams/{examId}`.
 */
data class ExamSchedule(
    override val id: String,
    override val schoolId: String,
    val title: String,
    val examType: ExamType = ExamType.MIDTERM,
    val academicSession: String,
    val gradeLevel: String,
    val startDate: String, // YYYY-MM-DD
    val endDate: String, // YYYY-MM-DD
    val status: ExamStatus = ExamStatus.SCHEDULED,
    val papers: List<ExamPaper> = emptyList(),
    val totalPapers: Int = papers.size,
    val createdAt: Long = System.currentTimeMillis()
) : TenantScopedEntity {

    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "id" to id,
        "schoolId" to schoolId,
        "title" to title,
        "examType" to examType.name,
        "academicSession" to academicSession,
        "gradeLevel" to gradeLevel,
        "startDate" to startDate,
        "endDate" to endDate,
        "status" to status.name,
        "papers" to papers.map { it.toFirestoreMap() },
        "totalPapers" to papers.size,
        "createdAt" to createdAt
    )

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromFirestoreMap(id: String, schoolId: String, data: Map<String, Any>): ExamSchedule {
            val papersRaw = data["papers"] as? List<Map<String, Any>> ?: emptyList()
            val parsedPapers = papersRaw.map { ExamPaper.fromFirestoreMap(it) }

            return ExamSchedule(
                id = id,
                schoolId = schoolId,
                title = data["title"] as? String ?: "",
                examType = ExamType.fromString(data["examType"] as? String),
                academicSession = data["academicSession"] as? String ?: "",
                gradeLevel = data["gradeLevel"] as? String ?: "",
                startDate = data["startDate"] as? String ?: "",
                endDate = data["endDate"] as? String ?: "",
                status = ExamStatus.fromString(data["status"] as? String),
                papers = parsedPapers,
                totalPapers = (data["totalPapers"] as? Number)?.toInt() ?: parsedPapers.size,
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}

/**
 * Student Examination Hall Ticket / Admit Card.
 */
data class HallTicket(
    val ticketNumber: String,
    val schoolId: String,
    val schoolName: String,
    val studentId: String,
    val studentName: String,
    val rollNumber: String,
    val admissionNumber: String,
    val gradeLevel: String,
    val section: String,
    val examTitle: String,
    val academicSession: String,
    val papers: List<ExamPaper>,
    val issuedDate: String,
    val verifiedBy: String
)
