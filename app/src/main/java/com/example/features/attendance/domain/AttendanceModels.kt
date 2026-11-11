package com.example.features.attendance.domain

import com.example.core.tenant.TenantScopedEntity

/**
 * Attendance status enum for student and teacher daily rosters.
 */
enum class AttendanceStatus(val displayName: String, val code: String) {
    PRESENT("Present", "P"),
    ABSENT("Absent", "A"),
    LATE("Late", "L"),
    EXCUSED("Excused", "E");

    companion object {
        fun fromString(value: String?): AttendanceStatus {
            return entries.firstOrNull {
                it.name.equals(value, ignoreCase = true) || it.code.equals(value, ignoreCase = true)
            } ?: PRESENT
        }
    }
}

/**
 * Individual student attendance entry within a daily register.
 */
data class StudentAttendanceEntry(
    val studentId: String,
    val studentName: String,
    val rollNumber: String,
    val status: AttendanceStatus = AttendanceStatus.PRESENT,
    val remarks: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "studentId" to studentId,
        "studentName" to studentName,
        "rollNumber" to rollNumber,
        "status" to status.name,
        "remarks" to remarks,
        "timestamp" to timestamp
    )

    companion object {
        fun fromFirestoreMap(data: Map<String, Any>): StudentAttendanceEntry {
            return StudentAttendanceEntry(
                studentId = data["studentId"] as? String ?: "",
                studentName = data["studentName"] as? String ?: "",
                rollNumber = data["rollNumber"] as? String ?: "",
                status = AttendanceStatus.fromString(data["status"] as? String),
                remarks = data["remarks"] as? String ?: "",
                timestamp = (data["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}

/**
 * Immutable audit log record capturing changes and finalization events in the attendance register.
 */
data class AttendanceAuditLog(
    val timestamp: Long = System.currentTimeMillis(),
    val actorId: String,
    val actorName: String,
    val action: String,
    val notes: String = ""
) {
    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "timestamp" to timestamp,
        "actorId" to actorId,
        "actorName" to actorName,
        "action" to action,
        "notes" to notes
    )

    companion object {
        fun fromFirestoreMap(data: Map<String, Any>): AttendanceAuditLog {
            return AttendanceAuditLog(
                timestamp = (data["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                actorId = data["actorId"] as? String ?: "",
                actorName = data["actorName"] as? String ?: "",
                action = data["action"] as? String ?: "",
                notes = data["notes"] as? String ?: ""
            )
        }
    }
}

/**
 * AttendanceRecord represents the complete roll call register for a class/section on a given date.
 * Strictly scoped to `schools/{schoolId}/attendance/{attendanceId}`.
 */
data class AttendanceRecord(
    override val id: String,
    override val schoolId: String,
    val date: String, // Format: YYYY-MM-DD
    val classId: String,
    val className: String,
    val sectionId: String,
    val sectionName: String,
    val markedById: String,
    val markedByName: String,
    val isFinalized: Boolean = false,
    val totalStudents: Int = 0,
    val presentCount: Int = 0,
    val absentCount: Int = 0,
    val lateCount: Int = 0,
    val excusedCount: Int = 0,
    val attendancePercentage: Float = 0.0f,
    val entries: List<StudentAttendanceEntry> = emptyList(),
    val auditTrail: List<AttendanceAuditLog> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
) : TenantScopedEntity {

    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "id" to id,
        "schoolId" to schoolId,
        "date" to date,
        "classId" to classId,
        "className" to className,
        "sectionId" to sectionId,
        "sectionName" to sectionName,
        "markedById" to markedById,
        "markedByName" to markedByName,
        "isFinalized" to isFinalized,
        "totalStudents" to totalStudents,
        "presentCount" to presentCount,
        "absentCount" to absentCount,
        "lateCount" to lateCount,
        "excusedCount" to excusedCount,
        "attendancePercentage" to attendancePercentage.toDouble(),
        "entries" to entries.map { it.toFirestoreMap() },
        "auditTrail" to auditTrail.map { it.toFirestoreMap() },
        "createdAt" to createdAt
    )

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromFirestoreMap(id: String, schoolId: String, data: Map<String, Any>): AttendanceRecord {
            val entriesRaw = data["entries"] as? List<Map<String, Any>> ?: emptyList()
            val parsedEntries = entriesRaw.map { StudentAttendanceEntry.fromFirestoreMap(it) }

            val auditRaw = data["auditTrail"] as? List<Map<String, Any>> ?: emptyList()
            val parsedAudit = auditRaw.map { AttendanceAuditLog.fromFirestoreMap(it) }

            return AttendanceRecord(
                id = id,
                schoolId = schoolId,
                date = data["date"] as? String ?: "",
                classId = data["classId"] as? String ?: "",
                className = data["className"] as? String ?: "",
                sectionId = data["sectionId"] as? String ?: "",
                sectionName = data["sectionName"] as? String ?: "",
                markedById = data["markedById"] as? String ?: "",
                markedByName = data["markedByName"] as? String ?: "",
                isFinalized = data["isFinalized"] as? Boolean ?: false,
                totalStudents = (data["totalStudents"] as? Number)?.toInt() ?: parsedEntries.size,
                presentCount = (data["presentCount"] as? Number)?.toInt() ?: parsedEntries.count { it.status == AttendanceStatus.PRESENT },
                absentCount = (data["absentCount"] as? Number)?.toInt() ?: parsedEntries.count { it.status == AttendanceStatus.ABSENT },
                lateCount = (data["lateCount"] as? Number)?.toInt() ?: parsedEntries.count { it.status == AttendanceStatus.LATE },
                excusedCount = (data["excusedCount"] as? Number)?.toInt() ?: parsedEntries.count { it.status == AttendanceStatus.EXCUSED },
                attendancePercentage = (data["attendancePercentage"] as? Number)?.toFloat() ?: 0.0f,
                entries = parsedEntries,
                auditTrail = parsedAudit,
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }

        fun createWithMetrics(
            id: String,
            schoolId: String,
            date: String,
            classId: String,
            className: String,
            sectionId: String,
            sectionName: String,
            markedById: String,
            markedByName: String,
            entries: List<StudentAttendanceEntry>,
            auditTrail: List<AttendanceAuditLog> = emptyList(),
            isFinalized: Boolean = false,
            createdAt: Long = System.currentTimeMillis()
        ): AttendanceRecord {
            val total = entries.size
            val present = entries.count { it.status == AttendanceStatus.PRESENT }
            val absent = entries.count { it.status == AttendanceStatus.ABSENT }
            val late = entries.count { it.status == AttendanceStatus.LATE }
            val excused = entries.count { it.status == AttendanceStatus.EXCUSED }
            // Effective present includes Present and Late
            val effectivePresent = present + late
            val percentage = if (total > 0) {
                ((effectivePresent.toFloat() / total.toFloat()) * 1000f).toInt() / 10f
            } else 0.0f

            return AttendanceRecord(
                id = id,
                schoolId = schoolId,
                date = date,
                classId = classId,
                className = className,
                sectionId = sectionId,
                sectionName = sectionName,
                markedById = markedById,
                markedByName = markedByName,
                isFinalized = isFinalized,
                totalStudents = total,
                presentCount = present,
                absentCount = absent,
                lateCount = late,
                excusedCount = excused,
                attendancePercentage = percentage,
                entries = entries,
                auditTrail = auditTrail,
                createdAt = createdAt
            )
        }
    }
}
