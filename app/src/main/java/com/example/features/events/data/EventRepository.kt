package com.example.features.events.data

import android.content.Context
import android.util.Log
import com.example.R
import com.example.core.error.ErrorHandler
import com.example.core.tenant.TenantPathProvider
import com.example.features.events.domain.EventCategory
import com.example.features.events.domain.EventRsvpRecord
import com.example.features.events.domain.EventStatus
import com.example.features.events.domain.SchoolEvent
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class EventRepository(
    private val context: Context? = null,
    private val firestoreInstance: FirebaseFirestore? = null
) {
    private val db: FirebaseFirestore by lazy {
        firestoreInstance ?: try {
            val dbId = context?.getString(R.string.firestore_database_id)
            if (dbId != null) FirebaseFirestore.getInstance(dbId) else FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w("EventRepository", "Fallback to default Firestore instance", e)
            FirebaseFirestore.getInstance()
        }
    }

    suspend fun getEvents(
        schoolId: String = TenantPathProvider.requireActiveSchoolId(),
        categoryFilter: EventCategory? = null,
        statusFilter: EventStatus? = null
    ): List<SchoolEvent> {
        return try {
            TenantPathProvider.verifyTenantBoundary(schoolId)
            val path = TenantPathProvider.eventsPath(schoolId)
            val snapshot = db.collection(path).get().await()
            val list = if (snapshot.isEmpty) {
                getPreSeededEvents(schoolId)
            } else {
                snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    SchoolEvent.fromFirestoreMap(doc.id, schoolId, data)
                }
            }
            filterEvents(list, categoryFilter, statusFilter)
        } catch (e: Exception) {
            Log.w("EventRepository", "Error retrieving school events from Firestore, fallback to preseeded", e)
            val seeded = getPreSeededEvents(schoolId)
            filterEvents(seeded, categoryFilter, statusFilter)
        }
    }

    private fun filterEvents(
        events: List<SchoolEvent>,
        categoryFilter: EventCategory?,
        statusFilter: EventStatus?
    ): List<SchoolEvent> {
        return events.filter { event ->
            (categoryFilter == null || event.category == categoryFilter) &&
                (statusFilter == null || event.status == statusFilter)
        }
    }

    suspend fun saveEvent(event: SchoolEvent): Result<SchoolEvent> {
        return try {
            TenantPathProvider.verifyTenantBoundary(event.schoolId)
            val path = TenantPathProvider.eventsPath(event.schoolId)
            db.collection(path).document(event.id).set(event.toFirestoreMap()).await()
            Result.success(event)
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    suspend fun rsvpToEvent(
        schoolId: String,
        eventId: String,
        rsvp: EventRsvpRecord
    ): Result<SchoolEvent> {
        return try {
            TenantPathProvider.verifyTenantBoundary(schoolId)
            val events = getEvents(schoolId)
            val target = events.firstOrNull { it.id == eventId }
                ?: return Result.failure(Exception("Event record not found"))

            if (!target.rsvpRequired) {
                return Result.failure(Exception("RSVP registration is not required for this event"))
            }
            if (target.status != EventStatus.UPCOMING) {
                return Result.failure(Exception("RSVP is only open for upcoming events"))
            }
            if (target.maxCapacity > 0 && target.totalRsvpHeadcount + rsvp.guestsCount > target.maxCapacity) {
                return Result.failure(Exception("Requested party size exceeds remaining event capacity"))
            }

            val updatedEvent = target.copy(
                rsvps = listOf(rsvp) + target.rsvps
            )
            saveEvent(updatedEvent)
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    suspend fun updateEventStatus(
        schoolId: String,
        eventId: String,
        newStatus: EventStatus
    ): Result<SchoolEvent> {
        return try {
            TenantPathProvider.verifyTenantBoundary(schoolId)
            val events = getEvents(schoolId)
            val target = events.firstOrNull { it.id == eventId }
                ?: return Result.failure(Exception("Event record not found"))

            val updatedEvent = target.copy(status = newStatus)
            saveEvent(updatedEvent)
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    suspend fun deleteEvent(schoolId: String, eventId: String): Result<Unit> {
        return try {
            TenantPathProvider.verifyTenantBoundary(schoolId)
            val path = TenantPathProvider.eventsPath(schoolId)
            db.collection(path).document(eventId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    fun getPreSeededEvents(schoolId: String): List<SchoolEvent> {
        return if (schoolId.contains("oxford") || schoolId.startsWith("sch_oxf")) {
            listOf(
                SchoolEvent(
                    id = "evt_oxf_01",
                    schoolId = schoolId,
                    title = "Annual Inter-House Athletics & Sports Day",
                    description = "Track & field championships, house relay finals, and medal presentation ceremony across Grades 6–12.",
                    category = EventCategory.SPORTS,
                    status = EventStatus.UPCOMING,
                    eventDate = "2026-04-18",
                    startTime = "08:30 AM",
                    endTime = "03:30 PM",
                    venue = "Oxford Olympic Track & Pavilions",
                    organizerName = "Coach Marcus Vance",
                    targetAudience = "All Students, Parents & Faculty",
                    isHoliday = false,
                    rsvpRequired = true,
                    maxCapacity = 300,
                    rsvps = listOf(
                        EventRsvpRecord(
                            rsvpId = "rsvp_oxf_101",
                            userId = "par_oxf_01",
                            attendeeName = "David Rahman",
                            attendeeRole = "Parent",
                            guestsCount = 2,
                            notes = "Attending Grade 10 4x100m relay"
                        ),
                        EventRsvpRecord(
                            rsvpId = "rsvp_oxf_102",
                            userId = "par_oxf_02",
                            attendeeName = "Claire Smith",
                            attendeeRole = "Parent",
                            guestsCount = 2,
                            notes = "Parent spectator stand"
                        )
                    )
                ),
                SchoolEvent(
                    id = "evt_oxf_02",
                    schoolId = schoolId,
                    title = "Spring Term Parent-Teacher Academic Conference",
                    description = "Individual faculty consultations reviewing midterm transcripts, coursework progress, and university prep goals.",
                    category = EventCategory.MEETING,
                    status = EventStatus.UPCOMING,
                    eventDate = "2026-04-11",
                    startTime = "09:00 AM",
                    endTime = "01:00 PM",
                    venue = "Main Academic Quad & Homerooms",
                    organizerName = "Dr. Robert Vance",
                    targetAudience = "Parents & Faculty",
                    isHoliday = false,
                    rsvpRequired = true,
                    maxCapacity = 120,
                    rsvps = listOf(
                        EventRsvpRecord(
                            rsvpId = "rsvp_oxf_103",
                            userId = "par_oxf_03",
                            attendeeName = "Wei Chen",
                            attendeeRole = "Parent",
                            guestsCount = 2,
                            notes = "10:15 AM slot with Physics & Maths"
                        )
                    )
                ),
                SchoolEvent(
                    id = "evt_oxf_03",
                    schoolId = schoolId,
                    title = "Founder's Day & Spring Bank Holiday",
                    description = "Campus closed for all regular classes and administrative offices. Transport routes suspended.",
                    category = EventCategory.HOLIDAY,
                    status = EventStatus.UPCOMING,
                    eventDate = "2026-05-04",
                    startTime = "All Day",
                    endTime = "All Day",
                    venue = "Campus-Wide Observance",
                    organizerName = "Office of the Principal",
                    targetAudience = "All School Community",
                    isHoliday = true,
                    rsvpRequired = false,
                    maxCapacity = 0
                ),
                SchoolEvent(
                    id = "evt_oxf_04",
                    schoolId = schoolId,
                    title = "Regional STEM & Robotics Symposium 2026",
                    description = "Student robotics showcases, AI ethics debate panels, and live science lab demonstrations.",
                    category = EventCategory.SCIENCE_FAIR,
                    status = EventStatus.COMPLETED,
                    eventDate = "2026-03-14",
                    startTime = "10:00 AM",
                    endTime = "04:00 PM",
                    venue = "Newton Innovation Auditorium",
                    organizerName = "Dr. Sarah Jenkins",
                    targetAudience = "Grades 9–12 & Visiting Judges",
                    isHoliday = false,
                    rsvpRequired = true,
                    maxCapacity = 150,
                    rsvps = listOf(
                        EventRsvpRecord(
                            rsvpId = "rsvp_oxf_104",
                            userId = "stu_oxf_01",
                            attendeeName = "Amina Rahman",
                            attendeeRole = "Student",
                            guestsCount = 1,
                            notes = "Autonomous rover booth #12"
                        )
                    )
                )
            )
        } else {
            listOf(
                SchoolEvent(
                    id = "evt_cam_01",
                    schoolId = schoolId,
                    title = "Cambridge Choral & Literary Evening",
                    description = "Annual chamber choir recital and student poetry laureate readings in the Great Hall.",
                    category = EventCategory.CULTURAL,
                    status = EventStatus.UPCOMING,
                    eventDate = "2026-04-22",
                    startTime = "06:00 PM",
                    endTime = "08:30 PM",
                    venue = "Trinity Heritage Great Hall",
                    organizerName = "Prof. Eleanor Vance",
                    targetAudience = "Parents, Students & Alumni",
                    isHoliday = false,
                    rsvpRequired = true,
                    maxCapacity = 200,
                    rsvps = listOf(
                        EventRsvpRecord(
                            rsvpId = "rsvp_cam_201",
                            userId = "stu_cam_01",
                            attendeeName = "Elizabeth Bennet",
                            attendeeRole = "Student",
                            guestsCount = 2
                        )
                    )
                ),
                SchoolEvent(
                    id = "evt_cam_02",
                    schoolId = schoolId,
                    title = "Easter Mid-Term Academic Break",
                    description = "Scheduled term recess; boarding dormitories open by prior arrangement.",
                    category = EventCategory.HOLIDAY,
                    status = EventStatus.UPCOMING,
                    eventDate = "2026-04-03",
                    startTime = "All Day",
                    endTime = "All Day",
                    venue = "Campus-Wide",
                    organizerName = "Academic Registrar",
                    targetAudience = "All School Community",
                    isHoliday = true,
                    rsvpRequired = false,
                    maxCapacity = 0
                )
            )
        }
    }
}
