package com.example.core.utils

import java.util.regex.Pattern

/**
 * Validation utilities for school registration, student profiles, and authentication.
 */
object ValidationUtils {
    private val EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"
    )

    private val SCHOOL_CODE_PATTERN = Pattern.compile(
        "^[A-Z0-9]{3,10}$"
    )

    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && EMAIL_PATTERN.matcher(email.trim()).matches()
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 8
    }

    fun isValidSchoolCode(code: String): Boolean {
        return code.isNotBlank() && SCHOOL_CODE_PATTERN.matcher(code.trim().uppercase()).matches()
    }

    fun isValidPhone(phone: String): Boolean {
        val digitsOnly = phone.filter { it.isDigit() }
        return digitsOnly.length in 7..15
    }

    fun isNotEmpty(value: String): Boolean {
        return value.trim().isNotEmpty()
    }
}
