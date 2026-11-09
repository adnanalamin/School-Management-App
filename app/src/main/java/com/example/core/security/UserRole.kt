package com.example.core.security

/**
 * All 9 primary user roles in the EduManage Multi-School SaaS platform.
 */
enum class UserRole(val displayName: String, val level: Int) {
    SUPER_ADMIN("Super Administrator", 100),
    SCHOOL_ADMIN("School Administrator", 80),
    TEACHER("Teacher / Faculty", 60),
    ACCOUNTANT("Accountant", 50),
    LIBRARIAN("Librarian", 50),
    TRANSPORT_MANAGER("Transport Manager", 50),
    STAFF("General Staff", 40),
    PARENT("Parent / Guardian", 20),
    STUDENT("Student", 10);

    companion object {
        fun fromString(role: String?): UserRole {
            return entries.firstOrNull { it.name.equals(role, ignoreCase = true) } ?: STUDENT
        }
    }
}
