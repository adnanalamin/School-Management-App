package com.example.features.transport.presentation

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
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
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
import com.example.features.transport.domain.RouteStatus
import com.example.features.transport.domain.TransportRoute
import com.example.features.transport.domain.VehicleType

@Composable
fun AddRouteDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        routeCode: String,
        routeName: String,
        vehicleNumber: String,
        vehicleType: VehicleType,
        capacity: Int,
        driverName: String,
        driverPhone: String,
        attendantName: String,
        departureTime: String,
        arrivalTime: String,
        primaryStopName: String,
        primaryStopPickupTime: String,
        primaryStopMonthlyFee: Double,
        notes: String
    ) -> Unit
) {
    var routeCode by remember { mutableStateOf("") }
    var routeName by remember { mutableStateOf("") }
    var vehicleNumber by remember { mutableStateOf("") }
    var selectedVehicleType by remember { mutableStateOf(VehicleType.SCHOOL_BUS) }
    var capacityStr by remember { mutableStateOf("40") }
    var driverName by remember { mutableStateOf("") }
    var driverPhone by remember { mutableStateOf("") }
    var attendantName by remember { mutableStateOf("") }
    var departureTime by remember { mutableStateOf("07:15 AM") }
    var arrivalTime by remember { mutableStateOf("08:10 AM") }
    var primaryStopName by remember { mutableStateOf("") }
    var primaryStopPickupTime by remember { mutableStateOf("07:25 AM") }
    var primaryStopFeeStr by remember { mutableStateOf("85.00") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("dialog_add_route"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DirectionsBus,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Register Fleet Route",
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
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = routeCode,
                        onValueChange = { routeCode = it },
                        label = { Text("Route Code *") },
                        placeholder = { Text("RT-OXF-04") },
                        singleLine = true,
                        modifier = Modifier.weight(0.9f).testTag("input_route_code")
                    )
                    OutlinedTextField(
                        value = vehicleNumber,
                        onValueChange = { vehicleNumber = it },
                        label = { Text("Vehicle Plate *") },
                        placeholder = { Text("OXF-BUS-401") },
                        singleLine = true,
                        modifier = Modifier.weight(1.1f).testTag("input_vehicle_number")
                    )
                }

                OutlinedTextField(
                    value = routeName,
                    onValueChange = { routeName = it },
                    label = { Text("Route Corridor Name *") },
                    placeholder = { Text("e.g. East Cowley & Iffley Loop") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_route_name")
                )

                Text(
                    text = "Vehicle Class",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    VehicleType.entries.forEach { vType ->
                        FilterChip(
                            selected = selectedVehicleType == vType,
                            onClick = { selectedVehicleType = vType },
                            label = { Text(vType.displayName, fontSize = 11.sp) },
                            modifier = Modifier.testTag("chip_vtype_${vType.name.lowercase()}")
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = driverName,
                        onValueChange = { driverName = it },
                        label = { Text("Driver Name *") },
                        singleLine = true,
                        modifier = Modifier.weight(1.2f).testTag("input_driver_name")
                    )
                    OutlinedTextField(
                        value = capacityStr,
                        onValueChange = { capacityStr = it },
                        label = { Text("Seats") },
                        singleLine = true,
                        modifier = Modifier.weight(0.6f).testTag("input_route_capacity")
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = driverPhone,
                        onValueChange = { driverPhone = it },
                        label = { Text("Driver Phone") },
                        placeholder = { Text("+44 7700 900...") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("input_driver_phone")
                    )
                    OutlinedTextField(
                        value = attendantName,
                        onValueChange = { attendantName = it },
                        label = { Text("Attendant") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("input_attendant_name")
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = departureTime,
                        onValueChange = { departureTime = it },
                        label = { Text("Departs") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("input_departure_time")
                    )
                    OutlinedTextField(
                        value = arrivalTime,
                        onValueChange = { arrivalTime = it },
                        label = { Text("Arrives Campus") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("input_arrival_time")
                    )
                }

                OutlinedTextField(
                    value = primaryStopName,
                    onValueChange = { primaryStopName = it },
                    label = { Text("First Pickup Stop (Optional)") },
                    placeholder = { Text("e.g. Magdalen Bridge Stop") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_primary_stop")
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = primaryStopPickupTime,
                        onValueChange = { primaryStopPickupTime = it },
                        label = { Text("Stop Time") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("input_stop_time")
                    )
                    OutlinedTextField(
                        value = primaryStopFeeStr,
                        onValueChange = { primaryStopFeeStr = it },
                        label = { Text("Monthly Fee") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("input_stop_fee")
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Dispatch & Safety Notes") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth().testTag("input_route_notes")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val capacity = capacityStr.toIntOrNull() ?: 40
                    val fee = primaryStopFeeStr.toDoubleOrNull() ?: 0.0
                    onConfirm(
                        routeCode,
                        routeName,
                        vehicleNumber,
                        selectedVehicleType,
                        capacity,
                        driverName,
                        driverPhone,
                        attendantName,
                        departureTime,
                        arrivalTime,
                        primaryStopName,
                        primaryStopPickupTime,
                        fee,
                        notes
                    )
                },
                enabled = routeCode.isNotBlank() && routeName.isNotBlank() && vehicleNumber.isNotBlank() && driverName.isNotBlank(),
                modifier = Modifier.testTag("btn_confirm_add_route")
            ) {
                Text("Save Route")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_cancel_add_route")
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AssignPassengerDialog(
    route: TransportRoute,
    onDismiss: () -> Unit,
    onConfirm: (
        studentName: String,
        rollNumber: String,
        gradeLevel: String,
        stopId: String,
        stopName: String,
        guardianContact: String,
        isMorningPickup: Boolean,
        isAfternoonDrop: Boolean
    ) -> Unit
) {
    var studentName by remember { mutableStateOf("") }
    var rollNumber by remember { mutableStateOf("") }
    var gradeLevel by remember { mutableStateOf("Grade 10 - A") }
    var selectedStopId by remember { mutableStateOf(route.stops.firstOrNull()?.stopId ?: "") }
    var customStopName by remember { mutableStateOf(route.stops.firstOrNull()?.stopName ?: "") }
    var guardianContact by remember { mutableStateOf("") }
    var isMorningPickup by remember { mutableStateOf(true) }
    var isAfternoonDrop by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("dialog_assign_passenger"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Assign Student Rider",
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
                            text = "${route.routeCode} • ${route.routeName}",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${route.vehicleNumber} • ${route.availableSeatsCount}/${route.capacity} seats open",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                OutlinedTextField(
                    value = studentName,
                    onValueChange = { studentName = it },
                    label = { Text("Student Full Name *") },
                    placeholder = { Text("e.g. Amina Rahman") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_pax_name")
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = rollNumber,
                        onValueChange = { rollNumber = it },
                        label = { Text("Roll Number *") },
                        placeholder = { Text("OXF-10A-005") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("input_pax_roll")
                    )
                    OutlinedTextField(
                        value = gradeLevel,
                        onValueChange = { gradeLevel = it },
                        label = { Text("Grade / Section") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("input_pax_grade")
                    )
                }

                if (route.stops.isNotEmpty()) {
                    Text(
                        text = "Select Boarding Stop",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        route.stops.forEach { stop ->
                            FilterChip(
                                selected = selectedStopId == stop.stopId,
                                onClick = {
                                    selectedStopId = stop.stopId
                                    customStopName = stop.stopName
                                },
                                label = { Text("${stop.stopName} (${stop.pickupTime})", fontSize = 11.sp) },
                                modifier = Modifier.testTag("chip_stop_${stop.stopId}")
                            )
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = customStopName,
                        onValueChange = { customStopName = it },
                        label = { Text("Boarding Stop Name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_pax_stop")
                    )
                }

                OutlinedTextField(
                    value = guardianContact,
                    onValueChange = { guardianContact = it },
                    label = { Text("Guardian Emergency Phone") },
                    placeholder = { Text("+44 7700 900...") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().testTag("input_pax_phone")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Morning Pickup", style = MaterialTheme.typography.bodySmall)
                    Switch(
                        checked = isMorningPickup,
                        onCheckedChange = { isMorningPickup = it },
                        modifier = Modifier.testTag("switch_morning_pickup")
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Afternoon Drop-off", style = MaterialTheme.typography.bodySmall)
                    Switch(
                        checked = isAfternoonDrop,
                        onCheckedChange = { isAfternoonDrop = it },
                        modifier = Modifier.testTag("switch_afternoon_drop")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        studentName,
                        rollNumber,
                        gradeLevel,
                        selectedStopId,
                        customStopName,
                        guardianContact,
                        isMorningPickup,
                        isAfternoonDrop
                    )
                },
                enabled = studentName.isNotBlank() && rollNumber.isNotBlank() && customStopName.isNotBlank(),
                modifier = Modifier.testTag("btn_confirm_assign_pax")
            ) {
                Text("Board Student")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_cancel_assign_pax")
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun RouteDetailDialog(
    route: TransportRoute,
    onDismiss: () -> Unit,
    onAssignClick: () -> Unit,
    onAddStopClick: () -> Unit,
    onRemovePassenger: (passengerId: String) -> Unit,
    onStatusChange: (RouteStatus) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("dialog_route_detail"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.DirectionsBus,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${route.routeCode} • ${route.vehicleType.displayName}",
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
                    text = route.routeName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Vehicle: ${route.vehicleNumber} • ${route.departureTime} → ${route.arrivalTime}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            text = "Driver: ${route.driverName} (${route.driverPhone.ifBlank { "On Radio" }})",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (route.attendantName.isNotBlank()) {
                            Text(
                                text = "Bus Attendant: ${route.attendantName}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Text(
                            text = "Occupancy: ${route.occupiedSeatsCount}/${route.capacity} seats (${route.availableSeatsCount} open)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                        if (route.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = route.notes,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (TenantContext.hasPermission(Permission.TRANSPORT_MANAGE)) {
                    Text(
                        text = "Dispatch Status",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        RouteStatus.entries.forEach { statusOption ->
                            FilterChip(
                                selected = route.status == statusOption,
                                onClick = { onStatusChange(statusOption) },
                                label = { Text(statusOption.displayName, fontSize = 11.sp) },
                                modifier = Modifier.testTag("chip_detail_status_${statusOption.name.lowercase()}")
                            )
                        }
                    }
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Route Stops (${route.stops.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (TenantContext.hasPermission(Permission.TRANSPORT_MANAGE)) {
                        OutlinedButton(
                            onClick = onAddStopClick,
                            modifier = Modifier.minimumInteractiveComponentSize().testTag("btn_open_add_stop")
                        ) {
                            Text("+ Add Stop", fontSize = 11.sp)
                        }
                    }
                }
                if (route.stops.isEmpty()) {
                    Text(
                        text = "Direct shuttle — no intermediate stops registered.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                } else {
                    route.stops.forEach { stop ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = stop.stopName,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "AM: ${stop.pickupTime} • PM: ${stop.dropTime}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                            if (stop.monthlyFee > 0.0) {
                                Text(
                                    text = "$${"%.0f".format(stop.monthlyFee)}/mo",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                HorizontalDivider()

                Text(
                    text = "Passenger Manifest (${route.passengers.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                if (route.passengers.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No students assigned to this route yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                } else {
                    route.passengers.forEach { pax ->
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
                                        text = "${pax.studentName} (${pax.rollNumber})",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "${pax.gradeLevel} • ${pax.stopName}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    val tripTags = buildList {
                                        if (pax.isMorningPickup) add("AM Pickup")
                                        if (pax.isAfternoonDrop) add("PM Drop")
                                    }.joinToString(" & ")
                                    if (tripTags.isNotEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .padding(top = 4.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = tripTags,
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                                if (TenantContext.hasPermission(Permission.TRANSPORT_MANAGE)) {
                                    OutlinedButton(
                                        onClick = { onRemovePassenger(pax.passengerId) },
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .testTag("btn_remove_pax_${pax.passengerId}")
                                    ) {
                                        Text("Remove", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (route.availableSeatsCount > 0 && TenantContext.hasPermission(Permission.TRANSPORT_MANAGE)) {
                Button(
                    onClick = onAssignClick,
                    modifier = Modifier.testTag("btn_assign_from_detail")
                ) {
                    Text("Board Student")
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

@Composable
fun AddStopDialog(
    route: TransportRoute,
    onDismiss: () -> Unit,
    onConfirm: (
        stopName: String,
        pickupTime: String,
        dropTime: String,
        landmark: String,
        monthlyFee: Double
    ) -> Unit
) {
    var stopName by remember { mutableStateOf("") }
    var pickupTime by remember { mutableStateOf("07:35 AM") }
    var dropTime by remember { mutableStateOf("03:45 PM") }
    var landmark by remember { mutableStateOf("") }
    var feeStr by remember { mutableStateOf("80.00") }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("dialog_add_stop"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add Stop (${route.routeCode})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = stopName,
                    onValueChange = { stopName = it },
                    label = { Text("Stop Name *") },
                    placeholder = { Text("e.g. Carfax Tower Stop") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_add_stop_name")
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = pickupTime,
                        onValueChange = { pickupTime = it },
                        label = { Text("AM Pickup *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("input_add_stop_pickup")
                    )
                    OutlinedTextField(
                        value = dropTime,
                        onValueChange = { dropTime = it },
                        label = { Text("PM Drop") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("input_add_stop_drop")
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = landmark,
                        onValueChange = { landmark = it },
                        label = { Text("Landmark") },
                        singleLine = true,
                        modifier = Modifier.weight(1.2f).testTag("input_add_stop_landmark")
                    )
                    OutlinedTextField(
                        value = feeStr,
                        onValueChange = { feeStr = it },
                        label = { Text("Monthly Fee") },
                        singleLine = true,
                        modifier = Modifier.weight(0.8f).testTag("input_add_stop_fee")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val fee = feeStr.toDoubleOrNull() ?: 0.0
                    onConfirm(stopName, pickupTime, dropTime, landmark, fee)
                },
                enabled = stopName.isNotBlank() && pickupTime.isNotBlank(),
                modifier = Modifier.testTag("btn_confirm_add_stop")
            ) {
                Text("Add Stop")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_cancel_add_stop")
            ) {
                Text("Cancel")
            }
        }
    )
}
