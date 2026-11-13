package com.example.features.leave.data

import android.content.Context
import android.util.Log
import com.example.R
import com.example.core.error.ErrorHandler
import com.example.core.tenant.TenantPathProvider
import com.example.features.leave.domain.LeaveApplicantType
import com.example.features.leave.domain.LeaveCategory
import com.example.features.leave.domain.LeaveRequest
import com.example.features.leave.domain.LeaveStatus
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class LeaveRepository(
    private val context: Context? = null,
    private val firestoreInstance: FirebaseFirestore? = null
) {
    private val db: FirebaseFirestore by lazy {
        firestoreInstance ?: try {
            val dbId = context?.getString(R.string.firestore_database_id)
            if (dbId != null) FirebaseFirestore.getInstance(dbId) else FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w("LeaveRepository", "Fallback to default Firestore instance", e)
            FirebaseFirestore.getInstance()
        }
    }

    suspend fun getLeaveRequests(
        schoolId: String = TenantPathProvider.requireActiveSchoolId(),
        statusFilter: LeaveStatus? = null,
        applicantTypeFilter: LeaveApplicantType? = null
    ): List<LeaveRequest> {
        return try {
            TenantPathProvider.verifyTenantBoundary(schoolId)
            val path = TenantPathProvider.leavesPath(schoolId)
            val snapshot = db.collection(path).get().await()
            val list = if (snapshot.isEmpty) {
                getPreSeededLeaveRequests(schoolId)
            } else {
                snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    LeaveRequest.fromFirestoreMap(doc.id, schoolId, data)
                }
            }
            filterRequests(list, statusFilter, applicantTypeFilter)
        } catch (e: Exception) {
            Log.w("LeaveRepository", "Error retrieving leave requests from Firestore, fallback to preseeded", e)
            val seeded = getPreSeededLeaveRequests(schoolId)
            filterRequests(seeded, statusFilter, applicantTypeFilter)
        }
    }

    private fun filterRequests(
        requests: List<LeaveRequest>,
        statusFilter: LeaveStatus?,
        applicantTypeFilter: LeaveApplicantType?
    ): List<LeaveRequest> {
        return requests.filter { req ->
            (statusFilter == null || req.status == statusFilter) &&
                (applicantTypeFilter == null || req.applicantType == applicantTypeFilter)
        }
    }

    suspend fun saveLeaveRequest(request: LeaveRequest): Result<LeaveRequest> {
        return try {
            TenantPathProvider.verifyTenantBoundary(request.schoolId)
            val path = TenantPathProvider.leavesPath(request.schoolId)
            db.collection(path).document(request.id).set(request.toFirestoreMap()).await()
            Result.success(request)
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    suspend fun reviewLeaveRequest(
        schoolId: String,
        leaveId: String,
        newStatus: LeaveStatus,
        reviewerName: String,
        reviewRemarks: String,
        reviewedDate: String
    ): Result<LeaveRequest> {
        return try {
            TenantPathProvider.verifyTenantBoundary(schoolId)
            val requests = getLeaveRequests(schoolId)
            val target = requests.firstOrNull { it.id == leaveId }
                ?: return Result.failure(Exception("Leave request not found"))

            val updated = target.copy(
                status = newStatus,
                reviewerName = reviewerName,
                reviewRemarks = reviewRemarks,
                reviewedDate = reviewedDate
            )
            saveLeaveRequest(updated)
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    suspend fun withdrawLeaveRequest(schoolId: String, leaveId: String): Result<LeaveRequest> {
        return try {
            TenantPathProvider.verifyTenantBoundary(schoolId)
            val requests = getLeaveRequests(schoolId)
            val target = requests.firstOrNull { it.id == leaveId }
                ?: return Result.failure(Exception("Leave request not found"))

            val updated = target.copy(status = LeaveStatus.CANCELLED)
            saveLeaveRequest(updated)
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    suspend fun deleteLeaveRequest(schoolId: String, leaveId: String): Result<Unit> {
        return try {
            TenantPathProvider.verifyTenantBoundary(schoolId)
            val path = TenantPathProvider.leavesPath(schoolId)
            db.collection(path).document(leaveId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    fun getPreSeededLeaveRequests(schoolId: String): List<LeaveRequest> {
        return if (schoolId.contains("oxford") || schoolId.startsWith("sch_oxf")) {
            listOf(
                LeaveRequest(
                    id = "lv_oxf_01",
                    schoolId = schoolId,
                    requestCode = "LV-OXF-101",
                    applicantId = "stu_oxf_001",
                    applicantName = "Amina Rahman",
                    applicantType = LeaveApplicantType.STUDENT,
                    departmentOrGrade = "Grade 10 - Section A",
                    category = LeaveCategory.SICK,
                    startDate = "2026-04-10",
                    endDate = "2026-04-11",
                    totalDays = 2,
                    reason = "Seasonal flu and high fever; attending physician advised 48 hours of bed rest.",
                    attachmentNote = "Clinic_Certificate_OXF101.pdf",
                    status = LeaveStatus.PENDING,
                    submittedDate = "2026-04-09",
                    createdAt = 1712620800000L
                ),
                LeaveRequest(
                    id = "lv_oxf_02",
                    schoolId = schoolId,
                    requestCode = "LV-OXF-102",
                    applicantId = "tch_oxf_001",
                    applicantName = "Dr. Sarah Jenkins",
                    applicantType = LeaveApplicantType.FACULTY,
                    departmentOrGrade = "Physics Department",
                    category = LeaveCategory.ACADEMIC,
                    startDate = "2026-04-18",
                    endDate = "2026-04-20",
                    totalDays = 3,
                    reason = "Invited keynote speaker at the National STEM Curriculum Symposium; substitute schedule arranged with Mr. Clarke.",
                    attachmentNote = "Symposium_Invitation.pdf",
                    status = LeaveStatus.APPROVED,
                    reviewerName = "Dr. Robert Vance",
                    reviewRemarks = "Approved as official academic duty. Lab coverage confirmed.",
                    submittedDate = "2026-04-02",
                    reviewedDate = "2026-04-03",
                    createdAt = 1712016000000L
                ),
                LeaveRequest(
                    id = "lv_oxf_03",
                    schoolId = schoolId,
                    requestCode = "LV-OXF-103",
                    applicantId = "stu_oxf_002",
                    applicantName = "Oliver Smith",
                    applicantType = LeaveApplicantType.STUDENT,
                    departmentOrGrade = "Grade 10 - Section A",
                    category = LeaveCategory.CASUAL,
                    startDate = "2026-04-22",
                    endDate = "2026-04-24",
                    totalDays = 3,
                    reason = "Attending immediate family wedding out of state; guardian consent signed.",
                    attachmentNote = "Parent_Consent_Smith.pdf",
                    status = LeaveStatus.PENDING,
                    submittedDate = "2026-04-08",
                    createdAt = 1712534400000L
                ),
                LeaveRequest(
                    id = "lv_oxf_04",
                    schoolId = schoolId,
                    requestCode = "LV-OXF-104",
                    applicantId = "stf_oxf_003",
                    applicantName = "Arthur Pendelton",
                    applicantType = LeaveApplicantType.STAFF,
                    departmentOrGrade = "Fleet & Transport Operations",
                    category = LeaveCategory.EMERGENCY,
                    startDate = "2026-03-28",
                    endDate = "2026-03-28",
                    totalDays = 1,
                    reason = "Urgent morning personal errand during school bus dispatch window.",
                    status = LeaveStatus.REJECTED,
                    reviewerName = "Dr. Robert Vance",
                    reviewRemarks = "Declined for morning dispatch window; standby driver unavailable on short notice.",
                    submittedDate = "2026-03-27",
                    reviewedDate = "2026-03-27",
                    createdAt = 1711497600000L
                )
            )
        } else {
            listOf(
                LeaveRequest(
                    id = "lv_cam_01",
                    schoolId = schoolId,
                    requestCode = "LV-CAM-201",
                    applicantId = "stu_cam_001",
                    applicantName = "Elizabeth Bennet",
                    applicantType = LeaveApplicantType.STUDENT,
                    departmentOrGrade = "Year 10 - Section A",
                    category = LeaveCategory.ACADEMIC,
                    startDate = "2026-04-15",
                    endDate = "2026-04-16",
                    totalDays = 2,
                    reason = "Representing Cambridge High School at Regional Inter-Collegiate Debate Finals.",
                    attachmentNote = "Debate_Selection_Letter.pdf",
                    status = LeaveStatus.APPROVED,
                    reviewerName = "Prof. Eleanor Vance",
                    reviewRemarks = "Approved. Academic attendance exemption granted.",
                    submittedDate = "2026-04-04",
                    reviewedDate = "2026-04-05",
                    createdAt = 1712188800000L
                ),
                LeaveRequest(
                    id = "lv_cam_02",
                    schoolId = schoolId,
                    requestCode = "LV-CAM-202",
                    applicantId = "tch_cam_002",
                    applicantName = "Prof. William Collins",
                    applicantType = LeaveApplicantType.FACULTY,
                    departmentOrGrade = "Humanities Department",
                    category = LeaveCategory.CASUAL,
                    startDate = "2026-04-25",
                    endDate = "2026-04-25",
                    totalDays = 1,
                    reason = "Family relocation administrative appointment.",
                    status = LeaveStatus.PENDING,
                    submittedDate = "2026-04-09",
                    createdAt = 1712620800000L
                )
            )
        }
    }
}
