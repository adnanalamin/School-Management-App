package com.example.features.events.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.shared.components.EduStatCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventScreen(
    viewModel: EventViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.statusMessage, uiState.errorMessage) {
        uiState.statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissStatusMessage()
        }
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissStatusMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("events_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Events & Academic Calendar",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Tenant: ${(TenantContext.activeSchoolId.value ?: "").take(15)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.minimumInteractiveComponentSize().testTag("btn_events_back")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (TenantContext.hasPermission(Permission.EVENTS_MANAGE)) {
                        IconButton(
                            onClick = { viewModel.openCreateEventDialog() },
                            modifier = Modifier.minimumInteractiveComponentSize().testTag("btn_add_event")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Schedule Event")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Calendar Summary Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    EduStatCard(
                        title = "Upcoming Events",
                        value = "${uiState.upcomingEventsCount}",
                        icon = Icons.Default.Event,
                        iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.width(160.dp)
                    )
                    EduStatCard(
                        title = "Total Scheduled",
                        value = "${uiState.totalEventsCount}",
                        icon = Icons.Default.Celebration,
                        iconContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.width(160.dp)
                    )
                    EduStatCard(
                        title = "School Holidays",
                        value = "${uiState.holidaysCount}",
                        icon = Icons.Default.WbSunny,
                        iconContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.width(160.dp)
                    )
                    EduStatCard(
                        title = "RSVP Headcount",
                        value = "${uiState.totalRsvpCount}",
                        icon = Icons.Default.Group,
                        iconContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.width(160.dp)
                    )
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search event, venue, date, organizer, or category...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_search_events")
                )
            }

            // Holiday Toggle & Category Filters
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.onlyHolidaysFilter,
                        onClick = { viewModel.toggleOnlyHolidaysFilter() },
                        leadingIcon = {
                            if (uiState.onlyHolidaysFilter) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        },
                        label = { Text("Holidays Only") },
                        modifier = Modifier.testTag("chip_holidays_only")
                    )
                    FilterChip(
                        selected = uiState.selectedCategoryFilter == null,
                        onClick = { viewModel.setCategoryFilter(null) },
                        label = { Text("All Categories") },
                        modifier = Modifier.testTag("chip_event_cat_all")
                    )
                    EventCategory.entries.forEach { category ->
                        FilterChip(
                            selected = uiState.selectedCategoryFilter == category,
                            onClick = {
                                if (uiState.selectedCategoryFilter == category) {
                                    viewModel.setCategoryFilter(null)
                                } else {
                                    viewModel.setCategoryFilter(category)
                                }
                            },
                            label = { Text(category.displayName) },
                            modifier = Modifier.testTag("chip_event_cat_${category.name.lowercase()}")
                        )
                    }
                }
            }

            // Status Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.selectedStatusFilter == null,
                        onClick = { viewModel.setStatusFilter(null) },
                        label = { Text("All Statuses") },
                        modifier = Modifier.testTag("chip_event_status_all")
                    )
                    EventStatus.entries.forEach { status ->
                        FilterChip(
                            selected = uiState.selectedStatusFilter == status,
                            onClick = {
                                if (uiState.selectedStatusFilter == status) {
                                    viewModel.setStatusFilter(null)
                                } else {
                                    viewModel.setStatusFilter(status)
                                }
                            },
                            label = { Text(status.displayName) },
                            modifier = Modifier.testTag("chip_event_status_${status.name.lowercase()}")
                        )
                    }
                }
            }

            // Loading indicator
            item {
                AnimatedVisibility(visible = uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }

            // Empty State
            if (!uiState.isLoading && uiState.filteredEvents.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No matching calendar events",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Adjust category filters or schedule a new school event.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }

            // Calendar Event Cards
            items(uiState.filteredEvents, key = { it.id }) { event ->
                SchoolEventCard(
                    event = event,
                    onInspect = { viewModel.openEventDetail(event) },
                    onRsvp = { viewModel.openRsvpDialog(event) },
                    onDelete = { viewModel.deleteEvent(event.id) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Dialogs
    if (uiState.showCreateEventDialog) {
        CreateEventDialog(
            onDismiss = { viewModel.closeCreateEventDialog() },
            onConfirm = { title, desc, cat, date, start, end, venue, org, aud, holiday, rsvpReq, cap ->
                viewModel.createEvent(title, desc, cat, date, start, end, venue, org, aud, holiday, rsvpReq, cap)
            }
        )
    }

    if (uiState.showRsvpDialog && uiState.selectedEventForAction != null) {
        val targetEvent = uiState.selectedEventForAction!!
        EventRsvpDialog(
            event = targetEvent,
            onDismiss = { viewModel.closeRsvpDialog() },
            onConfirm = { name, role, guests, notes ->
                viewModel.submitRsvp(
                    eventId = targetEvent.id,
                    attendeeName = name,
                    attendeeRole = role,
                    guestsCount = guests,
                    notes = notes
                )
            }
        )
    }

    if (uiState.showEventDetailDialog && uiState.selectedEventForAction != null) {
        val currentEvent = uiState.selectedEventForAction!!
        EventDetailDialog(
            event = currentEvent,
            onDismiss = { viewModel.closeEventDetail() },
            onRsvpClick = {
                viewModel.closeEventDetail()
                viewModel.openRsvpDialog(currentEvent)
            },
            onStatusChange = { newStatus ->
                viewModel.updateEventStatus(currentEvent.id, newStatus)
            }
        )
    }
}

@Composable
fun SchoolEventCard(
    event: SchoolEvent,
    onInspect: () -> Unit,
    onRsvp: () -> Unit,
    onDelete: () -> Unit
) {
    val statusColors = when (event.status) {
        EventStatus.UPCOMING -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        EventStatus.ONGOING -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        EventStatus.COMPLETED -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        EventStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().testTag("event_card_${event.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (event.isHoliday) MaterialTheme.colorScheme.tertiaryContainer
                                else MaterialTheme.colorScheme.primaryContainer
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (event.isHoliday) Icons.Default.WbSunny else Icons.Default.Event,
                            contentDescription = null,
                            tint = if (event.isHoliday) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = event.category.displayName,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = event.eventDate + if (event.isHoliday) " • Official Holiday" else "",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusColors.first)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = event.status.displayName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColors.second
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = event.title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = event.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = event.venue,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = event.startTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (event.rsvpRequired) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "RSVP Attendance",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    val rsvpText = if (event.maxCapacity > 0) {
                        "${event.totalRsvpHeadcount}/${event.maxCapacity} Confirmed"
                    } else {
                        "${event.totalRsvpHeadcount} Confirmed"
                    }
                    Text(
                        text = rsvpText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (event.maxCapacity > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    val fillFraction = event.totalRsvpHeadcount.toFloat() / event.maxCapacity.toFloat()
                    LinearProgressIndicator(
                        progress = { fillFraction.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onInspect,
                    modifier = Modifier.minimumInteractiveComponentSize().testTag("btn_inspect_${event.id}")
                ) {
                    Text("Details (${event.rsvps.size})", fontSize = 12.sp)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (event.isRsvpOpen && TenantContext.hasPermission(Permission.EVENTS_VIEW)) {
                        OutlinedButton(
                            onClick = onRsvp,
                            modifier = Modifier.minimumInteractiveComponentSize().testTag("btn_rsvp_${event.id}")
                        ) {
                            Icon(Icons.Default.EventAvailable, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("RSVP", fontSize = 12.sp)
                        }
                    }
                    if (TenantContext.hasPermission(Permission.EVENTS_MANAGE)) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.minimumInteractiveComponentSize().testTag("btn_delete_${event.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Event",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
