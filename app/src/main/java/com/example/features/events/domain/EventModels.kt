package com.example.features.events.domain

import com.example.core.tenant.TenantScopedEntity

/**
 * Classification categories for institutional calendar events and observances.
 */
enum class EventCategory(val displayName: String) {
    ACADEMIC("Academic Calendar"),
    SPORTS("Sports & Athletics"),
    CULTURAL("Cultural & Arts"),
    HOLIDAY("School Holiday"),
    MEETING("PTA & Governance"),
    SCIENCE_FAIR("STEM & Exhibitions");

    companion object {
        fun fromString(value: String?): EventCategory {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: ACADEMIC
        }
    }
}

/**
 * Lifecycle scheduling state of a school event.
 */
enum class EventStatus(val displayName: String) {
    UPCOMING("Upcoming"),
    ONGOING("Happening Today"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    companion object {
        fun fromString(value: String?): EventStatus {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: UPCOMING
        }
    }
}

/**
 * Attendee RSVP entry registered for a school calendar event.
 */
data class EventRsvpRecord(
    val rsvpId: String,
    val userId: String,
    val attendeeName: String,
    val attendeeRole: String = "Parent",
    val guestsCount: Int = 1,
    val notes: String = "",
    val respondedAt: Long = System.currentTimeMillis()
) {
    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "rsvpId" to rsvpId,
        "userId" to userId,
        "attendeeName" to attendeeName,
        "attendeeRole" to attendeeRole,
        "guestsCount" to guestsCount,
        "notes" to notes,
        "respondedAt" to respondedAt
    )

    companion object {
        fun fromFirestoreMap(data: Map<String, Any?>): EventRsvpRecord {
            return EventRsvpRecord(
                rsvpId = data["rsvpId"] as? String ?: "",
                userId = data["userId"] as? String ?: "",
                attendeeName = data["attendeeName"] as? String ?: "",
                attendeeRole = data["attendeeRole"] as? String ?: "Parent",
                guestsCount = (data["guestsCount"] as? Number)?.toInt() ?: 1,
                notes = data["notes"] as? String ?: "",
                respondedAt = (data["respondedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}

/**
 * Multi-tenant school event & academic calendar record.
 * Scoped to `schools/{schoolId}/events/{eventId}` (Subcollection 14).
 */
data class SchoolEvent(
    override val id: String,
    override val schoolId: String,
    val title: String,
    val description: String,
    val category: EventCategory = EventCategory.ACADEMIC,
    val status: EventStatus = EventStatus.UPCOMING,
    val eventDate: String,
    val startTime: String = "09:00 AM",
    val endTime: String = "02:00 PM",
    val venue: String,
    val organizerName: String,
    val targetAudience: String = "All Students, Parents & Faculty",
    val isHoliday: Boolean = false,
    val rsvpRequired: Boolean = false,
    val maxCapacity: Int = 0,
    val rsvps: List<EventRsvpRecord> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
) : TenantScopedEntity {

    val totalRsvpHeadcount: Int
        get() = rsvps.sumOf { it.guestsCount.coerceAtLeast(1) }

    val remainingRsvpSlots: Int
        get() = if (maxCapacity > 0) (maxCapacity - totalRsvpHeadcount).coerceAtLeast(0) else Int.MAX_VALUE

    val isRsvpOpen: Boolean
        get() = rsvpRequired &&
            status == EventStatus.UPCOMING &&
            (maxCapacity == 0 || remainingRsvpSlots > 0)

    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "id" to id,
        "schoolId" to schoolId,
        "title" to title,
        "description" to description,
        "category" to category.name,
        "status" to status.name,
        "eventDate" to eventDate,
        "startTime" to startTime,
        "endTime" to endTime,
        "venue" to venue,
        "organizerName" to organizerName,
        "targetAudience" to targetAudience,
        "isHoliday" to isHoliday,
        "rsvpRequired" to rsvpRequired,
        "maxCapacity" to maxCapacity,
        "rsvps" to rsvps.map { it.toFirestoreMap() },
        "createdAt" to createdAt
    )

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromFirestoreMap(id: String, schoolId: String, data: Map<String, Any>): SchoolEvent {
            val rawRsvps = data["rsvps"] as? List<Map<String, Any?>> ?: emptyList()

            return SchoolEvent(
                id = id,
                schoolId = schoolId,
                title = data["title"] as? String ?: "",
                description = data["description"] as? String ?: "",
                category = EventCategory.fromString(data["category"] as? String),
                status = EventStatus.fromString(data["status"] as? String),
                eventDate = data["eventDate"] as? String ?: "",
                startTime = data["startTime"] as? String ?: "09:00 AM",
                endTime = data["endTime"] as? String ?: "02:00 PM",
                venue = data["venue"] as? String ?: "",
                organizerName = data["organizerName"] as? String ?: "",
                targetAudience = data["targetAudience"] as? String ?: "All Students, Parents & Faculty",
                isHoliday = data["isHoliday"] as? Boolean ?: false,
                rsvpRequired = data["rsvpRequired"] as? Boolean ?: false,
                maxCapacity = (data["maxCapacity"] as? Number)?.toInt() ?: 0,
                rsvps = rawRsvps.map { EventRsvpRecord.fromFirestoreMap(it) },
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}
