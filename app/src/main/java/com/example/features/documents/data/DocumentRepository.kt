package com.example.features.documents.data

import android.content.Context
import android.util.Log
import com.example.R
import com.example.core.error.ErrorHandler
import com.example.core.tenant.TenantPathProvider
import com.example.features.documents.domain.DocumentCategory
import com.example.features.documents.domain.DocumentVerificationStatus
import com.example.features.documents.domain.SchoolDocument
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class DocumentRepository(
    private val context: Context? = null,
    private val firestoreInstance: FirebaseFirestore? = null
) {
    private val db: FirebaseFirestore by lazy {
        firestoreInstance ?: try {
            val dbId = context?.getString(R.string.firestore_database_id)
            if (dbId != null) FirebaseFirestore.getInstance(dbId) else FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w("DocumentRepository", "Fallback to default Firestore instance", e)
            FirebaseFirestore.getInstance()
        }
    }

    suspend fun getDocuments(
        schoolId: String = TenantPathProvider.requireActiveSchoolId(),
        categoryFilter: DocumentCategory? = null,
        statusFilter: DocumentVerificationStatus? = null
    ): List<SchoolDocument> {
        return try {
            TenantPathProvider.verifyTenantBoundary(schoolId)
            val path = TenantPathProvider.documentsPath(schoolId)
            val snapshot = db.collection(path).get().await()
            val list = if (snapshot.isEmpty) {
                getPreSeededDocuments(schoolId)
            } else {
                snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    SchoolDocument.fromFirestoreMap(doc.id, schoolId, data)
                }
            }
            filterDocuments(list, categoryFilter, statusFilter)
        } catch (e: Exception) {
            Log.w("DocumentRepository", "Error retrieving documents from Firestore, fallback to preseeded", e)
            val seeded = getPreSeededDocuments(schoolId)
            filterDocuments(seeded, categoryFilter, statusFilter)
        }
    }

    private fun filterDocuments(
        docs: List<SchoolDocument>,
        categoryFilter: DocumentCategory?,
        statusFilter: DocumentVerificationStatus?
    ): List<SchoolDocument> {
        return docs.filter { doc ->
            (categoryFilter == null || doc.category == categoryFilter) &&
                (statusFilter == null || doc.verificationStatus == statusFilter)
        }
    }

    suspend fun saveDocument(document: SchoolDocument): Result<SchoolDocument> {
        return try {
            TenantPathProvider.verifyTenantBoundary(document.schoolId)
            val path = TenantPathProvider.documentsPath(document.schoolId)
            db.collection(path).document(document.id).set(document.toFirestoreMap()).await()
            Result.success(document)
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    suspend fun recordDownload(schoolId: String, docId: String): Result<SchoolDocument> {
        return try {
            TenantPathProvider.verifyTenantBoundary(schoolId)
            val docs = getDocuments(schoolId)
            val target = docs.firstOrNull { it.id == docId }
                ?: return Result.failure(Exception("Document record not found"))

            val updatedDoc = target.copy(downloadCount = target.downloadCount + 1)
            saveDocument(updatedDoc)
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    suspend fun updateVerificationStatus(
        schoolId: String,
        docId: String,
        status: DocumentVerificationStatus
    ): Result<SchoolDocument> {
        return try {
            TenantPathProvider.verifyTenantBoundary(schoolId)
            val docs = getDocuments(schoolId)
            val target = docs.firstOrNull { it.id == docId }
                ?: return Result.failure(Exception("Document record not found"))

            val updatedDoc = target.copy(verificationStatus = status)
            saveDocument(updatedDoc)
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    suspend fun togglePin(schoolId: String, docId: String): Result<SchoolDocument> {
        return try {
            TenantPathProvider.verifyTenantBoundary(schoolId)
            val docs = getDocuments(schoolId)
            val target = docs.firstOrNull { it.id == docId }
                ?: return Result.failure(Exception("Document record not found"))

            val updatedDoc = target.copy(isPinned = !target.isPinned)
            saveDocument(updatedDoc)
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    suspend fun deleteDocument(schoolId: String, docId: String): Result<Unit> {
        return try {
            TenantPathProvider.verifyTenantBoundary(schoolId)
            val path = TenantPathProvider.documentsPath(schoolId)
            db.collection(path).document(docId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    fun getPreSeededDocuments(schoolId: String): List<SchoolDocument> {
        return if (schoolId.contains("oxford") || schoolId.startsWith("sch_oxf")) {
            listOf(
                SchoolDocument(
                    id = "doc_oxf_01",
                    schoolId = schoolId,
                    documentCode = "SYL-OXF-PHY10",
                    title = "Grade 10 Senior Physics & Practical Lab Syllabus",
                    description = "Complete 2026–2027 curriculum units covering mechanics, wave optics, electromagnetism, and laboratory assessment rubrics.",
                    category = DocumentCategory.SYLLABUS,
                    verificationStatus = DocumentVerificationStatus.VERIFIED,
                    targetGradeOrDepartment = "Grade 10 • Science",
                    fileName = "Oxford_G10_Physics_Syllabus_2026.pdf",
                    fileSizeLabel = "2.4 MB",
                    academicYear = "2026-2027",
                    issuedBy = "Dr. Sarah Jenkins",
                    issuedDate = "2026-03-01",
                    downloadCount = 42,
                    isPinned = true
                ),
                SchoolDocument(
                    id = "doc_oxf_02",
                    schoolId = schoolId,
                    documentCode = "CERT-OXF-2601",
                    title = "Bonafide Enrollment Certificate — Amina Rahman",
                    description = "Digitally signed institutional bonafide enrollment certificate for passport and scholarship verification.",
                    category = DocumentCategory.CERTIFICATE,
                    verificationStatus = DocumentVerificationStatus.VERIFIED,
                    targetGradeOrDepartment = "Grade 10 - Section A",
                    issuedToName = "Amina Rahman",
                    issuedToId = "OXF-10A-001",
                    fileName = "Bonafide_Amina_Rahman_2026.pdf",
                    fileSizeLabel = "420 KB",
                    academicYear = "2026-2027",
                    issuedBy = "Office of the Registrar",
                    issuedDate = "2026-03-18",
                    downloadCount = 3,
                    isPinned = false
                ),
                SchoolDocument(
                    id = "doc_oxf_03",
                    schoolId = schoolId,
                    documentCode = "IDC-OXF-1002",
                    title = "Smart Campus ID Card Credential — Oliver Smith",
                    description = "RFID-enabled library & bus boarding identity card badge pending annual photo verification.",
                    category = DocumentCategory.ID_CARD,
                    verificationStatus = DocumentVerificationStatus.PENDING_REVIEW,
                    targetGradeOrDepartment = "Grade 10 - Section A",
                    issuedToName = "Oliver Smith",
                    issuedToId = "OXF-10A-002",
                    fileName = "IDCard_Oliver_Smith_2026.pdf",
                    fileSizeLabel = "310 KB",
                    academicYear = "2026-2027",
                    issuedBy = "Student Affairs Desk",
                    issuedDate = "2026-03-22",
                    downloadCount = 0,
                    isPinned = false
                ),
                SchoolDocument(
                    id = "doc_oxf_04",
                    schoolId = schoolId,
                    documentCode = "POL-OXF-2026",
                    title = "Student Code of Conduct & Examination Integrity Handbook",
                    description = "Institutional policies governing campus safety, uniform guidelines, academic honesty, and grievance escalation.",
                    category = DocumentCategory.POLICY,
                    verificationStatus = DocumentVerificationStatus.VERIFIED,
                    targetGradeOrDepartment = "All Students & Parents",
                    fileName = "Oxford_Student_Handbook_2026.pdf",
                    fileSizeLabel = "3.8 MB",
                    academicYear = "2026-2027",
                    issuedBy = "Dr. Robert Vance",
                    issuedDate = "2026-02-15",
                    downloadCount = 118,
                    isPinned = true
                )
            )
        } else {
            listOf(
                SchoolDocument(
                    id = "doc_cam_01",
                    schoolId = schoolId,
                    documentCode = "SYL-CAM-LIT10",
                    title = "Year 10 British Literature & Rhetoric Syllabus",
                    description = "Term reading list, Shakespeare critical essays, and seminar coursework guide.",
                    category = DocumentCategory.SYLLABUS,
                    verificationStatus = DocumentVerificationStatus.VERIFIED,
                    targetGradeOrDepartment = "Year 10 • Humanities",
                    fileName = "Cambridge_Y10_Literature_Syllabus.pdf",
                    fileSizeLabel = "1.9 MB",
                    academicYear = "2026-2027",
                    issuedBy = "Prof. Eleanor Vance",
                    issuedDate = "2026-03-04",
                    downloadCount = 29,
                    isPinned = true
                ),
                SchoolDocument(
                    id = "doc_cam_02",
                    schoolId = schoolId,
                    documentCode = "CERT-CAM-901",
                    title = "Academic Honor Roll Certificate — Elizabeth Bennet",
                    description = "Dean's commendation certificate for First-Class Honors in Humanities.",
                    category = DocumentCategory.CERTIFICATE,
                    verificationStatus = DocumentVerificationStatus.VERIFIED,
                    targetGradeOrDepartment = "Year 10 - Section A",
                    issuedToName = "Elizabeth Bennet",
                    issuedToId = "CAM-10A-001",
                    fileName = "Honor_Certificate_Elizabeth_Bennet.pdf",
                    fileSizeLabel = "540 KB",
                    academicYear = "2026-2027",
                    issuedBy = "Cambridge Academic Board",
                    issuedDate = "2026-03-20",
                    downloadCount = 2,
                    isPinned = false
                )
            )
        }
    }
}
