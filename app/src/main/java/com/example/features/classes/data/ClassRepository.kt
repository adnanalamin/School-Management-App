package com.example.features.classes.data

import android.content.Context
import android.util.Log
import com.example.R
import com.example.core.error.ErrorHandler
import com.example.core.tenant.TenantPathProvider
import com.example.features.classes.domain.AcademicClass
import com.example.features.classes.domain.ClassSection
import com.example.features.classes.domain.SubjectAllocation
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ClassRepository(
    private val context: Context? = null,
    private val firestoreInstance: FirebaseFirestore? = null
) {
    private val db: FirebaseFirestore by lazy {
        firestoreInstance ?: try {
            val dbId = context?.getString(R.string.firestore_database_id)
            if (dbId != null) FirebaseFirestore.getInstance(dbId) else FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w("ClassRepository", "Fallback to default Firestore instance", e)
            FirebaseFirestore.getInstance()
        }
    }

    suspend fun getClasses(schoolId: String = TenantPathProvider.requireActiveSchoolId()): List<AcademicClass> {
        return try {
            TenantPathProvider.verifyTenantBoundary(schoolId)
            val path = TenantPathProvider.classesPath(schoolId)
            val snapshot = db.collection(path).get().await()
            if (snapshot.isEmpty) {
                getPreSeededClasses(schoolId)
            } else {
                snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    AcademicClass.fromFirestoreMap(doc.id, schoolId, data)
                }
            }
        } catch (e: Exception) {
            Log.w("ClassRepository", "Error retrieving classes from Firestore, loading seeded records", e)
            getPreSeededClasses(schoolId)
        }
    }

    suspend fun getSections(
        schoolId: String = TenantPathProvider.requireActiveSchoolId(),
        classId: String? = null
    ): List<ClassSection> {
        return try {
            TenantPathProvider.verifyTenantBoundary(schoolId)
            val path = TenantPathProvider.sectionsPath(schoolId)
            val snapshot = db.collection(path).get().await()
            val list = if (snapshot.isEmpty) {
                getPreSeededSections(schoolId)
            } else {
                snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    ClassSection.fromFirestoreMap(doc.id, schoolId, data)
                }
            }
            if (classId != null) list.filter { it.classId == classId } else list
        } catch (e: Exception) {
            Log.w("ClassRepository", "Error retrieving sections from Firestore, loading seeded records", e)
            val seeded = getPreSeededSections(schoolId)
            if (classId != null) seeded.filter { it.classId == classId } else seeded
        }
    }

    suspend fun getSubjectAllocations(
        schoolId: String = TenantPathProvider.requireActiveSchoolId(),
        sectionId: String? = null
    ): List<SubjectAllocation> {
        val all = getPreSeededAllocations(schoolId)
        return if (sectionId != null) all.filter { it.sectionId == sectionId } else all
    }

    suspend fun saveClass(academicClass: AcademicClass): Result<AcademicClass> {
        return try {
            TenantPathProvider.verifyTenantBoundary(academicClass.schoolId)
            val path = TenantPathProvider.classesPath(academicClass.schoolId)
            db.collection(path).document(academicClass.id).set(academicClass.toFirestoreMap()).await()
            Result.success(academicClass)
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    suspend fun saveSection(section: ClassSection): Result<ClassSection> {
        return try {
            TenantPathProvider.verifyTenantBoundary(section.schoolId)
            val path = TenantPathProvider.sectionsPath(section.schoolId)
            db.collection(path).document(section.id).set(section.toFirestoreMap()).await()
            Result.success(section)
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    suspend fun deleteClass(schoolId: String, classId: String): Result<Unit> {
        return try {
            TenantPathProvider.verifyTenantBoundary(schoolId)
            val path = TenantPathProvider.classesPath(schoolId)
            db.collection(path).document(classId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    suspend fun deleteSection(schoolId: String, sectionId: String): Result<Unit> {
        return try {
            TenantPathProvider.verifyTenantBoundary(schoolId)
            val path = TenantPathProvider.sectionsPath(schoolId)
            db.collection(path).document(sectionId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    fun getPreSeededClasses(schoolId: String): List<AcademicClass> {
        return if (schoolId.contains("oxford") || schoolId.startsWith("sch_oxf")) {
            listOf(
                AcademicClass(
                    id = "cls_oxf_g09",
                    schoolId = schoolId,
                    name = "Grade 9",
                    code = "G09",
                    numericGrade = 9,
                    stream = "General Academic",
                    sectionsCount = 2,
                    totalStudents = 56
                ),
                AcademicClass(
                    id = "cls_oxf_g10",
                    schoolId = schoolId,
                    name = "Grade 10",
                    code = "G10",
                    numericGrade = 10,
                    stream = "Science & Tech",
                    sectionsCount = 2,
                    totalStudents = 62
                ),
                AcademicClass(
                    id = "cls_oxf_g11",
                    schoolId = schoolId,
                    name = "Grade 11",
                    code = "G11",
                    numericGrade = 11,
                    stream = "Pure Sciences",
                    sectionsCount = 2,
                    totalStudents = 50
                ),
                AcademicClass(
                    id = "cls_oxf_g12",
                    schoolId = schoolId,
                    name = "Grade 12",
                    code = "G12",
                    numericGrade = 12,
                    stream = "Advanced STEM",
                    sectionsCount = 1,
                    totalStudents = 30
                )
            )
        } else {
            listOf(
                AcademicClass(
                    id = "cls_cam_g10",
                    schoolId = schoolId,
                    name = "Year 10",
                    code = "Y10",
                    numericGrade = 10,
                    stream = "GCSE Foundation",
                    sectionsCount = 2,
                    totalStudents = 45
                ),
                AcademicClass(
                    id = "cls_cam_g11",
                    schoolId = schoolId,
                    name = "Year 11",
                    code = "Y11",
                    numericGrade = 11,
                    stream = "GCSE Advanced",
                    sectionsCount = 2,
                    totalStudents = 42
                ),
                AcademicClass(
                    id = "cls_cam_g12",
                    schoolId = schoolId,
                    name = "Year 12 (Sixth Form)",
                    code = "Y12",
                    numericGrade = 12,
                    stream = "A-Levels Natural Sciences",
                    sectionsCount = 1,
                    totalStudents = 28
                )
            )
        }
    }

    fun getPreSeededSections(schoolId: String): List<ClassSection> {
        return if (schoolId.contains("oxford") || schoolId.startsWith("sch_oxf")) {
            listOf(
                ClassSection(
                    id = "sec_oxf_10a",
                    schoolId = schoolId,
                    classId = "cls_oxf_g10",
                    className = "Grade 10",
                    name = "Section A",
                    roomNumber = "Room 301 - West Wing",
                    capacity = 35,
                    currentEnrollment = 31,
                    classTeacherId = "tea_oxf_01",
                    classTeacherName = "Dr. Sarah Jenkins"
                ),
                ClassSection(
                    id = "sec_oxf_10b",
                    schoolId = schoolId,
                    classId = "cls_oxf_g10",
                    className = "Grade 10",
                    name = "Section B",
                    roomNumber = "Room 302 - West Wing",
                    capacity = 35,
                    currentEnrollment = 31,
                    classTeacherId = "tea_oxf_03",
                    classTeacherName = "Maria Montessori"
                ),
                ClassSection(
                    id = "sec_oxf_11a",
                    schoolId = schoolId,
                    classId = "cls_oxf_g11",
                    className = "Grade 11",
                    name = "Section A",
                    roomNumber = "Room 401 - STEM Wing",
                    capacity = 30,
                    currentEnrollment = 26,
                    classTeacherId = "tea_oxf_02",
                    classTeacherName = "Prof. Alan Turing"
                ),
                ClassSection(
                    id = "sec_oxf_09a",
                    schoolId = schoolId,
                    classId = "cls_oxf_g09",
                    className = "Grade 9",
                    name = "Section A",
                    roomNumber = "Room 201 - East Wing",
                    capacity = 35,
                    currentEnrollment = 28,
                    classTeacherId = "tea_oxf_04",
                    classTeacherName = "Arthur Conan"
                )
            )
        } else {
            listOf(
                ClassSection(
                    id = "sec_cam_12s",
                    schoolId = schoolId,
                    classId = "cls_cam_g12",
                    className = "Year 12 (Sixth Form)",
                    name = "Natural Sciences",
                    roomNumber = "Newton Hall",
                    capacity = 30,
                    currentEnrollment = 28,
                    classTeacherId = "tea_cam_01",
                    classTeacherName = "Dr. Isaac Newton"
                ),
                ClassSection(
                    id = "sec_cam_10a",
                    schoolId = schoolId,
                    classId = "cls_cam_g10",
                    className = "Year 10",
                    name = "Section A",
                    roomNumber = "Room 12 - Trinity Court",
                    capacity = 30,
                    currentEnrollment = 25,
                    classTeacherId = "tea_cam_02",
                    classTeacherName = "Jane Austen"
                )
            )
        }
    }

    fun getPreSeededAllocations(schoolId: String): List<SubjectAllocation> {
        return if (schoolId.contains("oxford") || schoolId.startsWith("sch_oxf")) {
            listOf(
                SubjectAllocation(
                    id = "sub_oxf_10a_phy",
                    schoolId = schoolId,
                    classId = "cls_oxf_g10",
                    sectionId = "sec_oxf_10a",
                    sectionName = "Grade 10 - Section A",
                    subjectName = "Advanced Physics",
                    subjectCode = "PHY-101",
                    teacherId = "tea_oxf_01",
                    teacherName = "Dr. Sarah Jenkins",
                    periodsPerWeek = 5,
                    isElective = false,
                    roomNumber = "Physics Lab 1"
                ),
                SubjectAllocation(
                    id = "sub_oxf_10a_mth",
                    schoolId = schoolId,
                    classId = "cls_oxf_g10",
                    sectionId = "sec_oxf_10a",
                    sectionName = "Grade 10 - Section A",
                    subjectName = "Pure Mathematics",
                    subjectCode = "MTH-102",
                    teacherId = "tea_oxf_02",
                    teacherName = "Prof. Alan Turing",
                    periodsPerWeek = 5,
                    isElective = false,
                    roomNumber = "Room 301"
                ),
                SubjectAllocation(
                    id = "sub_oxf_10a_eng",
                    schoolId = schoolId,
                    classId = "cls_oxf_g10",
                    sectionId = "sec_oxf_10a",
                    sectionName = "Grade 10 - Section A",
                    subjectName = "English Literature",
                    subjectCode = "ENG-103",
                    teacherId = "tea_oxf_03",
                    teacherName = "Maria Montessori",
                    periodsPerWeek = 4,
                    isElective = false,
                    roomNumber = "Room 301"
                ),
                SubjectAllocation(
                    id = "sub_oxf_10a_chm",
                    schoolId = schoolId,
                    classId = "cls_oxf_g10",
                    sectionId = "sec_oxf_10a",
                    sectionName = "Grade 10 - Section A",
                    subjectName = "Chemistry Laboratory",
                    subjectCode = "CHM-104",
                    teacherId = "tea_oxf_05",
                    teacherName = "Elena Rostova",
                    periodsPerWeek = 2,
                    isElective = true,
                    roomNumber = "Chemistry Lab 2"
                ),
                SubjectAllocation(
                    id = "sub_oxf_11a_cal",
                    schoolId = schoolId,
                    classId = "cls_oxf_g11",
                    sectionId = "sec_oxf_11a",
                    sectionName = "Grade 11 - Section A",
                    subjectName = "Calculus BC & Analysis",
                    subjectCode = "MTH-201",
                    teacherId = "tea_oxf_02",
                    teacherName = "Prof. Alan Turing",
                    periodsPerWeek = 6,
                    isElective = false,
                    roomNumber = "Room 401"
                ),
                SubjectAllocation(
                    id = "sub_oxf_09a_his",
                    schoolId = schoolId,
                    classId = "cls_oxf_g09",
                    sectionId = "sec_oxf_09a",
                    sectionName = "Grade 9 - Section A",
                    subjectName = "World History & Civics",
                    subjectCode = "HIS-091",
                    teacherId = "tea_oxf_04",
                    teacherName = "Arthur Conan",
                    periodsPerWeek = 4,
                    isElective = false,
                    roomNumber = "Room 201"
                )
            )
        } else {
            listOf(
                SubjectAllocation(
                    id = "sub_cam_12s_phy",
                    schoolId = schoolId,
                    classId = "cls_cam_g12",
                    sectionId = "sec_cam_12s",
                    sectionName = "Year 12 - Natural Sciences",
                    subjectName = "Classical Mechanics & Calculus",
                    subjectCode = "PHY-301",
                    teacherId = "tea_cam_01",
                    teacherName = "Dr. Isaac Newton",
                    periodsPerWeek = 6,
                    isElective = false,
                    roomNumber = "Newton Hall"
                ),
                SubjectAllocation(
                    id = "sub_cam_10a_lit",
                    schoolId = schoolId,
                    classId = "cls_cam_g10",
                    sectionId = "sec_cam_10a",
                    sectionName = "Year 10 - Section A",
                    subjectName = "British Literature & Rhetoric",
                    subjectCode = "ENG-201",
                    teacherId = "tea_cam_02",
                    teacherName = "Jane Austen",
                    periodsPerWeek = 5,
                    isElective = false,
                    roomNumber = "Room 12"
                ),
                SubjectAllocation(
                    id = "sub_cam_12s_bio",
                    schoolId = schoolId,
                    classId = "cls_cam_g12",
                    sectionId = "sec_cam_12s",
                    sectionName = "Year 12 - Natural Sciences",
                    subjectName = "Biological Sciences & Ecology",
                    subjectCode = "BIO-202",
                    teacherId = "tea_cam_03",
                    teacherName = "Charles Darwin",
                    periodsPerWeek = 4,
                    isElective = true,
                    roomNumber = "Botany Garden Wing"
                )
            )
        }
    }
}
