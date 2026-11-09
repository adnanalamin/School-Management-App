package com.example.features.schools.data

import android.content.Context
import android.util.Log
import com.example.R
import com.example.core.constants.AppConstants
import com.example.core.error.AppError
import com.example.core.error.ErrorHandler
import com.example.core.security.TenantContext
import com.example.core.security.UserRole
import com.example.features.schools.domain.SchoolRepository
import com.example.shared.models.TenantSchool
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseSchoolRepository(
    private val context: Context? = null,
    private val firestoreInstance: FirebaseFirestore? = null
) : SchoolRepository {

    private val db: FirebaseFirestore by lazy {
        firestoreInstance ?: try {
            val dbId = context?.getString(R.string.firestore_database_id)
            if (dbId != null) FirebaseFirestore.getInstance(dbId) else FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w("FirebaseSchoolRepository", "Could not init named DB, fallback to default", e)
            FirebaseFirestore.getInstance()
        }
    }

    override fun generateUniqueSchoolId(code: String): String {
        val sanitizedCode = code.trim().lowercase().replace(Regex("[^a-z0-9]"), "").take(8)
        val prefix = if (sanitizedCode.isEmpty()) "sch" else "sch_$sanitizedCode"
        val timestamp = System.currentTimeMillis()
        val randomSuffix = UUID.randomUUID().toString().replace("-", "").take(4)
        return "${prefix}_${timestamp}_$randomSuffix"
    }

    override suspend fun checkSchoolCodeAvailable(code: String): Result<Boolean> {
        return try {
            val cleanCode = code.trim().uppercase()
            val query = db.collection(AppConstants.COLLECTION_SCHOOLS)
                .whereEqualTo("code", cleanCode)
                .limit(1)
                .get()
                .await()
            Result.success(query.isEmpty)
        } catch (e: Exception) {
            // If offline, check against known demo codes
            val cleanCode = code.trim().uppercase()
            val isDemoCode = cleanCode == "OXF-101" || cleanCode == "CIS101"
            Result.success(!isDemoCode)
        }
    }

    override suspend fun registerSchool(school: TenantSchool, adminUid: String): Result<TenantSchool> {
        return try {
            val cleanCode = school.code.trim().uppercase()
            
            // Check availability if online
            val codeAvailable = checkSchoolCodeAvailable(cleanCode).getOrDefault(true)
            if (!codeAvailable && school.id.isBlank()) {
                return Result.failure(AppError.ValidationError("School code '$cleanCode' is already taken by another institution."))
            }

            val uniqueSchoolId = if (school.id.isNotBlank()) school.id else generateUniqueSchoolId(cleanCode)
            val schoolToSave = school.copy(
                id = uniqueSchoolId,
                code = cleanCode,
                adminUid = adminUid,
                createdAt = if (school.createdAt > 0) school.createdAt else System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            // Save school document in Firestore
            val schoolDocRef = db.collection(AppConstants.COLLECTION_SCHOOLS).document(uniqueSchoolId)
            schoolDocRef.set(schoolToSave.toFirestoreMap()).await()

            // Bind user record to the newly provisioned school
            if (adminUid.isNotBlank()) {
                val userDocRef = db.collection(AppConstants.COLLECTION_GLOBAL_USERS).document(adminUid)
                val userPatch = mapOf<String, Any>(
                    "schoolId" to uniqueSchoolId,
                    "role" to UserRole.SCHOOL_ADMIN.name,
                    "updatedAt" to com.google.firebase.Timestamp.now()
                )
                try {
                    userDocRef.update(userPatch).await()
                } catch (_: Exception) {
                    userDocRef.set(userPatch).await()
                }
            }

            // Immediately switch local tenant boundary to the newly created institution
            TenantContext.setTenant(
                schoolId = uniqueSchoolId,
                role = UserRole.SCHOOL_ADMIN,
                userId = adminUid.ifBlank { "usr_admin_${System.currentTimeMillis() % 1000}" }
            )

            Result.success(schoolToSave)
        } catch (e: Exception) {
            val appError = ErrorHandler.sanitize(e)
            Result.failure(appError)
        }
    }

    override suspend fun getSchoolById(schoolId: String): Result<TenantSchool> {
        return try {
            val doc = db.collection(AppConstants.COLLECTION_SCHOOLS).document(schoolId).get().await()
            if (doc.exists()) {
                val data = doc.data ?: emptyMap()
                Result.success(TenantSchool.fromFirestoreMap(doc.id, data))
            } else {
                // If demo school requested and not yet in Firestore, provide baseline
                if (schoolId == "sch_oxford_01") {
                    Result.success(
                        TenantSchool(
                            id = "sch_oxford_01",
                            name = "Oxford International Academy",
                            code = "OXF-101",
                            address = "742 Evergreen Terrace, Educational District",
                            phone = "+1 (555) 234-5678",
                            email = "info@oxfordacademy.edu",
                            website = "https://oxfordacademy.edu",
                            establishedYear = 1995,
                            schoolType = "K-12 Institution",
                            academicSession = "2026-2027",
                            currency = "USD",
                            currencySymbol = "$",
                            timezone = "America/New_York"
                        )
                    )
                } else {
                    Result.failure(AppError.ResourceNotFoundError("School with ID '$schoolId' not found."))
                }
            }
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    override suspend fun getSchoolByCode(code: String): Result<TenantSchool?> {
        return try {
            val cleanCode = code.trim().uppercase()
            val query = db.collection(AppConstants.COLLECTION_SCHOOLS)
                .whereEqualTo("code", cleanCode)
                .limit(1)
                .get()
                .await()

            if (query.isEmpty) {
                if (cleanCode == "OXF-101") {
                    getSchoolById("sch_oxford_01").map { it }
                } else {
                    Result.success(null)
                }
            } else {
                val doc = query.documents.first()
                Result.success(TenantSchool.fromFirestoreMap(doc.id, doc.data ?: emptyMap()))
            }
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    override suspend fun updateSchool(school: TenantSchool): Result<Unit> {
        return try {
            val updated = school.copy(updatedAt = System.currentTimeMillis())
            db.collection(AppConstants.COLLECTION_SCHOOLS)
                .document(school.id)
                .set(updated.toFirestoreMap())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    override fun observeSchool(schoolId: String): Flow<TenantSchool?> = callbackFlow {
        val docRef = db.collection(AppConstants.COLLECTION_SCHOOLS).document(schoolId)
        val registration = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(null)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val data = snapshot.data ?: emptyMap()
                trySend(TenantSchool.fromFirestoreMap(snapshot.id, data))
            } else {
                trySend(null)
            }
        }
        awaitClose { registration.remove() }
    }
}
