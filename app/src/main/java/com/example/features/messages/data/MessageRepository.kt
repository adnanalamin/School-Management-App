package com.example.features.messages.data

import android.content.Context
import android.util.Log
import com.example.R
import com.example.core.error.ErrorHandler
import com.example.core.tenant.TenantPathProvider
import com.example.features.messages.domain.ChatMessageEntry
import com.example.features.messages.domain.MessageChannelType
import com.example.features.messages.domain.MessagePriority
import com.example.features.messages.domain.MessageThread
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MessageRepository(
    private val context: Context? = null,
    private val firestoreInstance: FirebaseFirestore? = null
) {
    private val db: FirebaseFirestore by lazy {
        firestoreInstance ?: try {
            val dbId = context?.getString(R.string.firestore_database_id)
            if (dbId != null) FirebaseFirestore.getInstance(dbId) else FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w("MessageRepository", "Fallback to default Firestore instance", e)
            FirebaseFirestore.getInstance()
        }
    }

    suspend fun getThreads(
        schoolId: String = TenantPathProvider.requireActiveSchoolId(),
        channelFilter: MessageChannelType? = null
    ): List<MessageThread> {
        return try {
            TenantPathProvider.verifyTenantBoundary(schoolId)
            val path = TenantPathProvider.messagesPath(schoolId)
            val snapshot = db.collection(path).get().await()
            val list = if (snapshot.isEmpty) {
                getPreSeededThreads(schoolId)
            } else {
                snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    MessageThread.fromFirestoreMap(doc.id, schoolId, data)
                }
            }
            if (channelFilter == null) list else list.filter { it.channelType == channelFilter }
        } catch (e: Exception) {
            Log.w("MessageRepository", "Error retrieving message threads from Firestore, fallback to preseeded", e)
            val seeded = getPreSeededThreads(schoolId)
            if (channelFilter == null) seeded else seeded.filter { it.channelType == channelFilter }
        }
    }

    suspend fun saveThread(thread: MessageThread): Result<MessageThread> {
        return try {
            TenantPathProvider.verifyTenantBoundary(thread.schoolId)
            val path = TenantPathProvider.messagesPath(thread.schoolId)
            db.collection(path).document(thread.id).set(thread.toFirestoreMap()).await()
            Result.success(thread)
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    suspend fun sendReply(
        schoolId: String,
        threadId: String,
        reply: ChatMessageEntry
    ): Result<MessageThread> {
        return try {
            TenantPathProvider.verifyTenantBoundary(schoolId)
            val threads = getThreads(schoolId)
            val target = threads.firstOrNull { it.id == threadId }
                ?: return Result.failure(Exception("Conversation thread not found"))

            val updated = target.copy(
                messages = target.messages + reply,
                unreadCount = 0,
                lastActivityLabel = reply.sentAtLabel,
                updatedAt = reply.timestamp
            )
            saveThread(updated)
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    suspend fun markThreadRead(schoolId: String, threadId: String): Result<MessageThread> {
        return try {
            TenantPathProvider.verifyTenantBoundary(schoolId)
            val threads = getThreads(schoolId)
            val target = threads.firstOrNull { it.id == threadId }
                ?: return Result.failure(Exception("Conversation thread not found"))

            val updated = target.copy(unreadCount = 0)
            saveThread(updated)
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    suspend fun togglePin(schoolId: String, threadId: String): Result<MessageThread> {
        return try {
            TenantPathProvider.verifyTenantBoundary(schoolId)
            val threads = getThreads(schoolId)
            val target = threads.firstOrNull { it.id == threadId }
                ?: return Result.failure(Exception("Conversation thread not found"))

            val updated = target.copy(isPinned = !target.isPinned)
            saveThread(updated)
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    suspend fun toggleArchive(schoolId: String, threadId: String): Result<MessageThread> {
        return try {
            TenantPathProvider.verifyTenantBoundary(schoolId)
            val threads = getThreads(schoolId)
            val target = threads.firstOrNull { it.id == threadId }
                ?: return Result.failure(Exception("Conversation thread not found"))

            val updated = target.copy(isArchived = !target.isArchived)
            saveThread(updated)
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    suspend fun deleteThread(schoolId: String, threadId: String): Result<Unit> {
        return try {
            TenantPathProvider.verifyTenantBoundary(schoolId)
            val path = TenantPathProvider.messagesPath(schoolId)
            db.collection(path).document(threadId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    fun getPreSeededThreads(schoolId: String): List<MessageThread> {
        return if (schoolId.contains("oxford") || schoolId.startsWith("sch_oxf")) {
            listOf(
                MessageThread(
                    id = "msg_oxf_01",
                    schoolId = schoolId,
                    threadCode = "MSG-OXF-301",
                    subject = "Grade 10 Physics Lab Progress — Amina Rahman",
                    channelType = MessageChannelType.PARENT_TEACHER,
                    priority = MessagePriority.IMPORTANT,
                    participantsLabel = "Dr. Sarah Jenkins • David Rahman",
                    participantRoles = listOf("TEACHER", "PARENT"),
                    messages = listOf(
                        ChatMessageEntry(
                            id = "m_oxf_1",
                            senderId = "tch_oxf_001",
                            senderName = "Dr. Sarah Jenkins",
                            senderRole = "Faculty",
                            body = "Hello Mr. Rahman, Amina led her lab group in the wave optics experiment this week with top marks.",
                            sentAtLabel = "Today, 09:15 AM",
                            timestamp = 1712650500000L
                        ),
                        ChatMessageEntry(
                            id = "m_oxf_2",
                            senderId = "par_oxf_001",
                            senderName = "David Rahman",
                            senderRole = "Parent",
                            body = "Thank you Dr. Jenkins! Please share the Science Olympiad lab prep schedule when ready.",
                            sentAtLabel = "Today, 10:02 AM",
                            timestamp = 1712653320000L
                        )
                    ),
                    unreadCount = 2,
                    isPinned = true,
                    isArchived = false,
                    lastActivityLabel = "Today, 10:02 AM",
                    createdAt = 1712650500000L,
                    updatedAt = 1712653320000L
                ),
                MessageThread(
                    id = "msg_oxf_02",
                    schoolId = schoolId,
                    threadCode = "MSG-OXF-302",
                    subject = "Morning Bus Route R-101 Temporary Detour",
                    channelType = MessageChannelType.ADMIN_DESK,
                    priority = MessagePriority.URGENT,
                    participantsLabel = "Arthur Pendelton • Dr. Robert Vance",
                    participantRoles = listOf("TRANSPORT_MANAGER", "SCHOOL_ADMIN"),
                    messages = listOf(
                        ChatMessageEntry(
                            id = "m_oxf_3",
                            senderId = "trn_oxf_001",
                            senderName = "Arthur Pendelton",
                            senderRole = "Transport Manager",
                            body = "Oakridge Ave has roadwork until Friday; Bus OXF-BUS-04 will use Beacon St and arrive 5 mins early.",
                            sentAtLabel = "Today, 07:45 AM",
                            timestamp = 1712645100000L
                        ),
                        ChatMessageEntry(
                            id = "m_oxf_4",
                            senderId = "usr_admin_01",
                            senderName = "Dr. Robert Vance",
                            senderRole = "School Admin",
                            body = "Acknowledged. Please send an automated morning ETA note to Route R-101 parents.",
                            sentAtLabel = "Today, 08:05 AM",
                            timestamp = 1712646300000L
                        )
                    ),
                    unreadCount = 1,
                    isPinned = false,
                    isArchived = false,
                    lastActivityLabel = "Today, 08:05 AM",
                    createdAt = 1712645100000L,
                    updatedAt = 1712646300000L
                ),
                MessageThread(
                    id = "msg_oxf_03",
                    schoolId = schoolId,
                    threadCode = "MSG-OXF-303",
                    subject = "Mid-Term Exam Paper Moderation Schedule",
                    channelType = MessageChannelType.FACULTY_GROUP,
                    priority = MessagePriority.NORMAL,
                    participantsLabel = "Oxford Science & Math Faculty",
                    participantRoles = listOf("SCHOOL_ADMIN", "TEACHER"),
                    messages = listOf(
                        ChatMessageEntry(
                            id = "m_oxf_5",
                            senderId = "usr_admin_01",
                            senderName = "Dr. Robert Vance",
                            senderRole = "School Admin",
                            body = "Department heads should upload sealed question blueprints in the Documents Vault by Thursday 4 PM.",
                            sentAtLabel = "Yesterday, 04:20 PM",
                            timestamp = 1712589600000L
                        ),
                        ChatMessageEntry(
                            id = "m_oxf_6",
                            senderId = "tch_oxf_001",
                            senderName = "Dr. Sarah Jenkins",
                            senderRole = "Faculty",
                            body = "Physics & Lab rubrics are finalized and uploaded for review.",
                            sentAtLabel = "Yesterday, 05:10 PM",
                            timestamp = 1712592600000L
                        )
                    ),
                    unreadCount = 0,
                    isPinned = true,
                    isArchived = false,
                    lastActivityLabel = "Yesterday, 05:10 PM",
                    createdAt = 1712589600000L,
                    updatedAt = 1712592600000L
                ),
                MessageThread(
                    id = "msg_oxf_04",
                    schoolId = schoolId,
                    threadCode = "MSG-OXF-304",
                    subject = "Science Olympiad Team Briefing & Lab Slots",
                    channelType = MessageChannelType.CLASS_CHANNEL,
                    priority = MessagePriority.NORMAL,
                    participantsLabel = "Grade 10 Physics Cohort",
                    participantRoles = listOf("TEACHER", "STUDENT"),
                    messages = listOf(
                        ChatMessageEntry(
                            id = "m_oxf_7",
                            senderId = "tch_oxf_001",
                            senderName = "Dr. Sarah Jenkins",
                            senderRole = "Faculty",
                            body = "Practice sessions run Tuesday and Thursday at 3:30 PM in Lab 2.",
                            sentAtLabel = "2 days ago",
                            timestamp = 1712496000000L
                        )
                    ),
                    unreadCount = 0,
                    isPinned = false,
                    isArchived = false,
                    lastActivityLabel = "2 days ago",
                    createdAt = 1712496000000L,
                    updatedAt = 1712496000000L
                )
            )
        } else {
            listOf(
                MessageThread(
                    id = "msg_cam_01",
                    schoolId = schoolId,
                    threadCode = "MSG-CAM-401",
                    subject = "Cambridge Debate Finals Travel Itinerary",
                    channelType = MessageChannelType.CLASS_CHANNEL,
                    priority = MessagePriority.IMPORTANT,
                    participantsLabel = "Prof. Eleanor Vance • Year 10 Honors",
                    participantRoles = listOf("TEACHER", "STUDENT", "PARENT"),
                    messages = listOf(
                        ChatMessageEntry(
                            id = "m_cam_1",
                            senderId = "tch_cam_001",
                            senderName = "Prof. Eleanor Vance",
                            senderRole = "Faculty",
                            body = "Departure from North Gate is 07:30 AM sharp on Friday. Please bring school ID badges.",
                            sentAtLabel = "Today, 09:00 AM",
                            timestamp = 1712649600000L
                        )
                    ),
                    unreadCount = 1,
                    isPinned = true,
                    isArchived = false,
                    lastActivityLabel = "Today, 09:00 AM",
                    createdAt = 1712649600000L,
                    updatedAt = 1712649600000L
                ),
                MessageThread(
                    id = "msg_cam_02",
                    schoolId = schoolId,
                    threadCode = "MSG-CAM-402",
                    subject = "Library Rare Manuscripts Exhibition Coordination",
                    channelType = MessageChannelType.FACULTY_GROUP,
                    priority = MessagePriority.NORMAL,
                    participantsLabel = "Cambridge Faculty & Librarian",
                    participantRoles = listOf("LIBRARIAN", "TEACHER"),
                    messages = listOf(
                        ChatMessageEntry(
                            id = "m_cam_2",
                            senderId = "lib_cam_001",
                            senderName = "Eleanor Rigby",
                            senderRole = "Librarian",
                            body = "Reading Room B is reserved for Year 10 Literature seminars all next week.",
                            sentAtLabel = "Yesterday",
                            timestamp = 1712563200000L
                        )
                    ),
                    unreadCount = 0,
                    isPinned = false,
                    isArchived = false,
                    lastActivityLabel = "Yesterday",
                    createdAt = 1712563200000L,
                    updatedAt = 1712563200000L
                )
            )
        }
    }
}
