package com.example.features.transport.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.security.Permission
import com.example.core.security.TenantContext
import com.example.features.transport.data.TransportRepository
import com.example.features.transport.domain.RouteStatus
import com.example.features.transport.domain.TransportPassenger
import com.example.features.transport.domain.TransportRoute
import com.example.features.transport.domain.TransportStop
import com.example.features.transport.domain.VehicleType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class TransportUiState(
    val routes: List<TransportRoute> = emptyList(),
    val searchQuery: String = "",
    val selectedStatusFilter: RouteStatus? = null,
    val onlyAvailableSeatsFilter: Boolean = false,
    val isLoading: Boolean = false,
    val statusMessage: String? = null,
    val errorMessage: String? = null,
    val showAddRouteDialog: Boolean = false,
    val showAssignPassengerDialog: Boolean = false,
    val showAddStopDialog: Boolean = false,
    val showRouteDetailDialog: Boolean = false,
    val selectedRouteForAction: TransportRoute? = null
) {
    val filteredRoutes: List<TransportRoute>
        get() = routes.filter { route ->
            val matchesSearch = if (searchQuery.isBlank()) true else {
                val q = searchQuery.trim().lowercase()
                route.routeName.lowercase().contains(q) ||
                    route.routeCode.lowercase().contains(q) ||
                    route.vehicleNumber.lowercase().contains(q) ||
                    route.driverName.lowercase().contains(q) ||
                    route.stops.any {
                        it.stopName.lowercase().contains(q) || it.landmark.lowercase().contains(q)
                    } ||
                    route.passengers.any {
                        it.studentName.lowercase().contains(q) || it.rollNumber.lowercase().contains(q)
                    }
            }
            val matchesStatus = selectedStatusFilter == null || route.status == selectedStatusFilter
            val matchesSeats = !onlyAvailableSeatsFilter || route.hasAvailableSeat
            matchesSearch && matchesStatus && matchesSeats
        }

    val totalRoutesCount: Int
        get() = routes.size

    val activeRoutesCount: Int
        get() = routes.count { it.status == RouteStatus.ACTIVE }

    val totalFleetCapacity: Int
        get() = routes.sumOf { it.capacity }

    val assignedPassengersCount: Int
        get() = routes.sumOf { it.occupiedSeatsCount }

    val availableSeatsTotal: Int
        get() = routes.sumOf { it.availableSeatsCount }
}

