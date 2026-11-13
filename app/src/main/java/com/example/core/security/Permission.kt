package com.example.core.security

/**
 * Granular permissions system for EduManage SaaS.
 * Enforces least-privilege security at the application and repository boundaries.
 */
enum class Permission(val key: String, val description: String) {
    // School & System Configuration
    SCHOOL_CONFIG_VIEW("school.config.view", "View school profile and settings"),
    SCHOOL_CONFIG_EDIT("school.config.edit", "Modify school settings and branding"),

    // Student Management
    STUDENTS_VIEW("students.view", "View students list and profiles"),
    STUDENTS_CREATE("students.create", "Enroll new students"),
    STUDENTS_UPDATE("students.update", "Edit student profiles"),
    STUDENTS_DELETE("students.delete", "Archive or remove students"),

    // Teacher & Staff Management
    TEACHERS_VIEW("teachers.view", "View teachers list and assignments"),
    TEACHERS_CREATE("teachers.create", "Add new teachers and staff"),
    TEACHERS_UPDATE("teachers.update", "Update teacher records and allocations"),
    TEACHERS_DELETE("teachers.delete", "Deactivate teacher accounts"),

    // Classes & Routine
    CLASSES_VIEW("classes.view", "View classes, sections, and subjects"),
    CLASSES_MANAGE("classes.manage", "Create and modify classes and schedules"),

    // Attendance
    ATTENDANCE_VIEW("attendance.view", "View attendance records"),
    ATTENDANCE_MARK("attendance.mark", "Take and submit attendance"),
    ATTENDANCE_EDIT("attendance.edit", "Modify finalized attendance entries"),

    // Fees & Finance
    FEES_VIEW("fees.view", "View fee schedules and invoices"),
    FEES_CREATE("fees.create", "Generate fee invoices"),
    FEES_COLLECT("fees.collect", "Record payments and generate receipts"),
    FEES_REPORTS("fees.reports", "Access financial analytics"),

    // Exams & Results
    EXAMS_VIEW("exams.view", "View examination schedules"),
    EXAMS_MANAGE("exams.manage", "Create exam timetables and grade rules"),
    RESULTS_VIEW("results.view", "View grades and report cards"),
    RESULTS_ENTER("results.enter", "Input exam scores and evaluations"),
    RESULTS_PUBLISH("results.publish", "Publish results to parents and students"),

    // Homework & Learning
    HOMEWORK_VIEW("homework.view", "View assignments"),
    HOMEWORK_CREATE("homework.create", "Assign homework and deadlines"),
    HOMEWORK_SUBMIT("homework.submit", "Submit assignment deliverables"),
    HOMEWORK_GRADE("homework.grade", "Evaluate and mark submissions"),

    // Notice & Announcements
    NOTICES_VIEW("notices.view", "Read school announcements"),
    NOTICES_PUBLISH("notices.publish", "Publish global or targeted notices"),

    // Library
    LIBRARY_VIEW("library.view", "Browse book inventory"),
    LIBRARY_MANAGE("library.manage", "Issue and return books, catalog assets"),

    // Transport
    TRANSPORT_VIEW("transport.view", "View vehicle routes and stops"),
    TRANSPORT_MANAGE("transport.manage", "Assign vehicles, drivers, and routes"),

    // Events & Academic Calendar
    EVENTS_VIEW("events.view", "View school events and academic calendar"),
    EVENTS_MANAGE("events.manage", "Create, reschedule, and cancel calendar events"),

    // Documents, Syllabi, ID Cards & Certificates
    DOCUMENTS_VIEW("documents.view", "View syllabi, handbooks, ID cards, and certificates"),
    DOCUMENTS_MANAGE("documents.manage", "Upload institutional documents and issue credentials"),

    // Leave Requests & Approval Workflow
    LEAVES_APPLY("leaves.apply", "Submit and view personal or child leave requests"),
    LEAVES_APPROVE("leaves.approve", "Review, approve, or decline school leave applications"),

    // Messaging
    MESSAGES_SEND("messages.send", "Send direct or group messages"),
    MESSAGES_VIEW("messages.view", "Access communication channels"),
    MESSAGES_MODERATE("messages.moderate", "Pin, archive, or remove communication threads"),

    // Executive Reports & Data Analytics
    ANALYTICS_VIEW("analytics.view", "View executive KPI dashboards and operational analytics"),
    ANALYTICS_EXPORT("analytics.export", "Generate, preview, and export executive reports");

