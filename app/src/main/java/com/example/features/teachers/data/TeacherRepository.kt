package com.example.features.teachers.data

import android.content.Context
import android.util.Log
import com.example.R
import com.example.core.error.ErrorHandler
import com.example.core.tenant.TenantPathProvider
import com.example.features.teachers.domain.TeacherProfile
import com.example.features.teachers.domain.TeacherStatus
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class TeacherRepository(
    private val context: Context? = null,
    private val firestoreInstance: FirebaseFirestore? = null
) {
    private val db: FirebaseFirestore by lazy {
        firestoreInstance ?: try {
            val dbId = context?.getString(R.string.firestore_database_id)
            if (dbId != null) FirebaseFirestore.getInstance(dbId) else FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w("TeacherRepository", "Fallback to default Firestore instance", e)
            FirebaseFirestore.getInstance()
        }
    }

    suspend fun getTeachers(schoolId: String = TenantPathProvider.requireActiveSchoolId()): List<TeacherProfile> {
        return try {
            TenantPathProvider.verifyTenantBoundary(schoolId)
            val path = TenantPathProvider.teachersPath(schoolId)
            val snapshot = db.collection(path).get().await()
            if (snapshot.isEmpty) {
                getPreSeededTeachers(schoolId)
            } else {
                snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    TeacherProfile.fromFirestoreMap(doc.id, schoolId, data)
                }
            }
        } catch (e: Exception) {
            Log.w("TeacherRepository", "Error retrieving teachers from Firestore, loading seeded records", e)
            getPreSeededTeachers(schoolId)
        }
    }

    suspend fun getTeacherById(schoolId: String, teacherId: String): TeacherProfile? {
        return try {
            TenantPathProvider.verifyTenantBoundary(schoolId)
            val path = TenantPathProvider.teachersPath(schoolId)
            val doc = db.collection(path).document(teacherId).get().await()
            if (doc.exists() && doc.data != null) {
                TeacherProfile.fromFirestoreMap(doc.id, schoolId, doc.data!!)
            } else {
                getPreSeededTeachers(schoolId).firstOrNull { it.id == teacherId }
            }
        } catch (e: Exception) {
            getPreSeededTeachers(schoolId).firstOrNull { it.id == teacherId }
        }
    }

    suspend fun saveTeacher(teacher: TeacherProfile): Result<TeacherProfile> {
        return try {
            TenantPathProvider.verifyTenantBoundary(teacher.schoolId)
            val path = TenantPathProvider.teachersPath(teacher.schoolId)
            db.collection(path).document(teacher.id).set(teacher.toFirestoreMap()).await()
            Result.success(teacher)
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    suspend fun deleteTeacher(schoolId: String, teacherId: String): Result<Unit> {
        return try {
            TenantPathProvider.verifyTenantBoundary(schoolId)
            val path = TenantPathProvider.teachersPath(schoolId)
            db.collection(path).document(teacherId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    fun generateEmployeeId(schoolId: String, department: String): String {
        val cleanPrefix = schoolId.replace("sch_", "").take(3).uppercase()
        val deptCode = when {
            department.contains("Social", ignoreCase = true) || department.contains("Humanities", ignoreCase = true) || department.contains("History", ignoreCase = true) -> "HUM"
            department.contains("Science", ignoreCase = true) || department.contains("Math", ignoreCase = true) -> "SCI"
            department.contains("Language", ignoreCase = true) || department.contains("English", ignoreCase = true) -> "LAN"
            department.contains("Art", ignoreCase = true) || department.contains("Music", ignoreCase = true) -> "ART"
            department.contains("Physical", ignoreCase = true) || department.contains("Sports", ignoreCase = true) -> "PED"
            else -> "FAC"
        }
        val randomSeq = (100..999).random()
        return "$cleanPrefix-$deptCode-$randomSeq"
    }

    fun getPreSeededTeachers(schoolId: String): List<TeacherProfile> {
        return if (schoolId.contains("oxford") || schoolId.startsWith("sch_oxf")) {
            listOf(
                TeacherProfile(
                    id = "tea_oxf_01",
                    schoolId = schoolId,
                    employeeId = "OXF-SCI-101",
                    firstName = "Sarah",
                    lastName = "Jenkins",
                    fullName = "Dr. Sarah Jenkins",
                    designation = "Head of Department",
                    department = "Science & Mathematics",
                    primarySubject = "Advanced Physics",
                    additionalSubjects = listOf("AP Physics", "Astronomy"),
                    qualification = "Ph.D. Astrophysics, M.Sc. Physics",
                    joiningDate = "2019-08-15",
                    experienceYears = 12,
                    email = "teacher.sarah@oxfordacademy.edu",
                    phone = "+1 555-0201",
                    emergencyContact = "+1 555-0209",
                    address = "77 Commonwealth Ave, Boston, MA",
                    assignedClassId = "Grade 10",
                    assignedSectionId = "Section A",
                    isClassTeacher = true,
                    bloodGroup = "O+",
                    salaryTier = "Tier 1 - Executive Faculty",
                    status = TeacherStatus.ACTIVE
                ),
                TeacherProfile(
                    id = "tea_oxf_02",
                    schoolId = schoolId,
                    employeeId = "OXF-SCI-102",
                    firstName = "Alan",
                    lastName = "Turing",
                    fullName = "Prof. Alan Turing",
                    designation = "Senior Faculty",
                    department = "Science & Mathematics",
                    primarySubject = "Pure Mathematics",
                    additionalSubjects = listOf("Calculus BC", "Computer Science"),
                    qualification = "M.Sc. Applied Mathematics, B.Ed.",
                    joiningDate = "2020-09-01",
                    experienceYears = 9,
                    email = "alan.turing@oxfordacademy.edu",
                    phone = "+1 555-0202",
                    emergencyContact = "+1 555-0208",
                    address = "14 Cambridge St, Boston, MA",
                    assignedClassId = "Grade 11",
                    assignedSectionId = "Section B",
                    isClassTeacher = true,
                    bloodGroup = "A+",
                    salaryTier = "Tier 2 - Senior Faculty",
                    status = TeacherStatus.ACTIVE
                ),
                TeacherProfile(
                    id = "tea_oxf_03",
                    schoolId = schoolId,
                    employeeId = "OXF-LAN-103",
                    firstName = "Maria",
                    lastName = "Montessori",
                    fullName = "Maria Montessori",
                    designation = "Senior Faculty",
                    department = "Languages & Literature",
                    primarySubject = "English Literature",
                    additionalSubjects = listOf("World Literature", "Creative Writing"),
                    qualification = "M.A. English Literature, M.Ed.",
                    joiningDate = "2021-08-20",
                    experienceYears = 8,
                    email = "maria.m@oxfordacademy.edu",
                    phone = "+1 555-0203",
                    emergencyContact = "+1 555-0207",
                    address = "92 Newbury St, Boston, MA",
                    assignedClassId = "Grade 9",
                    assignedSectionId = "Section A",
                    isClassTeacher = true,
                    bloodGroup = "B+",
                    salaryTier = "Tier 2 - Senior Faculty",
                    status = TeacherStatus.ACTIVE
                ),
                TeacherProfile(
                    id = "tea_oxf_04",
                    schoolId = schoolId,
                    employeeId = "OXF-HUM-104",
                    firstName = "Arthur",
                    lastName = "Conan",
                    fullName = "Arthur Conan",
                    designation = "Assistant Teacher",
                    department = "Social Sciences & Humanities",
                    primarySubject = "World History",
                    additionalSubjects = listOf("Civics", "Geography"),
                    qualification = "B.A. History & Political Science",
                    joiningDate = "2023-01-10",
                    experienceYears = 4,
                    email = "arthur.c@oxfordacademy.edu",
                    phone = "+1 555-0204",
                    emergencyContact = "+1 555-0206",
                    address = "21 Baker St, Boston, MA",
                    assignedClassId = null,
                    assignedSectionId = null,
                    isClassTeacher = false,
                    bloodGroup = "AB-",
                    salaryTier = "Tier 3 - Associate Faculty",
                    status = TeacherStatus.ACTIVE
                ),
                TeacherProfile(
                    id = "tea_oxf_05",
                    schoolId = schoolId,
                    employeeId = "OXF-SCI-105",
                    firstName = "Elena",
                    lastName = "Rostova",
                    fullName = "Elena Rostova",
                    designation = "Lab Instructor",
                    department = "Science & Mathematics",
                    primarySubject = "Chemistry Laboratory",
                    additionalSubjects = listOf("Biochemistry Lab"),
                    qualification = "B.Sc. Chemistry, Lab Safety Certified",
                    joiningDate = "2022-09-01",
                    experienceYears = 5,
                    email = "elena.r@oxfordacademy.edu",
                    phone = "+1 555-0205",
                    emergencyContact = "+1 555-0210",
                    address = "40 Beacon St, Boston, MA",
                    assignedClassId = null,
                    assignedSectionId = null,
                    isClassTeacher = false,
                    bloodGroup = "O-",
                    salaryTier = "Tier 4 - Laboratory Specialist",
                    status = TeacherStatus.ON_LEAVE
                )
            )
        } else {
            listOf(
                TeacherProfile(
                    id = "tea_cam_01",
                    schoolId = schoolId,
                    employeeId = "CAM-SCI-201",
                    firstName = "Isaac",
                    lastName = "Newton",
                    fullName = "Dr. Isaac Newton",
                    designation = "Dean of Natural Sciences",
                    department = "Science & Mathematics",
                    primarySubject = "Classical Mechanics & Calculus",
                    additionalSubjects = listOf("Optics", "Higher Mathematics"),
                    qualification = "Ph.D. Physics, Fellow of Science",
                    joiningDate = "2018-09-01",
                    experienceYears = 16,
                    email = "newton@cambridge.co.uk",
                    phone = "+44 20 7946 0881",
                    emergencyContact = "+44 20 7946 0889",
                    address = "1 Trinity St, Cambridge, UK",
                    assignedClassId = "Grade 12",
                    assignedSectionId = "Science",
                    isClassTeacher = true,
                    bloodGroup = "O+",
                    salaryTier = "Tier 1 - Executive Faculty",
                    status = TeacherStatus.ACTIVE
                ),
                TeacherProfile(
                    id = "tea_cam_02",
                    schoolId = schoolId,
                    employeeId = "CAM-LAN-202",
                    firstName = "Jane",
                    lastName = "Austen",
                    fullName = "Jane Austen",
                    designation = "Senior Faculty",
                    department = "Languages & Literature",
                    primarySubject = "British Literature & Rhetoric",
                    additionalSubjects = listOf("Creative Prose", "Classical Drama"),
                    qualification = "M.A. English Literature",
                    joiningDate = "2020-09-01",
                    experienceYears = 10,
                    email = "austen@cambridge.co.uk",
                    phone = "+44 20 7946 0882",
                    emergencyContact = "+44 20 7946 0888",
                    address = "18 King's Parade, Cambridge, UK",
                    assignedClassId = "Grade 10",
                    assignedSectionId = "Section A",
                    isClassTeacher = true,
                    bloodGroup = "A-",
                    salaryTier = "Tier 2 - Senior Faculty",
                    status = TeacherStatus.ACTIVE
                ),
                TeacherProfile(
                    id = "tea_cam_03",
                    schoolId = schoolId,
                    employeeId = "CAM-SCI-203",
                    firstName = "Charles",
                    lastName = "Darwin",
                    fullName = "Charles Darwin",
                    designation = "Senior Faculty",
                    department = "Science & Mathematics",
                    primarySubject = "Biological Sciences & Ecology",
                    additionalSubjects = listOf("Genetics", "Botany"),
                    qualification = "M.Sc. Natural History & Biology",
                    joiningDate = "2021-09-01",
                    experienceYears = 8,
                    email = "darwin@cambridge.co.uk",
                    phone = "+44 20 7946 0883",
                    emergencyContact = "+44 20 7946 0887",
                    address = "5 Downing St, Cambridge, UK",
                    assignedClassId = null,
                    assignedSectionId = null,
                    isClassTeacher = false,
                    bloodGroup = "B+",
                    salaryTier = "Tier 2 - Senior Faculty",
                    status = TeacherStatus.ACTIVE
                )
            )
        }
    }
}
