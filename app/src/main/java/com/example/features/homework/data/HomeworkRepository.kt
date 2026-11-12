package com.example.features.homework.data

import android.content.Context
import android.util.Log
import com.example.R
import com.example.core.error.ErrorHandler
import com.example.core.tenant.TenantPathProvider
import com.example.features.homework.domain.HomeworkAssignment
import com.example.features.homework.domain.HomeworkStatus
import com.example.features.homework.domain.HomeworkSubmission
import com.example.features.homework.domain.SubmissionStatus
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class HomeworkRepository(
    private val context: Context? = null,
    private val firestoreInstance: FirebaseFirestore? = null
) {
    private val db: FirebaseFirestore by lazy {
        firestoreInstance ?: try {
            val dbId = context?.getString(R.string.firestore_database_id)
            if (dbId != null) FirebaseFirestore.getInstance(dbId) else FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w("HomeworkRepository", "Fallback to default Firestore instance", e)
            FirebaseFirestore.getInstance()
        }
    }

    suspend fun getHomeworkList(
        schoolId: String = TenantPathProvider.requireActiveSchoolId(),
        gradeLevel: String? = null,
        subjectCode: String? = null,
        status: HomeworkStatus? = null
    ): List<HomeworkAssignment> {
        return try {
            TenantPathProvider.verifyTenantBoundary(schoolId)
            val path = TenantPathProvider.homeworkPath(schoolId)
            val snapshot = db.collection(path).get().await()
            val list = if (snapshot.isEmpty) {
                getPreSeededHomework(schoolId)
            } else {
                snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    HomeworkAssignment.fromFirestoreMap(doc.id, schoolId, data)
                }
            }
            filterHomework(list, gradeLevel, subjectCode, status)
        } catch (e: Exception) {
            Log.w("HomeworkRepository", "Error retrieving homework from Firestore, using preseeded", e)
            val seeded = getPreSeededHomework(schoolId)
            filterHomework(seeded, gradeLevel, subjectCode, status)
        }
    }

    private fun filterHomework(
        list: List<HomeworkAssignment>,
        gradeLevel: String?,
        subjectCode: String?,
        status: HomeworkStatus?
    ): List<HomeworkAssignment> {
        return list.filter { hw ->
            (gradeLevel == null || hw.gradeLevel.contains(gradeLevel, ignoreCase = true)) &&
            (subjectCode == null || hw.subjectCode.equals(subjectCode, ignoreCase = true)) &&
            (status == null || hw.status == status)
        }
    }

    suspend fun saveHomework(assignment: HomeworkAssignment): Result<HomeworkAssignment> {
        return try {
            TenantPathProvider.verifyTenantBoundary(assignment.schoolId)
            val path = TenantPathProvider.homeworkPath(assignment.schoolId)
            db.collection(path).document(assignment.id).set(assignment.toFirestoreMap()).await()
            Result.success(assignment)
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    suspend fun submitHomework(
        schoolId: String,
        homeworkId: String,
        submission: HomeworkSubmission
    ): Result<HomeworkAssignment> {
        return try {
            TenantPathProvider.verifyTenantBoundary(schoolId)
            val currentList = getHomeworkList(schoolId)
            val target = currentList.firstOrNull { it.id == homeworkId }
                ?: return Result.failure(Exception("Assignment not found"))

            val existingFiltered = target.submissions.filterNot { it.studentId == submission.studentId }
            val updatedSubmissions = existingFiltered + submission
            val updatedAssignment = target.copy(submissions = updatedSubmissions)

            saveHomework(updatedAssignment)
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    suspend fun gradeSubmission(
        schoolId: String,
        homeworkId: String,
        studentId: String,
        marks: Double,
        feedback: String,
        evaluatorName: String
    ): Result<HomeworkAssignment> {
        return try {
            TenantPathProvider.verifyTenantBoundary(schoolId)
            val currentList = getHomeworkList(schoolId)
            val target = currentList.firstOrNull { it.id == homeworkId }
                ?: return Result.failure(Exception("Assignment not found"))

            val updatedSubmissions = target.submissions.map { sub ->
                if (sub.studentId == studentId) {
                    sub.copy(
                        marksAwarded = marks,
                        feedback = feedback,
                        status = SubmissionStatus.GRADED,
                        evaluatedBy = evaluatorName,
                        evaluatedAt = System.currentTimeMillis()
                    )
                } else sub
            }

            val updatedAssignment = target.copy(submissions = updatedSubmissions)
            saveHomework(updatedAssignment)
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    suspend fun deleteHomework(schoolId: String, homeworkId: String): Result<Unit> {
        return try {
            TenantPathProvider.verifyTenantBoundary(schoolId)
            val path = TenantPathProvider.homeworkPath(schoolId)
            db.collection(path).document(homeworkId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    fun getPreSeededHomework(schoolId: String): List<HomeworkAssignment> {
        return if (schoolId.contains("oxford") || schoolId.startsWith("sch_oxf")) {
            listOf(
                HomeworkAssignment(
                    id = "hw_oxf_phy_01",
                    schoolId = schoolId,
                    title = "Lab Report: Simple Harmonic Motion & Pendulum Dynamics",
                    description = "Analyze the empirical oscillation data gathered during Monday's lab experiment. Calculate gravitational acceleration 'g' with uncertainty propagation and compare with theoretical 9.81 m/s².",
                    subjectCode = "PHY-101",
                    subjectName = "Advanced Physics",
                    gradeLevel = "Grade 10",
                    section = "Section A",
                    assignedDate = "2026-03-23",
                    dueDate = "2026-03-30",
                    totalMarks = 50.0,
                    teacherId = "tea_oxf_01",
                    teacherName = "Dr. Sarah Jenkins",
                    status = HomeworkStatus.ASSIGNED,
                    attachmentTitle = "SHM_Experiment_DataSheet.xlsx",
                    totalEnrolledStudents = 32,
                    submissions = listOf(
                        HomeworkSubmission(
                            studentId = "stu_oxf_01",
                            studentName = "Amina Rahman",
                            rollNumber = "OXF-10A-001",
                            submissionDate = "2026-03-24",
                            attachmentTitle = "Amina_Rahman_Physics_SHM_Report.pdf",
                            studentNotes = "Calculated g = 9.79 ± 0.04 m/s² using regression analysis in Python.",
                            status = SubmissionStatus.GRADED,
                            marksAwarded = 49.0,
                            feedback = "Outstanding error propagation and clear graph plotting.",
                            evaluatedBy = "Dr. Sarah Jenkins",
                            evaluatedAt = 1711280000000L
                        ),
                        HomeworkSubmission(
                            studentId = "stu_oxf_02",
                            studentName = "Oliver Smith",
                            rollNumber = "OXF-10A-002",
                            submissionDate = "2026-03-25",
                            attachmentTitle = "Oliver_Smith_Lab_Report.pdf",
                            studentNotes = "Included all manual pendulum timer readings in Appendix A.",
                            status = SubmissionStatus.SUBMITTED,
                            feedback = "",
                            evaluatedBy = ""
                        )
                    )
                ),
                HomeworkAssignment(
                    id = "hw_oxf_mat_02",
                    schoolId = schoolId,
                    title = "Problem Set 5: Integral Calculus Applications",
                    description = "Solve Chapter 7 problems 14 through 28 on definite integrals, area between two curves, and solids of revolution using disk/washer methods. Show full derivative derivations.",
                    subjectCode = "MTH-102",
                    subjectName = "Pure Mathematics",
                    gradeLevel = "Grade 10",
                    section = "Section A",
                    assignedDate = "2026-03-20",
                    dueDate = "2026-03-27",
                    totalMarks = 40.0,
                    teacherId = "tea_oxf_02",
                    teacherName = "Marcus Aurelius Vance",
                    status = HomeworkStatus.DUE_SOON,
                    attachmentTitle = "Calculus_Problem_Set_5.pdf",
                    totalEnrolledStudents = 32,
                    submissions = listOf(
                        HomeworkSubmission(
                            studentId = "stu_oxf_01",
                            studentName = "Amina Rahman",
                            rollNumber = "OXF-10A-001",
                            submissionDate = "2026-03-22",
                            attachmentTitle = "Amina_Math_PS5.pdf",
                            studentNotes = "All 15 problems solved with step-by-step bounds verification.",
                            status = SubmissionStatus.GRADED,
                            marksAwarded = 40.0,
                            feedback = "Impeccable calculus proofs and neat presentation.",
                            evaluatedBy = "Marcus Aurelius Vance",
                            evaluatedAt = 1711190000000L
                        )
                    )
                ),
                HomeworkAssignment(
                    id = "hw_oxf_eng_03",
                    schoolId = schoolId,
                    title = "Critical Essay: Dramatic Irony in Shakespeare's Macbeth",
                    description = "Write a 1,200-word comparative essay analyzing how dramatic irony heightens psychological tension in Acts I and II. Cite at least 4 textual passages with line references.",
                    subjectCode = "ENG-103",
                    subjectName = "English Literature",
                    gradeLevel = "Grade 10",
                    section = "Section A",
                    assignedDate = "2026-03-18",
                    dueDate = "2026-03-25",
                    totalMarks = 50.0,
                    teacherId = "tea_oxf_03",
                    teacherName = "Helena Rostova",
                    status = HomeworkStatus.CLOSED,
                    attachmentTitle = "Macbeth_Essay_Prompt_Rubric.pdf",
                    totalEnrolledStudents = 32,
                    submissions = listOf(
                        HomeworkSubmission(
                            studentId = "stu_oxf_01",
                            studentName = "Amina Rahman",
                            rollNumber = "OXF-10A-001",
                            submissionDate = "2026-03-21",
                            attachmentTitle = "Amina_Macbeth_Essay_Final.docx",
                            studentNotes = "Focused on the dagger soliloquy and Banquo's foreboding dialogue.",
                            status = SubmissionStatus.GRADED,
                            marksAwarded = 48.0,
                            feedback = "Magnificent rhetorical analysis and nuanced literary depth.",
                            evaluatedBy = "Helena Rostova",
                            evaluatedAt = 1711100000000L
                        ),
                        HomeworkSubmission(
                            studentId = "stu_oxf_03",
                            studentName = "Sophia Chen",
                            rollNumber = "OXF-10A-003",
                            submissionDate = "2026-03-22",
                            attachmentTitle = "Sophia_Chen_Macbeth_Irony.docx",
                            studentNotes = "Analyzed Lady Macbeth's invocation of spirits.",
                            status = SubmissionStatus.GRADED,
                            marksAwarded = 46.0,
                            feedback = "Insightful character motivation study.",
                            evaluatedBy = "Helena Rostova",
                            evaluatedAt = 1711100000000L
                        )
                    )
                )
            )
        } else {
            listOf(
                HomeworkAssignment(
                    id = "hw_cam_lit_01",
                    schoolId = schoolId,
                    title = "Critical Commentary: British Romanticism & Ecology",
                    description = "Examine Wordsworth's 'Tintern Abbey' through an ecocritical lens in 1,000 words.",
                    subjectCode = "ENG-201",
                    subjectName = "British Literature & Rhetoric",
                    gradeLevel = "Year 10",
                    section = "Section Alpha",
                    assignedDate = "2026-03-22",
                    dueDate = "2026-03-29",
                    totalMarks = 50.0,
                    teacherId = "tea_cam_01",
                    teacherName = "Prof. Alistair Finch",
                    status = HomeworkStatus.ASSIGNED,
                    attachmentTitle = "Romantic_Poetry_Readings.pdf",
                    totalEnrolledStudents = 28,
                    submissions = listOf(
                        HomeworkSubmission(
                            studentId = "stu_cam_01",
                            studentName = "Elizabeth Bennet",
                            rollNumber = "CAM-10A-001",
                            submissionDate = "2026-03-24",
                            attachmentTitle = "Elizabeth_Ecocriticism_Essay.pdf",
                            studentNotes = "Explored human-nature dichotomy in Wordsworth.",
                            status = SubmissionStatus.SUBMITTED
                        )
                    )
                )
            )
        }
    }
}
