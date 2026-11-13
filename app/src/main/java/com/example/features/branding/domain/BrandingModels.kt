package com.example.features.branding.domain

import com.example.core.tenant.TenantScopedEntity

/**
 * Curated institutional color themes supporting WCAG-friendly contrast in Light & Dark modes.
 */
enum class BrandPalettePreset(
    val displayName: String,
    val subtitle: String,
    val primaryColorHex: String,
    val secondaryColorHex: String,
    val tertiaryColorHex: String,
    val primaryColorLong: Long,
    val secondaryColorLong: Long,
    val tertiaryColorLong: Long,
    val primaryContainerLightLong: Long
) {
    OXFORD_NAVY(
        displayName = "Oxford Classic Navy",
        subtitle = "Indigo primary with maritime teal accents",
        primaryColorHex = "#1E3A8A",
        secondaryColorHex = "#0F766E",
        tertiaryColorHex = "#B45309",
        primaryColorLong = 0xFF1E3A8A,
        secondaryColorLong = 0xFF0F766E,
        tertiaryColorLong = 0xFFB45309,
        primaryContainerLightLong = 0xFFDBEAFE
    ),
    CAMBRIDGE_CRIMSON(
        displayName = "Cambridge Heritage Crimson",
        subtitle = "Regal burgundy paired with warm gold",
        primaryColorHex = "#991B1B",
        secondaryColorHex = "#B45309",
        tertiaryColorHex = "#1E3A8A",
        primaryColorLong = 0xFF991B1B,
        secondaryColorLong = 0xFFB45309,
        tertiaryColorLong = 0xFF1E3A8A,
        primaryContainerLightLong = 0xFFFEE2E2
    ),
    EMERALD_STEM(
        displayName = "Emerald STEM Academy",
        subtitle = "Forest green with sky-blue highlights",
        primaryColorHex = "#047857",
        secondaryColorHex = "#0369A1",
        tertiaryColorHex = "#D97706",
        primaryColorLong = 0xFF047857,
        secondaryColorLong = 0xFF0369A1,
        tertiaryColorLong = 0xFFD97706,
        primaryContainerLightLong = 0xFFD1FAE5
    ),
    ROYAL_SCHOLAR(
        displayName = "Royal Violet Scholar",
        subtitle = "Deep amethyst with crisp jade accents",
        primaryColorHex = "#5B21B6",
        secondaryColorHex = "#0D9488",
        tertiaryColorHex = "#C2410C",
        primaryColorLong = 0xFF5B21B6,
        secondaryColorLong = 0xFF0D9488,
        tertiaryColorLong = 0xFFC2410C,
        primaryContainerLightLong = 0xFFEDE9FE
    ),
    SLATE_EXECUTIVE(
        displayName = "Modern Slate & Amber",
        subtitle = "Architectural charcoal with warm amber",
        primaryColorHex = "#334155",
        secondaryColorHex = "#D97706",
        tertiaryColorHex = "#0F766E",
        primaryColorLong = 0xFF334155,
        secondaryColorLong = 0xFFD97706,
        tertiaryColorLong = 0xFF0F766E,
        primaryContainerLightLong = 0xFFE2E8F0
    ),
    PACIFIC_TEAL(
        displayName = "Pacific Maritime Teal",
        subtitle = "Deep ocean teal with cobalt accents",
        primaryColorHex = "#0F766E",
        secondaryColorHex = "#1D4ED8",
        tertiaryColorHex = "#B45309",
        primaryColorLong = 0xFF0F766E,
        secondaryColorLong = 0xFF1D4ED8,
        tertiaryColorLong = 0xFFB45309,
        primaryContainerLightLong = 0xFFCCFBF1
    );

    companion object {
        fun fromString(value: String?): BrandPalettePreset {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: OXFORD_NAVY
        }
    }
}

enum class CrestEmblemStyle(val displayName: String, val badgeLabel: String) {
    SHIELD_CLASSIC("Classic Crest Shield", "Heritage"),
    TORCH_ACADEMIC("Academic Torch", "Illumination"),
    LAUREL_STEM("Laurel Wreath", "Excellence"),
    COMPASS_GLOBAL("Global Compass", "International"),
    CROWN_HERITAGE("Sovereign Crown", "Distinction");

    companion object {
        fun fromString(value: String?): CrestEmblemStyle {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: SHIELD_CLASSIC
        }
    }
}

enum class UiCornerStyle(val displayName: String, val cardRadiusDp: Int, val buttonRadiusDp: Int) {
    SHARP("Architectural Crisp", 6, 4),
    BALANCED("Material Balanced", 12, 8),
    ROUNDED("Soft Pill Modern", 20, 14);

    companion object {
        fun fromString(value: String?): UiCornerStyle {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: BALANCED
        }
    }
}

enum class DashboardLayoutDensity(val displayName: String, val description: String) {
    COMPACT("Compact High-Density", "Maximizes visible rows & KPI tiles per screen"),
    STANDARD("Balanced Standard", "Recommended Material 3 spacing for daily workflows"),
    EXECUTIVE("Spacious Executive", "Generous breathing room for boardroom tablets");