class TransportViewModel(
    private val transportRepository: TransportRepository = TransportRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransportUiState())
    val uiState: StateFlow<TransportUiState> = _uiState.asStateFlow()

    init {
        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        val seeded = transportRepository.getPreSeededRoutes(schoolId)
        _uiState.update { it.copy(routes = seeded) }
        loadRoutes()
    }

    fun loadRoutes() {
        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val list = transportRepository.getRoutes(schoolId = schoolId)
                _uiState.update { it.copy(routes = list, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to load transport fleet"
                    )
                }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setStatusFilter(status: RouteStatus?) {
        _uiState.update { it.copy(selectedStatusFilter = status) }
    }

    fun toggleOnlyAvailableSeatsFilter() {
        _uiState.update { it.copy(onlyAvailableSeatsFilter = !it.onlyAvailableSeatsFilter) }
    }

    fun clearFilters() {
        _uiState.update {
            it.copy(
                searchQuery = "",
                selectedStatusFilter = null,
                onlyAvailableSeatsFilter = false
            )
        }
    }

    fun openAddRouteDialog() {
        if (!TenantContext.hasPermission(Permission.TRANSPORT_MANAGE)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires TRANSPORT_MANAGE permission") }
            return
        }
        _uiState.update { it.copy(showAddRouteDialog = true, errorMessage = null) }
    }

    fun closeAddRouteDialog() {
        _uiState.update { it.copy(showAddRouteDialog = false) }
    }

    fun openAssignPassengerDialog(route: TransportRoute) {
        if (!TenantContext.hasPermission(Permission.TRANSPORT_MANAGE)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires TRANSPORT_MANAGE permission") }
            return
        }
        if (route.availableSeatsCount <= 0) {
            _uiState.update { it.copy(errorMessage = "Vehicle '${route.vehicleNumber}' is at full capacity.") }
            return
        }
        _uiState.update {
            it.copy(
                selectedRouteForAction = route,
                showAssignPassengerDialog = true,
                errorMessage = null
            )
        }
    }

    fun closeAssignPassengerDialog() {
        _uiState.update {
            it.copy(
                showAssignPassengerDialog = false,
                selectedRouteForAction = null
            )
        }
    }

    fun openRouteDetail(route: TransportRoute) {
        _uiState.update {
            it.copy(
                selectedRouteForAction = route,
                showRouteDetailDialog = true
            )
        }
    }

    fun closeRouteDetail() {
        _uiState.update {
            it.copy(
                showRouteDetailDialog = false,
                selectedRouteForAction = null
            )
        }
    }

    fun openAddStopDialog(route: TransportRoute) {
        if (!TenantContext.hasPermission(Permission.TRANSPORT_MANAGE)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires TRANSPORT_MANAGE permission") }
            return
        }
        _uiState.update {
            it.copy(
                selectedRouteForAction = route,
                showAddStopDialog = true,
                errorMessage = null
            )
        }
    }

    fun closeAddStopDialog() {
        _uiState.update { it.copy(showAddStopDialog = false) }
    }

    fun addRoute(
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
    ) {
        if (!TenantContext.hasPermission(Permission.TRANSPORT_MANAGE)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires TRANSPORT_MANAGE permission") }
            return
        }

        if (routeCode.isBlank() || routeName.isBlank() || vehicleNumber.isBlank() || driverName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Route code, name, vehicle number, and driver are required.") }
            return
        }

        val safeCapacity = capacity.coerceAtLeast(4)
        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        val initialStops = if (primaryStopName.isNotBlank()) {
            listOf(
                TransportStop(
                    stopId = "stp_${UUID.randomUUID().toString().take(6)}",
                    stopName = primaryStopName.trim(),
                    pickupTime = primaryStopPickupTime.ifBlank { departureTime.ifBlank { "07:15 AM" } }.trim(),
                    dropTime = "03:35 PM",
                    landmark = "Primary Boarding Point",
                    monthlyFee = primaryStopMonthlyFee.coerceAtLeast(0.0)
                )
            )
        } else {
            emptyList()
        }

        val newRoute = TransportRoute(
            id = "rt_${UUID.randomUUID().toString().take(8)}",
            schoolId = schoolId,
            routeCode = routeCode.trim().uppercase(),
            routeName = routeName.trim(),
            vehicleNumber = vehicleNumber.trim().uppercase(),
            vehicleType = vehicleType,
            capacity = safeCapacity,
            driverName = driverName.trim(),
            driverPhone = driverPhone.trim(),
            attendantName = attendantName.trim(),
            departureTime = departureTime.ifBlank { "07:15 AM" }.trim(),
            arrivalTime = arrivalTime.ifBlank { "08:10 AM" }.trim(),
            status = RouteStatus.ACTIVE,
            gpsTrackingEnabled = true,
            stops = initialStops,
            passengers = emptyList(),
            notes = notes.trim(),
            createdAt = System.currentTimeMillis()
        )

        _uiState.update { state ->
            state.copy(
                routes = listOf(newRoute) + state.routes,
                showAddRouteDialog = false,
                statusMessage = "Route '${newRoute.routeCode}' (${newRoute.vehicleNumber}) added to fleet.",
                errorMessage = null
            )
        }

        viewModelScope.launch {
            transportRepository.saveRoute(newRoute)
        }
    }

    fun assignPassenger(
        routeId: String,
        studentName: String,
        rollNumber: String,
        gradeLevel: String,
        stopId: String,
        stopName: String,
        guardianContact: String,
        isMorningPickup: Boolean,
        isAfternoonDrop: Boolean
    ) {
        if (!TenantContext.hasPermission(Permission.TRANSPORT_MANAGE)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires TRANSPORT_MANAGE permission") }
            return
        }

        if (studentName.isBlank() || rollNumber.isBlank() || stopName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Student name, roll number, and boarding stop are required.") }
            return
        }

        val target = _uiState.value.routes.firstOrNull { it.id == routeId } ?: return
        if (target.availableSeatsCount <= 0) {
            _uiState.update { it.copy(errorMessage = "No open seats left on '${target.routeCode}'.") }
            return
        }

        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        val passenger = TransportPassenger(
            passengerId = "pax_${UUID.randomUUID().toString().take(8)}",
            studentId = "stu_${UUID.randomUUID().toString().take(6)}",
            studentName = studentName.trim(),
            rollNumber = rollNumber.trim().uppercase(),
            gradeLevel = gradeLevel.ifBlank { "Grade 10" }.trim(),
            stopId = stopId.ifBlank { "stp_custom" },
            stopName = stopName.trim(),
            guardianContact = guardianContact.trim(),
            isMorningPickup = isMorningPickup,
            isAfternoonDrop = isAfternoonDrop
        )

        _uiState.update { state ->
            val updatedRoutes = state.routes.map { route ->
                if (route.id == routeId) {
                    route.copy(passengers = listOf(passenger) + route.passengers)
                } else route
            }
            val updatedSelected = updatedRoutes.firstOrNull { it.id == routeId }
            state.copy(
                routes = updatedRoutes,
                showAssignPassengerDialog = false,
                selectedRouteForAction = if (state.showRouteDetailDialog) updatedSelected else null,
                statusMessage = "Assigned ${passenger.studentName} to ${target.routeCode} (${passenger.stopName}).",
                errorMessage = null
            )
        }

        viewModelScope.launch {
            transportRepository.assignPassenger(schoolId, routeId, passenger)
        }
    }

    fun removePassenger(routeId: String, passengerId: String) {
        if (!TenantContext.hasPermission(Permission.TRANSPORT_MANAGE)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires TRANSPORT_MANAGE permission") }
            return
        }

        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        _uiState.update { state ->
            val updatedRoutes = state.routes.map { route ->
                if (route.id == routeId) {
                    route.copy(passengers = route.passengers.filterNot { it.passengerId == passengerId })
                } else route
            }
            val updatedSelected = updatedRoutes.firstOrNull { it.id == routeId }
            state.copy(
                routes = updatedRoutes,
                selectedRouteForAction = if (state.showRouteDetailDialog) updatedSelected else state.selectedRouteForAction,
                statusMessage = "Student removed from route manifest.",
                errorMessage = null
            )
        }

        viewModelScope.launch {
            transportRepository.removePassenger(schoolId, routeId, passengerId)
        }
    }

    fun updateRouteStatus(routeId: String, newStatus: RouteStatus) {
        if (!TenantContext.hasPermission(Permission.TRANSPORT_MANAGE)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires TRANSPORT_MANAGE permission") }
            return
        }

        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        _uiState.update { state ->
            val updatedRoutes = state.routes.map { route ->
                if (route.id == routeId) route.copy(status = newStatus) else route
            }
            val updatedSelected = updatedRoutes.firstOrNull { it.id == routeId }
            state.copy(
                routes = updatedRoutes,
                selectedRouteForAction = if (state.showRouteDetailDialog) updatedSelected else state.selectedRouteForAction,
                statusMessage = "Route status updated to ${newStatus.displayName}.",
                errorMessage = null
            )
        }

        viewModelScope.launch {
            transportRepository.updateRouteStatus(schoolId, routeId, newStatus)
        }
    }

    fun addStopToRoute(
        routeId: String,
        stopName: String,
        pickupTime: String,
        dropTime: String,
        landmark: String,
        monthlyFee: Double
    ) {
        if (!TenantContext.hasPermission(Permission.TRANSPORT_MANAGE)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires TRANSPORT_MANAGE permission") }
            return
        }

        if (stopName.isBlank() || pickupTime.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Stop name and morning pickup time are required.") }
            return
        }

        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        val newStop = TransportStop(
            stopId = "stp_${UUID.randomUUID().toString().take(6)}",
            stopName = stopName.trim(),
            pickupTime = pickupTime.trim(),
            dropTime = dropTime.ifBlank { "03:45 PM" }.trim(),
            landmark = landmark.trim(),
            monthlyFee = monthlyFee.coerceAtLeast(0.0)
        )

        _uiState.update { state ->
            val updatedRoutes = state.routes.map { route ->
                if (route.id == routeId) route.copy(stops = route.stops + newStop) else route
            }
            val updatedSelected = updatedRoutes.firstOrNull { it.id == routeId }
            state.copy(
                routes = updatedRoutes,
                showAddStopDialog = false,
                selectedRouteForAction = if (state.showRouteDetailDialog) updatedSelected else state.selectedRouteForAction,
                statusMessage = "Added stop '${newStop.stopName}' to route schedule.",
                errorMessage = null
            )
        }

        viewModelScope.launch {
            transportRepository.addStopToRoute(schoolId, routeId, newStop)
        }
    }

    fun deleteRoute(routeId: String) {
        if (!TenantContext.hasPermission(Permission.TRANSPORT_MANAGE)) {
            _uiState.update { it.copy(errorMessage = "Unauthorized: Requires TRANSPORT_MANAGE permission") }
            return
        }

        val schoolId = TenantContext.activeSchoolId.value ?: "sch_oxford_01"
        _uiState.update { state ->
            state.copy(
                routes = state.routes.filterNot { it.id == routeId },
                statusMessage = "Transport route decommissioned from fleet."
            )
        }

        viewModelScope.launch {
            transportRepository.deleteRoute(schoolId, routeId)
        }
    }

    fun dismissStatusMessage() {
        _uiState.update { it.copy(statusMessage = null, errorMessage = null) }
    }
}
