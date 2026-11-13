package com.example.features.events.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.security.Permission
import com.example.core.security.TenantContext
import com.example.features.events.data.EventRepository
import com.example.features.events.domain.EventCategory
import com.example.features.events.domain.EventRsvpRecord
import com.example.features.events.domain.EventStatus
import com.example.features.events.domain.SchoolEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class EventUiState(
    val events: List<SchoolEvent> = emptyList(),
    val searchQuery: String = "",
    val selectedCategoryFilter: EventCategory? = null,
    val selectedStatusFilter: EventStatus? = null,
    val onlyHolidaysFilter: Boolean = false,
    val isLoading: Boolean = false,
    val statusMessage: String? = null,
    val errorMessage: String? = null,
    val showCreateEventDialog: Boolean = false,
    val showRsvpDialog: Boolean = false,
    val showEventDetailDialog: Boolean = false,
    val selectedEventForAction: SchoolEvent? = null
) {
    val filteredEvents: List<SchoolEvent>
        get() = events.filter { event ->
            val matchesSearch = if (searchQuery.isBlank()) true else {
                val q = searchQuery.trim().lowercase()
                event.title.lowercase().contains(q) ||
                    event.description.lowercase().contains(q) ||
                    event.venue.lowercase().contains(q) ||
                    event.organizerName.lowercase().contains(q) ||
                    event.targetAudience.lowercase().contains(q) ||
                    event.eventDate.lowercase().contains(q) ||
                    event.category.displayName.lowercase().contains(q)
            }
            val matchesCategory = selectedCategoryFilter == null || event.category == selectedCategoryFilter
            val matchesStatus = selectedStatusFilter == null || event.status == selectedStatusFilter
            val matchesHoliday = !onlyHolidaysFilter || event.isHoliday
            matchesSearch && matchesCategory && matchesStatus && matchesHoliday
        }

    val totalEventsCount: Int
        get() = events.size

    val upcomingEventsCount: Int
        get() = events.count { it.status == EventStatus.UPCOMING || it.status == EventStatus.ONGOING }

    val holidaysCount: Int
        get() = events.count { it.isHoliday && it.status != EventStatus.CANCELLED }

    val totalRsvpCount: Int
        get() = events.sumOf { it.totalRsvpHeadcount }
}

