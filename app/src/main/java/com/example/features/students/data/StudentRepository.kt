package com.example.features.students.data

import android.content.Context
import android.util.Log
import com.example.R
import com.example.core.error.AppError
import com.example.core.error.ErrorHandler
import com.example.core.tenant.TenantPathProvider
import com.example.features.students.domain.StudentProfile
import com.example.features.students.domain.StudentStatus
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

class StudentRepository(
    private val context: Context? = null,
    private val firestoreInstance: FirebaseFirestore? = null
) {
    private val db: FirebaseFirestore by lazy {
        firestoreInstance ?: try {
            val dbId = context?.getString(R.string.firestore_database_id)
            if (dbId != null) FirebaseFirestore.getInstance(dbId) else FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w("StudentRepository", "Fallback to default Firestore instance", e)
            FirebaseFirestore.getInstance()
        }
    }

    suspend fun getStudents(schoolId: String = TenantPathProvider.requireActiveSchoolId()): List<StudentProfile> {
        return try {
            TenantPathProvider.verifyTenantBoundary(schoolId)
            val path = TenantPathProvider.studentsPath(schoolId)
            val snapshot = db.collection(path).get().await()
            if (snapshot.isEmpty) {
                getPreSeededStudents(schoolId)
            } else {
                snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    StudentProfile.fromFirestoreMap(doc.id, schoolId, data)
                }
            }
        } catch (e: Exception) {
            Log.w("StudentRepository", "Error retrieving students from Firestore, loading seeded records", e)
            getPreSeededStudents(schoolId)
        }
    }

    suspend fun getStudentById(schoolId: String, studentId: String): StudentProfile? {
        return try {
            TenantPathProvider.verifyTenantBoundary(schoolId)
            val path = TenantPathProvider.studentsPath(schoolId)
            val doc = db.collection(path).document(studentId).get().await()
            if (doc.exists() && doc.data != null) {
                StudentProfile.fromFirestoreMap(doc.id, schoolId, doc.data!!)
            } else {
                getPreSeededStudents(schoolId).firstOrNull { it.id == studentId }
            }
        } catch (e: Exception) {
            getPreSeededStudents(schoolId).firstOrNull { it.id == studentId }
        }
    }

    suspend fun saveStudent(student: StudentProfile): Result<StudentProfile> {
        return try {
            TenantPathProvider.verifyTenantBoundary(student.schoolId)
            val path = TenantPathProvider.studentsPath(student.schoolId)
            db.collection(path).document(student.id).set(student.toFirestoreMap()).await()
            Result.success(student)
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    suspend fun deleteStudent(schoolId: String, studentId: String): Result<Unit> {
        return try {
            TenantPathProvider.verifyTenantBoundary(schoolId)
            val path = TenantPathProvider.studentsPath(schoolId)
            db.collection(path).document(studentId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    fun generateUniqueRollNumber(schoolId: String, gradeLevel: String, section: String): String {
        val cleanPrefix = schoolId.replace("sch_", "").take(3).uppercase()
        val gradeNum = gradeLevel.filter { it.isDigit() }.ifBlank { "10" }
        val sectionClean = section.trim().takeLast(1).uppercase().ifBlank { "A" }
        val randomNum = (100..999).random()
        return "$cleanPrefix-$gradeNum$sectionClean-$randomNum"
    }

    fun generateAdmissionNumber(schoolId: String): String {
        val cleanPrefix = schoolId.replace("sch_", "").take(3).uppercase()
        val year = 2026
        val randomSeq = (1000..9999).random()
        return "ADM-$cleanPrefix-$year-$randomSeq"
    }

    fun getPreSeededStudents(schoolId: String): List<StudentProfile> {
        return if (schoolId.contains("oxford") || schoolId.startsWith("sch_oxf")) {
            listOf(
                StudentProfile(
                    id = "stu_oxf_01",
                    schoolId = schoolId,
                    firstName = "Amina",
                    lastName = "Rahman",
                    fullName = "Amina Rahman",
                    rollNumber = "OXF-10A-001",
                    admissionNumber = "ADM-OXF-2026-1001",
                    gradeLevel = "Grade 10",
                    section = "Section A",
                    dateOfBirth = "2010-04-12",
                    gender = "Female",
                    bloodGroup = "A+",
                    status = StudentStatus.ACTIVE,
                    guardianName = "David Rahman",
                    guardianRelationship = "Father",
                    guardianPhone = "+1 555-0101",
                    guardianEmail = "parent.david@gmail.com",
                    address = "12 Beacon St, Boston, MA",
                    emergencyContact = "+1 555-0109",
                    medicalNotes = "Mild seasonal pollen allergy; carries antihistamine inhaler.",
                    enrollmentDate = "2024-08-20"
                ),
                StudentProfile(
                    id = "stu_oxf_02",
                    schoolId = schoolId,
                    firstName = "Oliver",
                    lastName = "Smith",
                    fullName = "Oliver Smith",
                    rollNumber = "OXF-10A-002",
                    admissionNumber = "ADM-OXF-2026-1002",
                    gradeLevel = "Grade 10",
                    section = "Section A",
                    dateOfBirth = "2010-09-25",
                    gender = "Male",
                    bloodGroup = "O+",
                    status = StudentStatus.ACTIVE,
                    guardianName = "Margaret Smith",
                    guardianRelationship = "Mother",
                    guardianPhone = "+1 555-0102",
                    guardianEmail = "smith.family@gmail.com",
                    address = "45 Commonwealth Ave, Boston, MA",
                    emergencyContact = "+1 555-0108",
                    medicalNotes = "None reported.",
                    enrollmentDate = "2024-08-20"
                ),
                StudentProfile(
                    id = "stu_oxf_03",
                    schoolId = schoolId,
                    firstName = "Sophia",
                    lastName = "Chen",
                    fullName = "Sophia Chen",
                    rollNumber = "OXF-11S-003",
                    admissionNumber = "ADM-OXF-2026-1003",
                    gradeLevel = "Grade 11",
                    section = "Science",
                    dateOfBirth = "2009-02-18",
                    gender = "Female",
                    bloodGroup = "B+",
                    status = StudentStatus.ACTIVE,
                    guardianName = "Kevin Chen",
                    guardianRelationship = "Father",
                    guardianPhone = "+1 555-0103",
                    guardianEmail = "chen.corp@gmail.com",
                    address = "88 Newbury St, Boston, MA",
                    emergencyContact = "+1 555-0107",
                    medicalNotes = "Severe peanut allergy; EpiPen kept in school infirmary.",
                    enrollmentDate = "2023-08-15"
                ),
                StudentProfile(
                    id = "stu_oxf_04",
                    schoolId = schoolId,
                    firstName = "Lucas",
                    lastName = "Vance",
                    fullName = "Lucas Vance",
                    rollNumber = "OXF-09B-004",
                    admissionNumber = "ADM-OXF-2026-1004",
                    gradeLevel = "Grade 9",
                    section = "Section B",
                    dateOfBirth = "2011-11-03",
                    gender = "Male",
                    bloodGroup = "AB-",
                    status = StudentStatus.ACTIVE,
                    guardianName = "Dr. Marcus Vance",
                    guardianRelationship = "Father",
                    guardianPhone = "+1 555-0104",
                    guardianEmail = "vance.parent@gmail.com",
                    address = "104 Harvard Square, Cambridge, MA",
                    emergencyContact = "+1 555-0106",
                    medicalNotes = "Wears corrective eyewear.",
                    enrollmentDate = "2025-08-22"
                ),
                StudentProfile(
                    id = "stu_oxf_05",
                    schoolId = schoolId,
                    firstName = "Maya",
                    lastName = "Patel",
                    fullName = "Maya Patel",
                    rollNumber = "OXF-12A-005",
                    admissionNumber = "ADM-OXF-2026-1005",
                    gradeLevel = "Grade 12",
                    section = "Section A",
                    dateOfBirth = "2008-07-14",
                    gender = "Female",
                    bloodGroup = "O-",
                    status = StudentStatus.ACTIVE,
                    guardianName = "Suresh Patel",
                    guardianRelationship = "Father",
                    guardianPhone = "+1 555-0105",
                    guardianEmail = "patel.s@gmail.com",
                    address = "230 Boylston St, Boston, MA",
                    emergencyContact = "+1 555-0110",
                    medicalNotes = "None reported.",
                    enrollmentDate = "2022-08-18"
                )
            )
        } else {
            listOf(
                StudentProfile(
                    id = "stu_cam_01",
                    schoolId = schoolId,
                    firstName = "Liam",
                    lastName = "Davies",
                    fullName = "Liam Davies",
                    rollNumber = "CAM-10C-101",
                    admissionNumber = "ADM-CAM-2026-2001",
                    gradeLevel = "Grade 10",
                    section = "Section A",
                    dateOfBirth = "2010-06-30",
                    gender = "Male",
                    bloodGroup = "A-",
                    status = StudentStatus.ACTIVE,
                    guardianName = "Arthur Davies",
                    guardianRelationship = "Father",
                    guardianPhone = "+44 20 7946 0991",
                    guardianEmail = "davies@cambridge.co.uk",
                    address = "14 Trinity Lane, Cambridge, UK",
                    emergencyContact = "+44 20 7946 0999",
                    medicalNotes = "Asthma inhaler prescribed for sports.",
                    enrollmentDate = "2024-09-01"
                ),
                StudentProfile(
                    id = "stu_cam_02",
                    schoolId = schoolId,
                    firstName = "Emma",
                    lastName = "Watson",
                    fullName = "Emma Watson",
                    rollNumber = "CAM-11A-102",
                    admissionNumber = "ADM-CAM-2026-2002",
                    gradeLevel = "Grade 11",
                    section = "Section B",
                    dateOfBirth = "2009-08-12",
                    gender = "Female",
                    bloodGroup = "O+",
                    status = StudentStatus.ACTIVE,
                    guardianName = "Jacqueline Watson",
                    guardianRelationship = "Mother",
                    guardianPhone = "+44 20 7946 0992",
                    guardianEmail = "watson@cambridge.co.uk",
                    address = "32 King's Parade, Cambridge, UK",
                    emergencyContact = "+44 20 7946 0998",
                    medicalNotes = "None reported.",
                    enrollmentDate = "2023-09-01"
                ),
                StudentProfile(
                    id = "stu_cam_03",
                    schoolId = schoolId,
                    firstName = "Noah",
                    lastName = "Wilson",
                    fullName = "Noah Wilson",
                    rollNumber = "CAM-12S-103",
                    admissionNumber = "ADM-CAM-2026-2003",
                    gradeLevel = "Grade 12",
                    section = "Science",
                    dateOfBirth = "2008-12-05",
                    gender = "Male",
                    bloodGroup = "B+",
                    status = StudentStatus.ACTIVE,
                    guardianName = "Eleanor Wilson",
                    guardianRelationship = "Mother",
                    guardianPhone = "+44 20 7946 0993",
                    guardianEmail = "wilson@cambridge.co.uk",
                    address = "5 St John's St, Cambridge, UK",
                    emergencyContact = "+44 20 7946 0997",
                    medicalNotes = "None reported.",
                    enrollmentDate = "2022-09-01"
                )
            )
        }
    }
}
