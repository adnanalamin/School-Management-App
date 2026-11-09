package com.example.core.config

/**
 * Environment configuration for EduManage SaaS platform.
 * Supports isolated execution profiles across Development, Staging, and Production.
 */
enum class EnvironmentType {
    DEVELOPMENT,
    STAGING,
    PRODUCTION
}

data class AppConfig(
    val environment: EnvironmentType = EnvironmentType.DEVELOPMENT,
    val appVersion: String = "1.0.0",
    val buildNumber: Int = 1,
    val apiTimeoutSeconds: Long = 30L,
    val isDebugLoggingEnabled: Boolean = true,
    val isMultiTenantStrictIsolationEnabled: Boolean = true,
    val allowOfflineSync: Boolean = true,
    val defaultPageSize: Int = 20,
    val defaultCurrency: String = "USD",
    val defaultTimezone: String = "UTC",
    val supportedLanguages: List<String> = listOf("en", "bn")
) {
    companion object {
        val current: AppConfig by lazy {
            AppConfig(
                environment = EnvironmentType.DEVELOPMENT,
                appVersion = "1.0.0-phase1",
                buildNumber = 101,
                isDebugLoggingEnabled = true,
                isMultiTenantStrictIsolationEnabled = true
            )
        }
    }
}