class EventViewModel(
    private val eventRepository: EventRepository = EventRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventUiState())
    val uiState: StateFlow<EventUiState> = _uiState.asStateFlow()

    init {
        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        val seeded = eventRepository.getPreSeededEvents(schoolId)
        _uiState.update { it.copy(events = seeded) }
        loadEvents()
    }

    fun loadEvents() {
        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val list = eventRepository.getEvents(schoolId = schoolId)
                _uiState.update { it.copy(events = list, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to load school calendar"
                    )
                }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setCategoryFilter(category: EventCategory?) {
        _uiState.update { it.copy(selectedCategoryFilter = category) }
    }

    fun setStatusFilter(status: EventStatus?) {
        _uiState.update { it.copy(selectedStatusFilter = status) }
    }

    fun toggleOnlyHolidaysFilter() {
        _uiState.update { it.copy(onlyHolidaysFilter = !it.onlyHolidaysFilter) }
    }

    fun clearFilters() {
        _uiState.update {
            it.copy(
                searchQuery = "",
                selectedCategoryFilter = null,
                selectedStatusFilter = null,
                onlyHolidaysFilter = false
            )
        }
    }

    fun openCreateEventDialog() {
        if (!TenantContext.hasPermission(Permission.EVENTS_MANAGE)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires EVENTS_MANAGE permission") }
            return
        }
        _uiState.update { it.copy(showCreateEventDialog = true, errorMessage = null) }
    }

    fun closeCreateEventDialog() {
        _uiState.update { it.copy(showCreateEventDialog = false) }
    }

    fun openRsvpDialog(event: SchoolEvent) {
        if (!TenantContext.hasPermission(Permission.EVENTS_VIEW)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires EVENTS_VIEW permission") }
            return
        }
        if (!event.isRsvpOpen) {
            _uiState.update { it.copy(errorMessage = "RSVP registration is closed or at capacity for '${event.title}'.") }
            return
        }
        _uiState.update {
            it.copy(
                selectedEventForAction = event,
                showRsvpDialog = true,
                errorMessage = null
            )
        }
    }

    fun closeRsvpDialog() {
        _uiState.update {
            it.copy(
                showRsvpDialog = false,
                selectedEventForAction = null
            )
        }
    }

    fun openEventDetail(event: SchoolEvent) {
        _uiState.update {
            it.copy(
                selectedEventForAction = event,
                showEventDetailDialog = true
            )
        }
    }

    fun closeEventDetail() {
        _uiState.update {
            it.copy(
                showEventDetailDialog = false,
                selectedEventForAction = null
            )
        }
    }

    fun createEvent(
        title: String,
        description: String,
        category: EventCategory,
        eventDate: String,
        startTime: String,
        endTime: String,
        venue: String,
        organizerName: String,
        targetAudience: String,
        isHoliday: Boolean,
        rsvpRequired: Boolean,
        maxCapacity: Int
    ) {
        if (!TenantContext.hasPermission(Permission.EVENTS_MANAGE)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires EVENTS_MANAGE permission") }
            return
        }

        if (title.isBlank() || eventDate.isBlank() || venue.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Event title, date, and venue are required.") }
            return
        }

        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        val newEvent = SchoolEvent(
            id = "evt_${UUID.randomUUID().toString().take(8)}",
            schoolId = schoolId,
            title = title.trim(),
            description = description.trim(),
            category = category,
            status = EventStatus.UPCOMING,
            eventDate = eventDate.trim(),
            startTime = startTime.ifBlank { "09:00 AM" }.trim(),
            endTime = endTime.ifBlank { "02:00 PM" }.trim(),
            venue = venue.trim(),
            organizerName = organizerName.ifBlank { "School Administration" }.trim(),
            targetAudience = targetAudience.ifBlank { "All School Community" }.trim(),
            isHoliday = isHoliday || category == EventCategory.HOLIDAY,
            rsvpRequired = rsvpRequired,
            maxCapacity = if (rsvpRequired) maxCapacity.coerceAtLeast(0) else 0,
            rsvps = emptyList(),
            createdAt = System.currentTimeMillis()
        )

        _uiState.update { state ->
            state.copy(
                events = listOf(newEvent) + state.events,
                showCreateEventDialog = false,
                statusMessage = "Scheduled '${newEvent.title}' for ${newEvent.eventDate}.",
                errorMessage = null
            )
        }

        viewModelScope.launch {
            eventRepository.saveEvent(newEvent)
        }
    }

    fun submitRsvp(
        eventId: String,
        attendeeName: String,
        attendeeRole: String,
        guestsCount: Int,
        notes: String
    ) {
        if (!TenantContext.hasPermission(Permission.EVENTS_VIEW)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires EVENTS_VIEW permission") }
            return
        }

        if (attendeeName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Attendee name is required for RSVP.") }
            return
        }

        val target = _uiState.value.events.firstOrNull { it.id == eventId } ?: return
        if (!target.isRsvpOpen) {
            _uiState.update { it.copy(errorMessage = "RSVP is currently unavailable for '${target.title}'.") }
            return
        }

        val safeGuests = guestsCount.coerceIn(1, 10)
        if (target.maxCapacity > 0 && target.totalRsvpHeadcount + safeGuests > target.maxCapacity) {
            _uiState.update {
                it.copy(errorMessage = "Only ${target.remainingRsvpSlots} RSVP slot(s) remaining for '${target.title}'.")
            }
            return
        }

        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        val userId = TenantContext.activeUserId.value ?: "usr_guest"
        val rsvpRecord = EventRsvpRecord(
            rsvpId = "rsvp_${UUID.randomUUID().toString().take(8)}",
            userId = userId,
            attendeeName = attendeeName.trim(),
            attendeeRole = attendeeRole.ifBlank { "Parent" }.trim(),
            guestsCount = safeGuests,
            notes = notes.trim(),
            respondedAt = System.currentTimeMillis()
        )

        _uiState.update { state ->
            val updatedEvents = state.events.map { event ->
                if (event.id == eventId) {
                    event.copy(rsvps = listOf(rsvpRecord) + event.rsvps)
                } else event
            }
            val updatedSelected = updatedEvents.firstOrNull { it.id == eventId }
            state.copy(
                events = updatedEvents,
                showRsvpDialog = false,
                selectedEventForAction = if (state.showEventDetailDialog) updatedSelected else null,
                statusMessage = "RSVP confirmed for ${rsvpRecord.attendeeName} ($safeGuests guest(s)).",
                errorMessage = null
            )
        }

        viewModelScope.launch {
            eventRepository.rsvpToEvent(schoolId, eventId, rsvpRecord)
        }
    }

    fun updateEventStatus(eventId: String, newStatus: EventStatus) {
        if (!TenantContext.hasPermission(Permission.EVENTS_MANAGE)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires EVENTS_MANAGE permission") }
            return
        }

        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        _uiState.update { state ->
            val updatedEvents = state.events.map { event ->
                if (event.id == eventId) event.copy(status = newStatus) else event
            }
            val updatedSelected = updatedEvents.firstOrNull { it.id == eventId }
            state.copy(
                events = updatedEvents,
                selectedEventForAction = if (state.showEventDetailDialog) updatedSelected else state.selectedEventForAction,
                statusMessage = "Event status updated to ${newStatus.displayName}.",
                errorMessage = null
            )
        }

        viewModelScope.launch {
            eventRepository.updateEventStatus(schoolId, eventId, newStatus)
        }
    }

    fun deleteEvent(eventId: String) {
        if (!TenantContext.hasPermission(Permission.EVENTS_MANAGE)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires EVENTS_MANAGE permission") }
            return
        }

        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        _uiState.update { state ->
            state.copy(
                events = state.events.filterNot { it.id == eventId },
                statusMessage = "Event removed from school calendar."
            )
        }

        viewModelScope.launch {
            eventRepository.deleteEvent(schoolId, eventId)
        }
    }

    fun dismissStatusMessage() {
        _uiState.update { it.copy(statusMessage = null, errorMessage = null) }
    }
}
