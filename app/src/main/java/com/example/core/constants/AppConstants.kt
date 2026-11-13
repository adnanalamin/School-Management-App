package com.example.core.constants

/**
 * Global constants for EduManage SaaS.
 */
object AppConstants {
    const val APP_NAME = "EduManage"
    const val PRODUCT_TAGLINE = "Enterprise Multi-School SaaS Platform"
    
    // Multi-tenant header & context keys
    const val HEADER_TENANT_ID = "X-School-Tenant-ID"
    const val PREFS_KEY_ACTIVE_SCHOOL_ID = "active_school_id"
    const val PREFS_KEY_AUTH_TOKEN = "auth_session_token"
    const val PREFS_KEY_APP_THEME = "app_theme_mode"
    const val PREFS_KEY_APP_LOCALE = "app_locale"

    // Firestore root collections and paths
    const val COLLECTION_SCHOOLS = "schools"
    const val COLLECTION_GLOBAL_CONFIG = "global_config"
    const val COLLECTION_GLOBAL_USERS = "users"
    const val COLLECTION_SUBSCRIPTIONS = "subscriptions"

    // Sub-collections under schools/{schoolId}
    const val SUB_COLLECTION_STUDENTS = "students"
    const val SUB_COLLECTION_TEACHERS = "teachers"
    const val SUB_COLLECTION_PARENTS = "parents"
    const val SUB_COLLECTION_CLASSES = "classes"
    const val SUB_COLLECTION_SECTIONS = "sections"
    const val SUB_COLLECTION_ATTENDANCE = "attendance"
    const val SUB_COLLECTION_FEES = "fees"
    const val SUB_COLLECTION_EXAMS = "exams"
    const val SUB_COLLECTION_RESULTS = "results"
    const val SUB_COLLECTION_NOTICES = "notices"
    const val SUB_COLLECTION_HOMEWORK = "homework"
    const val SUB_COLLECTION_LIBRARY = "library"
    const val SUB_COLLECTION_TRANSPORT = "transport"
    const val SUB_COLLECTION_EVENTS = "events"
    const val SUB_COLLECTION_DOCUMENTS = "documents"
    const val SUB_COLLECTION_LEAVES = "leaves"
    const val SUB_COLLECTION_MESSAGES = "messages"
    const val SUB_COLLECTION_REPORTS = "reports"
    const val SUB_COLLECTION_BRANDING = "branding"

    // Pagination
    const val DEFAULT_PAGE_LIMIT = 20
    const val MAX_PAGE_LIMIT = 100
}
