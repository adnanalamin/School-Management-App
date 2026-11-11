package com.example.features.attendance.data

import android.content.Context
import android.util.Log
import com.example.R
import com.example.core.error.ErrorHandler
import com.example.core.tenant.TenantPathProvider
import com.example.features.attendance.domain.AttendanceAuditLog
import com.example.features.attendance.domain.AttendanceRecord
import com.example.features.attendance.domain.AttendanceStatus
import com.example.features.attendance.domain.StudentAttendanceEntry
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AttendanceRepository(
    private val context: Context? = null,
    private val firestoreInstance: FirebaseFirestore? = null
) {
    private val db: FirebaseFirestore by lazy {
        firestoreInstance ?: try {
            val dbId = context?.getString(R.string.firestore_database_id)
            if (dbId != null) FirebaseFirestore.getInstance(dbId) else FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w("AttendanceRepository", "Fallback to default Firestore instance", e)
            FirebaseFirestore.getInstance()
        }
    }

    suspend fun getAttendanceRecords(
        schoolId: String = TenantPathProvider.requireActiveSchoolId(),
        date: String? = null,
        classId: String? = null,
        sectionId: String? = null
    ): List<AttendanceRecord> {
        return try {
            TenantPathProvider.verifyTenantBoundary(schoolId)
            val path = TenantPathProvider.attendancePath(schoolId)
            val snapshot = db.collection(path).get().await()
            val list = if (snapshot.isEmpty) {
                getPreSeededRecords(schoolId)
            } else {
                snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    AttendanceRecord.fromFirestoreMap(doc.id, schoolId, data)
                }
            }
            filterRecords(list, date, classId, sectionId)
        } catch (e: Exception) {
            Log.w("AttendanceRepository", "Error retrieving attendance from Firestore, using seeded records", e)
            val seeded = getPreSeededRecords(schoolId)
            filterRecords(seeded, date, classId, sectionId)
        }
    }

    private fun filterRecords(
        records: List<AttendanceRecord>,
        date: String?,
        classId: String?,
        sectionId: String?
    ): List<AttendanceRecord> {
        return records.filter { record ->
            (date == null || record.date == date) &&
            (classId == null || record.classId == classId) &&
            (sectionId == null || record.sectionId == sectionId)
        }
    }

    suspend fun getAttendanceRecordById(schoolId: String, recordId: String): AttendanceRecord? {
        return try {
            TenantPathProvider.verifyTenantBoundary(schoolId)
            val path = TenantPathProvider.attendancePath(schoolId)
            val doc = db.collection(path).document(recordId).get().await()
            if (doc.exists() && doc.data != null) {
                AttendanceRecord.fromFirestoreMap(doc.id, schoolId, doc.data!!)
            } else {
                getPreSeededRecords(schoolId).firstOrNull { it.id == recordId }
            }
        } catch (e: Exception) {
            getPreSeededRecords(schoolId).firstOrNull { it.id == recordId }
        }
    }

    suspend fun saveAttendanceRecord(record: AttendanceRecord): Result<AttendanceRecord> {
        return try {
            TenantPathProvider.verifyTenantBoundary(record.schoolId)
            val path = TenantPathProvider.attendancePath(record.schoolId)
            db.collection(path).document(record.id).set(record.toFirestoreMap()).await()
            Result.success(record)
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    suspend fun finalizeAttendanceRecord(
        schoolId: String,
        recordId: String,
        actorId: String,
        actorName: String,
        notes: String = "Official roster submission finalized."
    ): Result<AttendanceRecord> {
        return try {
            val existing = getAttendanceRecordById(schoolId, recordId)
                ?: return Result.failure(Exception("Attendance record '$recordId' not found"))

            val newAudit = AttendanceAuditLog(
                timestamp = System.currentTimeMillis(),
                actorId = actorId,
                actorName = actorName,
                action = "FINALIZED",
                notes = notes
            )
            val updated = existing.copy(
                isFinalized = true,
                auditTrail = existing.auditTrail + newAudit
            )
            saveAttendanceRecord(updated)
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    suspend fun reopenAttendanceRecord(
        schoolId: String,
        recordId: String,
        actorId: String,
        actorName: String,
        reason: String
    ): Result<AttendanceRecord> {
        return try {
            val existing = getAttendanceRecordById(schoolId, recordId)
                ?: return Result.failure(Exception("Attendance record '$recordId' not found"))

            val newAudit = AttendanceAuditLog(
                timestamp = System.currentTimeMillis(),
                actorId = actorId,
                actorName = actorName,
                action = "REOPENED_FOR_EDIT",
                notes = "Unlocked by authorized user. Reason: $reason"
            )
            val updated = existing.copy(
                isFinalized = false,
                auditTrail = existing.auditTrail + newAudit
            )
            saveAttendanceRecord(updated)
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    fun getPreSeededRecords(schoolId: String): List<AttendanceRecord> {
        return if (schoolId.contains("oxford") || schoolId.startsWith("sch_oxf")) {
            val todayEntries = listOf(
                StudentAttendanceEntry("stu_oxf_01", "Amina Rahman", "OXF-10A-001", AttendanceStatus.PRESENT, "Prompt on time"),
                StudentAttendanceEntry("stu_oxf_02", "Oliver Smith", "OXF-10A-002", AttendanceStatus.PRESENT, ""),
                StudentAttendanceEntry("stu_oxf_03", "Sophia Chen", "OXF-11S-003", AttendanceStatus.PRESENT, ""),
                StudentAttendanceEntry("stu_oxf_04", "Lucas Vance", "OXF-09B-004", AttendanceStatus.LATE, "School bus delay (Route 4)"),
                StudentAttendanceEntry("stu_oxf_05", "Maya Patel", "OXF-12A-005", AttendanceStatus.EXCUSED, "Medical checkup appointment (excusal letter on file)")
            )
            val todayAudit = listOf(
                AttendanceAuditLog(
                    timestamp = System.currentTimeMillis() - 7200000,
                    actorId = "tea_oxf_01",
                    actorName = "Dr. Sarah Jenkins",
                    action = "INITIAL_CREATION",
                    notes = "Roll call commenced at 08:30 AM"
                ),
                AttendanceAuditLog(
                    timestamp = System.currentTimeMillis() - 3600000,
                    actorId = "tea_oxf_01",
                    actorName = "Dr. Sarah Jenkins",
                    action = "FINALIZED",
                    notes = "Daily homeroom attendance finalized and verified"
                )
            )

            val yesterdayEntries = listOf(
                StudentAttendanceEntry("stu_oxf_01", "Amina Rahman", "OXF-10A-001", AttendanceStatus.PRESENT, ""),
                StudentAttendanceEntry("stu_oxf_02", "Oliver Smith", "OXF-10A-002", AttendanceStatus.PRESENT, ""),
                StudentAttendanceEntry("stu_oxf_03", "Sophia Chen", "OXF-11S-003", AttendanceStatus.PRESENT, ""),
                StudentAttendanceEntry("stu_oxf_04", "Lucas Vance", "OXF-09B-004", AttendanceStatus.PRESENT, ""),
                StudentAttendanceEntry("stu_oxf_05", "Maya Patel", "OXF-12A-005", AttendanceStatus.ABSENT, "Unexcused absence - SMS notice dispatched")
            )

            listOf(
                AttendanceRecord.createWithMetrics(
                    id = "att_oxf_10a_today",
                    schoolId = schoolId,
                    date = "2026-03-24",
                    classId = "cls_oxf_g10",
                    className = "Grade 10",
                    sectionId = "sec_oxf_10a",
                    sectionName = "Section A",
                    markedById = "tea_oxf_01",
                    markedByName = "Dr. Sarah Jenkins",
                    entries = todayEntries,
                    auditTrail = todayAudit,
                    isFinalized = true
                ),
                AttendanceRecord.createWithMetrics(
                    id = "att_oxf_10a_yesterday",
                    schoolId = schoolId,
                    date = "2026-03-23",
                    classId = "cls_oxf_g10",
                    className = "Grade 10",
                    sectionId = "sec_oxf_10a",
                    sectionName = "Section A",
                    markedById = "tea_oxf_01",
                    markedByName = "Dr. Sarah Jenkins",
                    entries = yesterdayEntries,
                    auditTrail = listOf(
                        AttendanceAuditLog(
                            timestamp = System.currentTimeMillis() - 90000000,
                            actorId = "tea_oxf_01",
                            actorName = "Dr. Sarah Jenkins",
                            action = "FINALIZED",
                            notes = "Roster locked"
                        )
                    ),
                    isFinalized = true
                ),
                AttendanceRecord.createWithMetrics(
                    id = "att_oxf_11a_today",
                    schoolId = schoolId,
                    date = "2026-03-24",
                    classId = "cls_oxf_g11",
                    className = "Grade 11",
                    sectionId = "sec_oxf_11a",
                    sectionName = "Section A",
                    markedById = "tea_oxf_02",
                    markedByName = "Prof. Alan Turing",
                    entries = listOf(
                        StudentAttendanceEntry("stu_oxf_03", "Sophia Chen", "OXF-11S-003", AttendanceStatus.PRESENT, "")
                    ),
                    auditTrail = emptyList(),
                    isFinalized = false
                )
            )
        } else {
            val cambridgeEntries = listOf(
                StudentAttendanceEntry("stu_cam_01", "Liam Davies", "CAM-10C-101", AttendanceStatus.PRESENT, ""),
                StudentAttendanceEntry("stu_cam_02", "Emma Watson", "CAM-11A-102", AttendanceStatus.PRESENT, ""),
                StudentAttendanceEntry("stu_cam_03", "Noah Wilson", "CAM-12S-103", AttendanceStatus.LATE, "Train transit delay")
            )
            listOf(
                AttendanceRecord.createWithMetrics(
                    id = "att_cam_10a_today",
                    schoolId = schoolId,
                    date = "2026-03-24",
                    classId = "cls_cam_g10",
                    className = "Year 10",
                    sectionId = "sec_cam_10a",
                    sectionName = "Section A",
                    markedById = "tea_cam_02",
                    markedByName = "Jane Austen",
                    entries = cambridgeEntries,
                    auditTrail = listOf(
                        AttendanceAuditLog(
                            timestamp = System.currentTimeMillis() - 5000000,
                            actorId = "tea_cam_02",
                            actorName = "Jane Austen",
                            action = "FINALIZED",
                            notes = "Morning register recorded."
                        )
                    ),
                    isFinalized = true
                )
            )
        }
    }
}
