package com.example.features.events.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.security.Permission
import com.example.core.security.TenantContext
import com.example.features.events.domain.EventCategory
import com.example.features.events.domain.EventStatus
import com.example.features.events.domain.SchoolEvent

@Composable
fun CreateEventDialog(
    onDismiss: () -> Unit,
    onConfirm: (
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
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(EventCategory.ACADEMIC) }
    var eventDate by remember { mutableStateOf("2026-04-28") }
    var startTime by remember { mutableStateOf("09:00 AM") }
    var endTime by remember { mutableStateOf("02:00 PM") }
    var venue by remember { mutableStateOf("") }
    var organizerName by remember { mutableStateOf("") }
    var targetAudience by remember { mutableStateOf("All Students, Parents & Faculty") }
    var isHoliday by remember { mutableStateOf(false) }
    var rsvpRequired by remember { mutableStateOf(true) }
    var maxCapacityStr by remember { mutableStateOf("150") }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("dialog_create_event"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Schedule School Event",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Event Title *") },
                    placeholder = { Text("e.g. Annual Science & Innovation Expo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_event_title")
                )

                Text(
                    text = "Event Category",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    EventCategory.entries.forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = {
                                selectedCategory = category
                                if (category == EventCategory.HOLIDAY) {
                                    isHoliday = true
                                    rsvpRequired = false
                                }
                            },
                            label = { Text(category.displayName, fontSize = 11.sp) },
                            modifier = Modifier.testTag("chip_add_event_cat_${category.name.lowercase()}")
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = eventDate,
                        onValueChange = { eventDate = it },
                        label = { Text("Date (YYYY-MM-DD) *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("input_event_date")
                    )
                    OutlinedTextField(
                        value = venue,
                        onValueChange = { venue = it },
                        label = { Text("Venue / Hall *") },
                        placeholder = { Text("Main Auditorium") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("input_event_venue")
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Starts") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("input_event_start")
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("Ends") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("input_event_end")
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = organizerName,
                        onValueChange = { organizerName = it },
                        label = { Text("Organizer") },
                        placeholder = { Text("Dr. Sarah Jenkins") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("input_event_organizer")
                    )
                    OutlinedTextField(
                        value = targetAudience,
                        onValueChange = { targetAudience = it },
                        label = { Text("Audience") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("input_event_audience")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Mark as Official School Holiday", style = MaterialTheme.typography.bodySmall)
                    Switch(
                        checked = isHoliday,
                        onCheckedChange = { isHoliday = it },
                        modifier = Modifier.testTag("switch_event_holiday")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable RSVP Attendance Tracking", style = MaterialTheme.typography.bodySmall)
                    Switch(
                        checked = rsvpRequired,
                        onCheckedChange = { rsvpRequired = it },
                        modifier = Modifier.testTag("switch_event_rsvp")
                    )
                }

                if (rsvpRequired) {
                    OutlinedTextField(
                        value = maxCapacityStr,
                        onValueChange = { maxCapacityStr = it },
                        label = { Text("Max RSVP Capacity (0 = Unlimited)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_event_capacity")
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Agenda & Event Description") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth().testTag("input_event_description")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val capacity = maxCapacityStr.toIntOrNull() ?: 0
                    onConfirm(
                        title,
                        description,
                        selectedCategory,
                        eventDate,
                        startTime,
                        endTime,
                        venue,
                        organizerName,
                        targetAudience,
                        isHoliday,
                        rsvpRequired,
                        capacity
                    )
                },
                enabled = title.isNotBlank() && eventDate.isNotBlank() && venue.isNotBlank(),
                modifier = Modifier.testTag("btn_confirm_create_event")
            ) {
                Text("Publish Event")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_cancel_create_event")
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EventRsvpDialog(
    event: SchoolEvent,
    onDismiss: () -> Unit,
    onConfirm: (
        attendeeName: String,
        attendeeRole: String,
        guestsCount: Int,
        notes: String
    ) -> Unit
) {
    var attendeeName by remember { mutableStateOf("") }
    var attendeeRole by remember { mutableStateOf("Parent") }
    var guestsCountStr by remember { mutableStateOf("1") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("dialog_event_rsvp"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.EventAvailable,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Confirm Event RSVP",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = event.title,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        val capLabel = if (event.maxCapacity > 0) {
                            "${event.remainingRsvpSlots} of ${event.maxCapacity} slots open"
                        } else "Open Capacity"
                        Text(
                            text = "${event.eventDate} • ${event.startTime} • $capLabel",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Parent", "Student", "Faculty").forEach { role ->
                        FilterChip(
                            selected = attendeeRole == role,
                            onClick = { attendeeRole = role },
                            label = { Text(role) },
                            modifier = Modifier.testTag("chip_rsvp_role_${role.lowercase()}")
                        )
                    }
                }

                OutlinedTextField(
                    value = attendeeName,
                    onValueChange = { attendeeName = it },
                    label = { Text("Attendee Full Name *") },
                    placeholder = { Text("e.g. David Rahman") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().testTag("input_rsvp_name")
                )

                OutlinedTextField(
                    value = guestsCountStr,
                    onValueChange = { guestsCountStr = it },
                    label = { Text("Total Party Size (1–10) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_rsvp_guests")
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Seating / Accessibility Notes") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth().testTag("input_rsvp_notes")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val guests = (guestsCountStr.toIntOrNull() ?: 1).coerceIn(1, 10)
                    onConfirm(attendeeName, attendeeRole, guests, notes)
                },
                enabled = attendeeName.isNotBlank(),
                modifier = Modifier.testTag("btn_confirm_rsvp")
            ) {
                Text("Submit RSVP")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_cancel_rsvp")
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EventDetailDialog(
    event: SchoolEvent,
    onDismiss: () -> Unit,
    onRsvpClick: () -> Unit,
    onStatusChange: (EventStatus) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("dialog_event_detail"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = event.category.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Date & Time: ${event.eventDate} • ${event.startTime} – ${event.endTime}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = event.venue,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Text(
                            text = "Organizer: ${event.organizerName}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Audience: ${event.targetAudience}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (event.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = event.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (TenantContext.hasPermission(Permission.EVENTS_MANAGE)) {
                    Text(
                        text = "Schedule Status",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        EventStatus.entries.forEach { statusOption ->
                            FilterChip(
                                selected = event.status == statusOption,
                                onClick = { onStatusChange(statusOption) },
                                label = { Text(statusOption.displayName, fontSize = 11.sp) },
                                modifier = Modifier.testTag("chip_detail_event_status_${statusOption.name.lowercase()}")
                            )
                        }
                    }
                }

                if (event.rsvpRequired) {
                    HorizontalDivider()
                    val capacitySummary = if (event.maxCapacity > 0) {
                        "${event.totalRsvpHeadcount}/${event.maxCapacity} attending"
                    } else {
                        "${event.totalRsvpHeadcount} attending"
                    }
                    Text(
                        text = "RSVP Roster ($capacitySummary)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (event.rsvps.isEmpty()) {
                        Text(
                            text = "No RSVP confirmations recorded yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    } else {
                        event.rsvps.forEach { rsvp ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${rsvp.attendeeName} (${rsvp.attendeeRole})",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (rsvp.notes.isNotBlank()) {
                                            Text(
                                                text = rsvp.notes,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MaterialTheme.colorScheme.secondaryContainer)
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "${rsvp.guestsCount} Guest(s)",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (event.isRsvpOpen && TenantContext.hasPermission(Permission.EVENTS_VIEW)) {
                Button(
                    onClick = onRsvpClick,
                    modifier = Modifier.testTag("btn_rsvp_from_detail")
                ) {
                    Text("RSVP Now")
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