    companion object {
        fun getPermissionsForRole(role: UserRole): Set<Permission> {
            return when (role) {
                UserRole.SUPER_ADMIN -> entries.toSet()

                UserRole.SCHOOL_ADMIN -> setOf(
                    SCHOOL_CONFIG_VIEW, SCHOOL_CONFIG_EDIT,
                    STUDENTS_VIEW, STUDENTS_CREATE, STUDENTS_UPDATE, STUDENTS_DELETE,
                    TEACHERS_VIEW, TEACHERS_CREATE, TEACHERS_UPDATE, TEACHERS_DELETE,
                    CLASSES_VIEW, CLASSES_MANAGE,
                    ATTENDANCE_VIEW, ATTENDANCE_MARK, ATTENDANCE_EDIT,
                    FEES_VIEW, FEES_CREATE, FEES_COLLECT, FEES_REPORTS,
                    EXAMS_VIEW, EXAMS_MANAGE, RESULTS_VIEW, RESULTS_ENTER, RESULTS_PUBLISH,
                    HOMEWORK_VIEW, HOMEWORK_CREATE, HOMEWORK_GRADE,
                    NOTICES_VIEW, NOTICES_PUBLISH,
                    LIBRARY_VIEW, LIBRARY_MANAGE,
                    TRANSPORT_VIEW, TRANSPORT_MANAGE,
                    EVENTS_VIEW, EVENTS_MANAGE,
                    DOCUMENTS_VIEW, DOCUMENTS_MANAGE,
                    LEAVES_APPLY, LEAVES_APPROVE,
                    MESSAGES_VIEW, MESSAGES_SEND, MESSAGES_MODERATE,
                    ANALYTICS_VIEW, ANALYTICS_EXPORT
                )

                UserRole.TEACHER -> setOf(
                    STUDENTS_VIEW,
                    CLASSES_VIEW,
                    ATTENDANCE_VIEW, ATTENDANCE_MARK,
                    EXAMS_VIEW, RESULTS_VIEW, RESULTS_ENTER,
                    HOMEWORK_VIEW, HOMEWORK_CREATE, HOMEWORK_GRADE,
                    NOTICES_VIEW,
                    EVENTS_VIEW,
                    DOCUMENTS_VIEW, DOCUMENTS_MANAGE,
                    LEAVES_APPLY, LEAVES_APPROVE,
                    MESSAGES_VIEW, MESSAGES_SEND,
                    ANALYTICS_VIEW
                )

                UserRole.ACCOUNTANT -> setOf(
                    STUDENTS_VIEW,
                    FEES_VIEW, FEES_CREATE, FEES_COLLECT, FEES_REPORTS,
                    NOTICES_VIEW,
                    EVENTS_VIEW,
                    DOCUMENTS_VIEW,
                    LEAVES_APPLY,
                    MESSAGES_VIEW, MESSAGES_SEND,
                    ANALYTICS_VIEW, ANALYTICS_EXPORT
                )

                UserRole.LIBRARIAN -> setOf(
                    STUDENTS_VIEW,
                    LIBRARY_VIEW, LIBRARY_MANAGE,
                    NOTICES_VIEW,
                    EVENTS_VIEW,
                    DOCUMENTS_VIEW,
                    LEAVES_APPLY,
                    MESSAGES_VIEW, MESSAGES_SEND,
                    ANALYTICS_VIEW
                )

                UserRole.TRANSPORT_MANAGER -> setOf(
                    STUDENTS_VIEW,
                    TRANSPORT_VIEW, TRANSPORT_MANAGE,
                    NOTICES_VIEW,
                    EVENTS_VIEW,
                    DOCUMENTS_VIEW,
                    LEAVES_APPLY,
                    MESSAGES_VIEW, MESSAGES_SEND,
                    ANALYTICS_VIEW
                )

                UserRole.STAFF -> setOf(
                    NOTICES_VIEW,
                    ATTENDANCE_VIEW,
                    EVENTS_VIEW,
                    DOCUMENTS_VIEW,
                    LEAVES_APPLY,
                    MESSAGES_VIEW, MESSAGES_SEND
                )

                UserRole.PARENT -> setOf(
                    STUDENTS_VIEW,
                    ATTENDANCE_VIEW,
                    FEES_VIEW,
                    RESULTS_VIEW,
                    HOMEWORK_VIEW,
                    NOTICES_VIEW,
                    TRANSPORT_VIEW,
                    EVENTS_VIEW,
                    DOCUMENTS_VIEW,
                    LEAVES_APPLY,
                    MESSAGES_VIEW, MESSAGES_SEND
                )

                UserRole.STUDENT -> setOf(
                    CLASSES_VIEW,
                    ATTENDANCE_VIEW,
                    RESULTS_VIEW,
                    HOMEWORK_VIEW, HOMEWORK_SUBMIT,
                    NOTICES_VIEW,
                    LIBRARY_VIEW,
                    TRANSPORT_VIEW,
                    EVENTS_VIEW,
                    DOCUMENTS_VIEW,
                    LEAVES_APPLY,
                    MESSAGES_VIEW, MESSAGES_SEND
                )
            }
        }
    }
}
