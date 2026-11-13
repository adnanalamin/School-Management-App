package com.example.features.messages.domain

import com.example.core.tenant.TenantScopedEntity

/**
 * Communication channel categories within an isolated school tenant.
 */
enum class MessageChannelType(val displayName: String) {
    DIRECT("Direct Message"),
    PARENT_TEACHER("Parent–Teacher"),
    FACULTY_GROUP("Faculty & Staff"),
    CLASS_CHANNEL("Class Channel"),
    ADMIN_DESK("Administration Desk");

    companion object {
        fun fromString(value: String?): MessageChannelType {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: DIRECT
        }
    }
}

/**
 * Urgency classification for internal communication threads.
 */
enum class MessagePriority(val displayName: String) {
    NORMAL("Standard"),
    IMPORTANT("Important"),
    URGENT("Urgent");

    companion object {
        fun fromString(value: String?): MessagePriority {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: NORMAL
        }
    }
}

/**
 * Individual message payload inside a tenant-isolated conversation thread.
 */
data class ChatMessageEntry(
    val id: String,
    val senderId: String,
    val senderName: String,
    val senderRole: String,
    val body: String,
    val sentAtLabel: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "senderId" to senderId,
        "senderName" to senderName,
        "senderRole" to senderRole,
        "body" to body,
        "sentAtLabel" to sentAtLabel,
        "timestamp" to timestamp
    )

    companion object {
        fun fromMap(data: Map<String, Any>): ChatMessageEntry {
            return ChatMessageEntry(
                id = data["id"] as? String ?: "",
                senderId = data["senderId"] as? String ?: "",
                senderName = data["senderName"] as? String ?: "",
                senderRole = data["senderRole"] as? String ?: "",
                body = data["body"] as? String ?: "",
                sentAtLabel = data["sentAtLabel"] as? String ?: "",
                timestamp = (data["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}

/**
 * Multi-tenant Secure Internal Messaging Thread.
 * Scoped to `schools/{schoolId}/messages/{threadId}`.
 */
data class MessageThread(
    override val id: String,
    override val schoolId: String,
    val threadCode: String,
    val subject: String,
    val channelType: MessageChannelType = MessageChannelType.DIRECT,
    val priority: MessagePriority = MessagePriority.NORMAL,
    val participantsLabel: String,
    val participantRoles: List<String> = emptyList(),
    val messages: List<ChatMessageEntry> = emptyList(),
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val lastActivityLabel: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : TenantScopedEntity {

    val latestMessagePreview: String
        get() = messages.lastOrNull()?.body ?: ""

    val messageCount: Int
        get() = messages.size

    val hasUnread: Boolean
        get() = unreadCount > 0

    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "id" to id,
        "schoolId" to schoolId,
        "threadCode" to threadCode,
        "subject" to subject,
        "channelType" to channelType.name,
        "priority" to priority.name,
        "participantsLabel" to participantsLabel,
        "participantRoles" to participantRoles,
        "messages" to messages.map { it.toMap() },
        "unreadCount" to unreadCount,
        "isPinned" to isPinned,
        "isArchived" to isArchived,
        "lastActivityLabel" to lastActivityLabel,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    )

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromFirestoreMap(id: String, schoolId: String, data: Map<String, Any>): MessageThread {
            val rawMessages = (data["messages"] as? List<*>)
                ?.mapNotNull { it as? Map<String, Any> }
                ?.map { ChatMessageEntry.fromMap(it) }
                ?: emptyList()
            val rawRoles = (data["participantRoles"] as? List<*>)
                ?.mapNotNull { it as? String }
                ?: emptyList()

            return MessageThread(
                id = id,
                schoolId = schoolId,
                threadCode = data["threadCode"] as? String ?: "",
                subject = data["subject"] as? String ?: "",
                channelType = MessageChannelType.fromString(data["channelType"] as? String),
                priority = MessagePriority.fromString(data["priority"] as? String),
                participantsLabel = data["participantsLabel"] as? String ?: "",
                participantRoles = rawRoles,
                messages = rawMessages,
                unreadCount = (data["unreadCount"] as? Number)?.toInt() ?: 0,
                isPinned = data["isPinned"] as? Boolean ?: false,
                isArchived = data["isArchived"] as? Boolean ?: false,
                lastActivityLabel = data["lastActivityLabel"] as? String ?: "",
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}
