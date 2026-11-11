package com.example.features.results.domain

import com.example.core.tenant.TenantScopedEntity

/**
 * Standard Academic Grading Scale.
 */
enum class GradeScale(
    val letter: String,
    val minPercentage: Double,
    val maxPercentage: Double,
    val gradePoint: Double,
    val description: String
) {
    A_PLUS("A+", 90.0, 100.0, 4.0, "Outstanding Academic Mastery"),
    A("A", 80.0, 89.99, 3.7, "Excellent Academic Competency"),
    B_PLUS("B+", 70.0, 79.99, 3.3, "Very Good Proficiency"),
    B("B", 60.0, 69.99, 3.0, "Good Working Understanding"),
    C("C", 50.0, 59.99, 2.0, "Satisfactory / Average"),
    D("D", 40.0, 49.99, 1.0, "Marginal / Passing"),
    F("F", 0.0, 39.99, 0.0, "Unsatisfactory / Unsatisfied Criteria");

    companion object {
        fun calculate(percentage: Double): GradeScale {
            val clamped = percentage.coerceIn(0.0, 100.0)
            return entries.firstOrNull { clamped >= it.minPercentage && clamped <= it.maxPercentage } ?: F
        }

        fun fromLetter(letter: String?): GradeScale {
            return entries.firstOrNull { it.letter.equals(letter, ignoreCase = true) } ?: C
        }
    }
}

/**
 * Result evaluation lifecycle status.
 */
enum class ResultStatus(val displayName: String) {
    DRAFT("Draft Entry"),
    SUBMITTED("Under Review"),
    PUBLISHED("Officially Published");

    companion object {
        fun fromString(value: String?): ResultStatus {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: PUBLISHED
        }
    }
}

/**
 * Individual subject score in student transcript.
 */
data class SubjectScore(
    val subjectCode: String,
    val subjectName: String,
    val marksObtained: Double,
    val totalMarks: Double = 100.0,
    val grade: String = GradeScale.calculate((marksObtained / totalMarks.coerceAtLeast(1.0)) * 100.0).letter,
    val gradePoint: Double = GradeScale.calculate((marksObtained / totalMarks.coerceAtLeast(1.0)) * 100.0).gradePoint,
    val remarks: String = if (marksObtained >= (totalMarks * 0.4)) "Passed" else "Remedial Recommended"
) {
    val percentage: Double
        get() = if (totalMarks > 0) (marksObtained / totalMarks) * 100.0 else 0.0

    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "subjectCode" to subjectCode,
        "subjectName" to subjectName,
        "marksObtained" to marksObtained,
        "totalMarks" to totalMarks,
        "grade" to grade,
        "gradePoint" to gradePoint,
        "remarks" to remarks
    )

    companion object {
        fun fromFirestoreMap(data: Map<String, Any>): SubjectScore {
            return SubjectScore(
                subjectCode = data["subjectCode"] as? String ?: "",
                subjectName = data["subjectName"] as? String ?: "",
                marksObtained = (data["marksObtained"] as? Number)?.toDouble() ?: 0.0,
                totalMarks = (data["totalMarks"] as? Number)?.toDouble() ?: 100.0,
                grade = data["grade"] as? String ?: "C",
                gradePoint = (data["gradePoint"] as? Number)?.toDouble() ?: 2.0,
                remarks = data["remarks"] as? String ?: ""
            )
        }
    }
}

/**
 * Student complete examination result record.
 * Scoped to `schools/{schoolId}/results/{resultId}` (Subcollection 9).
 */