    companion object {
        fun fromString(value: String?): DashboardLayoutDensity {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: STANDARD
        }
    }
}

enum class ReportCardHeaderStyle(val displayName: String, val subtitle: String) {
    CLASSIC_CREST("Classic Crest Letterhead", "Centered emblem, motto, and border trim"),
    MODERN_BANNER("Full-Width Branded Banner", "Primary color banner with school code pill"),
    MINIMAL_MONOCHROME("Clean Monochrome Audit", "High-contrast ink header for formal printing");

    companion object {
        fun fromString(value: String?): ReportCardHeaderStyle {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: CLASSIC_CREST
        }
    }
}

/**
 * Tenant-scoped School Customization & White-Label Branding Configuration stored in
 * `schools/{schoolId}/branding/{brandingId}`.
 */
data class SchoolBrandingConfig(
    override val id: String = "brand_profile",
    override val schoolId: String,
    val schoolDisplayName: String,
    val schoolCode: String,
    val motto: String = "",
    val websiteUrl: String = "",
    val supportEmail: String = "",
    val academicSession: String = "2026-2027",
    val timezone: String = "America/New_York",
    val currencyCode: String = "USD",
    val currencySymbol: String = "$",
    val palettePreset: BrandPalettePreset = BrandPalettePreset.OXFORD_NAVY,
    val crestEmblem: CrestEmblemStyle = CrestEmblemStyle.SHIELD_CLASSIC,
    val cornerStyle: UiCornerStyle = UiCornerStyle.BALANCED,
    val layoutDensity: DashboardLayoutDensity = DashboardLayoutDensity.STANDARD,
    val reportHeaderStyle: ReportCardHeaderStyle = ReportCardHeaderStyle.CLASSIC_CREST,
    val showSchoolCodeInHeader: Boolean = true,
    val showMottoOnReceipts: Boolean = true,
    val darkModeDefault: Boolean = false,
    val lastUpdatedBy: String = "School Admin",
    val updatedAt: Long = System.currentTimeMillis()
) : TenantScopedEntity {

    val isDefaultTheme: Boolean
        get() = palettePreset == BrandPalettePreset.OXFORD_NAVY && cornerStyle == UiCornerStyle.BALANCED

    val formattedBrandSummary: String
        get() = "${palettePreset.displayName} • ${crestEmblem.displayName} • ${cornerStyle.displayName}"

    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "id" to id,
        "schoolId" to schoolId,
        "schoolDisplayName" to schoolDisplayName,
        "schoolCode" to schoolCode,
        "motto" to motto,
        "websiteUrl" to websiteUrl,
        "supportEmail" to supportEmail,
        "academicSession" to academicSession,
        "timezone" to timezone,
        "currencyCode" to currencyCode,
        "currencySymbol" to currencySymbol,
        "palettePreset" to palettePreset.name,
        "crestEmblem" to crestEmblem.name,
        "cornerStyle" to cornerStyle.name,
        "layoutDensity" to layoutDensity.name,
        "reportHeaderStyle" to reportHeaderStyle.name,
        "showSchoolCodeInHeader" to showSchoolCodeInHeader,
        "showMottoOnReceipts" to showMottoOnReceipts,
        "darkModeDefault" to darkModeDefault,
        "lastUpdatedBy" to lastUpdatedBy,
        "updatedAt" to updatedAt
    )

    companion object {
        fun fromFirestoreMap(
            id: String,
            schoolId: String,
            data: Map<String, Any?>
        ): SchoolBrandingConfig {
            return SchoolBrandingConfig(
                id = id,
                schoolId = schoolId,
                schoolDisplayName = data["schoolDisplayName"] as? String ?: "",
                schoolCode = data["schoolCode"] as? String ?: "",
                motto = data["motto"] as? String ?: "",
                websiteUrl = data["websiteUrl"] as? String ?: "",
                supportEmail = data["supportEmail"] as? String ?: "",
                academicSession = data["academicSession"] as? String ?: "2026-2027",
                timezone = data["timezone"] as? String ?: "UTC",
                currencyCode = data["currencyCode"] as? String ?: "USD",
                currencySymbol = data["currencySymbol"] as? String ?: "$",
                palettePreset = BrandPalettePreset.fromString(data["palettePreset"] as? String),
                crestEmblem = CrestEmblemStyle.fromString(data["crestEmblem"] as? String),
                cornerStyle = UiCornerStyle.fromString(data["cornerStyle"] as? String),
                layoutDensity = DashboardLayoutDensity.fromString(data["layoutDensity"] as? String),
                reportHeaderStyle = ReportCardHeaderStyle.fromString(data["reportHeaderStyle"] as? String),
                showSchoolCodeInHeader = data["showSchoolCodeInHeader"] as? Boolean ?: true,
                showMottoOnReceipts = data["showMottoOnReceipts"] as? Boolean ?: true,
                darkModeDefault = data["darkModeDefault"] as? Boolean ?: false,
                lastUpdatedBy = data["lastUpdatedBy"] as? String ?: "School Admin",
                updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}
