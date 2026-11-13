package com.example.features.branding.data

import android.content.Context
import android.util.Log
import com.example.R
import com.example.core.error.ErrorHandler
import com.example.core.tenant.TenantPathProvider
import com.example.core.theme.BrandThemeController
import com.example.features.branding.domain.BrandPalettePreset
import com.example.features.branding.domain.CrestEmblemStyle
import com.example.features.branding.domain.DashboardLayoutDensity
import com.example.features.branding.domain.ReportCardHeaderStyle
import com.example.features.branding.domain.SchoolBrandingConfig
import com.example.features.branding.domain.UiCornerStyle
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class BrandingRepository(
    private val context: Context? = null,
    private val firestoreInstance: FirebaseFirestore? = null
) {
    private val db: FirebaseFirestore by lazy {
        firestoreInstance ?: try {
            val dbId = context?.getString(R.string.firestore_database_id)
            if (dbId != null) FirebaseFirestore.getInstance(dbId) else FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w("BrandingRepository", "Fallback to default Firestore instance", e)
            FirebaseFirestore.getInstance()
        }
    }

    suspend fun getBrandingConfig(
        schoolId: String = TenantPathProvider.requireActiveSchoolId()
    ): SchoolBrandingConfig {
        return try {
            TenantPathProvider.verifyTenantBoundary(schoolId)
            val path = TenantPathProvider.brandingPath(schoolId)
            val snapshot = db.collection(path).document("brand_profile").get().await()
            val data = snapshot.data
            if (data != null) {
                SchoolBrandingConfig.fromFirestoreMap("brand_profile", schoolId, data)
            } else {
                getDefaultBrandingForSchool(schoolId)
            }
        } catch (e: Exception) {
            Log.w("BrandingRepository", "Using pre-seeded tenant branding profile for $schoolId", e)
            getDefaultBrandingForSchool(schoolId)
        }
    }

    suspend fun saveBrandingConfig(config: SchoolBrandingConfig): Result<SchoolBrandingConfig> {
        return try {
            TenantPathProvider.verifyTenantBoundary(config.schoolId)
            val path = TenantPathProvider.brandingPath(config.schoolId)
            db.collection(path).document(config.id).set(config.toFirestoreMap()).await()
            BrandThemeController.applyBranding(config)
            Result.success(config)
        } catch (e: Exception) {
            Result.failure(ErrorHandler.sanitize(e))
        }
    }

    suspend fun resetBrandingConfig(
        schoolId: String = TenantPathProvider.requireActiveSchoolId()
    ): Result<SchoolBrandingConfig> {
        TenantPathProvider.verifyTenantBoundary(schoolId)
        val defaults = getDefaultBrandingForSchool(schoolId)
        return saveBrandingConfig(defaults)
    }

    fun getDefaultBrandingForSchool(schoolId: String): SchoolBrandingConfig {
        TenantPathProvider.verifyTenantBoundary(schoolId)
        val isOxford = schoolId.contains("oxford") || schoolId.startsWith("sch_oxf")
        return if (isOxford) {
            SchoolBrandingConfig(
                id = "brand_profile",
                schoolId = schoolId,
                schoolDisplayName = "Oxford International Academy",
                schoolCode = "OXF-101",
                motto = "Excellence Through Knowledge & Integrity",
                websiteUrl = "https://oxfordacademy.edu",
                supportEmail = "branding@oxfordacademy.edu",
                academicSession = "2026-2027",
                timezone = "America/New_York",
                currencyCode = "USD",
                currencySymbol = "$",
                palettePreset = BrandPalettePreset.OXFORD_NAVY,
                crestEmblem = CrestEmblemStyle.SHIELD_CLASSIC,
                cornerStyle = UiCornerStyle.BALANCED,
                layoutDensity = DashboardLayoutDensity.STANDARD,
                reportHeaderStyle = ReportCardHeaderStyle.CLASSIC_CREST,
                showSchoolCodeInHeader = true,
                showMottoOnReceipts = true,
                darkModeDefault = false,
                lastUpdatedBy = "Dr. Robert Vance",
                updatedAt = 1712620800000L
            )
        } else {
            SchoolBrandingConfig(
                id = "brand_profile",
                schoolId = schoolId,
                schoolDisplayName = "Cambridge High School",
                schoolCode = "CAM-204",
                motto = "Scientia, Virtus et Probitas",
                websiteUrl = "https://cambridgehigh.edu",
                supportEmail = "admin@cambridgehigh.edu",
                academicSession = "2026-2027",
                timezone = "Europe/London",
                currencyCode = "GBP",
                currencySymbol = "£",
                palettePreset = BrandPalettePreset.CAMBRIDGE_CRIMSON,
                crestEmblem = CrestEmblemStyle.LAUREL_STEM,
                cornerStyle = UiCornerStyle.SHARP,
                layoutDensity = DashboardLayoutDensity.EXECUTIVE,
                reportHeaderStyle = ReportCardHeaderStyle.MODERN_BANNER,
                showSchoolCodeInHeader = true,
                showMottoOnReceipts = true,
                darkModeDefault = false,
                lastUpdatedBy = "Prof. Eleanor Vance",
                updatedAt = 1712534400000L
            )
        }
    }
}
