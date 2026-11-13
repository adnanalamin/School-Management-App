package com.example.features.transport.domain

import com.example.core.tenant.TenantScopedEntity

/**
 * Operational dispatch status for a school transport route.
 */
enum class RouteStatus(val displayName: String) {
    ACTIVE("Active Route"),
    DELAYED("Delayed"),
    MAINTENANCE("Under Maintenance"),
    SUSPENDED("Suspended");

    companion object {
        fun fromString(value: String?): RouteStatus {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: ACTIVE
        }
    }
}

/**
 * Fleet vehicle classification for transport capacity & maintenance scheduling.
 */
enum class VehicleType(val displayName: String) {
    SCHOOL_BUS("School Bus"),
    MINI_BUS("Mini Bus"),
    SHUTTLE_VAN("Shuttle Van"),
    ELECTRIC_COACH("Electric Coach");

    companion object {
        fun fromString(value: String?): VehicleType {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: SCHOOL_BUS
        }
    }
}

/**
 * Individual boarding/drop-off stop along a transport route.
 */
data class TransportStop(
    val stopId: String,
    val stopName: String,
    val pickupTime: String,
    val dropTime: String,
    val landmark: String = "",
    val monthlyFee: Double = 0.0
) {
    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "stopId" to stopId,
        "stopName" to stopName,
        "pickupTime" to pickupTime,
        "dropTime" to dropTime,
        "landmark" to landmark,
        "monthlyFee" to monthlyFee
    )

    companion object {
        fun fromFirestoreMap(data: Map<String, Any?>): TransportStop {
            return TransportStop(
                stopId = data["stopId"] as? String ?: "",
                stopName = data["stopName"] as? String ?: "",
                pickupTime = data["pickupTime"] as? String ?: "",
                dropTime = data["dropTime"] as? String ?: "",
                landmark = data["landmark"] as? String ?: "",
                monthlyFee = (data["monthlyFee"] as? Number)?.toDouble() ?: 0.0
            )
        }
    }
}

/**
 * Student passenger boarding manifest entry bound to a route and pickup stop.
 */
data class TransportPassenger(
    val passengerId: String,
    val studentId: String,
    val studentName: String,
    val rollNumber: String,
    val gradeLevel: String,
    val stopId: String,
    val stopName: String,
    val guardianContact: String,
    val isMorningPickup: Boolean = true,
    val isAfternoonDrop: Boolean = true
) {
    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "passengerId" to passengerId,
        "studentId" to studentId,
        "studentName" to studentName,
        "rollNumber" to rollNumber,
        "gradeLevel" to gradeLevel,
        "stopId" to stopId,
        "stopName" to stopName,
        "guardianContact" to guardianContact,
        "isMorningPickup" to isMorningPickup,
        "isAfternoonDrop" to isAfternoonDrop
    )

    companion object {
        fun fromFirestoreMap(data: Map<String, Any?>): TransportPassenger {
            return TransportPassenger(
                passengerId = data["passengerId"] as? String ?: "",
                studentId = data["studentId"] as? String ?: "",
                studentName = data["studentName"] as? String ?: "",
                rollNumber = data["rollNumber"] as? String ?: "",
                gradeLevel = data["gradeLevel"] as? String ?: "",
                stopId = data["stopId"] as? String ?: "",
                stopName = data["stopName"] as? String ?: "",
                guardianContact = data["guardianContact"] as? String ?: "",
                isMorningPickup = data["isMorningPickup"] as? Boolean ?: true,
                isAfternoonDrop = data["isAfternoonDrop"] as? Boolean ?: true
            )
        }
    }
}

/**
 * Tenant-scoped school transport route, vehicle, driver, stop schedule, and passenger manifest.
 * Stored under `schools/{schoolId}/transport/{routeId}` (Subcollection 13).
 */
data class TransportRoute(
    override val id: String,
    override val schoolId: String,
    val routeCode: String,
    val routeName: String,
    val vehicleNumber: String,
    val vehicleType: VehicleType = VehicleType.SCHOOL_BUS,
    val capacity: Int = 40,
    val driverName: String,
    val driverPhone: String,
    val attendantName: String = "",
    val departureTime: String = "07:15 AM",
    val arrivalTime: String = "08:10 AM",
    val status: RouteStatus = RouteStatus.ACTIVE,
    val gpsTrackingEnabled: Boolean = true,
    val stops: List<TransportStop> = emptyList(),
    val passengers: List<TransportPassenger> = emptyList(),
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
) : TenantScopedEntity {

    val occupiedSeatsCount: Int
        get() = passengers.size

    val availableSeatsCount: Int
        get() = (capacity - passengers.size).coerceAtLeast(0)

    val occupancyRatePercentage: Double
        get() = if (capacity > 0) (passengers.size.toDouble() / capacity.toDouble()) * 100.0 else 0.0

    val hasAvailableSeat: Boolean
        get() = availableSeatsCount > 0 && status == RouteStatus.ACTIVE

    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "id" to id,
        "schoolId" to schoolId,
        "routeCode" to routeCode,
        "routeName" to routeName,
        "vehicleNumber" to vehicleNumber,
        "vehicleType" to vehicleType.name,
        "capacity" to capacity,
        "driverName" to driverName,
        "driverPhone" to driverPhone,
        "attendantName" to attendantName,
        "departureTime" to departureTime,
        "arrivalTime" to arrivalTime,
        "status" to status.name,
        "gpsTrackingEnabled" to gpsTrackingEnabled,
        "stops" to stops.map { it.toFirestoreMap() },
        "passengers" to passengers.map { it.toFirestoreMap() },
        "notes" to notes,
        "createdAt" to createdAt
    )

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromFirestoreMap(id: String, schoolId: String, data: Map<String, Any>): TransportRoute {
            val rawStops = data["stops"] as? List<Map<String, Any?>> ?: emptyList()
            val rawPassengers = data["passengers"] as? List<Map<String, Any?>> ?: emptyList()

            return TransportRoute(
                id = id,
                schoolId = schoolId,
                routeCode = data["routeCode"] as? String ?: "",
                routeName = data["routeName"] as? String ?: "",
                vehicleNumber = data["vehicleNumber"] as? String ?: "",
                vehicleType = VehicleType.fromString(data["vehicleType"] as? String),
                capacity = (data["capacity"] as? Number)?.toInt() ?: 40,
                driverName = data["driverName"] as? String ?: "",
                driverPhone = data["driverPhone"] as? String ?: "",
                attendantName = data["attendantName"] as? String ?: "",
                departureTime = data["departureTime"] as? String ?: "07:15 AM",
                arrivalTime = data["arrivalTime"] as? String ?: "08:10 AM",
                status = RouteStatus.fromString(data["status"] as? String),
                gpsTrackingEnabled = data["gpsTrackingEnabled"] as? Boolean ?: true,
                stops = rawStops.map { TransportStop.fromFirestoreMap(it) },
                passengers = rawPassengers.map { TransportPassenger.fromFirestoreMap(it) },
                notes = data["notes"] as? String ?: "",
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}
