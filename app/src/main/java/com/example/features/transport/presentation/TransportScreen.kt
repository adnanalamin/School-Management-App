package com.example.features.transport.presentation

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
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
import com.example.features.transport.domain.RouteStatus
import com.example.features.transport.domain.TransportRoute
import com.example.shared.components.EduStatCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransportScreen(
    viewModel: TransportViewModel,
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
        modifier = modifier.fillMaxSize().testTag("transport_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Transport & Fleet",
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
                        modifier = Modifier.minimumInteractiveComponentSize().testTag("btn_transport_back")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (TenantContext.hasPermission(Permission.TRANSPORT_MANAGE)) {
                        IconButton(
                            onClick = { viewModel.openAddRouteDialog() },
                            modifier = Modifier.minimumInteractiveComponentSize().testTag("btn_add_route")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Register Route")
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
            // Fleet Analytics Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    EduStatCard(
                        title = "Active Routes",
                        value = "${uiState.activeRoutesCount}/${uiState.totalRoutesCount}",
                        icon = Icons.Default.DirectionsBus,
                        iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.width(160.dp)
                    )
                    EduStatCard(
                        title = "Boarded Riders",
                        value = "${uiState.assignedPassengersCount}",
                        icon = Icons.Default.Group,
                        iconContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.width(160.dp)
                    )
                    EduStatCard(
                        title = "Open Seats",
                        value = "${uiState.availableSeatsTotal}",
                        icon = Icons.Default.EventSeat,
                        iconContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.width(160.dp)
                    )
                    EduStatCard(
                        title = "Fleet Capacity",
                        value = "${uiState.totalFleetCapacity}",
                        icon = Icons.Default.GpsFixed,
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
                    placeholder = { Text("Search route, vehicle, driver, stop, or student...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_search_transport")
                )
            }

            // Status & Seat Availability Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.onlyAvailableSeatsFilter,
                        onClick = { viewModel.toggleOnlyAvailableSeatsFilter() },
                        leadingIcon = {
                            if (uiState.onlyAvailableSeatsFilter) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        },
                        label = { Text("Seats Open") },
                        modifier = Modifier.testTag("chip_seats_open")
                    )
                    FilterChip(
                        selected = uiState.selectedStatusFilter == null,
                        onClick = { viewModel.setStatusFilter(null) },
                        label = { Text("All Fleet") },
                        modifier = Modifier.testTag("chip_status_all")
                    )
                    RouteStatus.entries.forEach { status ->
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
                            modifier = Modifier.testTag("chip_status_${status.name.lowercase()}")
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
            if (!uiState.isLoading && uiState.filteredRoutes.isEmpty()) {
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
                                imageVector = Icons.Default.DirectionsBus,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No matching transport routes",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Adjust search criteria or register a new school vehicle route.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }

            // Fleet Route Cards
            items(uiState.filteredRoutes, key = { it.id }) { route ->
                TransportRouteCard(
                    route = route,
                    onInspect = { viewModel.openRouteDetail(route) },
                    onAssign = { viewModel.openAssignPassengerDialog(route) },
                    onDelete = { viewModel.deleteRoute(route.id) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Dialogs
    if (uiState.showAddRouteDialog) {
        AddRouteDialog(
            onDismiss = { viewModel.closeAddRouteDialog() },
            onConfirm = { code, name, vehicle, vType, capacity, driver, phone, attendant, dep, arr, stop, stopTime, fee, notes ->
                viewModel.addRoute(code, name, vehicle, vType, capacity, driver, phone, attendant, dep, arr, stop, stopTime, fee, notes)
            }
        )
    }

    if (uiState.showAssignPassengerDialog && uiState.selectedRouteForAction != null) {
        val targetRoute = uiState.selectedRouteForAction!!
        AssignPassengerDialog(
            route = targetRoute,
            onDismiss = { viewModel.closeAssignPassengerDialog() },
            onConfirm = { name, roll, grade, stopId, stopName, phone, morning, afternoon ->
                viewModel.assignPassenger(
                    routeId = targetRoute.id,
                    studentName = name,
                    rollNumber = roll,
                    gradeLevel = grade,
                    stopId = stopId,
                    stopName = stopName,
                    guardianContact = phone,
                    isMorningPickup = morning,
                    isAfternoonDrop = afternoon
                )
            }
        )
    }

    if (uiState.showRouteDetailDialog && uiState.selectedRouteForAction != null) {
        val currentRoute = uiState.selectedRouteForAction!!
        RouteDetailDialog(
            route = currentRoute,
            onDismiss = { viewModel.closeRouteDetail() },
            onAssignClick = {
                viewModel.closeRouteDetail()
                viewModel.openAssignPassengerDialog(currentRoute)
            },
            onAddStopClick = {
                viewModel.openAddStopDialog(currentRoute)
            },
            onRemovePassenger = { paxId ->
                viewModel.removePassenger(currentRoute.id, paxId)
            },
            onStatusChange = { newStatus ->
                viewModel.updateRouteStatus(currentRoute.id, newStatus)
            }
        )
    }

    if (uiState.showAddStopDialog && uiState.selectedRouteForAction != null) {
        val targetRoute = uiState.selectedRouteForAction!!
        AddStopDialog(
            route = targetRoute,
            onDismiss = { viewModel.closeAddStopDialog() },
            onConfirm = { stopName, pickupTime, dropTime, landmark, fee ->
                viewModel.addStopToRoute(
                    routeId = targetRoute.id,
                    stopName = stopName,
                    pickupTime = pickupTime,
                    dropTime = dropTime,
                    landmark = landmark,
                    monthlyFee = fee
                )
            }
        )
    }
}

@Composable
fun TransportRouteCard(
    route: TransportRoute,
    onInspect: () -> Unit,
    onAssign: () -> Unit,
    onDelete: () -> Unit
) {
    val statusColors = when (route.status) {
        RouteStatus.ACTIVE -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        RouteStatus.DELAYED -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        RouteStatus.MAINTENANCE, RouteStatus.SUSPENDED -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().testTag("route_card_${route.id}")
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
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsBus,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "${route.routeCode} • ${route.vehicleType.displayName}",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Vehicle: ${route.vehicleNumber}" + if (route.gpsTrackingEnabled) " • GPS Live" else "",
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
                        text = route.status.displayName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColors.second
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = route.routeName,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Driver: ${route.driverName} (${route.driverPhone.ifBlank { "Radio Dispatch" }})",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${route.departureTime} → ${route.arrivalTime}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${route.stops.size} Stop(s)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Seat Occupancy",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "${route.occupiedSeatsCount}/${route.capacity} (${route.availableSeatsCount} open)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            val occupancyFraction = if (route.capacity > 0) {
                route.occupiedSeatsCount.toFloat() / route.capacity.toFloat()
            } else 0f
            LinearProgressIndicator(
                progress = { occupancyFraction.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
            )

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
                    modifier = Modifier.minimumInteractiveComponentSize().testTag("btn_inspect_${route.id}")
                ) {
                    Text("Manifest (${route.passengers.size})", fontSize = 12.sp)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (TenantContext.hasPermission(Permission.TRANSPORT_MANAGE)) {
                        OutlinedButton(
                            onClick = onAssign,
                            enabled = route.availableSeatsCount > 0,
                            modifier = Modifier.minimumInteractiveComponentSize().testTag("btn_assign_${route.id}")
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Board", fontSize = 12.sp)
                        }
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.minimumInteractiveComponentSize().testTag("btn_delete_${route.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Route",
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
