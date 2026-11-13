package com.example.features.transport.data

import android.content.Context
import android.util.Log
import com.example.R
import com.example.core.error.ErrorHandler
import com.example.core.tenant.TenantPathProvider
import com.example.features.transport.domain.RouteStatus
import com.example.features.transport.domain.TransportPassenger
import com.example.features.transport.domain.TransportRoute
import com.example.features.transport.domain.TransportStop
import com.example.features.transport.domain.VehicleType
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class TransportRepository(
    private val context: Context? = null,
    private val firestoreInstance: FirebaseFirestore? = null
) {
    private val db: FirebaseFirestore by lazy {
        firestoreInstance ?: try {
            val dbId = context?.getString(R.string.firestore_database_id)
            if (dbId != null) FirebaseFirestore.getInstance(dbId) else FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w("TransportRepository", "Fallback to default Firestore instance", e)
            FirebaseFirestore.getInstance()
        }
    }

    suspend fun getRoutes(
        schoolId: String = TenantPathProvider.requireActiveSchoolId(),
        statusFilter: RouteStatus? = null,
        onlyWithSeats: Boolean = false
    ): List<TransportRoute> {
        return try {
            TenantPathProvider.verifyTenantBoundary(schoolId)
            val path = TenantPathProvider.transportPath(schoolId)
            val snapshot = db.collection(path).get().await()
            val list = if (snapshot.isEmpty) {
                getPreSeededRoutes(schoolId)
            } else {
                snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    TransportRoute.fromFirestoreMap(doc.id, schoolId, data)
                }
            }
            filterRoutes(list, statusFilter, onlyWithSeats)
        } catch (e: Exception) {
            Log.w("TransportRepository", "Error retrieving transport routes from Firestore, fallback to preseeded", e)
            val seeded = getPreSeededRoutes(schoolId)
            filterRoutes(seeded, statusFilter, onlyWithSeats)
        }
    }

    private fun filterRoutes(
        routes: List<TransportRoute>,
        statusFilter: RouteStatus?,
        onlyWithSeats: Boolean
    ): List<TransportRoute> {
        return routes.filter { route ->
            (statusFilter == null || route.status == statusFilter) &&
                (!onlyWithSeats || route.hasAvailableSeat)
        }
    }

    suspend fun saveRoute(route: TransportRoute): Result<TransportRoute> {
        return try {
            TenantPathProvider.verifyTenantBoundary(route.schoolId)
            val path = TenantPathProvider.transportPath(route.schoolId)
            db.collection(path).document(route.id).set(route.toFirestoreMap()).await()
            Result.success(route)
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    suspend fun assignPassenger(
        schoolId: String,
        routeId: String,
        passenger: TransportPassenger
    ): Result<TransportRoute> {
        return try {
            TenantPathProvider.verifyTenantBoundary(schoolId)
            val routes = getRoutes(schoolId)
            val target = routes.firstOrNull { it.id == routeId }
                ?: return Result.failure(Exception("Transport route not found"))

            if (target.availableSeatsCount <= 0) {
                return Result.failure(Exception("Vehicle capacity reached for route ${target.routeCode}"))
            }

            val updatedRoute = target.copy(
                passengers = listOf(passenger) + target.passengers
            )
            saveRoute(updatedRoute)
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    suspend fun removePassenger(
        schoolId: String,
        routeId: String,
        passengerId: String
    ): Result<TransportRoute> {
        return try {
            TenantPathProvider.verifyTenantBoundary(schoolId)
            val routes = getRoutes(schoolId)
            val target = routes.firstOrNull { it.id == routeId }
                ?: return Result.failure(Exception("Transport route not found"))

            val updatedRoute = target.copy(
                passengers = target.passengers.filterNot { it.passengerId == passengerId }
            )
            saveRoute(updatedRoute)
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    suspend fun updateRouteStatus(
        schoolId: String,
        routeId: String,
        newStatus: RouteStatus
    ): Result<TransportRoute> {
        return try {
            TenantPathProvider.verifyTenantBoundary(schoolId)
            val routes = getRoutes(schoolId)
            val target = routes.firstOrNull { it.id == routeId }
                ?: return Result.failure(Exception("Transport route not found"))

            val updatedRoute = target.copy(status = newStatus)
            saveRoute(updatedRoute)
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    suspend fun addStopToRoute(
        schoolId: String,
        routeId: String,
        stop: TransportStop
    ): Result<TransportRoute> {
        return try {
            TenantPathProvider.verifyTenantBoundary(schoolId)
            val routes = getRoutes(schoolId)
            val target = routes.firstOrNull { it.id == routeId }
                ?: return Result.failure(Exception("Transport route not found"))

            val updatedRoute = target.copy(stops = target.stops + stop)
            saveRoute(updatedRoute)
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    suspend fun deleteRoute(schoolId: String, routeId: String): Result<Unit> {
        return try {
            TenantPathProvider.verifyTenantBoundary(schoolId)
            val path = TenantPathProvider.transportPath(schoolId)
            db.collection(path).document(routeId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    fun getPreSeededRoutes(schoolId: String): List<TransportRoute> {
        return if (schoolId.contains("oxford") || schoolId.startsWith("sch_oxf")) {
            listOf(
                TransportRoute(
                    id = "rt_oxf_01",
                    schoolId = schoolId,
                    routeCode = "RT-OXF-01",
                    routeName = "North Campus Express — Headington",
                    vehicleNumber = "OXF-BUS-104",
                    vehicleType = VehicleType.SCHOOL_BUS,
                    capacity = 42,
                    driverName = "Arthur Pendelton",
                    driverPhone = "+44 7700 900112",
                    attendantName = "Martha Evans",
                    departureTime = "07:10 AM",
                    arrivalTime = "08:05 AM",
                    status = RouteStatus.ACTIVE,
                    gpsTrackingEnabled = true,
                    notes = "Primary morning & afternoon commuter coach with live RFID boarding.",
                    stops = listOf(
                        TransportStop("stp_oxf_1", "Headington Shark Junction", "07:15 AM", "03:45 PM", "London Rd", 85.0),
                        TransportStop("stp_oxf_2", "Marston Ferry Crossroads", "07:32 AM", "03:30 PM", "St Andrew's Park", 75.0),
                        TransportStop("stp_oxf_3", "Oxford High Gate", "08:05 AM", "03:15 PM", "Main Campus Bay 1", 0.0)
                    ),
                    passengers = listOf(
                        TransportPassenger(
                            passengerId = "pax_oxf_01",
                            studentId = "stu_oxf_01",
                            studentName = "Amina Rahman",
                            rollNumber = "OXF-10A-001",
                            gradeLevel = "Grade 10 - A",
                            stopId = "stp_oxf_1",
                            stopName = "Headington Shark Junction",
                            guardianContact = "+44 7700 900401"
                        ),
                        TransportPassenger(
                            passengerId = "pax_oxf_02",
                            studentId = "stu_oxf_02",
                            studentName = "Oliver Smith",
                            rollNumber = "OXF-10A-002",
                            gradeLevel = "Grade 10 - A",
                            stopId = "stp_oxf_2",
                            stopName = "Marston Ferry Crossroads",
                            guardianContact = "+44 7700 900402"
                        )
                    )
                ),
                TransportRoute(
                    id = "rt_oxf_02",
                    schoolId = schoolId,
                    routeCode = "RT-OXF-02",
                    routeName = "Jericho & Summertown Shuttle",
                    vehicleNumber = "OXF-VAN-208",
                    vehicleType = VehicleType.ELECTRIC_COACH,
                    capacity = 24,
                    driverName = "Daniel Okafor",
                    driverPhone = "+44 7700 900215",
                    attendantName = "Clara Jenkins",
                    departureTime = "07:25 AM",
                    arrivalTime = "08:10 AM",
                    status = RouteStatus.ACTIVE,
                    gpsTrackingEnabled = true,
                    notes = "Zero-emission electric shuttle serving north-west residential stops.",
                    stops = listOf(
                        TransportStop("stp_oxf_4", "Summertown Parade", "07:30 AM", "03:40 PM", "Banbury Rd", 90.0),
                        TransportStop("stp_oxf_5", "Walton Street Corner", "07:48 AM", "03:28 PM", "Observatory Quarter", 80.0)
                    ),
                    passengers = listOf(
                        TransportPassenger(
                            passengerId = "pax_oxf_03",
                            studentId = "stu_oxf_03",
                            studentName = "Sophia Chen",
                            rollNumber = "OXF-10A-003",
                            gradeLevel = "Grade 10 - A",
                            stopId = "stp_oxf_4",
                            stopName = "Summertown Parade",
                            guardianContact = "+44 7700 900403"
                        )
                    )
                ),
                TransportRoute(
                    id = "rt_oxf_03",
                    schoolId = schoolId,
                    routeCode = "RT-OXF-03",
                    routeName = "South Abingdon Link",
                    vehicleNumber = "OXF-BUS-312",
                    vehicleType = VehicleType.MINI_BUS,
                    capacity = 28,
                    driverName = "Robert Gallagher",
                    driverPhone = "+44 7700 900319",
                    attendantName = "Hannah Patel",
                    departureTime = "07:00 AM",
                    arrivalTime = "08:15 AM",
                    status = RouteStatus.DELAYED,
                    gpsTrackingEnabled = true,
                    notes = "Minor detour via Kennington Bridge due to roadworks.",
                    stops = listOf(
                        TransportStop("stp_oxf_6", "Abingdon Market Square", "07:08 AM", "04:05 PM", "Guildhall Bay", 110.0),
                        TransportStop("stp_oxf_7", "Botley Interchange", "07:40 AM", "03:35 PM", "West Way Stop", 85.0)
                    ),
                    passengers = listOf(
                        TransportPassenger(
                            passengerId = "pax_oxf_04",
                            studentId = "stu_oxf_04",
                            studentName = "Liam Thompson",
                            rollNumber = "OXF-09B-014",
                            gradeLevel = "Grade 9 - B",
                            stopId = "stp_oxf_6",
                            stopName = "Abingdon Market Square",
                            guardianContact = "+44 7700 900414",
                            isAfternoonDrop = false
                        )
                    )
                )
            )
        } else {
            listOf(
                TransportRoute(
                    id = "rt_cam_01",
                    schoolId = schoolId,
                    routeCode = "RT-CAM-01",
                    routeName = "Grantchester & Trumpington Loop",
                    vehicleNumber = "CAM-BUS-011",
                    vehicleType = VehicleType.SCHOOL_BUS,
                    capacity = 40,
                    driverName = "Thomas Hargreaves",
                    driverPhone = "+44 7700 900701",
                    attendantName = "Lucy Abbott",
                    departureTime = "07:20 AM",
                    arrivalTime = "08:05 AM",
                    status = RouteStatus.ACTIVE,
                    gpsTrackingEnabled = true,
                    stops = listOf(
                        TransportStop("stp_cam_1", "Trumpington Park & Ride", "07:25 AM", "03:45 PM", "Hauxton Rd", 95.0),
                        TransportStop("stp_cam_2", "Barton Road Gate", "07:50 AM", "03:25 PM", "Newnham Rd", 75.0)
                    ),
                    passengers = listOf(
                        TransportPassenger(
                            passengerId = "pax_cam_01",
                            studentId = "stu_cam_01",
                            studentName = "Elizabeth Bennet",
                            rollNumber = "CAM-10A-001",
                            gradeLevel = "Year 10 - A",
                            stopId = "stp_cam_1",
                            stopName = "Trumpington Park & Ride",
                            guardianContact = "+44 7700 900801"
                        )
                    )
                ),
                TransportRoute(
                    id = "rt_cam_02",
                    schoolId = schoolId,
                    routeCode = "RT-CAM-02",
                    routeName = "Chesterton River Express",
                    vehicleNumber = "CAM-VAN-022",
                    vehicleType = VehicleType.SHUTTLE_VAN,
                    capacity = 16,
                    driverName = "Marcus Sterling",
                    driverPhone = "+44 7700 900702",
                    attendantName = "Grace Lin",
                    departureTime = "07:35 AM",
                    arrivalTime = "08:10 AM",
                    status = RouteStatus.MAINTENANCE,
                    gpsTrackingEnabled = false,
                    notes = "Scheduled quarterly brake inspection.",
                    stops = listOf(
                        TransportStop("stp_cam_3", "Chesterton High St", "07:40 AM", "03:35 PM", "Green End Rd", 80.0)
                    )
                )
            )
        }
    }
}
