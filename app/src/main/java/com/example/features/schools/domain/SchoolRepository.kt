package com.example.features.schools.domain

import com.example.shared.models.TenantSchool
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository contract for managing tenant educational institutions.
 * Handles creation, validation, unique tenant ID generation, and real-time observation.
 */
interface SchoolRepository {
    /**
     * Registers a new school institution in Firestore and binds the creating admin account.
     * Enforces unique tenant ID and validates school code uniqueness.
     */
    suspend fun registerSchool(school: TenantSchool, adminUid: String): Result<TenantSchool>

    /**
     * Retrieves school institution details by unique tenant ID.
     */
    suspend fun getSchoolById(schoolId: String): Result<TenantSchool>

    /**
     * Looks up an institution by human-readable school code (e.g. OXF-101).
     */
    suspend fun getSchoolByCode(code: String): Result<TenantSchool?>

    /**
     * Checks if a proposed school code is available for registration.
     */
    suspend fun checkSchoolCodeAvailable(code: String): Result<Boolean>

    /**
     * Updates institution profile and operational settings.
     */
    suspend fun updateSchool(school: TenantSchool): Result<Unit>

    /**
     * Emits continuous updates for a specific school tenant.
     */
    fun observeSchool(schoolId: String): Flow<TenantSchool?>

    /**
     * Generates a collision-resistant, cryptographically unique school identifier (e.g. sch_oxf101_17415...).
     */
    fun generateUniqueSchoolId(code: String): String
}
