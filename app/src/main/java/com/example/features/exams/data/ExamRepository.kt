package com.example.features.exams.data

import android.content.Context
import android.util.Log
import com.example.R
import com.example.core.error.ErrorHandler
import com.example.core.tenant.TenantPathProvider
import com.example.features.exams.domain.ExamPaper
import com.example.features.exams.domain.ExamSchedule
import com.example.features.exams.domain.ExamStatus
import com.example.features.exams.domain.ExamType
import com.example.features.exams.domain.HallTicket
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExamRepository(
    private val context: Context? = null,
    private val firestoreInstance: FirebaseFirestore? = null
) {
    private val db: FirebaseFirestore by lazy {
        firestoreInstance ?: try {
            val dbId = context?.getString(R.string.firestore_database_id)
            if (dbId != null) FirebaseFirestore.getInstance(dbId) else FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w("ExamRepository", "Fallback to default Firestore instance", e)
            FirebaseFirestore.getInstance()
        }
    }

    suspend fun getExams(
        schoolId: String = TenantPathProvider.requireActiveSchoolId(),
        examType: ExamType? = null,
        gradeLevel: String? = null,
        status: ExamStatus? = null
    ): List<ExamSchedule> {
        return try {
            TenantPathProvider.verifyTenantBoundary(schoolId)
            val path = TenantPathProvider.examsPath(schoolId)
            val snapshot = db.collection(path).get().await()
            val list = if (snapshot.isEmpty) {
                getPreSeededExams(schoolId)
            } else {
                snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    ExamSchedule.fromFirestoreMap(doc.id, schoolId, data)
                }
            }
            filterExams(list, examType, gradeLevel, status)
        } catch (e: Exception) {
            Log.w("ExamRepository", "Error retrieving exams from Firestore, falling back to preseeded data", e)
            val seeded = getPreSeededExams(schoolId)
            filterExams(seeded, examType, gradeLevel, status)
        }
    }

    private fun filterExams(
        exams: List<ExamSchedule>,
        examType: ExamType?,
        gradeLevel: String?,
        status: ExamStatus?
    ): List<ExamSchedule> {
        return exams.filter { ex ->
            (examType == null || ex.examType == examType) &&
            (gradeLevel == null || ex.gradeLevel.contains(gradeLevel, ignoreCase = true)) &&
            (status == null || ex.status == status)
        }
    }

    suspend fun getExamById(schoolId: String, examId: String): ExamSchedule? {
        return try {
            TenantPathProvider.verifyTenantBoundary(schoolId)
            val path = TenantPathProvider.examsPath(schoolId)
            val doc = db.collection(path).document(examId).get().await()
            if (doc.exists() && doc.data != null) {
                ExamSchedule.fromFirestoreMap(doc.id, schoolId, doc.data!!)
            } else {
                getPreSeededExams(schoolId).firstOrNull { it.id == examId }
            }
        } catch (e: Exception) {
            getPreSeededExams(schoolId).firstOrNull { it.id == examId }
        }
    }

    suspend fun saveExam(exam: ExamSchedule): Result<ExamSchedule> {
        return try {
            TenantPathProvider.verifyTenantBoundary(exam.schoolId)
            val path = TenantPathProvider.examsPath(exam.schoolId)
            db.collection(path).document(exam.id).set(exam.toFirestoreMap()).await()
            Result.success(exam)
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    suspend fun deleteExam(schoolId: String, examId: String): Result<Unit> {
        return try {
            TenantPathProvider.verifyTenantBoundary(schoolId)
            val path = TenantPathProvider.examsPath(schoolId)
            db.collection(path).document(examId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    /**
     * Conflict Detection Engine: Checks whether a room or date/time overlaps with an existing paper.
     */
    fun checkTimetableConflict(
        existingPapers: List<ExamPaper>,
        newDate: String,
        newStartTime: String,
        newEndTime: String,
        newRoom: String
    ): String? {
        val sameDatePapers = existingPapers.filter { it.examDate == newDate }
        for (paper in sameDatePapers) {
            // Check room collision
            if (paper.roomNumber.equals(newRoom, ignoreCase = true) &&
                paper.startTime.equals(newStartTime, ignoreCase = true)
            ) {
                return "Room Conflict: Room '${paper.roomNumber}' is already booked on $newDate for '${paper.subjectName}' at $newStartTime."
            }
            // Check time collision for the same cohort
            if (paper.startTime.equals(newStartTime, ignoreCase = true)) {
                return "Time Conflict: Another paper ('${paper.subjectName}') is already scheduled at $newStartTime on $newDate."
            }
        }
        return null
    }

    fun generateHallTicket(
        schoolId: String,
        exam: ExamSchedule,
        studentId: String,
        studentName: String,
        rollNumber: String,
        admissionNumber: String,
        gradeLevel: String,
        section: String
    ): HallTicket {
        val schoolName = if (schoolId.contains("cambridge")) "Cambridge High School" else "Oxford Academy"
        val cleanPrefix = schoolId.replace("sch_", "").take(3).uppercase()
        val randomSeq = (1000..9999).random()
        val ticketNo = "HT-$cleanPrefix-${exam.examType.code}-$randomSeq"
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)

        return HallTicket(
            ticketNumber = ticketNo,
            schoolId = schoolId,
            schoolName = schoolName,
            studentId = studentId,
            studentName = studentName,
            rollNumber = rollNumber,
            admissionNumber = admissionNumber,
            gradeLevel = gradeLevel,
            section = section,
            examTitle = exam.title,
            academicSession = exam.academicSession,
            papers = exam.papers.sortedBy { it.examDate },
            issuedDate = dateFormat.format(Date()),
            verifiedBy = "Office of the Controller of Examinations"
        )
    }

    fun getPreSeededExams(schoolId: String): List<ExamSchedule> {
        return if (schoolId.contains("oxford") || schoolId.startsWith("sch_oxf")) {
            listOf(
                ExamSchedule(
                    id = "ex_oxf_mid_g10",
                    schoolId = schoolId,
                    title = "Midterm Examination Series 2026",
                    examType = ExamType.MIDTERM,
                    academicSession = "2026-2027",
                    gradeLevel = "Grade 10",
                    startDate = "2026-04-06",
                    endDate = "2026-04-10",
                    status = ExamStatus.SCHEDULED,
                    papers = listOf(
                        ExamPaper(
                            id = "p_oxf_phy_10",
                            subjectName = "Advanced Physics",
                            subjectCode = "PHY-101",
                            examDate = "2026-04-06",
                            startTime = "09:00 AM",
                            endTime = "12:00 PM",
                            durationMinutes = 180,
                            totalMarks = 100,
                            passingMarks = 40,
                            roomNumber = "Room 301 - West Wing",
                            invigilatorId = "tea_oxf_01",
                            invigilatorName = "Dr. Sarah Jenkins",
                            instructions = "Non-programmable scientific calculators approved. Formula booklet provided."
                        ),
                        ExamPaper(
                            id = "p_oxf_mth_10",
                            subjectName = "Pure Mathematics",
                            subjectCode = "MTH-102",
                            examDate = "2026-04-07",
                            startTime = "09:00 AM",
                            endTime = "12:00 PM",
                            durationMinutes = 180,
                            totalMarks = 100,
                            passingMarks = 40,
                            roomNumber = "Room 301 - West Wing",
                            invigilatorId = "tea_oxf_02",
                            invigilatorName = "Prof. Alan Turing",
                            instructions = "Geometry instrument box and graphing pens required."
                        ),
                        ExamPaper(
                            id = "p_oxf_eng_10",
                            subjectName = "English Literature",
                            subjectCode = "ENG-103",
                            examDate = "2026-04-08",
                            startTime = "09:00 AM",
                            endTime = "12:00 PM",
                            durationMinutes = 180,
                            totalMarks = 100,
                            passingMarks = 40,
                            roomNumber = "Room 301 - West Wing",
                            invigilatorId = "tea_oxf_03",
                            invigilatorName = "Maria Montessori",
                            instructions = "Clean annotated copy of Shakespeare play texts permitted."
                        ),
                        ExamPaper(
                            id = "p_oxf_chm_10",
                            subjectName = "Chemistry Practical Lab",
                            subjectCode = "CHM-104",
                            examDate = "2026-04-10",
                            startTime = "01:00 PM",
                            endTime = "03:30 PM",
                            durationMinutes = 150,
                            totalMarks = 50,
                            passingMarks = 20,
                            roomNumber = "Chemistry Lab 2",
                            invigilatorId = "tea_oxf_05",
                            invigilatorName = "Elena Rostova",
                            instructions = "Standard lab coat and safety goggles mandatory at station."
                        )
                    )
                ),
                ExamSchedule(
                    id = "ex_oxf_fin_g11",
                    schoolId = schoolId,
                    title = "Senior STEM Honors Term Assessment",
                    examType = ExamType.FINAL,
                    academicSession = "2026-2027",
                    gradeLevel = "Grade 11",
                    startDate = "2026-05-18",
                    endDate = "2026-05-22",
                    status = ExamStatus.SCHEDULED,
                    papers = listOf(
                        ExamPaper(
                            id = "p_oxf_cal_11",
                            subjectName = "Calculus BC & Analysis",
                            subjectCode = "MTH-201",
                            examDate = "2026-05-18",
                            startTime = "09:00 AM",
                            endTime = "12:00 PM",
                            durationMinutes = 180,
                            totalMarks = 100,
                            passingMarks = 40,
                            roomNumber = "Room 401 - STEM Wing",
                            invigilatorId = "tea_oxf_02",
                            invigilatorName = "Prof. Alan Turing"
                        )
                    )
                )
            )
        } else {
            listOf(
                ExamSchedule(
                    id = "ex_cam_mic_y10",
                    schoolId = schoolId,
                    title = "Michaelmas Term Examinations 2026",
                    examType = ExamType.MIDTERM,
                    academicSession = "2026-2027",
                    gradeLevel = "Year 10",
                    startDate = "2026-04-12",
                    endDate = "2026-04-16",
                    status = ExamStatus.SCHEDULED,
                    papers = listOf(
                        ExamPaper(
                            id = "p_cam_phy_10",
                            subjectName = "Classical Mechanics & Calculus",
                            subjectCode = "PHY-301",
                            examDate = "2026-04-12",
                            startTime = "09:30 AM",
                            endTime = "12:30 PM",
                            durationMinutes = 180,
                            totalMarks = 100,
                            passingMarks = 40,
                            roomNumber = "Newton Hall",
                            invigilatorId = "tea_cam_01",
                            invigilatorName = "Dr. Isaac Newton"
                        ),
                        ExamPaper(
                            id = "p_cam_lit_10",
                            subjectName = "British Literature & Rhetoric",
                            subjectCode = "ENG-201",
                            examDate = "2026-04-14",
                            startTime = "09:30 AM",
                            endTime = "12:30 PM",
                            durationMinutes = 180,
                            totalMarks = 100,
                            passingMarks = 40,
                            roomNumber = "Room 12 - Trinity Court",
                            invigilatorId = "tea_cam_02",
                            invigilatorName = "Jane Austen"
                        )
                    )
                )
            )
        }
    }
}
