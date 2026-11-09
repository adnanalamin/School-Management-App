package com.example.shared.models

import com.google.firebase.Timestamp

/**
 * TenantSchool represents an isolated educational institution in the EduManage SaaS platform.
 * Contains complete institutional metadata, contact information, regional configuration, and tenant branding.
 */
data class TenantSchool(
    val id: String,
    val name: String,
    val code: String,
    val logoUrl: String? = null,
    val slogan: String? = null,
    val address: String = "",
    val phone: String = "",
    val email: String = "",
    val website: String = "",
    val establishedYear: Int = 2000,
    val schoolType: String = "K-12 Institution", // e.g., K-12, Primary & Middle, Secondary High, Academy, Vocational
    val academicSession: String = "2026-2027",
    val currency: String = "USD",
    val currencySymbol: String = "$",
    val timezone: String = "UTC",
    val primaryColorHex: String = "#1E3A8A",
    val secondaryColorHex: String = "#0F766E",
    val status: SchoolStatus = SchoolStatus.ACTIVE,
    val subscriptionPlan: String = "PRO",
    val adminUid: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Serializes this TenantSchool instance into a Firestore document map.
     */
    fun toFirestoreMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "id" to id,
            "name" to name,
            "code" to code,
            "address" to address,
            "phone" to phone,
            "email" to email,
            "website" to website,
            "establishedYear" to establishedYear,
            "schoolType" to schoolType,
            "academicSession" to academicSession,
            "currency" to currency,
            "currencySymbol" to currencySymbol,
            "timezone" to timezone,
            "primaryColorHex" to primaryColorHex,
            "secondaryColorHex" to secondaryColorHex,
            "status" to status.name,
            "subscriptionPlan" to subscriptionPlan,
            "adminUid" to adminUid,
            "ownerUid" to adminUid,
            "createdAt" to Timestamp(createdAt / 1000, ((createdAt % 1000) * 1000000).toInt()),
            "updatedAt" to Timestamp(updatedAt / 1000, ((updatedAt % 1000) * 1000000).toInt())
        )
        logoUrl?.let { map["logoUrl"] = it }
        slogan?.let { map["slogan"] = it }
        return map
    }

    companion object {
        /**
         * Deserializes a Firestore document map into a TenantSchool instance.
         */
        fun fromFirestoreMap(id: String, data: Map<String, Any?>): TenantSchool {
            val createdTs = data["createdAt"] as? Timestamp
            val updatedTs = data["updatedAt"] as? Timestamp
            val statusStr = data["status"] as? String ?: SchoolStatus.ACTIVE.name
            val uid = (data["adminUid"] ?: data["ownerUid"]) as? String ?: ""

            return TenantSchool(
                id = id,
                name = data["name"] as? String ?: "",
                code = data["code"] as? String ?: "",
                logoUrl = data["logoUrl"] as? String,
                slogan = data["slogan"] as? String,
                address = data["address"] as? String ?: "",
                phone = data["phone"] as? String ?: "",
                email = data["email"] as? String ?: "",
                website = data["website"] as? String ?: "",
                establishedYear = (data["establishedYear"] as? Number)?.toInt() ?: 2000,
                schoolType = data["schoolType"] as? String ?: "K-12 Institution",
                academicSession = data["academicSession"] as? String ?: "2026-2027",
                currency = data["currency"] as? String ?: "USD",
                currencySymbol = data["currencySymbol"] as? String ?: "$",
                timezone = data["timezone"] as? String ?: "UTC",
                primaryColorHex = data["primaryColorHex"] as? String ?: "#1E3A8A",
                secondaryColorHex = data["secondaryColorHex"] as? String ?: "#0F766E",
                status = try { SchoolStatus.valueOf(statusStr) } catch (_: Exception) { SchoolStatus.ACTIVE },
                subscriptionPlan = data["subscriptionPlan"] as? String ?: "PRO",
                adminUid = uid,
                createdAt = createdTs?.toDate()?.time ?: System.currentTimeMillis(),
                updatedAt = updatedTs?.toDate()?.time ?: System.currentTimeMillis()
            )
        }
    }
}

enum class SchoolStatus {
    PENDING_APPROVAL,
    ACTIVE,
    SUSPENDED,
    ARCHIVED
}
