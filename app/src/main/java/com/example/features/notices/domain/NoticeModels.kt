package com.example.features.notices.domain

import com.example.core.tenant.TenantScopedEntity

/**
 * Notice thematic classifications.
 */
enum class NoticeCategory(val displayName: String, val iconName: String) {
    ACADEMIC("Academic Affairs", "School"),
    EXAMINATION("Examinations & Schedules", "DateRange"),
    EVENT("School Events & Sports", "EmojiEvents"),
    HOLIDAY("Holidays & Recess", "CalendarMonth"),
    ADMINISTRATIVE("Administration & Policy", "Apartment"),
    EMERGENCY("Safety & Emergency", "Warning");

    companion object {
        fun fromString(value: String?): NoticeCategory {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: ACADEMIC
        }
    }
}

/**
 * Notice broadcast urgency levels.
 */
enum class NoticePriority(val displayName: String) {
    NORMAL("Standard Notice"),
    IMPORTANT("Important Attention"),
    URGENT("Urgent Broadcast");

    companion object {
        fun fromString(value: String?): NoticePriority {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: NORMAL
        }
    }
}

/**
 * Target demographic audience for school broadcasts.
 */
enum class NoticeTargetAudience(val displayName: String) {
    ALL("Entire School Community"),
    STUDENTS("Students Cohort"),
    TEACHERS("Faculty & Staff"),
    PARENTS("Parents & Guardians"),
    STAFF("Administrative Staff");

    companion object {
        fun fromString(value: String?): NoticeTargetAudience {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: ALL
        }
    }
}

/**
 * School announcement and broadcast notice entity.
 * Scoped to `schools/{schoolId}/notices/{noticeId}` (Subcollection 10).
 */
data class SchoolNotice(
    override val id: String,
    override val schoolId: String,
    val title: String,
    val content: String,
    val category: NoticeCategory = NoticeCategory.ACADEMIC,
    val priority: NoticePriority = NoticePriority.NORMAL,
    val targetAudience: NoticeTargetAudience = NoticeTargetAudience.ALL,
    val authorId: String,
    val authorName: String,
    val publishedDate: String, // Format: YYYY-MM-DD
    val expiresDate: String = "",
    val isPinned: Boolean = false,
    val attachmentTitle: String = "",
    val createdAt: Long = System.currentTimeMillis()
) : TenantScopedEntity {

    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "id" to id,
        "schoolId" to schoolId,
        "title" to title,
        "content" to content,
        "category" to category.name,
        "priority" to priority.name,
        "targetAudience" to targetAudience.name,
        "authorId" to authorId,
        "authorName" to authorName,
        "publishedDate" to publishedDate,
        "expiresDate" to expiresDate,
        "isPinned" to isPinned,
        "attachmentTitle" to attachmentTitle,
        "createdAt" to createdAt
    )

    companion object {
        fun fromFirestoreMap(id: String, schoolId: String, data: Map<String, Any>): SchoolNotice {
            return SchoolNotice(
                id = id,
                schoolId = schoolId,
                title = data["title"] as? String ?: "",
                content = data["content"] as? String ?: "",
                category = NoticeCategory.fromString(data["category"] as? String),
                priority = NoticePriority.fromString(data["priority"] as? String),
                targetAudience = NoticeTargetAudience.fromString(data["targetAudience"] as? String),
                authorId = data["authorId"] as? String ?: "",
                authorName = data["authorName"] as? String ?: "",
                publishedDate = data["publishedDate"] as? String ?: "",
                expiresDate = data["expiresDate"] as? String ?: "",
                isPinned = data["isPinned"] as? Boolean ?: false,
                attachmentTitle = data["attachmentTitle"] as? String ?: "",
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}
