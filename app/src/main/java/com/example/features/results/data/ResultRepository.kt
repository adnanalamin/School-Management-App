package com.example.features.results.data

import android.content.Context
import android.util.Log
import com.example.R
import com.example.core.error.ErrorHandler
import com.example.core.tenant.TenantPathProvider
import com.example.features.results.domain.GradeScale
import com.example.features.results.domain.ReportCard
import com.example.features.results.domain.ResultStatus
import com.example.features.results.domain.StudentResult
import com.example.features.results.domain.SubjectScore
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ResultRepository(
    private val context: Context? = null,
    private val firestoreInstance: FirebaseFirestore? = null
) {
    private val db: FirebaseFirestore by lazy {
        firestoreInstance ?: try {
            val dbId = context?.getString(R.string.firestore_database_id)
            if (dbId != null) FirebaseFirestore.getInstance(dbId) else FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w("ResultRepository", "Fallback to default Firestore instance", e)
            FirebaseFirestore.getInstance()
        }
    }

    suspend fun getResults(
        schoolId: String = TenantPathProvider.requireActiveSchoolId(),
        examId: String? = null,
        gradeLevel: String? = null,
        status: ResultStatus? = null
    ): List<StudentResult> {
        return try {
            TenantPathProvider.verifyTenantBoundary(schoolId)
            val path = TenantPathProvider.resultsPath(schoolId)
            val snapshot = db.collection(path).get().await()
            val list = if (snapshot.isEmpty) {
                getPreSeededResults(schoolId)
            } else {
                snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    StudentResult.fromFirestoreMap(doc.id, schoolId, data)
                }
            }
            filterAndRankResults(list, examId, gradeLevel, status)
        } catch (e: Exception) {
            Log.w("ResultRepository", "Error retrieving results from Firestore, fallback to preseeded", e)
            val seeded = getPreSeededResults(schoolId)
            filterAndRankResults(seeded, examId, gradeLevel, status)
        }
    }

    private fun filterAndRankResults(
        results: List<StudentResult>,
        examId: String?,
        gradeLevel: String?,
        status: ResultStatus?
    ): List<StudentResult> {
        val filtered = results.filter { res ->
            (examId == null || res.examId == examId) &&
            (gradeLevel == null || res.gradeLevel.contains(gradeLevel, ignoreCase = true)) &&
            (status == null || res.status == status)
        }
        // Compute relative rankings based on percentage descending
        val sorted = filtered.sortedByDescending { it.percentage }
        return sorted.mapIndexed { index, item ->
            item.copy(rankInClass = index + 1)
        }
    }

    suspend fun saveResult(result: StudentResult): Result<StudentResult> {
        return try {
            TenantPathProvider.verifyTenantBoundary(result.schoolId)
            val path = TenantPathProvider.resultsPath(result.schoolId)
            db.collection(path).document(result.id).set(result.toFirestoreMap()).await()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    suspend fun publishAllResults(schoolId: String, examId: String): Result<Unit> {
        return try {
            TenantPathProvider.verifyTenantBoundary(schoolId)
            val results = getResults(schoolId = schoolId, examId = examId)
            val path = TenantPathProvider.resultsPath(schoolId)
            for (res in results) {
                val published = res.copy(status = ResultStatus.PUBLISHED)
                db.collection(path).document(published.id).set(published.toFirestoreMap()).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    fun generateReportCard(
        schoolId: String,
        result: StudentResult,
        attendanceRate: Double = 96.5
    ): ReportCard {
        val isCambridge = schoolId.contains("cambridge")
        val schoolName = if (isCambridge) "Cambridge High School" else "Oxford Academy"
        val schoolCode = if (isCambridge) "CAM-101" else "OXF-2026"
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.US)

        return ReportCard(
            reportId = "RC-${result.rollNumber}-${result.academicSession.take(4)}",
            schoolId = schoolId,
            schoolName = schoolName,
            schoolCode = schoolCode,
            studentResult = result,
            attendancePercentage = attendanceRate,
            principalSignature = if (isCambridge) "Dr. Charles Darwin, FRS" else "Dr. Margaret Sterling, D.Ed.",
            issuanceDate = dateFormat.format(Date()),
            honorsRemark = when {
                result.percentage >= 90.0 -> "Summa Cum Laude / High Distinction"
                result.percentage >= 80.0 -> "Magna Cum Laude / Merit Honors"
                result.percentage >= 60.0 -> "Good Academic Standing"
                else -> "Academic Support Recommended"
            }
        )
    }

    fun getPreSeededResults(schoolId: String): List<StudentResult> {
        return if (schoolId.contains("oxford") || schoolId.startsWith("sch_oxf")) {
            listOf(
                StudentResult(
                    id = "res_oxf_01",
                    schoolId = schoolId,
                    examId = "ex_oxf_mid_g10",
                    examTitle = "Midterm Examination Series 2026",
                    academicSession = "2026-2027",
                    studentId = "stu_oxf_01",
                    studentName = "Amina Rahman",
                    rollNumber = "OXF-10A-001",
                    gradeLevel = "Grade 10",
                    section = "Section A",
                    subjectScores = listOf(
                        SubjectScore("PHY-101", "Advanced Physics", 94.0, 100.0, "A+", 4.0, "Exceptional laboratory and theory mastery"),
                        SubjectScore("MTH-102", "Pure Mathematics", 98.0, 100.0, "A+", 4.0, "Flawless calculus proofs"),
                        SubjectScore("ENG-103", "English Literature", 88.0, 100.0, "A", 3.7, "Insightful essay construction"),
                        SubjectScore("CHM-104", "Chemistry Practical Lab", 48.0, 50.0, "A+", 4.0, "Precise titration analysis")
                    ),
                    totalObtained = 328.0,
                    totalMaxMarks = 350.0,
                    percentage = 93.71,
                    gpa = 3.92,
                    overallGrade = "A+",
                    rankInClass = 1,
                    status = ResultStatus.PUBLISHED,
                    teacherRemarks = "Exemplary scholastic dedication and leadership in STEM projects."
                ),
                StudentResult(
                    id = "res_oxf_02",
                    schoolId = schoolId,
                    examId = "ex_oxf_mid_g10",
                    examTitle = "Midterm Examination Series 2026",
                    academicSession = "2026-2027",
                    studentId = "stu_oxf_02",
                    studentName = "Oliver Smith",
                    rollNumber = "OXF-10A-002",
                    gradeLevel = "Grade 10",
                    section = "Section A",
                    subjectScores = listOf(
                        SubjectScore("PHY-101", "Advanced Physics", 82.0, 100.0, "A", 3.7, "Strong grasp of mechanics"),
                        SubjectScore("MTH-102", "Pure Mathematics", 79.0, 100.0, "B+", 3.3, "Good problem solving in trigonometry"),
                        SubjectScore("ENG-103", "English Literature", 85.0, 100.0, "A", 3.7, "Excellent critical reading"),
                        SubjectScore("CHM-104", "Chemistry Practical Lab", 42.0, 50.0, "A", 3.7, "Diligent lab experiment notes")
                    ),
                    totalObtained = 288.0,
                    totalMaxMarks = 350.0,
                    percentage = 82.28,
                    gpa = 3.60,
                    overallGrade = "A",
                    rankInClass = 2,
                    status = ResultStatus.PUBLISHED,
                    teacherRemarks = "Solid analytical skills. Keep striving for distinction in mathematics."
                ),
                StudentResult(
                    id = "res_oxf_03",
                    schoolId = schoolId,
                    examId = "ex_oxf_mid_g10",
                    examTitle = "Midterm Examination Series 2026",
                    academicSession = "2026-2027",
                    studentId = "stu_oxf_03",
                    studentName = "Sophia Chen",
                    rollNumber = "OXF-10A-003",
                    gradeLevel = "Grade 10",
                    section = "Section A",
                    subjectScores = listOf(
                        SubjectScore("PHY-101", "Advanced Physics", 72.0, 100.0, "B+", 3.3, "Satisfactory electromagnetism"),
                        SubjectScore("MTH-102", "Pure Mathematics", 68.0, 100.0, "B", 3.0, "Needs more calculus drill"),
                        SubjectScore("ENG-103", "English Literature", 91.0, 100.0, "A+", 4.0, "Brilliant creative writing"),
                        SubjectScore("CHM-104", "Chemistry Practical Lab", 38.0, 50.0, "B+", 3.3, "Good safety and procedure adherence")
                    ),
                    totalObtained = 269.0,
                    totalMaxMarks = 350.0,
                    percentage = 76.85,
                    gpa = 3.40,
                    overallGrade = "B+",
                    rankInClass = 3,
                    status = ResultStatus.PUBLISHED,
                    teacherRemarks = "Outstanding linguistic creativity. Recommend math peer-study workshops."
                )
            )
        } else {
            listOf(
                StudentResult(
                    id = "res_cam_01",
                    schoolId = schoolId,
                    examId = "ex_cam_mic_y10",
                    examTitle = "Michaelmas Term Examinations 2026",
                    academicSession = "2026-2027",
                    studentId = "stu_cam_01",
                    studentName = "Elizabeth Bennet",
                    rollNumber = "CAM-10A-001",
                    gradeLevel = "Year 10",
                    section = "Section Alpha",
                    subjectScores = listOf(
                        SubjectScore("PHY-301", "Classical Mechanics & Calculus", 92.0, 100.0, "A+", 4.0, "Superb mathematical physics"),
                        SubjectScore("ENG-201", "British Literature & Rhetoric", 96.0, 100.0, "A+", 4.0, "Distinguished prose analysis")
                    ),
                    totalObtained = 188.0,
                    totalMaxMarks = 200.0,
                    percentage = 94.0,
                    gpa = 4.0,
                    overallGrade = "A+",
                    rankInClass = 1,
                    status = ResultStatus.PUBLISHED,
                    teacherRemarks = "Superlative scholarly achievement in all disciplines."
                )
            )
        }
    }
}