data class StudentResult(
    override val id: String,
    override val schoolId: String,
    val examId: String,
    val examTitle: String,
    val academicSession: String,
    val studentId: String,
    val studentName: String,
    val rollNumber: String,
    val gradeLevel: String,
    val section: String,
    val subjectScores: List<SubjectScore> = emptyList(),
    val totalObtained: Double = subjectScores.sumOf { it.marksObtained },
    val totalMaxMarks: Double = subjectScores.sumOf { it.totalMarks },
    val percentage: Double = if (totalMaxMarks > 0) (totalObtained / totalMaxMarks) * 100.0 else 0.0,
    val gpa: Double = if (subjectScores.isNotEmpty()) subjectScores.map { it.gradePoint }.average() else 0.0,
    val overallGrade: String = GradeScale.calculate(percentage).letter,
    val rankInClass: Int = 0,
    val status: ResultStatus = ResultStatus.PUBLISHED,
    val teacherRemarks: String = "Demonstrates consistent academic effort across curricula.",
    val createdAt: Long = System.currentTimeMillis()
) : TenantScopedEntity {

    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "id" to id,
        "schoolId" to schoolId,
        "examId" to examId,
        "examTitle" to examTitle,
        "academicSession" to academicSession,
        "studentId" to studentId,
        "studentName" to studentName,
        "rollNumber" to rollNumber,
        "gradeLevel" to gradeLevel,
        "section" to section,
        "subjectScores" to subjectScores.map { it.toFirestoreMap() },
        "totalObtained" to totalObtained,
        "totalMaxMarks" to totalMaxMarks,
        "percentage" to percentage,
        "gpa" to gpa,
        "overallGrade" to overallGrade,
        "rankInClass" to rankInClass,
        "status" to status.name,
        "teacherRemarks" to teacherRemarks,
        "createdAt" to createdAt
    )

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromFirestoreMap(id: String, schoolId: String, data: Map<String, Any>): StudentResult {
            val scoresRaw = data["subjectScores"] as? List<Map<String, Any>> ?: emptyList()
            val parsedScores = scoresRaw.map { SubjectScore.fromFirestoreMap(it) }

            val totalObtained = (data["totalObtained"] as? Number)?.toDouble() ?: parsedScores.sumOf { it.marksObtained }
            val totalMax = (data["totalMaxMarks"] as? Number)?.toDouble() ?: parsedScores.sumOf { it.totalMarks }
            val pct = (data["percentage"] as? Number)?.toDouble() ?: (if (totalMax > 0) (totalObtained / totalMax) * 100.0 else 0.0)
            val gpaVal = (data["gpa"] as? Number)?.toDouble() ?: (if (parsedScores.isNotEmpty()) parsedScores.map { it.gradePoint }.average() else 0.0)

            return StudentResult(
                id = id,
                schoolId = schoolId,
                examId = data["examId"] as? String ?: "",
                examTitle = data["examTitle"] as? String ?: "",
                academicSession = data["academicSession"] as? String ?: "",
                studentId = data["studentId"] as? String ?: "",
                studentName = data["studentName"] as? String ?: "",
                rollNumber = data["rollNumber"] as? String ?: "",
                gradeLevel = data["gradeLevel"] as? String ?: "",
                section = data["section"] as? String ?: "",
                subjectScores = parsedScores,
                totalObtained = totalObtained,
                totalMaxMarks = totalMax,
                percentage = pct,
                gpa = gpaVal,
                overallGrade = data["overallGrade"] as? String ?: GradeScale.calculate(pct).letter,
                rankInClass = (data["rankInClass"] as? Number)?.toInt() ?: 0,
                status = ResultStatus.fromString(data["status"] as? String),
                teacherRemarks = data["teacherRemarks"] as? String ?: "Commendable academic effort.",
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }

        fun createWithComputedMetrics(
            id: String,
            schoolId: String,
            examId: String,
            examTitle: String,
            academicSession: String,
            studentId: String,
            studentName: String,
            rollNumber: String,
            gradeLevel: String,
            section: String,
            subjectScores: List<SubjectScore>,
            teacherRemarks: String = "Satisfactory progress noted."
        ): StudentResult {
            val obtained = subjectScores.sumOf { it.marksObtained }
            val maxMarks = subjectScores.sumOf { it.totalMarks }.coerceAtLeast(1.0)
            val pct = (obtained / maxMarks) * 100.0
            val gpa = if (subjectScores.isNotEmpty()) subjectScores.map { it.gradePoint }.average() else 0.0
            val grade = GradeScale.calculate(pct).letter

            return StudentResult(
                id = id,
                schoolId = schoolId,
                examId = examId,
                examTitle = examTitle,
                academicSession = academicSession,
                studentId = studentId,
                studentName = studentName,
                rollNumber = rollNumber,
                gradeLevel = gradeLevel,
                section = section,
                subjectScores = subjectScores,
                totalObtained = obtained,
                totalMaxMarks = maxMarks,
                percentage = pct,
                gpa = gpa,
                overallGrade = grade,
                rankInClass = 0,
                status = ResultStatus.PUBLISHED,
                teacherRemarks = teacherRemarks,
                createdAt = System.currentTimeMillis()
            )
        }
    }
}

/**
 * Formal Academic Report Card / Transcript structure.
 */
data class ReportCard(
    val reportId: String,
    val schoolId: String,
    val schoolName: String,
    val schoolCode: String,
    val studentResult: StudentResult,
    val attendancePercentage: Double = 96.5,
    val principalSignature: String = "Dr. Margaret Sterling, D.Ed.",
    val issuanceDate: String,
    val honorsRemark: String = if (studentResult.percentage >= 85.0) "Dean's List / High Honors Distinction" else "Good Standing"
)
